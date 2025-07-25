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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

public class GroupByElement implements Serializable {
    private ExpressionList<Expression> groupByExpressions = new ExpressionList<>();
    private List<ExpressionList<Expression>> groupingSets = new ArrayList<>();
    // postgres rollup is an ExpressionList
    private boolean mysqlWithRollup = false;

    public boolean isUsingBrackets() {
        return groupByExpressions.isUsingBrackets();
    }

    public <T, S> T accept(GroupByVisitor<T> groupByVisitor, S context) {
        return groupByVisitor.visit(this, context);
    }

    public ExpressionList<Expression> getGroupByExpressionList() {
        return groupByExpressions;
    }

    @Deprecated
    public ExpressionList<Expression> getGroupByExpressions() {
        return groupByExpressions;
    }

    public void setGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        this.groupByExpressions = groupByExpressions;
    }

    @Deprecated
    public void addGroupByExpression(Expression groupByExpression) {
        if (groupByExpressions.getExpressions() == null) {
            groupByExpressions.setExpressions(new ArrayList<>());
        }
        groupByExpressions.add(groupByExpression);
    }

    public List<ExpressionList<Expression>> getGroupingSets() {
        return groupingSets;
    }

    public void setGroupingSets(List<ExpressionList<Expression>> groupingSets) {
        this.groupingSets = groupingSets;
    }

    public void addGroupingSet(ExpressionList<Expression> list) {
        this.groupingSets.add(list);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("GROUP BY ");

        if (groupByExpressions != null) {
            b.append(groupByExpressions);
        }

        int i = 0;
        if (!groupingSets.isEmpty()) {
            if (b.charAt(b.length() - 1) != ' ') {
                b.append(' ');
            }
            b.append("GROUPING SETS (");
            for (ExpressionList<?> expressionList : groupingSets) {
                b.append(i++ > 0 ? ", " : "").append(Select.getStringList(
                        expressionList,
                        true, expressionList instanceof ParenthesedExpressionList));
            }
            b.append(")");
        }

        if (isMysqlWithRollup()) {
            b.append(" WITH ROLLUP");
        }

        return b.toString();
    }

    public GroupByElement withGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        this.setGroupByExpressions(groupByExpressions);
        return this;
    }

    public GroupByElement withGroupingSets(List<ExpressionList<Expression>> groupingSets) {
        this.setGroupingSets(groupingSets);
        return this;
    }

    public GroupByElement addGroupByExpressions(Expression... groupByExpressions) {
        return this.addGroupByExpressions(Arrays.asList(groupByExpressions));
    }

    public GroupByElement addGroupByExpressions(
            Collection<? extends Expression> groupByExpressions) {
        ExpressionList collection =
                Optional.ofNullable(getGroupByExpressions()).orElseGet(ExpressionList::new);
        Collections.addAll(collection, groupByExpressions);
        return this.withGroupByExpressions(collection);
    }

    public GroupByElement addGroupingSets(Object... groupingSets) {
        List collection = Optional.ofNullable(getGroupingSets()).orElseGet(ArrayList::new);
        Collections.addAll(collection, groupingSets);
        return this.withGroupingSets(collection);
    }

    public GroupByElement addGroupingSets(
            Collection<List<ExpressionList<Expression>>> groupingSets) {
        List collection = Optional.ofNullable(getGroupingSets()).orElseGet(ArrayList::new);
        collection.addAll(groupingSets);
        return this.withGroupingSets(collection);
    }

    public boolean isMysqlWithRollup() {
        return mysqlWithRollup;
    }

    public GroupByElement setMysqlWithRollup(boolean mysqlWithRollup) {
        this.mysqlWithRollup = mysqlWithRollup;
        return this;
    }
}
