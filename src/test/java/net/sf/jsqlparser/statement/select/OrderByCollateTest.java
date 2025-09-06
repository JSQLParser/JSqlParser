/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

public class OrderByCollateTest {

    @Test
    public void testOrderByWithCollate() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY CAST(a.xyz AS TEXT) COLLATE \"und-x-icu\" ASC NULLS FIRST";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateSimple() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col COLLATE \"C\" ASC";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateMultiple() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col1 COLLATE \"C\" ASC, col2 COLLATE \"POSIX\" DESC";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateAndNulls() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col COLLATE \"C\" DESC NULLS LAST";
        assertSqlCanBeParsedAndDeparsed(sql);
    }
}
