/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Partition;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.export.Export;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.InsertConflictAction;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitor;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitorAdapter;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.List;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class StatementVisitorAdapter<T> implements StatementVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;
    private final PivotVisitor<T> pivotVisitor;
    private final SelectItemVisitor<T> selectItemVisitor;
    private final FromItemVisitor<T> fromItemVisitor;
    private final SelectVisitor<T> selectVisitor;
    private final MergeOperationVisitor<T> mergeOperationVisitor;

    public StatementVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<>();
        this.pivotVisitor = new PivotVisitorAdapter<>(this.expressionVisitor);
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = new FromItemVisitorAdapter<>();

        this.selectVisitor = new SelectVisitorAdapter<>(this.expressionVisitor, this.pivotVisitor,
                this.selectItemVisitor, this.fromItemVisitor);
        this.mergeOperationVisitor = new MergeOperationVisitorAdapter<>();
    }

    public StatementVisitorAdapter(ExpressionVisitor<T> expressionVisitor,
            PivotVisitor<T> pivotVisitor, SelectItemVisitor<T> selectItemVisitor,
            FromItemVisitor<T> fromItemVisitor, SelectVisitor<T> selectVisitor,
            MergeOperationVisitor<T> mergeOperationVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = pivotVisitor;
        this.selectItemVisitor = selectItemVisitor;
        this.fromItemVisitor = fromItemVisitor;
        this.selectVisitor = selectVisitor;
        this.mergeOperationVisitor = mergeOperationVisitor;
    }

    public StatementVisitorAdapter(SelectVisitorAdapter<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
        this.expressionVisitor = selectVisitor.getExpressionVisitor();
        this.pivotVisitor = selectVisitor.getPivotVisitor();
        this.selectItemVisitor = selectVisitor.getSelectItemVisitor();
        this.fromItemVisitor = selectVisitor.getFromItemVisitor();
        this.mergeOperationVisitor = new MergeOperationVisitorAdapter<>();
    }

    @Override
    public <S> T visit(Comment comment, S context) {

        return null;
    }

    @Override
    public <S> T visit(Commit commit, S context) {

        return null;
    }

    @Override
    public <S> T visit(Select select, S context) {
        return select.accept(selectVisitor, context);
    }

    @Override
    public <S> T visit(Delete delete, S context) {
        visitWithItems(delete.getWithItemsList(), context);
        fromItemVisitor.visitTables(delete.getTables(), context);
        selectVisitor.visitOutputClause(delete.getOutputClause(), context);
        fromItemVisitor.visitFromItem(delete.getTable(), context);
        fromItemVisitor.visitTables(delete.getUsingList(), context);
        fromItemVisitor.visitJoins(delete.getJoins(), context);
        expressionVisitor.visitExpression(delete.getWhere(), context);

        expressionVisitor.visitPreferringClause(delete.getPreferringClause(), context);

        expressionVisitor.visitOrderBy(delete.getOrderByElements(), context);

        expressionVisitor.visitLimit(delete.getLimit(), context);

        return null;
    }

    private <S> void visitWithItems(List<WithItem<?>> withItemsList, S context) {
        if (withItemsList != null) {
            for (WithItem<?> item : withItemsList) {
                item.accept(this, context);
            }
        }
    }

    @Override
    public <S> T visit(ParenthesedDelete delete, S context) {
        delete.getDelete().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(SessionStatement sessionStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(Update update, S context) {
        visitWithItems(update.getWithItemsList(), context);
        fromItemVisitor.visitFromItem(update.getTable(), context);
        fromItemVisitor.visitJoins(update.getStartJoins(), context);
        expressionVisitor.visitUpdateSets(update.getUpdateSets(), context);
        selectVisitor.visitOutputClause(update.getOutputClause(), context);
        fromItemVisitor.visitFromItem(update.getFromItem(), context);
        fromItemVisitor.visitJoins(update.getJoins(), context);
        expressionVisitor.visitExpression(update.getWhere(), context);
        expressionVisitor.visitPreferringClause(update.getPreferringClause(), context);
        expressionVisitor.visitOrderBy(update.getOrderByElements(), context);
        expressionVisitor.visitLimit(update.getLimit(), context);
        visitReturningClause(update.getReturningClause(), context);
        return null;
    }

    @Override
    public <S> T visit(ParenthesedUpdate update, S context) {
        return update.getUpdate().accept(this, context);
    }

    @Override
    public <S> T visit(Insert insert, S context) {
        visitWithItems(insert.getWithItemsList(), context);

        insert.getTable().accept(fromItemVisitor, context);

        if (insert.getColumns() != null) {
            for (Column column : insert.getColumns()) {
                column.accept(expressionVisitor, context);
            }
        }

        if (insert.getPartitions() != null) {
            for (Partition partition : insert.getPartitions()) {
                partition.getColumn().accept(expressionVisitor, context);
                if (partition.getValue() != null) {
                    partition.getValue().accept(expressionVisitor, context);
                }
            }
        }

        selectVisitor.visitOutputClause(insert.getOutputClause(), context);

        if (insert.getSelect() != null) {
            insert.getSelect().accept(selectVisitor, null);
        }

        expressionVisitor.visitUpdateSets(insert.getSetUpdateSets(), context);

        expressionVisitor.visitUpdateSets(insert.getDuplicateUpdateSets(), context);

        final InsertConflictAction conflictAction = insert.getConflictAction();
        if (conflictAction != null) {
            expressionVisitor.visitExpression(conflictAction.getWhereExpression(), context);
            expressionVisitor.visitUpdateSets(conflictAction.getUpdateSets(), context);
        }

        visitReturningClause(insert.getReturningClause(), context);
        return null;
    }

    private <S> T visitReturningClause(ReturningClause returningClause, S context) {
        if (returningClause != null) {
            returningClause.forEach(selectItem -> selectItem.accept(selectItemVisitor, context));
            // @todo: verify why this is a list of strings and not columns
        }
        return null;
    }

    @Override
    public <S> T visit(ParenthesedInsert insert, S context) {
        insert.getInsert().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(Drop drop, S context) {
        if (drop.getType().equalsIgnoreCase("table")) {
            fromItemVisitor.visitFromItem(drop.getName(), context);
        }
        // @todo: handle schemas

        return null;
    }

    @Override
    public <S> T visit(Truncate truncate, S context) {
        return truncate.getTable().accept(fromItemVisitor, context);
    }

    @Override
    public <S> T visit(CreateIndex createIndex, S context) {

        return null;
    }

    @Override
    public <S> T visit(CreateSchema createSchema, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateTable createTable, S context) {
        return createTable.getTable().accept(fromItemVisitor, context);
    }

    @Override
    public <S> T visit(CreateView createView, S context) {
        return null;
    }

    @Override
    public <S> T visit(Alter alter, S context) {

        return null;
    }

    @Override
    public <S> T visit(Statements statements, S context) {
        for (Statement statement : statements) {
            statement.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(Execute execute, S context) {

        return null;
    }

    @Override
    public <S> T visit(LockStatement lock, S context) {

        return null;
    }

    @Override
    public <S> T visit(SetStatement set, S context) {

        return null;
    }

    @Override
    public <S> T visit(ResetStatement reset, S context) {

        return null;
    }

    @Override
    public <S> T visit(Merge merge, S context) {
        visitWithItems(merge.getWithItemsList(), context);
        fromItemVisitor.visitFromItem(merge.getTable(), context);
        fromItemVisitor.visitFromItem(merge.getFromItem(), context);
        expressionVisitor.visitExpression(merge.getOnCondition(), context);
        mergeOperationVisitor.visit(merge.getOperations(), context);
        selectVisitor.visitOutputClause(merge.getOutputClause(), context);
        return null;
    }

    @Override
    public <S> T visit(AlterView alterView, S context) {
        return null;
    }

    @Override
    public <S> T visit(Upsert upsert, S context) {
        return null;
    }

    @Override
    public <S> T visit(UseStatement use, S context) {
        return null;
    }

    @Override
    public <S> T visit(Block block, S context) {
        return null;
    }

    @Override
    public <S> T visit(DescribeStatement describe, S context) {
        return null;
    }

    @Override
    public <S> T visit(ExplainStatement explainStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowStatement showStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowColumnsStatement showColumnsStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowIndexStatement showIndexStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowTablesStatement showTables, S context) {
        return null;
    }

    @Override
    public <S> T visit(DeclareStatement declareStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(Grant grant, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateSequence createSequence, S context) {
        return null;
    }

    @Override
    public <S> T visit(AlterSequence alterSequence, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateSynonym createSynonym, S context) {
        return null;
    }

    @Override
    public <S> T visit(Analyze analyze, S context) {

        return null;
    }

    @Override
    public <S> T visit(SavepointStatement savepointStatement, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(RollbackStatement rollbackStatement, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(AlterSession alterSession, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.getIfStatement().accept(this, context);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(RenameTableStatement renameTableStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(PurgeStatement purgeStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(AlterSystemStatement alterSystemStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(UnsupportedStatement unsupportedStatement, S context) {

        return null;
    }

    @Override
    public <S> T visit(RefreshMaterializedViewStatement materializedView, S context) {
        return null;
    }

    @Override
    public <S> T visit(Import imprt, S context) {
        return null;
    }

    @Override
    public <S> T visit(Export export, S context) {
        return null;
    }
}
