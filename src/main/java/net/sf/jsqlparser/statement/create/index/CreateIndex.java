/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.index;

import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;

import java.util.*;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

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

        if (index.getUsing() != null){
            buffer.append(" USING ");
            buffer.append(index.getUsing());
        }

        if (index.getColumnsNames() != null) {
            buffer.append(" (");

            for (Iterator iter = index.getColumnsNames().iterator(); iter.hasNext();) {
                String columnName = (String) iter.next();

                buffer.append(columnName);

                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }

            buffer.append(")");
        }

        return buffer.toString();
    }

}
