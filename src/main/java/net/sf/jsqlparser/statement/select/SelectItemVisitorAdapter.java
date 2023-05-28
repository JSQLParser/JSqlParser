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

@SuppressWarnings({ "PMD.UncommentedEmptyMethodBody" })
public class SelectItemVisitorAdapter implements SelectItemVisitor {

    @Override
    public void visit(AllColumns columns) {
    }

    @Override
    public void visit(AllTableColumns columns) {
    }

    @Override
    public void visit(SelectExpressionItem item) {
    }
}
