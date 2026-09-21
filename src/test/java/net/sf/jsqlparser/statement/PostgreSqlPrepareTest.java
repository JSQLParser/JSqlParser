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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class PostgreSqlPrepareTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "PREPARE p(bigint) AS SELECT * FROM users WHERE id = $1",
            "PREPARE p(bigint[], text) AS SELECT * FROM users WHERE id = ANY($1) AND name = $2",
            "PREPARE p(pg_catalog.int4, numeric(10, 2)) AS SELECT $1 + $2",
            "PREPARE \"prepared query\"(timestamp with time zone, double precision) AS SELECT $1, $2",
            "PREPARE p(unknown) AS SELECT $1::text",
            "PREPARE p(bigint, text) AS INSERT INTO users(id, name) VALUES ($1, $2)",
            "PREPARE p(text, bigint) AS UPDATE users SET name = $1 WHERE id = $2",
            "PREPARE p(bigint) AS DELETE FROM users WHERE id = $1",
            "PREPARE p AS SELECT * FROM users WHERE id = $1"
    })
    void preservesDeclaredTypesAndNestedStatements(String sql) throws JSQLParserException {
        PrepareStatement prepare = parse(sql);
        assertRoundTrip(prepare);
        assertEquals(prepare.toString(), CCJSqlParserUtil.parse(prepare.toString()).toString());
    }

    @Test
    void exposesMutableTypesAndRetainsInferredForm() throws JSQLParserException {
        PrepareStatement prepare = parse("PREPARE p(bigint, text) AS SELECT $1, $2");
        assertEquals(2, prepare.getParameterTypes().size());
        assertEquals("bigint", prepare.getParameterTypes().get(0).getBaseTypeName());
        prepare.getParameterTypes().get(0).setDataType("integer");
        assertEquals("PREPARE p(integer, text) AS SELECT $1, $2", prepare.toString());
        assertRoundTrip(prepare);
        prepare.setParameterTypes(null);
        assertEquals("PREPARE p AS SELECT $1, $2", prepare.toString());
        assertNull(parse(prepare.toString()).getParameterTypes());
        assertRoundTrip(new PrepareStatement("p", prepare.getStatement()));
    }

    @Test
    void passesNestedExpressionsToTheConfiguredDeparser() throws JSQLParserException {
        PrepareStatement prepare =
                parse("PREPARE p(bigint) AS SELECT $1 + 7 FROM users WHERE id > 8");
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        prepare.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);
        assertEquals("PREPARE p(bigint) AS SELECT $1 + 107 FROM users WHERE id > 108",
                buffer.toString());
        assertRoundTrip(parse(buffer.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE p() AS SELECT 1", "PREPARE p(bigint,) AS SELECT $1",
            "PREPARE p(bigint text) AS SELECT $1", "PREPARE p(bigint) SELECT $1"})
    void rejectsMalformedTypeLists(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static PrepareStatement parse(String sql) throws JSQLParserException {
        return (PrepareStatement) CCJSqlParserUtil.parse(sql,
                p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(PrepareStatement prepare) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        prepare.accept(new StatementDeParser(buffer), null);
        assertEquals(prepare.toString(), buffer.toString());
        assertEquals(prepare.toString(), parse(buffer.toString()).toString());
    }
}
