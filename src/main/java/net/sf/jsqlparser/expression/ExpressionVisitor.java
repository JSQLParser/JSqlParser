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
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public interface ExpressionVisitor {

    default void visit(BitwiseRightShift aThis) { // default implementation ignored
    }

    default void visit(BitwiseLeftShift aThis) { // default implementation ignored
    }

    default void visit(NullValue nullValue) { // default implementation ignored
    }

    default void visit(Function function) { // default implementation ignored
    }

    default void visit(SignedExpression signedExpression) { // default implementation ignored
    }

    default void visit(JdbcParameter jdbcParameter) { // default implementation ignored
    }

    default void visit(JdbcNamedParameter jdbcNamedParameter) { // default implementation ignored
    }

    default void visit(DoubleValue doubleValue) { // default implementation ignored
    }

    default void visit(LongValue longValue) { // default implementation ignored
    }

    default void visit(HexValue hexValue) { // default implementation ignored
    }

    default void visit(DateValue dateValue) { // default implementation ignored
    }

    default void visit(TimeValue timeValue) { // default implementation ignored
    }

    default void visit(TimestampValue timestampValue) { // default implementation ignored
    }

    default void visit(Parenthesis parenthesis) { // default implementation ignored
    }

    default void visit(StringValue stringValue) { // default implementation ignored
    }

    default void visit(Addition addition) { // default implementation ignored
    }

    default void visit(Division division) { // default implementation ignored
    }

    default void visit(IntegerDivision division) { // default implementation ignored
    }

    default void visit(Multiplication multiplication) { // default implementation ignored
    }

    default void visit(Subtraction subtraction) { // default implementation ignored
    }

    default void visit(AndExpression andExpression) { // default implementation ignored
    }

    default void visit(OrExpression orExpression) { // default implementation ignored
    }

    default void visit(Between between) { // default implementation ignored
    }

    default void visit(EqualsTo equalsTo) { // default implementation ignored
    }

    default void visit(GreaterThan greaterThan) { // default implementation ignored
    }

    default void visit(GreaterThanEquals greaterThanEquals) { // default implementation ignored
    }

    default void visit(InExpression inExpression) { // default implementation ignored
    }

    default void visit(FullTextSearch fullTextSearch) { // default implementation ignored
    }

    default void visit(IsNullExpression isNullExpression) { // default implementation ignored
    }

    default void visit(IsBooleanExpression isBooleanExpression) { // default implementation ignored
    }

    default void visit(LikeExpression likeExpression) { // default implementation ignored
    }

    default void visit(MinorThan minorThan) { // default implementation ignored
    }

    default void visit(MinorThanEquals minorThanEquals) { // default implementation ignored
    }

    default void visit(NotEqualsTo notEqualsTo) { // default implementation ignored
    }

    default void visit(Column tableColumn) { // default implementation ignored
    }

    default void visit(SubSelect subSelect) { // default implementation ignored
    }

    default void visit(CaseExpression caseExpression) { // default implementation ignored
    }

    default void visit(WhenClause whenClause) { // default implementation ignored
    }

    default void visit(ExistsExpression existsExpression) { // default implementation ignored
    }

    default void visit(AllComparisonExpression allComparisonExpression) { // default implementation ignored
    }

    default void visit(AnyComparisonExpression anyComparisonExpression) { // default implementation ignored
    }

    default void visit(Concat concat) { // default implementation ignored
    }

    default void visit(Matches matches) { // default implementation ignored
    }

    default void visit(BitwiseAnd bitwiseAnd) { // default implementation ignored
    }

    default void visit(BitwiseOr bitwiseOr) { // default implementation ignored
    }

    default void visit(BitwiseXor bitwiseXor) { // default implementation ignored
    }

    default void visit(CastExpression cast) { // default implementation ignored
    }

    default void visit(Modulo modulo) { // default implementation ignored
    }

    default void visit(AnalyticExpression aexpr) { // default implementation ignored
    }

    default void visit(ExtractExpression eexpr) { // default implementation ignored
    }

    default void visit(IntervalExpression iexpr) { // default implementation ignored
    }

    default void visit(OracleHierarchicalExpression oexpr) { // default implementation ignored
    }

    default void visit(RegExpMatchOperator rexpr) { // default implementation ignored
    }

    default void visit(JsonExpression jsonExpr) { // default implementation ignored
    }

    default void visit(JsonOperator jsonExpr) { // default implementation ignored
    }

    default void visit(RegExpMySQLOperator regExpMySQLOperator) { // default implementation ignored
    }

    default void visit(UserVariable var) { // default implementation ignored
    }

    default void visit(NumericBind bind) { // default implementation ignored
    }

    default void visit(KeepExpression aexpr) { // default implementation ignored
    }

    default void visit(MySQLGroupConcat groupConcat) { // default implementation ignored
    }

    default void visit(ValueListExpression valueList) { // default implementation ignored
    }

    default void visit(RowConstructor rowConstructor) { // default implementation ignored
    }

    default void visit(OracleHint hint) { // default implementation ignored
    }

    default void visit(TimeKeyExpression timeKeyExpression) { // default implementation ignored
    }

    default void visit(DateTimeLiteralExpression literal) { // default implementation ignored
    }

    default void visit(NotExpression aThis) { // default implementation ignored
    }

    default void visit(NextValExpression aThis) { // default implementation ignored
    }

    default void visit(CollateExpression aThis) { // default implementation ignored
    }

    default void visit(SimilarToExpression aThis) { // default implementation ignored
    }

}
