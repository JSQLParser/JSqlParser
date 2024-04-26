/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Arrays;
import java.util.List;

public class LambdaExpression extends ASTNodeAccessImpl implements Expression {
    private List<String> identifiers;
    private Expression expression;

    public LambdaExpression(String identifier, Expression expression) {
        this.identifiers = Arrays.asList(identifier);
        this.expression = expression;
    }

    public LambdaExpression(List<String> identifiers, Expression expression) {
        this.identifiers = identifiers;
        this.expression = expression;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public LambdaExpression setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Expression getExpression() {
        return expression;
    }

    public LambdaExpression setExpression(Expression expression) {
        this.expression = expression;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        if (identifiers.size() == 1) {
            builder.append(identifiers.get(0));
        } else {
            int i = 0;
            builder.append("( ");
            for (String s : identifiers) {
                builder.append(i++ > 0 ? ", " : "").append(s);
            }
            builder.append(" )");
        }
        return builder.append(" -> ").append(expression);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
