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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Pivot;

/**
 * A table. It can have an alias and the schema name it belongs to.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Table extends ASTNodeAccessImpl implements FromItem, MultiPartName {

    private Database database;
    private String schemaName;
    private String name;

    private Alias alias;
    private Pivot pivot;
    private MySQLIndexHint mySQLIndexHint;

    public Table(String name) {
        this.name = name;
    }

    public Table(String schemaName, String name) {
        this.schemaName = schemaName;
        this.name = name;
    }

    public Table(Database database, String schemaName, String name) {
        this.database = database;
        this.schemaName = schemaName;
        this.name = name;
    }

    @Override
    public String getFullyQualifiedName() {
        String fqn = "";

        if (database != null) {
            fqn += database.getFullyQualifiedName();
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (schemaName != null) {
            fqn += schemaName;
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (name != null) {
            fqn += name;
        }

        return fqn;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    public void accept(IntoTableVisitor intoTableVisitor) {
        intoTableVisitor.visit(this);
    }

    @Override
    public String toString() {
        return getFullyQualifiedName()
            + ((alias != null) ? alias.toString() : "")
            + ((pivot != null) ? " " + pivot : "")
            + ((mySQLIndexHint != null) ? mySQLIndexHint.toString() : "");
    }
}
