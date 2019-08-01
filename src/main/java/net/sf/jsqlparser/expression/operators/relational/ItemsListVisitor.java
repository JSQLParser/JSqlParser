/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.statement.select.SubSelect;

public interface ItemsListVisitor {

    default void visit(SubSelect subSelect) { // default implementation ignored
    }

    default void visit(ExpressionList expressionList) { // default implementation ignored
    }

    default void visit(NamedExpressionList namedExpressionList) { // default implementation ignored
    }

    default void visit(MultiExpressionList multiExprList) { // default implementation ignored
    }
}
