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

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlConvertTest {
    @ParameterizedTest
    @ValueSource(strings = {"'42', SIGNED", "'-1', UNSIGNED INTEGER",
            "'3.14', DECIMAL(10,2)", "'xx', CHAR(16) CHARACTER SET 'utf8mb4'",
            "'xx', CHAR(16) CHARSET binary", "'2026-09-21', DATE",
            "NULL, CHAR", "amount, DECIMAL(12,4)", "COALESCE(amount, 0), SIGNED",
            "CONVERT('42', SIGNED), CHAR(10)", "?, SIGNED"})
    void representsExpressionBeforeType(String arguments) throws Exception {
        PlainSelect select = parse("SELECT CONVERT(" + arguments + ") FROM t", Dialect.MYSQL);
        TranscodingFunction convert = conversion(select);
        assertEquals(TranscodingFunction.Syntax.TYPE_LAST, convert.getSyntax());
        assertFalse(convert.isTranscodeStyle());
        assertNotNull(convert.getColDataType());
        roundTrip(select, Dialect.MYSQL);
    }

    @Test
    void distinguishesDialectDependentOrderAndLegacyAccessors() throws Exception {
        TranscodingFunction mysql = conversion(parse("SELECT CONVERT(value, CHAR)", Dialect.MYSQL));
        assertInstanceOf(Column.class, mysql.getExpression());
        assertEquals("CHAR", mysql.getColDataType().getDataType());
        TranscodingFunction sqlserver = conversion(
                parse("SELECT CONVERT(VARCHAR(10), value, 120)", Dialect.SQLSERVER));
        assertEquals(TranscodingFunction.Syntax.TYPE_FIRST, sqlserver.getSyntax());
        assertEquals("120", sqlserver.getTranscodingName());
        roundTrip(parse("SELECT TRY_CONVERT(INT, value)", Dialect.SQLSERVER), Dialect.SQLSERVER);
        roundTrip(parse("SELECT CONVERT(VARCHAR(10), value, 120)", Dialect.SQLSERVER),
                Dialect.SQLSERVER);
        TranscodingFunction using =
                conversion(parse("SELECT CONVERT(value USING utf8mb4)", Dialect.MYSQL));
        assertTrue(using.isTranscodeStyle());
        using.setTranscodeStyle(false);
        assertEquals(TranscodingFunction.Syntax.TYPE_FIRST, using.getSyntax());
        using.setTranscodeStyle(true);
        assertEquals(TranscodingFunction.Syntax.USING, using.getSyntax());
    }

    @Test
    void visitsOperandAndRendersTypeMutations() throws Exception {
        PlainSelect select = parse("SELECT CONVERT(42, SIGNED)", Dialect.MYSQL);
        conversion(select).getColDataType().setDataType("UNSIGNED");
        StringBuilder out = new StringBuilder();
        ExpressionDeParser visitor = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 1);
            }
        };
        select.accept(new StatementDeParser(visitor, new SelectDeParser(), out), null);
        assertEquals("SELECT CONVERT( 43, UNSIGNED )", out.toString());
        roundTrip(select, Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT CONVERT(1,)", "SELECT CONVERT(, SIGNED)",
            "SELECT CONVERT(1, SIGNED, 120)", "SELECT CONVERT(1 USING)"})
    void rejectsIncompleteArguments(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL));
    }

    private static PlainSelect parse(String sql, Dialect dialect) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static TranscodingFunction conversion(PlainSelect select) {
        return assertInstanceOf(TranscodingFunction.class, select.getSelectItem(0).getExpression());
    }

    private static void roundTrip(PlainSelect select, Dialect dialect) throws Exception {
        StringBuilder out = new StringBuilder();
        select.accept(new StatementDeParser(out), null);
        assertEquals(select.toString(), out.toString());
        assertEquals(select.toString(), parse(out.toString(), dialect).toString());
    }
}
