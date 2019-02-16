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
import net.sf.jsqlparser.statement.select.SubSelect;

public class AnyComparisonExpression extends ASTNodeAccessImpl implements Expression {

    private final SubSelect subSelect;
    private final AnyType anyType;

    public AnyComparisonExpression(AnyType anyType, SubSelect subSelect) {
        this.anyType = anyType;
        this.subSelect = subSelect;
    }

    public SubSelect getSubSelect() {
        return subSelect;
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
        return anyType.name() + " " + subSelect.toString();
    }
}
