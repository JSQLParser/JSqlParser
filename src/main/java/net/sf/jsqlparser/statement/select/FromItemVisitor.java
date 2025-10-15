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

import net.sf.jsqlparser.expression.JsonTable;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.piped.FromQuery;

import java.util.Collection;
import java.util.List;

public interface FromItemVisitor<T> {

    default <S> T visitFromItem(FromItem fromItem, S context) {
        if (fromItem != null) {
            fromItem.accept(this, context);
        }
        return null;
    }

    default <S> T visitTables(List<Table> tables, S context) {
        if (tables != null) {
            for (Table table : tables) {
                table.accept(this, context);
            }
        }
        return null;
    }

    default <S> T visitJoins(Collection<Join> joins, S context) {
        if (joins != null) {
            for (Join join : joins) {
                join.getFromItem().accept(this, context);
            }
        }
        return null;
    }

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

    <S> T visit(PlainSelect plainSelect, S context);

    default void visit(PlainSelect plainSelect) {
        this.visit(plainSelect, null);
    }

    <S> T visit(SetOperationList setOperationList, S context);

    default void visit(SetOperationList setOperationList) {
        this.visit(setOperationList, null);
    }

    <S> T visit(TableStatement tableStatement, S context);

    default void visit(TableStatement tableStatement) {
        this.visit(tableStatement, null);
    }

    <S> T visit(Import imprt, S context);

    default void visit(Import imprt) {
        this.visit(imprt, null);
    }

    <S> T visit(FromQuery fromQuery, S context);

    default void visit(JsonTable jsonTable) {
        this.visit(jsonTable, null);
    }

    <S> T visit(JsonTable jsonTable, S context);
}
