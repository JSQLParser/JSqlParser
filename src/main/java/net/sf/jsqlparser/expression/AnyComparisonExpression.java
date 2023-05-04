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
import net.sf.jsqlparser.statement.select.Select;

/**
 * Combines ANY and SOME expressions.
 *
 * @author toben
 */
public class AnyComparisonExpression extends ASTNodeAccessImpl implements Expression {
    private final Select select;
    private final AnyType anyType;

    public AnyComparisonExpression(AnyType anyType, Select select) {
        this.anyType = anyType;
        this.select = select;
    }

    public Select getSelect() {
        return select;
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
        String s = anyType.name() + select;
        return s;
    }
}
