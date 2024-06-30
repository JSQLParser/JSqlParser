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

public interface FromItemVisitor<T> {

    <S> T visit(Table tableName, S context);

    default void visit(Table tableName) {
        this.visit(tableName, null);
    }

    <S> T visit(ParenthesedSelect selectBody, S context);

    default void visit(ParenthesedSelect selectBody) {
        this.visit(selectBody, null);
    }

    <S> T visit(LateralSubSelect lateralSubSelect, S context);

    default void visit(LateralSubSelect lateralSubSelect) {
        this.visit(lateralSubSelect, null);
    }

    <S> T visit(TableFunction tableFunction, S context);

    default void visit(TableFunction tableFunction) {
        this.visit(tableFunction, null);
    }

    <S> T visit(ParenthesedFromItem parenthesedFromItem, S context);

    default void visit(ParenthesedFromItem parenthesedFromItem) {
        this.visit(parenthesedFromItem, null);
    }

    <S> T visit(Values values, S context);

    default void visit(Values values) {
        this.visit(values, null);
    }
}
