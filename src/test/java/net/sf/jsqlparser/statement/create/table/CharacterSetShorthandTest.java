/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColDataType.CharacterSetSyntax;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CharacterSetShorthandTest {
    @ParameterizedTest
    @CsvSource({"ASCII,latin1", "UNICODE,ucs2"})
    void shorthandsExposeCharacterSetAndRetainSpelling(String shorthand, String charset)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST('ab' AS CHAR(10) " + shorthand + "), "
                        + "JSON_VALUE('{\"v\":\"ab\"}', '$.v' RETURNING CHAR(10) " + shorthand
                        + ")",
                true, parser -> parser.withDialect(Dialect.MYSQL));
        ColDataType castType =
                ((CastExpression) select.getSelectItem(0).getExpression()).getColDataType();
        ColDataType jsonType =
                ((JsonFunction) select.getSelectItem(1).getExpression()).getReturningType();
        for (ColDataType type : new ColDataType[] {castType, jsonType}) {
            assertEquals("CHAR", type.getBaseTypeName());
            assertEquals(charset, type.getCharacterSet());
            assertEquals(CharacterSetSyntax.valueOf(shorthand), type.getCharacterSetSyntax());
        }
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString(), false,
                parser -> parser.withDialect(Dialect.MYSQL));
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE t (j JSON, g CHAR(10) AS (JSON_VALUE(j, '$.v' RETURNING CHAR(10) "
                        + shorthand + ")) STORED)",
                true, parser -> parser.withDialect(Dialect.MYSQL));
        CreateTable create = (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE t (c CHAR(10) " + shorthand + ")", true,
                parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(charset,
                create.getColumnDefinitions().get(0).getColDataType().getCharacterSet());
    }

    @Test
    void parsedReturningTypeCanBeChanged() throws JSQLParserException {
        String sql = "SELECT JSON_VALUE('{\"v\":\"ab\"}', '$.v' RETURNING CHAR(10) ASCII)";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL));
        ColDataType type =
                ((JsonFunction) select.getSelectItem(0).getExpression()).getReturningType();
        type.setCharacterSetSyntax(CharacterSetSyntax.UNICODE);
        TestUtils.assertStatementCanBeDeparsedAs(select, sql.replace("ASCII", "UNICODE"), true);
        type.setCharacterSet("utf8mb4");
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql.replace("ASCII", "CHARACTER SET utf8mb4"), true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString(), false,
                parser -> parser.withDialect(Dialect.MYSQL));
    }

    @Test
    void characterSetSyntaxCanBeConstructedChangedAndCleared() throws JSQLParserException {
        ColDataType type = new ColDataType("CHAR").addArgumentsStringList("10")
                .withCharacterSetSyntax(CharacterSetSyntax.ASCII);
        assertEquals("latin1", type.getCharacterSet());
        assertEquals("CHAR (10) ASCII", type.toString());
        type.setCharacterSetSyntax(CharacterSetSyntax.UNICODE);
        assertEquals("ucs2", type.getCharacterSet());
        assertEquals("CHAR (10) UNICODE", type.toString());
        type.setCharacterSet("utf8mb4");
        assertEquals(CharacterSetSyntax.CHARACTER_SET, type.getCharacterSetSyntax());
        assertEquals("CHAR (10) CHARACTER SET utf8mb4", type.toString());
        type.setUseCharsetKeyword(true);
        assertTrue(type.isUseCharsetKeyword());
        type.setCharacterSet("latin1");
        assertEquals("CHAR (10) CHARSET latin1", type.toString());
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT CAST('ab' AS " + type + ")", true,
                parser -> parser.withDialect(Dialect.MYSQL));
        type.setCharacterSetSyntax(CharacterSetSyntax.ASCII);
        type.setUseCharsetKeyword(false);
        assertEquals("CHAR (10) CHARACTER SET latin1", type.toString());
        type.setCharacterSetSyntax(CharacterSetSyntax.UNICODE);
        type.setCharacterSet(null);
        assertEquals("CHAR (10)", type.toString());
    }

    @Test
    void syntaxParticipatesInEqualityAndHashCode() {
        ColDataType ascii =
                new ColDataType("CHAR").withCharacterSetSyntax(CharacterSetSyntax.ASCII);
        ColDataType same = new ColDataType("char").withCharacterSetSyntax(CharacterSetSyntax.ASCII);
        ColDataType explicit = new ColDataType("CHAR").withCharacterSet("latin1");
        assertEquals(ascii, same);
        assertEquals(ascii.hashCode(), same.hashCode());
        assertNotEquals(ascii, explicit);
    }

    @Test
    void shorthandIsMySqlSpecificAndDoesNotReserveIdentifiers() throws JSQLParserException {
        for (String shorthand : new String[] {"ASCII", "UNICODE"}) {
            String sql = "SELECT CAST('ab' AS CHAR(10) " + shorthand + ")";
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql,
                            parser -> parser.withDialect(Dialect.POSTGRESQL)));
            TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT " + shorthand + " FROM t", true,
                    parser -> parser.withDialect(Dialect.MYSQL));
        }
    }
}
