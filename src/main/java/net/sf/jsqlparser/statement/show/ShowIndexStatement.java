/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * @author Jayant Kumar Yadav
 */

public class ShowIndexStatement implements Statement {

    private String tableName;

    public ShowIndexStatement() {
        // empty constructor
    }

    public ShowIndexStatement(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "SHOW INDEX FROM " + tableName;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public ShowIndexStatement withTableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }
}
