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
import java.util.Iterator;
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
        StringBuilder sql = new StringBuilder("INTO ");
        sql.append(table);

        if (columns != null && !columns.isEmpty()) {
            sql.append(" (");
            for (Iterator<Column> iter = columns.iterator(); iter.hasNext();) {
                Column column = iter.next();
                sql.append(column.getColumnName());
                if (iter.hasNext()) {
                    sql.append(", ");
                }
            }
            sql.append(")");
        }

        if (select != null) {
            sql.append(" ").append(select);
        }

        return sql.toString();
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
