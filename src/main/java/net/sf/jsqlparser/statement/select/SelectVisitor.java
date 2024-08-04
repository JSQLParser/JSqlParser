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

public interface SelectVisitor<T> {

    <S> T visit(ParenthesedSelect parenthesedSelect, S context);

    default void visit(ParenthesedSelect parenthesedSelect) {
        this.visit(parenthesedSelect, null);
    }

    <S> T visit(PlainSelect plainSelect, S context);

    default void visit(PlainSelect plainSelect) {
        this.visit(plainSelect, null);
    }

    <S> T visit(SetOperationList setOpList, S context);

    default void visit(SetOperationList setOpList) {
        this.visit(setOpList, null);
    }

    <S> T visit(WithItem<?> withItem, S context);

    default void visit(WithItem<?> withItem) {
        this.visit(withItem, null);
    }

    <S> T visit(Values values, S context);

    default void visit(Values values) {
        this.visit(values, null);
    }

    <S> T visit(LateralSubSelect lateralSubSelect, S context);

    default void visit(LateralSubSelect lateralSubSelect) {
        this.visit(lateralSubSelect, null);
    }

    <S> T visit(TableStatement tableStatement, S context);

    default void visit(TableStatement tableStatement) {
        this.visit(tableStatement, null);
    }
}
