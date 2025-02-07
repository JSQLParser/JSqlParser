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

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ConnectionDefinition;
import net.sf.jsqlparser.statement.SourceDestinationType;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.List;

public class DBMSSource extends ImportFromItem implements Serializable {
    private SourceDestinationType sourceType;
    private ConnectionDefinition connectionDefinition;
    private Table table;
    private ExpressionList<Column> columns;
    private List<StringValue> statements;

    public SourceDestinationType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceDestinationType sourceType) {
        this.sourceType = sourceType;
    }

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

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

    public List<StringValue> getStatements() {
        return statements;
    }

    public void setStatements(List<StringValue> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(sourceType);

        sql.append(" ");
        sql.append(connectionDefinition);

        if (table != null) {
            sql.append(" TABLE ").append(table);
            PlainSelect.appendStringListTo(sql, columns, true, true);
        } else if (statements != null) {
            for (StringValue statement : statements) {
                sql.append(" STATEMENT ").append(statement);
            }
        }

        if (errorClause != null) {
            sql.append(errorClause);
        }

        return sql.toString();
    }
}
