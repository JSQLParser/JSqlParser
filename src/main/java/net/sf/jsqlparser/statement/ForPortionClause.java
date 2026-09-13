/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** The application-time interval affected by an UPDATE or DELETE statement. */
public class ForPortionClause extends ASTNodeAccessImpl {
    private String periodName;
    private Expression fromExpression;
    private Expression toExpression;

    public ForPortionClause(String periodName, Expression fromExpression, Expression toExpression) {
        this.periodName = periodName;
        this.fromExpression = fromExpression;
        this.toExpression = toExpression;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public Expression getFromExpression() {
        return fromExpression;
    }

    public void setFromExpression(Expression fromExpression) {
        this.fromExpression = fromExpression;
    }

    public Expression getToExpression() {
        return toExpression;
    }

    public void setToExpression(Expression toExpression) {
        this.toExpression = toExpression;
    }

    /** Visits both interval bounds, preserving the caller's context. */
    public <S> void accept(ExpressionVisitor<?> visitor, S context) {
        fromExpression.accept(visitor, context);
        toExpression.accept(visitor, context);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append("FOR PORTION OF ").append(periodName).append(" FROM ");
        expressionPrinter.accept(fromExpression);
        builder.append(" TO ");
        expressionPrinter.accept(toExpression);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
