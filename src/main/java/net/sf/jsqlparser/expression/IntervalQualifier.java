/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * Models an SQL interval qualifier as defined by the SQL standard (see
 * <a href="https://www.postgresql.org/docs/current/datatype-datetime.html">PostgreSQL: Interval
 * Input</a>), e.g. {@code YEAR}, {@code DAY TO SECOND}, {@code DAY(9) TO SECOND} or
 * {@code SECOND(2, 4)}.
 * <p>
 * It is shared by {@link IntervalExpression} (the {@code INTERVAL ...} expression literal, used in
 * DML) and by {@code net.sf.jsqlparser.statement.create.table.ColDataType} (the {@code INTERVAL}
 * column / cast target type, used in DDL), so the qualifier is represented once and round-trips
 * consistently across all contexts.
 */
public final class IntervalQualifier implements Serializable {

    private final String leadingField;
    private final Integer leadingFieldPrecision;
    private final String trailingField;
    private final Integer fractionalSecondsPrecision;

    public IntervalQualifier(
            String leadingField, Integer leadingFieldPrecision, String trailingField,
            Integer fractionalSecondsPrecision) {
        this.leadingField = leadingField;
        this.leadingFieldPrecision = leadingFieldPrecision;
        this.trailingField = trailingField;
        this.fractionalSecondsPrecision = fractionalSecondsPrecision;
    }

    public String getLeadingField() {
        return leadingField;
    }

    public Integer getLeadingFieldPrecision() {
        return leadingFieldPrecision;
    }

    public String getTrailingField() {
        return trailingField;
    }

    public Integer getFractionalSecondsPrecision() {
        return fractionalSecondsPrecision;
    }

    public IntervalQualifier withLeadingField(String leadingField) {
        return new IntervalQualifier(
                leadingField, leadingFieldPrecision, trailingField, fractionalSecondsPrecision);
    }

    public IntervalQualifier withLeadingFieldPrecision(Integer leadingFieldPrecision) {
        return new IntervalQualifier(
                leadingField, leadingFieldPrecision, trailingField, fractionalSecondsPrecision);
    }

    public IntervalQualifier withTrailingField(String trailingField) {
        return new IntervalQualifier(
                leadingField, leadingFieldPrecision, trailingField, fractionalSecondsPrecision);
    }

    public IntervalQualifier withFractionalSecondsPrecision(Integer fractionalSecondsPrecision) {
        return new IntervalQualifier(
                leadingField, leadingFieldPrecision, trailingField, fractionalSecondsPrecision);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntervalQualifier)) {
            return false;
        }
        IntervalQualifier that = (IntervalQualifier) o;
        return Objects.equals(leadingField, that.leadingField)
                && Objects.equals(leadingFieldPrecision, that.leadingFieldPrecision)
                && Objects.equals(trailingField, that.trailingField)
                && Objects.equals(fractionalSecondsPrecision, that.fractionalSecondsPrecision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                leadingField, leadingFieldPrecision, trailingField, fractionalSecondsPrecision);
    }

    /**
     * Renders the qualifier back to SQL, e.g. {@code DAY}, {@code DAY TO SECOND},
     * {@code DAY(9) TO SECOND} or {@code SECOND(2, 4)}.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Objects.toString(leadingField, ""));
        if (leadingFieldPrecision != null) {
            sb.append("(").append(leadingFieldPrecision);
            // For a single-field qualifier such as SECOND(2, 4), the fractional seconds
            // precision is rendered together with the leading precision.
            if (trailingField == null && fractionalSecondsPrecision != null) {
                sb.append(", ").append(fractionalSecondsPrecision);
            }
            sb.append(")");
        }
        if (trailingField != null) {
            sb.append(" TO ").append(trailingField);
            if (fractionalSecondsPrecision != null) {
                sb.append("(").append(fractionalSecondsPrecision).append(")");
            }
        }
        return sb.toString();
    }
}
