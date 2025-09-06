/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreateSchema implements Statement {

    private String authorization;
    private String catalogName = null;
    private String schemaName;
    private List<String> schemaPath;
    private List<Statement> statements = new ArrayList<>();
    private boolean hasIfNotExists = false;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /**
     * Add a statement to the schema definition
     *
     * @param statement The statement to be added
     * @return true if the operation was successful
     */
    public boolean addStatement(Statement statement) {
        return statements.add(statement);
    }

    /**
     * The owner of the schema.
     *
     * @return Owner name
     */
    public String getAuthorization() {
        return authorization;
    }

    /**
     * The owner of the schems.
     *
     * @param authorization Owner name
     */
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public CreateSchema setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    /**
     * The name of the schema
     *
     * @return Schema name
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Set the name of the schema
     *
     * @param schemaName Schema name
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * The path of the schema
     *
     * @return Schema path
     */
    public List<String> getSchemaPath() {
        return schemaPath;
    }

    /**
     * Set the path of the schema
     *
     * @param schemaPath Schema path
     */
    public void setSchemaPath(List<String> schemaPath) {
        this.schemaPath = schemaPath;
    }

    /**
     * The statements executed as part of the schema creation
     *
     * @return the statements
     */
    public List<Statement> getStatements() {
        return statements;
    }

    public boolean hasIfNotExists() {
        return hasIfNotExists;
    }

    public CreateSchema setIfNotExists(boolean hasIfNotExists) {
        this.hasIfNotExists = hasIfNotExists;
        return this;
    }

    public String toString() {
        String sql = "CREATE SCHEMA";
        if (hasIfNotExists) {
            sql += " IF NOT EXISTS";
        }
        if (schemaName != null) {
            sql += " ";

            if (catalogName!=null) {
                sql += catalogName + ".";
            }
            sql += schemaName;
        }
        if (authorization != null) {
            sql += " AUTHORIZATION " + authorization;
        }
        return sql;
    }

    public CreateSchema withAuthorization(String authorization) {
        this.setAuthorization(authorization);
        return this;
    }

    public CreateSchema withSchemaName(String schemaName) {
        this.setSchemaName(schemaName);
        return this;
    }

    public CreateSchema withSchemaPath(List<String> schemaPath) {
        this.setSchemaPath(schemaPath);
        return this;
    }

    public CreateSchema addSchemaPath(String... schemaPath) {
        List<String> collection = Optional.ofNullable(getSchemaPath()).orElseGet(ArrayList::new);
        Collections.addAll(collection, schemaPath);
        return this.withSchemaPath(collection);
    }

    public CreateSchema addSchemaPath(Collection<String> schemaPath) {
        List<String> collection = Optional.ofNullable(getSchemaPath()).orElseGet(ArrayList::new);
        collection.addAll(schemaPath);
        return this.withSchemaPath(collection);
    }
}
