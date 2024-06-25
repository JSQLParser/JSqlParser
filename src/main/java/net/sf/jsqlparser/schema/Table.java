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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.UnPivot;

/**
 * A table. It can have an alias and the schema name it belongs to.
 */
public class Table extends ASTNodeAccessImpl implements FromItem, MultiPartName {

    // private Database database;
    // private String schemaName;
    // private String name;
    private static final int NAME_IDX = 0;

    private static final int SCHEMA_IDX = 1;

    private static final int DATABASE_IDX = 2;

    private static final int SERVER_IDX = 3;

    private List<String> partItems = new ArrayList<>();

    private List<String> partDelimiters = new ArrayList<>();

    private Alias alias;

    private SampleClause sampleClause;

    private Pivot pivot;

    private UnPivot unpivot;

    private MySQLIndexHint mysqlHints;

    private SQLServerHints sqlServerHints;

    public Table() {}

    public Table(String name) {
        setName(name);
    }

    public Table(String schemaName, String name) {
        setName(name);
        setSchemaName(schemaName);
    }

    public Table(Database database, String schemaName, String name) {
        setName(name);
        setSchemaName(schemaName);
        setDatabase(database);
    }

    public Table(String catalogName, String schemaName, String tableName) {
        setName(tableName);
        setSchemaName(schemaName);
        setDatabase(new Database(catalogName));
    }

    public Table(List<String> partItems) {
        this.partItems = new ArrayList<>(partItems);
        Collections.reverse(this.partItems);
    }

    public Table(List<String> partItems, List<String> partDelimiters) {
        if (partDelimiters.size() != partItems.size() - 1) {
            throw new IllegalArgumentException(
                    "the length of the delimiters list must be 1 less than nameParts");
        }
        this.partItems = new ArrayList<>(partItems);
        this.partDelimiters = new ArrayList<>(partDelimiters);
        Collections.reverse(this.partItems);
        Collections.reverse(this.partDelimiters);
    }

    public String getCatalogName() {
        return getIndex(DATABASE_IDX);
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
    }

    public Table withDatabase(Database database) {
        setDatabase(database);
        return this;
    }

    public void setDatabase(Database database) {
        setIndex(DATABASE_IDX, database.getDatabaseName());
        if (database.getServer() != null) {
            setIndex(SERVER_IDX, database.getServer().getFullyQualifiedName());
        }
    }

    public String getSchemaName() {
        return getIndex(SCHEMA_IDX);
    }

    public Table withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    public void setSchemaName(String schemaName) {
        setIndex(SCHEMA_IDX, schemaName);
    }

    public String getName() {
        String name = getIndex(NAME_IDX);
        if (name != null && name.contains("@")) {
            int pos = name.lastIndexOf('@');
            if (pos > 0) {
                name = name.substring(0, pos);
            }
        }
        return name;
    }

    public String getDBLinkName() {
        String name = getIndex(NAME_IDX);
        if (name != null && name.contains("@")) {
            int pos = name.lastIndexOf('@');
            if (pos > 0) {
                name = name.substring(pos + 1);
            }
        }
        return name;
    }

    public Table withName(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        setIndex(NAME_IDX, name);
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    private void setIndex(int idx, String value) {
        int size = partItems.size();
        for (int i = 0; i < idx - size + 1; i++) {
            partItems.add(null);
        }

        if (value == null && idx == partItems.size() - 1) {
            partItems.remove(idx);
        } else {
            partItems.set(idx, value);
        }
    }

    private String getIndex(int idx) {
        if (idx < partItems.size()) {
            return partItems.get(idx);
        } else {
            return null;
        }
    }

    @Override
    public String getFullyQualifiedName() {
        StringBuilder fqn = new StringBuilder();

        for (int i = partItems.size() - 1; i >= 0; i--) {
            String part = partItems.get(i);
            if (part == null) {
                part = "";
            }
            fqn.append(part);
            if (i != 0) {
                fqn.append(partDelimiters.isEmpty() ? "." : partDelimiters.get(i - 1));
            }
        }

        return fqn.toString();
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S arguments) {
        return fromItemVisitor.visit(this, arguments);
    }

    public <T, S> T accept(IntoTableVisitor<T> intoTableVisitor, S arguments) {
        return intoTableVisitor.visit(this, arguments);
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
    public UnPivot getUnPivot() {
        return this.unpivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unpivot = unpivot;
    }

    public MySQLIndexHint getIndexHint() {
        return mysqlHints;
    }

    public Table withHint(MySQLIndexHint hint) {
        setHint(hint);
        return this;
    }

    public void setHint(MySQLIndexHint hint) {
        this.mysqlHints = hint;
    }

    public SQLServerHints getSqlServerHints() {
        return sqlServerHints;
    }

    public void setSqlServerHints(SQLServerHints sqlServerHints) {
        this.sqlServerHints = sqlServerHints;
    }

    public SampleClause getSampleClause() {
        return sampleClause;
    }

    public Table setSampleClause(SampleClause sampleClause) {
        this.sampleClause = sampleClause;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(getFullyQualifiedName());
        if (alias != null) {
            builder.append(alias);
        }

        if (sampleClause != null) {
            sampleClause.appendTo(builder);
        }

        if (pivot != null) {
            builder.append(" ").append(pivot);
        }

        if (unpivot != null) {
            builder.append(" ").append(unpivot);
        }

        if (mysqlHints != null) {
            builder.append(mysqlHints);
        }

        if (sqlServerHints != null) {
            builder.append(sqlServerHints);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public Table withUnPivot(UnPivot unpivot) {
        return (Table) FromItem.super.withUnPivot(unpivot);
    }

    @Override
    public Table withAlias(Alias alias) {
        return (Table) FromItem.super.withAlias(alias);
    }

    @Override
    public Table withPivot(Pivot pivot) {
        return (Table) FromItem.super.withPivot(pivot);
    }

    public Table withSqlServerHints(SQLServerHints sqlServerHints) {
        this.setSqlServerHints(sqlServerHints);
        return this;
    }

    public List<String> getNameParts() {
        return partItems;
    }

    public List<String> getNamePartDelimiters() {
        return partDelimiters;
    }
}
