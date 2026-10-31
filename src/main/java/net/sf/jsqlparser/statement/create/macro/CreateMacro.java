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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.Select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DuckDB's
 * {@code CREATE [OR REPLACE] [TEMPORARY] {MACRO | FUNCTION} [IF NOT EXISTS] name (parameters) AS [TABLE] body [, (parameters) AS [TABLE] body ...]},
 * a named expression or query that is inlined at call sites, optionally overloaded on its
 * parameters.
 *
 * <p>
 * {@link #getParameters()}, {@link #getExpression()} and {@link #getSelect()} and their setters
 * address the first overload.
 *
 * <p>
 * As in DuckDB, a {@code ,} after a {@code TABLE} body that ends with a list (FROM, GROUP BY, ORDER
 * BY ...) continues that list rather than starting a new overload.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/create_macro">CREATE MACRO</a>
 */
public class CreateMacro extends ASTNodeAccessImpl implements Statement {
    private boolean orReplace;
    private boolean temporary;
    private boolean functionKeyword;
    private boolean ifNotExists;
    private String name;
    private List<Overload> overloads = new ArrayList<>();

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

    /** Whether it was written {@code CREATE FUNCTION}, DuckDB's alias of {@code CREATE MACRO}. */
    public boolean isFunctionKeyword() {
        return functionKeyword;
    }

    public void setFunctionKeyword(boolean functionKeyword) {
        this.functionKeyword = functionKeyword;
    }

    public CreateMacro withFunctionKeyword(boolean functionKeyword) {
        setFunctionKeyword(functionKeyword);
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public CreateMacro withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
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

    public List<Overload> getOverloads() {
        return overloads;
    }

    /** Copies {@code overloads}, so that the first-overload setters can add to an empty list. */
    public void setOverloads(List<Overload> overloads) {
        this.overloads = overloads == null ? new ArrayList<>() : new ArrayList<>(overloads);
    }

    public CreateMacro withOverloads(List<Overload> overloads) {
        setOverloads(overloads);
        return this;
    }

    private Overload firstOverload() {
        if (overloads.isEmpty()) {
            overloads.add(new Overload());
        }
        return overloads.get(0);
    }

    public List<Parameter> getParameters() {
        return overloads.isEmpty() ? null : overloads.get(0).getParameters();
    }

    public void setParameters(List<Parameter> parameters) {
        firstOverload().setParameters(parameters);
    }

    public CreateMacro withParameters(List<Parameter> parameters) {
        setParameters(parameters);
        return this;
    }

    public Expression getExpression() {
        return overloads.isEmpty() ? null : overloads.get(0).getExpression();
    }

    public void setExpression(Expression expression) {
        firstOverload().setExpression(expression);
    }

    public CreateMacro withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public Select getSelect() {
        return overloads.isEmpty() ? null : overloads.get(0).getSelect();
    }

    public void setSelect(Select select) {
        firstOverload().setSelect(select);
    }

    public CreateMacro withSelect(Select select) {
        setSelect(select);
        return this;
    }

    /** A table macro returns a relation, a scalar macro an expression. */
    public boolean isTable() {
        return getSelect() != null;
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
        builder.append(functionKeyword ? "FUNCTION " : "MACRO ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(name).append(" ");
        for (int i = 0; i < overloads.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            overloads.get(i).appendTo(builder, expressionRenderer, selectRenderer);
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

    /** One {@code (parameters) AS [TABLE] body} of a macro. */
    public static class Overload implements Serializable {
        private List<Parameter> parameters = new ArrayList<>();
        private Expression expression;
        private Select select;

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public Overload withParameters(List<Parameter> parameters) {
            setParameters(parameters);
            return this;
        }

        public Expression getExpression() {
            return expression;
        }

        public void setExpression(Expression expression) {
            this.expression = expression;
        }

        public Overload withExpression(Expression expression) {
            setExpression(expression);
            return this;
        }

        public Select getSelect() {
            return select;
        }

        public void setSelect(Select select) {
            this.select = select;
        }

        public Overload withSelect(Select select) {
            setSelect(select);
            return this;
        }

        /** A table overload returns a relation, a scalar one an expression. */
        public boolean isTable() {
            return select != null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return appendTo(builder, builder::append, builder::append).toString();
        }

        private StringBuilder appendTo(StringBuilder builder,
                Consumer<Expression> expressionRenderer, Consumer<Select> selectRenderer) {
            builder.append("(");
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
    }

    /**
     * A macro parameter, optionally typed ({@code a INTEGER}) and carrying a default value
     * ({@code b := 5}).
     */
    public static class Parameter implements Serializable {
        private final String name;
        private final ColDataType type;
        private final Expression defaultValue;

        public Parameter(String name) {
            this(name, null, null);
        }

        public Parameter(String name, Expression defaultValue) {
            this(name, null, defaultValue);
        }

        public Parameter(String name, ColDataType type, Expression defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public ColDataType getType() {
            return type;
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
            if (type != null) {
                builder.append(" ").append(type);
            }
            if (defaultValue != null) {
                builder.append(" := ");
                expressionRenderer.accept(defaultValue);
            }
            return builder;
        }
    }
}
