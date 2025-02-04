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
    private Table intoTable;
    private ExpressionList<Column> columnList;
    private List<ImportColumn> importColumns;
    private ImportFromItem fromItem;

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("IMPORT ");
        if (intoTable != null || importColumns != null) {
            sql.append("INTO ");
            if (intoTable != null) {
                sql.append(intoTable);
                PlainSelect.appendStringListTo(sql, columnList, true, true);
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
