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
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

public class ExpressionListDeParser<T extends Expression>
        extends AbstractDeParser<ExpressionList<?>> {

    private final ExpressionVisitor expressionVisitor;
    private final boolean useComma;

    public ExpressionListDeParser(ExpressionVisitor expressionVisitor, StringBuilder builder,
            boolean useComma) {
        super(builder);
        this.expressionVisitor = expressionVisitor;
        this.useComma = useComma;
    }

    @Override
    public void deParse(ExpressionList<?> expressions) {
        if (expressions != null) {
            String comma = useComma ? ", " : " ";
            if (expressions instanceof ParenthesedExpressionList<?>) {
                buffer.append("(");
            }
            int i = 0;
            int size = expressions.size() - 1;
            for (Expression expression : expressions) {
                expression.accept(expressionVisitor);
                if (i < size) {
                    buffer.append(comma);
                }
                i++;
            }

            if (expressions instanceof ParenthesedExpressionList<?>) {
                buffer.append(")");
            }
        }
    }
}
