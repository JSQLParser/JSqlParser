/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.SQLServerTableHint;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Pivot;

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

    private List<String> partItems = new ArrayList<String>();

    private Alias alias;
    private Pivot pivot;
    private MySQLIndexHint hint;
    private List<SQLServerTableHint> sqlServerTableHints;

    public Table() {}

    public Table(String name) {
        setIndex(NAME_IDX, name);
    }

    public Table(String schemaName, String name) {
        setIndex(NAME_IDX, name);
        setIndex(SCHEMA_IDX, schemaName);
    }

    public Table(Database database, String schemaName, String name) {
        setIndex(NAME_IDX, name);
        setIndex(SCHEMA_IDX, schemaName);
        if (database != null) {
            setIndex(DATABASE_IDX, database.getDatabaseName());
            setIndex(SERVER_IDX, database.getServer().getFullyQualifiedName());
        }
    }

    public Table(List<String> partItems) {
        this.partItems = new ArrayList<String>(partItems);
        Collections.reverse(this.partItems);
    }

    public Table(Alias alias, Database database, String schemaName, String name) {
        this(database, schemaName, name);
        this.alias = alias;
    }

    public Table(Table table) {
        this(table.getDatabase(), table.getSchemaName(), table.getName());
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
    }

    public void setDatabase(Database database) {
        setIndex(DATABASE_IDX, database.getDatabaseName());
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
        for (int i = 0; i < idx - partItems.size() + 1; i++) {
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
            //TODO(PB): looks like part1..part3, for example, when part2 is null (missing) - is a correct syntax
            //            if (part == null) {
            //                continue;
            //            }
            if (fqn.length() > 0) {
                fqn.append(".");
            }
            fqn.append(part != null ? part : "");
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

    public MySQLIndexHint getIndexHint() {
        return hint;
    }

    public void setHint(MySQLIndexHint hint) {
        this.hint = hint;
    }

    public List<SQLServerTableHint> getSqlServerTableHints() {
        return sqlServerTableHints;
    }

    public void setSqlServerTableHints(List<SQLServerTableHint> sqlServerTableHints) {
        this.sqlServerTableHints = sqlServerTableHints;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getFullyQualifiedName());
        if (pivot != null) {
            stringBuilder.append(" ").append(pivot);
        }
        if (alias != null) {
            stringBuilder.append(alias);
        }
        if (hint != null) {
            stringBuilder.append(hint);
        }
        if (sqlServerTableHints != null && sqlServerTableHints.size() > 0) {
            stringBuilder.append(" WITH(");
            boolean first = true;
            for (SQLServerTableHint sqlServerTableHint : sqlServerTableHints) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append(sqlServerTableHint);
            }
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (String part : partItems) {
            result = prime * result + ((part == null) ? 0 : part.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Table other = (Table) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getSchemaName() == null) {
            if (other.getSchemaName() != null) {
                return false;
            }
        } else if (!getSchemaName().equals(other.getSchemaName())) {
            return false;
        }
        if (getDatabase() == null) {
            if (other.getDatabase() != null) {
                return false;
            }
        } else if (!getDatabase().equals(other.getDatabase())) {
            return false;
        }
        return true;
    }
}
