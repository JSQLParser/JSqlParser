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

public interface IntoTableVisitor<T> {

    <S> T visit(Table tableName, S context);

    default void visit(Table tableName) {
        this.visit(tableName, null);
    }
}
