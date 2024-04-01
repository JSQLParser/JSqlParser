/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.util.SelectUtils;

import java.io.Serializable;

public class PartitionByClause implements Serializable {
    ExpressionList partitionExpressionList;
    boolean brackets = false;

    public ExpressionList getPartitionExpressionList() {
        return partitionExpressionList;
    }
    
    public void setPartitionExpressionList(ExpressionList partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList, boolean brackets) {
        this.partitionExpressionList = partitionExpressionList;
        this.brackets = brackets;
    }

    public void toStringPartitionBy(StringBuilder b) {
        if (partitionExpressionList != null && !partitionExpressionList.getExpressions().isEmpty()) {
            b.append("PARTITION BY ");
            b.append(SelectUtils.
                    getStringList(partitionExpressionList.getExpressions(), true, brackets));
            b.append(" ");
        }
    }
    
    public boolean isBrackets() {
        return brackets;
    }

    public PartitionByClause withPartitionExpressionList(ExpressionList partitionExpressionList) {
        this.setPartitionExpressionList(partitionExpressionList);
        return this;
    }
}
