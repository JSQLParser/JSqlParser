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
 * Simple uservariables like @test.
 */
public class UserVariable extends ASTNodeAccessImpl implements Expression {

    private String name;

    private boolean doubleAdd = false;

    public UserVariable() {
        // empty constructor
    }

    public UserVariable(String name) {
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

    public boolean isDoubleAdd() {
        return doubleAdd;
    }

    public void setDoubleAdd(boolean doubleAdd) {
        this.doubleAdd = doubleAdd;
    }

    @Override
    public String toString() {
        return "@" + (doubleAdd ? "@" : "") + name;
    }

    public UserVariable withName(String name) {
        this.setName(name);
        return this;
    }

    public UserVariable withDoubleAdd(boolean doubleAdd) {
        this.setDoubleAdd(doubleAdd);
        return this;
    }
}
