/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.statement.select.SubSelect;

public class ItemsListVisitorAdapter implements ItemsListVisitor {

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {

    }

    @Override
    public void visit(ExpressionList expressionList) {

    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList list : multiExprList.getExprList()) {
            visit(list);
        }
    }
}
