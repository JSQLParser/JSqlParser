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
    public <S> T visit(NullValue nullValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(Function function, S parameters) {
        if (function.getParameters() != null) {
            function.getParameters().accept(this, parameters);
        }
        if (function.getKeep() != null) {
            function.getKeep().accept(this, parameters);
        }
        if (function.getOrderByElements() != null) {
            for (OrderByElement orderByElement : function.getOrderByElements()) {
                orderByElement.getExpression().accept(this, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(SignedExpression signedExpression, S parameters) {
        signedExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(JdbcParameter jdbcParameter, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(JdbcNamedParameter jdbcNamedParameter, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(DoubleValue doubleValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(LongValue longValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(DateValue dateValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(TimeValue timeValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(TimestampValue timestampValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(StringValue stringValue, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(Addition addition, S parameters) {
        visitBinaryExpression(addition, parameters);
        return null;
    }

    @Override
    public <S> T visit(Division division, S parameters) {
        visitBinaryExpression(division, parameters);
        return null;
    }

    @Override
    public <S> T visit(IntegerDivision integerDivision, S parameters) {
        visitBinaryExpression(integerDivision, parameters);
        return null;
    }

    @Override
    public <S> T visit(Multiplication multiplication, S parameters) {
        visitBinaryExpression(multiplication, parameters);
        return null;
    }

    @Override
    public <S> T visit(Subtraction subtraction, S parameters) {
        visitBinaryExpression(subtraction, parameters);
        return null;
    }

    @Override
    public <S> T visit(AndExpression andExpression, S parameters) {
        visitBinaryExpression(andExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(OrExpression orExpression, S parameters) {
        visitBinaryExpression(orExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(XorExpression xorExpression, S parameters) {
        visitBinaryExpression(xorExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(Between between, S parameters) {
        between.getLeftExpression().accept(this, parameters);
        between.getBetweenExpressionStart().accept(this, parameters);
        between.getBetweenExpressionEnd().accept(this, parameters);
        return null;
    }

    public <S> T visit(OverlapsCondition overlapsCondition, S parameters) {
        overlapsCondition.getLeft().accept(this, parameters);
        overlapsCondition.getRight().accept(this, parameters);
        return null;
    }


    @Override
    public <S> T visit(EqualsTo equalsTo, S parameters) {
        visitBinaryExpression(equalsTo, parameters);
        return null;
    }

    @Override
    public <S> T visit(GreaterThan greaterThan, S parameters) {
        visitBinaryExpression(greaterThan, parameters);
        return null;
    }

    @Override
    public <S> T visit(GreaterThanEquals greaterThanEquals, S parameters) {
        visitBinaryExpression(greaterThanEquals, parameters);
        return null;
    }

    @Override
    public <S> T visit(InExpression inExpression, S parameters) {
        inExpression.getLeftExpression().accept(this, parameters);
        inExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(IncludesExpression includesExpression, S parameters) {
        includesExpression.getLeftExpression().accept(this, parameters);
        includesExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(ExcludesExpression excludesExpression, S parameters) {
        excludesExpression.getLeftExpression().accept(this, parameters);
        excludesExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(IsNullExpression isNullExpression, S parameters) {
        isNullExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(FullTextSearch fullTextSearch, S parameters) {
        for (Column col : fullTextSearch.getMatchColumns()) {
            col.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(IsBooleanExpression isBooleanExpression, S parameters) {
        isBooleanExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(LikeExpression likeExpression, S parameters) {
        visitBinaryExpression(likeExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(MinorThan minorThan, S parameters) {
        visitBinaryExpression(minorThan, parameters);
        return null;
    }

    @Override
    public <S> T visit(MinorThanEquals minorThanEquals, S parameters) {
        visitBinaryExpression(minorThanEquals, parameters);
        return null;
    }

    @Override
    public <S> T visit(NotEqualsTo notEqualsTo, S parameters) {
        visitBinaryExpression(notEqualsTo, parameters);
        return null;
    }

    @Override
    public <S> T visit(DoubleAnd doubleAnd, S parameters) {
        visitBinaryExpression(doubleAnd, parameters);
        return null;
    }

    @Override
    public <S> T visit(Contains contains, S parameters) {
        visitBinaryExpression(contains, parameters);
        return null;
    }

    @Override
    public <S> T visit(ContainedBy containedBy, S parameters) {
        visitBinaryExpression(containedBy, parameters);
        return null;
    }

    @Override
    public <S> T visit(Column column, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S parameters) {
        visit((Select) select, parameters);
        if (select.getPivot() != null) {
            select.getPivot().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(CaseExpression caseExpression, S parameters) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this, parameters);
        }
        for (Expression x : caseExpression.getWhenClauses()) {
            x.accept(this, parameters);
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(WhenClause whenClause, S parameters) {
        whenClause.getWhenExpression().accept(this, parameters);
        whenClause.getThenExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(ExistsExpression existsExpression, S parameters) {
        existsExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(MemberOfExpression memberOfExpression, S parameters) {
        memberOfExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(AnyComparisonExpression anyComparisonExpression, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(Concat concat, S parameters) {
        visitBinaryExpression(concat, parameters);
        return null;
    }

    @Override
    public <S> T visit(Matches matches, S parameters) {
        visitBinaryExpression(matches, parameters);
        return null;
    }

    @Override
    public <S> T visit(BitwiseAnd bitwiseAnd, S parameters) {
        visitBinaryExpression(bitwiseAnd, parameters);
        return null;
    }

    @Override
    public <S> T visit(BitwiseOr bitwiseOr, S parameters) {
        visitBinaryExpression(bitwiseOr, parameters);
        return null;
    }

    @Override
    public <S> T visit(BitwiseXor bitwiseXor, S parameters) {
        visitBinaryExpression(bitwiseXor, parameters);
        return null;
    }

    @Override
    public <S> T visit(CastExpression castExpression, S parameters) {
        castExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(Modulo modulo, S parameters) {
        visitBinaryExpression(modulo, parameters);
        return null;
    }

    @Override
    public <S> T visit(AnalyticExpression analyticExpression, S parameters) {
        if (analyticExpression.getExpression() != null) {
            analyticExpression.getExpression().accept(this, parameters);
        }
        if (analyticExpression.getDefaultValue() != null) {
            analyticExpression.getDefaultValue().accept(this, parameters);
        }
        if (analyticExpression.getOffset() != null) {
            analyticExpression.getOffset().accept(this, parameters);
        }
        if (analyticExpression.getKeep() != null) {
            analyticExpression.getKeep().accept(this, parameters);
        }
        if (analyticExpression.getFuncOrderBy() != null) {
            for (OrderByElement element : analyticExpression.getOrderByElements()) {
                element.getExpression().accept(this, parameters);
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
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, parameters));
            Optional.ofNullable(analyticExpression.getWindowElement().getRange())
                    .map(WindowRange::getEnd)
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, parameters));
            Optional.ofNullable(analyticExpression.getWindowElement().getOffset())
                    .map(WindowOffset::getExpression).ifPresent(e -> e.accept(this, parameters));
        }
        return null;
    }

    @Override
    public <S> T visit(ExtractExpression extractExpression, S parameters) {
        extractExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(IntervalExpression intervalExpression, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(OracleHierarchicalExpression hierarchicalExpression, S parameters) {
        hierarchicalExpression.getConnectExpression().accept(this, parameters);
        hierarchicalExpression.getStartExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(RegExpMatchOperator regExpMatchOperator, S parameters) {
        visitBinaryExpression(regExpMatchOperator, parameters);
        return null;
    }

    @Override
    public <S> T visit(ExpressionList<? extends Expression> expressionList, S parameters) {
        for (Expression expr : expressionList) {
            expr.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(RowConstructor<? extends Expression> rowConstructor, S parameters) {
        for (Expression expr : rowConstructor) {
            expr.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(NotExpression notExpr, S parameters) {
        notExpr.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(BitwiseRightShift bitwiseRightShift, S parameters) {
        visitBinaryExpression(bitwiseRightShift, parameters);
        return null;
    }

    @Override
    public <S> T visit(BitwiseLeftShift bitwiseLeftShift, S parameters) {
        visitBinaryExpression(bitwiseLeftShift, parameters);
        return null;
    }

    protected <S> T visitBinaryExpression(BinaryExpression binaryExpression, S parameters) {
        binaryExpression.getLeftExpression().accept(this, parameters);
        binaryExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(JsonExpression jsonExpr, S parameters) {
        jsonExpr.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(JsonOperator jsonOperator, S parameters) {
        visitBinaryExpression(jsonOperator, parameters);
        return null;
    }

    @Override
    public <S> T visit(UserVariable userVariable, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(NumericBind numericBind, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(KeepExpression keepExpression, S parameters) {
        for (OrderByElement element : keepExpression.getOrderByElements()) {
            element.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(MySQLGroupConcat groupConcat, S parameters) {
        for (Expression expr : groupConcat.getExpressionList()) {
            expr.accept(this, parameters);
        }
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                element.getExpression().accept(this, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(Pivot pivot, S parameters) {
        for (SelectItem<?> item : pivot.getFunctionItems()) {
            item.getExpression().accept(this, parameters);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this, parameters);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectItem<?> item : pivot.getSingleInItems()) {
                item.getExpression().accept(this, parameters);
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (SelectItem<ExpressionList<?>> item : pivot.getMultiInItems()) {
                item.getExpression().accept(this, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(PivotXml pivotXml, S parameters) {
        for (SelectItem<?> item : pivotXml.getFunctionItems()) {
            item.getExpression().accept(this, parameters);
        }
        for (Column col : pivotXml.getForColumns()) {
            col.accept(this, parameters);
        }
        if (pivotXml.getInSelect() != null && selectVisitor != null) {
            pivotXml.getInSelect().accept(selectVisitor, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(UnPivot unpivot, S parameters) {
        unpivot.accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(AllColumns allColumns, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(AllTableColumns allTableColumns, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(AllValue allValue, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(IsDistinctExpression isDistinctExpression, S parameters) {
        visitBinaryExpression(isDistinctExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(SelectItem<? extends Expression> selectItem, S parameters) {
        selectItem.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(RowGetExpression rowGetExpression, S parameters) {
        rowGetExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(HexValue hexValue, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(OracleHint hint, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(TimeKeyExpression timeKeyExpression, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(DateTimeLiteralExpression dateTimeLiteralExpression, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(NextValExpression nextValExpression, S parameters) {
        return null;
    }

    @Override
    public <S> T visit(CollateExpression collateExpression, S parameters) {
        collateExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(SimilarToExpression similarToExpression, S parameters) {
        visitBinaryExpression(similarToExpression, parameters);
        return null;
    }

    @Override
    public <S> T visit(ArrayExpression arrayExpression, S parameters) {
        arrayExpression.getObjExpression().accept(this, parameters);
        if (arrayExpression.getIndexExpression() != null) {
            arrayExpression.getIndexExpression().accept(this, parameters);
        }
        if (arrayExpression.getStartIndexExpression() != null) {
            arrayExpression.getStartIndexExpression().accept(this, parameters);
        }
        if (arrayExpression.getStopIndexExpression() != null) {
            arrayExpression.getStopIndexExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(ArrayConstructor arrayConstructor, S parameters) {
        for (Expression expression : arrayConstructor.getExpressions()) {
            expression.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(VariableAssignment variableAssignment, S parameters) {
        variableAssignment.getVariable().accept(this, parameters);
        variableAssignment.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(XMLSerializeExpr xmlSerializeExpr, S parameters) {
        xmlSerializeExpr.getExpression().accept(this, parameters);
        for (OrderByElement elm : xmlSerializeExpr.getOrderByElements()) {
            elm.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(TimezoneExpression timezoneExpression, S parameters) {
        timezoneExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(JsonAggregateFunction jsonAggregateFunction, S parameters) {
        Expression expr = jsonAggregateFunction.getExpression();
        if (expr != null) {
            expr.accept(this, parameters);
        }

        expr = jsonAggregateFunction.getFilterExpression();
        if (expr != null) {
            expr.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(JsonFunction jsonFunction, S parameters) {
        for (JsonFunctionExpression expr : jsonFunction.getExpressions()) {
            expr.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(ConnectByRootOperator connectByRootOperator, S parameters) {
        connectByRootOperator.getColumn().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S parameters) {
        oracleNamedFunctionParameter.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(GeometryDistance geometryDistance, S parameters) {
        visitBinaryExpression(geometryDistance, parameters);
        return null;
    }

    @Override
    public <S> T visit(Select select, S parameters) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                for (WithItem item : select.getWithItemsList()) {
                    item.accept(selectVisitor, parameters);
                }
            }
            select.accept(selectVisitor, parameters);
        }
        return null;
    }

    @Override
    public <S> T visit(TranscodingFunction transcodingFunction, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(TrimFunction trimFunction, S parameters) {

        return null;
    }

    @Override
    public <S> T visit(RangeExpression rangeExpression, S parameters) {
        rangeExpression.getStartExpression().accept(this, parameters);
        rangeExpression.getEndExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> T visit(TSQLLeftJoin tsqlLeftJoin, S parameters) {
        visitBinaryExpression(tsqlLeftJoin, parameters);
        return null;
    }

    @Override
    public <S> T visit(TSQLRightJoin tsqlRightJoin, S parameters) {
        visitBinaryExpression(tsqlRightJoin, parameters);
        return null;
    }

    @Override
    public <S> T visit(StructType structType, S parameters) {
        // @todo: visit the ColType also
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                visit(selectItem, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(LambdaExpression lambdaExpression, S parameters) {
        lambdaExpression.getExpression().accept(this, parameters);
        return null;
    }

}
