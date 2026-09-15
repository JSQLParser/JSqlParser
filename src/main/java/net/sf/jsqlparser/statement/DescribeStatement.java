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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;

public class DescribeStatement implements Statement {

    private Table table;
    private Select select;
    private String describeType;

    public DescribeStatement() {
        // empty constructor
    }

    public DescribeStatement(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    /** DuckDB also describes a query: {@code DESCRIBE SELECT ...}. */
    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public DescribeStatement withSelect(Select select) {
        setSelect(select);
        return this;
    }

    @Override
    public String toString() {
        return this.describeType + " "
                + (select != null ? select.toString() : table.getFullyQualifiedName());
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public DescribeStatement withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public String getDescribeType() {
        return describeType;
    }

    public DescribeStatement setDescribeType(String describeType) {
        this.describeType = describeType;
        return this;
    }
}
