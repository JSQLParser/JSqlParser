/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

public final class Database implements MultiPartName {

    private Server server;
    private String databaseName;

    public Database(String databaseName) {
        setDatabaseName(databaseName);
    }

    public Database(Server server, String databaseName) {
        setServer(server);
        setDatabaseName(databaseName);
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getFullyQualifiedName() {
        String fqn = "";

        if (server != null) {
            fqn += server.getFullyQualifiedName();
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (databaseName != null) {
            fqn += databaseName;
        }

        return fqn;
    }

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }

    public Database withServer(Server server) {
        this.setServer(server);
        return this;
    }

    public Database withDatabaseName(String databaseName) {
        this.setDatabaseName(databaseName);
        return this;
    }
}
