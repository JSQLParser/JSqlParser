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

import net.sf.jsqlparser.statement.values.ValuesStatement;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectVisitorAdapter implements SelectVisitor {

    @Override
    public void visit(ParenthesedSelectBody parenthesedSelectBody) {
        parenthesedSelectBody.getSelectBody().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {

    }

    @Override
    public void visit(SetOperationList setOpList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }
}
