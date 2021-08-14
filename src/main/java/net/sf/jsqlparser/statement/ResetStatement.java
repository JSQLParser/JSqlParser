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


public final class ResetStatement extends DDLStatement {

    private String name;

    public ResetStatement() {
        name = "";
    }

    public ResetStatement(String name) {
        this.name = name;
    }

    @Deprecated
    public void add(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("RESET ").append(name);
        return builder;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

}
