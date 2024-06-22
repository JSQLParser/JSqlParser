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
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

public interface ExpressionVisitor<T> {

    <S> T visit(BitwiseRightShift bitwiseRightShift, S parameters);

    <S> T visit(BitwiseLeftShift bitwiseLeftShift, S parameters);

    <S> T visit(NullValue nullValue, S parameters);

    <S> T visit(Function function, S parameters);

    <S> T visit(SignedExpression signedExpression, S parameters);

    <S> T visit(JdbcParameter jdbcParameter, S parameters);

    <S> T visit(JdbcNamedParameter jdbcNamedParameter, S parameters);

    <S> T visit(DoubleValue doubleValue, S parameters);

    <S> T visit(LongValue longValue, S parameters);

    <S> T visit(HexValue hexValue, S parameters);

    <S> T visit(DateValue dateValue, S parameters);

    <S> T visit(TimeValue timeValue, S parameters);

    <S> T visit(TimestampValue timestampValue, S parameters);

    <S> T visit(StringValue stringValue, S parameters);

    <S> T visit(Addition addition, S parameters);

    <S> T visit(Division division, S parameters);

    <S> T visit(IntegerDivision integerDivision, S parameters);

    <S> T visit(Multiplication multiplication, S parameters);

    <S> T visit(Subtraction subtraction, S parameters);

    <S> T visit(AndExpression andExpression, S parameters);

    <S> T visit(OrExpression orExpression, S parameters);

    <S> T visit(XorExpression xorExpression, S parameters);

    <S> T visit(Between between, S parameters);

    <S> T visit(OverlapsCondition overlapsCondition, S parameters);

    <S> T visit(EqualsTo equalsTo, S parameters);

    <S> T visit(GreaterThan greaterThan, S parameters);

    <S> T visit(GreaterThanEquals greaterThanEquals, S parameters);

    <S> T visit(InExpression inExpression, S parameters);

    <S> T visit(IncludesExpression includesExpression, S parameters);

    <S> T visit(ExcludesExpression excludesExpression, S parameters);

    <S> T visit(FullTextSearch fullTextSearch, S parameters);

    <S> T visit(IsNullExpression isNullExpression, S parameters);

    <S> T visit(IsBooleanExpression isBooleanExpression, S parameters);

    <S> T visit(LikeExpression likeExpression, S parameters);

    <S> T visit(MinorThan minorThan, S parameters);

    <S> T visit(MinorThanEquals minorThanEquals, S parameters);

    <S> T visit(NotEqualsTo notEqualsTo, S parameters);

    <S> T visit(DoubleAnd doubleAnd, S parameters);

    <S> T visit(Contains contains, S parameters);

    <S> T visit(ContainedBy containedBy, S parameters);

    <S> T visit(ParenthesedSelect select, S parameters);

    <S> T visit(Column column, S parameters);

    <S> T visit(CaseExpression caseExpression, S parameters);

    <S> T visit(WhenClause whenClause, S parameters);

    <S> T visit(ExistsExpression existsExpression, S parameters);

    <S> T visit(MemberOfExpression memberOfExpression, S parameters);

    <S> T visit(AnyComparisonExpression anyComparisonExpression, S parameters);

    <S> T visit(Concat concat, S parameters);

    <S> T visit(Matches matches, S parameters);

    <S> T visit(BitwiseAnd bitwiseAnd, S parameters);

    <S> T visit(BitwiseOr bitwiseOr, S parameters);

    <S> T visit(BitwiseXor bitwiseXor, S parameters);

    <S> T visit(CastExpression castExpression, S parameters);

    <S> T visit(Modulo modulo, S parameters);

    <S> T visit(AnalyticExpression analyticExpression, S parameters);

    <S> T visit(ExtractExpression extractExpression, S parameters);

    <S> T visit(IntervalExpression intervalExpression, S parameters);

    <S> T visit(OracleHierarchicalExpression hierarchicalExpression, S parameters);

    <S> T visit(RegExpMatchOperator regExpMatchOperator, S parameters);

    <S> T visit(JsonExpression jsonExpression, S parameters);

    <S> T visit(JsonOperator jsonOperator, S parameters);

    <S> T visit(UserVariable userVariable, S parameters);

    <S> T visit(NumericBind numericBind, S parameters);

    <S> T visit(KeepExpression keepExpression, S parameters);

    <S> T visit(MySQLGroupConcat groupConcat, S parameters);

    <S> T visit(ExpressionList<? extends Expression> expressionList, S parameters);

    <S> T visit(RowConstructor<? extends Expression> rowConstructor, S parameters);

    <S> T visit(RowGetExpression rowGetExpression, S parameters);

    <S> T visit(OracleHint hint, S parameters);

    <S> T visit(TimeKeyExpression timeKeyExpression, S parameters);

    <S> T visit(DateTimeLiteralExpression dateTimeLiteralExpression, S parameters);

    <S> T visit(NotExpression notExpression, S parameters);

    <S> T visit(NextValExpression nextValExpression, S parameters);

    <S> T visit(CollateExpression collateExpression, S parameters);

    <S> T visit(SimilarToExpression similarToExpression, S parameters);

    <S> T visit(ArrayExpression arrayExpression, S parameters);

    <S> T visit(ArrayConstructor arrayConstructor, S parameters);

    <S> T visit(VariableAssignment variableAssignment, S parameters);

    <S> T visit(XMLSerializeExpr xmlSerializeExpr, S parameters);

    <S> T visit(TimezoneExpression timezoneExpression, S parameters);

    <S> T visit(JsonAggregateFunction jsonAggregateFunction, S parameters);

    <S> T visit(JsonFunction jsonFunction, S parameters);

    <S> T visit(ConnectByRootOperator connectByRootOperator, S parameters);

    <S> T visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S parameters);

    <S> T visit(AllColumns allColumns, S parameters);

    <S> T visit(AllTableColumns allTableColumns, S parameters);

    <S> T visit(AllValue allValue, S parameters);

    <S> T visit(IsDistinctExpression isDistinctExpression, S parameters);

    <S> T visit(GeometryDistance geometryDistance, S parameters);

    <S> T visit(Select select, S parameters);

    <S> T visit(TranscodingFunction transcodingFunction, S parameters);

    <S> T visit(TrimFunction trimFunction, S parameters);

    <S> T visit(RangeExpression rangeExpression, S parameters);

    <S> T visit(TSQLLeftJoin tsqlLeftJoin, S parameters);

    <S> T visit(TSQLRightJoin tsqlRightJoin, S parameters);

    <S> T visit(StructType structType, S parameters);

    <S> T visit(LambdaExpression lambdaExpression, S parameters);
}
