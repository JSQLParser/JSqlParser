/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class ShowTablesStatementTest {

    @Test
    public void showTables() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW TABLES");
    }

    @Test
    public void showTablesModifiers() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW EXTENDED FULL TABLES");
    }

    @Test
    public void showTablesFromDbName() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW EXTENDED TABLES FROM db_name");
    }

    @Test
    public void showTablesInDbName() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW FULL TABLES IN db_name");
    }

    @Test
    public void showTablesLikeExpression() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW TABLES LIKE '%FOO%'");
    }

    @Test
    public void showTablesWhereExpression() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SHOW TABLES WHERE table_name = 'FOO'");
    }

    @Test
    public void testObject() throws JSQLParserException, JSQLParserException {
        ShowTablesStatement showTablesStatement = (ShowTablesStatement) CCJSqlParserUtil.parse("SHOW TABLES WHERE table_name = 'FOO'");
        Assert.assertEquals(0, showTablesStatement.getModifiers().size());
        TestUtils.assertExpressionCanBeDeparsedAs(showTablesStatement.getWhereCondition(), "table_name = 'FOO'");

        showTablesStatement = (ShowTablesStatement) CCJSqlParserUtil.parse("SHOW FULL TABLES IN db_name");
        Assert.assertEquals(1, showTablesStatement.getModifiers().size());
        Assert.assertEquals(ShowTablesStatement.SelectionMode.IN, showTablesStatement.getSelectionMode());

        showTablesStatement = (ShowTablesStatement) CCJSqlParserUtil.parse("SHOW TABLES LIKE '%FOO%'");
        TestUtils.assertExpressionCanBeDeparsedAs(showTablesStatement.getLikeExpression(), "'%FOO%'");
    }
}
