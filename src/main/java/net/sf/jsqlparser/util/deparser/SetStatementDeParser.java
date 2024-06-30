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
import net.sf.jsqlparser.statement.SetStatement;

import java.util.List;

public class SetStatementDeParser extends AbstractDeParser<SetStatement> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;

    public SetStatementDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(SetStatement set) {
        buffer.append("SET ");
        if (set.getEffectParameter() != null) {
            buffer.append(set.getEffectParameter()).append(" ");
        }
        for (int i = 0; i < set.getCount(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(set.getName(i));
            if (set.isUseEqual(i)) {
                buffer.append(" =");
            }
            buffer.append(" ");
            List<Expression> expressions = set.getExpressions(i);
            for (int j = 0; j < expressions.size(); j++) {
                if (j > 0) {
                    buffer.append(", ");
                }
                expressions.get(j).accept(expressionVisitor, null);
            }
        }

    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }
}
