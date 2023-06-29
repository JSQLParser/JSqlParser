package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class InterpretExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private ColDataType type;
    private RowConstructor rowConstructor;

    public RowConstructor getRowConstructor() {
        return rowConstructor;
    }

    public void setRowConstructor(RowConstructor rowConstructor) {
        this.rowConstructor = rowConstructor;
        this.type = null;
    }

    public InterpretExpression withRowConstructor(RowConstructor rowConstructor) {
        setRowConstructor(rowConstructor);
        return this;
    }

    public ColDataType getType() {
        return type;
    }

    public void setType(ColDataType type) {
        this.type = type;
        this.rowConstructor = null;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return rowConstructor != null
                ? "INTERPRET(" + leftExpression + " AS " + rowConstructor.toString() + ")"
                : "INTERPRET(" + leftExpression + " AS " + type.toString() + ")";
    }

    public InterpretExpression withType(ColDataType type) {
        this.setType(type);
        return this;
    }

    public InterpretExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
