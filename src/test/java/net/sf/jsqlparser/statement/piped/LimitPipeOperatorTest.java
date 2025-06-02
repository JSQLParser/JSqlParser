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

import static org.junit.jupiter.api.Assertions.*;

class LimitPipeOperatorTest {

    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "(\n"
                + "  SELECT 'apples' AS item, 2 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'bananas' AS item, 5 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'carrots' AS item, 8 AS sales\n"
                + ")\n"
                + "|> ORDER BY item\n"
                + "|> LIMIT 1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testParseAndDeparseWithOffset() throws JSQLParserException {
        String sqlStr = "(\n"
                + "  SELECT 'apples' AS item, 2 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'bananas' AS item, 5 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'carrots' AS item, 8 AS sales\n"
                + ")\n"
                + "|> ORDER BY item\n"
                + "|> LIMIT 1 OFFSET 2;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
