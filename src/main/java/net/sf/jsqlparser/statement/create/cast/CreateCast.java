/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.cast;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.CastSignature;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL
 * {@code CREATE CAST (source AS target) {WITH FUNCTION f [(args)] | WITHOUT FUNCTION | WITH INOUT} [AS ASSIGNMENT | AS IMPLICIT]}.
 * The cast is named by the {@link CastSignature} that {@code DROP CAST} uses.
 */
public class CreateCast extends ASTNodeAccessImpl implements Statement {
    /** How the cast converts a value. */
    public enum Method {
        WITH_FUNCTION("WITH FUNCTION"), WITHOUT_FUNCTION("WITHOUT FUNCTION"), WITH_INOUT(
                "WITH INOUT");

        private final String sql;

        Method(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return sql;
        }
    }

    /** Where the cast applies without being written; an explicit-only cast has no context. */
    public enum Context {
        ASSIGNMENT, IMPLICIT
    }

    private CastSignature cast;
    private Method method;
    private RoutineReference function;
    private Context context;

    public CastSignature getCast() {
        return cast;
    }

    public void setCast(CastSignature cast) {
        this.cast = cast;
    }

    public CreateCast withCast(CastSignature cast) {
        setCast(cast);
        return this;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Sets how the cast converts a value. Only {@link Method#WITH_FUNCTION} has a function, so
     * another method clears it.
     *
     * @param method the conversion method
     */
    public void setMethod(Method method) {
        this.method = method;
        if (method != Method.WITH_FUNCTION) {
            function = null;
        }
    }

    public CreateCast withMethod(Method method) {
        setMethod(method);
        return this;
    }

    /**
     * The function of {@code WITH FUNCTION}. Its arguments are {@code null} when the statement
     * names the function without an argument list.
     *
     * @return the function, or {@code null} for {@code WITHOUT FUNCTION} and {@code WITH INOUT}
     */
    public RoutineReference getFunction() {
        return function;
    }

    /**
     * Sets the conversion function; a function makes the method {@link Method#WITH_FUNCTION}.
     *
     * @param function the function
     */
    public void setFunction(RoutineReference function) {
        this.function = function;
        if (function != null) {
            method = Method.WITH_FUNCTION;
        }
    }

    public CreateCast withFunction(RoutineReference function) {
        setFunction(function);
        return this;
    }

    /**
     * The context of {@code AS ASSIGNMENT} or {@code AS IMPLICIT}.
     *
     * @return the context, or {@code null} for a cast that applies only when it is written
     */
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public CreateCast withContext(Context context) {
        setContext(context);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE CAST ");
        cast.appendTo(builder).append(' ').append(method);
        if (method == Method.WITH_FUNCTION) {
            builder.append(' ').append(function);
        }
        if (context != null) {
            builder.append(" AS ").append(context);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
