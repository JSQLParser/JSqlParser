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

import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MySqlTypeCharacterSetTest {
    private Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT CAST('xx' AS CHAR(16) CHARSET BINARY)",
            "SELECT CAST('xx' AS CHAR(16) CHARACTER SET 'utf8mb4')",
            "SELECT CONVERT('xx' USING utf8mb4)",
            "CREATE TABLE t (c VARCHAR(20) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin')",
            "CREATE TABLE t (c TEXT CHARSET utf8mb4)",
            "CREATE TABLE t (c SET('a','b') CHARSET 'utf8mb4')",
            "CREATE TABLE t (c BINARY(16) AS (CAST('xx' AS CHAR(16) CHARSET BINARY)))",
            "ALTER TABLE t MODIFY c ENUM('a','b') CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci'",
            "ALTER TABLE t ADD c CHAR(4) CHARSET utf8mb4, ADD d VARCHAR(10) CHARACTER SET utf8mb4"
    })
    void sharesTypeSyntaxAcrossCastsAndColumnDefinitions(String sql) throws Exception {
        Statement statement = parse(sql);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(statement.toString(), parse(output.toString()).toString());
    }

    @Test
    void exposesCharsetAndRetainsAbbreviationWhenMutated() throws Exception {
        CreateTable table = (CreateTable) parse("CREATE TABLE t (c VARCHAR(20) CHARSET 'utf8mb4')");
        ColDataType type = table.getColumnDefinitions().get(0).getColDataType();
        assertEquals("'utf8mb4'", type.getCharacterSet());
        assertTrue(type.isUseCharsetKeyword());
        type.setCharacterSet("latin1");
        assertEquals("CREATE TABLE t (c VARCHAR (20) CHARSET latin1)", table.toString());
        type.setUseCharsetKeyword(false);
        assertEquals("CREATE TABLE t (c VARCHAR (20) CHARACTER SET latin1)", table.toString());
        type.setCharacterSet(null);
        assertEquals("CREATE TABLE t (c VARCHAR (20))", table.toString());
    }

    @Test
    void preservesDefaultApiSpellingAndTypeEquality() throws Exception {
        ColDataType legacy = new ColDataType("CHAR").withCharacterSet("binary");
        assertEquals("CHAR CHARACTER SET binary", legacy.toString());
        PlainSelect select = (PlainSelect) parse("SELECT CAST('a' AS CHAR CHARACTER SET binary)");
        ColDataType parsed =
                ((CastExpression) select.getSelectItem(0).getExpression()).getColDataType();
        assertEquals(legacy, parsed);
        assertEquals(legacy.hashCode(), parsed.hashCode());
        parsed.setUseCharsetKeyword(true);
        assertNotEquals(legacy, parsed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT CAST('x' AS CHAR CHARSET)",
            "SELECT CAST('x' AS CHAR CHARACTER SET)",
            "SELECT CAST('x' AS CHAR CHARSET = utf8mb4)"})
    void requiresCharsetName(String sql) {
        assertThrows(Exception.class, () -> parse(sql));
    }
}
