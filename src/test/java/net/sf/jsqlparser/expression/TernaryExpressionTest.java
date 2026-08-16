/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 * Tests for the ClickHouse-style ternary conditional operator {@code cond ? then : else}.
 */
class TernaryExpressionTest {

    @Test
    void testIssue2436() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT x > 0 ? 'y' : 'n' FROM t", true);

        Select select = (Select) CCJSqlParserUtil.parse("SELECT x > 0 ? 'y' : 'n' FROM t");
        TernaryExpression ternary = (TernaryExpression) ((PlainSelect) select).getSelectItem(0)
                .getExpression();
        Assertions.assertTrue(ternary.getCondition() instanceof ComparisonOperator);
        Assertions.assertTrue(ternary.getThenExpression() instanceof StringValue);
        Assertions.assertTrue(ternary.getElseExpression() instanceof StringValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // select items, where, order by, group by, having
            "SELECT a ? b : c FROM t",
            "SELECT * FROM t WHERE a ? b : c",
            "SELECT * FROM t WHERE a ? b : c ORDER BY x ? y : z",
            "SELECT id FROM t GROUP BY v ? 1 : 0 HAVING count(*) > 1",
            // function arguments, nested in parentheses and subqueries
            "SELECT abs(a ? b : c) FROM t",
            "SELECT (a ? b : c) FROM t",
            "SELECT * FROM (SELECT a ? b : c AS v FROM t) x",
            // dml statements
            "UPDATE t SET v = a ? b : c",
            "INSERT INTO t VALUES (a ? b : c)",
            "DELETE FROM t WHERE a ? b : c",
            // joins
            "SELECT * FROM t1 JOIN t2 ON t1.a ? t1.b : t2.c",
            // branches with arithmetic or boolean operators
            "SELECT x > 0 ? 1 + 2 : 3 * 4 FROM t",
            "SELECT a ? b OR c : d AND e FROM t",
            "SELECT a IS NULL ? 'x' : y FROM t",
            // jdbc parameters as branches
            "SELECT a ? ? : c FROM t",
            "SELECT a ? b + ? : c FROM t",
            "SELECT a ? ? : ? FROM t",
            "SELECT a ? b : ? FROM t",
            "SELECT * FROM t WHERE x = ? AND y ? z : w",
            // case expression inside a branch
            "SELECT a ? CASE WHEN b THEN c ELSE d END : e FROM t",
            // cast target type ends the then-branch (a bare ":" after a type
            // keyword is the ternary separator, not a named parameter)
            "SELECT a ? b :: int : c FROM t",
            "SELECT x > 0 ? CAST(b AS int) : c FROM t"
    })
    void testTernaryInVariousContexts(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPrecedenceBindsLooserThanBooleanOperators() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil
                .parse("SELECT * FROM t WHERE a OR b ? c : d");
        Expression where = ((PlainSelect) select).getWhere();
        Assertions.assertTrue(where instanceof TernaryExpression);
        Assertions.assertTrue(((TernaryExpression) where).getCondition() instanceof OrExpression);

        select = (Select) CCJSqlParserUtil.parse("SELECT * FROM t WHERE a AND b ? c : d");
        where = ((PlainSelect) select).getWhere();
        Assertions.assertTrue(where instanceof TernaryExpression);
        Assertions.assertTrue(((TernaryExpression) where).getCondition() instanceof AndExpression);

        select = (Select) CCJSqlParserUtil.parse("SELECT * FROM t WHERE a ? b : c OR d");
        where = ((PlainSelect) select).getWhere();
        Assertions.assertTrue(where instanceof TernaryExpression);
        Assertions.assertTrue(
                ((TernaryExpression) where).getElseExpression() instanceof OrExpression);
    }

    @Test
    void testRightAssociativeNesting() throws JSQLParserException {
        // a ? b : c ? d : e ==> a ? b : (c ? d : e)
        Select select = (Select) CCJSqlParserUtil.parse("SELECT * FROM t WHERE a ? b : c ? d : e");
        TernaryExpression ternary = (TernaryExpression) ((PlainSelect) select).getWhere();
        Assertions.assertTrue(ternary.getThenExpression() instanceof Column);
        Assertions.assertTrue(ternary.getElseExpression() instanceof TernaryExpression);

        // a ? b ? c : d : e ==> a ? (b ? c : d) : e
        select = (Select) CCJSqlParserUtil.parse("SELECT * FROM t WHERE a ? b ? c : d : e");
        ternary = (TernaryExpression) ((PlainSelect) select).getWhere();
        Assertions.assertTrue(ternary.getThenExpression() instanceof TernaryExpression);
        Assertions.assertTrue(ternary.getElseExpression() instanceof Column);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // PostgreSQL JSON operators must keep working
            "SELECT col ? 'key' FROM t",
            "SELECT col ?| 'key' FROM t",
            "SELECT col ?& 'key' FROM t",
            "SELECT * FROM t WHERE col ? 'key' AND x = 1",
            // JSON path operator must keep working
            "SELECT col -> 'key' FROM t",
            // JDBC parameters must keep working
            "SELECT * FROM t WHERE x = ?",
            "SELECT * FROM t WHERE x = ?5",
            "SELECT * FROM t WHERE x = ? AND y = ?",
            "SELECT * FROM t LIMIT ?",
            // array ranges and casts are unaffected
            "SELECT ARRAY[1:3]",
            "SELECT CAST(a AS CHAR)"
    })
    void testUnrelatedSyntaxUnaffected(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testJsonbOperatorWithNamedParameter() throws JSQLParserException {
        // regression guard (ternary support, #2466): a ":" in operand position
        // starts a JDBC named parameter and must not close a ternary then-branch
        Select select = (Select) CCJSqlParserUtil.parse("SELECT * FROM t WHERE j ? :key");
        Expression where = ((PlainSelect) select).getWhere();
        Assertions.assertTrue(where instanceof JsonOperator);
        Assertions.assertTrue(
                ((JsonOperator) where).getRightExpression() instanceof JdbcNamedParameter);
    }

    @Test
    void testTernaryWithCastThenBranch() throws JSQLParserException {
        // regression guard: a DATA_TYPE token can end the then-branch, so the
        // ":" after the cast target type must close the ternary
        Select select = (Select) CCJSqlParserUtil.parse("SELECT a ? b :: int : c FROM t");
        TernaryExpression ternary = (TernaryExpression) ((PlainSelect) select).getSelectItem(0)
                .getExpression();
        Assertions.assertTrue(ternary.getThenExpression() instanceof CastExpression);
    }

    @Test
    void testTernaryWithPositionalParameterThenBranch() throws JSQLParserException {
        // regression guard: a bare "?" positional parameter is a complete
        // expression, so the ":" after it closes the ternary instead of being
        // skipped in favor of the jsonb reading
        Select select = (Select) CCJSqlParserUtil.parse("SELECT a ? ? : c FROM t");
        TernaryExpression ternary = (TernaryExpression) ((PlainSelect) select).getSelectItem(0)
                .getExpression();
        Assertions.assertTrue(ternary.getThenExpression() instanceof JdbcParameter);
        Assertions.assertTrue(ternary.getElseExpression() instanceof Column);

        // a positional parameter as the LAST token of the then-branch, too
        select = (Select) CCJSqlParserUtil.parse("SELECT a ? b + ? : c FROM t");
        ternary = (TernaryExpression) ((PlainSelect) select).getSelectItem(0).getExpression();
        Assertions.assertTrue(ternary.getThenExpression() instanceof Addition);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // PostgreSQL jsonb operator combined with JDBC named parameters
            "SELECT * FROM t WHERE j ? :key",
            "SELECT * FROM t WHERE j ? 'k' AND x = :p",
            "SELECT * FROM t WHERE j ? 'k' OR x = :p",
            "SELECT * FROM t WHERE j ? :key AND x = 1",
            // named parameters keep working inside ternary branches, too
            "SELECT a ? :t : :e FROM t",
            "SELECT * FROM t WHERE c = :c AND a ? :t : :e"
    })
    void testTernaryAndJsonbWithJdbcNamedParameters(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
