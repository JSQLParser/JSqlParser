/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

public interface MergeOperationVisitor<T> {

    <S> T visit(MergeDelete mergeDelete, S context);

    <S> T visit(MergeUpdate mergeUpdate, S context);

    <S> T visit(MergeInsert mergeInsert, S context);
}
