/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class QuotedHexValueTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT X'6162' AS v",
            "SELECT X'6162'     AS v",
            "SELECT X'6162' FROM t",
            "SELECT X'6162' ",
            "SELECT X'6162'\tAS v",
            "SELECT X'6162'\nAS v"
    })
    void whitespaceIsNotPartOfHexLiteral(String sql) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.MYSQL, Dialect.POSTGRESQL}) {
            PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql,
                    parser -> {
                        if (dialect != null) {
                            parser.withDialect(dialect);
                        }
                    });
            HexValue hex = (HexValue) select.getSelectItem(0).getExpression();
            assertEquals("X'6162'", hex.getValue());
            assertEquals("6162", hex.getDigits());
            assertEquals(24930L, hex.getLong());
            assertEquals("ab", hex.getStringValue().getValue());
            assertEquals("\\x61\\x62", hex.getBlob().getValue());
            String canonical = select.toString();
            for (int i = 0; i < 3; i++) {
                select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(canonical,
                        false, parser -> {
                            if (dialect != null) {
                                parser.withDialect(dialect);
                            }
                        });
                assertEquals(canonical, select.toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"X'61 62'", "X'61' '62'", "X'61''62'", "X''", "x'6162'", "0x6162"})
    void existingHexTokenFormsArePreserved(String literal) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT " + literal + " AS v");
        assertEquals(literal, ((HexValue) select.getSelectItem(0).getExpression()).getValue());
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString(), false);
    }

    @Test
    void jsonDefaultHexIsAccessibleAndEditable() throws JSQLParserException {
        String sql = "SELECT JSON_VALUE('{}', '$.v' DEFAULT X'6162' ON EMPTY)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false);
        JsonFunction function = (JsonFunction) select.getSelectItem(0).getExpression();
        HexValue hex = (HexValue) function.getOnEmptyBehavior().getExpression();
        assertEquals("6162", hex.getDigits());
        assertEquals("ab", hex.getStringValue().getValue());
        hex.setValue("X'6364'");
        TestUtils.assertStatementCanBeDeparsedAs(select, sql.replace("6162", "6364"));
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString(), false);
        assertEquals("cd", hex.getStringValue().getValue());
    }
}
