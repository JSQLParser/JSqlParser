package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.ExpressionVisitor;

public class GeometryDistance extends ComparisonOperator {

    public GeometryDistance() {
        super("<->");
    }

    public GeometryDistance(String operator) {
        super(operator);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
