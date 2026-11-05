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
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class UpdateSet implements Serializable {
    protected ExpressionList<Column> columns = new ExpressionList<>();
    protected ExpressionList<Expression> values = new ExpressionList<>();

    public UpdateSet() {

    }

    public UpdateSet(Column column) {
        this.columns.add(column);
    }

    public UpdateSet(Column column, Expression value) {
        this.columns.add(column);
        this.values.add(value);
    }

    public final static StringBuilder appendUpdateSetsTo(StringBuilder builder,
            Collection<UpdateSet> updateSets) {
        int j = 0;
        for (UpdateSet updateSet : updateSets) {
            updateSet.appendTo(builder, j);
            j++;
        }
        return builder;
    }

    /**
     * Appends {@code columns = values, ...}, letting a deparser visit each column and value. With
     * {@code builder::append} as the renderer the output equals
     * {@link #appendUpdateSetsTo(StringBuilder, Collection)}.
     */
    public static StringBuilder appendUpdateSetsTo(StringBuilder builder,
            Collection<UpdateSet> updateSets, Consumer<Expression> expressionRenderer) {
        if (updateSets == null) {
            return builder;
        }
        int j = 0;
        for (UpdateSet updateSet : updateSets) {
            if (j++ > 0) {
                builder.append(", ");
            }
            appendListTo(builder, updateSet.getColumns(), expressionRenderer);
            builder.append(" = ");
            appendListTo(builder, updateSet.getValues(), expressionRenderer);
        }
        return builder;
    }

    /** Appends {@code a, b} or {@code (a, b)}, as {@code Select.getStringList} does. */
    private static void appendListTo(StringBuilder builder, ExpressionList<?> list,
            Consumer<Expression> expressionRenderer) {
        boolean parenthesed = list instanceof ParenthesedExpressionList;
        if (parenthesed) {
            builder.append("(");
        }
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            expressionRenderer.accept(list.get(i));
        }
        if (parenthesed) {
            builder.append(")");
        }
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = Objects.requireNonNull(columns);
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public ExpressionList<?> getValues() {
        return values;
    }

    public void setValues(ExpressionList values) {
        this.values = Objects.requireNonNull(values);
    }

    public Expression getValue(int index) {
        return values.get(index);
    }

    public void add(Column column, Expression value) {
        this.add(column);
        this.add(value);
    }

    /**
     * Add another column to the existing column list. Transform this list into a
     * ParenthesedExpression list when needed.
     *
     * @param column
     */
    public void add(Column column) {
        if (!columns.isEmpty() && !(columns instanceof ParenthesedExpressionList)) {
            columns = new ParenthesedExpressionList<>(columns);
        }
        columns.add(column);
    }

    /**
     * Add another expression to the existing value list. Transform this list into a
     * ParenthesedExpression list when needed.
     *
     * @param expression
     */
    public void add(Expression expression) {
        if (!values.isEmpty() && !(values instanceof ParenthesedExpressionList)) {
            values = new ParenthesedExpressionList<>(values);
        }
        values.add(expression);
    }

    public void add(ExpressionList<?> expressionList) {
        values.addAll(expressionList);
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPath"})
    StringBuilder appendTo(StringBuilder builder, int j) {
        if (j > 0) {
            builder.append(", ");
        }
        builder.append(
                Select.getStringList(columns, true, columns instanceof ParenthesedExpressionList));
        builder.append(" = ");
        builder.append(
                Select.getStringList(values, true, values instanceof ParenthesedExpressionList));
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder(), 0).toString();
    }
}
