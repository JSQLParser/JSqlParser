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

    <S> T visit(ParenthesedSelect parenthesedSelect, S parameters);

    <S> T visit(PlainSelect plainSelect, S parameters);

    <S> T visit(SetOperationList setOpList, S parameters);

    <S> T visit(WithItem withItem, S parameters);

    <S> T visit(Values aThis, S parameters);

    <S> T visit(LateralSubSelect lateralSubSelect, S parameters);

    <S> T visit(TableStatement tableStatement, S parameters);
}
