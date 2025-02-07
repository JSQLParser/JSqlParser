package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Expression;

public class LimitPipeOperator extends PipeOperator {
    private Expression limitExpression;
    private Expression offsetExpression;

    public LimitPipeOperator(Expression limitExpression, Expression offsetExpression) {
        this.limitExpression = limitExpression;
        this.offsetExpression = offsetExpression;
    }

    public LimitPipeOperator(Expression limitExpression) {
        this(limitExpression, null);
    }

    public Expression getLimitExpression() {
        return limitExpression;
    }

    public LimitPipeOperator setLimitExpression(Expression limitExpression) {
        this.limitExpression = limitExpression;
        return this;
    }

    public Expression getOffsetExpression() {
        return offsetExpression;
    }

    public LimitPipeOperator setOffsetExpression(Expression offsetExpression) {
        this.offsetExpression = offsetExpression;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("LIMIT ").append(limitExpression);
        if (offsetExpression != null) {
            builder.append(" OFFSET ").append(offsetExpression);
        }
        return builder;
    }
}
