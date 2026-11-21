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

import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.WindowDefinition;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.piped.AggregatePipeOperator;
import net.sf.jsqlparser.statement.piped.AsPipeOperator;
import net.sf.jsqlparser.statement.piped.CallPipeOperator;
import net.sf.jsqlparser.statement.piped.DropPipeOperator;
import net.sf.jsqlparser.statement.piped.ExtendPipeOperator;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.JoinPipeOperator;
import net.sf.jsqlparser.statement.piped.LimitPipeOperator;
import net.sf.jsqlparser.statement.piped.OrderByPipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperatorVisitor;
import net.sf.jsqlparser.statement.piped.PivotPipeOperator;
import net.sf.jsqlparser.statement.piped.RenamePipeOperator;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.statement.piped.SetOperationPipeOperator;
import net.sf.jsqlparser.statement.piped.SetPipeOperator;
import net.sf.jsqlparser.statement.piped.TableSamplePipeOperator;
import net.sf.jsqlparser.statement.piped.UnPivotPipeOperator;
import net.sf.jsqlparser.statement.piped.WherePipeOperator;
import net.sf.jsqlparser.statement.piped.WindowPipeOperator;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectVisitorAdapter<T> implements SelectVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;
    private final PivotVisitor<T> pivotVisitor;
    private final SelectItemVisitor<T> selectItemVisitor;
    private final FromItemVisitor<T> fromItemVisitor;

    /**
     * Optional, and only needed for data-modifying CTEs - see {@link #visit(WithItem, Object)}. A
     * plain SelectVisitor cannot accept an INSERT/UPDATE/DELETE body; without this the CTE is
     * skipped rather than traversed. {@link net.sf.jsqlparser.statement.StatementVisitorAdapter}
     * sets it automatically.
     */
    private StatementVisitor<T> statementVisitor;

    public SelectVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<>(this);
        this.pivotVisitor = new PivotVisitorAdapter<>(this.expressionVisitor);
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = new FromItemVisitorAdapter<>(this, this.expressionVisitor);
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor,
            PivotVisitor<T> pivotVisitor, SelectItemVisitor<T> selectItemVisitor,
            FromItemVisitor<T> fromItemVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = pivotVisitor;
        this.selectItemVisitor = selectItemVisitor;
        this.fromItemVisitor = fromItemVisitor;
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor,
            FromItemVisitor<T> fromItemVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = new PivotVisitorAdapter<>();
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = fromItemVisitor;
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = new PivotVisitorAdapter<>();
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = new FromItemVisitorAdapter<>(this, this.expressionVisitor);
    }

    @Override
    public <S> T visitOutputClause(OutputClause outputClause, S context) {
        if (outputClause != null) {
            visitSelectItems(outputClause.getSelectItemList(), context);
            if (outputClause.getTableVariable() != null) {
                outputClause.getTableVariable().accept(expressionVisitor, context);
            }
            if (outputClause.getOutputTable() != null) {
                outputClause.getOutputTable().accept(fromItemVisitor, context);
            }
            // @todo: check why this is a list of strings
            // if (outputClause.getColumnList()!=null) {
            // for (Column column:outputClause.getColumnList())
            // }
        }
        return null;
    }

    private <S> void visitSelectItems(List<SelectItem<?>> items, S context) {
        if (items != null) {
            for (SelectItem<?> item : items) {
                item.accept(selectItemVisitor, context);
            }
        }
    }

    public ExpressionVisitor<T> getExpressionVisitor() {
        return expressionVisitor;
    }

    public PivotVisitor<T> getPivotVisitor() {
        return pivotVisitor;
    }

    public SelectItemVisitor<T> getSelectItemVisitor() {
        return selectItemVisitor;
    }

    public FromItemVisitor<T> getFromItemVisitor() {
        return fromItemVisitor;
    }

    public StatementVisitor<T> getStatementVisitor() {
        return statementVisitor;
    }

    public SelectVisitorAdapter<T> setStatementVisitor(StatementVisitor<T> statementVisitor) {
        this.statementVisitor = statementVisitor;
        return this;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {
        visitWithItems(select.withItemsList, context);

        select.getSelect().accept(this, context);

        expressionVisitor.visitOrderBy(select.getOrderByElements(), context);

        Pivot pivot = select.getPivot();
        if (pivot != null) {
            pivot.accept(pivotVisitor, context);
        }
        UnPivot unpivot = select.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(pivotVisitor, context);
        }

        visitLimitOffsetFetch(select, context);
        return null;
    }

    private <S> void visitLimitOffsetFetch(Select select, S context) {
        expressionVisitor.visitLimit(select.getLimitBy(), context);
        expressionVisitor.visitLimit(select.getLimit(), context);
        if (select.getOffset() != null) {
            expressionVisitor.visitExpression(select.getOffset().getOffset(), context);
        }
        if (select.getFetch() != null) {
            expressionVisitor.visitExpression(select.getFetch().getExpression(), context);
        }
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public <S> T visit(PlainSelect plainSelect, S context) {
        visitWithItems(plainSelect.withItemsList, context);

        if (plainSelect.getDistinct() != null) {
            visitSelectItems(plainSelect.getDistinct().getOnSelectItems(), context);
        }

        if (plainSelect.getTop() != null) {
            plainSelect.getTop().getExpression().accept(expressionVisitor, context);
        }

        visitSelectItems(plainSelect.getSelectItems(), context);

        if (plainSelect.getMySqlSelectIntoClause() != null) {
            MySqlSelectIntoClause mySqlSelectIntoClause = plainSelect.getMySqlSelectIntoClause();
            expressionVisitor.visitExpressions(mySqlSelectIntoClause.getVariables(), context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getFileName(), context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getFieldsTerminatedBy(),
                    context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getFieldsEnclosedBy(), context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getFieldsEscapedBy(), context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getLinesStartingBy(), context);
            expressionVisitor.visitExpression(mySqlSelectIntoClause.getLinesTerminatedBy(),
                    context);
        }

        fromItemVisitor.visitTables(plainSelect.getIntoTables(), context);
        fromItemVisitor.visitFromItem(plainSelect.getFromItem(), context);

        // if (plainSelect.getLateralViews() != null) {
        // //@todo: implement this
        // }

        fromItemVisitor.visitJoins(plainSelect.getJoins(), context);

        // if (plainSelect.getKsqlWindow() != null) {
        // //@todo: implement
        // }

        expressionVisitor.visitExpression(plainSelect.getPreWhere(), context);
        expressionVisitor.visitExpression(plainSelect.getWhere(), context);
        expressionVisitor.visitExpression(plainSelect.getOracleHierarchical(), context);

        expressionVisitor.visitPreferringClause(plainSelect.getPreferringClause(), context);
        expressionVisitor.visit(plainSelect.getGroupBy(), context);
        expressionVisitor.visitExpression(plainSelect.getHaving(), context);
        expressionVisitor.visitExpression(plainSelect.getQualify(), context);

        if (plainSelect.getWindowDefinitions() != null) {
            for (WindowDefinition window : plainSelect.getWindowDefinitions()) {
                for (Expression expression : window.getAllExpressions()) {
                    expressionVisitor.visitExpression(expression, context);
                }
            }
        }

        Pivot pivot = plainSelect.getPivot();
        if (pivot != null) {
            pivot.accept(pivotVisitor, context);
        }
        UnPivot unpivot = plainSelect.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(pivotVisitor, context);
        }

        expressionVisitor.visitOrderBy(plainSelect.getOrderByElements(), context);
        visitLimitOffsetFetch(plainSelect, context);

        if (plainSelect.getMySqlProcedureAnalyse() != null) {
            expressionVisitor.visitExpression(
                    plainSelect.getMySqlProcedureAnalyse().getMaxElements(), context);
            expressionVisitor.visitExpression(
                    plainSelect.getMySqlProcedureAnalyse().getMaxMemory(), context);
        }
        // if (plainSelect.getForMode() != null) {
        // //@todo: implement
        // }

        fromItemVisitor.visitFromItem(plainSelect.getIntoTempTable(), context);
        return null;
    }

    @Override
    public <S> T visit(PivotQuery pivotQuery, S context) {
        visitWithItems(pivotQuery.getWithItemsList(), context);

        fromItemVisitor.visitFromItem(pivotQuery.getFromItem(), context);
        expressionVisitor.visitExpressions(pivotQuery.getOnExpressions(), context);

        if (pivotQuery.getUsingItems() != null) {
            for (SelectItem<Function> item : pivotQuery.getUsingItems()) {
                item.accept(selectItemVisitor, context);
            }
        }

        expressionVisitor.visitExpressions(pivotQuery.getGroupByExpressions(), context);
        expressionVisitor.visitOrderBy(pivotQuery.getOrderByElements(), context);
        expressionVisitor.visitLimit(pivotQuery.getLimit(), context);

        if (pivotQuery.getOffset() != null) {
            expressionVisitor.visitExpression(pivotQuery.getOffset().getOffset(), context);
        }
        if (pivotQuery.getFetch() != null) {
            expressionVisitor.visitExpression(pivotQuery.getFetch().getExpression(), context);
        }
        return null;
    }

    @Override
    public <S> T visit(UnPivotQuery unPivotQuery, S context) {
        visitWithItems(unPivotQuery.getWithItemsList(), context);

        fromItemVisitor.visitFromItem(unPivotQuery.getFromItem(), context);
        expressionVisitor.visitExpressions(unPivotQuery.getOnExpressions(), context);
        expressionVisitor.visitExpressions(unPivotQuery.getValueColumns(), context);
        return null;
    }

    @Override
    public <S> T visit(FromQuery fromQuery, S context) {
        visitWithItems(fromQuery.getWithItemsList(), context);
        fromItemVisitor.visitFromItem(fromQuery.getFromItem(), context);
        if (fromQuery.getLateralViews() != null) {
            for (LateralView lateralView : fromQuery.getLateralViews()) {
                expressionVisitor.visitExpression(lateralView.getGeneratorFunction(), context);
            }
        }
        fromItemVisitor.visitJoins(fromQuery.getJoins(), context);

        PipeOperatorWalker<S> pipeOperators = new PipeOperatorWalker<>();
        for (PipeOperator pipeOperator : fromQuery.getPipeOperators()) {
            pipeOperator.accept(pipeOperators, context);
        }
        return null;
    }

    /** Walks the expressions, joins and nested queries of the pipe operators of a FromQuery. */
    private final class PipeOperatorWalker<S> implements PipeOperatorVisitor<T, S> {

        @Override
        public T visit(AggregatePipeOperator aggregate, S context) {
            visitSelectItems(aggregate.getSelectItems(), context);
            visitSelectItems(aggregate.getGroupItems(), context);
            return null;
        }

        @Override
        public T visit(AsPipeOperator as, S context) {
            return null;
        }

        @Override
        public T visit(CallPipeOperator call, S context) {
            return fromItemVisitor.visitFromItem(call.getTableFunction(), context);
        }

        @Override
        public T visit(DropPipeOperator drop, S context) {
            return expressionVisitor.visitExpressions(drop.getColumns(), context);
        }

        @Override
        public T visit(ExtendPipeOperator extend, S context) {
            return visit((SelectPipeOperator) extend, context);
        }

        @Override
        public T visit(JoinPipeOperator join, S context) {
            return fromItemVisitor.visitJoins(Collections.singletonList(join.getJoin()), context);
        }

        @Override
        public T visit(LimitPipeOperator limit, S context) {
            expressionVisitor.visitExpression(limit.getLimitExpression(), context);
            return expressionVisitor.visitExpression(limit.getOffsetExpression(), context);
        }

        @Override
        public T visit(OrderByPipeOperator orderBy, S context) {
            return expressionVisitor.visitOrderBy(orderBy.getOrderByElements(), context);
        }

        @Override
        public T visit(PivotPipeOperator pivot, S context) {
            expressionVisitor.visitExpression(pivot.getAggregateExpression(), context);
            visitSelectItems(pivot.getPivotColumns(), context);
            return null;
        }

        @Override
        public T visit(RenamePipeOperator rename, S context) {
            return visit((SelectPipeOperator) rename, context);
        }

        @Override
        public T visit(SelectPipeOperator select, S context) {
            visitSelectItems(select.getSelectItems(), context);
            return null;
        }

        @Override
        public T visit(SetPipeOperator set, S context) {
            return expressionVisitor.visitUpdateSets(set.getUpdateSets(), context);
        }

        @Override
        public T visit(TableSamplePipeOperator tableSample, S context) {
            return null;
        }

        @Override
        public T visit(SetOperationPipeOperator setOperation, S context) {
            for (ParenthesedSelect select : setOperation.getSelects()) {
                select.accept(SelectVisitorAdapter.this, context);
            }
            return null;
        }

        @Override
        public T visit(UnPivotPipeOperator unPivot, S context) {
            visitSelectItems(unPivot.getPivotColumns(), context);
            return null;
        }

        @Override
        public T visit(WherePipeOperator where, S context) {
            return expressionVisitor.visitExpression(where.getExpression(), context);
        }

        @Override
        public T visit(WindowPipeOperator window, S context) {
            return visit((SelectPipeOperator) window, context);
        }
    }

    @Override
    public <S> T visit(SetOperationList setOpList, S context) {
        visitWithItems(setOpList.getWithItemsList(), context);
        for (Select select : setOpList.getSelects()) {
            select.accept(this, context);
        }
        expressionVisitor.visitOrderBy(setOpList.getOrderByElements(), context);
        visitLimitOffsetFetch(setOpList, context);
        return null;
    }

    /**
     * A CTE body is a {@link ParenthesedStatement}, which is not necessarily a SELECT: PostgreSQL
     * and others allow {@code WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c}.
     *
     * <p>
     * The previous implementation called {@code withItem.getSelect()}, an unchecked cast to
     * {@link ParenthesedSelect}, and threw {@link ClassCastException} on every data-modifying CTE
     * reached through the select path - which {@code ExpressionVisitorAdapter#visit(Select)} does
     * for any sub-select. {@code WithItem#accept(StatementVisitor)} has always dispatched correctly
     * through {@code getParenthesedStatement()}; only this path did not.
     */
    @Override
    public <S> T visit(WithItem<?> withItem, S context) {
        if (withItem.getCycleClause() != null) {
            withItem.getCycleClause().accept(expressionVisitor, context);
        }
        ParenthesedStatement body = withItem.getParenthesedStatement();

        // ParenthesedSelect is a Select and stays on the select path
        if (body instanceof Select) {
            return ((Select) body).accept(this, context);
        }

        // ParenthesedInsert / ParenthesedUpdate / ParenthesedDelete need a StatementVisitor
        if (statementVisitor != null && body instanceof Statement) {
            return ((Statement) body).accept(statementVisitor, context);
        }

        // a ClickHouse expression alias (WITH <expression> AS <identifier>): a
        // parenthesized subquery stays on the select path, any other expression
        // goes to the expression visitor
        Expression expression = withItem.getExpression();
        if (expression != null) {
            if (expression instanceof Select) {
                return ((Select) expression).accept(this, context);
            }
            if (expressionVisitor != null) {
                return expression.accept(expressionVisitor, context);
            }
        }

        // no statement visitor available: skip the body rather than fail
        return null;
    }

    @Override
    public <S> T visit(Values values, S context) {
        visitWithItems(values.getWithItemsList(), context);
        expressionVisitor.visitExpressions(values.getExpressions(), context);
        expressionVisitor.visitOrderBy(values.getOrderByElements(), context);
        visitLimitOffsetFetch(values, context);
        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {
        return lateralSubSelect.getSelect().accept(this, context);
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {
        visitWithItems(tableStatement.getWithItemsList(), context);
        expressionVisitor.visitOrderBy(tableStatement.getOrderByElements(), context);
        visitLimitOffsetFetch(tableStatement, context);
        return null;
    }
}
