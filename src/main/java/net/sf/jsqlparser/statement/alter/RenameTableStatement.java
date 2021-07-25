/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement.alter;

import java.util.Objects;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 *
 * @author are
 * @see  <a href="https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_9019.htm">Rename</a>
 */
public class RenameTableStatement implements Statement {
    private final Table oldName;
    private final Table newName;

    public RenameTableStatement(Table oldName, Table newName) {
        this.oldName = Objects.requireNonNull(oldName, "The OLD NAME of the Rename Statement must not be null.");
        this.newName = Objects.requireNonNull(newName, "The NEW NAME of the Rename Statement must not be null.");
    }

    public Table getNewName() {
        return newName;
    }

    public Table getOldName() {
        return oldName;
    }
    
    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
      }
    
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("RENAME ")
          .append(oldName)
          .append(" TO ")
          .append(newName);
        
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
