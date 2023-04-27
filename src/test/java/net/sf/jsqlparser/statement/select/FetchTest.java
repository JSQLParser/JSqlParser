/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

class FetchTest {
    @Test
    void testParser() throws JSQLParserException {
        String sqlStr = "SELECT table_schema \n" + "FROM information_schema.tables \n"
                + "fetch next :variable rows only";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void getExpression() throws JSQLParserException {
        String sqlStr = "SELECT table_schema \n" + "FROM information_schema.tables \n"
                + "fetch next (SELECT 1 FROM DUAL) rows only";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Fetch fetch = plainSelect.getFetch();
        Assertions.assertInstanceOf(ParenthesedSelect.class, fetch.getExpression());
    }
}
