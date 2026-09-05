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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;

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
}
