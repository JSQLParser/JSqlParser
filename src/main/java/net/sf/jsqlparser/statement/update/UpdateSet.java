/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class UpdateSet {
    protected boolean usingBracketsForColumns = false;
    protected boolean usingBracketsForValues = false;
    protected ArrayList<Column> columns = new ArrayList<>();
    protected ArrayList<Expression> expressions = new ArrayList<>();

    public UpdateSet() {

    }

    public UpdateSet(Column column) {
        this.columns.add(column);
    }

    public UpdateSet(Column column, Expression expression) {
        this.columns.add(column);
        this.expressions.add(expression);
    }

    public boolean isUsingBracketsForValues() {
        return usingBracketsForValues;
    }

    public void setUsingBracketsForValues(boolean usingBracketsForValues) {
        this.usingBracketsForValues = usingBracketsForValues;
    }

    public boolean isUsingBracketsForColumns() {
        return usingBracketsForColumns;
    }

    public void setUsingBracketsForColumns(boolean usingBracketsForColumns) {
        this.usingBracketsForColumns = usingBracketsForColumns;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = Objects.requireNonNull(columns);
    }

    public ArrayList<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(ArrayList<Expression> expressions) {
        this.expressions = Objects.requireNonNull(expressions);
    }

    public void add(Column column, Expression expression) {
        columns.add(column);
        expressions.add(expression);
    }

    public void add(Column column) {
        columns.add(column);
    }

    public void add(Expression expression) {
        expressions.add(expression);
    }

    public void add(ExpressionList expressionList) {
        expressions.addAll(expressionList.getExpressions());
    }

    public final static StringBuilder appendUpdateSetsTo(StringBuilder builder, Collection<UpdateSet> updateSets) {
        builder.append(" SET ");

        int j = 0;
        for (UpdateSet updateSet : updateSets) {
            updateSet.appendTo(builder, j);
            j++;
        }
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPath"})
    StringBuilder appendTo(StringBuilder builder, int j) {
        if (j > 0) {
            builder.append(", ");
        }

        if (usingBracketsForColumns) {
            builder.append("(");
        }

        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns.get(i));
        }

        if (usingBracketsForColumns) {
            builder.append(")");
        }

        builder.append(" = ");

        if (usingBracketsForValues) {
            builder.append("(");
        }

        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(expressions.get(i));
        }
        if (usingBracketsForValues) {
            builder.append(")");
        }

        return builder;
    }

}
