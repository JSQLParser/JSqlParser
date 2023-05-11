package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class MemberOfExpression extends ASTNodeAccessImpl implements Expression {

    Expression leftExpression;
    Expression rightExpression;
    boolean isNot;

    public MemberOfExpression(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public MemberOfExpression setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
        return this;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public MemberOfExpression setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
        return this;
    }

    public boolean isNot() {
        return isNot;
    }

    public MemberOfExpression setNot(boolean not) {
        isNot = not;
        return this;
    }

    @Override
    public String toString() {
        return leftExpression + " MEMBER OF " + rightExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
