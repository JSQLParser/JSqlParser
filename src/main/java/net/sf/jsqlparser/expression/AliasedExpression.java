/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.function.Consumer;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** An expression with an alias, such as an XMLFOREST argument. */
public class AliasedExpression extends ASTNodeAccessImpl implements Expression {
    private Expression expression;
    private Alias alias;

    public AliasedExpression(Expression expression, Alias alias) {
        this.expression = expression;
        this.alias = alias;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        expressionPrinter.accept(expression);
        if (alias != null) {
            builder.append(alias);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
