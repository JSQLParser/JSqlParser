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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class MergeOperationVisitorAdapter<T> implements MergeOperationVisitor<T> {
    private ExpressionVisitor<T> expressionVisitor;

    public MergeOperationVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<>();
    }

    public MergeOperationVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    public MergeOperationVisitorAdapter(SelectVisitorAdapter<T> selectVisitorAdapter) {
        this.expressionVisitor = selectVisitorAdapter.getExpressionVisitor();
    }

    @Override
    public <S> T visit(MergeDelete mergeDelete, S context) {
        expressionVisitor.visitExpression(mergeDelete.getAndPredicate(), context);
        return null;
    }

    @Override
    public <S> T visit(MergeUpdate mergeUpdate, S context) {
        expressionVisitor.visitExpression(mergeUpdate.getAndPredicate(), context);
        expressionVisitor.visitUpdateSets(mergeUpdate.getUpdateSets(), context);
        expressionVisitor.visitExpression(mergeUpdate.getWhereCondition(), context);
        expressionVisitor.visitExpression(mergeUpdate.getDeleteWhereCondition(), context);
        return null;
    }

    @Override
    public <S> T visit(MergeInsert mergeInsert, S context) {
        expressionVisitor.visitExpression(mergeInsert.getAndPredicate(), context);
        expressionVisitor.visitExpressions(mergeInsert.getColumns(), context);
        expressionVisitor.visitExpressions(mergeInsert.getValues(), context);
        expressionVisitor.visitExpression(mergeInsert.getWhereCondition(), context);
        return null;
    }
}
