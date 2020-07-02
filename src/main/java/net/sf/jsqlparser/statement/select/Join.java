/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

public class Join extends ASTNodeAccessImpl {

    private boolean outer = false;
    private boolean right = false;
    private boolean left = false;
    private boolean natural = false;
    private boolean full = false;
    private boolean inner = false;
    private boolean simple = false;
    private boolean cross = false;
    private boolean semi = false;
    private boolean straight = false;
    private boolean apply = false;
    private FromItem rightItem;
    private Expression onExpression;
    private List<Column> usingColumns;
    private KSQLJoinWindow joinWindow;

    public boolean isSimple() {
        return simple;
    }

    public Join simple(boolean b) {
        setSimple(b);
        return this;
    }

    public void setSimple(boolean b) {
        simple = b;
    }

    public boolean isInner() {
        return inner;
    }

    public Join inner(boolean b) {
        setInner(b);
        return this;
    }

    public void setInner(boolean b) {
        inner = b;
    }

    public boolean isStraight() {
        return straight;
    }

    public Join straight(boolean b) {
        setStraight(b);
        return this;
    }

    public void setStraight(boolean b) {
        straight = b;
    }

    /**
     * Whether is a "OUTER" join
     *
     * @return true if is a "OUTER" join
     */
    public boolean isOuter() {
        return outer;
    }

    public Join outer(boolean b) {
        setOuter(b);
        return this;
    }

    public void setOuter(boolean b) {
        outer = b;
    }

    public boolean isApply() {
        return apply;
    }

    public Join apply(boolean apply) {
        setApply(apply);
        return this;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }

    /**
     * Whether is a "SEMI" join
     *
     * @return true if is a "SEMI" join
     */
    public boolean isSemi() {
        return semi;
    }

    public Join semi(boolean b) {
        setSemi(b);
        return this;
    }

    public void setSemi(boolean b) {
        semi = b;
    }

    /**
     * Whether is a "LEFT" join
     *
     * @return true if is a "LEFT" join
     */
    public boolean isLeft() {
        return left;
    }

    public Join left(boolean b) {
        setLeft(b);
        return this;
    }

    public void setLeft(boolean b) {
        left = b;
    }

    /**
     * Whether is a "RIGHT" join
     *
     * @return true if is a "RIGHT" join
     */
    public boolean isRight() {
        return right;
    }

    public Join right(boolean b) {
        setRight(b);
        return this;
    }

    public void setRight(boolean b) {
        right = b;
    }

    /**
     * Whether is a "NATURAL" join
     *
     * @return true if is a "NATURAL" join
     */
    public boolean isNatural() {
        return natural;
    }

    public Join natural(boolean b) {
        setNatural(b);
        return this;
    }

    public void setNatural(boolean b) {
        natural = b;
    }

    /**
     * Whether is a "FULL" join
     *
     * @return true if is a "FULL" join
     */
    public boolean isFull() {
        return full;
    }

    public Join full(boolean b) {
        setFull(b);
        return this;
    }

    public void setFull(boolean b) {
        full = b;
    }

    public boolean isCross() {
        return cross;
    }

    public Join cross(boolean cross) {
        setCross(cross);
        return this;
    }

    public void setCross(boolean cross) {
        this.cross = cross;
    }

    /**
     * Returns the right item of the join
     */
    public FromItem getRightItem() {
        return rightItem;
    }

    public Join rightItem(FromItem item) {
        setRightItem(item);
        return this;
    }

    public void setRightItem(FromItem item) {
        rightItem = item;
    }

    /**
     * Returns the "ON" expression (if any)
     */
    public Expression getOnExpression() {
        return onExpression;
    }

    public Join onExpression(Expression expression) {
        setOnExpression(expression);
        return this;
    }

    public void setOnExpression(Expression expression) {
        onExpression = expression;
    }

    /**
     * Returns the "USING" list of {@link net.sf.jsqlparser.schema.Column}s (if any)
     */
    public List<Column> getUsingColumns() {
        return usingColumns;
    }

    public Join usingColumns(List<Column> list) {
        setUsingColumns(list);
        return this;
    }

    public void setUsingColumns(List<Column> list) {
        usingColumns = list;
    }

    public boolean isWindowJoin() {
        return joinWindow != null;
    }

    /**
     * Return the "WITHIN" join window (if any)
     */
    public KSQLJoinWindow getJoinWindow() {
        return joinWindow;
    }

    public Join joinWindow(KSQLJoinWindow joinWindow) {
        setJoinWindow(joinWindow);
        return this;
    }

    public void setJoinWindow(KSQLJoinWindow joinWindow) {
        this.joinWindow = joinWindow;
    }

    @Override
    public String toString() {
        if (isSimple() && isOuter()) {
            return "OUTER " + rightItem;
        } else if (isSimple()) {
            return "" + rightItem;
        } else {
            String type = "";

            if (isRight()) {
                type += "RIGHT ";
            } else if (isNatural()) {
                type += "NATURAL ";
            } else if (isFull()) {
                type += "FULL ";
            } else if (isLeft()) {
                type += "LEFT ";
            } else if (isCross()) {
                type += "CROSS ";
            }

            if (isOuter()) {
                type += "OUTER ";
            } else if (isInner()) {
                type += "INNER ";
            } else if (isSemi()) {
                type += "SEMI ";
            }

            if (isStraight()) {
                type = "STRAIGHT_JOIN ";
            } else if (isApply()) {
                type += "APPLY ";
            } else {
                type += "JOIN ";
            }

            return type + rightItem + ((joinWindow != null) ? " WITHIN " + joinWindow : "")
                    + ((onExpression != null) ? " ON " + onExpression + "" : "")
                    + PlainSelect.getFormatedList(usingColumns, "USING", true, true);
        }

    }
    
    public static Join create() {
        return new Join();
    }

    public Join addUsingColumns(Column... usingColumns) {
        List<Column> collection = Optional.ofNullable(getUsingColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, usingColumns);
        return this.usingColumns(collection);
    }

    public Join addUsingColumns(Collection<? extends Column> usingColumns) {
        List<Column> collection = Optional.ofNullable(getUsingColumns()).orElseGet(ArrayList::new);
        collection.addAll(usingColumns);
        return this.usingColumns(collection);
    }
}
