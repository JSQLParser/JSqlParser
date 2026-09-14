/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlNumericTypeTest {
    static Stream<Arguments> types() {
        return Stream.of(Arguments.of("numeric(2,-3)", 2, -3), Arguments.of("decimal(2,-3)", 2, -3),
                Arguments.of("dec(2,-3)", 2, -3), Arguments.of("numeric(2,-1)", 2, -1),
                Arguments.of("numeric(2,0)", 2, 0), Arguments.of("numeric(3,5)", 3, 5),
                Arguments.of("numeric(1000,-1000)", 1000, -1000),
                Arguments.of("numeric(1,1000)", 1, 1000), Arguments.of("numeric(2,- 3)", 2, -3),
                Arguments.of("numeric(2,/* scale */-3)", 2, -3),
                Arguments.of("pg_catalog.numeric(2,-3)", 2, -3),
                Arguments.of("\"numeric\"(2,-3)", 2, -3), Arguments.of("numeric(2,-3)[]", 2, -3),
                Arguments.of("numeric(2,-3)[][]", 2, -3), Arguments.of("numeric(2)", 2, null),
                Arguments.of("numeric", null, null), Arguments.of("varchar(8)", 8, null),
                Arguments.of("timestamp(3) with time zone", 3, null));
    }

    @ParameterizedTest
    @MethodSource("types")
    void exposesParametersInFragmentsDdlAndCasts(String sqlType, Integer precision, Integer scale)
            throws Exception {
        ColDataType type = CCJSqlParserUtil.parseColDataType(sqlType,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertParameters(type, precision, scale);
        assertParameters(CCJSqlParserUtil.parseColDataType(type.toString()), precision, scale);
        for (String sql : List.of("CREATE TABLE t (c " + sqlType + ")",
                "SELECT CAST(NULL AS " + sqlType + ")", "SELECT NULL::" + sqlType)) {
            Statement statement = parse(sql);
            StringBuilder buffer = new StringBuilder();
            statement.accept(new StatementDeParser(buffer), null);
            assertEquals(statement.toString(), buffer.toString());
            for (Statement tree : List.of(statement, parse(buffer.toString()))) {
                ColDataType actual = tree instanceof CreateTable
                        ? ((CreateTable) tree).getColumnDefinitions().get(0).getColDataType()
                        : ((PlainSelect) tree).getSelectItem(0).getExpression(CastExpression.class)
                                .getColDataType();
                assertParameters(actual, precision, scale);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ALTER COLUMN c TYPE numeric(2,-3)",
            "CREATE DOMAIN rounded AS numeric(2,-3)",
            "CREATE TYPE composite_t AS (c numeric(2,-3))",
            "CREATE TABLE t(c numeric(2,-3) DEFAULT 12345::numeric(2,-3))",
            "CREATE TABLE t(c numeric(2,-3) GENERATED ALWAYS AS (12345::numeric(2,-3)) STORED)"})
    void supportsSharedTypeGrammarInOtherDdl(String sql) throws Exception {
        Statement statement = parse(sql);
        assertEquals(statement.toString(), parse(statement.toString()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"numeric(2,-)", "numeric(2,+3)", "numeric(+2,3)", "numeric(2,-3.5)",
            "numeric(2,--3)", "numeric(2,-3) NOT NULL"})
    void rejectsMalformedFragmentsAndTrailingInput(String type) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseColDataType(type,
                        p -> p.withDialect(Dialect.POSTGRESQL)));
    }

    @Test
    void distinguishesNegativeScaleFromOmittedScaleWithoutChangingLegacyConstructor() {
        ColDataType negative = ColDataType.fromNumericParameters("numeric", 2, -1);
        assertEquals(-1, negative.getScale());
        assertEquals("numeric (2, -1)", negative.toString());
        ColDataType legacy = new ColDataType("numeric", 2, -1);
        assertNull(legacy.getScale());
        assertEquals("numeric (2)", legacy.toString());
        ColDataType boxedLegacy = new ColDataType("numeric", 2, Integer.valueOf(-1));
        assertNull(boxedLegacy.getScale());
    }

    @Test
    void preservesSharedOracleNumberSyntaxAndDefaultConfiguration() throws Exception {
        for (ColDataType type : List.of(CCJSqlParserUtil.parseColDataType("NUMBER(5,-2)"),
                CCJSqlParserUtil.parseColDataType("NUMBER(5,-2)",
                        p -> p.withDialect(Dialect.ORACLE)))) {
            assertParameters(type, 5, -2);
        }
    }

    private static Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertParameters(ColDataType type, Integer precision, Integer scale) {
        assertEquals(precision, type.getPrecision(), type.toString());
        assertEquals(scale, type.getScale(), type.toString());
    }
}
