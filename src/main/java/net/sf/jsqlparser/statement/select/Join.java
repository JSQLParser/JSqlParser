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

import java.util.*;

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
    private final LinkedList<Expression> onExpressions = new LinkedList<>();
    private final LinkedList<Column> usingColumns = new LinkedList<>();
    private KSQLJoinWindow joinWindow;

    public boolean isSimple() {
        return simple;
    }

    public Join withSimple(boolean b) {
        this.setSimple(b);
        return this;
    }

    public void setSimple(boolean b) {
        simple = b;
    }

    public boolean isInner() {
        return inner;
    }

    public Join withInner(boolean b) {
        this.setInner(b);
        return this;
    }

    public void setInner(boolean b) {
        inner = b;
    }

    public boolean isStraight() {
        return straight;
    }

    public Join withStraight(boolean b) {
        this.setStraight(b);
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

    public Join withOuter(boolean b) {
        this.setOuter(b);
        return this;
    }

    public void setOuter(boolean b) {
        outer = b;
    }

    public boolean isApply() {
        return apply;
    }

    public Join withApply(boolean apply) {
        this.setApply(apply);
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

    public Join withSemi(boolean b) {
        this.setSemi(b);
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

    public Join withLeft(boolean b) {
        this.setLeft(b);
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

    public Join withRight(boolean b) {
        this.setRight(b);
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

    public Join withNatural(boolean b) {
        this.setNatural(b);
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

    public Join withFull(boolean b) {
        this.setFull(b);
        return this;
    }

    public void setFull(boolean b) {
        full = b;
    }

    public boolean isCross() {
        return cross;
    }

    public Join withCross(boolean cross) {
        this.setCross(cross);
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

    public Join withRightItem(FromItem item) {
        this.setRightItem(item);
        return this;
    }

    public void setRightItem(FromItem item) {
        rightItem = item;
    }

    /**
     * Returns the "ON" expression (if any)
     */
    @Deprecated
    public Expression getOnExpression() {
        return onExpressions.get(0);
    }

    public Collection<Expression> getOnExpressions() {
        return onExpressions;
    }

    @Deprecated
    public Join withOnExpression(Expression expression) {
        this.setOnExpression(expression);
        return this;
    }

    @Deprecated
    public void setOnExpression(Expression expression) {
        onExpressions.add(0, expression);
    }

    public Join addOnExpression(Expression expression) {
        onExpressions.add(expression);
        return this;
    }

    public Join setOnExpressions(Collection<Expression> expressions) {
        onExpressions.clear();
        onExpressions.addAll(expressions);
        return this;
    }

    /**
     * Returns the "USING" list of {@link net.sf.jsqlparser.schema.Column}s (if any)
     */
    public List<Column> getUsingColumns() {
        return usingColumns;
    }

    public Join withUsingColumns(List<Column> list) {
        this.setUsingColumns(list);
        return this;
    }

    public void setUsingColumns(List<Column> list) {
        usingColumns.clear();
        usingColumns.addAll(list);
    }

    public boolean isWindowJoin() {
        return joinWindow != null;
    }

    /**
     * Return the "WITHIN" join window (if any)
     * @return 
     */
    public KSQLJoinWindow getJoinWindow() {
        return joinWindow;
    }

    public Join withJoinWindow(KSQLJoinWindow joinWindow) {
        this.setJoinWindow(joinWindow);
        return this;
    }

    public void setJoinWindow(KSQLJoinWindow joinWindow) {
        this.joinWindow = joinWindow;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (isSimple() && isOuter()) {
            builder.append("OUTER ").append(rightItem);
        } else if (isSimple()) {
            builder.append(rightItem);
        } else {
            if (isRight()) {
                builder.append("RIGHT ");
            } else if (isNatural()) {
                builder.append("NATURAL ");
            } else if (isFull()) {
                builder.append("FULL ");
            } else if (isLeft()) {
                builder.append("LEFT ");
            } else if (isCross()) {
                builder.append("CROSS ");
            }

            if (isOuter()) {
                builder.append("OUTER ");
            } else if (isInner()) {
                builder.append("INNER ");
            } else if (isSemi()) {
                builder.append("SEMI ");
            }

            if (isStraight()) {
                builder.append("STRAIGHT_JOIN ");
            } else if (isApply()) {
                builder.append("APPLY ");
            } else {
                builder.append("JOIN ");
            }

            builder.append(rightItem).append((joinWindow != null) ? " WITHIN " + joinWindow : "");
        }

        for (Expression onExpression: onExpressions) {
            builder.append(" ON ").append(onExpression);
        }
        if (usingColumns.size()>0) {
            builder.append(PlainSelect.getFormatedList(usingColumns, "USING", true, true));
        }

        return builder.toString();
    }

    public Join addUsingColumns(Column... usingColumns) {
        List<Column> collection = Optional.ofNullable(getUsingColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, usingColumns);
        return this.withUsingColumns(collection);
    }

    public Join addUsingColumns(Collection<? extends Column> usingColumns) {
        List<Column> collection = Optional.ofNullable(getUsingColumns()).orElseGet(ArrayList::new);
        collection.addAll(usingColumns);
        return this.withUsingColumns(collection);
    }
}
