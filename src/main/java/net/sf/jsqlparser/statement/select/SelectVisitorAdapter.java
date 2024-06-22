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
    public <S> T visit(ParenthesedSelect parenthesedSelect, S parameters) {
        return parenthesedSelect.getSelect().accept(this, parameters);
    }

    @Override
    public <S> T visit(PlainSelect plainSelect, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(SetOperationList setOpList, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(WithItem withItem, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(Values aThis, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S parameters) {
        return null;
    }
}
