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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class JdbcNamedParameter extends ASTNodeAccessImpl implements Expression {
    private String parameterCharacter = ":";
    private String name;

    public JdbcNamedParameter() {}

    public JdbcNamedParameter(String name) {
        this.name = name;
    }

    public String getParameterCharacter() {
        return parameterCharacter;
    }

    public JdbcNamedParameter setParameterCharacter(String parameterCharacter) {
        this.parameterCharacter = parameterCharacter;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public String toString() {
        return parameterCharacter + name;
    }

    public JdbcNamedParameter withName(String name) {
        this.setName(name);
        return this;
    }
}
