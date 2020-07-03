/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.*;

public class AllTableColumns extends ASTNodeAccessImpl implements SelectItem {

    private Table table;

    public AllTableColumns() {
    }

    public AllTableColumns(Table tableName) {
        this.table = tableName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public void accept(SelectItemVisitor selectItemVisitor) {
        selectItemVisitor.visit(this);
    }

    @Override
    public String toString() {
        return table + ".*";
    }

    public AllTableColumns table(Table table) {
        this.setTable(table);
        return this;
    }
}
