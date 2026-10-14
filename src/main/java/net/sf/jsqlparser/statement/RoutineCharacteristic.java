/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * A characteristic set by {@code ALTER FUNCTION}, {@code ALTER PROCEDURE} or {@code ALTER ROUTINE},
 * or declared by MySQL's {@code CREATE FUNCTION|PROCEDURE}. PostgreSQL and MySQL share the model,
 * and each {@link Kind} renders its own SQL:
 *
 * <ul>
 * <li>PostgreSQL: the volatility, the null input behavior, {@code [NOT] LEAKPROOF},
 * {@code [EXTERNAL] SECURITY}, {@code PARALLEL}, {@code COST}, {@code ROWS}, {@code SUPPORT} and
 * the {@code SET} or {@code RESET} of a configuration parameter;</li>
 * <li>MySQL: {@code COMMENT}, {@code LANGUAGE SQL}, {@code [NOT] DETERMINISTIC} (CREATE only), the
 * SQL data access and {@code SQL SECURITY}.</li>
 * </ul>
 *
 * The security kinds are shared; {@link #getSecurityClause()} keeps the spelling. Values keep their
 * SQL text, such as {@code 100}. The {@code COMMENT} literal is a {@link StringValue}.
 */
public class RoutineCharacteristic implements Serializable {

    public enum Kind {
        /** PostgreSQL {@code IMMUTABLE}. */
        IMMUTABLE("IMMUTABLE"),
        /** PostgreSQL {@code STABLE}. */
        STABLE("STABLE"),
        /** PostgreSQL {@code VOLATILE}. */
        VOLATILE("VOLATILE"),
        /** PostgreSQL {@code CALLED ON NULL INPUT}. */
        CALLED_ON_NULL_INPUT("CALLED ON NULL INPUT"),
        /** PostgreSQL {@code RETURNS NULL ON NULL INPUT}. */
        RETURNS_NULL_ON_NULL_INPUT("RETURNS NULL ON NULL INPUT"),
        /** PostgreSQL {@code STRICT}. */
        STRICT("STRICT"),
        /** PostgreSQL {@code LEAKPROOF}. */
        LEAKPROOF("LEAKPROOF"),
        /** PostgreSQL {@code NOT LEAKPROOF}. */
        NOT_LEAKPROOF("NOT LEAKPROOF"),
        /** {@code DEFINER}, preceded by the {@link SecurityClause}. */
        SECURITY_DEFINER("DEFINER"),
        /** {@code INVOKER}, preceded by the {@link SecurityClause}. */
        SECURITY_INVOKER("INVOKER"),
        /** PostgreSQL {@code PARALLEL UNSAFE}. */
        PARALLEL_UNSAFE("PARALLEL UNSAFE"),
        /** PostgreSQL {@code PARALLEL RESTRICTED}. */
        PARALLEL_RESTRICTED("PARALLEL RESTRICTED"),
        /** PostgreSQL {@code PARALLEL SAFE}. */
        PARALLEL_SAFE("PARALLEL SAFE"),
        /** PostgreSQL {@code COST} followed by the numeric {@link #getValue()}. */
        COST("COST"),
        /** PostgreSQL {@code ROWS} followed by the numeric {@link #getValue()}. */
        ROWS("ROWS"),
        /** PostgreSQL {@code SUPPORT} followed by the function name in {@link #getValue()}. */
        SUPPORT("SUPPORT"),
        /** PostgreSQL {@code SET} or {@code RESET}, see {@link #getParameterChange()}. */
        CONFIGURATION_PARAMETER(null),
        /** MySQL {@code COMMENT} followed by the string literal of {@link #getComment()}. */
        COMMENT("COMMENT"),
        /** MySQL {@code LANGUAGE SQL}. */
        LANGUAGE_SQL("LANGUAGE SQL"),
        /** MySQL {@code DETERMINISTIC}, only in {@code CREATE FUNCTION|PROCEDURE}. */
        DETERMINISTIC("DETERMINISTIC"),
        /** MySQL {@code NOT DETERMINISTIC}, only in {@code CREATE FUNCTION|PROCEDURE}. */
        NOT_DETERMINISTIC("NOT DETERMINISTIC"),
        /** MySQL {@code CONTAINS SQL}. */
        CONTAINS_SQL("CONTAINS SQL"),
        /** MySQL {@code NO SQL}. */
        NO_SQL("NO SQL"),
        /** MySQL {@code READS SQL DATA}. */
        READS_SQL_DATA("READS SQL DATA"),
        /** MySQL {@code MODIFIES SQL DATA}. */
        MODIFIES_SQL_DATA("MODIFIES SQL DATA");

        private final String keywords;

        Kind(String keywords) {
            this.keywords = keywords;
        }

        private boolean hasValue() {
            return this == COST || this == ROWS || this == SUPPORT || this == COMMENT;
        }
    }

    /** The words before {@code DEFINER} or {@code INVOKER}. */
    public enum SecurityClause {
        /** PostgreSQL {@code SECURITY}. */
        SECURITY("SECURITY"),
        /** PostgreSQL {@code EXTERNAL SECURITY}; {@code EXTERNAL} has no effect. */
        EXTERNAL_SECURITY("EXTERNAL SECURITY"),
        /** MySQL {@code SQL SECURITY}. */
        SQL_SECURITY("SQL SECURITY");

        private final String keywords;

        SecurityClause(String keywords) {
            this.keywords = keywords;
        }
    }

    private Kind kind;
    private String value;
    private StringValue comment;
    private SecurityClause securityClause;
    private ConfigurationParameterChange parameterChange;

    public RoutineCharacteristic(Kind kind) {
        this.kind = kind;
    }

    public RoutineCharacteristic(Kind kind, String value) {
        this.kind = kind;
        setValue(value);
    }

    public RoutineCharacteristic(ConfigurationParameterChange parameterChange) {
        this.kind = Kind.CONFIGURATION_PARAMETER;
        this.parameterChange = parameterChange;
    }

    /**
     * Creates a MySQL {@code COMMENT} characteristic.
     *
     * @param comment the string literal
     * @return the characteristic
     */
    public static RoutineCharacteristic comment(StringValue comment) {
        return new RoutineCharacteristic(Kind.COMMENT).withComment(comment);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * The SQL text after the keyword of {@link Kind#COST}, {@link Kind#ROWS}, {@link Kind#SUPPORT}
     * and {@link Kind#COMMENT}. For {@code COMMENT} it is derived from {@link #getComment()}, such
     * as {@code 'it\'s'}.
     *
     * @return the value as written, or {@code null} for other kinds
     */
    public String getValue() {
        if (kind == Kind.COMMENT) {
            return comment == null ? null : comment.toString();
        }
        return value;
    }

    /**
     * Sets the SQL text of the value. For {@link Kind#COMMENT} it replaces {@link #getComment()}
     * with a literal read without dialect escaping; use {@link #setComment(StringValue)} to keep
     * MySQL backslash escapes.
     *
     * @param value the SQL text
     */
    public void setValue(String value) {
        if (kind == Kind.COMMENT) {
            comment = value == null ? null : new StringValue(value);
        } else {
            this.value = value;
        }
    }

    /**
     * The string literal of {@link Kind#COMMENT}. {@link StringValue#getNotExcapedValue()} returns
     * the text that MySQL stores.
     *
     * @return the literal, or {@code null} for other kinds
     */
    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    public RoutineCharacteristic withComment(StringValue comment) {
        setComment(comment);
        return this;
    }

    /**
     * The spelling of {@link Kind#SECURITY_DEFINER} and {@link Kind#SECURITY_INVOKER}; {@code null}
     * renders {@link SecurityClause#SECURITY}.
     *
     * @return the clause, or {@code null}
     */
    public SecurityClause getSecurityClause() {
        return securityClause;
    }

    public void setSecurityClause(SecurityClause securityClause) {
        this.securityClause = securityClause;
    }

    /**
     * The change of {@link Kind#CONFIGURATION_PARAMETER}.
     *
     * @return the change, or {@code null} for other kinds
     */
    public ConfigurationParameterChange getParameterChange() {
        return parameterChange;
    }

    public void setParameterChange(ConfigurationParameterChange parameterChange) {
        this.parameterChange = parameterChange;
    }

    public RoutineCharacteristic withValue(String value) {
        setValue(value);
        return this;
    }

    public RoutineCharacteristic withSecurityClause(SecurityClause securityClause) {
        setSecurityClause(securityClause);
        return this;
    }

    /**
     * Appends this characteristic to a builder.
     *
     * @param builder the target
     * @param valueRenderer appends each configuration parameter value
     * @return the builder
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> valueRenderer) {
        switch (kind) {
            case CONFIGURATION_PARAMETER:
                if (parameterChange == null) {
                    throw new IllegalStateException("Expected a configuration parameter change");
                }
                return parameterChange.appendTo(builder, valueRenderer);
            case SECURITY_DEFINER:
            case SECURITY_INVOKER:
                return builder
                        .append((securityClause == null ? SecurityClause.SECURITY
                                : securityClause).keywords)
                        .append(' ').append(kind.keywords);
            default:
                builder.append(kind.keywords);
                return kind.hasValue() ? builder.append(' ').append(getValue()) : builder;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
