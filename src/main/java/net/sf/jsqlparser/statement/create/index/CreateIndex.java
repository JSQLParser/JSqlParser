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

import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import java.util.*;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;
    private List<String> tailParameters;
    private boolean indexTypeBeforeOn = false;
    private boolean usingIfNotExists = false;
    private boolean concurrently;
    private boolean only;
    private boolean nullFiltered;
    private List<String> includeColumns;
    private Boolean nullsDistinct;
    private List<IndexOption> storageParameters;
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

    /** Whether this Spanner index omits rows with null key values. */
    public boolean isNullFiltered() {
        return nullFiltered;
    }

    public void setNullFiltered(boolean nullFiltered) {
        this.nullFiltered = nullFiltered;
    }

    public CreateIndex withNullFiltered(boolean nullFiltered) {
        setNullFiltered(nullFiltered);
        return this;
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

    public List<IndexOption> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<IndexOption> storageParameters) {
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
        return appendTo(new StringBuilder()).toString();
    }

    /** Shared rendering for the statement model and CreateIndexDeParser. */
    public StringBuilder appendTo(StringBuilder buffer) {
        return appendTo(buffer, expression -> buffer.append(expression));
    }

    /** Shares rendering while allowing visitors to transform key and option expressions. */
    public StringBuilder appendTo(StringBuilder buffer, Consumer<Expression> expressionPrinter) {
        appendIndexHeader(buffer);
        appendIndexTarget(buffer);
        appendIndexColumns(buffer, expressionPrinter);
        appendPostgreSqlTail(buffer, expressionPrinter);
        if (tailParameters != null) {
            for (String param : tailParameters) {
                buffer.append(" ").append(param);
            }
        }
        return buffer;
    }

    private void appendIndexHeader(StringBuilder buffer) {
        buffer.append("CREATE ");
        if (index.getType() != null) {
            buffer.append(index.getType()).append(" ");
        }
        if (index.getClustering() != null) {
            buffer.append(index.getClustering()).append(" ");
        }
        if (nullFiltered) {
            buffer.append("NULL_FILTERED ");
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
    }

    private void appendIndexTarget(StringBuilder buffer) {
        if (index.getUsing() != null && isIndexTypeBeforeOn()) {
            buffer.append("USING ").append(index.getUsing()).append(" ");
        }
        buffer.append("ON ");
        if (only) {
            buffer.append("ONLY ");
        }
        buffer.append(table.getFullyQualifiedName());
        if (index.getUsing() != null && !isIndexTypeBeforeOn()) {
            buffer.append(" USING ").append(index.getUsing());
        }
    }

    private void appendIndexColumns(StringBuilder buffer, Consumer<Expression> expressionPrinter) {
        if (index.getColumns() != null) {
            buffer.append(" (");
            for (Iterator<KeyElement> columns = index.getColumns().iterator(); columns
                    .hasNext();) {
                columns.next().appendTo(buffer, expressionPrinter);
                if (columns.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }
    }

    private void appendPostgreSqlTail(StringBuilder buffer,
            Consumer<Expression> expressionPrinter) {
        if (includeColumns != null) {
            buffer.append(" INCLUDE (").append(String.join(", ", includeColumns)).append(")");
        }
        if (nullsDistinct != null) {
            buffer.append(" NULLS ").append(nullsDistinct ? "DISTINCT" : "NOT DISTINCT");
        }
        if (storageParameters != null) {
            buffer.append(" WITH ");
            IndexOption.appendListTo(buffer, storageParameters, expressionPrinter);
        }
        if (tableSpace != null) {
            buffer.append(" TABLESPACE ").append(tableSpace);
        }
        if (where != null) {
            buffer.append(" WHERE ");
            expressionPrinter.accept(where);
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

    public CreateIndex withStorageParameters(List<IndexOption> storageParameters) {
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
