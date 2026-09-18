/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.schema.Column;

/**
 * Read-only metadata for current date/time expressions in PostgreSQL and MySQL. The parser's
 * existing TimeKeyExpression, Function and Column nodes are retained. Results are snapshots;
 * inspect the expression again after editing its AST. This is not a database range validator.
 */
public final class TemporalExpressionInfo {

    public enum Kind {
        CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP, LOCAL_TIME, LOCAL_TIMESTAMP
    }

    private final Kind kind;
    private final String name;
    private final Integer precision;
    private final boolean parentheses;

    private TemporalExpressionInfo(Kind kind, String name, Integer precision, boolean parentheses) {
        this.kind = kind;
        this.name = name;
        this.precision = precision;
        this.parentheses = parentheses;
    }

    public Kind getKind() {
        return kind;
    }

    /** Returns the original keyword or function name, without argument parentheses. */
    public String getName() {
        return name;
    }

    /**
     * Returns the explicitly requested fractional precision, or null when omitted. Zero is distinct
     * from omission. Database defaults, range checks and precision clamping are not applied.
     */
    public Integer getPrecision() {
        return precision;
    }

    public boolean hasParentheses() {
        return parentheses;
    }

    /**
     * Recognizes standard current-time spellings and MySQL NOW/CURDATE/CURTIME aliases. MySQL
     * LOCALTIME and LOCALTIMESTAMP normalize to CURRENT_TIMESTAMP; PostgreSQL retains their local
     * time/time-stamp distinction. Quoted/qualified names, ordinary functions, unsupported dialects
     * and expressions outside the supported call shapes return an empty result.
     */
    public static Optional<TemporalExpressionInfo> from(Expression source, Dialect dialect) {
        if (dialect != Dialect.POSTGRESQL && dialect != Dialect.MYSQL) {
            return Optional.empty();
        }
        Expression expression = unwrap(source);
        if (expression instanceof TimeKeyExpression) {
            return fromKeyword(((TimeKeyExpression) expression).getStringValue(), dialect);
        }
        if (expression instanceof Column) {
            Column column = (Column) expression;
            if (column.getTable() != null && column.getTable().getName() != null) {
                return Optional.empty();
            }
            return create(column.getColumnName(), null, false, dialect);
        }
        if (expression instanceof Function) {
            return fromFunction((Function) expression, dialect);
        }
        return Optional.empty();
    }

    private static Expression unwrap(Expression source) {
        Expression expression = source;
        while (expression instanceof ParenthesedExpressionList
                && ((ParenthesedExpressionList<?>) expression).size() == 1) {
            expression = ((ParenthesedExpressionList<?>) expression).get(0);
        }
        return expression;
    }

    private static Optional<TemporalExpressionInfo> fromKeyword(String name, Dialect dialect) {
        boolean parentheses = name != null && name.endsWith("()");
        String keyword = parentheses ? name.substring(0, name.length() - 2) : name;
        return create(keyword, null, parentheses, dialect);
    }

    private static Optional<TemporalExpressionInfo> fromFunction(Function function,
            Dialect dialect) {
        if (!isPlainCall(function)) {
            return Optional.empty();
        }
        if (function.getParameters() == null || function.getParameters().isEmpty()) {
            return create(function.getName(), null, true, dialect);
        }
        if (function.getParameters().size() != 1
                || !(function.getParameters().get(0) instanceof LongValue)) {
            return Optional.empty();
        }
        BigInteger value = ((LongValue) function.getParameters().get(0)).getBigIntegerValue();
        if (value.signum() < 0 || value.bitLength() >= Integer.SIZE) {
            return Optional.empty();
        }
        return create(function.getName(), value.intValue(), true, dialect);
    }

    private static Optional<TemporalExpressionInfo> create(String name, Integer precision,
            boolean parentheses, Dialect dialect) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = name.toUpperCase(Locale.ROOT);
        Kind kind = kind(normalized, dialect);
        if (kind == null || !validCallShape(normalized, kind, precision, parentheses, dialect)) {
            return Optional.empty();
        }
        return Optional.of(new TemporalExpressionInfo(kind, name, precision, parentheses));
    }

    private static boolean isPlainCall(Function function) {
        return function.getClass() == Function.class
                && function.getMultipartName() != null && function.getMultipartName().size() == 1
                && !function.isEscaped() && !function.isAllColumns() && !function.isDistinct()
                && !function.isUnique() && function.getNamedParameters() == null
                && function.getChainedParameters() == null && function.getAttribute() == null
                && function.getHavingClause() == null && function.getOrderByElements() == null
                && function.getNullHandling() == null && function.getLimit() == null
                && function.getKeep() == null && function.getOnOverflowTruncate() == null
                && function.getExtraKeyword() == null && function.getKeywordArguments() == null;
    }

    private static Kind kind(String name, Dialect dialect) {
        switch (name) {
            case "CURRENT_DATE":
                return Kind.CURRENT_DATE;
            case "CURRENT_TIME":
                return Kind.CURRENT_TIME;
            case "CURRENT_TIMESTAMP":
            case "NOW":
                return Kind.CURRENT_TIMESTAMP;
            case "LOCALTIME":
                return dialect == Dialect.POSTGRESQL ? Kind.LOCAL_TIME : Kind.CURRENT_TIMESTAMP;
            case "LOCALTIMESTAMP":
                return dialect == Dialect.POSTGRESQL ? Kind.LOCAL_TIMESTAMP
                        : Kind.CURRENT_TIMESTAMP;
            case "CURDATE":
                return dialect == Dialect.MYSQL ? Kind.CURRENT_DATE : null;
            case "CURTIME":
                return dialect == Dialect.MYSQL ? Kind.CURRENT_TIME : null;
            default:
                return null;
        }
    }

    private static boolean validCallShape(String name, Kind kind, Integer precision,
            boolean parentheses, Dialect dialect) {
        if (("NOW".equals(name) || "CURDATE".equals(name) || "CURTIME".equals(name))
                && !parentheses) {
            return false;
        }
        if (kind == Kind.CURRENT_DATE && precision != null) {
            return false;
        }
        if (dialect == Dialect.POSTGRESQL) {
            return "NOW".equals(name) ? precision == null : parentheses == (precision != null);
        }
        return true;
    }
}
