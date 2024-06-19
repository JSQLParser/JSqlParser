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
import java.util.List;

public class Statements extends ArrayList<Statement> implements Serializable {

    @Deprecated
    public List<Statement> getStatements() {
        return this;
    }

    @Deprecated
    public void setStatements(List<Statement> statements) {
        this.clear();
        this.addAll(statements);
    }

    public void accept(StatementVisitor<?> statementVisitor) {
        statementVisitor.visit(this);
    }

    public <E extends Statement> E get(Class<E> type, int index) {
        return type.cast(get(index));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Statement stmt : this) {
            // IfElseStatements and Blocks control the Semicolons by themselves
            if (stmt instanceof IfElseStatement || stmt instanceof Block) {
                b.append(stmt).append("\n");
            } else {
                b.append(stmt).append(";\n");
            }
        }
        return b.toString();
    }
}
