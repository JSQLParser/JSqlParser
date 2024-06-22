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
    public <S> T visit(Table table, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(TableFunction tableFunction, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedFromItem fromItem, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(Values values, S parameters) {

        return null;
    }
}
