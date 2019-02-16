/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

public interface SelectItemVisitor {

    void visit(AllColumns allColumns);

    void visit(AllTableColumns allTableColumns);

    void visit(SelectExpressionItem selectExpressionItem);
}
