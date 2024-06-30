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

    <S> T visit(Pivot pivot, S context);

    default void visit(Pivot pivot) {
        this.visit(pivot, null);
    }

    <S> T visit(PivotXml pivotXml, S context);

    default void visit(PivotXml pivotXml) {
        this.visit(pivotXml, null);
    }

    <S> T visit(UnPivot unpivot, S context);

    default void visit(UnPivot unpivot) {
        this.visit(unpivot, null);
    }
}
