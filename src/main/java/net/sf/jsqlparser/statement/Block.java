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

public class Block extends StatementImpl {

    private Statements statements;

    public Statements getStatements() {
        return statements;
    }

    public void setStatements(Statements statements) {
        this.statements = statements;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public StatementType getStatementType() {
        return StatementType.BLOCK;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("BEGIN\n").append(statements != null ? statements.toString() : "").append("END");
        return builder;
    }

    public Block withStatements(Statements statements) {
        this.setStatements(statements);
        return this;
    }
}
