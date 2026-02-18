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
import net.sf.jsqlparser.expression.operators.relational.CosineSimilarity;
import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExcludesExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IncludesExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.IsUnknownExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.Plus;
import net.sf.jsqlparser.expression.operators.relational.PriorTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FunctionAllColumns;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.UncommentedEmptyMethodBody"})
public class ExpressionVisitorAdapter<T>
        implements ExpressionVisitor<T>, PivotVisitor<T>, SelectItemVisitor<T> {

    private SelectVisitor<T> selectVisitor;

    public ExpressionVisitorAdapter(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    public ExpressionVisitorAdapter() {
        this.selectVisitor = null;
    }

    public SelectVisitor<T> getSelectVisitor() {
        return selectVisitor;
    }

    public ExpressionVisitorAdapter<T> setSelectVisitor(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
        return this;
    }

    @Override
    public <S> T visit(NullValue nullValue, S context) {
        return applyExpression(nullValue, context);
    }

    @Override
    public <S> T visit(Function function, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();
        if (function.getParameters() != null) {
            subExpressions.addAll(function.getParameters());
        }
        if (function.getKeep() != null) {
            subExpressions.add(function.getKeep());
        }
        if (function.getOrderByElements() != null) {
            for (OrderByElement orderByElement : function.getOrderByElements()) {
                subExpressions.add(orderByElement.getExpression());
            }
        }
        return visitExpressions(function, context, subExpressions);
    }

    @Override
    public <S> T visit(SignedExpression signedExpression, S context) {
        return signedExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(JdbcParameter jdbcParameter, S context) {
        return applyExpression(jdbcParameter, context);
    }

    @Override
    public <S> T visit(JdbcNamedParameter jdbcNamedParameter, S context) {
        return applyExpression(jdbcNamedParameter, context);
    }

    @Override
    public <S> T visit(DoubleValue doubleValue, S context) {
        return applyExpression(doubleValue, context);
    }

    @Override
    public <S> T visit(LongValue longValue, S context) {
        return applyExpression(longValue, context);
    }

    @Override
    public <S> T visit(DateValue dateValue, S context) {
        return applyExpression(dateValue, context);
    }

    @Override
    public <S> T visit(TimeValue timeValue, S context) {
        return applyExpression(timeValue, context);
    }

    @Override
    public <S> T visit(TimestampValue timestampValue, S context) {
        return applyExpression(timestampValue, context);
    }

    @Override
    public <S> T visit(StringValue stringValue, S context) {
        return applyExpression(stringValue, context);
    }

    @Override
    public <S> T visit(BooleanValue booleanValue, S context) {
        return applyExpression(booleanValue, context);
    }

    @Override
    public <S> T visit(Addition addition, S context) {
        return visitBinaryExpression(addition, context);
    }

    @Override
    public <S> T visit(Division division, S context) {
        return visitBinaryExpression(division, context);
    }

    @Override
    public <S> T visit(IntegerDivision integerDivision, S context) {
        return visitBinaryExpression(integerDivision, context);
    }

    @Override
    public <S> T visit(Multiplication multiplication, S context) {
        return visitBinaryExpression(multiplication, context);
    }

    @Override
    public <S> T visit(Subtraction subtraction, S context) {
        return visitBinaryExpression(subtraction, context);
    }

    @Override
    public <S> T visit(AndExpression andExpression, S context) {
        return visitBinaryExpression(andExpression, context);
    }

    @Override
    public <S> T visit(OrExpression orExpression, S context) {
        return visitBinaryExpression(orExpression, context);
    }

    @Override
    public <S> T visit(XorExpression xorExpression, S context) {
        return visitBinaryExpression(xorExpression, context);
    }

    @Override
    public <S> T visit(Between between, S context) {
        return visitExpressions(between, context, between.getLeftExpression(),
                between.getBetweenExpressionStart(), between.getBetweenExpressionEnd());
    }

    public <S> T visit(OverlapsCondition overlapsCondition, S context) {
        return visitExpressions(overlapsCondition, context, overlapsCondition.getLeft(),
                overlapsCondition.getRight());
    }


    @Override
    public <S> T visit(EqualsTo equalsTo, S context) {
        return visitBinaryExpression(equalsTo, context);
    }

    @Override
    public <S> T visit(GreaterThan greaterThan, S context) {
        return visitBinaryExpression(greaterThan, context);
    }

    @Override
    public <S> T visit(GreaterThanEquals greaterThanEquals, S context) {
        return visitBinaryExpression(greaterThanEquals, context);
    }

    @Override
    public <S> T visit(InExpression inExpression, S context) {
        return visitExpressions(inExpression, context, inExpression.getLeftExpression(),
                inExpression.getRightExpression());
    }

    @Override
    public <S> T visit(IncludesExpression includesExpression, S context) {
        return visitExpressions(includesExpression, context, includesExpression.getLeftExpression(),
                includesExpression.getRightExpression());
    }

    @Override
    public <S> T visit(ExcludesExpression excludesExpression, S context) {
        return visitExpressions(excludesExpression, context, excludesExpression.getLeftExpression(),
                excludesExpression.getRightExpression());
    }

    @Override
    public <S> T visit(IsNullExpression isNullExpression, S context) {
        return isNullExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(FullTextSearch fullTextSearch, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>(fullTextSearch.getMatchColumns());
        subExpressions.add(fullTextSearch.getAgainstValue());
        return visitExpressions(fullTextSearch, context, subExpressions);
    }

    @Override
    public <S> T visit(IsBooleanExpression isBooleanExpression, S context) {
        return isBooleanExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(IsUnknownExpression isUnknownExpression, S context) {
        return isUnknownExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(LikeExpression likeExpression, S context) {
        return visitBinaryExpression(likeExpression, context);
    }

    @Override
    public <S> T visit(MinorThan minorThan, S context) {
        return visitBinaryExpression(minorThan, context);
    }

    @Override
    public <S> T visit(MinorThanEquals minorThanEquals, S context) {
        return visitBinaryExpression(minorThanEquals, context);
    }

    @Override
    public <S> T visit(NotEqualsTo notEqualsTo, S context) {
        return visitBinaryExpression(notEqualsTo, context);
    }

    @Override
    public <S> T visit(DoubleAnd doubleAnd, S context) {
        return visitBinaryExpression(doubleAnd, context);
    }

    @Override
    public <S> T visit(Contains contains, S context) {
        return visitBinaryExpression(contains, context);
    }

    @Override
    public <S> T visit(ContainedBy containedBy, S context) {
        return visitBinaryExpression(containedBy, context);
    }

    @Override
    public <S> T visit(Column column, S context) {
        return applyExpression(column, context);
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
        ArrayList<Expression> subExpressions = new ArrayList<>();

        if (caseExpression.getSwitchExpression() != null) {
            subExpressions.add(caseExpression.getSwitchExpression());
        }
        subExpressions.addAll(caseExpression.getWhenClauses());
        if (caseExpression.getElseExpression() != null) {
            subExpressions.add(caseExpression.getElseExpression());
        }
        return visitExpressions(caseExpression, context, subExpressions);
    }

    @Override
    public <S> T visit(WhenClause whenClause, S context) {
        return visitExpressions(whenClause, context, whenClause.getWhenExpression(),
                whenClause.getThenExpression());
    }

    @Override
    public <S> T visit(ExistsExpression existsExpression, S context) {
        return existsExpression.getRightExpression().accept(this, context);
    }

    @Override
    public <S> T visit(MemberOfExpression memberOfExpression, S context) {
        return memberOfExpression.getRightExpression().accept(this, context);
    }

    @Override
    public <S> T visit(AnyComparisonExpression anyComparisonExpression, S context) {
        return applyExpression(anyComparisonExpression, context);
    }

    @Override
    public <S> T visit(Concat concat, S context) {
        return visitBinaryExpression(concat, context);
    }

    @Override
    public <S> T visit(Matches matches, S context) {
        return visitBinaryExpression(matches, context);
    }

    @Override
    public <S> T visit(BitwiseAnd bitwiseAnd, S context) {
        return visitBinaryExpression(bitwiseAnd, context);
    }

    @Override
    public <S> T visit(BitwiseOr bitwiseOr, S context) {
        return visitBinaryExpression(bitwiseOr, context);
    }

    @Override
    public <S> T visit(BitwiseXor bitwiseXor, S context) {
        return visitBinaryExpression(bitwiseXor, context);
    }

    @Override
    public <S> T visit(CastExpression castExpression, S context) {
        return castExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(Modulo modulo, S context) {
        return visitBinaryExpression(modulo, context);
    }

    @Override
    public <S> T visit(AnalyticExpression analyticExpression, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();

        if (analyticExpression.getExpression() != null) {
            subExpressions.add(analyticExpression.getExpression());
        }
        if (analyticExpression.getDefaultValue() != null) {
            subExpressions.add(analyticExpression.getDefaultValue());
        }
        if (analyticExpression.getOffset() != null) {
            subExpressions.add(analyticExpression.getOffset());
        }
        if (analyticExpression.getKeep() != null) {
            subExpressions.add(analyticExpression.getKeep());
        }
        if (analyticExpression.getFuncOrderBy() != null) {
            for (OrderByElement element : analyticExpression.getOrderByElements()) {
                subExpressions.add(element.getExpression());
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
                    .map(WindowOffset::getExpression).ifPresent(subExpressions::add);
            Optional.ofNullable(analyticExpression.getWindowElement().getRange())
                    .map(WindowRange::getEnd)
                    .map(WindowOffset::getExpression).ifPresent(subExpressions::add);
            Optional.ofNullable(analyticExpression.getWindowElement().getOffset())
                    .map(WindowOffset::getExpression).ifPresent(subExpressions::add);
        }
        return visitExpressions(analyticExpression, context, subExpressions);
    }

    @Override
    public <S> T visit(ExtractExpression extractExpression, S context) {
        return extractExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(IntervalExpression intervalExpression, S context) {
        if (intervalExpression.getExpression() != null) {
            intervalExpression.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(OracleHierarchicalExpression hierarchicalExpression, S context) {
        return visitExpressions(hierarchicalExpression, context,
                hierarchicalExpression.getConnectExpression(),
                hierarchicalExpression.getStartExpression());
    }

    @Override
    public <S> T visit(RegExpMatchOperator regExpMatchOperator, S context) {
        return visitBinaryExpression(regExpMatchOperator, context);
    }

    @Override
    public <S> T visit(ExpressionList<? extends Expression> expressionList, S context) {
        return visitExpressions(expressionList, context, (Collection<Expression>) expressionList);
    }

    @Override
    public <S> T visit(RowConstructor<? extends Expression> rowConstructor, S context) {
        return visitExpressions(rowConstructor, context, (Collection<Expression>) rowConstructor);
    }

    @Override
    public <S> T visit(NotExpression notExpr, S context) {
        return notExpr.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(BitwiseRightShift bitwiseRightShift, S context) {
        return visitBinaryExpression(bitwiseRightShift, context);
    }

    @Override
    public <S> T visit(BitwiseLeftShift bitwiseLeftShift, S context) {
        return visitBinaryExpression(bitwiseLeftShift, context);
    }

    protected <S> T applyExpression(Expression expression, S context) {
        return null;
    }

    protected <S> T visitExpressions(Expression expression, S context,
            ExpressionList<? extends Expression> subExpressions) {
        return visitExpressions(expression, context, (Collection<Expression>) subExpressions);
    }

    protected <S> T visitExpressions(Expression expression, S context,
            Collection<Expression> subExpressions) {
        for (Expression e : subExpressions) {
            if (e != null) {
                e.accept(this, context);
            }
        }
        return null;
    }

    protected <S> T visitExpressions(Expression expression, S context,
            Expression... subExpressions) {
        return visitExpressions(expression, context, Arrays.asList(subExpressions));
    }

    protected <S> T visitBinaryExpression(BinaryExpression binaryExpression, S context) {
        return visitExpressions(binaryExpression, context, binaryExpression.getLeftExpression(),
                binaryExpression.getRightExpression());
    }

    @Override
    public <S> T visit(JsonExpression jsonExpr, S context) {
        return jsonExpr.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(JsonOperator jsonOperator, S context) {
        return visitBinaryExpression(jsonOperator, context);
    }

    @Override
    public <S> T visit(UserVariable userVariable, S context) {
        return applyExpression(userVariable, context);
    }

    @Override
    public <S> T visit(NumericBind numericBind, S context) {
        return applyExpression(numericBind, context);
    }

    @Override
    public <S> T visit(KeepExpression keepExpression, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();
        for (OrderByElement element : keepExpression.getOrderByElements()) {
            subExpressions.add(element.getExpression());
        }
        return visitExpressions(keepExpression, context, subExpressions);
    }

    @Override
    public <S> T visit(MySQLGroupConcat groupConcat, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>(groupConcat.getExpressionList());
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                subExpressions.add(element.getExpression());
            }
        }
        return visitExpressions(groupConcat, context, subExpressions);
    }

    @Override
    public <S> T visit(Pivot pivot, S context) {
        for (SelectItem<?> item : pivot.getFunctionItems()) {
            item.accept(this, context);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this, context);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectItem<?> item : pivot.getSingleInItems()) {
                item.accept(this, context);
            }
        }
        if (pivot.getMultiInItems() != null) {
            for (SelectItem<ExpressionList<?>> item : pivot.getMultiInItems()) {
                item.accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(PivotXml pivotXml, S context) {
        for (SelectItem<?> item : pivotXml.getFunctionItems()) {
            item.accept(this, context);
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
        return unpivot.accept(this, context);
    }

    @Override
    public <S> T visit(AllColumns allColumns, S context) {
        return applyExpression(allColumns, context);
    }

    @Override
    public <S> T visit(AllTableColumns allTableColumns, S context) {
        return applyExpression(allTableColumns, context);
    }

    @Override
    public <S> T visit(FunctionAllColumns functionAllColumns, S context) {
        return applyExpression(functionAllColumns, context);
    }

    @Override
    public <S> T visit(AllValue allValue, S context) {
        return applyExpression(allValue, context);
    }

    @Override
    public <S> T visit(IsDistinctExpression isDistinctExpression, S context) {
        return visitBinaryExpression(isDistinctExpression, context);
    }

    @Override
    public <S> T visit(SelectItem<? extends Expression> selectItem, S context) {
        return selectItem.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(RowGetExpression rowGetExpression, S context) {
        return rowGetExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(HexValue hexValue, S context) {
        return applyExpression(hexValue, context);
    }

    @Override
    public <S> T visit(OracleHint hint, S context) {
        return applyExpression(hint, context);
    }

    @Override
    public <S> T visit(TimeKeyExpression timeKeyExpression, S context) {
        return applyExpression(timeKeyExpression, context);
    }

    @Override
    public <S> T visit(DateTimeLiteralExpression dateTimeLiteralExpression, S context) {
        return applyExpression(dateTimeLiteralExpression, context);
    }

    @Override
    public <S> T visit(NextValExpression nextValExpression, S context) {
        return applyExpression(nextValExpression, context);
    }

    @Override
    public <S> T visit(CollateExpression collateExpression, S context) {
        return collateExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(SimilarToExpression similarToExpression, S context) {
        return visitBinaryExpression(similarToExpression, context);
    }

    @Override
    public <S> T visit(ArrayExpression arrayExpression, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();

        subExpressions.add(arrayExpression.getObjExpression());
        if (arrayExpression.getIndexExpression() != null) {
            subExpressions.add(arrayExpression.getIndexExpression());
        }
        if (arrayExpression.getStartIndexExpression() != null) {
            subExpressions.add(arrayExpression.getStartIndexExpression());
        }
        if (arrayExpression.getStopIndexExpression() != null) {
            subExpressions.add(arrayExpression.getStopIndexExpression());
        }
        return visitExpressions(arrayExpression, context, subExpressions);
    }

    @Override
    public <S> T visit(ArrayConstructor arrayConstructor, S context) {
        return visitExpressions(arrayConstructor, context, arrayConstructor.getExpressions());
    }

    @Override
    public <S> T visit(VariableAssignment variableAssignment, S context) {
        return visitExpressions(variableAssignment, context, variableAssignment.getVariable(),
                variableAssignment.getExpression());
    }

    @Override
    public <S> T visit(XMLSerializeExpr xmlSerializeExpr, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();

        subExpressions.add(xmlSerializeExpr.getExpression());
        for (OrderByElement orderByElement : xmlSerializeExpr.getOrderByElements()) {
            subExpressions.add(orderByElement.getExpression());
        }
        return visitExpressions(xmlSerializeExpr, context, subExpressions);
    }

    @Override
    public <S> T visit(TimezoneExpression timezoneExpression, S context) {
        return timezoneExpression.getLeftExpression().accept(this, context);
    }

    @Override
    public <S> T visit(JsonAggregateFunction jsonAggregateFunction, S context) {
        return visitExpressions(jsonAggregateFunction, context,
                jsonAggregateFunction.getExpression(), jsonAggregateFunction.getFilterExpression());
    }

    @Override
    public <S> T visit(JsonFunction jsonFunction, S context) {
        ArrayList<Expression> subExpressions = new ArrayList<>();
        for (JsonKeyValuePair keyValuePair : jsonFunction.getKeyValuePairs()) {
            if (keyValuePair.getKey() instanceof Expression) {
                subExpressions.add((Expression) keyValuePair.getKey());
            }
            if (keyValuePair.getValue() instanceof Expression) {
                subExpressions.add((Expression) keyValuePair.getValue());
            }
        }
        for (JsonFunctionExpression expr : jsonFunction.getExpressions()) {
            subExpressions.add(expr.getExpression());
        }
        if (jsonFunction.getInputExpression() != null) {
            subExpressions.add(jsonFunction.getInputExpression().getExpression());
        }
        if (jsonFunction.getJsonPathExpression() != null) {
            subExpressions.add(jsonFunction.getJsonPathExpression());
        }
        subExpressions.addAll(jsonFunction.getPassingExpressions());
        if (jsonFunction.getOnEmptyBehavior() != null
                && jsonFunction.getOnEmptyBehavior().getExpression() != null) {
            subExpressions.add(jsonFunction.getOnEmptyBehavior().getExpression());
        }
        if (jsonFunction.getOnErrorBehavior() != null
                && jsonFunction.getOnErrorBehavior().getExpression() != null) {
            subExpressions.add(jsonFunction.getOnErrorBehavior().getExpression());
        }
        return visitExpressions(jsonFunction, context, subExpressions);
    }

    @Override
    public <S> T visit(JsonTableFunction jsonTableFunction, S context) {
        return visitExpressions(jsonTableFunction, context, jsonTableFunction.getAllExpressions());
    }

    @Override
    public <S> T visit(ConnectByRootOperator connectByRootOperator, S context) {
        return connectByRootOperator.getColumn().accept(this, context);
    }

    @Override
    public <S> T visit(ConnectByPriorOperator connectByPriorOperator, S context) {
        return connectByPriorOperator.getColumn().accept(this, context);
    }

    @Override
    public <S> T visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S context) {
        return oracleNamedFunctionParameter.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(PostgresNamedFunctionParameter postgresNamedFunctionParameter, S context) {
        return postgresNamedFunctionParameter.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(GeometryDistance geometryDistance, S context) {
        return visitBinaryExpression(geometryDistance, context);
    }

    @Override
    public <S> T visit(Select select, S context) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                for (WithItem<?> item : select.getWithItemsList()) {
                    item.accept(selectVisitor, context);
                }
            }
            select.accept(selectVisitor, context);
        }
        return null;
    }

    @Override
    public <S> T visit(TranscodingFunction transcodingFunction, S context) {
        return transcodingFunction.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(TrimFunction trimFunction, S context) {
        return trimFunction.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(RangeExpression rangeExpression, S context) {
        return visitExpressions(rangeExpression, context, rangeExpression.getStartExpression(),
                rangeExpression.getEndExpression());
    }

    @Override
    public <S> T visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        return visitBinaryExpression(tsqlLeftJoin, context);
    }

    @Override
    public <S> T visit(TSQLRightJoin tsqlRightJoin, S context) {
        return visitBinaryExpression(tsqlRightJoin, context);
    }

    @Override
    public <S> T visit(StructType structType, S context) {
        // @todo: visit the ColType also
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                selectItem.accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(LambdaExpression lambdaExpression, S context) {
        return lambdaExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(HighExpression highExpression, S context) {
        return highExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(LowExpression lowExpression, S context) {
        return lowExpression.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(Plus plus, S context) {
        return visitBinaryExpression(plus, context);
    }

    @Override
    public <S> T visit(PriorTo priorTo, S context) {
        return visitBinaryExpression(priorTo, context);
    }

    @Override
    public <S> T visit(Inverse inverse, S context) {
        return inverse.getExpression().accept(this, context);
    }

    @Override
    public <S> T visit(CosineSimilarity cosineSimilarity, S context) {
        cosineSimilarity.getLeftExpression().accept(this, context);
        cosineSimilarity.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(FromQuery fromQuery, S context) {
        return null;
    }

    @Override
    public <S> T visit(DateUnitExpression dateUnitExpression, S context) {
        return null;
    }

}
