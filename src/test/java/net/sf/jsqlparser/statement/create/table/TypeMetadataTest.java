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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TypeMetadataTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "MYSQL | DECIMAL(65, 30) | DECIMAL | 65 | 30",
            "MYSQL | NUMERIC(1, 0) | NUMERIC | 1 | 0",
            "MYSQL | VARCHAR(255) | VARCHAR | 255 |",
            "MYSQL | NATIONAL VARCHAR(20) | NATIONAL VARCHAR | 20 |",
            "MYSQL | mediumint(9) UNSIGNED | mediumint | 9 |",
            "MYSQL | ENUM('a(b)', 'c') | ENUM | |",
            "POSTGRESQL | NUMERIC(1000, 1000) | NUMERIC | 1000 | 1000",
            "POSTGRESQL | NUMERIC(3, 5) | NUMERIC | 3 | 5",
            "POSTGRESQL | DECIMAL(10) | DECIMAL | 10 |",
            "POSTGRESQL | CHARACTER VARYING(255) | CHARACTER VARYING | 255 |",
            "POSTGRESQL | TIMESTAMP(6) WITH TIME ZONE | TIMESTAMP WITH TIME ZONE | 6 |",
            "POSTGRESQL | TIME(0) WITHOUT TIME ZONE | TIME WITHOUT TIME ZONE | 0 |",
            "POSTGRESQL | NUMERIC(10, 2)[] | NUMERIC | 10 | 2",
            "POSTGRESQL | INTERVAL(6) | INTERVAL | 6 |"})
    void exposesMetadataAcrossFragmentsDdlAndPostgreSqlCasts(Dialect dialect, String typeName,
            String base, Integer precision, Integer scale) throws JSQLParserException {
        ColDataType fragment = parseType(typeName, dialect);
        assertMetadata(fragment, base, precision, scale);
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE type_case (v " + typeName + ")", p -> p.withDialect(dialect));
        assertMetadata(table.getColumnDefinitions().get(0).getColDataType(), base, precision,
                scale);
        assertMetadata(parseType(fragment.toString(), dialect), base, precision, scale);
        if (dialect == Dialect.POSTGRESQL) {
            PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                    "SELECT CAST(NULL AS " + typeName + ")", p -> p.withDialect(dialect));
            CastExpression cast = select.getSelectItem(0).getExpression(CastExpression.class);
            assertMetadata(cast.getColDataType(), base, precision, scale);
        }
    }

    // MySQL BLOB/TEXT family boundaries and the signed-int boundary, verified on MySQL 8.4.
    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "255", "256", "65535", "65536", "16777215",
            "16777216", "2147483647", "2147483648", "4294967295"})
    void retainsLargeMySqlLengthsThroughBothRenderers(String length) throws JSQLParserException {
        BigInteger expected = new BigInteger(length);
        for (String name : List.of("BLOB", "TEXT")) {
            ColDataType fragment = parseType(name + "(" + length + ")", Dialect.MYSQL);
            assertEquals(name, fragment.getBaseTypeName());
            assertEquals(expected, fragment.getNumericPrecision());
            assertFalse(fragment.isMaxPrecision());
            assertFalse(fragment.toString().contains("MAX"));
            assertEquals(expected.bitLength() < Integer.SIZE ? Integer.valueOf(length) : null,
                    fragment.getPrecision());
            CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                    "CREATE TABLE type_case (v " + name + "(" + length + "))",
                    p -> p.withDialect(Dialect.MYSQL));
            StringBuilder visitor = new StringBuilder();
            table.accept(new StatementDeParser(visitor));
            for (String rendered : List.of(table.toString(), visitor.toString())) {
                CreateTable reparsed = (CreateTable) CCJSqlParserUtil.parse(rendered,
                        p -> p.withDialect(Dialect.MYSQL));
                ColDataType type = reparsed.getColumnDefinitions().get(0).getColDataType();
                assertEquals(expected, type.getNumericPrecision());
                assertFalse(type.isMaxPrecision());
            }
        }
    }

    @Test
    void distinguishesMaxKeywordFromItsLegacySentinelValue() throws JSQLParserException {
        ColDataType max = parseType("VARCHAR(MAX)", Dialect.SQLSERVER);
        assertTrue(max.isMaxPrecision());
        assertNull(max.getNumericPrecision());
        assertEquals(Integer.MAX_VALUE, max.getPrecision());
        assertEquals("VARCHAR (MAX)", max.getDataType());
        ColDataType literal = parseType("BLOB(2147483647)", Dialect.MYSQL);
        assertFalse(literal.isMaxPrecision());
        assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), literal.getNumericPrecision());
        assertEquals("BLOB (2147483647)", literal.getDataType());
        assertEquals("VARCHAR (MAX)", new ColDataType("VARCHAR", Integer.MAX_VALUE, -1).toString());
        assertNull(new ColDataType("VARCHAR", -1, -1).getNumericPrecision());
    }

    @Test
    void metadataSettersDoNotLeaveStaleLargeOrMaxValues() throws JSQLParserException {
        ColDataType type = parseType("BLOB(4294967295)", Dialect.MYSQL);
        type.setPrecision(5);
        assertEquals(BigInteger.valueOf(5), type.getNumericPrecision());
        type.setPrecision(null);
        assertNull(type.getNumericPrecision());
        type = parseType("VARCHAR(MAX)", Dialect.SQLSERVER);
        type.setNumericPrecision(BigInteger.valueOf(2147483648L));
        assertFalse(type.isMaxPrecision());
        assertNull(type.getPrecision());
        assertEquals(BigInteger.valueOf(2147483648L), type.getNumericPrecision());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"odd(type)\"", "public.\"odd(type)\"", "\"a\"\"(b)\""})
    void retainsQuotedParenthesesAndQualification(String name) throws JSQLParserException {
        assertEquals(name, parseType(name, Dialect.POSTGRESQL).getBaseTypeName());
        ColDataType manual = new ColDataType(name, 10, 2);
        assertEquals(name, manual.getBaseTypeName());
        assertEquals(name + " (10, 2)", manual.getDataType());
    }

    @Test
    void baseNameFollowsLegacyTypeEditsWithoutStaleCache() {
        ColDataType type = new ColDataType("DECIMAL", 10, 2);
        type.setDataType("VARCHAR (20)");
        assertEquals("VARCHAR", type.getBaseTypeName());
        assertEquals("VARCHAR (20)", type.getDataType());
        type.setDataType(List.of("app", "\"kind(1)\""));
        assertEquals("app.\"kind(1)\"", type.getBaseTypeName());
        type.setDataType((String) null);
        assertNull(type.getBaseTypeName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BLOB(-1)", "BLOB(1.5)", "BLOB(4294967295", "DECIMAL(10,)"})
    void rejectsMalformedNumericParameters(String type) {
        assertThrows(JSQLParserException.class, () -> parseType(type, Dialect.MYSQL));
    }

    @Test
    void leavesDatabaseSpecificRangeValidationToTheDatabase() throws JSQLParserException {
        // Syntactically numeric, but MySQL rejects this BLOB length as out of range.
        String length = "18446744073709551616";
        assertEquals(new BigInteger(length),
                parseType("BLOB(" + length + ")", Dialect.MYSQL).getNumericPrecision());
    }

    private static ColDataType parseType(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parseColDataType(sql, p -> p.withDialect(dialect));
    }

    private static void assertMetadata(ColDataType type, String base, Integer precision,
            Integer scale) {
        assertEquals(base, type.getBaseTypeName());
        assertEquals(precision, type.getPrecision());
        assertEquals(precision == null ? null : BigInteger.valueOf(precision),
                type.getNumericPrecision());
        assertEquals(scale, type.getScale());
    }
}
