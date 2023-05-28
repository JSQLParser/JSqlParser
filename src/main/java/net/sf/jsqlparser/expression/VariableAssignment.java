/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Assignment to a user variable like in select @a = 5.
 */
public class VariableAssignment extends ASTNodeAccessImpl implements Expression {

    private UserVariable variable;

    private String operation;

    private Expression expression;

    public UserVariable getVariable() {
        return variable;
    }

    public void setVariable(UserVariable variable) {
        this.variable = variable;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return variable.toString() + " " + operation + " " + expression.toString();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
