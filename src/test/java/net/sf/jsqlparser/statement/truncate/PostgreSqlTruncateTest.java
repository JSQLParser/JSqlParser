/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.truncate;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTruncateTest {
    @ParameterizedTest
    @ValueSource(strings = {"t", "ONLY t", "t *", "ONLY (public.t)",
            "ONLY t, public.u *", "t, ONLY u", "ONLY t, ONLY u"})
    void targetsAndOptionalClausesRoundTrip(String targets) throws JSQLParserException {
        for (String identity : new String[] {"", " RESTART IDENTITY", " CONTINUE IDENTITY"}) {
            for (String behavior : new String[] {"", " CASCADE", " RESTRICT"}) {
                for (String tableKeyword : new String[] {"", " TABLE"}) {
                    String sql = "TRUNCATE" + tableKeyword + " " + targets + identity + behavior;
                    Truncate statement = (Truncate) CCJSqlParserUtil.parse(sql);
                    assertEquals(sql, statement.toString());
                    assertEquals(targets.contains(",") ? 2 : 1, statement.getTargets().size());
                    assertEquals(identity.contains("RESTART") ? Truncate.IdentityOption.RESTART
                            : identity.contains("CONTINUE") ? Truncate.IdentityOption.CONTINUE
                                    : null,
                            statement.getIdentityOption());
                    assertEquals(behavior.contains("CASCADE") ? Truncate.DropBehavior.CASCADE
                            : behavior.contains("RESTRICT") ? Truncate.DropBehavior.RESTRICT : null,
                            statement.getDropBehavior());
                    roundTrip(statement);
                    assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
                }
            }
        }
    }

    @Test
    void visitsEveryTableAndKeepsLegacyMutationsLive() throws JSQLParserException {
        Truncate statement = (Truncate) CCJSqlParserUtil.parse(
                "TRUNCATE ONLY a, b *, ONLY c RESTART IDENTITY CASCADE");
        assertEquals(Set.of("a", "b", "c"), new TablesNamesFinder().getTables(statement));
        assertEquals("c", statement.getTable().getName());
        statement.setTable(new Table("x"));
        statement.getTables().set(1, new Table("y"));
        assertTrue(statement.getTargets().get(1).isIncludeDescendants());
        statement.getTargets().get(2).setOnly(false);
        statement.getTables().add(new Table("z"));
        statement.setCascade(false);
        statement.setDropBehavior(Truncate.DropBehavior.RESTRICT);
        assertFalse(statement.getCascade());
        assertEquals("TRUNCATE ONLY a, y *, x, z RESTART IDENTITY RESTRICT", statement.toString());
        assertEquals(Set.of("a", "y", "x", "z"), new TablesNamesFinder().getTables(statement));
        statement.getTables().remove(2);
        roundTrip(statement);
        statement.setTables(statement.getTables());
        assertEquals(3, statement.getTargets().size());
        statement.setTables(List.of(new Table("replacement")));
        assertTrue(statement.isOnly());
        assertEquals("replacement", statement.getTable().getName());
    }

    @Test
    void lockAndTruncateShareRelationTargets() throws JSQLParserException {
        LockStatement lock = (LockStatement) CCJSqlParserUtil.parse(
                "LOCK TABLE ONLY (public.t), u * IN ACCESS SHARE MODE");
        assertEquals(LockStatement.Scope.ONLY, lock.getTargets().get(0).getScope());
        assertTrue(lock.getTargets().get(0).isParenthesized());
        assertEquals(LockStatement.Scope.INCLUDING_DESCENDANTS,
                lock.getTargets().get(1).getScope());
        lock.getTargets().get(0).setScope(LockStatement.Scope.DEFAULT);
        assertFalse(lock.getTargets().get(0).isParenthesized());
        roundTrip(lock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ONLY t *", "(t)", "t RESTART", "t CONTINUE",
            "t RESTART IDENTITY CONTINUE IDENTITY", "t CASCADE RESTRICT", "t,",
            "t RESTRICT RESTART IDENTITY"})
    void rejectsMalformedTargetsAndClauses(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("TRUNCATE " + sql));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
