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

import net.sf.jsqlparser.statement.select.Select;

public class ExplainStatement implements Statement {

    private Select select;

    public ExplainStatement(Select select) {
        this.select = select;
    }

    public Select getStatement() {
        return select;
    }

    public void setStatement(Select select) {
        this.select = select;
    }

    @Override
    public String toString() {
        return "EXPLAIN " + select.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
}
