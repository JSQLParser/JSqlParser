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
import static java.util.stream.Collectors.joining;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;
    private List<String> tailParameters;

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

    public List<String> getTailParameters() {
        return tailParameters;
    }

    public void setTailParameters(List<String> tailParameters) {
        this.tailParameters = tailParameters;
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

        if (index.getUsing() != null) {
            buffer.append(" USING ");
            buffer.append(index.getUsing());
        }

        if (index.getColumnsNames() != null) {
            buffer.append(" (");

            buffer.append(
                    index.getColumnWithParams().stream()
                            .map(cp -> cp.columnName + (cp.getParams() != null ? " " + String.join(" ", cp.getParams()) : "")).collect(joining(", "))
            );

            buffer.append(")");

            if (tailParameters != null) {
                for (String param : tailParameters) {
                    buffer.append(" ").append(param);
                }
            }
        }

        return buffer.toString();
    }

    public CreateIndex withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public CreateIndex withIndex(Index index) {
        this.setIndex(index);
        return this;
    }

    public CreateIndex withTailParameters(List<String> tailParameters) {
        this.setTailParameters(tailParameters);
        return this;
    }
}
