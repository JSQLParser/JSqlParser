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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.alter.AlterDomain;
import net.sf.jsqlparser.statement.alter.AlterType;
import net.sf.jsqlparser.statement.drop.DropPolicy;
import net.sf.jsqlparser.statement.grant.Revoke;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CascadeBehaviorTest {
    static Stream<String> statements() {
        return Stream.of(
                "ALTER DOMAIN amount DROP CONSTRAINT positive",
                "ALTER TYPE item RENAME ATTRIBUTE old_name TO new_name",
                "ALTER TYPE item ADD ATTRIBUTE code text",
                "ALTER TYPE item DROP ATTRIBUTE price",
                "ALTER TYPE item ALTER ATTRIBUTE price SET DATA TYPE bigint",
                "DROP POLICY visible ON items",
                "TRUNCATE TABLE items",
                "REVOKE SELECT ON items FROM reader");
    }

    @ParameterizedTest
    @MethodSource("statements")
    void parsesSharedEnumAndPreservesOmittedClauses(String sql) throws JSQLParserException {
        for (boolean postgres : new boolean[] {false, true}) {
            for (CascadeBehavior behavior : new CascadeBehavior[] {
                    null, CascadeBehavior.CASCADE, CascadeBehavior.RESTRICT}) {
                String expectedSql = sql + (behavior == null ? "" : " " + behavior);
                Statement statement = parse(expectedSql, postgres);
                assertSame(behavior, getBehavior(statement));
                assertRoundTrip(statement, expectedSql, behavior, postgres);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("statements")
    void sharedEnumSettersChangeAndRemoveClauses(String sql) throws JSQLParserException {
        for (boolean postgres : new boolean[] {false, true}) {
            Statement statement = parse(sql + " CASCADE", postgres);
            for (CascadeBehavior behavior : new CascadeBehavior[] {
                    CascadeBehavior.RESTRICT, null, CascadeBehavior.CASCADE}) {
                setBehavior(statement, behavior);
                assertSame(behavior, getBehavior(statement));
                assertRoundTrip(statement, sql + (behavior == null ? "" : " " + behavior),
                        behavior, postgres);
            }
        }
    }

    @Test
    void truncateBooleanSetterStillClearsRatherThanSelectsRestrict() throws JSQLParserException {
        Truncate statement = (Truncate) parse("TRUNCATE TABLE items RESTRICT", true);
        assertFalse(statement.getCascade());
        statement.setCascade(true);
        assertTrue(statement.getCascade());
        assertSame(CascadeBehavior.CASCADE, statement.getDropBehavior());
        assertRoundTrip(statement, "TRUNCATE TABLE items CASCADE", CascadeBehavior.CASCADE, true);

        statement.setCascade(false);
        assertFalse(statement.getCascade());
        assertNull(statement.getDropBehavior());
        assertRoundTrip(statement, "TRUNCATE TABLE items", null, true);
    }

    private static Statement parse(String sql, boolean postgres) throws JSQLParserException {
        return postgres
                ? CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL))
                : CCJSqlParserUtil.parse(sql);
    }

    private static void assertRoundTrip(Statement statement, String expectedSql,
            CascadeBehavior behavior, boolean postgres) throws JSQLParserException {
        assertEquals(expectedSql, statement.toString());
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(expectedSql, deparsed.toString());
        Statement reparsed = parse(deparsed.toString(), postgres);
        assertSame(behavior, getBehavior(reparsed));
        assertEquals(expectedSql, reparsed.toString());
    }

    private static CascadeBehavior getBehavior(Statement statement) {
        if (statement instanceof AlterDomain) {
            return ((AlterDomain) statement).getBehavior();
        } else if (statement instanceof AlterType) {
            AlterType type = (AlterType) statement;
            return type.getAttributeChanges().isEmpty() ? type.getBehavior()
                    : type.getAttributeChanges().get(0).getBehavior();
        } else if (statement instanceof DropPolicy) {
            return ((DropPolicy) statement).getBehavior();
        } else if (statement instanceof Truncate) {
            return ((Truncate) statement).getDropBehavior();
        } else if (statement instanceof Revoke) {
            return ((Revoke) statement).getBehavior();
        }
        throw new IllegalArgumentException("Unsupported statement: " + statement);
    }

    private static void setBehavior(Statement statement, CascadeBehavior behavior) {
        if (statement instanceof AlterDomain) {
            ((AlterDomain) statement).setBehavior(behavior);
        } else if (statement instanceof AlterType) {
            AlterType type = (AlterType) statement;
            if (type.getAttributeChanges().isEmpty()) {
                type.setBehavior(behavior);
            } else {
                type.getAttributeChanges().get(0).setBehavior(behavior);
            }
        } else if (statement instanceof DropPolicy) {
            ((DropPolicy) statement).setBehavior(behavior);
        } else if (statement instanceof Truncate) {
            ((Truncate) statement).setDropBehavior(behavior);
        } else if (statement instanceof Revoke) {
            ((Revoke) statement).setBehavior(behavior);
        } else {
            throw new IllegalArgumentException("Unsupported statement: " + statement);
        }
    }
}
