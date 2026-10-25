/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** Index implementation options explicitly present in an index or supporting-key declaration. */
public class IndexOptions implements Serializable {
    public enum Clustering {
        CLUSTERED, NONCLUSTERED
    }

    private String using;
    private List<IndexSpecOption> specOptions;
    private String commentText;
    private Clustering clustering;
    private Boolean nullsDistinct;
    private List<String> includeColumns;
    private List<IndexOption> storageParameters;
    private String tableSpace;

    public String getUsing() {
        return using;
    }

    public void setUsing(String using) {
        this.using = using;
    }

    /**
     * Returns the mutable, ordered options written after the key list, or null when unset. An index
     * type written before the key list is {@link #getUsing()} instead.
     */
    public List<IndexSpecOption> getSpecOptions() {
        return specOptions;
    }

    /** Copies the list membership and retains the option nodes; null clears the options. */
    public void setSpecOptions(List<IndexSpecOption> specOptions) {
        this.specOptions = specOptions == null ? null : new ArrayList<>(specOptions);
    }

    public IndexOptions addSpecOptions(IndexSpecOption... options) {
        return addSpecOptions(Arrays.asList(options));
    }

    public IndexOptions addSpecOptions(Collection<? extends IndexSpecOption> options) {
        if (specOptions == null) {
            specOptions = new ArrayList<>();
        }
        specOptions.addAll(options);
        return this;
    }

    /**
     * Returns an unmodifiable token snapshot of {@link #getSpecOptions()}, such as
     * {@code [KEY_BLOCK_SIZE, =, 8, COMMENT, 'text']}, or null when unset. Edit the option nodes
     * instead of the snapshot.
     */
    public List<String> getIndexSpec() {
        if (specOptions == null) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        for (IndexSpecOption option : specOptions) {
            tokens.addAll(option.getTokens());
        }
        return Collections.unmodifiableList(tokens);
    }

    /**
     * Replaces all options after the key list with one opaque option holding a copy of the tokens,
     * without parsing them. Null clears the options; an empty list stores no options.
     */
    public void setIndexSpec(List<String> indexSpec) {
        if (indexSpec == null) {
            specOptions = null;
            return;
        }
        specOptions = new ArrayList<>();
        if (!indexSpec.isEmpty()) {
            specOptions.add(IndexSpecOption.raw(indexSpec));
        }
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Clustering getClustering() {
        return clustering;
    }

    public void setClustering(Clustering clustering) {
        this.clustering = clustering;
    }

    public Boolean getNullsDistinct() {
        return nullsDistinct;
    }

    public void setNullsDistinct(Boolean nullsDistinct) {
        this.nullsDistinct = nullsDistinct;
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns == null ? null : new ArrayList<>(includeColumns);
    }

    public List<IndexOption> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<IndexOption> storageParameters) {
        this.storageParameters =
                storageParameters == null ? null : new ArrayList<>(storageParameters);
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public String nullsDistinctClause() {
        return nullsDistinct == null ? ""
                : nullsDistinct ? " NULLS DISTINCT" : " NULLS NOT DISTINCT";
    }

    public String clusteringClause() {
        return clustering == null ? "" : " " + clustering;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        if (includeColumns != null) {
            sql.append(" INCLUDE ").append(PlainSelect.getStringList(includeColumns, true, true));
        }
        if (storageParameters != null) {
            sql.append(" WITH ");
            IndexOption.appendListTo(sql, storageParameters, expressionPrinter);
        }
        if (tableSpace != null) {
            sql.append(" USING INDEX TABLESPACE ").append(tableSpace);
        }
    }
}
