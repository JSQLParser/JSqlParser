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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.ErrorDestination;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.UnPivot;

/**
 * A table. It can have an alias and the schema name it belongs to.
 */
public class Table extends ASTNodeAccessImpl
        implements ErrorDestination, FromItem, MultiPartName, Cloneable {

    private static final int NAME_IDX = 0;

    private static final int SCHEMA_IDX = 1;

    private static final int DATABASE_IDX = 2;

    private static final int SERVER_IDX = 3;

    private List<String> partItems = new ArrayList<>();

    private List<String> partDelimiters = new ArrayList<>();

    // holds the various `time travel` syntax for BigQuery, RedShift, Snowflake or RedShift
    private String timeTravelStr = null;

    private Alias alias;

    // thank you, Google!
    private String timeTravelStrAfterAlias = null;

    private SampleClause sampleClause;

    private Pivot pivot;

    private UnPivot unpivot;

    private MySQLIndexHint mysqlHints;

    private SQLServerHints sqlServerHints;

    // holds the physical table when resolved against an actual schema information
    private Table resolvedTable = null;

    public Table() {}

    /**
     * Instantiates a new Table.
     *
     * Sets the table name, splitting it into parts (catalog, schema, name) on `.` dots when quoted
     * unless the system property `SPLIT_NAMES_ON_DELIMITER` points to `FALSE`
     *
     * @param name the table name, optionally quoted
     */
    public Table(String name) {
        setName(name);
    }

    public Table(String name, boolean splitNamesOnDelimiter) {
        setName(name, splitNamesOnDelimiter);
    }

    public Table(String schemaName, String name) {
        setSchemaName(schemaName);
        setName(name);
    }

    public Table(Database database, String schemaName, String name) {
        setDatabase(database);
        setSchemaName(schemaName);
        setName(name);
    }

    public Table(String catalogName, String schemaName, String tableName) {
        setSchemaName(schemaName);
        setDatabase(new Database(catalogName));
        setName(tableName);
    }

    public Table(List<String> partItems) {
        if (partItems.size() == 1) {
            setName(partItems.get(0));
        } else {
            this.partItems = new ArrayList<>(partItems);
            Collections.reverse(this.partItems);
        }
    }

    public Table(List<String> partItems, List<String> partDelimiters) {
        if (partItems.size() == 1) {
            setName(partItems.get(0));
        } else {
            if (partDelimiters.size() != partItems.size() - 1) {
                throw new IllegalArgumentException(
                        "the length of the delimiters list must be 1 less than nameParts");
            }
            this.partItems = new ArrayList<>(partItems);
            this.partDelimiters = new ArrayList<>(partDelimiters);
            Collections.reverse(this.partItems);
            Collections.reverse(this.partDelimiters);
        }
    }

    public String getCatalogName() {
        return getIndex(DATABASE_IDX);
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
    }

    public String getDatabaseName() {
        return getIndex(DATABASE_IDX);
    }

    public String getUnquotedCatalogName() {
        return MultiPartName.unquote(getDatabaseName());
    }

    public String getUnquotedDatabaseName() {
        return MultiPartName.unquote(getDatabaseName());
    }

    public void setDatabase(Database database) {
        setIndex(DATABASE_IDX, database.getDatabaseName());
        if (database.getServer() != null) {
            setIndex(SERVER_IDX, database.getServer().getFullyQualifiedName());
        }
    }

    public Table setDatabaseName(String databaseName) {
        this.setDatabase(new Database(databaseName));
        return this;
    }

    public Table withDatabase(Database database) {
        setDatabase(database);
        return this;
    }

    public String getSchemaName() {
        return getIndex(SCHEMA_IDX);
    }

    public String getUnquotedSchemaName() {
        return MultiPartName.unquote(getSchemaName());
    }

    public Table setSchemaName(String schemaName) {
        this.setIndex(SCHEMA_IDX, schemaName);
        return this;
    }

    public Table withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
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


    /**
     * Sets the table name, splitting it into parts (catalog, schema, name) on `.` dots when quoted
     * unless the system property `SPLIT_NAMES_ON_DELIMITER` points to `FALSE`
     *
     * @param name the table name, optionally quoted
     */
    public void setName(String name) {
        // BigQuery seems to allow things like: `catalogName.schemaName.tableName` in only one pair
        // of quotes
        // however, some people believe that Dots in Names are a good idea, so provide a switch-off
        boolean splitNamesOnDelimiter = System.getProperty("SPLIT_NAMES_ON_DELIMITER") == null ||
                !List
                        .of("0", "N", "n", "FALSE", "false", "OFF", "off")
                        .contains(System.getProperty("SPLIT_NAMES_ON_DELIMITER"));

        setName(name, splitNamesOnDelimiter);
    }

    public void setName(String name, boolean splitNamesOnDelimiter) {
        if (MultiPartName.isQuoted(name) && name.contains(".") && splitNamesOnDelimiter) {
            partItems.clear();
            for (String unquotedIdentifier : MultiPartName.unquote(name).split("\\.")) {
                partItems.add("\"" + unquotedIdentifier + "\"");
            }
            Collections.reverse(partItems);
        } else if (name.contains(".") && splitNamesOnDelimiter) {
            partItems.clear();
            partItems.addAll(Arrays.asList(MultiPartName.unquote(name).split("\\.")));
            Collections.reverse(partItems);
        } else {
            setIndex(NAME_IDX, name);
        }
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

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public String getTimeTravelStrAfterAlias() {
        return timeTravelStrAfterAlias;
    }

    public Table setTimeTravelStrAfterAlias(String timeTravelStrAfterAlias) {
        this.timeTravelStrAfterAlias = timeTravelStrAfterAlias;
        return this;
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

        // remove any leading empty items
        // only middle items can be suppressed (e.g. dbo..MY_TABLE )
        while (!partItems.isEmpty() && (partItems.get(partItems.size() - 1) == null
                || partItems.get(partItems.size() - 1).isEmpty())) {
            partItems.remove(partItems.size() - 1);
        }

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
    public String getUnquotedName() {
        return MultiPartName.unquote(getName());
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    public <T, S> T accept(IntoTableVisitor<T> intoTableVisitor, S context) {
        return intoTableVisitor.visit(this, context);
    }

    public String getTimeTravel() {
        return timeTravelStr;
    }

    public Table setTimeTravel(String timeTravelStr) {
        this.timeTravelStr = timeTravelStr;
        return this;
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

        if (timeTravelStr != null) {
            builder.append(" ").append(timeTravelStr);
        }

        if (alias != null) {
            builder.append(alias);
        }

        if (timeTravelStrAfterAlias != null) {
            builder.append(" ").append(timeTravelStrAfterAlias);
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

    /**
     * Gets the actual table when resolved against a physical schema information.
     *
     * @return the actual table when resolved against a physical schema information
     */
    public Table getResolvedTable() {
        return resolvedTable;
    }

    /**
     * Sets resolved table.
     *
     * @param resolvedTable the resolved table
     * @return this table
     */
    public Table setResolvedTable(Table resolvedTable) {
        // clone, not reference
        if (resolvedTable != null) {
            this.resolvedTable = new Table(resolvedTable.getFullyQualifiedName());
        }
        return this;
    }

    /**
     * Sets a table's catalog and schema only when not set. Useful for setting CURRENT_SCHEMA() and
     * CURRENT_DATABASE()
     *
     * @param currentCatalogName the catalog name
     * @param currentSchemaName the schema name
     * @return the provided table
     */
    public Table setUnsetCatalogAndSchema(String currentCatalogName, String currentSchemaName) {
        String databaseName = getDatabaseName();
        if (databaseName == null || databaseName.isEmpty()) {
            setDatabaseName(currentCatalogName);
        }

        String schemaName = getSchemaName();
        if (schemaName == null || schemaName.isEmpty()) {
            setSchemaName(currentSchemaName);
        }
        return this;
    }

    /**
     * Sets a tables' catalog and schema only when not set. Useful for setting CURRENT_SCHEMA() and
     * CURRENT_DATABASE()
     *
     * @param currentCatalogName the current catalog name
     * @param currentSchemaName the current schema name
     * @param tables the tables
     * @return the tables
     */
    public static Table[] setUnsetCatalogAndSchema(String currentCatalogName,
            String currentSchemaName, Table... tables) {
        for (Table t : tables) {
            if (t != null) {
                t.setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);
            }
        }
        return tables;
    }

    @Override
    public Table clone() {
        try {
            Table clone = (Table) super.clone();
            clone.setName(this.getFullyQualifiedName());
            clone.setResolvedTable(this.resolvedTable);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
