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

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ConnectionDefinition;
import net.sf.jsqlparser.statement.ErrorClause;
import net.sf.jsqlparser.statement.SourceDestinationType;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.List;

public class DBMSDestination implements ExportIntoItem, Serializable {
    private SourceDestinationType destinationType;
    private ConnectionDefinition connectionDefinition;
    private Table table;
    private ExpressionList<Column> columns;
    private List<DBMSTableDestinationOption> dbmsTableDestinationOptions;
    private StringValue statement;
    private ErrorClause errorClause;

    public SourceDestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(SourceDestinationType destinationType) {
        this.destinationType = destinationType;
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

    public List<DBMSTableDestinationOption> getDBMSTableDestinationOptions() {
        return dbmsTableDestinationOptions;
    }

    public void setDBMSTableDestinationOptions(List<DBMSTableDestinationOption> dbmsTableDestinationOptions) {
        this.dbmsTableDestinationOptions = dbmsTableDestinationOptions;
    }

    public StringValue getStatement() {
        return statement;
    }

    public void setStatement(StringValue statement) {
        this.statement = statement;
    }

    @Override
    public ErrorClause getErrorClause() {
        return errorClause;
    }

    @Override
    public void setErrorClause(ErrorClause errorClause) {
        this.errorClause = errorClause;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(destinationType);

        sql.append(" ");
        sql.append(connectionDefinition);

        if (table != null) {
            sql.append(" TABLE ").append(table);
            PlainSelect.appendStringListTo(sql, columns, true, true);
            if (dbmsTableDestinationOptions != null) {
                sql.append(" ");
                PlainSelect.appendStringListTo(sql, dbmsTableDestinationOptions, false, false);
            }
        } else if (statement != null) {
            sql.append(" STATEMENT ").append(statement);
        }

        if (errorClause != null) {
            sql.append(errorClause);
        }

        return sql.toString();
    }
}
