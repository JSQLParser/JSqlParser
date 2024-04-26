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
        String sqlString = "SELECT 0xF001, X'00A1'";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlString);

        HexValue hex1 = (HexValue) select.getSelectItem(0).getExpression();
        Assertions.assertEquals("F001", hex1.getDigits());
        Assertions.assertEquals(61441, hex1.getLong());
        Assertions.assertEquals(61441, hex1.getLongValue().getValue());

        HexValue hex2 = (HexValue) select.getSelectItem(1).getExpression();
        Assertions.assertEquals("00A1", hex2.getDigits());
        Assertions.assertEquals(161, hex2.getLong());
        Assertions.assertEquals(161, hex2.getLongValue().getValue());
    }
}
