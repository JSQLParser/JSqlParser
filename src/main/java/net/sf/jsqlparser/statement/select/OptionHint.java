/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * Models one hint of the SQL Server (T-SQL) {@code OPTION (...)} query hint clause, see
 * <a href="https://learn.microsoft.com/en-us/sql/t-sql/queries/hints-transact-sql-query">Hints
 * (Transact-SQL) - Query Hints</a>.
 * <p>
 * A hint is a (possibly multi-word) keyword such as {@code RECOMPILE}, {@code HASH JOIN} or
 * {@code OPTIMIZE FOR UNKNOWN}, optionally followed by a single value argument ({@code FAST 100},
 * {@code MAXDOP 4}, {@code MAX_GRANT_PERCENT = 25}) or by a parenthesized argument list
 * ({@code USE HINT ('...')}, {@code OPTIMIZE FOR (@p = 1)}, {@code TABLE HINT (t, INDEX (i))}).
 */
public class OptionHint implements Serializable {

    private String name;
    private Expression value;
    private ExpressionList<Expression> parameters;
    private boolean useEquals = false;

    public OptionHint() {
        super();
    }

    public OptionHint(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OptionHint withName(String name) {
        setName(name);
        return this;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public OptionHint withValue(Expression value) {
        setValue(value);
        return this;
    }

    public ExpressionList<Expression> getParameters() {
        return parameters;
    }

    public void setParameters(ExpressionList<Expression> parameters) {
        this.parameters = parameters;
    }

    public OptionHint withParameters(ExpressionList<Expression> parameters) {
        setParameters(parameters);
        return this;
    }

    public OptionHint addParameter(Expression parameter) {
        if (parameters == null) {
            parameters = new ExpressionList<>();
        }
        parameters.add(parameter);
        return this;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    public OptionHint withUseEquals(boolean useEquals) {
        setUseEquals(useEquals);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(name);
        if (value != null) {
            b.append(useEquals ? " = " : " ").append(value);
        }
        if (parameters != null) {
            b.append(" (").append(Select.getStringList(parameters, true, false)).append(')');
        }
        return b.toString();
    }
}
