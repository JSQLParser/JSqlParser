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
    public T visit(Table table) {

        return null;
    }

    @Override
    public T visit(ParenthesedSelect selectBody) {

        return null;
    }

    @Override
    public T visit(LateralSubSelect lateralSubSelect) {

        return null;
    }

    @Override
    public T visit(TableFunction valuesList) {

        return null;
    }

    @Override
    public T visit(ParenthesedFromItem aThis) {

        return null;
    }

    @Override
    public T visit(Values values) {

        return null;
    }
}
