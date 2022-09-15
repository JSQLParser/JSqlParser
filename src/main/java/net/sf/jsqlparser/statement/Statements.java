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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Statements implements Serializable {

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
            if (stmt instanceof IfElseStatement) {
                // IfElseStatements print the Semicolons by themselves
                b.append(stmt).append("\n");
            } else {
                b.append(stmt).append(";\n");
            }
        }
        return b.toString();
    }

    public Statements withStatements(List<Statement> statements) {
        this.setStatements(statements);
        return this;
    }

    public Statements addStatements(Statement... statements) {
        List<Statement> collection = Optional.ofNullable(getStatements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, statements);
        return this.withStatements(collection);
    }

    public Statements addStatements(Collection<? extends Statement> statements) {
        List<Statement> collection = Optional.ofNullable(getStatements()).orElseGet(ArrayList::new);
        collection.addAll(statements);
        return this.withStatements(collection);
    }
}
