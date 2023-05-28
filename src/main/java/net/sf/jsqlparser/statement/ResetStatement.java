/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

public final class ResetStatement implements Statement {

    private String name = "";

    public ResetStatement() {
        // empty constructor
    }

    public ResetStatement(String name) {
        add(name);
    }

    public void add(String name) {
        setNameProperty(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setNameProperty(name);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("RESET ").append(name);
        return b.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    private void setNameProperty(String name) {
        this.name = name;
    }
}
