/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;

public class GroupByDeParser extends AbstractDeParser<GroupByElement> {

    private ExpressionVisitor expressionVisitor;

    GroupByDeParser() {
        super(new StringBuilder());
    }

    public GroupByDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.buffer = buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append("GROUP BY ");
        if (groupBy.isUsingBrackets()) {
            buffer.append("( ");
        }
        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();
        if (expressions != null) {
            for (Iterator<Expression> iter = expressions.iterator(); iter.hasNext();) {
                iter.next().accept(expressionVisitor);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }
        if (groupBy.isUsingBrackets()) {
            buffer.append(" )");
        }
        if (!groupBy.getGroupingSets().isEmpty()) {
            if (buffer.charAt(buffer.length() - 1) != ' ') {
                buffer.append(' ');
            }
            buffer.append("GROUPING SETS (");
            boolean first = true;
            for (Object o : groupBy.getGroupingSets()) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(", ");
                }
                if (o instanceof Expression) {
                    buffer.append(o.toString());
                } else if (o instanceof ExpressionList) {
                    ExpressionList list = (ExpressionList) o;
                    buffer.append(list.getExpressions() == null ? "()" : list.toString());
                }
            }
            buffer.append(")");
        }
    }

    void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

}
