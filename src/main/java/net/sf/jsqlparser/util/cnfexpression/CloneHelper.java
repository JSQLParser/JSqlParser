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
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;

/**
 * This class is mainly used for handling the cloning of an expression tree.
 * Note this is the shallow copy of the tree. That means I do not modify
 * or copy the expression other than these expressions:
 * AND, OR, NOT, (), MULTI-AND, MULTI-OR.
 * Since the CNF conversion only change the condition part of the tree.
 *
 * @author messfish
 *
 */
class CloneHelper {

    public Expression modify(Expression express) {
        if (express instanceof NotExpression) {
            return new NotExpression(modify(((NotExpression) express).getExpression()));
        }
        if (express instanceof Parenthesis) {
            Parenthesis parenthesis = (Parenthesis) express;
            Expression result = modify(parenthesis.getExpression());
            return result;
        }
        if (express instanceof AndExpression) {
            AndExpression and = (AndExpression) express;
            List<Expression> list = new ArrayList<Expression>();
            list.add(modify(and.getLeftExpression()));
            list.add(modify(and.getRightExpression()));
            MultiAndExpression result = new MultiAndExpression(list);
//            if (and.isNot()) {
//                return new NotExpression(result);
//            }
            return result;
        }
        if (express instanceof OrExpression) {
            OrExpression or = (OrExpression) express;
            List<Expression> list = new ArrayList<Expression>();
            list.add(modify(or.getLeftExpression()));
            list.add(modify(or.getRightExpression()));
            MultiOrExpression result = new MultiOrExpression(list);
//            if (or.isNot()) {
//                return new NotExpression(result);
//            }
            return result;
        }
//        if (express instanceof BinaryExpression) {
//            BinaryExpression binary = (BinaryExpression) express;
//            if (binary.isNot()) {
//                binary.removeNot();
//                return new NotExpression(modify(binary));
//            }
//        }
        return express;
    }

    /**
     * This method is used to copy the expression which happens at step four. I only copy the
     * conditional expressions since the CNF only changes the conditional part.
     *
     * @param express the expression that will be copied.
     * @return the copied expression.
     */
    public Expression shallowCopy(Expression express) {
        if (express instanceof MultipleExpression) {
            MultipleExpression multi = (MultipleExpression) express;
            List<Expression> list = new ArrayList<Expression>();
            for (int i = 0; i < multi.size(); i++) {
                list.add(shallowCopy(multi.getChild(i)));
            }
            if (express instanceof MultiAndExpression) {
                return new MultiAndExpression(list);
            }
            /* since there only two possibilities of the multiple expression,
             * so after the if condition, it is certain this is a multi-or. */
            return new MultiOrExpression(list);
        }
        return express;
    }

    /**
     * This helper method is used to change the multiple expression into the binary form,
     * respectively and return the root of the expression tree.
     *
     * @param isMultiOr variable tells whether the expression is or.
     * @param exp the expression that needs to be converted.
     * @return the root of the expression tree.
     */
    public Expression changeBack(Boolean isMultiOr, Expression exp) {
        if (!(exp instanceof MultipleExpression)) {
            return exp;
        }
        MultipleExpression changed = (MultipleExpression) exp;
        Expression result = changed.getChild(0);
        for (int i = 1; i < changed.size(); i++) {
            Expression left = result;
            Expression right = changed.getChild(i);
            if (isMultiOr) {
                result = new OrExpression(left, right);
            } else {
                result = new AndExpression(left, right);
            }
        }
        if (isMultiOr) {
            return new Parenthesis(result);
        }
        return result;
    }

}
