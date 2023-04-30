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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.Limit;

import java.util.List;

public class LimitDeparser extends AbstractDeParser<Limit> {
    private ExpressionVisitor expressionVisitor;
    public LimitDeparser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(Limit limit) {
        buffer.append(" LIMIT ");
        if (limit.isLimitNull()) {
            buffer.append("NULL");
        } else {
            if (limit.isLimitAll()) {
                buffer.append("ALL");
            } else {
                if (null != limit.getOffset()) {
                    limit.getOffset().accept(expressionVisitor);
                    buffer.append(", ");
                }

                if (null != limit.getRowCount()) {
                   limit.getRowCount().accept(expressionVisitor);
                }
            }
        }

        final List<Expression> byExpressions = limit.getByExpressions();
        if (byExpressions!=null && !byExpressions.isEmpty()) {
            buffer.append(" BY");
            int i=0;
            for (Expression expression: byExpressions) {
                buffer.append( i++ > 0 ? ", " : " ");
                expression.accept(expressionVisitor);
            }
        }
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }
}
