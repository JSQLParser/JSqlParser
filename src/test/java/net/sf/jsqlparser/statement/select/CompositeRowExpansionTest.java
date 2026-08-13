/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * PostgreSQL composite row expansion {@code (function_returning_composite).*}.
 *
 * <p>
 * This feature was merged via #2207 and afterwards disabled, because the speculative syntactic
 * lookahead used back then caused a severe performance regression. It is re-enabled here through a
 * bounded semantic follower check, see {@code FunctionAllColumns} in the grammar.
 */
public class CompositeRowExpansionTest {

    private static FunctionAllColumns assertFunctionAllColumns(String sql)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Expression expression = select.getSelectItems().get(0).getExpression();
        Assertions.assertTrue(expression instanceof FunctionAllColumns,
                "Expected a FunctionAllColumns select item but got " + expression.getClass());
        return (FunctionAllColumns) expression;
    }

    @Test
    public void testIssue2412JsonPopulateRecord() throws JSQLParserException {
        FunctionAllColumns result = assertFunctionAllColumns(
                "SELECT (json_populate_record(NULL::users, data)).* FROM staging_users");
        Assertions.assertEquals("json_populate_record", result.getFunction().getName());
    }

    @Test
    public void testSimpleFunctionAllColumns() throws JSQLParserException {
        FunctionAllColumns result = assertFunctionAllColumns("SELECT (foo(a, b)).* FROM t");
        Assertions.assertEquals("foo", result.getFunction().getName());
    }

    @Test
    public void testPgStatFileExampleFrom2207() throws JSQLParserException {
        FunctionAllColumns result = assertFunctionAllColumns(
                "SELECT (pg_stat_file('postgresql.conf')).*");
        Assertions.assertEquals("pg_stat_file", result.getFunction().getName());
    }

    @Test
    public void testIssue2412InsertSelectUseCase() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO users SELECT (json_populate_record(NULL::users, data)).* FROM staging_users",
                true);
    }

    @Test
    public void testMultipleSurroundingParensAreUnwrapped() throws JSQLParserException {
        // Redundant parentheses around a single value are semantically transparent in
        // PostgreSQL, so they are unwrapped to the inner function. The round-trip
        // therefore normalises to a single surrounding pair.
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT ((((foo(a))))).* FROM t");
        Expression expression = select.getSelectItems().get(0).getExpression();
        Assertions.assertTrue(expression instanceof FunctionAllColumns);
        Assertions.assertEquals("foo", ((FunctionAllColumns) expression).getFunction().getName());
        Assertions.assertEquals("(foo(a)).*", expression.toString());
    }

    @Test
    public void testParenthesedFunctionWithoutExpansionUnchanged() throws JSQLParserException {
        // Without the trailing .* a parenthesised function stays a plain expression.
        assertSqlCanBeParsedAndDeparsed("SELECT (foo(a, b)) FROM t", true);
    }

    @Test
    public void testRowGetExpressionAfterParenthesedFunctionUnchanged() throws JSQLParserException {
        // (function()).name must keep parsing as a RowGetExpression, not be swallowed.
        assertSqlCanBeParsedAndDeparsed("SELECT (foo(a, b)).colname FROM t", true);
    }

    @Test
    public void testNonFunctionCompositeExpansionStillUnsupported() {
        // Expanding an arbitrary (non-function) expression is out of scope and must
        // keep failing cleanly instead of producing a wrong AST.
        Assertions.assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT (a + b).* FROM t"));
    }
}
