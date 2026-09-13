/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

/** Visitor for the structural operators of a row pattern. */
public interface RowPatternVisitor<T> {
    <S> T visit(RowPattern.Variable pattern, S context);

    <S> T visit(RowPattern.Empty pattern, S context);

    <S> T visit(RowPattern.Anchor pattern, S context);

    <S> T visit(RowPattern.Group pattern, S context);

    <S> T visit(RowPattern.Operation pattern, S context);

    <S> T visit(RowPattern.Quantified pattern, S context);

    <S> T visit(RowPattern.Permute pattern, S context);

    <S> T visit(RowPattern.Exclusion pattern, S context);
}
