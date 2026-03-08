/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Column;

public class WithSearchClause implements Serializable {
    public enum SearchOrder {
        BREADTH, DEPTH
    }

    private SearchOrder searchOrder;
    private List<Column> searchColumns;
    private String sequenceColumnName;

    public WithSearchClause() {}

    public WithSearchClause(SearchOrder searchOrder, List<Column> searchColumns,
            String sequenceColumnName) {
        this.searchOrder = searchOrder;
        this.searchColumns = searchColumns;
        this.sequenceColumnName = sequenceColumnName;
    }

    public SearchOrder getSearchOrder() {
        return searchOrder;
    }

    public void setSearchOrder(SearchOrder searchOrder) {
        this.searchOrder = searchOrder;
    }

    public WithSearchClause withSearchOrder(SearchOrder searchOrder) {
        this.setSearchOrder(searchOrder);
        return this;
    }

    public List<Column> getSearchColumns() {
        return searchColumns;
    }

    public void setSearchColumns(List<Column> searchColumns) {
        this.searchColumns = searchColumns;
    }

    public WithSearchClause withSearchColumns(List<Column> searchColumns) {
        this.setSearchColumns(searchColumns);
        return this;
    }

    public WithSearchClause addSearchColumns(Column... searchColumns) {
        List<Column> collection = Optional.ofNullable(getSearchColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, searchColumns);
        return this.withSearchColumns(collection);
    }

    public WithSearchClause addSearchColumns(Collection<? extends Column> searchColumns) {
        List<Column> collection = Optional.ofNullable(getSearchColumns()).orElseGet(ArrayList::new);
        collection.addAll(searchColumns);
        return this.withSearchColumns(collection);
    }

    public String getSequenceColumnName() {
        return sequenceColumnName;
    }

    public void setSequenceColumnName(String sequenceColumnName) {
        this.sequenceColumnName = sequenceColumnName;
    }

    public WithSearchClause withSequenceColumnName(String sequenceColumnName) {
        this.setSequenceColumnName(sequenceColumnName);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("SEARCH ")
                .append(searchOrder)
                .append(" FIRST BY ")
                .append(Select.getStringList(searchColumns))
                .append(" SET ")
                .append(sequenceColumnName)
                .toString();
    }
}
