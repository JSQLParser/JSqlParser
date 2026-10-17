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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * A PostgreSQL {@code SET} or {@code RESET} of a configuration parameter, shared by statements that
 * store parameter defaults, such as {@code ALTER ROLE} and {@code ALTER DATABASE}:
 *
 * <pre>
 * SET parameter { TO | = } { value [, ...] | DEFAULT }
 * SET parameter FROM CURRENT
 * RESET { parameter | ALL }
 * </pre>
 *
 * Parameter names keep their SQL spelling, including qualifiers and identifier quotes.
 * {@code RESET ALL} uses the parameter name {@code ALL}.
 */
public class ConfigurationParameterChange implements Serializable {

    public enum Action {
        SET, RESET
    }

    /** What a {@code SET} assigns. Only the selected kind is rendered. */
    public enum ValueKind {
        /** The explicit values of {@link #getValues()}. */
        VALUES,
        /** {@code TO DEFAULT} or {@code = DEFAULT}. */
        DEFAULT,
        /** {@code FROM CURRENT}. */
        FROM_CURRENT
    }

    private Action action = Action.SET;
    private String parameter;
    private ValueKind valueKind = ValueKind.VALUES;
    private boolean useEquals;
    private ExpressionList<Expression> values;

    public ConfigurationParameterChange() {}

    public ConfigurationParameterChange(Action action, String parameter) {
        this.action = action;
        this.parameter = parameter;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public ValueKind getValueKind() {
        return valueKind;
    }

    /**
     * Selects what a {@code SET} assigns. The values are retained when another kind is selected.
     *
     * @param valueKind the assigned value kind
     */
    public void setValueKind(ValueKind valueKind) {
        this.valueKind = valueKind;
    }

    /**
     * Whether {@code =} rather than {@code TO} precedes the value.
     *
     * @return {@code true} for {@code =}
     */
    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    /**
     * The values assigned by {@link ValueKind#VALUES}, as a mutable list.
     *
     * @return the values, or {@code null} if none were set
     */
    public ExpressionList<Expression> getValues() {
        return values;
    }

    public void setValues(ExpressionList<Expression> values) {
        this.values = values;
    }

    public ConfigurationParameterChange withAction(Action action) {
        setAction(action);
        return this;
    }

    public ConfigurationParameterChange withParameter(String parameter) {
        setParameter(parameter);
        return this;
    }

    public ConfigurationParameterChange withValueKind(ValueKind valueKind) {
        setValueKind(valueKind);
        return this;
    }

    public ConfigurationParameterChange withUseEquals(boolean useEquals) {
        setUseEquals(useEquals);
        return this;
    }

    public ConfigurationParameterChange withValues(ExpressionList<Expression> values) {
        setValues(values);
        return this;
    }

    /**
     * Appends this clause to a builder.
     *
     * @param builder the target
     * @param valueRenderer appends each value, for example through an expression deparser
     * @return the builder
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> valueRenderer) {
        if (action == Action.RESET) {
            return builder.append("RESET ").append(parameter);
        }
        builder.append("SET ").append(parameter);
        if (valueKind == ValueKind.FROM_CURRENT) {
            return builder.append(" FROM CURRENT");
        }
        builder.append(useEquals ? " = " : " TO ");
        if (valueKind == ValueKind.DEFAULT) {
            return builder.append("DEFAULT");
        }
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                valueRenderer.accept(values.get(i));
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
