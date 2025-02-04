/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.imprt;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

public class Import implements Statement {
    private Table table;
    private ExpressionList<Column> columns;
    private List<ImportColumn> importColumns;
    private ImportFromItem fromItem;

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

    public List<ImportColumn> getImportColumns() {
        return importColumns;
    }

    public void setImportColumns(List<ImportColumn> importColumns) {
        this.importColumns = importColumns;
    }

    public ImportFromItem getFromItem() {
        return fromItem;
    }

    public void setFromItem(ImportFromItem fromItem) {
        this.fromItem = fromItem;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("IMPORT ");
        if (table != null || importColumns != null) {
            sql.append("INTO ");
            if (table != null) {
                sql.append(table);
                PlainSelect.appendStringListTo(sql, columns, true, true);
            } else {
                PlainSelect.appendStringListTo(sql, importColumns, true, true);
            }
        }

        sql.append(" FROM ");
        sql.append(fromItem);

        return sql.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
