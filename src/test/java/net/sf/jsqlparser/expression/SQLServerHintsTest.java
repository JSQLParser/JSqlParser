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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.SQLServerHints.LockHint;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class SQLServerHintsTest {
    @ParameterizedTest
    @EnumSource(LockHint.class)
    void parsesEachLockingHint(LockHint hint) throws Exception {
        Statement statement = parse("SELECT * FROM dbo.jobs WITH (" + hint + ")");
        assertEquals(List.of(hint), hints(statement).getLockHints());
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM dbo.jobs WITH (UPDLOCK, ROWLOCK)",
            "SELECT * FROM [dbo].[jobs] AS j WITH (INDEX ([ix_jobs]), UPDLOCK, ROWLOCK) WHERE j.id = 1",
            "SELECT * FROM jobs WITH (ROWLOCK, READPAST, UPDLOCK) ORDER BY id",
            "SELECT * FROM jobs j WITH (HOLDLOCK) JOIN workers w WITH (ROWLOCK, NOWAIT) ON j.id = w.id",
            "WITH q AS (SELECT * FROM jobs WITH (UPDLOCK, ROWLOCK)) SELECT * FROM q",
            "UPDATE j SET state = 1 FROM jobs j WITH (UPDLOCK, ROWLOCK) WHERE id = 1",
            "DELETE j FROM jobs j WITH (ROWLOCK) WHERE id = 1",
            "DELETE FROM jobs WITH (ROWLOCK) WHERE id = 1",
            "UPDATE jobs WITH (ROWLOCK, UPDLOCK) SET state = 1 WHERE id = 1",
            "SELECT * FROM jobs WITH (NOLOCK)",
            "SELECT * FROM jobs WITH (INDEX (ix_jobs), NOLOCK)"})
    void retainsHintsAcrossAliasesJoinsCtesAndDml(String sql) throws Exception {
        roundTrip(parse(sql));
    }

    @Test
    void exposesTypedHintsWithoutChangingLegacyAccessors() throws Exception {
        Statement statement = parse("SELECT * FROM jobs WITH (updlock, rowlock)");
        SQLServerHints hints = hints(statement);
        assertEquals(List.of(LockHint.UPDLOCK, LockHint.ROWLOCK), hints.getLockHints());
        hints.getLockHints().set(0, LockHint.READPAST);
        hints.setIndexName("ix_jobs");
        assertEquals("SELECT * FROM jobs WITH (INDEX (ix_jobs), READPAST, ROWLOCK)",
                statement.toString());
        assertThat(new TablesNamesFinder().getTables(statement)).containsExactly("jobs");
        roundTrip(statement);

        SQLServerHints legacy = new SQLServerHints();
        assertNull(legacy.getNoLock());
        legacy.withNoLock().withIndexName("ix");
        assertEquals(Boolean.TRUE, legacy.getNoLock());
        assertEquals(" WITH (INDEX (ix), NOLOCK)", legacy.toString());
        legacy.setNoLock(false);
        assertEquals(Boolean.FALSE, legacy.getNoLock());
        assertEquals(" WITH (INDEX (ix))", legacy.toString());
        legacy.withNoLock(null);
        assertNull(legacy.getNoLock());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM jobs WITH ()", "SELECT * FROM jobs WITH (ROWLOCK,)",
            "SELECT * FROM jobs WITH (,UPDLOCK)", "SELECT * FROM jobs WITH (UPDLOCK(1))",
            "SELECT * FROM jobs WITH (ROWLOCK = 1)", "SELECT * FROM jobs WITH (UNKNOWN_HINT)",
            "SELECT * FROM jobs WITH ('ROWLOCK')", "SELECT * FROM jobs WITH (ROWLOCK"})
    void rejectsMalformedOrUnknownHints(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static SQLServerHints hints(Statement statement) {
        return ((Table) ((PlainSelect) statement).getFromItem()).getSqlServerHints();
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.SQLSERVER));
    }

    private static void roundTrip(Statement statement) throws Exception {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
    }
}
