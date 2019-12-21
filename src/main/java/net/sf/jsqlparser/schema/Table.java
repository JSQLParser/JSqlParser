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
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.*;

/**
 * A table. It can have an alias and the schema name it belongs to.
 */
public class Table extends ASTNodeAccessImpl implements FromItem, MultiPartName {

//    private Database database;
//    private String schemaName;
//    private String name;
    private static final int NAME_IDX = 0;
    private static final int SCHEMA_IDX = 1;
    private static final int DATABASE_IDX = 2;
    private static final int SERVER_IDX = 3;

    private List<String> partItems = new ArrayList<>();

    private Alias alias;
    private Pivot pivot;
    private UnPivot unpivot;
    private MySQLIndexHint hint;
    private List<SqlServerTableHint> sqlServerTableHints;

    public Table() {
    }

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

    public Table(List<String> partItems) {
        this.partItems = new ArrayList<>(partItems);
        Collections.reverse(this.partItems);
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
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

    public void setSchemaName(String string) {
        setIndex(SCHEMA_IDX, string);
    }

    public String getName() {
        return getIndex(NAME_IDX);
    }

    public void setName(String string) {
        setIndex(NAME_IDX, string);
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
        partItems.set(idx, value);
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
                fqn.append(".");
            }
        }

        return fqn.toString();
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    public void accept(IntoTableVisitor intoTableVisitor) {
        intoTableVisitor.visit(this);
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
        return hint;
    }

    public void setHint(MySQLIndexHint hint) {
        this.hint = hint;
    }

    public List<SqlServerTableHint> getSqlServerHints() {
        return sqlServerTableHints;
    }

    public void setSqlServerHints(List<SqlServerTableHint> sqlServerTableHints) {
        this.sqlServerTableHints = sqlServerTableHints;
    }

    @Override
    public String toString() {
        return getFullyQualifiedName()
                + ((alias != null) ? alias.toString() : "")
                + ((pivot != null) ? " " + pivot : "")
                + ((unpivot != null) ? " " + unpivot : "")
                + ((hint != null) ? hint.toString() : "")
                + ((sqlServerTableHints != null) ? SqlServerTableHint.toString(sqlServerTableHints) : "");
    }
}
