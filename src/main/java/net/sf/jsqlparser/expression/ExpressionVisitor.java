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

import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

public interface ExpressionVisitor {

    void visit(BitwiseRightShift aThis);

    void visit(BitwiseLeftShift aThis);

    void visit(NullValue nullValue);

    void visit(Function function);

    void visit(SignedExpression signedExpression);

    void visit(JdbcParameter jdbcParameter);

    void visit(JdbcNamedParameter jdbcNamedParameter);

    void visit(DoubleValue doubleValue);

    void visit(LongValue longValue);

    void visit(HexValue hexValue);

    void visit(DateValue dateValue);

    void visit(TimeValue timeValue);

    void visit(TimestampValue timestampValue);

    void visit(Parenthesis parenthesis);

    void visit(StringValue stringValue);

    void visit(Addition addition);

    void visit(Division division);

    void visit(IntegerDivision division);

    void visit(Multiplication multiplication);

    void visit(Subtraction subtraction);

    void visit(AndExpression andExpression);

    void visit(OrExpression orExpression);

    void visit(XorExpression orExpression);

    void visit(Between between);

    void visit (OverlapsCondition overlapsCondition);

    void visit(EqualsTo equalsTo);

    void visit(GreaterThan greaterThan);

    void visit(GreaterThanEquals greaterThanEquals);

    void visit(InExpression inExpression);

    void visit(FullTextSearch fullTextSearch);

    void visit(IsNullExpression isNullExpression);

    void visit(IsBooleanExpression isBooleanExpression);

    void visit(LikeExpression likeExpression);

    void visit(MinorThan minorThan);

    void visit(MinorThanEquals minorThanEquals);

    void visit(NotEqualsTo notEqualsTo);

    void visit(Column tableColumn);

    void visit(SubSelect subSelect);

    void visit(CaseExpression caseExpression);

    void visit(WhenClause whenClause);

    void visit(ExistsExpression existsExpression);

    void visit(AnyComparisonExpression anyComparisonExpression);

    void visit(Concat concat);

    void visit(Matches matches);

    void visit(BitwiseAnd bitwiseAnd);

    void visit(BitwiseOr bitwiseOr);

    void visit(BitwiseXor bitwiseXor);

    void visit(CastExpression cast);

    void visit(TryCastExpression cast);

    void visit(Modulo modulo);

    void visit(AnalyticExpression aexpr);

    void visit(ExtractExpression eexpr);

    void visit(IntervalExpression iexpr);

    void visit(OracleHierarchicalExpression oexpr);

    void visit(RegExpMatchOperator rexpr);

    void visit(JsonExpression jsonExpr);

    void visit(JsonOperator jsonExpr);

    void visit(RegExpMySQLOperator regExpMySQLOperator);

    void visit(UserVariable var);

    void visit(NumericBind bind);

    void visit(KeepExpression aexpr);

    void visit(MySQLGroupConcat groupConcat);

    void visit(ValueListExpression valueList);

    void visit(RowTypeConstructor rowTypeConstructor);

    void visit(RowGetExpression rowGetExpression);

    void visit(OracleHint hint);

    void visit(TimeKeyExpression timeKeyExpression);

    void visit(DateTimeLiteralExpression literal);

    void visit(NotExpression aThis);

    void visit(NextValExpression aThis);

    void visit(CollateExpression aThis);

    void visit(SimilarToExpression aThis);

    void visit(ArrayExpression aThis);

    void visit(ArrayConstructor aThis);

    void visit(VariableAssignment aThis);

    void visit(XMLSerializeExpr aThis);

    void visit(TimezoneExpression aThis);

    void visit(JsonAggregateFunction aThis);

    void visit(JsonFunction aThis);

    void visit(ConnectByRootOperator aThis);

    void visit(OracleNamedFunctionParameter aThis);

    void visit(AllColumns allColumns);

    void visit(AllTableColumns allTableColumns);

    void visit(AllValue allValue);

    void visit(IsDistinctExpression isDistinctExpression);

    void visit(GeometryDistance geometryDistance);
}
