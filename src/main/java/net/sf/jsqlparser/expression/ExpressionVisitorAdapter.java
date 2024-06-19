/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.ContainedBy;
import net.sf.jsqlparser.expression.operators.relational.Contains;
import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Optional;

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.UncommentedEmptyMethodBody"})
public class ExpressionVisitorAdapter
        implements ExpressionVisitor<Void>, PivotVisitor<Void>, SelectItemVisitor<Void> {

    private SelectVisitor<Void> selectVisitor;

    public SelectVisitor<Void> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<Void> selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    @Override
    public Void visit(NullValue value) {

        return null;
    }

    @Override
    public Void visit(Function function) {
        if (function.getParameters() != null) {
            function.getParameters().accept(this);
        }
        if (function.getKeep() != null) {
            function.getKeep().accept(this);
        }
        if (function.getOrderByElements() != null) {
            for (OrderByElement orderByElement : function.getOrderByElements()) {
                orderByElement.getExpression().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(SignedExpression expr) {
        expr.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(JdbcParameter parameter) {

        return null;
    }

    @Override
    public Void visit(JdbcNamedParameter parameter) {

        return null;
    }

    @Override
    public Void visit(DoubleValue value) {

        return null;
    }

    @Override
    public Void visit(LongValue value) {

        return null;
    }

    @Override
    public Void visit(DateValue value) {

        return null;
    }

    @Override
    public Void visit(TimeValue value) {

        return null;
    }

    @Override
    public Void visit(TimestampValue value) {

        return null;
    }

    @Override
    public Void visit(StringValue value) {

        return null;
    }

    @Override
    public Void visit(Addition expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Division expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(IntegerDivision expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Multiplication expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Subtraction expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(AndExpression expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(OrExpression expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(XorExpression expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Between expr) {
        expr.getLeftExpression().accept(this);
        expr.getBetweenExpressionStart().accept(this);
        expr.getBetweenExpressionEnd().accept(this);
        return null;
    }

    public Void visit(OverlapsCondition overlapsCondition) {
        overlapsCondition.getLeft().accept(this);
        overlapsCondition.getRight().accept(this);
        return null;
    }


    @Override
    public Void visit(EqualsTo expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(GreaterThan expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(GreaterThanEquals expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(InExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(IncludesExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(ExcludesExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(IsNullExpression expr) {
        expr.getLeftExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(FullTextSearch expr) {
        for (Column col : expr.getMatchColumns()) {
            col.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IsBooleanExpression expr) {
        expr.getLeftExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(LikeExpression expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(MinorThan expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(MinorThanEquals expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(NotEqualsTo expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(DoubleAnd expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Contains expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(ContainedBy expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Column column) {

        return null;
    }

    @Override
    public Void visit(ParenthesedSelect selectBody) {
        visit((Select) selectBody);
        if (selectBody.getPivot() != null) {
            selectBody.getPivot().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CaseExpression expr) {
        if (expr.getSwitchExpression() != null) {
            expr.getSwitchExpression().accept(this);
        }
        for (Expression x : expr.getWhenClauses()) {
            x.accept(this);
        }
        if (expr.getElseExpression() != null) {
            expr.getElseExpression().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhenClause expr) {
        expr.getWhenExpression().accept(this);
        expr.getThenExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(ExistsExpression expr) {
        expr.getRightExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(MemberOfExpression memberOfExpression) {
        memberOfExpression.getRightExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(AnyComparisonExpression expr) {

        return null;
    }

    @Override
    public Void visit(Concat expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(Matches expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(BitwiseAnd expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(BitwiseOr expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(BitwiseXor expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(CastExpression expr) {
        expr.getLeftExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(Modulo expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(AnalyticExpression expr) {
        if (expr.getExpression() != null) {
            expr.getExpression().accept(this);
        }
        if (expr.getDefaultValue() != null) {
            expr.getDefaultValue().accept(this);
        }
        if (expr.getOffset() != null) {
            expr.getOffset().accept(this);
        }
        if (expr.getKeep() != null) {
            expr.getKeep().accept(this);
        }
        if (expr.getFuncOrderBy() != null) {
            for (OrderByElement element : expr.getOrderByElements()) {
                element.getExpression().accept(this);
            }
        }
        if (expr.getWindowElement() != null) {
            /*
             * Visit expressions from the range and offset of the window element. Do this using
             * optional chains, because several things down the tree can be null e.g. the
             * expression. So, null-safe versions of e.g.:
             * expr.getWindowElement().getOffset().getExpression().accept(this);
             */
            Optional.ofNullable(expr.getWindowElement().getRange()).map(WindowRange::getStart)
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this));
            Optional.ofNullable(expr.getWindowElement().getRange()).map(WindowRange::getEnd)
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this));
            Optional.ofNullable(expr.getWindowElement().getOffset())
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this));
        }
        return null;
    }

    @Override
    public Void visit(ExtractExpression expr) {
        expr.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(IntervalExpression expr) {
        return null;
    }

    @Override
    public Void visit(OracleHierarchicalExpression expr) {
        expr.getConnectExpression().accept(this);
        expr.getStartExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(RegExpMatchOperator expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(ExpressionList<?> expressionList) {
        for (Expression expr : expressionList) {
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(RowConstructor<?> rowConstructor) {
        for (Expression expr : rowConstructor) {
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(NotExpression notExpr) {
        notExpr.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr);
        return null;
    }

    protected void visitBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    @Override
    public Void visit(JsonExpression jsonExpr) {
        jsonExpr.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(JsonOperator expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(UserVariable var) {

        return null;
    }

    @Override
    public Void visit(NumericBind bind) {

        return null;
    }

    @Override
    public Void visit(KeepExpression expr) {
        for (OrderByElement element : expr.getOrderByElements()) {
            element.getExpression().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(MySQLGroupConcat groupConcat) {
        for (Expression expr : groupConcat.getExpressionList()) {
            expr.accept(this);
        }
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                element.getExpression().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(Pivot pivot) {
        for (SelectItem<?> item : pivot.getFunctionItems()) {
            item.getExpression().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectItem<?> item : pivot.getSingleInItems()) {
                item.getExpression().accept(this);
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (SelectItem<ExpressionList> item : pivot.getMultiInItems()) {
                item.getExpression().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(PivotXml pivot) {
        for (SelectItem<?> item : pivot.getFunctionItems()) {
            item.getExpression().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getInSelect() != null && selectVisitor != null) {
            pivot.getInSelect().accept(selectVisitor);
        }
        return null;
    }

    @Override
    public Void visit(UnPivot unpivot) {
        unpivot.accept(this);
        return null;
    }

    @Override
    public Void visit(AllColumns allColumns) {
        return null;
    }

    @Override
    public Void visit(AllTableColumns allTableColumns) {
        return null;
    }

    @Override
    public Void visit(AllValue allValue) {
        return null;
    }

    @Override
    public Void visit(IsDistinctExpression isDistinctExpression) {
        visitBinaryExpression(isDistinctExpression);
        return null;
    }

    @Override
    public Void visit(SelectItem<?> selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(RowGetExpression rowGetExpression) {
        rowGetExpression.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(HexValue hexValue) {

        return null;
    }

    @Override
    public Void visit(OracleHint hint) {

        return null;
    }

    @Override
    public Void visit(TimeKeyExpression timeKeyExpression) {

        return null;
    }

    @Override
    public Void visit(DateTimeLiteralExpression literal) {
        return null;
    }

    @Override
    public Void visit(NextValExpression nextVal) {
        return null;
    }

    @Override
    public Void visit(CollateExpression col) {
        col.getLeftExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ArrayConstructor aThis) {
        for (Expression expression : aThis.getExpressions()) {
            expression.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VariableAssignment var) {
        var.getVariable().accept(this);
        var.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(XMLSerializeExpr expr) {
        expr.getExpression().accept(this);
        for (OrderByElement elm : expr.getOrderByElements()) {
            elm.getExpression().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(TimezoneExpression expr) {
        expr.getLeftExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(JsonAggregateFunction expression) {
        Expression expr = expression.getExpression();
        if (expr != null) {
            expr.accept(this);
        }

        expr = expression.getFilterExpression();
        if (expr != null) {
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(JsonFunction expression) {
        for (JsonFunctionExpression expr : expression.getExpressions()) {
            expr.getExpression().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConnectByRootOperator connectByRootOperator) {
        connectByRootOperator.getColumn().accept(this);
        return null;
    }

    @Override
    public Void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        oracleNamedFunctionParameter.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(GeometryDistance geometryDistance) {
        visitBinaryExpression(geometryDistance);
        return null;
    }

    @Override
    public Void visit(Select selectBody) {
        if (selectVisitor != null) {
            if (selectBody.getWithItemsList() != null) {
                for (WithItem item : selectBody.getWithItemsList()) {
                    item.accept(selectVisitor);
                }
            }
            selectBody.accept(selectVisitor);
        }
        return null;
    }

    @Override
    public Void visit(TranscodingFunction transcodingFunction) {

        return null;
    }

    @Override
    public Void visit(TrimFunction trimFunction) {

        return null;
    }

    @Override
    public Void visit(RangeExpression rangeExpression) {
        rangeExpression.getStartExpression().accept(this);
        rangeExpression.getEndExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(TSQLLeftJoin tsqlLeftJoin) {
        visitBinaryExpression(tsqlLeftJoin);
        return null;
    }

    @Override
    public Void visit(TSQLRightJoin tsqlRightJoin) {
        visitBinaryExpression(tsqlRightJoin);
        return null;
    }

    @Override
    public Void visit(StructType structType) {
        // @todo: visit the ColType also
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                visit(selectItem);
            }
        }
        return null;
    }

    @Override
    public Void visit(LambdaExpression lambdaExpression) {
        lambdaExpression.getExpression().accept(this);
        return null;
    }

}
