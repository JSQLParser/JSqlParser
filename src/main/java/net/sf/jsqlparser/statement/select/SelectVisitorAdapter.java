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
    public <S> T visit(ParenthesedSelect parenthesedSelect, S context) {
        return parenthesedSelect.getSelect().accept(this, context);
    }

    @Override
    public <S> T visit(PlainSelect plainSelect, S context) {
        return null;
    }

    @Override
    public <S> T visit(SetOperationList setOpList, S context) {
        return null;
    }

    @Override
    public <S> T visit(WithItem withItem, S context) {
        return null;
    }

    @Override
    public <S> T visit(Values aThis, S context) {
        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {
        return null;
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {
        return null;
    }
}
