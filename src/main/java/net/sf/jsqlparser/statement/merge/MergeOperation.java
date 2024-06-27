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

/**
 * Marker interface to cover {@link MergeDelete}, {@link MergeUpdate} and {@link MergeInsert}
 */
public interface MergeOperation {
    <S, T> T accept(MergeOperationVisitor<T> mergeOperationVisitor, S context);
}
