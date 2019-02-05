/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2018 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class PartitionByClause {
    ExpressionList partitionExpressionList;

    public ExpressionList getPartitionExpressionList() {
        return partitionExpressionList;
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList) {
        this.partitionExpressionList = partitionExpressionList;
    }

    void toStringPartitionBy(StringBuilder b) {
        if (partitionExpressionList != null && !partitionExpressionList.getExpressions().isEmpty()) {
            b.append("PARTITION BY ");
            b.append(PlainSelect.
                    getStringList(partitionExpressionList.getExpressions(), true, false));
            b.append(" ");
        }
    }
}
