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
import net.sf.jsqlparser.statement.SetStatement;

public class SetStatementDeParser extends AbstractDeParser<SetStatement> {

    private ExpressionVisitor expressionVisitor;

    public SetStatementDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(SetStatement set) {
        buffer.append("SET ");

        for (int i = 0; i < set.getCount(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(set.getName(i));
            if (set.isUseEqual(i)) {
                buffer.append(" =");
            }
            buffer.append(" ");
            set.getExpression(i).accept(expressionVisitor);
        }

    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }
}
