/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class PivotPipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "(\n" +
                "  SELECT \"kale\" AS product, 51 AS sales, \"Q1\" AS quarter\n" +
                "  UNION ALL\n" +
                "  SELECT \"kale\" AS product, 4 AS sales, \"Q1\" AS quarter\n" +
                "  UNION ALL\n" +
                "  SELECT \"kale\" AS product, 45 AS sales, \"Q2\" AS quarter\n" +
                "  UNION ALL\n" +
                "  SELECT \"apple\" AS product, 8 AS sales, \"Q1\" AS quarter\n" +
                "  UNION ALL\n" +
                "  SELECT \"apple\" AS product, 10 AS sales, \"Q2\" AS quarter\n" +
                ")\n" +
                "|> PIVOT(SUM(sales) FOR quarter IN (\"Q1\", \"Q2\"));";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
