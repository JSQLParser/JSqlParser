/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HexValueTest {

    @Test
    void testHexCode() throws JSQLParserException {
        String sqlString = "SELECT 0xF001, X'00A1', X'C3BC'";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlString);

        HexValue hex1 = (HexValue) select.getSelectItem(0).getExpression();
        Assertions.assertEquals("F001", hex1.getDigits());
        Assertions.assertEquals(61441, hex1.getLong());
        Assertions.assertEquals(61441, hex1.getLongValue().getValue());

        HexValue hex2 = (HexValue) select.getSelectItem(1).getExpression();
        Assertions.assertEquals("00A1", hex2.getDigits());
        Assertions.assertEquals(161, hex2.getLong());
        Assertions.assertEquals(161, hex2.getLongValue().getValue());

        HexValue hex3 = (HexValue) select.getSelectItem(2).getExpression();
        Assertions.assertEquals("C3BC", hex3.getDigits());
        Assertions.assertEquals("'ü'", hex3.getStringValue().toString());
        Assertions.assertEquals("ü", hex3.getStringValue().getValue());

        Assertions.assertEquals("'\\xC3\\xBC'", hex3.getBlob().toString());
    }

    @Test
    void testHexLiteralFollowedByPrimaryKeyword() throws JSQLParserException {
        // Issue #2435: a "0x"-prefixed hex literal must not greedily consume the
        // following whitespace nor adjacent hex letters (e.g. the "F" of FROM),
        // which previously made "SELECT 0xFF FROM t" fail to parse.
        assertHexSelectItem("SELECT 0xFF FROM t", "FF");
        assertHexSelectItem("SELECT 0xff FROM t", "ff");
        assertHexSelectItem("SELECT 0xDEADBEEF FROM t", "DEADBEEF");
        assertHexSelectItem("SELECT 0xab FROM dual", "ab");
    }

    private static void assertHexSelectItem(String sql, String expectedDigits)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
        Expression expr = select.getSelectItem(0).getExpression();
        Assertions.assertTrue(expr instanceof HexValue, () -> "Expected HexValue for: " + sql);
        Assertions.assertEquals(expectedDigits, ((HexValue) expr).getDigits());
    }
}
