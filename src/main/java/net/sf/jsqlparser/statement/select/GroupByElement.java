/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public class GroupByElement {

    private List<Expression> groupByExpressions = new ArrayList<>();

    public void accept(GroupByVisitor groupByVisitor) {
        groupByVisitor.visit(this);
    }

    public List<Expression> getGroupByExpressions() {
        return groupByExpressions;
    }

    public void setGroupByExpressions(List<Expression> groupByExpressions) {
        this.groupByExpressions = groupByExpressions;
    }

    public void addGroupByExpression(Expression groupByExpression) {
        groupByExpressions.add(groupByExpression);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("GROUP BY ");

        b.append(PlainSelect.getStringList(groupByExpressions));

        return b.toString();
    }
}
