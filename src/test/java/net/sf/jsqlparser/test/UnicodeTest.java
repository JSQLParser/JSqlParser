/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import net.sf.jsqlparser.*;
import org.junit.jupiter.api.*;

public class UnicodeTest {

    @Test
    void testCJKSetIssue1741() throws JSQLParserException {
        String sqlStr = "select c as 中文 from t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        sqlStr = "select * from t where 中文 = 'abc'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCJKSetIssue1747() throws JSQLParserException {
        String sqlStr = "SELECT 가 FROM 나";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
