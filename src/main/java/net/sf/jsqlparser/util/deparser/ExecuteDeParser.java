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

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.execute.Execute;

public class ExecuteDeParser extends AbstractDeParser<Execute> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;

    public ExecuteDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(Execute execute) {
        builder.append(execute.getExecType().name()).append(" ").append(execute.getName());
        if (execute.isParenthesis()) {
            builder.append(" (");
        } else if (execute.getExprList() != null) {
            builder.append(" ");
        }
        if (execute.getExprList() != null) {
            List<Expression> expressions = execute.getExprList().getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                expressions.get(i).accept(expressionVisitor, null);
            }
        }
        if (execute.isParenthesis()) {
            builder.append(")");
        }
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }
}
