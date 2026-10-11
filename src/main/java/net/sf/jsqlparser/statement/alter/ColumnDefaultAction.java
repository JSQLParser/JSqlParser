/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/**
 * Shared access to a column SET DEFAULT action on a table or view. Obtain active actions through
 * {@link AlterExpression#getColumnDefaults()}; edits update the existing statement model directly.
 */
public interface ColumnDefaultAction {
    String getColumnName();

    void setColumnName(String columnName);

    /** Returns the parsed default, or null for a legacy opaque SQL value. */
    Expression getDefaultExpression();

    /** Replaces the default expression, discarding any legacy opaque SQL value. */
    void setDefaultExpression(Expression expression);

    /** Returns the default's SQL text, including legacy opaque values when present. */
    default String getDefaultValue() {
        Expression expression = getDefaultExpression();
        return expression == null ? null : expression.toString();
    }

    /** Renders the shared value while preserving the caller's expression visitor. */
    default void appendDefaultValueTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        Expression expression = getDefaultExpression();
        if (expression == null) {
            sql.append(getDefaultValue());
        } else {
            expressionPrinter.accept(expression);
        }
    }
}
