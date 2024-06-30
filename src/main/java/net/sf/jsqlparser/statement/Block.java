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

public class Block implements Statement {
    private boolean hasSemicolonAfterEnd = false;

    private Statements statements;

    public Statements getStatements() {
        return statements;
    }

    public void setStatements(Statements statements) {
        this.statements = statements;
    }

    public boolean hasSemicolonAfterEnd() {
        return hasSemicolonAfterEnd;
    }

    public void setSemicolonAfterEnd(boolean hasSemicolonAfterEnd) {
        this.hasSemicolonAfterEnd = hasSemicolonAfterEnd;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("BEGIN\n");
        if (statements != null) {
            builder.append(statements);
        }
        builder.append("END");
        if (hasSemicolonAfterEnd) {
            builder.append(";");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public Block withStatements(Statements statements) {
        this.setStatements(statements);
        return this;
    }
}
