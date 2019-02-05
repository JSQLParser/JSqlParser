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

public interface ItemsListVisitor {

    void visit(SubSelect subSelect);

    void visit(ExpressionList expressionList);

    void visit(NamedExpressionList namedExpressionList);

    void visit(MultiExpressionList multiExprList);
}
