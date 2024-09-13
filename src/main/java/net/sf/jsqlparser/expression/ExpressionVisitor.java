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
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

public interface ExpressionVisitor<T> {

    <S> T visit(BitwiseRightShift bitwiseRightShift, S context);

    default void visit(BitwiseRightShift bitwiseRightShift) {
        this.visit(bitwiseRightShift, null);
    }

    <S> T visit(BitwiseLeftShift bitwiseLeftShift, S context);

    default void visit(BitwiseLeftShift bitwiseLeftShift) {
        this.visit(bitwiseLeftShift, null);
    }

    <S> T visit(NullValue nullValue, S context);

    default void visit(NullValue nullValue) {
        this.visit(nullValue, null);
    }

    <S> T visit(Function function, S context);

    default void visit(Function function) {
        this.visit(function, null);
    }

    <S> T visit(SignedExpression signedExpression, S context);

    default void visit(SignedExpression signedExpression) {
        this.visit(signedExpression, null);
    }

    <S> T visit(JdbcParameter jdbcParameter, S context);

    default void visit(JdbcParameter jdbcParameter) {
        this.visit(jdbcParameter, null);
    }

    <S> T visit(JdbcNamedParameter jdbcNamedParameter, S context);

    default void visit(JdbcNamedParameter jdbcNamedParameter) {
        this.visit(jdbcNamedParameter, null);
    }

    <S> T visit(DoubleValue doubleValue, S context);

    default void visit(DoubleValue doubleValue) {
        this.visit(doubleValue, null);
    }

    <S> T visit(LongValue longValue, S context);

    default void visit(LongValue longValue) {
        this.visit(longValue, null);
    }

    <S> T visit(HexValue hexValue, S context);

    default void visit(HexValue hexValue) {
        this.visit(hexValue, null);
    }

    <S> T visit(DateValue dateValue, S context);

    default void visit(DateValue dateValue) {
        this.visit(dateValue, null);
    }

    <S> T visit(TimeValue timeValue, S context);

    default void visit(TimeValue timeValue) {
        this.visit(timeValue, null);
    }

    <S> T visit(TimestampValue timestampValue, S context);

    default void visit(TimestampValue timestampValue) {
        this.visit(timestampValue, null);
    }

    <S> T visit(StringValue stringValue, S context);

    default void visit(StringValue stringValue) {
        this.visit(stringValue, null);
    }

    <S> T visit(Addition addition, S context);

    default void visit(Addition addition) {
        this.visit(addition, null);
    }

    <S> T visit(Division division, S context);

    default void visit(Division division) {
        this.visit(division, null);
    }

    <S> T visit(IntegerDivision integerDivision, S context);

    default void visit(IntegerDivision integerDivision) {
        this.visit(integerDivision, null);
    }

    <S> T visit(Multiplication multiplication, S context);

    default void visit(Multiplication multiplication) {
        this.visit(multiplication, null);
    }

    <S> T visit(Subtraction subtraction, S context);

    default void visit(Subtraction subtraction) {
        this.visit(subtraction, null);
    }

    <S> T visit(AndExpression andExpression, S context);

    default void visit(AndExpression andExpression) {
        this.visit(andExpression, null);
    }

    <S> T visit(OrExpression orExpression, S context);

    default void visit(OrExpression orExpression) {
        this.visit(orExpression, null);
    }

    <S> T visit(XorExpression xorExpression, S context);

    default void visit(XorExpression xorExpression) {
        this.visit(xorExpression, null);
    }

    <S> T visit(Between between, S context);

    default void visit(Between between) {
        this.visit(between, null);
    }

    <S> T visit(OverlapsCondition overlapsCondition, S context);

    default void visit(OverlapsCondition overlapsCondition) {
        this.visit(overlapsCondition, null);
    }

    <S> T visit(EqualsTo equalsTo, S context);

    default void visit(EqualsTo equalsTo) {
        this.visit(equalsTo, null);
    }

    <S> T visit(GreaterThan greaterThan, S context);

    default void visit(GreaterThan greaterThan) {
        this.visit(greaterThan, null);
    }

    <S> T visit(GreaterThanEquals greaterThanEquals, S context);

    default void visit(GreaterThanEquals greaterThanEquals) {
        this.visit(greaterThanEquals, null);
    }

    <S> T visit(InExpression inExpression, S context);

    default void visit(InExpression inExpression) {
        this.visit(inExpression, null);
    }

    <S> T visit(IncludesExpression includesExpression, S context);

    default void visit(IncludesExpression includesExpression) {
        this.visit(includesExpression, null);
    }

    <S> T visit(ExcludesExpression excludesExpression, S context);

    default void visit(ExcludesExpression excludesExpression) {
        this.visit(excludesExpression, null);
    }

    <S> T visit(FullTextSearch fullTextSearch, S context);

    default void visit(FullTextSearch fullTextSearch) {
        this.visit(fullTextSearch, null);
    }

    <S> T visit(IsNullExpression isNullExpression, S context);

    default void visit(IsNullExpression isNullExpression) {
        this.visit(isNullExpression, null);
    }

    <S> T visit(IsBooleanExpression isBooleanExpression, S context);

    default void visit(IsBooleanExpression isBooleanExpression) {
        this.visit(isBooleanExpression, null);
    }

    <S> T visit(LikeExpression likeExpression, S context);

    default void visit(LikeExpression likeExpression) {
        this.visit(likeExpression, null);
    }

    <S> T visit(MinorThan minorThan, S context);

    default void visit(MinorThan minorThan) {
        this.visit(minorThan, null);
    }

    <S> T visit(MinorThanEquals minorThanEquals, S context);

    default void visit(MinorThanEquals minorThanEquals) {
        this.visit(minorThanEquals, null);
    }

    <S> T visit(NotEqualsTo notEqualsTo, S context);

    default void visit(NotEqualsTo notEqualsTo) {
        this.visit(notEqualsTo, null);
    }

    <S> T visit(DoubleAnd doubleAnd, S context);

    default void visit(DoubleAnd doubleAnd) {
        this.visit(doubleAnd, null);
    }

    <S> T visit(Contains contains, S context);

    default void visit(Contains contains) {
        this.visit(contains, null);
    }

    <S> T visit(ContainedBy containedBy, S context);

    default void visit(ContainedBy containedBy) {
        this.visit(containedBy, null);
    }

    <S> T visit(ParenthesedSelect select, S context);

    <S> T visit(Column column, S context);

    default void visit(Column column) {
        this.visit(column, null);
    }

    <S> T visit(CaseExpression caseExpression, S context);

    default void visit(CaseExpression caseExpression) {
        this.visit(caseExpression, null);
    }

    <S> T visit(WhenClause whenClause, S context);

    default void visit(WhenClause whenClause) {
        this.visit(whenClause, null);
    }

    <S> T visit(ExistsExpression existsExpression, S context);

    default void visit(ExistsExpression existsExpression) {
        this.visit(existsExpression, null);
    }

    <S> T visit(MemberOfExpression memberOfExpression, S context);

    default void visit(MemberOfExpression memberOfExpression) {
        this.visit(memberOfExpression, null);
    }

    <S> T visit(AnyComparisonExpression anyComparisonExpression, S context);

    default void visit(AnyComparisonExpression anyComparisonExpression) {
        this.visit(anyComparisonExpression, null);
    }

    <S> T visit(Concat concat, S context);

    default void visit(Concat concat) {
        this.visit(concat, null);
    }

    <S> T visit(Matches matches, S context);

    default void visit(Matches matches) {
        this.visit(matches, null);
    }

    <S> T visit(BitwiseAnd bitwiseAnd, S context);

    default void visit(BitwiseAnd bitwiseAnd) {
        this.visit(bitwiseAnd, null);
    }

    <S> T visit(BitwiseOr bitwiseOr, S context);

    default void visit(BitwiseOr bitwiseOr) {
        this.visit(bitwiseOr, null);
    }

    <S> T visit(BitwiseXor bitwiseXor, S context);

    default void visit(BitwiseXor bitwiseXor) {
        this.visit(bitwiseXor, null);
    }

    <S> T visit(CastExpression castExpression, S context);

    default void visit(CastExpression castExpression) {
        this.visit(castExpression, null);
    }

    <S> T visit(Modulo modulo, S context);

    default void visit(Modulo modulo) {
        this.visit(modulo, null);
    }

    <S> T visit(AnalyticExpression analyticExpression, S context);

    default void visit(AnalyticExpression analyticExpression) {
        this.visit(analyticExpression, null);
    }

    <S> T visit(ExtractExpression extractExpression, S context);

    default void visit(ExtractExpression extractExpression) {
        this.visit(extractExpression, null);
    }

    <S> T visit(IntervalExpression intervalExpression, S context);

    default void visit(IntervalExpression intervalExpression) {
        this.visit(intervalExpression, null);
    }

    <S> T visit(OracleHierarchicalExpression hierarchicalExpression, S context);

    default void visit(OracleHierarchicalExpression hierarchicalExpression) {
        this.visit(hierarchicalExpression, null);
    }

    <S> T visit(RegExpMatchOperator regExpMatchOperator, S context);

    default void visit(RegExpMatchOperator regExpMatchOperator) {
        this.visit(regExpMatchOperator, null);
    }

    <S> T visit(JsonExpression jsonExpression, S context);

    default void visit(JsonExpression jsonExpression) {
        this.visit(jsonExpression, null);
    }

    <S> T visit(JsonOperator jsonOperator, S context);

    default void visit(JsonOperator jsonOperator) {
        this.visit(jsonOperator, null);
    }

    <S> T visit(UserVariable userVariable, S context);

    default void visit(UserVariable userVariable) {
        this.visit(userVariable, null);
    }

    <S> T visit(NumericBind numericBind, S context);

    default void visit(NumericBind numericBind) {
        this.visit(numericBind, null);
    }

    <S> T visit(KeepExpression keepExpression, S context);

    default void visit(KeepExpression keepExpression) {
        this.visit(keepExpression, null);
    }

    <S> T visit(MySQLGroupConcat groupConcat, S context);

    default void visit(MySQLGroupConcat groupConcat) {
        this.visit(groupConcat, null);
    }

    <S> T visit(ExpressionList<? extends Expression> expressionList, S context);

    default void visit(ExpressionList<? extends Expression> expressionList) {
        this.visit(expressionList, null);
    }

    <S> T visit(RowConstructor<? extends Expression> rowConstructor, S context);

    default void visit(RowConstructor<? extends Expression> rowConstructor) {
        this.visit(rowConstructor, null);
    }

    <S> T visit(RowGetExpression rowGetExpression, S context);

    default void visit(RowGetExpression rowGetExpression) {
        this.visit(rowGetExpression, null);
    }

    <S> T visit(OracleHint hint, S context);

    default void visit(OracleHint hint) {
        this.visit(hint, null);
    }

    <S> T visit(TimeKeyExpression timeKeyExpression, S context);

    default void visit(TimeKeyExpression timeKeyExpression) {
        this.visit(timeKeyExpression, null);
    }

    <S> T visit(DateTimeLiteralExpression dateTimeLiteralExpression, S context);

    default void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {
        this.visit(dateTimeLiteralExpression, null);
    }

    <S> T visit(NotExpression notExpression, S context);

    default void visit(NotExpression notExpression) {
        this.visit(notExpression, null);
    }

    <S> T visit(NextValExpression nextValExpression, S context);

    default void visit(NextValExpression nextValExpression) {
        this.visit(nextValExpression, null);
    }

    <S> T visit(CollateExpression collateExpression, S context);

    default void visit(CollateExpression collateExpression) {
        this.visit(collateExpression, null);
    }

    <S> T visit(SimilarToExpression similarToExpression, S context);

    default void visit(SimilarToExpression similarToExpression) {
        this.visit(similarToExpression, null);
    }

    <S> T visit(ArrayExpression arrayExpression, S context);

    default void visit(ArrayExpression arrayExpression) {
        this.visit(arrayExpression, null);
    }

    <S> T visit(ArrayConstructor arrayConstructor, S context);

    default void visit(ArrayConstructor arrayConstructor) {
        this.visit(arrayConstructor, null);
    }

    <S> T visit(VariableAssignment variableAssignment, S context);

    default void visit(VariableAssignment variableAssignment) {
        this.visit(variableAssignment, null);
    }

    <S> T visit(XMLSerializeExpr xmlSerializeExpr, S context);

    default void visit(XMLSerializeExpr xmlSerializeExpr) {
        this.visit(xmlSerializeExpr, null);
    }

    <S> T visit(TimezoneExpression timezoneExpression, S context);

    default void visit(TimezoneExpression timezoneExpression) {
        this.visit(timezoneExpression, null);
    }

    <S> T visit(JsonAggregateFunction jsonAggregateFunction, S context);

    default void visit(JsonAggregateFunction jsonAggregateFunction) {
        this.visit(jsonAggregateFunction, null);
    }

    <S> T visit(JsonFunction jsonFunction, S context);

    default void visit(JsonFunction jsonFunction) {
        this.visit(jsonFunction, null);
    }

    <S> T visit(ConnectByRootOperator connectByRootOperator, S context);

    default void visit(ConnectByRootOperator connectByRootOperator) {
        this.visit(connectByRootOperator, null);
    }

    <S> T visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S context);

    default void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        this.visit(oracleNamedFunctionParameter, null);
    }

    <S> T visit(AllColumns allColumns, S context);

    default void visit(AllColumns allColumns) {
        this.visit(allColumns, null);
    }

    <S> T visit(AllTableColumns allTableColumns, S context);

    default void visit(AllTableColumns allTableColumns) {
        this.visit(allTableColumns, null);
    }

    <S> T visit(AllValue allValue, S context);

    default void visit(AllValue allValue) {
        this.visit(allValue, null);
    }

    <S> T visit(IsDistinctExpression isDistinctExpression, S context);

    default void visit(IsDistinctExpression isDistinctExpression) {
        this.visit(isDistinctExpression, null);
    }

    <S> T visit(GeometryDistance geometryDistance, S context);

    default void visit(GeometryDistance geometryDistance) {
        this.visit(geometryDistance, null);
    }

    <S> T visit(Select select, S context);

    <S> T visit(TranscodingFunction transcodingFunction, S context);

    default void visit(TranscodingFunction transcodingFunction) {
        this.visit(transcodingFunction, null);
    }

    <S> T visit(TrimFunction trimFunction, S context);

    default void visit(TrimFunction trimFunction) {
        this.visit(trimFunction, null);
    }

    <S> T visit(RangeExpression rangeExpression, S context);

    default void visit(RangeExpression rangeExpression) {
        this.visit(rangeExpression, null);
    }

    <S> T visit(TSQLLeftJoin tsqlLeftJoin, S context);

    default void visit(TSQLLeftJoin tsqlLeftJoin) {
        this.visit(tsqlLeftJoin, null);
    }

    <S> T visit(TSQLRightJoin tsqlRightJoin, S context);

    default void visit(TSQLRightJoin tsqlRightJoin) {
        this.visit(tsqlRightJoin, null);
    }

    <S> T visit(StructType structType, S context);

    default void visit(StructType structType) {
        this.visit(structType, null);
    }

    <S> T visit(LambdaExpression lambdaExpression, S context);

    default void visit(LambdaExpression lambdaExpression) {
        this.visit(lambdaExpression, null);
    }

    <S> T visit(HighExpression highExpression, S context);

    default void visit(HighExpression highExpression) {
        this.visit(highExpression, null);
    }

    <S> T visit(LowExpression lowExpression, S context);

    default void visit(LowExpression lowExpression) {
        this.visit(lowExpression, null);
    }

    <S> T visit(Plus plus, S context);

    default void visit(Plus plus) {
        this.visit(plus, null);
    }

    <S> T visit(PriorTo priorTo, S context);

    default void visit(PriorTo priorTo) {
        this.visit(priorTo, null);
    }

    <S> T visit(Inverse inverse, S context);

    default void visit(Inverse inverse) {
        this.visit(inverse, null);
    }
}
