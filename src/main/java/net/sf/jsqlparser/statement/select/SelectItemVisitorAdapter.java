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

import net.sf.jsqlparser.expression.Expression;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectItemVisitorAdapter<T> implements SelectItemVisitor<T> {
    @Override
    public <S> T visit(SelectItem<? extends Expression> item, S parameters) {
        return null;
    }
}
