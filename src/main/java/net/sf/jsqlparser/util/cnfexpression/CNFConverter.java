/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.cnfexpression;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;

/**
 * This class handles the conversion from a normal expression tree into the CNF form.
 * <p>
 * Here is the definition of CNF form: https://en.wikipedia.org/wiki/Conjunctive_normal_form
 * <p>
 * Basically it will follow these steps:
 * <p>
 * To help understanding, I will generate an example: Here is the original tree: OR / \ OR NOT / \ |
 * NOT H AND | / \ NOT G OR | / \ F H NOT | OR / \ AND L / \ ( ) ( ) | | J K
 * <p>
 * 1. rebuild the tree by replacing the "and" and "or" operators (which are binary) into their
 * counterparts node that could hold multiple elements. Also, leave out the parenthesis node between
 * the conditional operators to make the tree uniform.
 * <p>
 * After the transform, the result should be like this: OR(M) / \ OR(M) NOT / \ | NOT H AND(M) | / \
 * NOT G OR(M) | / \ F H NOT | OR(M) / \ AND(M) L / \ J K
 * <p>
 * 2. push the not operators into the bottom of the expression. That means the not operator will be
 * the root of the expression tree where no "and" or "or" exists. Be sure use the De Morgan's law
 * and double not law.
 * <p>
 * How to use De Morgan law: For example, here is the original expression tree: NOT | AND(M) / \ G H
 * <p>
 * After we use the De Morgan law, the result should be like this: OR(M) / \ NOT NOT | | G H
 * <p>
 * After the transform, the result should be like this: OR(M) / \ OR(M) OR(M) / \ / \ F H NOT AND(M)
 * | / \ G NOT OR(M) | / \ H AND(M) L / \ J K
 * <p>
 * 3. gather all the adjacent "and" or "or" operator together. After doing that, the expression tree
 * will be presented as: all the and expression will be in either odd or even levels, this will be
 * the same for the or operator.
 * <p>
 * After the transform, the expression tree should be like this: OR(M) / / \ \ F H NOT AND(M) | / \
 * G NOT OR(M) | / \ H AND(M) L / \ J K
 * <p>
 * 4. push the and operator upwards until the root is an and operator and all the children are or
 * operators with multiple components. At this time we get the result: an expression in CNF form.
 * How do we push and up? Use distribution law!
 * <p>
 * For example, here is the way to push the and up and merge them. OR / \ AND L / \ J K
 * <p>
 * In the normal form, it could be: (J AND K) OR L. If we apply the distribution law, we will get
 * the result like this: (J OR L) AND (K OR L), the tree form of this should be like: AND / \ OR OR
 * / \ / \ J L K L
 * <p>
 * So after we push the AND at the deepest level up and merge it with the existing add, we get this
 * result. OR(M) / / \ \ F H NOT AND(M) | / | \ G NOT OR(M) OR(M) | / \ / \ H J L K L
 * <p>
 * Now let us push the and up and we will get the result like this: AND(M) / | \ OR(M) OR(M) OR(M) /
 * / \ \ / / | \ \ / / | \ \ F H NOT NOT F H NOT J L F H NOT K L | | | | G H G G
 * <p>
 * 5. The last step, convert the Multiple Expression back to the binary form. Note the final tree
 * shall be left-inclined.
 * <p>
 * The final expression tree shall be like this: AND / \ AND ( ) / \ | ( ) ( ) part1 | | OR part2 /
 * \ OR NOT / \ | OR NOT H / \ | F H G
 * <p>
 * part1: OR / \ OR L / \ OR K / \ OR NOT / \ | F H G
 * <p>
 * part2: OR / \ OR L / \ OR J / \ OR NOT / \ | F H G
 *
 * @author messfish
 */
public class CNFConverter {

    private Expression root;
    // the variable that stores the newly generated root.
    private Expression dummy;
    // this variable mainly serves as the dummy root of the true root.
    // generally it will be a multi and operator with root as the child.
    private Expression temp1;
    private Expression temp2;
    private Expression child;
    // these two variable mainly serves as nodes that traverse through
    // the expression tree to change the structure of expression tree.
    // notice temp1 will be settled as the root and temp2 will be
    // settled as the dummy root.
    private boolean isUsed = false;

    public static Expression convertToCNF(Expression expr) {
        CNFConverter cnf = new CNFConverter();
        return cnf.convert(expr);
    }

    /**
     * this method takes an expression tree and converts that into a CNF form. Notice the 5 steps
     * shown above will turn into 5 different methods. For the sake of testing, I set them public.
     * return the converted expression.
     *
     * @param express the original expression tree.
     */
    private Expression convert(Expression express)
            throws IllegalStateException {
        if (isUsed) {
            throw new IllegalStateException("The class could only be used once!");
        } else {
            isUsed = true;
        }
        reorder(express);
        pushNotDown();
        /*
         * notice for the gather() function, we do not change the variable that points to the root
         * by pointing to others. Also, we do not change those temp variables. So there is no need
         * to set those variables back to their modified state.
         */
        gather();
        pushAndUp();
        changeBack();
        return root;
    }

    /**
     * this is the first step that rebuild the expression tree. Use the standard specified in the
     * above class. Traverse the original tree recursively and rebuild the tree from that.
     *
     * @param express the original expression tree.
     */
    private void reorder(Expression express) {
        root = CloneHelper.modify(express);
        List<Expression> list = new ArrayList<Expression>();
        list.add(root);
        dummy = new MultiAndExpression(list);
    }

    /**
     * This method is used to deal with pushing not operators down. Since it needs an extra
     * parameter, I will create a new method to handle this.
     */
    private void pushNotDown() {
        /* set the two temp parameters to their staring point. */
        temp1 = root;
        temp2 = dummy;
        /*
         * I set it to zero since if the modification happens at the root, the parent will have the
         * correct pointer to the children.
         */
        pushNot(0);
        /* do not forget to set the operators back! */
        root = ((MultiAndExpression) dummy).getChild(0);
        temp1 = root;
        temp2 = dummy;
    }

    /**
     * This method is the helper function to push not operators down. traverse the tree thoroughly,
     * when we meet the not operator. We only need to consider these three operators:
     * MultiAndOperator, MultiOrOperator, NotOperator. Handle them in a seperate way. when we finish
     * the traverse, the expression tree will have all the not operators pushed as downwards as they
     * could. In the method, I use two global variables: temp1 and temp2 to traverse the expression
     * tree. Notice that temp2 will always be the parent of temp1.
     *
     * @param index the index of the children appeared in parents array.
     */
    private void pushNot(int index) {
        /*
         * what really matters is the three logical operators: and, or, not. so we only deal with
         * these three operators.
         */
        if (temp1 instanceof MultiAndExpression) {
            MultiAndExpression and = (MultiAndExpression) temp1;
            for (int i = 0; i < and.size(); i++) {
                temp2 = and;
                temp1 = and.getChild(i);
                pushNot(i);
            }
        } else if (temp1 instanceof MultiOrExpression) {
            MultiOrExpression or = (MultiOrExpression) temp1;
            for (int i = 0; i < or.size(); i++) {
                temp2 = or;
                temp1 = or.getChild(i);
                pushNot(i);
            }
        } else if (temp1 instanceof NotExpression) {
            handleNot(index);
        }
    }

    /**
     * This function mainly deals with pushing not operators down. check the child. If it is not a
     * logic operator(and or or). stop at that point. Else use De Morgan law to push not downwards.
     *
     * @param index the index of the children appeared in parents array.
     */
    private void handleNot(int index) {
        child = ((NotExpression) temp1).getExpression();
        int nums = 1; // takes down the number of not operators.
        while (child instanceof NotExpression) {
            child = ((NotExpression) child).getExpression();
            nums++;
        }
        /*
         * if the number of not operators are even. we could get rid of all the not operators. set
         * the child to the parent.
         */
        if (nums % 2 == 0) {
            ((MultipleExpression) temp2).setChild(index, child);
            temp1 = child;
            pushNot(-1);
        } else {
            /*
             * otherwise there will be one not left to push. if the child is not these two types of
             * operators. that means we reach the leaves of the logical part. set a new not operator
             * whose child is the current one and connect that operator with the parent and return.
             */
            if (!(child instanceof MultiAndExpression)
                    && !(child instanceof MultiOrExpression)) {
                // if (child instanceof LikeExpression) {
                // ((LikeExpression) child).setNot();
                // } else if (child instanceof BinaryExpression) {
                // ((BinaryExpression) child).setNot();
                // } else {
                child = new NotExpression(child);
                // }
                ((MultipleExpression) temp2).setChild(index, child);
                // return;
            } else if (child instanceof MultiAndExpression) {
                MultiAndExpression and = (MultiAndExpression) child;
                List<Expression> list = new ArrayList<Expression>();
                for (int i = 0; i < and.size(); i++) {
                    /* push not to every element in the operator. */
                    NotExpression not = new NotExpression(and.getChild(i));
                    list.add(not);
                }
                /* the De Morgan law shows we need to change and to or. */
                temp1 = new MultiOrExpression(list);
                ((MultipleExpression) temp2).setChild(index, temp1);
                pushNot(-1);
            } else if (child instanceof MultiOrExpression) {
                MultiOrExpression or = (MultiOrExpression) child;
                List<Expression> list = new ArrayList<Expression>();
                for (int i = 0; i < or.size(); i++) {
                    /* push not to every element in the operator. */
                    NotExpression not = new NotExpression(or.getChild(i));
                    list.add(not);
                }
                /* the De Morgan law shows we need to change or to and. */
                temp1 = new MultiAndExpression(list);
                ((MultipleExpression) temp2).setChild(index, temp1);
                pushNot(-1);
            }
        }
    }

    /**
     * This method serves as dealing with the third step. It is used to put all the adjacent same
     * multi operators together. BFS the tree and do it node by node. In the end we will get the
     * tree where all the same multi operators store in the same odd level of the tree or in the
     * same even level of the tree.
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private void gather() {
        Queue<Expression> queue = new LinkedList<Expression>();
        queue.offer(temp1);
        while (!queue.isEmpty()) {
            Expression express = queue.poll();
            /*
             * at this level, we only deal with "multi and" and "multi or" operators, so we only
             * consider these two operators. that means we do nothing if the operator is not those
             * two.
             */
            if (express instanceof MultiAndExpression) {
                MultiAndExpression and = (MultiAndExpression) express;
                while (true) {
                    int index = 0;
                    Expression get = null;
                    for (; index < and.size(); index++) {
                        get = and.getChild(index);
                        if (get instanceof MultiAndExpression) {
                            break;
                        }
                    }
                    /*
                     * if the index is the size of the multi operator, that means this is already
                     * valid. jump out of the loop.
                     */
                    if (index == and.size()) {
                        break;
                    } else {
                        /*
                         * if not, remove the child out and push the child of that child in the
                         * operator, starting from the index where the child is removed.
                         */
                        and.removeChild(index);
                        MultipleExpression order = (MultipleExpression) get;
                        for (int i = 0; i < order.size(); i++) {
                            and.addChild(index, order.getChild(i));
                            index++;
                        }
                    }
                }
                /* Do the standard BFS now since all children are not and operators. */
                for (int i = 0; i < and.size(); i++) {
                    queue.offer(and.getChild(i));
                }
            } else if (express instanceof MultiOrExpression) {
                /* for the multi or operator, the logic is the similar. */
                MultiOrExpression or = (MultiOrExpression) express;
                while (true) {
                    int index = 0;
                    Expression get = null;
                    for (; index < or.size(); index++) {
                        get = or.getChild(index);
                        if (get instanceof MultiOrExpression) {
                            break;
                        }
                    }
                    /*
                     * if the index is the size of the multi operator, that means this is already
                     * valid. jump out of the loop.
                     */
                    if (index == or.size()) {
                        break;
                    } else {
                        /*
                         * if not, remove the child out and push the child of that child in the
                         * operator, starting from the index where the child is removed.
                         */
                        or.removeChild(index);
                        MultipleExpression order = (MultipleExpression) get;
                        for (int i = 0; i < order.size(); i++) {
                            or.addChild(index, order.getChild(i));
                            index++;
                        }
                    }
                }
                /* Do the standard BFS now since all children are not or operators. */
                for (int i = 0; i < or.size(); i++) {
                    queue.offer(or.getChild(i));
                }
            }
        }
    }

    /**
     * First, BFS the tree and gather all the or operators and their parents into a stack. Next, pop
     * them out and push the and operators under the or operators upwards(if there are). Do this
     * level by level, which means during each level we will call the gather() method to make the
     * tree uniform. When we move out of the stack. The expression tree shall be in CNF form.
     */
    private void pushAndUp() {
        Queue<Mule> queue = new LinkedList<Mule>();
        Stack<Mule> stack = new Stack<Mule>();
        Mule root = new Mule(temp2, temp1, 0);
        queue.offer(root);
        int level = 1;
        /*
         * do the BFS and store valid mule into the stack. Notice the first parameter is parent and
         * the second parameter is children.
         */
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Mule mule = queue.poll();
                Expression parent = mule.parent;
                Expression child = mule.child;
                if (parent instanceof MultiAndExpression
                        && child instanceof MultiOrExpression) {
                    stack.push(mule);
                }
                /* Note the child may not be an instance of multiple expression!. */
                if (child instanceof MultipleExpression) {
                    MultipleExpression multi = (MultipleExpression) child;
                    for (int j = 0; j < multi.size(); j++) {
                        Expression get = multi.getChild(j);
                        if (get instanceof MultipleExpression) {
                            Mule added = new Mule(child, get, level);
                            queue.offer(added);
                        }
                    }
                }
            }
            level++;
        }
        /* use another function to handle pushing and up. */
        pushAnd(stack);
        /* do not forget to set the operators back! */
        this.root = ((MultiAndExpression) dummy).getChild(0);
        temp1 = this.root;
        temp2 = dummy;
        /*
         * at last, remember to gather again since there are no gather() method called if there are
         * some movements on the root.
         */
        gather();
    }

    /**
     * This helper function is used to deal with pushing and up: generally, pop the top element out
     * of the stack, use BFS to traverse the tree and push and up. It will case the expression tree
     * to have the and as the new root and multiple or as the children. Push them on the queue and
     * repeat the same process until the newly generated or operator does not have any and operators
     * in it(which means no elements will be added into the queue). when one level is finished,
     * regroup the tree. Do this until the stack is empty, the result will be the expression in CNF
     * form.
     *
     * @param stack the stack stores a list of combined data.
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private void pushAnd(Stack<Mule> stack) {
        int level = 0;
        if (!stack.isEmpty()) {
            level = stack.peek().level;
        }
        while (!stack.isEmpty()) {
            Mule mule = stack.pop();
            /* we finish a level, uniform the tree by calling gather. */
            if (level != mule.level) {
                gather();
                level = mule.level;
            }
            Queue<Mule> queue = new LinkedList<Mule>();
            /*
             * this time we do not need to take down the level of the tree, so simply set a 0 to the
             * last parameter.
             */
            Mule combined = new Mule(mule.parent, mule.child, 0);
            queue.offer(combined);
            while (!queue.isEmpty()) {
                Mule get = queue.poll();
                Expression parent = get.parent;
                Expression child = get.child;
                /*
                 * based on the code above, the stack only have the expression which they are multi
                 * operators. so safely convert them.
                 */
                MultipleExpression children = (MultipleExpression) child;
                int index = 0;
                MultiAndExpression and = null;
                /* find the children that the child is an multi and operator. */
                for (; index < children.size(); index++) {
                    if (children.getChild(index) instanceof MultiAndExpression) {
                        and = (MultiAndExpression) children.getChild(index);
                        break;
                    }
                }
                if (index == children.size() || and == null) {
                    continue;
                }
                children.removeChild(index);
                MultipleExpression parents = (MultipleExpression) parent;
                List<Expression> list = new ArrayList<Expression>();
                MultiAndExpression newand = new MultiAndExpression(list);
                parents.setChild(parents.getIndex(children), newand);
                for (int i = 0; i < and.size(); i++) {
                    Expression temp = CloneHelper.shallowCopy(children);
                    MultipleExpression mtemp = (MultipleExpression) temp;
                    mtemp.addChild(mtemp.size(), and.getChild(i));
                    newand.addChild(i, mtemp);
                    queue.offer(new Mule(newand, mtemp, 0));
                }
            }
        }
    }

    /**
     * This is the final step of the CNF conversion: now we have the Expression tree that has one
     * multiple and expression with a list of multiple or expression as the child. So we need to
     * convert the multiple expression back to the binary counterparts. Note the converted tree is
     * left inclined. Also I attach a parenthesis node before the or expression that is attached to
     * the and expression to make the generated result resembles the CNF form.
     */
    private void changeBack() {
        if (!(root instanceof MultiAndExpression)) {
            return;
        }
        MultipleExpression temp = (MultipleExpression) root;
        for (int i = 0; i < temp.size(); i++) {
            temp.setChild(i, CloneHelper.changeBack(true, temp.getChild(i)));
        }
        root = CloneHelper.changeBack(false, temp);
    }

    private class Mule {

        private Expression parent;
        private Expression child;
        private int level;

        private Mule(Expression parent, Expression child, int level) {
            this.parent = parent;
            this.child = child;
            this.level = level;
        }
    }

}
