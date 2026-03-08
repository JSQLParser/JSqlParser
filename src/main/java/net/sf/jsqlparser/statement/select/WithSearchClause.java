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
import java.util.Collection;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

public class WithSearchClause implements Serializable {
    public enum SearchOrder {
        BREADTH, DEPTH
    }

    private SearchOrder searchOrder;
    private ExpressionList<Column> searchColumns;
    private String sequenceColumnName;

    public WithSearchClause() {}

    public WithSearchClause(SearchOrder searchOrder, ExpressionList<Column> searchColumns,
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

    public ExpressionList<Column> getSearchColumns() {
        return searchColumns;
    }

    public void setSearchColumns(ExpressionList<Column> searchColumns) {
        this.searchColumns = searchColumns;
    }

    public WithSearchClause withSearchColumns(ExpressionList<Column> searchColumns) {
        this.setSearchColumns(searchColumns);
        return this;
    }

    public WithSearchClause addSearchColumns(Column... searchColumns) {
        ExpressionList<Column> collection =
                getSearchColumns() != null ? getSearchColumns() : new ExpressionList<>();
        collection.addExpressions(searchColumns);
        return this.withSearchColumns(collection);
    }

    public WithSearchClause addSearchColumns(Collection<? extends Column> searchColumns) {
        ExpressionList<Column> collection =
                getSearchColumns() != null ? getSearchColumns() : new ExpressionList<>();
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
