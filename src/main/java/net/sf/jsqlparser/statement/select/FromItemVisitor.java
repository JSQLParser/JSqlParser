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

    <S> T visit(Table tableName, S parameters);

    <S> T visit(ParenthesedSelect selectBody, S parameters);

    <S> T visit(LateralSubSelect lateralSubSelect, S parameters);

    <S> T visit(TableFunction tableFunction, S parameters);

    <S> T visit(ParenthesedFromItem aThis, S parameters);

    <S> T visit(Values values, S parameters);
}
