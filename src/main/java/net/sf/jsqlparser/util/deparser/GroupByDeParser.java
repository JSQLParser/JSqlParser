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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;

public class GroupByDeParser extends AbstractDeParser<GroupByElement> {

    private ExpressionListDeParser expressionListDeParser;

    GroupByDeParser() {
        this(null, new StringBuilder());
    }

    public GroupByDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionListDeParser = new ExpressionListDeParser(expressionVisitor, buffer);
        this.buffer = buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append("GROUP BY ");
        expressionListDeParser.deParse(groupBy.getGroupByExpressionList());

        int i = 0;
        if (!groupBy.getGroupingSets().isEmpty()) {
            if (buffer.charAt(buffer.length() - 1) != ' ') {
                buffer.append(' ');
            }
            buffer.append("GROUPING SETS (");
            for (ExpressionList expressionList : groupBy.getGroupingSets()) {
                buffer.append(i++ > 0 ? ", " : "");
                expressionListDeParser.deParse(expressionList);
            }
            buffer.append(")");
        }
    }
}
