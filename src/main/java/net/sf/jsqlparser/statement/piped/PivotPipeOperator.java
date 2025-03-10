/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public class PivotPipeOperator extends PipeOperator {
    private Function aggregateExpression;
    private Column inputColumn;
    private List<SelectItem<?>> pivotColumns;
    private Alias alias = null;

    public PivotPipeOperator(Function aggregateExpression, Column inputColumn,
            List<SelectItem<?>> pivotColumns, Alias alias) {
        this.aggregateExpression = aggregateExpression;
        this.inputColumn = inputColumn;
        this.pivotColumns = pivotColumns;
        this.alias = alias;
    }

    public Function getAggregateExpression() {
        return aggregateExpression;
    }

    public PivotPipeOperator setAggregateExpression(Function aggregateExpression) {
        this.aggregateExpression = aggregateExpression;
        return this;
    }

    public Column getInputColumn() {
        return inputColumn;
    }

    public PivotPipeOperator setInputColumn(Column inputColumn) {
        this.inputColumn = inputColumn;
        return this;
    }

    public List<SelectItem<?>> getPivotColumns() {
        return pivotColumns;
    }

    public PivotPipeOperator setPivotColumns(List<SelectItem<?>> pivotColumns) {
        this.pivotColumns = pivotColumns;
        return this;
    }

    public Alias getAlias() {
        return alias;
    }

    public PivotPipeOperator setAlias(Alias alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder
                .append("|> ")
                .append("PIVOT( ")
                .append(aggregateExpression)
                .append(" FOR ")
                .append(inputColumn)
                .append(" IN (")
                .append(Select.getStringList(pivotColumns))
                .append("))");
        if (alias != null) {
            builder.append(" ").append(alias);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }
}
