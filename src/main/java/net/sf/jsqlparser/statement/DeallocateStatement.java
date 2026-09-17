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

/**
 * {@code DEALLOCATE [PREPARE] name}, which drops a statement prepared by {@link PrepareStatement}.
 */
public class DeallocateStatement implements Statement {
    private String name;
    private boolean usingPrepareKeyword;

    public DeallocateStatement() {}

    public DeallocateStatement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeallocateStatement withName(String name) {
        setName(name);
        return this;
    }

    public boolean isUsingPrepareKeyword() {
        return usingPrepareKeyword;
    }

    public void setUsingPrepareKeyword(boolean usingPrepareKeyword) {
        this.usingPrepareKeyword = usingPrepareKeyword;
    }

    public DeallocateStatement withUsingPrepareKeyword(boolean usingPrepareKeyword) {
        setUsingPrepareKeyword(usingPrepareKeyword);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("DEALLOCATE ");
        if (usingPrepareKeyword) {
            builder.append("PREPARE ");
        }
        builder.append(name);
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
}
