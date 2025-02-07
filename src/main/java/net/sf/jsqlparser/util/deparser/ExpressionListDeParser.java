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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.Collections;
import java.util.List;

public class ExpressionListDeParser<T extends Expression>
        extends AbstractDeParser<ExpressionList<?>> {

    private final ExpressionVisitor<StringBuilder> expressionVisitor;

    public ExpressionListDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder builder) {
        super(builder);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(ExpressionList<?> expressionList) {
        // @todo: remove this NameExpressionList related part
        String comma = expressionList instanceof NamedExpressionList
                ? " "
                : ", ";
        // @todo: remove this NameExpressionList related part
        List<String> names = expressionList instanceof NamedExpressionList
                ? ((NamedExpressionList<?>) expressionList).getNames()
                : Collections.nCopies(expressionList.size(), "");

        if (expressionList instanceof ParenthesedExpressionList<?>) {
            builder.append("(");
        }
        int i = 0;
        for (Expression expression : expressionList) {
            if (i > 0) {
                builder.append(comma);
            }

            // @todo: remove this NameExpressionList related part
            String name = names.get(i);
            if (!name.isEmpty()) {
                builder.append(name);
                builder.append(" ");
            }
            expression.accept(expressionVisitor, null);
            i++;
        }

        if (expressionList instanceof ParenthesedExpressionList<?>) {
            builder.append(")");
        }
    }
}
