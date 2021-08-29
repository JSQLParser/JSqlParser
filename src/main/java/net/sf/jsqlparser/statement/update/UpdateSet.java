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
import java.util.Objects;

public class UpdateSet {
    protected boolean usingBrackets = false;
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

    public boolean isUsingBrackets() {
        return usingBrackets;
    }

    public void setUsingBrackets(boolean usingBrackets) {
        this.usingBrackets = usingBrackets;
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
    };

    public void add(Column column) {
        columns.add(column);
    };

    public void add(ExpressionList expressionList) {
        expressions.addAll(expressionList.getExpressions());
    };
}
