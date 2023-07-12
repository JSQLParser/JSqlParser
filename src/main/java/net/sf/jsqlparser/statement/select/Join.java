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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Join extends ASTNodeAccessImpl {

    private boolean outer = false;
    private boolean right = false;
    private boolean left = false;
    private boolean natural = false;
    private boolean global = false;
    private boolean full = false;
    private boolean inner = false;
    private boolean simple = false;
    private boolean cross = false;
    private boolean semi = false;
    private boolean straight = false;
    private boolean apply = false;
    private FromItem fromItem;
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

    /**
     * A JOIN means INNER when the INNER keyword is set or when no other qualifier has been set.
     *
     * @return Tells, if a JOIN means a qualified INNER JOIN.
     *
     */
    public boolean isInnerJoin() {
        return inner
                || !(
                /* Qualified Joins */
                left || right || full || outer

                /* Cross Join */
                        || cross

                        /* Natural Join */
                        || natural);
    }

    /**
     *
     * @return Tells, if the INNER keyword has been set.
     */
    public boolean isInner() {
        return inner;
    }

    public Join withInner(boolean b) {
        this.setInner(b);
        return this;
    }

    /**
     *
     * Sets the INNER keyword and switches off any contradicting qualifiers automatically.
     */
    public void setInner(boolean b) {
        if (b) {
            left = false;
            right = false;
            outer = false;
            cross = false;
            natural = false;
        }
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

    /**
     *
     * Sets the OUTER keyword and switches off any contradicting qualifiers automatically.
     */
    public void setOuter(boolean b) {
        if (b) {
            inner = false;
        }
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

    /**
     *
     * Sets the LEFT keyword and switches off any contradicting qualifiers automatically.
     */
    public void setLeft(boolean b) {
        if (b) {
            inner = false;
            right = false;
        }
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

    /**
     *
     * Sets the RIGHT keyword and switches off any contradicting qualifiers automatically.
     */
    public void setRight(boolean b) {
        if (b) {
            inner = false;
            left = false;
        }
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

    public boolean isGlobal() {
        return global;
    }

    public Join withNatural(boolean b) {
        this.setNatural(b);
        return this;
    }

    public void setNatural(boolean b) {
        natural = b;
    }

    public void setGlobal(boolean b) {
        global = b;
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
     *
     */
    public FromItem getRightItem() {
        return fromItem;
    }

    @Deprecated
    public Join withRightItem(FromItem item) {
        this.setFromItem(item);
        return this;
    }

    public void setRightItem(FromItem item) {
        fromItem = item;
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public Join setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
        return this;
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
     * 
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

        if (isGlobal()) {
            builder.append("GLOBAL ");
        }

        if (isSimple() && isOuter()) {
            builder.append("OUTER ").append(fromItem);
        } else if (isSimple()) {
            builder.append(fromItem);
        } else {
            if (isNatural()) {
                builder.append("NATURAL ");
            }

            if (isRight()) {
                builder.append("RIGHT ");
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

            builder.append(fromItem).append((joinWindow != null) ? " WITHIN " + joinWindow : "");
        }

        for (Expression onExpression : onExpressions) {
            builder.append(" ON ").append(onExpression);
        }
        if (usingColumns.size() > 0) {
            builder.append(PlainSelect.getFormattedList(usingColumns, "USING", true, true));
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
