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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.DiscardStatement.Target;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DiscardStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final List<Consumer<CCJSqlParser>> POSTGRESQL_AND_NO_DIALECT =
            Arrays.asList(POSTGRESQL, parser -> {
            });

    @ParameterizedTest
    @EnumSource(Target.class)
    void parsesEveryTargetWithPostgreSqlAndWithoutDialect(Target target)
            throws JSQLParserException {
        String sql = "DISCARD " + target.name();
        for (Consumer<CCJSqlParser> mode : POSTGRESQL_AND_NO_DIALECT) {
            DiscardStatement discard = assertInstanceOf(DiscardStatement.class,
                    assertSqlCanBeParsedAndDeparsed(sql, false, mode));
            assertEquals(target, discard.getTarget());
        }
    }

    @Test
    void keepsTheSpellingOfTheTarget() throws JSQLParserException {
        assertEquals("DISCARD TEMP", CCJSqlParserUtil.parse("discard temp", POSTGRESQL).toString());
        assertEquals("DISCARD TEMPORARY",
                CCJSqlParserUtil.parse("Discard Temporary", POSTGRESQL).toString());
        assertEquals("DISCARD SEQUENCES",
                CCJSqlParserUtil.parse("discard sequences", POSTGRESQL).toString());
    }

    @Test
    void buildsAndDeparses() {
        assertDeparse(new DiscardStatement(), "DISCARD ALL");
        DiscardStatement discard = new DiscardStatement(Target.PLANS);
        assertDeparse(discard, "DISCARD PLANS");
        assertDeparse(discard.withTarget(Target.TEMPORARY), "DISCARD TEMPORARY");
        assertThrows(NullPointerException.class, () -> discard.setTarget(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DISCARD", "DISCARD TABLES", "DISCARD ALL PLANS", "DISCARD PLAN",
            "DISCARD SEQUENCE", "DISCARD TEMPORARY TABLES", "DISCARD 'ALL'", "DISCARD (ALL)"})
    void rejectsOtherTargets(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void otherDialectsAndOtherUsesKeepTheirReading() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER}) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("DISCARD ALL",
                    parser -> parser.withDialect(dialect)), dialect.name());
        }
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse("DISCARD ALL",
                parser -> parser.withDialect(Dialect.MYSQL).withUnsupportedStatements(true)));
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse("DISCARD foo",
                parser -> parser.withUnsupportedStatements(true)));
        assertInstanceOf(Alter.class, assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE t DISCARD TABLESPACE", true,
                parser -> parser.withDialect(Dialect.MYSQL)));
        assertSqlCanBeParsedAndDeparsed("SELECT discard FROM discard AS discard", true,
                POSTGRESQL);
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "DISCARD ALL; DISCARD PLANS\nSELECT 1;\nDISCARD PLANS\nDISCARD TEMP;DISCARD SEQUENCES",
                POSTGRESQL);
        assertEquals(6, statements.size());
        assertEquals("DISCARD PLANS", statements.get(1).toString());
        assertEquals("DISCARD PLANS", statements.get(3).toString());
        assertEquals("DISCARD TEMP", statements.get(4).toString());
        assertEquals("DISCARD SEQUENCES", statements.get(5).toString());

        Statements unsupported = CCJSqlParserUtil.parseStatements(
                "IMPORT INTO t FROM LOCAL CSV FILE 'f.csv'; DISCARD ALL",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(2, unsupported.size());
        assertInstanceOf(DiscardStatement.class, unsupported.get(1));
    }

    @Test
    void reachesStatementVisitors() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("DISCARD SEQUENCES", POSTGRESQL);
        String visited = statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(DiscardStatement discard, S context) {
                return discard.getTarget().name();
            }
        }, null);
        assertEquals("SEQUENCES", visited);
        assertThat(new TablesNamesFinder<>().getTables(statement)).isEmpty();
    }

    @Test
    void modifiesTheSessionAndMayDropTemporaryTables() throws JSQLParserException {
        StatementFeatures plans = CCJSqlParserUtil.parse("DISCARD PLANS").getFeatures();
        assertThat(plans.getCertain()).containsExactly(StmtFeature.MODIFIES_SESSION);
        assertThat(plans.getUncertain()).isEmpty();

        for (String sql : new String[] {"DISCARD ALL", "DISCARD TEMP", "DISCARD TEMPORARY"}) {
            StatementFeatures features = CCJSqlParserUtil.parse(sql).getFeatures();
            assertThat(features.getCertain()).containsExactly(StmtFeature.MODIFIES_SESSION);
            assertThat(features.getUncertain()).containsExactly(StmtFeature.MODIFIES_SCHEMA);
        }
        assertTrue(new DiscardStatement(Target.ALL).isDroppingTemporaryTables());
        assertFalse(new DiscardStatement(Target.SEQUENCES).isDroppingTemporaryTables());
    }
}
