/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.trigger;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.junit.jupiter.api.Test;

class CreateTriggerTest {

    @Test
    void parsesTriggerDefinitionAndSetBody() throws JSQLParserException {
        String sql = "CREATE TRIGGER normalize_name BEFORE INSERT ON customer "
                + "FOR EACH ROW SET NEW.name = UPPER(NEW.name)";
        CreateTrigger trigger =
                assertInstanceOf(CreateTrigger.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals("normalize_name", trigger.getTrigger().getFullyQualifiedName());
        assertEquals(CreateTrigger.Timing.BEFORE, trigger.getTiming());
        assertEquals(CreateTrigger.Event.INSERT, trigger.getEvent());
        assertEquals("customer", trigger.getTable().getFullyQualifiedName());
        assertNull(trigger.getOrder());
        SetStatement body = assertInstanceOf(SetStatement.class, trigger.getBody());
        assertEquals("NEW.name", body.getName().toString());
        assertEquals("UPPER(NEW.name)", body.getExpressions().get(0).toString());
    }

    @Test
    void parsesAfterTriggerWithInsertBody() throws JSQLParserException {
        String sql = "CREATE TRIGGER record_delete AFTER DELETE ON customer FOR EACH ROW "
                + "INSERT INTO audit_log (customer_id) VALUES (OLD.id)";
        CreateTrigger trigger =
                assertInstanceOf(CreateTrigger.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals(CreateTrigger.Timing.AFTER, trigger.getTiming());
        assertEquals(CreateTrigger.Event.DELETE, trigger.getEvent());
        assertInstanceOf(Insert.class, trigger.getBody());
    }

    @Test
    void parsesDefinerWithoutReparsingAccountValues() throws JSQLParserException {
        String sql = "CREATE DEFINER = 'automation'@'localhost' TRIGGER normalize_name "
                + "BEFORE UPDATE ON customer FOR EACH ROW SET NEW.name = UPPER(NEW.name)";
        CreateTrigger trigger =
                assertInstanceOf(CreateTrigger.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals("automation", trigger.getDefiner().getUser().getValue());
        assertEquals("localhost", trigger.getDefiner().getHost().getValue());
    }

    @Test
    void parsesTriggerOrdering() throws JSQLParserException {
        String sql = "CREATE TRIGGER normalize_name_second BEFORE INSERT ON customer "
                + "FOR EACH ROW FOLLOWS normalize_name SET NEW.name = TRIM(NEW.name)";
        CreateTrigger trigger =
                assertInstanceOf(CreateTrigger.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertEquals(CreateTrigger.Order.FOLLOWS, trigger.getOrder());
        assertEquals("normalize_name", trigger.getOtherTrigger().getFullyQualifiedName());
    }
}
