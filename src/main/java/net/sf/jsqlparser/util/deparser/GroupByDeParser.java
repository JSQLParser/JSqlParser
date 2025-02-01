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

    private final ExpressionListDeParser<?> expressionListDeParser;

    public GroupByDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionListDeParser = new ExpressionListDeParser<>(expressionVisitor, buffer);
        this.builder = buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        builder.append("GROUP BY ");
        expressionListDeParser.deParse(groupBy.getGroupByExpressionList());

        int i = 0;
        if (!groupBy.getGroupingSets().isEmpty()) {
            if (builder.charAt(builder.length() - 1) != ' ') {
                builder.append(' ');
            }
            builder.append("GROUPING SETS (");
            for (ExpressionList<?> expressionList : groupBy.getGroupingSets()) {
                builder.append(i++ > 0 ? ", " : "");
                expressionListDeParser.deParse(expressionList);
            }
            builder.append(")");
        }

        if (groupBy.isMysqlWithRollup()) {
            builder.append(" WITH ROLLUP");
        }
    }
}
