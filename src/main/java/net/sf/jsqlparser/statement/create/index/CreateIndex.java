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

public class CreateIndex extends DDLStatement {

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

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE ");

        if (index.getType() != null) {
            builder.append(index.getType());
            builder.append(" ");
        }

        builder.append("INDEX ");
        builder.append(index.getName());
        builder.append(" ON ");
        builder.append(table.getFullyQualifiedName());

        if (index.getUsing() != null) {
            builder.append(" USING ");
            builder.append(index.getUsing());
        }

        if (index.getColumnsNames() != null) {
            builder.append(" (");

            builder.append(
                    index.getColumns().stream()
                            .map(cp -> cp.columnName + (cp.getParams() != null ? " " + String.join(" ", cp.getParams()) : "")).collect(joining(", "))
            );

            builder.append(")");

            if (tailParameters != null) {
                for (String param : tailParameters) {
                    builder.append(" ").append(param);
                }
            }
        }
        return builder;
    }
}
