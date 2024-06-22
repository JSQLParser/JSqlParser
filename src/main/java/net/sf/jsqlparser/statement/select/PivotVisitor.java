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

public interface PivotVisitor<T> {

    <S> T visit(Pivot pivot, S parameters);

    <S> T visit(PivotXml pivot, S parameters);

    <S> T visit(UnPivot unpivot, S parameters);

}
