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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.List;

public class AllTableColumns extends AllColumns {

    private Table table;

    public AllTableColumns(Table table, ExpressionList<Column> exceptColumns,
            List<SelectItem<Column>> replaceExpressions, String exceptKeyword) {
        super(exceptColumns, replaceExpressions, exceptKeyword);
        this.table = table;
    }

    public AllTableColumns(Table table, ExpressionList<Column> exceptColumns,
            List<SelectItem<Column>> replaceExpressions) {
        this(table, exceptColumns, replaceExpressions, "EXCEPT");
    }

    public AllTableColumns(Table table) {
        this(table, null, null);
    }

    public AllTableColumns(Table table, AllColumns allColumns) {
        this(table, allColumns.exceptColumns, allColumns.replaceExpressions,
                allColumns.getExceptKeyword());
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public AllTableColumns withTable(Table table) {
        this.setTable(table);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return super.appendTo(table.appendTo(builder).append("."));
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }
}
