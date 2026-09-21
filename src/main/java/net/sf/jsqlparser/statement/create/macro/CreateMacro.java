/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.macro;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Select;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * DuckDB's {@code CREATE [OR REPLACE] [TEMPORARY] MACRO name (parameters) AS [TABLE] body}, a named
 * expression or query that is inlined at call sites.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/create_macro">CREATE MACRO</a>
 */
public class CreateMacro implements Statement {
    private boolean orReplace;
    private boolean temporary;
    private String name;
    private List<Parameter> parameters;
    private Expression expression;
    private Select select;

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public CreateMacro withOrReplace(boolean orReplace) {
        setOrReplace(orReplace);
        return this;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public CreateMacro withTemporary(boolean temporary) {
        setTemporary(temporary);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateMacro withName(String name) {
        setName(name);
        return this;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public CreateMacro withParameters(List<Parameter> parameters) {
        setParameters(parameters);
        return this;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public CreateMacro withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public CreateMacro withSelect(Select select) {
        setSelect(select);
        return this;
    }

    /** A table macro returns a relation, a scalar macro an expression. */
    public boolean isTable() {
        return select != null;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append, builder::append);
    }

    /** Shares punctuation while exposing parameter defaults and both kinds of macro body. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer,
            Consumer<Select> selectRenderer) {
        builder.append("CREATE ");
        if (orReplace) {
            builder.append("OR REPLACE ");
        }
        if (temporary) {
            builder.append("TEMPORARY ");
        }
        builder.append("MACRO ").append(name).append(" (");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            parameters.get(i).appendTo(builder, expressionRenderer);
        }
        builder.append(") AS ");
        if (select != null) {
            builder.append("TABLE ");
            selectRenderer.accept(select);
        } else {
            expressionRenderer.accept(expression);
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

    /** A macro parameter, optionally carrying a default value ({@code b := 5}). */
    public static class Parameter implements Serializable {
        private final String name;
        private final Expression defaultValue;

        public Parameter(String name) {
            this(name, null);
        }

        public Parameter(String name, Expression defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public Expression getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return appendTo(builder, builder::append).toString();
        }

        private StringBuilder appendTo(StringBuilder builder,
                Consumer<Expression> expressionRenderer) {
            builder.append(name);
            if (defaultValue != null) {
                builder.append(" := ");
                expressionRenderer.accept(defaultValue);
            }
            return builder;
        }
    }
}
