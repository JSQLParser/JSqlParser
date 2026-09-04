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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;
    private List<String> tailParameters;
    private boolean indexTypeBeforeOn = false;
    private boolean usingIfNotExists = false;
    private boolean concurrently;
    private boolean only;
    private List<String> includeColumns;
    private Boolean nullsDistinct;
    private List<Index.Option> storageParameters;
    private String tableSpace;
    private Expression where;

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

    public boolean isConcurrently() {
        return concurrently;
    }

    public void setConcurrently(boolean concurrently) {
        this.concurrently = concurrently;
    }

    public boolean isOnly() {
        return only;
    }

    public void setOnly(boolean only) {
        this.only = only;
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public Boolean getNullsDistinct() {
        return nullsDistinct;
    }

    public void setNullsDistinct(Boolean nullsDistinct) {
        this.nullsDistinct = nullsDistinct;
    }

    public List<Index.Option> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<Index.Option> storageParameters) {
        this.storageParameters = storageParameters;
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public Expression getWhere() {
        return where;
    }

    public void setWhere(Expression where) {
        this.where = where;
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
        if (concurrently) {
            buffer.append("CONCURRENTLY ");
        }
        if (usingIfNotExists) {
            buffer.append("IF NOT EXISTS ");
        }
        if (index.getName() != null) {
            buffer.append(index.getName()).append(" ");
        }

        if (index.getUsing() != null && isIndexTypeBeforeOn()) {
            buffer.append("USING ").append(index.getUsing()).append(" ");
        }

        buffer.append("ON ");
        if (only) {
            buffer.append("ONLY ");
        }
        buffer.append(table.getFullyQualifiedName());

        if (index.getUsing() != null && !isIndexTypeBeforeOn()) {
            buffer.append(" USING ");
            buffer.append(index.getUsing());
        }

        if (index.getColumnsNames() != null) {
            buffer.append(" (");

            buffer.append(
                    index.getColumns().stream()
                            .map(Index.ColumnParams::toString)
                            .collect(joining(", ")));

            buffer.append(")");

            appendPostgreSqlTail(buffer);

            if (tailParameters != null) {
                for (String param : tailParameters) {
                    buffer.append(" ").append(param);
                }
            }
        }

        return buffer.toString();
    }

    private void appendPostgreSqlTail(StringBuilder buffer) {
        if (includeColumns != null) {
            buffer.append(" INCLUDE (").append(String.join(", ", includeColumns)).append(")");
        }
        if (nullsDistinct != null) {
            buffer.append(" NULLS ").append(nullsDistinct ? "DISTINCT" : "NOT DISTINCT");
        }
        if (storageParameters != null) {
            buffer.append(" WITH ")
                    .append(PlainSelect.getStringList(storageParameters, true, true));
        }
        if (tableSpace != null) {
            buffer.append(" TABLESPACE ").append(tableSpace);
        }
        if (where != null) {
            buffer.append(" WHERE ").append(where);
        }
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

    public CreateIndex withConcurrently(boolean concurrently) {
        setConcurrently(concurrently);
        return this;
    }

    public CreateIndex withOnly(boolean only) {
        setOnly(only);
        return this;
    }

    public CreateIndex withIncludeColumns(List<String> includeColumns) {
        setIncludeColumns(includeColumns);
        return this;
    }

    public CreateIndex withNullsDistinct(Boolean nullsDistinct) {
        setNullsDistinct(nullsDistinct);
        return this;
    }

    public CreateIndex withStorageParameters(List<Index.Option> storageParameters) {
        setStorageParameters(storageParameters);
        return this;
    }

    public CreateIndex withTableSpace(String tableSpace) {
        setTableSpace(tableSpace);
        return this;
    }

    public CreateIndex withWhere(Expression where) {
        setWhere(where);
        return this;
    }
}
