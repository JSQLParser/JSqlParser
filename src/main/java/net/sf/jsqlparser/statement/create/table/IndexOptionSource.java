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

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** Access to the single options object of an index or an index-backed constraint. */
public interface IndexOptionSource {
    IndexOptions getIndexOptions();

    default String getUsing() {
        return getIndexOptions().getUsing();
    }

    default void setUsing(String value) {
        getIndexOptions().setUsing(value);
    }

    default List<String> getIndexSpec() {
        return getIndexOptions().getIndexSpec();
    }

    default void setIndexSpec(List<String> value) {
        getIndexOptions().setIndexSpec(value);
    }

    default String getCommentText() {
        return getIndexOptions().getCommentText();
    }

    default void setCommentText(String value) {
        getIndexOptions().setCommentText(value);
    }

    default IndexOptions.Clustering getClustering() {
        return getIndexOptions().getClustering();
    }

    default void setClustering(IndexOptions.Clustering value) {
        getIndexOptions().setClustering(value);
    }

    default Boolean getNullsDistinct() {
        return getIndexOptions().getNullsDistinct();
    }

    default void setNullsDistinct(Boolean value) {
        getIndexOptions().setNullsDistinct(value);
    }

    default List<String> getIncludeColumns() {
        return getIndexOptions().getIncludeColumns();
    }

    default void setIncludeColumns(List<String> value) {
        getIndexOptions().setIncludeColumns(value);
    }

    default List<IndexOption> getStorageParameters() {
        return getIndexOptions().getStorageParameters();
    }

    default void setStorageParameters(List<IndexOption> value) {
        getIndexOptions().setStorageParameters(value);
    }

    default String getTableSpace() {
        return getIndexOptions().getTableSpace();
    }

    default void setTableSpace(String value) {
        getIndexOptions().setTableSpace(value);
    }

    default String nullsDistinctClause() {
        return getIndexOptions().nullsDistinctClause();
    }

    default String clusteringClause() {
        return getIndexOptions().clusteringClause();
    }

    default void appendIndexOptionsTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        getIndexOptions().appendTo(sql, expressionPrinter);
    }
}
