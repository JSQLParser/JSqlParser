package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class RangeExpression extends ASTNodeAccessImpl implements Expression {
    private Expression startExpression;
    private Expression endExpression;

    public RangeExpression(Expression startExpression, Expression endExpression) {
        this.startExpression = startExpression;
        this.endExpression = endExpression;
    }

    public Expression getStartExpression() {
        return startExpression;
    }

    public RangeExpression setStartExpression(Expression startExpression) {
        this.startExpression = startExpression;
        return this;
    }

    public Expression getEndExpression() {
        return endExpression;
    }

    public RangeExpression setEndExpression(Expression endExpression) {
        this.endExpression = endExpression;
        return this;
    }

    @Override
    public String toString() {
        return startExpression + ":" + endExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
