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

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.imprt.ImportColumn;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.Serializable;
import java.util.List;

/**
 * Exasol Like Clause
 *
 * @see <a href="https://docs.exasol.com/db/latest/sql/create_table.htm">Like Clause in CREATE
 *      TABLE</a>
 * @see <a href="https://docs.exasol.com/db/latest/sql/import.htm">Like Clause in IMPORT</a>
 */
public class LikeClause implements ImportColumn, Serializable {
    private Table table;
    private List<SelectItem<Column>> columnsList;

    private Boolean includingDefaults;
    private Boolean includingIdentity;
    private Boolean includingComments;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<SelectItem<Column>> getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(List<SelectItem<Column>> columnsList) {
        this.columnsList = columnsList;
    }

    public Boolean isIncludingDefaults() {
        return includingDefaults;
    }

    public void setIncludingDefaults(Boolean includingDefaults) {
        this.includingDefaults = includingDefaults;
    }

    public Boolean isExcludingDefaults() {
        return includingDefaults == null ? null : !includingDefaults;
    }

    public void setExcludingDefaults(Boolean excludingDefaults) {
        this.includingDefaults = !excludingDefaults;
    }

    public Boolean isIncludingIdentity() {
        return includingIdentity;
    }

    public void setIncludingIdentity(Boolean includingIdentity) {
        this.includingIdentity = includingIdentity;
    }

    public Boolean isExcludingIdentity() {
        return includingIdentity == null ? null : !includingIdentity;
    }

    public void setExcludingIdentity(Boolean excludingIdentity) {
        this.includingIdentity = !excludingIdentity;
    }

    public Boolean isIncludingComments() {
        return includingComments;
    }

    public void setIncludingComments(Boolean includingComments) {
        this.includingComments = includingComments;
    }

    public Boolean isExcludingComments() {
        return includingComments == null ? null : !includingComments;
    }

    public void setExcludingComments(Boolean excludingComments) {
        this.includingComments = !excludingComments;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" LIKE ");
        builder.append(table);
        if (columnsList != null) {
            builder.append(" ");
            PlainSelect.appendStringListTo(builder, columnsList, true, true);
        }

        if (includingDefaults != null) {
            if (includingDefaults) {
                builder.append(" INCLUDING ");
            } else {
                builder.append(" EXCLUDING ");
            }
            builder.append(" DEFAULTS ");
        }

        if (includingIdentity != null) {
            if (includingIdentity) {
                builder.append(" INCLUDING ");
            } else {
                builder.append(" EXCLUDING ");
            }
            builder.append(" IDENTITY ");
        }

        if (includingComments != null) {
            if (includingComments) {
                builder.append(" INCLUDING ");
            } else {
                builder.append(" EXCLUDING ");
            }
            builder.append(" COMMENTS ");
        }

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
