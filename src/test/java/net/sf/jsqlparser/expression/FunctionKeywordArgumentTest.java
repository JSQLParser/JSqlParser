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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the generic keyword-argument support inside {@link Function} and the removal of the
 * dedicated {@code MySQLGroupConcat} production.
 * <p>
 * The {@code (KEYWORD expr)*} tail in InternalFunction generically captures dialect-specific
 * keyword-expression pairs like {@code SEPARATOR ','} or {@code USING utf8} without requiring a
 * dedicated grammar branch per keyword.
 * <p>
 * GROUP_CONCAT is no longer a special production - it routes through InternalFunction like any
 * other function, with SEPARATOR handled as a keyword argument.
 */
class FunctionKeywordArgumentTest {

    // ====================================================================
    // Roundtrip parse tests - parameterised
    // ====================================================================

    static Stream<Arguments> roundtripSqlProvider() {
        return Stream.of(

                // -- GROUP_CONCAT: basic SEPARATOR (was MySQLGroupConcat) ----
                //
                // These previously required the dedicated MySQLGroupConcat
                // production. Now handled by InternalFunction + keyword args.

                Arguments.of(
                        "GROUP_CONCAT with SEPARATOR string literal",
                        "SELECT GROUP_CONCAT(col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "GROUP_CONCAT with DISTINCT and SEPARATOR",
                        "SELECT GROUP_CONCAT(DISTINCT col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "GROUP_CONCAT with ORDER BY and SEPARATOR",
                        "SELECT GROUP_CONCAT(col ORDER BY col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "GROUP_CONCAT with DISTINCT, ORDER BY and SEPARATOR",
                        "SELECT GROUP_CONCAT(DISTINCT col ORDER BY col ASC SEPARATOR ';') FROM t"),

                Arguments.of(
                        "GROUP_CONCAT multiple expressions with SEPARATOR",
                        "SELECT GROUP_CONCAT(a, b SEPARATOR ',') FROM t"),

                // -- Original bug: SEPARATOR with expression, not just literal

                Arguments.of(
                        "GROUP_CONCAT SEPARATOR CHR(10) - the original bug",
                        "SELECT GROUP_CONCAT(description SEPARATOR CHR(10)) FROM t"),

                Arguments.of(
                        "GROUP_CONCAT SEPARATOR CONCAT expression",
                        "SELECT GROUP_CONCAT(col SEPARATOR CONCAT(',', ' ')) FROM t"),

                Arguments.of(
                        "GROUP_CONCAT SEPARATOR with hex literal",
                        "SELECT GROUP_CONCAT(col SEPARATOR 0x0A) FROM t"),

                Arguments.of(
                        "GROUP_CONCAT SEPARATOR with empty string",
                        "SELECT GROUP_CONCAT(col SEPARATOR '') FROM t"),

                Arguments.of(
                        "GROUP_CONCAT SEPARATOR with column reference",
                        "SELECT GROUP_CONCAT(col SEPARATOR sep_col) FROM t"),

                // -- GitHub Issue #688: CONVERT(expr USING charset) ----------
                // https://github.com/JSQLParser/JSqlParser/issues/688
                // "select * from a order by convert(a.name using gbk) desc"
                // Failed: ParseException at "("

                Arguments.of(
                        "Issue #688: CONVERT with USING charset",
                        "SELECT CONVERT(a.name USING gbk) FROM t"),

                Arguments.of(
                        "Issue #688: CONVERT USING in ORDER BY",
                        "SELECT * FROM a ORDER BY CONVERT(a.name USING gbk) DESC"),

                Arguments.of(
                        "Issue #688: CONVERT USING utf8mb4",
                        "SELECT CONVERT(col USING utf8mb4) FROM t"),

                // -- GitHub Issue #1257: CONVERT(name USING GBK) -------------
                // https://github.com/JSQLParser/JSqlParser/issues/1257
                // Same root cause as #688, different reporter.

                Arguments.of(
                        "Issue #1257: CONVERT USING GBK with WHERE clause",
                        "SELECT id, name FROM tbl_template WHERE name LIKE ? ORDER BY CONVERT(name USING GBK) ASC"),

                // -- Generic SEPARATOR on non-GROUP_CONCAT functions ---------

                Arguments.of(
                        "SEPARATOR with string literal on generic function",
                        "SELECT list_agg(col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "SEPARATOR with CHR() on generic function",
                        "SELECT list_agg(col SEPARATOR CHR(10)) FROM t"),

                Arguments.of(
                        "ORDER BY then SEPARATOR on generic function",
                        "SELECT my_agg(col ORDER BY col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "ORDER BY DESC then SEPARATOR with function expr",
                        "SELECT my_agg(col ORDER BY col DESC SEPARATOR CHR(10)) FROM t"),

                Arguments.of(
                        "ORDER BY multiple columns then SEPARATOR",
                        "SELECT my_agg(col ORDER BY a ASC, b DESC SEPARATOR '|') FROM t"),

                // -- DISTINCT / UNIQUE + SEPARATOR ---------------------------

                Arguments.of(
                        "DISTINCT with SEPARATOR",
                        "SELECT my_agg(DISTINCT col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "UNIQUE with SEPARATOR",
                        "SELECT my_agg(UNIQUE col SEPARATOR ';') FROM t"),

                Arguments.of(
                        "DISTINCT + ORDER BY + SEPARATOR",
                        "SELECT my_agg(DISTINCT col ORDER BY col SEPARATOR ',') FROM t"),

                // -- Multiple expression-list args + keyword arg -------------

                Arguments.of(
                        "Two args then SEPARATOR",
                        "SELECT my_agg(col, ',' SEPARATOR CHR(10)) FROM t"),

                Arguments.of(
                        "Three args then DELIMITER",
                        "SELECT custom_agg(a, b, c DELIMITER '|') FROM t"),

                // -- USING on other functions --------------------------------

                Arguments.of(
                        "USING with identifier",
                        "SELECT transcode(expr USING utf8mb4) FROM t"),

                Arguments.of(
                        "USING with quoted identifier",
                        "SELECT transcode('hello' USING utf8) FROM t"),

                Arguments.of(
                        "TRANSLATE with USING",
                        "SELECT translate_func(col USING unicode_to_latin) FROM t"),

                // -- FORMAT keyword (SQL Server, Snowflake, BigQuery) --------

                Arguments.of(
                        "FORMAT with string literal",
                        "SELECT to_json(col FORMAT 'json') FROM t"),

                Arguments.of(
                        "FORMAT with identifier",
                        "SELECT fmt_func(col FORMAT json) FROM t"),

                // -- ENCODING keyword ----------------------------------------

                Arguments.of(
                        "ENCODING with string literal",
                        "SELECT encode_func(col ENCODING 'UTF-8') FROM t"),

                // -- DELIMITER keyword (Redshift, Vertica) -------------------

                Arguments.of(
                        "DELIMITER with pipe",
                        "SELECT str_agg(col DELIMITER '|') FROM t"),

                Arguments.of(
                        "DELIMITER with CHR",
                        "SELECT str_agg(col DELIMITER CHR(9)) FROM t"),

                Arguments.of(
                        "ORDER BY then DELIMITER",
                        "SELECT str_agg(col ORDER BY col DELIMITER '|') FROM t"),

                // -- Multiple keyword arguments ------------------------------

                Arguments.of(
                        "Two keyword args: SEPARATOR + ENCODING",
                        "SELECT custom_func(col SEPARATOR ',' ENCODING 'utf8') FROM t"),

                Arguments.of(
                        "Three keyword args",
                        "SELECT custom_func(col FORMAT 'json' ENCODING 'utf8' MODE 'strict') FROM t"),

                // -- Complex separator expressions ---------------------------

                Arguments.of(
                        "SEPARATOR with nested function call",
                        "SELECT agg_func(col SEPARATOR REPLACE(CHR(10), CHR(13), '')) FROM t"),

                Arguments.of(
                        "SEPARATOR with CASE expression",
                        "SELECT agg_func(col SEPARATOR CASE WHEN x = 1 THEN ',' ELSE ';' END) FROM t"),

                Arguments.of(
                        "SEPARATOR with arithmetic expression",
                        "SELECT agg_func(col SEPARATOR 1 + 2) FROM t"),

                // -- Schema-qualified function names -------------------------

                Arguments.of(
                        "Schema-qualified function with SEPARATOR",
                        "SELECT myschema.agg_func(col SEPARATOR ',') FROM t"),

                Arguments.of(
                        "Two-level schema with SEPARATOR",
                        "SELECT cat.myschema.agg_func(col SEPARATOR ',') FROM t"),

                // -- Integration with other InternalFunction clauses ---------

                Arguments.of(
                        "ALL + ORDER BY + SEPARATOR",
                        "SELECT my_agg(ALL col ORDER BY col SEPARATOR ',') FROM t"),

                // -- Multi-value keyword arguments (USING col1, col2, ...) ---
                // Oracle Data Mining functions use USING followed by a
                // comma-separated column list.

                Arguments.of(
                        "Oracle PREDICTION with USING column list",
                        "SELECT PREDICTION(dt_sh_clas_sample USING cust_marital_status, education, household_size) FROM t"),

                Arguments.of(
                        "Oracle PREDICTION in WHERE clause",
                        "SELECT cust_gender, COUNT(*) AS cnt FROM mining_data_apply_v WHERE PREDICTION(dt_sh_clas_sample USING cust_marital_status, education, household_size) = 1 GROUP BY cust_gender ORDER BY cust_gender"),

                Arguments.of(
                        "Oracle PREDICTION_PROBABILITY with USING",
                        "SELECT PREDICTION_PROBABILITY(my_model USING col1, col2, col3) FROM t"),

                Arguments.of(
                        "Oracle CLUSTER_ID with USING",
                        "SELECT CLUSTER_ID(my_model USING col1, col2) FROM t"),

                Arguments.of(
                        "USING with single column",
                        "SELECT my_func(model USING col1) FROM t"),

                Arguments.of(
                        "USING with many columns",
                        "SELECT my_func(model USING a, b, c, d, e) FROM t"),

                // -- Keyword arg in different SQL contexts -------------------

                Arguments.of(
                        "Keyword arg function in WHERE",
                        "SELECT * FROM t WHERE my_agg(col SEPARATOR ',') = 'a,b,c'"),

                Arguments.of(
                        "Keyword arg function in SELECT + alias",
                        "SELECT my_agg(col SEPARATOR ',') AS concatenated FROM t"),

                // -- Edge cases ----------------------------------------------

                Arguments.of(
                        "SEPARATOR with parenthesised expression",
                        "SELECT agg_func(col SEPARATOR (CHR(10))) FROM t"),

                Arguments.of(
                        "Keyword arg in function with chained call",
                        "SELECT quantile_agg(col SEPARATOR ',')(cost) FROM t"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundtripSqlProvider")
    void testRoundtrip(String label, String sql) throws JSQLParserException {
        // First parse
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt, "Parse returned null for: " + sql);

        // Deparse
        String deparsed = stmt.toString();
        assertNotNull(deparsed, "toString returned null for: " + sql);

        // Second parse of deparsed output
        Statement stmt2 = CCJSqlParserUtil.parse(deparsed);
        assertNotNull(stmt2, "Re-parse returned null for deparsed: " + deparsed);

        // Structural equivalence
        assertEquals(deparsed, stmt2.toString(),
                "Roundtrip mismatch for [" + label + "]:\n"
                        + "  original:  " + sql + "\n"
                        + "  deparsed:  " + deparsed + "\n"
                        + "  reparsed:  " + stmt2);
    }

    // ====================================================================
    // GitHub Issue #688 / #1257 - CONVERT(expr USING charset)
    // These were ParseExceptions before the generic keyword-arg tail.
    // ====================================================================

    @Test
    void testIssue688_ConvertUsingGbk() throws JSQLParserException {
        // Exact SQL from issue #688 — was a ParseException before
        String sql = "SELECT * FROM a ORDER BY CONVERT(a.name USING gbk) DESC";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt);
        // Roundtrip
        String deparsed = stmt.toString();
        assertEquals(deparsed, CCJSqlParserUtil.parse(deparsed).toString());
    }

    @Test
    void testIssue1257_ConvertUsingGBK() throws JSQLParserException {
        // Exact SQL from issue #1257
        String sql =
                "SELECT id, name FROM tbl_template WHERE name LIKE ? ORDER BY CONVERT(name USING GBK) ASC";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt);
    }

    // ====================================================================
    // GROUP_CONCAT migration - now parsed as Function, not MySQLGroupConcat
    // ====================================================================

    @Test
    void testGroupConcatParsedAsFunction() throws JSQLParserException {
        String sql = "SELECT GROUP_CONCAT(col SEPARATOR ',') FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func, "GROUP_CONCAT should parse as Function");
        assertEquals("GROUP_CONCAT", func.getName());

        // SEPARATOR should be a keyword argument
        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals(1, kwArgs.size());
        assertEquals("SEPARATOR", kwArgs.get(0).getKeyword().toUpperCase());
        assertEquals("','", kwArgs.get(0).getExpression().toString());
    }

    @Test
    void testGroupConcatDistinctOrderBySeparator() throws JSQLParserException {
        String sql = "SELECT GROUP_CONCAT(DISTINCT col ORDER BY col ASC SEPARATOR ';') FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        assertTrue(func.isDistinct(), "DISTINCT should be set");
        assertNotNull(func.getOrderByElements(), "ORDER BY should be present");

        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals("SEPARATOR", kwArgs.get(0).getKeyword().toUpperCase());
    }

    @Test
    void testGroupConcatSeparatorExpression() throws JSQLParserException {
        // The original bug: SEPARATOR with a function call, not just a string literal
        String sql = "SELECT GROUP_CONCAT(description SEPARATOR CHR(10)) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals(1, kwArgs.size());

        Expression separatorExpr = kwArgs.get(0).getExpression();
        assertInstanceOf(Function.class, separatorExpr,
                "SEPARATOR expression should be a Function call (CHR)");
        assertEquals("CHR", ((Function) separatorExpr).getName());
    }

    // ====================================================================
    // AST structure assertions
    // ====================================================================

    @Test
    void testKeywordArgumentsPresentInAST() throws JSQLParserException {
        String sql = "SELECT my_agg(col ORDER BY col SEPARATOR ',') FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        assertEquals("my_agg", func.getName());

        // ORDER BY should be captured by the explicit clause
        assertNotNull(func.getOrderByElements());
        assertFalse(func.getOrderByElements().isEmpty());

        // SEPARATOR should be captured as a generic keyword argument
        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals(1, kwArgs.size());
        assertEquals("SEPARATOR", kwArgs.get(0).getKeyword().toUpperCase());
        assertEquals("','", kwArgs.get(0).getExpression().toString());
    }

    @Test
    void testMultipleKeywordArguments() throws JSQLParserException {
        String sql = "SELECT custom_func(col FORMAT 'json' ENCODING 'utf8') FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals(2, kwArgs.size());

        assertEquals("FORMAT", kwArgs.get(0).getKeyword().toUpperCase());
        assertEquals("'json'", kwArgs.get(0).getExpression().toString());

        assertEquals("ENCODING", kwArgs.get(1).getKeyword().toUpperCase());
        assertEquals("'utf8'", kwArgs.get(1).getExpression().toString());
    }

    @Test
    void testMultiValueKeywordArgument_OraclePrediction() throws JSQLParserException {
        String sql = "SELECT PREDICTION(my_model USING col1, col2, col3) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        assertEquals("PREDICTION", func.getName());

        List<Function.KeywordArgument> kwArgs = func.getKeywordArguments();
        assertNotNull(kwArgs);
        assertEquals(1, kwArgs.size());

        // USING col1, col2, col3 — multi-value, kept as ExpressionList
        assertEquals("USING", kwArgs.get(0).getKeyword().toUpperCase());
        Expression usingExpr = kwArgs.get(0).getExpression();
        assertInstanceOf(ExpressionList.class,
                usingExpr, "Multi-value keyword arg should be an ExpressionList");
        assertEquals("col1, col2, col3", usingExpr.toString());
    }

    @Test
    void testGetKeywordArgumentValue() throws JSQLParserException {
        String sql = "SELECT my_agg(col SEPARATOR ',' ENCODING 'utf8') FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);

        assertNotNull(func);
        Expression sep = func.getKeywordArgumentValue("SEPARATOR");
        assertNotNull(sep, "Should find SEPARATOR by name");
        assertEquals("','", sep.toString());

        Expression enc = func.getKeywordArgumentValue("ENCODING");
        assertNotNull(enc, "Should find ENCODING by name");

        Expression missing = func.getKeywordArgumentValue("NONEXISTENT");
        assertNull(missing, "Non-existent keyword should return null");
    }

    @Test
    void testKeywordArgumentPreservedInAnalyticExpression() throws JSQLParserException {
        String sql = "SELECT my_agg(col SEPARATOR ',') OVER (PARTITION BY grp) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);

        PlainSelect select = getPlainSelect(stmt);
        Expression expr = select.getSelectItems().get(0).getExpression();

        assertInstanceOf(AnalyticExpression.class, expr);
        AnalyticExpression analytic = (AnalyticExpression) expr;

        List<Function.KeywordArgument> kwArgs = analytic.getKeywordArguments();
        assertNotNull(kwArgs,
                "Keyword arguments should be copied from Function to AnalyticExpression");
        assertEquals(1, kwArgs.size());
        assertEquals("SEPARATOR", kwArgs.get(0).getKeyword().toUpperCase());
    }

    // ====================================================================
    // Negative / regression tests - must NOT break existing clauses
    // ====================================================================

    @Test
    void testExplicitClausesStillWork() throws JSQLParserException {
        String sql =
                "SELECT LISTAGG(col, ',' ON OVERFLOW TRUNCATE '...' WITH COUNT) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt);
        assertEquals(stmt.toString(), CCJSqlParserUtil.parse(stmt.toString()).toString());
    }

    @Test
    void testOrderByStillWorks() throws JSQLParserException {
        String sql = "SELECT my_func(col ORDER BY col ASC) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);
        assertNotNull(func);
        assertNotNull(func.getOrderByElements());
        assertNull(func.getKeywordArguments(),
                "No keyword args - ORDER BY should be handled by explicit clause");
    }

    @Test
    void testIgnoreNullsStillWorks() throws JSQLParserException {
        String sql = "SELECT my_func(col IGNORE NULLS) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);
        assertNotNull(func);
        assertEquals(Function.NullHandling.IGNORE_NULLS, func.getNullHandling());
        assertNull(func.getKeywordArguments());
    }

    @Test
    void testNoKeywordArguments() throws JSQLParserException {
        String sql = "SELECT MAX(col) FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Function func = extractFirstFunction(stmt);
        assertNotNull(func);
        assertNull(func.getKeywordArguments(),
                "Normal function should have null keywordArguments");
    }

    @Test
    void testOperatorsNotSwallowed() throws JSQLParserException {
        // Regression: f1(a1=1) must NOT treat "=" as a keyword arg
        String sql = "SELECT f1(a1 = 1).f2 = 1 FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt);
        assertEquals(stmt.toString(), CCJSqlParserUtil.parse(stmt.toString()).toString());
    }

    @Test
    void testCaseEndNotSwallowed() throws JSQLParserException {
        // Regression: CASE...END='pastdue' must not lose the = comparison
        String sql = "SELECT CASE WHEN a = 1 THEN 'x' ELSE 'y' END = 'x' FROM t";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertNotNull(stmt);
        assertEquals(stmt.toString(), CCJSqlParserUtil.parse(stmt.toString()).toString());
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private static PlainSelect getPlainSelect(Statement stmt) {
        assertInstanceOf(Select.class, stmt);
        Select select = (Select) stmt;
        assertInstanceOf(PlainSelect.class, select);
        return (PlainSelect) select;
    }

    private static Function extractFirstFunction(Statement stmt) {
        PlainSelect select = getPlainSelect(stmt);
        Expression expr = select.getSelectItems().get(0).getExpression();
        if (expr instanceof Function) {
            return (Function) expr;
        }
        return null;
    }

}
