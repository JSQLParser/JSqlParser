/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.StringProvider;
import net.sf.jsqlparser.statement.select.Select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class StatementsTest {

    @Test
    public void testStatements() throws JSQLParserException {
        String sqlStr = "select * from mytable; select * from mytable2;";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);

        assertEquals("SELECT * FROM mytable;\nSELECT * FROM mytable2;\n", statements.toString());

        assertInstanceOf(Select.class, statements.get(0));
        assertInstanceOf(Select.class, statements.get(1));
    }

    @Test
    public void testStatementsProblem() throws JSQLParserException {
        String sqls = ";;select * from mytable;;select * from mytable2;;;";
        Statements statements = CCJSqlParserUtil.parseStatements(sqls);

        assertEquals("SELECT * FROM mytable;\nSELECT * FROM mytable2;\n", statements.toString());

        assertInstanceOf(Select.class, statements.get(0));
        assertInstanceOf(Select.class, statements.get(1));
    }

    @Test
    public void testStatementsErrorRecovery() throws JSQLParserException, ParseException {
        String sqlStr = "select * from mytable; select from;";

        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqlStr));
        parser.setErrorRecovery(true);
        Statements parseStatements = parser.Statements();

        assertEquals(2, parseStatements.size());

        assertInstanceOf(Select.class, parseStatements.get(0));
        assertInstanceOf(Select.class, parseStatements.get(0));

        assertEquals(1, parser.getParseErrors().size());
    }

    @Test
    public void testStatementsErrorRecovery2() throws JSQLParserException, ParseException {
        String sqls = "select * from1 table;";
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
        parser.setErrorRecovery(true);
        Statements parseStatements = parser.Statements();

        assertEquals(1, parseStatements.size());

        assertNull(parseStatements.get(0));
        assertEquals(1, parser.getParseErrors().size());
    }

    @Test
    public void testStatementsErrorRecovery3() throws JSQLParserException, ParseException {
        CCJSqlParser parser =
                new CCJSqlParser("select * from mytable; select from; select * from mytable2");
        Statements statements = parser.withErrorRecovery().Statements();

        assertEquals(3, statements.size());

        assertInstanceOf(Select.class, statements.get(0));
        assertNull(statements.get(1));
        assertInstanceOf(Select.class, statements.get(2));

        assertEquals(1, parser.getParseErrors().size());
    }

    @Test
    public void testStatementsErrorRecovery4() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "select * from mytable; select from; select * from mytable2; select 4 from dual;",
                parser -> parser.withUnsupportedStatements());

        assertEquals(4, statements.size());

        assertInstanceOf(Select.class, statements.get(0));
        assertInstanceOf(UnsupportedStatement.class, statements.get(1));
        assertInstanceOf(Select.class, statements.get(2));
        assertInstanceOf(Select.class, statements.get(3));

        TestUtils.assertStatementCanBeDeparsedAs(statements.get(1), "select from", true);
    }
}
