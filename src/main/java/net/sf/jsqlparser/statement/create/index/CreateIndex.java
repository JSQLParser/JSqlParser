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

import static java.util.stream.Collectors.joining;

import java.util.*;

import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;
    private List<String> tailParameters;
    private boolean indexTypeBeforeOn = false;
    private boolean usingIfNotExists = false;

    public boolean isIndexTypeBeforeOn() {
        return indexTypeBeforeOn;
    }

    public void setIndexTypeBeforeOn(boolean indexTypeBeforeOn) {
        this.indexTypeBeforeOn = indexTypeBeforeOn;
    }

    public boolean isUsingIfNotExists() {
        return usingIfNotExists;
    }

    public CreateIndex setUsingIfNotExists(boolean usingIfNotExists) {
        this.usingIfNotExists = usingIfNotExists;
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
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
        if (usingIfNotExists) {
            buffer.append("IF NOT EXISTS ");
        }
        buffer.append(index.getName());

        if (index.getUsing() != null && isIndexTypeBeforeOn()) {
            buffer.append(" USING ");
            buffer.append(index.getUsing());
        }

        buffer.append(" ON ");
        buffer.append(table.getFullyQualifiedName());

        if (index.getUsing() != null && !isIndexTypeBeforeOn()) {
            buffer.append(" USING ");
            buffer.append(index.getUsing());
        }

        if (index.getColumnsNames() != null) {
            buffer.append(" (");

            buffer.append(
                    index.getColumns().stream()
                            .map(cp -> cp.columnName + (cp.getParams() != null
                                    ? " " + String.join(" ", cp.getParams())
                                    : ""))
                            .collect(joining(", ")));

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
