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

    T visit(BitwiseRightShift aThis);

    T visit(BitwiseLeftShift aThis);

    T visit(NullValue nullValue);

    T visit(Function function);

    T visit(SignedExpression signedExpression);

    T visit(JdbcParameter jdbcParameter);

    T visit(JdbcNamedParameter jdbcNamedParameter);

    T visit(DoubleValue doubleValue);

    T visit(LongValue longValue);

    T visit(HexValue hexValue);

    T visit(DateValue dateValue);

    T visit(TimeValue timeValue);

    T visit(TimestampValue timestampValue);

    T visit(StringValue stringValue);

    T visit(Addition addition);

    T visit(Division division);

    T visit(IntegerDivision division);

    T visit(Multiplication multiplication);

    T visit(Subtraction subtraction);

    T visit(AndExpression andExpression);

    T visit(OrExpression orExpression);

    T visit(XorExpression orExpression);

    T visit(Between between);

    T visit(OverlapsCondition overlapsCondition);

    T visit(EqualsTo equalsTo);

    T visit(GreaterThan greaterThan);

    T visit(GreaterThanEquals greaterThanEquals);

    T visit(InExpression inExpression);

    T visit(IncludesExpression includesExpression);

    T visit(ExcludesExpression excludesExpression);

    T visit(FullTextSearch fullTextSearch);

    T visit(IsNullExpression isNullExpression);

    T visit(IsBooleanExpression isBooleanExpression);

    T visit(LikeExpression likeExpression);

    T visit(MinorThan minorThan);

    T visit(MinorThanEquals minorThanEquals);

    T visit(NotEqualsTo notEqualsTo);

    T visit(DoubleAnd doubleAnd);

    T visit(Contains contains);

    T visit(ContainedBy containedBy);

    T visit(ParenthesedSelect selectBody);

    T visit(Column tableColumn);

    T visit(CaseExpression caseExpression);

    T visit(WhenClause whenClause);

    T visit(ExistsExpression existsExpression);

    T visit(MemberOfExpression memberOfExpression);

    T visit(AnyComparisonExpression anyComparisonExpression);

    T visit(Concat concat);

    T visit(Matches matches);

    T visit(BitwiseAnd bitwiseAnd);

    T visit(BitwiseOr bitwiseOr);

    T visit(BitwiseXor bitwiseXor);

    T visit(CastExpression cast);

    T visit(Modulo modulo);

    T visit(AnalyticExpression aexpr);

    T visit(ExtractExpression eexpr);

    T visit(IntervalExpression iexpr);

    T visit(OracleHierarchicalExpression oexpr);

    T visit(RegExpMatchOperator rexpr);

    T visit(JsonExpression jsonExpr);

    T visit(JsonOperator jsonExpr);

    T visit(UserVariable var);

    T visit(NumericBind bind);

    T visit(KeepExpression aexpr);

    T visit(MySQLGroupConcat groupConcat);

    T visit(ExpressionList<?> expressionList);

    T visit(RowConstructor<?> rowConstructor);

    T visit(RowGetExpression rowGetExpression);

    T visit(OracleHint hint);

    T visit(TimeKeyExpression timeKeyExpression);

    T visit(DateTimeLiteralExpression literal);

    T visit(NotExpression aThis);

    T visit(NextValExpression aThis);

    T visit(CollateExpression aThis);

    T visit(SimilarToExpression aThis);

    T visit(ArrayExpression aThis);

    T visit(ArrayConstructor aThis);

    T visit(VariableAssignment aThis);

    T visit(XMLSerializeExpr aThis);

    T visit(TimezoneExpression aThis);

    T visit(JsonAggregateFunction aThis);

    T visit(JsonFunction aThis);

    T visit(ConnectByRootOperator aThis);

    T visit(OracleNamedFunctionParameter aThis);

    T visit(AllColumns allColumns);

    T visit(AllTableColumns allTableColumns);

    T visit(AllValue allValue);

    T visit(IsDistinctExpression isDistinctExpression);

    T visit(GeometryDistance geometryDistance);

    T visit(Select selectBody);

    T visit(TranscodingFunction transcodingFunction);

    T visit(TrimFunction trimFunction);

    T visit(RangeExpression rangeExpression);

    T visit(TSQLLeftJoin tsqlLeftJoin);

    T visit(TSQLRightJoin tsqlRightJoin);

    T visit(StructType structType);

    T visit(LambdaExpression lambdaExpression);
}
