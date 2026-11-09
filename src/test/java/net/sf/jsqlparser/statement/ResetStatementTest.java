/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ResetStatementTest {

    @Test
    public void tesResetTZ() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET Time Zone");
    }

    @Test
    public void tesResetAll() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET ALL");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "RESET TIME ZONE | Time Zone",
            "RESET TRANSACTION ISOLATION LEVEL | TRANSACTION ISOLATION LEVEL",
            "RESET SESSION AUTHORIZATION | SESSION AUTHORIZATION",
            "RESET transaction | transaction",
            "RESET session | session",
            "RESET time | time"})
    void readsTheParameterNamesOfShow(String sql, String name) throws JSQLParserException {
        for (ResetStatement reset : new ResetStatement[] {
                (ResetStatement) CCJSqlParserUtil.parse(sql,
                        parser -> parser.withDialect(Dialect.POSTGRESQL)),
                (ResetStatement) CCJSqlParserUtil.parse(sql)}) {
            assertEquals(name, reset.getName());
            assertEquals("RESET " + name, reset.toString());
            assertSqlCanBeParsedAndDeparsed(reset.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESET TRANSACTION ISOLATION", "RESET TRANSACTION LEVEL",
            "RESET SESSION AUTHORIZATION x", "RESET DATE ZONE", "RESET TIME ZONE x"})
    void rejectsIncompletePhrases(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
    }

    @Test
    void timeZoneNeedsTheWordTime() throws JSQLParserException {
        // other date/time keywords before ZONE were read as TIME ZONE
        for (String sql : new String[] {"RESET DATE ZONE", "RESET TIMESTAMP ZONE",
                "SET DATE ZONE 'UTC'"}) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql), sql);
        }
        assertEquals("SET Time Zone 'UTC'",
                CCJSqlParserUtil.parse("SET time zone 'UTC'").toString());
        assertEquals("SET LOCAL Time Zone LOCAL",
                CCJSqlParserUtil.parse("SET LOCAL TIME ZONE LOCAL").toString());
    }

    @Test
    void otherDialectsKeepRejectingThePostgreSqlPhrases() {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.ORACLE}) {
            for (String sql : new String[] {"RESET TRANSACTION ISOLATION LEVEL",
                    "RESET SESSION AUTHORIZATION"}) {
                assertThrows(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                        dialect + " " + sql);
            }
        }
    }

    @Test
    public void testObject() {
        ResetStatement resetStatement = new ResetStatement();
        assertNotNull(resetStatement.getName());

        resetStatement.add("something");
        resetStatement.setName("somethingElse");
        assertEquals("somethingElse", resetStatement.getName());
    }

}
