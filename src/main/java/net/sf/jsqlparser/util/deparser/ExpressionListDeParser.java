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

import java.util.Collection;

public class ExpressionListDeParser extends AbstractDeParser<Collection<Expression>> {

    private final ExpressionVisitor expressionVisitor;
    private final boolean useBrackets;
    private final boolean useComma;

    public ExpressionListDeParser(ExpressionVisitor expressionVisitor, StringBuilder builder, boolean useBrackets, boolean useComma) {
        super(builder);
        this.expressionVisitor = expressionVisitor;
        this.useBrackets = useBrackets;
        this.useComma = useComma;
    }

    @Override
    public void deParse(Collection<Expression> expressions) {
        if (expressions != null) {
            String comma = useComma ? ", " : " ";
            if (useBrackets) {
                buffer.append("(");
            }
            int i=0;
            int size = expressions.size() - 1;
            for (Expression expression: expressions) {
                expression.accept(expressionVisitor);
                if (i<size) {
                    buffer.append(comma);
                }
                i++;
            }

            if (useBrackets) {
                buffer.append(")");
            }
        }
    }
}
