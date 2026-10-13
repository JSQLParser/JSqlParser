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
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DigitSeparatorsTest {

    private static Expression firstItem(String sql, boolean digitSeparators)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDigitSeparators(digitSeparators));
        return select.getSelectItem(0).getExpression();
    }

    @ParameterizedTest
    @CsvSource({"1_000, 1000", "1_000_000, 1000000", "92_000.0, 92000.0", "1_000.5_5, 1000.55",
            ".5_5, .55", "1e1_0, 1e10", "1_0e10, 10e10", "1.5e+1_0, 1.5e+10"})
    void testSeparatorsAreDropped(String literal, String expected) throws JSQLParserException {
        assertEquals(expected, firstItem("SELECT " + literal, true).toString());
    }

    @Test
    void testSeparatedNumbersAreNumbers() throws JSQLParserException {
        assertEquals(1000L, ((LongValue) firstItem("SELECT 1_000", true)).getValue());
        assertEquals(92000.0, ((DoubleValue) firstItem("SELECT 92_000.0", true)).getValue());
        assertEquals("SELECT * FROM t LIMIT 1000", CCJSqlParserUtil
                .parse("SELECT * FROM t LIMIT 1_000", parser -> parser.withDigitSeparators())
                .toString());
    }

    @Test
    void testPostgreSqlAllowsSeparators() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT 1_000",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertInstanceOf(LongValue.class, select.getSelectItem(0).getExpression());
    }

    // MySQL identifiers may start with digits
    @Test
    void testWithoutSeparatorsDigitLedNamesStayIdentifiers() throws JSQLParserException {
        assertInstanceOf(Column.class, firstItem("SELECT 1_000", false));
        assertInstanceOf(Column.class, firstItem("SELECT 2024_01.c FROM 2024_01", false));
        assertEquals("SELECT .5 _5", CCJSqlParserUtil.parse("SELECT .5_5").toString());
        assertEquals("SELECT 1.5e+1 _0", CCJSqlParserUtil.parse("SELECT 1.5e+1_0").toString());
    }
}
