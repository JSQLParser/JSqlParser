/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class OverlapsCondition extends ASTNodeAccessImpl implements Expression{


    private RowValueConstructor left;
    private RowValueConstructor right;


    public OverlapsCondition(RowValueConstructor left, RowValueConstructor right) {
        this.left = left;
        this.right = right;
    }

    public RowValueConstructor getLeft() {
        return left;
    }

    public RowValueConstructor getRight() {
        return right;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
            expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s OVERLAPS %s"
                , left.toString()
                , right.toString()
        );
    }
}
