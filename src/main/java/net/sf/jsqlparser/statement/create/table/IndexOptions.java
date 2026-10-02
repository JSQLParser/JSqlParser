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
    private List<String> indexSpec;
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

    public List<String> getIndexSpec() {
        return indexSpec;
    }

    public void setIndexSpec(List<String> indexSpec) {
        this.indexSpec = indexSpec;
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
