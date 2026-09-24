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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LambdaExpression extends ASTNodeAccessImpl implements Expression {
    private List<String> identifiers;
    private Expression expression;
    private boolean parenthesized;

    public LambdaExpression(String identifier, Expression expression) {
        this.identifiers = Collections.singletonList(identifier);
        this.expression = expression;
    }

    public LambdaExpression(List<String> identifiers, Expression expression) {
        this.identifiers = identifiers;
        this.expression = expression;
    }

    public static LambdaExpression from(ExpressionList<? extends Expression> expressionList,
            Expression expression) {
        List<String> identifiers = new ArrayList<>(expressionList.size());
        for (Expression variable : expressionList) {
            identifiers.add(variable.toString());
        }
        return new LambdaExpression(identifiers, expression)
                .setParenthesized(expressionList instanceof ParenthesedExpressionList);
    }

    /** Whether the parameter list was explicitly parenthesized, including a single parameter. */
    public boolean isParenthesized() {
        return parenthesized;
    }

    public LambdaExpression setParenthesized(boolean parenthesized) {
        this.parenthesized = parenthesized;
        return this;
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
        return appendTo(builder, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (identifiers.size() == 1 && !parenthesized) {
            builder.append(identifiers.get(0));
        } else {
            int i = 0;
            builder.append("( ");
            for (String s : identifiers) {
                builder.append(i++ > 0 ? ", " : "").append(s);
            }
            builder.append(" )");
        }
        builder.append(" -> ");
        expressionPrinter.accept(expression);
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
