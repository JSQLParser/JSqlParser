package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class TranscodingFunction extends ASTNodeAccessImpl implements Expression {
    private Expression expression;
    private String transcodingName;

    public TranscodingFunction(Expression expression, String transcodingName) {
        this.expression = expression;
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction() {
        this(null, null);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public TranscodingFunction withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public String getTranscodingName() {
        return transcodingName;
    }

    public void setTranscodingName(String transcodingName) {
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction withTranscodingName(String transcodingName) {
        this.setTranscodingName(transcodingName);
        return this;

    }

    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder
                .append("CONVERT( ")
                .append(expression)
                .append(" USING ")
                .append(transcodingName)
                .append(" )");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
