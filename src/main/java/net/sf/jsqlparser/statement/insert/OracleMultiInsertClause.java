/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import java.io.Serializable;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;

public class OracleMultiInsertClause implements Serializable {

    private Table table;
    private ExpressionList<Column> columns;
    private Select select;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    /** Appends {@code INTO table [(columns)] query}, letting a deparser visit the query. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Select> selectRenderer) {
        builder.append("INTO ").append(table);
        if (columns != null && !columns.isEmpty()) {
            builder.append(" ");
            Insert.appendColumnNamesTo(builder, columns);
        }
        if (select != null) {
            builder.append(" ");
            selectRenderer.accept(select);
        }
        return builder;
    }

    public OracleMultiInsertClause withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public OracleMultiInsertClause withColumns(ExpressionList<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public OracleMultiInsertClause withSelect(Select select) {
        this.setSelect(select);
        return this;
    }
}
