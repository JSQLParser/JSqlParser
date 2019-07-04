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
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public interface ExpressionVisitor {

    public void visit(BitwiseRightShift aThis);

    public void visit(BitwiseLeftShift aThis);

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

    void visit(Between between);

    void visit(EqualsTo equalsTo);

    void visit(GreaterThan greaterThan);

    void visit(GreaterThanEquals greaterThanEquals);

    void visit(InExpression inExpression);

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

    void visit(AllComparisonExpression allComparisonExpression);

    void visit(AnyComparisonExpression anyComparisonExpression);

    void visit(Concat concat);

    void visit(Matches matches);

    void visit(BitwiseAnd bitwiseAnd);

    void visit(BitwiseOr bitwiseOr);

    void visit(BitwiseXor bitwiseXor);

    void visit(CastExpression cast);

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

    void visit(RowConstructor rowConstructor);

    void visit(OracleHint hint);

    void visit(TimeKeyExpression timeKeyExpression);

    void visit(DateTimeLiteralExpression literal);

    public void visit(NotExpression aThis);

    public void visit(NextValExpression aThis);

    public void visit(CollateExpression aThis);

    public void visit(SimilarToExpression aThis);

}
