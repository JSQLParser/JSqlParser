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

import java.util.*;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.create.table.*;

public class CreateIndex implements Statement {

    private Table table;
    private Index index;
    private Index detachedOptions;
    private List<String> tailParameters;
    private boolean indexTypeBeforeOn = false;
    private boolean usingIfNotExists = false;
    private boolean concurrently;
    private boolean only;
    private boolean nullFiltered;
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
        Index options = getOptions();
        return options == null ? null : options.getIncludeColumns();
    }

    /**
     * Copies the supplied list as {@link Index#setIncludeColumns(List)} does; null clears it.
     * {@link #getIncludeColumns()} returns the live, mutable list held by the index options.
     */
    public void setIncludeColumns(List<String> includeColumns) {
        getOrCreateOptions().setIncludeColumns(includeColumns);
    }

    public Boolean getNullsDistinct() {
        Index options = getOptions();
        return options == null ? null : options.getNullsDistinct();
    }

    public void setNullsDistinct(Boolean nullsDistinct) {
        getOrCreateOptions().setNullsDistinct(nullsDistinct);
    }

    public List<Index.Option> getStorageParameters() {
        Index options = getOptions();
        return options == null ? null : options.getStorageParameters();
    }

    /**
     * Copies the list container as {@link Index#setStorageParameters(List)} does, retaining the
     * option objects; null clears it. {@link #getStorageParameters()} returns the live, mutable
     * list held by the index options.
     */
    public void setStorageParameters(List<Index.Option> storageParameters) {
        getOrCreateOptions().setStorageParameters(storageParameters);
    }

    public String getTableSpace() {
        Index options = getOptions();
        return options == null ? null : options.getTableSpace();
    }

    public void setTableSpace(String tableSpace) {
        getOrCreateOptions().setTableSpace(tableSpace);
    }

    private Index getOptions() {
        return index == null ? detachedOptions : index;
    }

    private Index getOrCreateOptions() {
        if (index != null) {
            return index;
        }
        if (detachedOptions == null) {
            detachedOptions = new Index();
        }
        return detachedOptions;
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

    /**
     * Replaces the index definition. Options supplied by the new index take precedence; omitted
     * options inherit the current statement options, including those set before an index was
     * attached. Passing null detaches the definition without discarding its options. The detached
     * option lists have independent containers, with their elements retained. Use the option
     * setters with null to clear individual options.
     */
    public void setIndex(Index index) {
        Index previousOptions = getOptions();
        if (index == null) {
            if (this.index != null) {
                detachedOptions = new Index();
                detachedOptions.setIncludeColumns(previousOptions.getIncludeColumns());
                detachedOptions.setNullsDistinct(previousOptions.getNullsDistinct());
                detachedOptions.setStorageParameters(previousOptions.getStorageParameters());
                detachedOptions.setTableSpace(previousOptions.getTableSpace());
            }
        } else {
            if (previousOptions != null && previousOptions != index) {
                if (index.getIncludeColumns() == null) {
                    index.setIncludeColumns(previousOptions.getIncludeColumns());
                }
                if (index.getNullsDistinct() == null) {
                    index.setNullsDistinct(previousOptions.getNullsDistinct());
                }
                if (index.getStorageParameters() == null) {
                    index.setStorageParameters(previousOptions.getStorageParameters());
                }
                if (index.getTableSpace() == null) {
                    index.setTableSpace(previousOptions.getTableSpace());
                }
            }
            detachedOptions = null;
        }
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
            for (Iterator<Index.ColumnParams> columns = index.getColumns().iterator(); columns
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
        if (getIncludeColumns() != null) {
            buffer.append(" INCLUDE (").append(String.join(", ", getIncludeColumns())).append(")");
        }
        if (getNullsDistinct() != null) {
            buffer.append(" NULLS ").append(getNullsDistinct() ? "DISTINCT" : "NOT DISTINCT");
        }
        if (getStorageParameters() != null) {
            buffer.append(" WITH ");
            Index.Option.appendListTo(buffer, getStorageParameters(), expressionPrinter);
        }
        if (getTableSpace() != null) {
            buffer.append(" TABLESPACE ").append(getTableSpace());
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
