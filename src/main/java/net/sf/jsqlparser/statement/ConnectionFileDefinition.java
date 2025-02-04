/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

public class ConnectionFileDefinition {
    private ConnectionDefinition connectionDefinition;
    private List<StringValue> filePaths;

    public ConnectionFileDefinition(ConnectionDefinition connectionDefinition, List<StringValue> filePaths) {
        this.connectionDefinition = connectionDefinition;
        this.filePaths = filePaths;
    }

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public List<StringValue> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<StringValue> filePaths) {
        this.filePaths = filePaths;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(connectionDefinition);
        for (StringValue filePath : filePaths) {
            sql.append(" FILE ").append(filePath);
        }

        return sql.toString();
    }
}
