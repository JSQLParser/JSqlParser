/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.event;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.buildSqlString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EventStatementTest {

    @Test
    void parsesCreateEventIntoStructuredProperties() throws JSQLParserException {
        String sql = "CREATE EVENT hourly_rollup ON SCHEDULE EVERY 6 HOUR "
                + "COMMENT 'Refresh aggregate' DO UPDATE app.metrics SET counter = counter + 1";
        CreateEvent event =
                assertInstanceOf(CreateEvent.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals("hourly_rollup", event.getEvent().getFullyQualifiedName());
        assertEquals(EventSchedule.Type.EVERY, event.getSchedule().getType());
        assertEquals(6, assertInstanceOf(LongValue.class, event.getSchedule().getInterval())
                .getValue());
        assertEquals("HOUR", event.getSchedule().getIntervalUnit());
        assertEquals("Refresh aggregate", event.getComment().getValue());
        assertInstanceOf(Update.class, event.getBody());
    }

    @Test
    void parsesRecurringScheduleBounds() throws JSQLParserException {
        String sql = "ALTER EVENT hourly_rollup ON SCHEDULE EVERY 12 HOUR "
                + "STARTS CURRENT_TIMESTAMP + INTERVAL 4 HOUR "
                + "ENDS CURRENT_TIMESTAMP + INTERVAL 2 DAY";
        AlterEvent event =
                assertInstanceOf(AlterEvent.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals(EventSchedule.Type.EVERY, event.getSchedule().getType());
        assertEquals("HOUR", event.getSchedule().getIntervalUnit());
        assertEquals("CURRENT_TIMESTAMP + INTERVAL 4 HOUR",
                event.getSchedule().getStarts().toString());
        assertEquals("CURRENT_TIMESTAMP + INTERVAL 2 DAY",
                event.getSchedule().getEnds().toString());
    }

    @Test
    void parsesOneTimeScheduleAndReplacementBody() throws JSQLParserException {
        String sql = "ALTER EVENT hourly_rollup ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 1 DAY "
                + "DO TRUNCATE TABLE app.metrics";
        AlterEvent event =
                assertInstanceOf(AlterEvent.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals(EventSchedule.Type.AT, event.getSchedule().getType());
        assertEquals("CURRENT_TIMESTAMP + INTERVAL 1 DAY",
                event.getSchedule().getExecuteAt().toString());
        assertInstanceOf(Truncate.class, event.getBody());
    }

    @Test
    void parsesStatusAndRenameOperations() throws JSQLParserException {
        AlterEvent disabled = assertInstanceOf(AlterEvent.class,
                assertSqlCanBeParsedAndDeparsed("ALTER EVENT hourly_rollup DISABLE"));
        assertEquals(EventStatus.DISABLE, disabled.getStatus());
        assertNull(disabled.getRenameTo());

        AlterEvent renamed = assertInstanceOf(AlterEvent.class,
                assertSqlCanBeParsedAndDeparsed(
                        "ALTER EVENT hourly_rollup RENAME TO daily_rollup"));
        assertEquals("daily_rollup", renamed.getRenameTo().getFullyQualifiedName());
    }

    @Test
    void parsesCompletionPolicy() throws JSQLParserException {
        String sql = "CREATE EVENT IF NOT EXISTS one_time_cleanup "
                + "ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 1 DAY "
                + "ON COMPLETION NOT PRESERVE ENABLE DO TRUNCATE TABLE app.staging";
        CreateEvent event =
                assertInstanceOf(CreateEvent.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertFalse(event.getOnCompletionPreserve());
        assertEquals(EventStatus.ENABLE, event.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE DEFINER = `root`@`localhost` EVENT `e_daily` ON SCHEDULE EVERY 1 DAY "
                    + "STARTS CURRENT_TIMESTAMP + INTERVAL 6 HOUR ON COMPLETION NOT PRESERVE "
                    + "ENABLE COMMENT 'Saves total number of sessions then clears the table' "
                    + "DO BEGIN INSERT INTO site_activity.totals (time, total) "
                    + "SELECT CURRENT_TIMESTAMP, COUNT(*) FROM site_activity.sessions; "
                    + "DELETE FROM site_activity.sessions; END",
            "CREATE DEFINER = 'admin'@'%' EVENT IF NOT EXISTS ev ON SCHEDULE EVERY 1 HOUR "
                    + "DO SELECT 1",
            "CREATE DEFINER = CURRENT_USER() EVENT ev ON SCHEDULE AT CURRENT_TIMESTAMP "
                    + "DISABLE ON REPLICA DO SELECT 1",
            "CREATE EVENT ev ON SCHEDULE EVERY 1 DAY DO BEGIN END",
            "CREATE EVENT ev ON SCHEDULE EVERY 1 DAY "
                    + "DO BEGIN IF @a > 0 THEN DELETE FROM t1; END IF; END",
            "ALTER DEFINER = CURRENT_USER EVENT ev ON COMPLETION PRESERVE RENAME TO db2.ev2 "
                    + "ENABLE COMMENT 'moved' DO SELECT 2",
            "ALTER EVENT ev DISABLE ON REPLICA",
            "ALTER EVENT ev DISABLE ON SLAVE",
            "ALTER EVENT ev DO BEGIN DELETE FROM t1; DELETE FROM t2; END"})
    void parsesMySqlEventForms(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> consumer : List.<Consumer<CCJSqlParser>>of(
                parser -> parser.withDialect(Dialect.MYSQL), parser -> {
                })) {
            // a statement block prints one statement per line
            Statement event = assertSqlCanBeParsedAndDeparsed(sql, true, consumer);
            assertInstanceOf(EventStatement.class, event);
            Statements script = CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", consumer);
            assertEquals(2, script.size());
            assertEquals(buildSqlString(sql, true),
                    buildSqlString(script.get(0).toString(), true));
        }
    }

    @Test
    void parsesDefinerAsShowCreateEventPrintsIt() throws JSQLParserException {
        CreateEvent event = assertInstanceOf(CreateEvent.class, CCJSqlParserUtil.parse(
                "CREATE DEFINER=`root`@`localhost` EVENT `ev` ON SCHEDULE EVERY 1 DAY "
                        + "STARTS '2026-01-01 00:00:00' ON COMPLETION NOT PRESERVE ENABLE "
                        + "DO DELETE FROM t1"));

        assertEquals("root", event.getDefiner().getUser().getValue());
        assertEquals("localhost", event.getDefiner().getHost().getValue());
        assertInstanceOf(Delete.class, event.getBody());
        assertEquals("CREATE DEFINER = `root`@`localhost` EVENT `ev` ON SCHEDULE EVERY 1 DAY "
                + "STARTS '2026-01-01 00:00:00' ON COMPLETION NOT PRESERVE ENABLE "
                + "DO DELETE FROM t1", event.toString());

        AlterEvent altered = assertInstanceOf(AlterEvent.class, CCJSqlParserUtil.parse(
                "ALTER DEFINER = 'admin'@'%' EVENT ev ENABLE"));
        assertEquals("admin", altered.getDefiner().getUser().getValue());
        assertEquals("%", altered.getDefiner().getHost().getValue());
        assertEquals(EventStatus.ENABLE, altered.getStatus());
    }

    @Test
    void keepsStatementBlockBodiesStructured() throws JSQLParserException {
        CreateEvent event = assertInstanceOf(CreateEvent.class, CCJSqlParserUtil.parse(
                "CREATE EVENT ev ON SCHEDULE EVERY 1 DAY "
                        + "DO BEGIN DELETE FROM a; DELETE FROM b; END",
                parser -> parser.withDialect(Dialect.MYSQL)));

        Block body = assertInstanceOf(Block.class, event.getBody());
        assertEquals(2, body.getStatements().size());
        assertInstanceOf(Delete.class, body.getStatements().get(0));
        assertEquals(Set.of("a", "b"),
                new HashSet<>(new TablesNamesFinder<>().getTables((Statement) event)));

        AlterEvent altered = assertInstanceOf(AlterEvent.class, CCJSqlParserUtil.parse(
                "ALTER EVENT ev DO BEGIN IF @a > 0 THEN DELETE FROM t1; END IF; END"));
        assertTrue(altered.getBody() instanceof UnsupportedStatement);
    }

    @Test
    void parsesDisableOnReplica() throws JSQLParserException {
        AlterEvent event = assertInstanceOf(AlterEvent.class,
                CCJSqlParserUtil.parse("ALTER EVENT ev DISABLE ON REPLICA"));
        assertEquals(EventStatus.DISABLE_ON_REPLICA, event.getStatus());

        event = assertInstanceOf(AlterEvent.class,
                CCJSqlParserUtil.parse("ALTER EVENT ev DISABLE ON SLAVE"));
        assertEquals(EventStatus.DISABLE_ON_SLAVE, event.getStatus());
    }
}
