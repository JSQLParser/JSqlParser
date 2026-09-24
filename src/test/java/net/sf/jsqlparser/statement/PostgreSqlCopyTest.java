/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCopyTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "COPY users (email, name) FROM STDIN WITH (FORMAT csv, HEADER true)",
            "COPY users TO STDOUT WITH (FORMAT csv, DELIMITER ';', NULL '')",
            "COPY users TO STDOUT WITH (FORMAT csv, FORCE_QUOTE (email, name))",
            "COPY users TO STDOUT WITH (FORMAT csv, FORCE_QUOTE *)",
            "COPY (SELECT email FROM users) TO STDOUT WITH (FORMAT csv, HEADER true)",
            "COPY users FROM STDIN WITH (FORMAT csv, HEADER MATCH, FORCE_NULL (name))",
            "COPY users FROM STDIN WITH (FORMAT csv, ON_ERROR ignore, ENCODING 'UTF8')",
            "COPY users TO '/tmp/export.csv' WITH (FORMAT csv)",
            "COPY users TO STDOUT (FORMAT csv, HEADER true)",
            "COPY users FROM STDIN",
            "COPY users TO STDOUT WITH",
            "COPY (WITH c AS (SELECT email FROM users) SELECT email FROM c) TO STDOUT WITH (FORMAT csv)",
            "COPY (SELECT 1 UNION ALL SELECT 2) TO STDOUT WITH (FORMAT csv)"
    })
    void preservesCopyOptionsAndWithKeyword(String sql) throws JSQLParserException {
        CopyStatement copy = parse(sql);
        assertEquals(sql.contains(" WITH ") || sql.endsWith(" WITH"), copy.isWithKeyword());
        assertRoundTrip(copy);
        assertEquals(copy.toString(), CCJSqlParserUtil.parse(copy.toString()).toString());
    }

    @Test
    void optionalWithKeywordCanBeEditedWithoutChangingTheOptions() throws JSQLParserException {
        CopyStatement copy = parse("COPY users TO STDOUT WITH (FORMAT csv, HEADER true)");
        assertTrue(copy.isWithKeyword());
        copy.setWithKeyword(false);
        assertEquals("COPY users TO STDOUT (FORMAT csv, HEADER true)", copy.toString());
        assertFalse(parse(copy.toString()).isWithKeyword());
        copy.withWithKeyword(true);
        assertRoundTrip(copy);
        copy.setOptions(null);
        assertEquals("COPY users TO STDOUT WITH", copy.toString());
        assertRoundTrip(copy);
    }

    @Test
    void passesQueryExpressionsToTheConfiguredDeparser() throws JSQLParserException {
        CopyStatement copy = parse("COPY (SELECT 7 FROM users WHERE id > 8) "
                + "TO STDOUT WITH (FORMAT csv)");
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        copy.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);
        assertEquals("COPY (SELECT 107 FROM users WHERE id > 108) TO STDOUT WITH (FORMAT csv)",
                buffer.toString());
        assertRoundTrip(parse(buffer.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WITH ()", "WITH (FORMAT csv,)", "WITH (,FORMAT csv)",
            "WITH FORMAT csv", "WITH (FORMAT csv,, HEADER true)", "WITH (FORCE_QUOTE (email)"})
    void rejectsMalformedWithOptions(String options) {
        assertThrows(JSQLParserException.class, () -> parse("COPY users TO STDOUT " + options));
    }

    private static CopyStatement parse(String sql) throws JSQLParserException {
        return (CopyStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(CopyStatement copy) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        copy.accept(new StatementDeParser(buffer), null);
        assertEquals(copy.toString(), buffer.toString());
        assertEquals(copy.toString(), parse(buffer.toString()).toString());
    }
}
