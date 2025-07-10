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
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;

public class PartitionByClause extends ExpressionList<Expression> implements Serializable {
    boolean brackets = false;

    @Deprecated
    public ExpressionList<Expression> getPartitionExpressionList() {
        return this;
    }

    @Deprecated
    public void setPartitionExpressionList(ExpressionList<Expression> partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    @Deprecated
    public void setPartitionExpressionList(ExpressionList<Expression> partitionExpressionList,
            boolean brackets) {
        setExpressions(partitionExpressionList, brackets);
    }

    public PartitionByClause setExpressions(ExpressionList<Expression> partitionExpressionList,
            boolean brackets) {
        clear();
        if (partitionExpressionList != null) {
            addAll(partitionExpressionList);
        }
        this.brackets = brackets;
        return this;
    }

    public void toStringPartitionBy(StringBuilder b) {
        if (!isEmpty()) {
            b.append("PARTITION BY ");
            b.append(PlainSelect.getStringList(this, true,
                    brackets));
            b.append(" ");
        }
    }

    public boolean isBrackets() {
        return brackets;
    }

    @Deprecated
    public PartitionByClause withPartitionExpressionList(
            ExpressionList<Expression> partitionExpressionList) {
        this.setPartitionExpressionList(partitionExpressionList);
        return this;
    }
}
