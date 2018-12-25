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
package net.sf.jsqlparser.statement.create.index;

import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;

import java.util.*;

/**
 * A "CREATE INDEX" statement
 *
 * @author Raymond Augé
 */
public class CreateIndex implements Statement {

    private Table table;
    private Index index;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    /**
     * The index to be created
     */
    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    /**
     * The table on which the index is to be created
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("CREATE ");

        if (index.getType() != null) {
            buffer.append(index.getType());
            buffer.append(" ");
        }

        buffer.append("INDEX ");
        buffer.append(index.getName());
        buffer.append(" ON ");
        buffer.append(table.getFullyQualifiedName());

        if (index.getColumnsNames() != null) {
            buffer.append(" (");

            int i = 0;
            for (Iterator<String> iter = index.getColumnsNames().iterator(); iter.hasNext();) {
                String columnName = iter.next();

                buffer.append(columnName);
                if(index.getIndexSpec().size() > i && index.getIndexSpec().get(i) != null) {
                    buffer.append(" ").append(index.getIndexSpec().get(i));
                }

                if (iter.hasNext()) {
                    buffer.append(", ");
                }
                i++;
            }

            buffer.append(")");
        }

        return buffer.toString();
    }

}
