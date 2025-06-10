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

public class CSVFileDestination implements ErrorDestination {
    private ConnectionDefinition connectionDefinition;
    private boolean local;
    private boolean secure;
    private StringValue file;

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public StringValue getFile() {
        return file;
    }

    public void setFile(StringValue file) {
        this.file = file;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (local) {
            sql.append("LOCAL ");
            if (secure) {
                sql.append("SECURE ");
            }
        }

        sql.append("CSV");

        if (connectionDefinition != null) {
            sql.append(" ");
            sql.append(connectionDefinition);
        }

        sql.append(" FILE ");
        sql.append(file);

        return sql.toString();
    }
}
