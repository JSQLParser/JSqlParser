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
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/**
 * One assignment of the {@code SET} clause of MySQL's {@code INSTALL COMPONENT}:
 * {@code [GLOBAL | PERSIST] variable {= | :=} value}. The value is an expression, or the word
 * {@code ON}, which is read as a column.
 *
 * @see PluginStatement
 */
public class ComponentVariable implements Serializable {
    private SetScope scope = SetScope.NONE;
    private String name;
    private SetStatement.AssignmentOperator operator = SetStatement.AssignmentOperator.EQUALS;
    private Expression value;

    public ComponentVariable(String name, Expression value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = Objects.requireNonNull(value, "value");
    }

    /** {@link SetScope#GLOBAL}, {@link SetScope#PERSIST}, or {@link SetScope#NONE} if not given. */
    public SetScope getScope() {
        return scope;
    }

    /**
     * Sets the scope keyword; null means none.
     *
     * @throws IllegalArgumentException for a scope other than GLOBAL, PERSIST or NONE
     */
    public void setScope(SetScope scope) {
        SetScope value = scope == null ? SetScope.NONE : scope;
        if (value != SetScope.NONE && value != SetScope.GLOBAL && value != SetScope.PERSIST) {
            throw new IllegalArgumentException("INSTALL COMPONENT SET does not accept " + value);
        }
        this.scope = value;
    }

    public ComponentVariable withScope(SetScope scope) {
        setScope(scope);
        return this;
    }

    /** The variable name as written: {@code variable} or {@code component.variable}. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** {@code =} or {@code :=}. */
    public SetStatement.AssignmentOperator getOperator() {
        return operator;
    }

    /**
     * Sets the assignment operator.
     *
     * @throws IllegalArgumentException for an operator other than EQUALS or COLON_EQUALS
     */
    public void setOperator(SetStatement.AssignmentOperator operator) {
        if (operator != SetStatement.AssignmentOperator.EQUALS
                && operator != SetStatement.AssignmentOperator.COLON_EQUALS) {
            throw new IllegalArgumentException("INSTALL COMPONENT SET assigns with = or :=");
        }
        this.operator = operator;
    }

    public ComponentVariable withOperator(SetStatement.AssignmentOperator operator) {
        setOperator(operator);
        return this;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (scope != SetScope.NONE) {
            builder.append(scope.getKeyword()).append(' ');
        }
        builder.append(name).append(' ').append(operator.getToken()).append(' ');
        expressionPrinter.accept(value);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
