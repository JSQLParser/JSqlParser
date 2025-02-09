package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public class UnPivotPipeOperator extends PipeOperator {
    private Column valuesColumn;
    private Column nameColumn;
    private List<SelectItem<?>> pivotColumns;
    private Alias alias = null;

    public UnPivotPipeOperator(Column valuesColumn, Column nameColumn,
            List<SelectItem<?>> pivotColumns, Alias alias) {
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

    public List<SelectItem<?>> getPivotColumns() {
        return pivotColumns;
    }

    public UnPivotPipeOperator setPivotColumns(List<SelectItem<?>> pivotColumns) {
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
