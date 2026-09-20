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

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class TableFunction extends Function implements FromItem {
    private String prefix = null;
    private Alias alias = null;
    private Pivot pivot = null;
    private UnPivot unPivot = null;
    private Function function;
    private ParenthesedExpressionList<Function> rowsFromFunctions;
    private String withClause = null;
    private boolean withOffset = false;
    private Alias offsetAlias = null;

    public TableFunction(Function function) {
        this.function = function;
    }

    public TableFunction(String prefix, Function function) {
        this.prefix = prefix;
        this.function = function;
    }

    public TableFunction(Function function, String withClause) {
        this.function = function;
        this.withClause = withClause;
    }

    public TableFunction(String prefix, Function function, String withClause) {
        this.prefix = prefix;
        this.function = function;
        this.withClause = withClause;
    }

    public TableFunction(ParenthesedExpressionList<Function> rowsFromFunctions) {
        this.rowsFromFunctions = rowsFromFunctions;
    }

    public TableFunction(String prefix, ParenthesedExpressionList<Function> rowsFromFunctions) {
        this.prefix = prefix;
        this.rowsFromFunctions = rowsFromFunctions;
    }

    public TableFunction(ParenthesedExpressionList<Function> rowsFromFunctions, String withClause) {
        this.rowsFromFunctions = rowsFromFunctions;
        this.withClause = withClause;
    }

    public TableFunction(String prefix, ParenthesedExpressionList<Function> rowsFromFunctions,
            String withClause) {
        this.prefix = prefix;
        this.rowsFromFunctions = rowsFromFunctions;
        this.withClause = withClause;
    }

    public TableFunction(String prefix, String name, Expression... parameters) {
        this.prefix = prefix;
        this.function = new Function(name, parameters);
    }

    public TableFunction(String name, Expression... parameters) {
        this(null, name, parameters);
    }

    public Function getFunction() {
        return function;
    }

    public TableFunction setFunction(Function function) {
        this.function = function;
        this.rowsFromFunctions = null;
        return this;
    }

    public ParenthesedExpressionList<Function> getRowsFromFunctions() {
        return rowsFromFunctions;
    }

    public TableFunction setRowsFromFunctions(
            ParenthesedExpressionList<Function> rowsFromFunctions) {
        this.rowsFromFunctions = rowsFromFunctions;
        this.function = null;
        return this;
    }

    public boolean isRowsFrom() {
        return rowsFromFunctions != null;
    }

    public List<Function> getFunctions() {
        if (rowsFromFunctions != null) {
            return rowsFromFunctions;
        }
        return function != null ? List.of(function) : null;
    }

    @Deprecated
    public Function getExpression() {
        return getFunction();
    }

    public String getPrefix() {
        return prefix;
    }

    public TableFunction setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * BigQuery's {@code UNNEST(array) [AS alias] WITH OFFSET [AS offsetAlias]}, where the
     * {@code WITH OFFSET} clause follows the table alias and carries an alias of its own.
     */
    public boolean isWithOffset() {
        return withOffset;
    }

    public void setWithOffset(boolean withOffset) {
        this.withOffset = withOffset;
    }

    public TableFunction withWithOffset(boolean withOffset) {
        setWithOffset(withOffset);
        return this;
    }

    public Alias getOffsetAlias() {
        return offsetAlias;
    }

    public void setOffsetAlias(Alias offsetAlias) {
        this.offsetAlias = offsetAlias;
        this.withOffset = true;
    }

    public TableFunction withOffsetAlias(Alias offsetAlias) {
        setOffsetAlias(offsetAlias);
        return this;
    }

    public String getWithClause() {
        return withClause;
    }

    public void setWithClause(String withClause) {
        this.withClause = withClause;
    }

    public TableFunction withWithClause(String withClause) {
        this.withClause = withClause;
        return this;
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public TableFunction withAlias(Alias alias) {
        return (TableFunction) FromItem.super.withAlias(alias);
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
    public TableFunction withPivot(Pivot pivot) {
        return (TableFunction) FromItem.super.withPivot(pivot);
    }

    @Override
    public UnPivot getUnPivot() {
        return unPivot;
    }

    @Override
    public void setUnPivot(UnPivot unPivot) {
        this.unPivot = unPivot;
    }

    @Override
    public TableFunction withUnPivot(UnPivot unpivot) {
        return (TableFunction) FromItem.super.withUnPivot(unpivot);
    }

    @Override
    public SampleClause getSampleClause() {
        return null;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        return null;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (prefix != null) {
            builder.append(prefix).append(" ");
        }
        if (rowsFromFunctions != null) {
            builder.append("ROWS FROM (");
            for (int i = 0; i < rowsFromFunctions.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                expressionPrinter.accept(rowsFromFunctions.get(i));
            }
            builder.append(")");
        } else {
            expressionPrinter.accept(function);
        }

        if (withClause != null) {
            builder.append(" WITH ").append(withClause);
        }

        if (alias != null) {
            builder.append(alias);
        }

        if (withOffset) {
            builder.append(" WITH OFFSET");
            if (offsetAlias != null) {
                builder.append(offsetAlias);
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
