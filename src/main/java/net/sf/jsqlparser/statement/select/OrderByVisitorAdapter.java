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
public class OrderByVisitorAdapter<T> implements OrderByVisitor<T> {

    @Override
    public <S> T visit(OrderByElement orderBy, S context) {
        return null;
    }
}
