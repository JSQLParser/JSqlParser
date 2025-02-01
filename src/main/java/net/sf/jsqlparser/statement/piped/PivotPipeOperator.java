package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

public class PivotPipeOperator extends PipeOperator {
    private Expression aggregateExpression;
    private Column inputColumn;
    private ExpressionList<Column> pivotColumns;
    private Alias alias = null;

    public PivotPipeOperator(Expression aggregateExpression, Column inputColumn,
            ExpressionList<Column> pivotColumns, Alias alias) {
        this.aggregateExpression = aggregateExpression;
        this.inputColumn = inputColumn;
        this.pivotColumns = pivotColumns;
        this.alias = alias;
    }

    public Expression getAggregateExpression() {
        return aggregateExpression;
    }

    public PivotPipeOperator setAggregateExpression(Expression aggregateExpression) {
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

    public ExpressionList<Column> getPivotColumns() {
        return pivotColumns;
    }

    public PivotPipeOperator setPivotColumns(ExpressionList<Column> pivotColumns) {
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
                .append(pivotColumns)
                .append("))");
        if (alias != null) {
            builder.append(" ").append(alias);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
