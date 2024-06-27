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
public class ExpressionVisitorAdapter<T>
        implements ExpressionVisitor<T>, PivotVisitor<T>, SelectItemVisitor<T> {

    private SelectVisitor<T> selectVisitor;

    public SelectVisitor<T> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    @Override
    public <S> T visit(NullValue nullValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(Function function, S context) {
        if (function.getParameters() != null) {
            function.getParameters().accept(this, context);
        }
        if (function.getKeep() != null) {
            function.getKeep().accept(this, context);
        }
        if (function.getOrderByElements() != null) {
            for (OrderByElement orderByElement : function.getOrderByElements()) {
                orderByElement.getExpression().accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(SignedExpression signedExpression, S context) {
        signedExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(JdbcParameter jdbcParameter, S context) {

        return null;
    }

    @Override
    public <S> T visit(JdbcNamedParameter jdbcNamedParameter, S context) {

        return null;
    }

    @Override
    public <S> T visit(DoubleValue doubleValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(LongValue longValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(DateValue dateValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(TimeValue timeValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(TimestampValue timestampValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(StringValue stringValue, S context) {
        return null;
    }

    @Override
    public <S> T visit(Addition addition, S context) {
        visitBinaryExpression(addition, context);
        return null;
    }

    @Override
    public <S> T visit(Division division, S context) {
        visitBinaryExpression(division, context);
        return null;
    }

    @Override
    public <S> T visit(IntegerDivision integerDivision, S context) {
        visitBinaryExpression(integerDivision, context);
        return null;
    }

    @Override
    public <S> T visit(Multiplication multiplication, S context) {
        visitBinaryExpression(multiplication, context);
        return null;
    }

    @Override
    public <S> T visit(Subtraction subtraction, S context) {
        visitBinaryExpression(subtraction, context);
        return null;
    }

    @Override
    public <S> T visit(AndExpression andExpression, S context) {
        visitBinaryExpression(andExpression, context);
        return null;
    }

    @Override
    public <S> T visit(OrExpression orExpression, S context) {
        visitBinaryExpression(orExpression, context);
        return null;
    }

    @Override
    public <S> T visit(XorExpression xorExpression, S context) {
        visitBinaryExpression(xorExpression, context);
        return null;
    }

    @Override
    public <S> T visit(Between between, S context) {
        between.getLeftExpression().accept(this, context);
        between.getBetweenExpressionStart().accept(this, context);
        between.getBetweenExpressionEnd().accept(this, context);
        return null;
    }

    public <S> T visit(OverlapsCondition overlapsCondition, S context) {
        overlapsCondition.getLeft().accept(this, context);
        overlapsCondition.getRight().accept(this, context);
        return null;
    }


    @Override
    public <S> T visit(EqualsTo equalsTo, S context) {
        visitBinaryExpression(equalsTo, context);
        return null;
    }

    @Override
    public <S> T visit(GreaterThan greaterThan, S context) {
        visitBinaryExpression(greaterThan, context);
        return null;
    }

    @Override
    public <S> T visit(GreaterThanEquals greaterThanEquals, S context) {
        visitBinaryExpression(greaterThanEquals, context);
        return null;
    }

    @Override
    public <S> T visit(InExpression inExpression, S context) {
        inExpression.getLeftExpression().accept(this, context);
        inExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(IncludesExpression includesExpression, S context) {
        includesExpression.getLeftExpression().accept(this, context);
        includesExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(ExcludesExpression excludesExpression, S context) {
        excludesExpression.getLeftExpression().accept(this, context);
        excludesExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(IsNullExpression isNullExpression, S context) {
        isNullExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(FullTextSearch fullTextSearch, S context) {
        for (Column col : fullTextSearch.getMatchColumns()) {
            col.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(IsBooleanExpression isBooleanExpression, S context) {
        isBooleanExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(LikeExpression likeExpression, S context) {
        visitBinaryExpression(likeExpression, context);
        return null;
    }

    @Override
    public <S> T visit(MinorThan minorThan, S context) {
        visitBinaryExpression(minorThan, context);
        return null;
    }

    @Override
    public <S> T visit(MinorThanEquals minorThanEquals, S context) {
        visitBinaryExpression(minorThanEquals, context);
        return null;
    }

    @Override
    public <S> T visit(NotEqualsTo notEqualsTo, S context) {
        visitBinaryExpression(notEqualsTo, context);
        return null;
    }

    @Override
    public <S> T visit(DoubleAnd doubleAnd, S context) {
        visitBinaryExpression(doubleAnd, context);
        return null;
    }

    @Override
    public <S> T visit(Contains contains, S context) {
        visitBinaryExpression(contains, context);
        return null;
    }

    @Override
    public <S> T visit(ContainedBy containedBy, S context) {
        visitBinaryExpression(containedBy, context);
        return null;
    }

    @Override
    public <S> T visit(Column column, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {
        visit((Select) select, context);
        if (select.getPivot() != null) {
            select.getPivot().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(CaseExpression caseExpression, S context) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this, context);
        }
        for (Expression x : caseExpression.getWhenClauses()) {
            x.accept(this, context);
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(WhenClause whenClause, S context) {
        whenClause.getWhenExpression().accept(this, context);
        whenClause.getThenExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(ExistsExpression existsExpression, S context) {
        existsExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(MemberOfExpression memberOfExpression, S context) {
        memberOfExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(AnyComparisonExpression anyComparisonExpression, S context) {

        return null;
    }

    @Override
    public <S> T visit(Concat concat, S context) {
        visitBinaryExpression(concat, context);
        return null;
    }

    @Override
    public <S> T visit(Matches matches, S context) {
        visitBinaryExpression(matches, context);
        return null;
    }

    @Override
    public <S> T visit(BitwiseAnd bitwiseAnd, S context) {
        visitBinaryExpression(bitwiseAnd, context);
        return null;
    }

    @Override
    public <S> T visit(BitwiseOr bitwiseOr, S context) {
        visitBinaryExpression(bitwiseOr, context);
        return null;
    }

    @Override
    public <S> T visit(BitwiseXor bitwiseXor, S context) {
        visitBinaryExpression(bitwiseXor, context);
        return null;
    }

    @Override
    public <S> T visit(CastExpression castExpression, S context) {
        castExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(Modulo modulo, S context) {
        visitBinaryExpression(modulo, context);
        return null;
    }

    @Override
    public <S> T visit(AnalyticExpression analyticExpression, S context) {
        if (analyticExpression.getExpression() != null) {
            analyticExpression.getExpression().accept(this, context);
        }
        if (analyticExpression.getDefaultValue() != null) {
            analyticExpression.getDefaultValue().accept(this, context);
        }
        if (analyticExpression.getOffset() != null) {
            analyticExpression.getOffset().accept(this, context);
        }
        if (analyticExpression.getKeep() != null) {
            analyticExpression.getKeep().accept(this, context);
        }
        if (analyticExpression.getFuncOrderBy() != null) {
            for (OrderByElement element : analyticExpression.getOrderByElements()) {
                element.getExpression().accept(this, context);
            }
        }
        if (analyticExpression.getWindowElement() != null) {
            /*
             * Visit expressions from the range and offset of the window element. Do this using
             * optional chains, because several things down the tree can be null e.g. the
             * expression. So, null-safe versions of e.g.:
             * analyticExpression.getWindowElement().getOffset().getExpression().accept(this,
             * parameters);
             */
            Optional.ofNullable(analyticExpression.getWindowElement().getRange())
                    .map(WindowRange::getStart)
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, context));
            Optional.ofNullable(analyticExpression.getWindowElement().getRange())
                    .map(WindowRange::getEnd)
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, context));
            Optional.ofNullable(analyticExpression.getWindowElement().getOffset())
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, context));
        }
        return null;
    }

    @Override
    public <S> T visit(ExtractExpression extractExpression, S context) {
        extractExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(IntervalExpression intervalExpression, S context) {
        return null;
    }

    @Override
    public <S> T visit(OracleHierarchicalExpression hierarchicalExpression, S context) {
        hierarchicalExpression.getConnectExpression().accept(this, context);
        hierarchicalExpression.getStartExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(RegExpMatchOperator regExpMatchOperator, S context) {
        visitBinaryExpression(regExpMatchOperator, context);
        return null;
    }

    @Override
    public <S> T visit(ExpressionList<? extends Expression> expressionList, S context) {
        for (Expression expr : expressionList) {
            expr.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(RowConstructor<? extends Expression> rowConstructor, S context) {
        for (Expression expr : rowConstructor) {
            expr.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(NotExpression notExpr, S context) {
        notExpr.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(BitwiseRightShift bitwiseRightShift, S context) {
        visitBinaryExpression(bitwiseRightShift, context);
        return null;
    }

    @Override
    public <S> T visit(BitwiseLeftShift bitwiseLeftShift, S context) {
        visitBinaryExpression(bitwiseLeftShift, context);
        return null;
    }

    protected <S> T visitBinaryExpression(BinaryExpression binaryExpression, S context) {
        binaryExpression.getLeftExpression().accept(this, context);
        binaryExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(JsonExpression jsonExpr, S context) {
        jsonExpr.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(JsonOperator jsonOperator, S context) {
        visitBinaryExpression(jsonOperator, context);
        return null;
    }

    @Override
    public <S> T visit(UserVariable userVariable, S context) {

        return null;
    }

    @Override
    public <S> T visit(NumericBind numericBind, S context) {

        return null;
    }

    @Override
    public <S> T visit(KeepExpression keepExpression, S context) {
        for (OrderByElement element : keepExpression.getOrderByElements()) {
            element.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(MySQLGroupConcat groupConcat, S context) {
        for (Expression expr : groupConcat.getExpressionList()) {
            expr.accept(this, context);
        }
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                element.getExpression().accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(Pivot pivot, S context) {
        for (SelectItem<?> item : pivot.getFunctionItems()) {
            item.getExpression().accept(this, context);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this, context);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectItem<?> item : pivot.getSingleInItems()) {
                item.getExpression().accept(this, context);
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (SelectItem<ExpressionList<?>> item : pivot.getMultiInItems()) {
                item.getExpression().accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(PivotXml pivotXml, S context) {
        for (SelectItem<?> item : pivotXml.getFunctionItems()) {
            item.getExpression().accept(this, context);
        }
        for (Column col : pivotXml.getForColumns()) {
            col.accept(this, context);
        }
        if (pivotXml.getInSelect() != null && selectVisitor != null) {
            pivotXml.getInSelect().accept(selectVisitor, context);
        }
        return null;
    }

    @Override
    public <S> T visit(UnPivot unpivot, S context) {
        unpivot.accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(AllColumns allColumns, S context) {
        return null;
    }

    @Override
    public <S> T visit(AllTableColumns allTableColumns, S context) {
        return null;
    }

    @Override
    public <S> T visit(AllValue allValue, S context) {
        return null;
    }

    @Override
    public <S> T visit(IsDistinctExpression isDistinctExpression, S context) {
        visitBinaryExpression(isDistinctExpression, context);
        return null;
    }

    @Override
    public <S> T visit(SelectItem<? extends Expression> selectItem, S context) {
        selectItem.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(RowGetExpression rowGetExpression, S context) {
        rowGetExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(HexValue hexValue, S context) {

        return null;
    }

    @Override
    public <S> T visit(OracleHint hint, S context) {

        return null;
    }

    @Override
    public <S> T visit(TimeKeyExpression timeKeyExpression, S context) {

        return null;
    }

    @Override
    public <S> T visit(DateTimeLiteralExpression dateTimeLiteralExpression, S context) {
        return null;
    }

    @Override
    public <S> T visit(NextValExpression nextValExpression, S context) {
        return null;
    }

    @Override
    public <S> T visit(CollateExpression collateExpression, S context) {
        collateExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(SimilarToExpression similarToExpression, S context) {
        visitBinaryExpression(similarToExpression, context);
        return null;
    }

    @Override
    public <S> T visit(ArrayExpression arrayExpression, S context) {
        arrayExpression.getObjExpression().accept(this, context);
        if (arrayExpression.getIndexExpression() != null) {
            arrayExpression.getIndexExpression().accept(this, context);
        }
        if (arrayExpression.getStartIndexExpression() != null) {
            arrayExpression.getStartIndexExpression().accept(this, context);
        }
        if (arrayExpression.getStopIndexExpression() != null) {
            arrayExpression.getStopIndexExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(ArrayConstructor arrayConstructor, S context) {
        for (Expression expression : arrayConstructor.getExpressions()) {
            expression.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(VariableAssignment variableAssignment, S context) {
        variableAssignment.getVariable().accept(this, context);
        variableAssignment.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(XMLSerializeExpr xmlSerializeExpr, S context) {
        xmlSerializeExpr.getExpression().accept(this, context);
        for (OrderByElement elm : xmlSerializeExpr.getOrderByElements()) {
            elm.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(TimezoneExpression timezoneExpression, S context) {
        timezoneExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(JsonAggregateFunction jsonAggregateFunction, S context) {
        Expression expr = jsonAggregateFunction.getExpression();
        if (expr != null) {
            expr.accept(this, context);
        }

        expr = jsonAggregateFunction.getFilterExpression();
        if (expr != null) {
            expr.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(JsonFunction jsonFunction, S context) {
        for (JsonFunctionExpression expr : jsonFunction.getExpressions()) {
            expr.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(ConnectByRootOperator connectByRootOperator, S context) {
        connectByRootOperator.getColumn().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S context) {
        oracleNamedFunctionParameter.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(GeometryDistance geometryDistance, S context) {
        visitBinaryExpression(geometryDistance, context);
        return null;
    }

    @Override
    public <S> T visit(Select select, S context) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                for (WithItem item : select.getWithItemsList()) {
                    item.accept(selectVisitor, context);
                }
            }
            select.accept(selectVisitor, context);
        }
        return null;
    }

    @Override
    public <S> T visit(TranscodingFunction transcodingFunction, S context) {

        return null;
    }

    @Override
    public <S> T visit(TrimFunction trimFunction, S context) {

        return null;
    }

    @Override
    public <S> T visit(RangeExpression rangeExpression, S context) {
        rangeExpression.getStartExpression().accept(this, context);
        rangeExpression.getEndExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        visitBinaryExpression(tsqlLeftJoin, context);
        return null;
    }

    @Override
    public <S> T visit(TSQLRightJoin tsqlRightJoin, S context) {
        visitBinaryExpression(tsqlRightJoin, context);
        return null;
    }

    @Override
    public <S> T visit(StructType structType, S context) {
        // @todo: visit the ColType also
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                visit(selectItem, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(LambdaExpression lambdaExpression, S context) {
        lambdaExpression.getExpression().accept(this, context);
        return null;
    }

}
