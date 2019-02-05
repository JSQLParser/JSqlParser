/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2018 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/**
 * 
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Models a list of expressions usable as condition.<br>
 * This allows for instance the following expression : 
 * <code>"[WHERE] (a, b) [OPERATOR] (c, d)"</code>
 * where "(a, b)" and "(c, d)" are instances of this class.
 * 
 * @author adriil
 */
public class ValueListExpression extends ASTNodeAccessImpl implements Expression {

    private ExpressionList expressionList;
    
    public ExpressionList getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(ExpressionList expressionList) {
        this.expressionList = expressionList;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
    
    @Override
    public String toString() {
        return expressionList.toString();
    }

}
