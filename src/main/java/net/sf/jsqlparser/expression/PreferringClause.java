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

import java.io.Serializable;

public class PreferringClause implements Serializable {
    private Expression preferring;
    private PartitionByClause partitionBy;

    public PreferringClause(Expression preferring) {
        this.preferring = preferring;
    }

    public void setPartitionExpressionList(ExpressionList expressionList,
        boolean brackets) {
        if (this.partitionBy == null) {
            this.partitionBy = new PartitionByClause();
        }
        partitionBy.setPartitionExpressionList(expressionList, brackets);
    }

    public void toStringPreferring(StringBuilder b) {
        b.append("PREFERRING ");
        b.append(preferring.toString());

        if (partitionBy != null) {
            b.append(" ");
            partitionBy.toStringPartitionBy(b);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringPreferring(sb);
        return sb.toString();
    }
}
