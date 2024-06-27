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

public interface GroupByVisitor<T> {

    <S> T visit(GroupByElement groupBy, S context);

    default void visit(GroupByElement groupBy) {
        this.visit(groupBy, null);
    }
}
