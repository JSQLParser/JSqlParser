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
package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * A "CREATE TABLE" statement
 */
@Data
public class CreateTable implements Statement {

    /**
     * The name of the table to be created
     */
    private Table table;
    /**
     * Whether the table is unlogged or not (PostgreSQL 9.1+ feature)
     */
    private boolean unlogged = false;
    private List<String> createOptionsStrings;
    /**
     * A list of options (as simple strings) of this table definition, as ("TYPE", "=", "MYISAM")
     */
    private List<String> tableOptionsStrings;
    /**
     * A list of {@link ColumnDefinition}s of this table.
     */
    private List<ColumnDefinition> columnDefinitions;
    /**
     * A list of {@link Index}es (for example "PRIMARY KEY") of this table.<br>
     * Indexes created with column definitions (as in mycol INT PRIMARY KEY) are not inserted into
     * this list.
     */
    private List<Index> indexes;
    private Select select;
    private boolean parenthesis;
    private boolean ifNotExists = false;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        String sql;
        String createOps = PlainSelect.getStringList(createOptionsStrings, false, false);

        sql = "CREATE " + (unlogged ? "UNLOGGED " : "")
            + (!"".equals(createOps) ? createOps + " " : "")
            + "TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table;

        if (select != null) {
            sql += " AS " + (parenthesis ? "(" : "") + select.toString() + (parenthesis ? ")" : "");
        } else {
            sql += " (";

            sql += PlainSelect.getStringList(columnDefinitions, true, false);
            if (indexes != null && !indexes.isEmpty()) {
                sql += ", ";
                sql += PlainSelect.getStringList(indexes);
            }
            sql += ")";
            String options = PlainSelect.getStringList(tableOptionsStrings, false, false);
            if (options != null && options.length() > 0) {
                sql += " " + options;
            }
        }

        return sql;
    }
}
