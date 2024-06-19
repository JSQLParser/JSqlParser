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

public class ShowColumnsStatement implements Statement {

    private String tableName;

    public ShowColumnsStatement() {
        // empty constructor
    }

    public ShowColumnsStatement(String tableName) {
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
        return "SHOW COLUMNS FROM " + tableName;
    }

    @Override
    public <T> T accept(StatementVisitor<T> statementVisitor) {
        return statementVisitor.visit(this);
    }

    public ShowColumnsStatement withTableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }
}
