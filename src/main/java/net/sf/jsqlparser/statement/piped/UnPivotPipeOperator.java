package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

public class UnPivotPipeOperator extends PipeOperator {
    private Column valuesColumn;
    private Column nameColumn;
    private ExpressionList<Column> pivotColumns;
    private Alias alias = null;

    public UnPivotPipeOperator(Column valuesColumn, Column nameColumn,
            ExpressionList<Column> pivotColumns, Alias alias) {
        this.valuesColumn = valuesColumn;
        this.nameColumn = nameColumn;
        this.pivotColumns = pivotColumns;
        this.alias = alias;
    }

    public Column getValuesColumn() {
        return valuesColumn;
    }

    public UnPivotPipeOperator setValuesColumn(Column valuesColumn) {
        this.valuesColumn = valuesColumn;
        return this;
    }

    public Column getNameColumn() {
        return nameColumn;
    }

    public UnPivotPipeOperator setNameColumn(Column nameColumn) {
        this.nameColumn = nameColumn;
        return this;
    }

    public ExpressionList<Column> getPivotColumns() {
        return pivotColumns;
    }

    public UnPivotPipeOperator setPivotColumns(ExpressionList<Column> pivotColumns) {
        this.pivotColumns = pivotColumns;
        return this;
    }

    public Alias getAlias() {
        return alias;
    }

    public UnPivotPipeOperator setAlias(Alias alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder
                .append("|> ")
                .append("UNPIVOT( ")
                .append(valuesColumn)
                .append(" FOR ")
                .append(nameColumn)
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
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }
}
