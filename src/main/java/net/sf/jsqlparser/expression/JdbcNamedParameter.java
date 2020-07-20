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

/**
 * A ':'<name> in a statement, e.g. :id
 */
public class JdbcNamedParameter extends ASTNodeAccessImpl implements Expression {

    private String name;

    public JdbcNamedParameter() {
    }

    public JdbcNamedParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return ":" + name;
    }

    public JdbcNamedParameter withName(String name) {
        this.setName(name);
        return this;
    }
}
