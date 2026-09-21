/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlLockTest {
    @ParameterizedTest
    @ValueSource(strings = {"ACCESS SHARE", "ROW SHARE", "ROW EXCLUSIVE",
            "SHARE UPDATE EXCLUSIVE", "SHARE", "SHARE ROW EXCLUSIVE", "EXCLUSIVE",
            "ACCESS EXCLUSIVE"})
    void supportsAllPostgreSqlModes(String mode) throws Exception {
        LockStatement lock = parse("LOCK TABLE a, public.b IN " + mode + " MODE NOWAIT");
        assertEquals(mode, lock.getLockMode().getValue());
        assertEquals(2, lock.getTargets().size());
        assertTrue(lock.isNoWait());
        roundTrip(lock);
    }

    @Test
    void preservesScopesNamesAndOmittedKeywords() throws Exception {
        LockStatement lock = parse("LOCK ONLY \"a.b\", public.b * NOWAIT");
        assertFalse(lock.isUseTableKeyword());
        assertNull(lock.getLockMode());
        assertEquals(LockStatement.Scope.ONLY, lock.getTargets().get(0).getScope());
        assertEquals(LockStatement.Scope.INCLUDING_DESCENDANTS,
                lock.getTargets().get(1).getScope());
        assertEquals("\"a.b\"", lock.getTable().getName());
        assertThat(new TablesNamesFinder().getTables(lock))
                .containsExactlyInAnyOrder("\"a.b\"", "public.b");
        roundTrip(lock);
        roundTrip(parse("LOCK TABLE a"));
        roundTrip(parse("LOCK a IN SHARE MODE"));
    }

    @Test
    void legacyTableAccessorsRemainViewsOfTheFirstTarget() throws Exception {
        LockStatement lock = parse("LOCK TABLE ONLY a, b *");
        lock.getTargets().get(0).getTable().setName("changed");
        assertEquals("changed", lock.getTable().getName());
        lock.setTable(new Table("replacement"));
        assertEquals(2, lock.getTargets().size());
        assertEquals(LockStatement.Scope.ONLY, lock.getTargets().get(0).getScope());
        assertEquals("LOCK TABLE ONLY replacement, b *", lock.toString());
        roundTrip(lock);
        LockStatement legacy = new LockStatement(new Table("t"), LockMode.Exclusive, false, 5L);
        assertEquals("LOCK TABLE t IN EXCLUSIVE MODE WAIT 5", legacy.toString());
        assertThrows(IllegalStateException.class, () -> legacy.setNoWait(true));
    }

    @Test
    void preservesOracleModeAndWait() throws Exception {
        String sql = "LOCK TABLE t IN SHARE UPDATE MODE WAIT 5";
        LockStatement lock = (LockStatement) CCJSqlParserUtil.parse(sql,
                p -> p.withDialect(Dialect.ORACLE));
        assertEquals(LockMode.ShareUpdate, lock.getLockMode());
        assertEquals(sql, lock.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCK TABLE", "LOCK TABLE a,", "LOCK TABLE a IN ACCESS MODE",
            "LOCK TABLE a IN SHARE UPDATE EXCLUSIVE", "LOCK TABLE ONLY a *",
            "LOCK TABLE a NOWAIT WAIT 1"})
    void rejectsIncompleteAndConflictingClauses(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static LockStatement parse(String sql) throws JSQLParserException {
        return (LockStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(LockStatement statement) throws Exception {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out), null);
        assertEquals(statement.toString(), out.toString());
        assertEquals(statement.toString(), parse(out.toString()).toString());
    }
}
