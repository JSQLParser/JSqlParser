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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

public class GroupByElement {

    private List<Expression> groupByExpressions = new ArrayList<>();
    private List groupingSets = new ArrayList();

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

    public List getGroupingSets() {
        return groupingSets;
    }

    public void setGroupingSets(List groupingSets) {
        this.groupingSets = groupingSets;
    }

    public void addGroupingSet(Expression expr) {
        this.groupingSets.add(expr);
    }

    public void addGroupingSet(ExpressionList list) {
        this.groupingSets.add(list);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("GROUP BY ");

        if (groupByExpressions.size() > 0) {
            b.append(PlainSelect.getStringList(groupByExpressions));
        } else if (groupingSets.size() > 0) {
            b.append("GROUPING SETS (");
            boolean first = true;
            for (Object o : groupingSets) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                if (o instanceof Expression) {
                    b.append(o.toString());
                } else if (o instanceof ExpressionList) {
                    ExpressionList list = (ExpressionList) o;
                    b.append(list.getExpressions() == null ? "()" : list.toString());
                }
            }
            b.append(")");
        }

        return b.toString();
    }
}
