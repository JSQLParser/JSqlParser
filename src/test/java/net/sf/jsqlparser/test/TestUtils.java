/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author toben
 */
public class TestUtils {

    private static final Pattern SQL_COMMENT_PATTERN = Pattern.
            compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

    public static void assertSqlCanBeParsedAndDeparsed(String statement) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(statement, false);
    }

    /**
     * Tries to parse and deparse the given statement.
     *
     * @param statement
     * @param laxDeparsingCheck removes all linefeeds from the original and
     * removes all double spaces. The check is caseinsensitive.
     * @throws JSQLParserException
     */
    public static void assertSqlCanBeParsedAndDeparsed(String statement, boolean laxDeparsingCheck) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(statement, laxDeparsingCheck, null);
    }

    public static void assertSqlCanBeParsedAndDeparsed(String statement, boolean laxDeparsingCheck, Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        Statement parsed = CCJSqlParserUtil.parse(statement, consumer);
        assertStatementCanBeDeparsedAs(parsed, statement, laxDeparsingCheck);
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement) {
        assertStatementCanBeDeparsedAs(parsed, statement, false);
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement, boolean laxDeparsingCheck) {
        assertEquals(buildSqlString(statement, laxDeparsingCheck),
                buildSqlString(parsed.toString(), laxDeparsingCheck));

        assertDeparse(parsed, statement, laxDeparsingCheck);
    }

    public static void assertDeparse(Statement parsed, String statement) {
        assertDeparse(parsed, statement, false);
    }

    public static void assertDeparse(Statement parsed, String statement, boolean laxDeparsingCheck) {
        StatementDeParser deParser = new StatementDeParser(new StringBuilder());
        parsed.accept(deParser);
        assertEquals(buildSqlString(statement, laxDeparsingCheck),
                buildSqlString(deParser.getBuffer().toString(), laxDeparsingCheck));
    }

    public static String buildSqlString(final String originalSql, boolean laxDeparsingCheck) {
        String sql = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        if (laxDeparsingCheck) {
            return sql.replaceAll("\\s", " ").replaceAll("\\s+", " ").
                    replaceAll("\\s*([!/,()=+\\-*|\\]<>])\\s*", "$1").toLowerCase().trim();
        } else {
            return sql;
        }
    }

    @Test
    public void testBuildSqlString() {
        assertEquals("select col from test", buildSqlString("   SELECT   col FROM  \r\n \t  TEST \n", true));
        assertEquals("select  col  from test", buildSqlString("select  col  from test", false));
    }

    public static void assertExpressionCanBeDeparsedAs(final Expression parsed, String expression) {
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        StringBuilder stringBuilder = new StringBuilder();
        expressionDeParser.setBuffer(stringBuilder);
        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, stringBuilder);
        expressionDeParser.setSelectVisitor(selectDeParser);
        parsed.accept(expressionDeParser);

        assertEquals(expression, stringBuilder.toString());
    }

    public static void assertOracleHintExists(String sql, boolean assertDeparser, String... hints) throws JSQLParserException {
        if (assertDeparser) {
            assertSqlCanBeParsedAndDeparsed(sql, true);
        }
        Select stmt = (Select) CCJSqlParserUtil.parse(sql);
        if (stmt.getSelectBody() instanceof PlainSelect) {
            PlainSelect ps = (PlainSelect) stmt.getSelectBody();
            OracleHint hint = ps.getOracleHint();
            assertNotNull(hint);
            assertEquals(hints[0], hint.getValue());
        } else {
            if (stmt.getSelectBody() instanceof SetOperationList) {
                SetOperationList setop = (SetOperationList) stmt.getSelectBody();
                for (int i = 0; i < setop.getSelects().size(); i++) {
                    PlainSelect pselect = (PlainSelect) setop.getSelects().get(i);
                    OracleHint hint = pselect.getOracleHint();
                    if (hints[i] == null) {
                        Assert.assertNull(hint);
                    } else {
                        assertNotNull(hint);
                        assertEquals(hints[i], hint.getValue());
                    }
                }
            }
        }
    }

}
