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

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectVisitorAdapter<T> implements SelectVisitor<T> {

    @Override
    public T visit(ParenthesedSelect parenthesedSelect) {
        parenthesedSelect.getSelect().accept(this);
        return null;
    }

    @Override
    public T visit(PlainSelect plainSelect) {
        return null;
    }

    @Override
    public T visit(SetOperationList setOpList) {
        return null;
    }

    @Override
    public T visit(WithItem withItem) {
        return null;
    }

    @Override
    public T visit(Values aThis) {
        return null;
    }

    @Override
    public T visit(LateralSubSelect lateralSubSelect) {
        return null;
    }

    @Override
    public T visit(TableStatement tableStatement) {
        return null;
    }
}
