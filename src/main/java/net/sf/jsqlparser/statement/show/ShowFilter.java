/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import java.io.Serializable;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/**
 * The {@code LIKE 'pattern'} or {@code WHERE expr} filter that ends many MySQL {@code SHOW}
 * statements.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/extended-show.html">MySQL extensions to
 *      SHOW statements</a>
 */
public class ShowFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Kind {
        LIKE, WHERE
    }

    private Kind kind = Kind.LIKE;
    private Expression expression;

    public ShowFilter() {
        // empty constructor
    }

    public ShowFilter(Kind kind, Expression expression) {
        setKind(kind);
        this.expression = expression;
    }

    /** A {@code LIKE pattern} filter. */
    public static ShowFilter like(Expression pattern) {
        return new ShowFilter(Kind.LIKE, pattern);
    }

    /** A {@code WHERE condition} filter. */
    public static ShowFilter where(Expression condition) {
        return new ShowFilter(Kind.WHERE, condition);
    }

    /** {@code LIKE} or {@code WHERE}; never null. */
    public Kind getKind() {
        return kind;
    }

    /** Sets the kind; null means {@code LIKE}. */
    public void setKind(Kind kind) {
        this.kind = kind == null ? Kind.LIKE : kind;
    }

    /** The pattern of a {@code LIKE} filter or the condition of a {@code WHERE} filter. */
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /** The pattern of a {@code LIKE} filter, or null. */
    public static Expression likeExpression(ShowFilter filter) {
        return filter != null && filter.kind == Kind.LIKE ? filter.expression : null;
    }

    /** The condition of a {@code WHERE} filter, or null. */
    public static Expression whereCondition(ShowFilter filter) {
        return filter != null && filter.kind == Kind.WHERE ? filter.expression : null;
    }

    /**
     * The filter after a legacy {@code setLikeExpression} or {@code setWhereCondition} call: an
     * expression replaces the filter, and null removes a filter of that kind only.
     */
    public static ShowFilter replace(ShowFilter filter, Kind kind, Expression expression) {
        if (expression != null) {
            return new ShowFilter(kind, expression);
        }
        return filter != null && filter.kind == kind ? null : filter;
    }

    /** Appends the filter, printing its expression with the given printer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> printer) {
        builder.append(kind).append(' ');
        printer.accept(expression);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    public ShowFilter withKind(Kind kind) {
        this.setKind(kind);
        return this;
    }

    public ShowFilter withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }
}
