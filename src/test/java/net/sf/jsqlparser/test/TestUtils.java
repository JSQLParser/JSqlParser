/*
 * Copyright (C) 2013 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

/**
 *
 * @author toben
 */
public class TestUtils {

    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("(--.*$)|(/\\*.*?\\*/)",Pattern.MULTILINE);

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
        Statement parsed = CCJSqlParserUtil.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(parsed, statement, laxDeparsingCheck);
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement) {
        assertStatementCanBeDeparsedAs(parsed, statement, false);
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement, boolean laxDeparsingCheck) {
        assertEquals(buildSqlString(statement, laxDeparsingCheck), 
                buildSqlString(parsed.toString(), laxDeparsingCheck));

        StatementDeParser deParser = new StatementDeParser(new StringBuilder());
        parsed.accept(deParser);
        assertEquals(buildSqlString(statement, laxDeparsingCheck), 
                buildSqlString(deParser.getBuffer().toString(), laxDeparsingCheck));
    }
    
    public static String buildSqlString(final String originalSql, boolean laxDeparsingCheck) {
    	String sql = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        if (laxDeparsingCheck) {
            return sql.replaceAll("\\s", " ").replaceAll("\\s+", " ").replaceAll("\\s*([!/,()=+\\-*|\\]<>])\\s*", "$1").toLowerCase().trim();
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
