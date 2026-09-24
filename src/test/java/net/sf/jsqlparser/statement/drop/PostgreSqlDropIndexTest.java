/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlDropIndexTest {
    @ParameterizedTest
    @ValueSource(strings = {"DROP INDEX CONCURRENTLY ix",
            "DROP INDEX CONCURRENTLY IF EXISTS ix",
            "DROP INDEX CONCURRENTLY IF EXISTS public.ix RESTRICT"})
    void concurrencyIsIndependentOfTarget(String sql) throws JSQLParserException {
        Drop drop = (Drop) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
        assertTrue(drop.isConcurrently());
        assertEquals(sql.contains("IF EXISTS"), drop.isIfExists());
        assertEquals("ix", drop.getName().getName());
        assertEquals(sql, drop.toString());
        drop.setName(new Table("new_ix"));
        String expected = sql.replace("public.ix", "ix").replace("ix", "new_ix");
        assertEquals(expected, drop.toString());
        StringBuilder buffer = new StringBuilder();
        drop.accept(new StatementDeParser(buffer), null);
        assertEquals(expected, buffer.toString());
        Drop reparsed = (Drop) CCJSqlParserUtil.parse(expected);
        assertEquals("new_ix", reparsed.getName().getName());
        assertTrue(reparsed.isConcurrently());
        assertEquals(2, CCJSqlParserUtil.parseStatements(expected + "; SELECT 1").size());
        drop.setConcurrently(false);
        assertEquals(expected.replace("CONCURRENTLY ", ""), drop.toString());
    }

    @Test
    void ordinaryAndQuotedConcurrentNamesArePreserved() throws JSQLParserException {
        for (String sql : new String[] {"DROP TABLE concurrently", "DROP INDEX \"concurrently\"",
                "DROP INDEX ix1, ix2 CASCADE", "DROP INDEX ix ON t ALGORITHM = INPLACE"}) {
            Drop drop = (Drop) CCJSqlParserUtil.parse(sql);
            assertFalse(drop.isConcurrently());
            assertEquals(sql, drop.toString());
        }
    }
}
