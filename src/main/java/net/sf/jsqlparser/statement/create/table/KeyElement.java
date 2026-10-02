/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** A column or expression key shared by index and constraint declarations. */
public class KeyElement implements Serializable {
    public enum SortOrder {
        ASC, DESC
    }

    public enum NullOrdering {
        FIRST, LAST
    }

    public final String columnName;
    public final List<String> params;
    private final Expression expression;
    private boolean expressionParenthesized = true;
    private String collation;
    private String operatorClass;
    private List<IndexOption> operatorClassParameters;
    private SortOrder sortOrder;
    private NullOrdering nullOrdering;
    private ExclusionOperator exclusionOperator;
    private boolean withoutOverlaps;
    private boolean period;

    public String getExclusionOperator() {
        return exclusionOperator == null ? null : exclusionOperator.toString();
    }

    /** Replaces the complete operator text; retained for source compatibility. */
    public void setExclusionOperator(String exclusionOperator) {
        this.exclusionOperator = exclusionOperator == null ? null
                : new ExclusionOperator().withName(exclusionOperator);
    }

    /** Returns the editable operator reference, or null when this is not an exclusion key. */
    public ExclusionOperator getExclusionOperatorReference() {
        return exclusionOperator;
    }

    public void setExclusionOperatorReference(ExclusionOperator exclusionOperator) {
        this.exclusionOperator = exclusionOperator;
    }

    public KeyElement withExclusionOperatorReference(ExclusionOperator exclusionOperator) {
        setExclusionOperatorReference(exclusionOperator);
        return this;
    }

    public KeyElement(String columnName) {
        this.columnName = columnName;
        this.params = null;
        this.expression = null;
    }

    public KeyElement(String columnName, List<String> params) {
        this.columnName = columnName;
        this.params = params;
        this.expression = null;
    }

    public KeyElement(Expression expression) {
        this.columnName = null;
        this.params = null;
        this.expression = expression;
    }

    public KeyElement(Expression expression, List<String> params) {
        this.columnName = null;
        this.params = params;
        this.expression = expression;
    }

    /** Marks the final key of a PostgreSQL temporal PRIMARY KEY or UNIQUE constraint. */
    public boolean isWithoutOverlaps() {
        return withoutOverlaps;
    }

    public void setWithoutOverlaps(boolean withoutOverlaps) {
        this.withoutOverlaps = withoutOverlaps;
    }

    public KeyElement withWithoutOverlaps(boolean withoutOverlaps) {
        setWithoutOverlaps(withoutOverlaps);
        return this;
    }

    /** Marks the final referencing column of a temporal foreign key. */
    public boolean isPeriod() {
        return period;
    }

    public void setPeriod(boolean period) {
        this.period = period;
    }

    public KeyElement withPeriod(boolean period) {
        setPeriod(period);
        return this;
    }

    public String getColumnName() {
        return expression != null ? expression.toString() : columnName;
    }

    public List<String> getParams() {
        return params;
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isExpression() {
        return expression != null;
    }

    public boolean isExpressionParenthesized() {
        return expressionParenthesized;
    }

    public void setExpressionParenthesized(boolean expressionParenthesized) {
        this.expressionParenthesized = expressionParenthesized;
    }

    public KeyElement withExpressionParenthesized(boolean expressionParenthesized) {
        setExpressionParenthesized(expressionParenthesized);
        return this;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public String getOperatorClass() {
        return operatorClass;
    }

    public void setOperatorClass(String operatorClass) {
        this.operatorClass = operatorClass;
    }

    public List<IndexOption> getOperatorClassParameters() {
        return operatorClassParameters;
    }

    public void setOperatorClassParameters(List<IndexOption> operatorClassParameters) {
        this.operatorClassParameters = operatorClassParameters;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public NullOrdering getNullOrdering() {
        return nullOrdering;
    }

    public void setNullOrdering(NullOrdering nullOrdering) {
        this.nullOrdering = nullOrdering;
    }

    public KeyElement withCollation(String collation) {
        setCollation(collation);
        return this;
    }

    public KeyElement withOperatorClass(String operatorClass) {
        setOperatorClass(operatorClass);
        return this;
    }

    public KeyElement withOperatorClassParameters(List<IndexOption> operatorClassParameters) {
        setOperatorClassParameters(operatorClassParameters);
        return this;
    }

    public KeyElement withSortOrder(SortOrder sortOrder) {
        setSortOrder(sortOrder);
        return this;
    }

    public KeyElement withNullOrdering(NullOrdering nullOrdering) {
        setNullOrdering(nullOrdering);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, value -> builder.append(value));
        return builder.toString();
    }

    /** Renders expression keys through the caller's expression printer. */
    public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (period) {
            builder.append("PERIOD ");
        }
        if (expression != null) {
            if (expressionParenthesized) {
                builder.append('(');
            }
            expressionPrinter.accept(expression);
            if (expressionParenthesized) {
                builder.append(')');
            }
        } else {
            builder.append(columnName);
        }
        appendParams(builder);
        appendCollation(builder);
        appendOperatorClass(builder, expressionPrinter);
        appendSortOrder(builder);
        appendNullOrdering(builder);
        if (exclusionOperator != null) {
            builder.append(" WITH ").append(exclusionOperator);
        }
        if (withoutOverlaps) {
            builder.append(" WITHOUT OVERLAPS");
        }
    }

    private void appendParams(StringBuilder builder) {
        if (params != null) {
            builder.append(" ").append(String.join(" ", params));
        }
    }

    private void appendCollation(StringBuilder builder) {
        if (collation != null && !hasParam("COLLATE")) {
            builder.append(" COLLATE ").append(collation);
        }
    }

    private void appendOperatorClass(StringBuilder builder,
            Consumer<Expression> expressionPrinter) {
        if (operatorClass != null && !hasParam(operatorClass)) {
            builder.append(" ").append(operatorClass);
            if (operatorClassParameters != null && !operatorClassParameters.isEmpty()) {
                builder.append(" ");
                IndexOption.appendListTo(builder, operatorClassParameters, expressionPrinter);
            }
        }
    }

    private void appendSortOrder(StringBuilder builder) {
        if (sortOrder != null && !hasParam(sortOrder.name())) {
            builder.append(" ").append(sortOrder);
        }
    }

    private void appendNullOrdering(StringBuilder builder) {
        if (nullOrdering != null && !hasParam("NULLS")) {
            builder.append(" NULLS ").append(nullOrdering);
        }
    }

    private boolean hasParam(String expected) {
        return params != null && params.stream().anyMatch(expected::equalsIgnoreCase);
    }
}
