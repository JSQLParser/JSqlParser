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

import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Combines ANY and SOME expressions.
 *
 * @author toben
 */
public class AnyComparisonExpression extends ASTNodeAccessImpl implements Expression {

    private final ItemsList itemsList;
    private boolean useBracketsForValues = false;
    private final SubSelect subSelect;
    private final AnyType anyType;

    public AnyComparisonExpression(AnyType anyType, SubSelect subSelect) {
        this.anyType = anyType;
        this.subSelect = subSelect;
        this.itemsList = null;
    }

    public AnyComparisonExpression(AnyType anyType, ItemsList itemsList) {
        this.anyType = anyType;
        this.itemsList = itemsList;
        this.subSelect = null;
    }

    public SubSelect getSubSelect() {
        return subSelect;
    }

    public ItemsList getItemsList() {
        return itemsList;
    }

    public boolean isUsingItemsList() {
        return itemsList!=null;
    }

    public boolean isUsingSubSelect() {
        return subSelect!=null;
    }
    
    public boolean isUsingBracketsForValues() {
        return useBracketsForValues;
    }

    public void setUseBracketsForValues(boolean useBracketsForValues) {
        this.useBracketsForValues = useBracketsForValues;
    }

    public AnyComparisonExpression withUseBracketsForValues(boolean useBracketsForValues) {
        this.setUseBracketsForValues(useBracketsForValues);
        return this;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public AnyType getAnyType() {
        return anyType;
    }

    @Override
    public String toString() {
        String s = anyType.name() 
                + " (" 
                + ( subSelect!=null 
                    ? subSelect.toString()
                    : "VALUES " + itemsList.toString())
                + " )";
        return s;
    }
}
