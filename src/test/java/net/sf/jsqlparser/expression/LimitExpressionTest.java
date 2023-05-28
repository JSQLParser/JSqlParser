/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class LimitExpressionTest {

    @Test
    public void testIssue933() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM tmp3 LIMIT '2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM tmp3 LIMIT (SELECT 2)", true);
    }

    @Test
    public void testIssue1373() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT 1 LIMIT 1+0", true);
    }

    @Test
    public void testIssue1376() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("select 1 offset '0'", true);
    }

    @Test
    public void testMethods() throws JSQLParserException {
        String sqlStr = "SELECT * FROM tmp3 LIMIT 5 OFFSET 3";
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        LongValue longValue = plainSelect.getLimit().getRowCount(LongValue.class);
        Assertions.assertNotNull(longValue);
        Assertions.assertEquals(longValue, longValue);
        Assertions.assertNull(plainSelect.getLimit().getOffset(LongValue.class));
        Assertions.assertNotNull(plainSelect.getOffset().getOffset(LongValue.class));
        sqlStr = "SELECT * FROM tmp3 LIMIT ALL";
        plainSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        AllValue allValue = plainSelect.getLimit().getRowCount(AllValue.class);
        allValue.accept(new ExpressionVisitorAdapter());
    }
}
