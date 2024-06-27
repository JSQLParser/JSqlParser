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

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class MergeOperationVisitorAdapter<T> implements MergeOperationVisitor<T> {
    @Override
    public <S> T visit(MergeDelete mergeDelete, S context) {
        return null;
    }

    @Override
    public <S> T visit(MergeUpdate mergeUpdate, S context) {
        return null;
    }

    @Override
    public <S> T visit(MergeInsert mergeInsert, S context) {
        return null;
    }
}
