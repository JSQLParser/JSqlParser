/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.export;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.*;

public class Export implements Statement {
    private Table table;
    private ExpressionList<Column> columns;
    private Select select;
    private ExportIntoItem exportIntoItem;

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

    public ExportIntoItem getExportIntoItem() {
        return exportIntoItem;
    }

    public void setExportIntoItem(ExportIntoItem exportIntoItem) {
        this.exportIntoItem = exportIntoItem;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("EXPORT ");
        if (table != null || select != null) {
            if (table != null) {
                sql.append(table);
                PlainSelect.appendStringListTo(sql, columns, true, true);
            } else {
                sql.append(select);
            }
            sql.append(" ");
        }

        sql.append("INTO ");
        sql.append(exportIntoItem);

        return sql.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
