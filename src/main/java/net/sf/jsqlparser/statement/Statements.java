/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.List;

/**
 *
 * @author toben
 */
public class Statements {

    private List<Statement> statements;

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Statement stmt : statements) {
            b.append(stmt.toString()).append(";\n");
        }
        return b.toString();
    }
}
