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

import net.sf.jsqlparser.schema.Table;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class FromItemVisitorAdapter<T> implements FromItemVisitor<T> {

    @Override
    public <S> T visit(Table table, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {

        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {

        return null;
    }

    @Override
    public <S> T visit(TableFunction tableFunction, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedFromItem fromItem, S context) {

        return null;
    }

    @Override
    public <S> T visit(Values values, S context) {

        return null;
    }

    @Override
    public <S> T visit(PlainSelect plainSelect, S context) {

        return null;
    }

    @Override
    public <S> T visit(SetOperationList setOperationList, S context) {

        return null;
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {

        return null;
    }
}
