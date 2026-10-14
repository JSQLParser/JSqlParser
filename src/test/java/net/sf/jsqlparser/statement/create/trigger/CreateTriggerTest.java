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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE DEFINER = `root`@`localhost` TRIGGER IF NOT EXISTS trg2 AFTER UPDATE ON t1 "
                    + "FOR EACH ROW FOLLOWS trg INSERT INTO audit (id) VALUES (NEW.id)",
            "CREATE TRIGGER IF NOT EXISTS trg2 AFTER UPDATE ON t1 FOR EACH ROW "
                    + "INSERT INTO audit (id) VALUES (NEW.id)",
            "CREATE OR REPLACE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW SET NEW.a = 1",
            "CREATE OR REPLACE DEFINER = CURRENT_USER TRIGGER trg BEFORE INSERT ON t1 "
                    + "FOR EACH ROW SET NEW.a = 1",
            "CREATE TRIGGER trg3 BEFORE UPDATE ON t1 FOR EACH ROW "
                    + "BEGIN IF NEW.a < 0 THEN SET NEW.a = 0; END IF; END",
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW lbl: BEGIN "
                    + "DECLARE x INT DEFAULT 0; WHILE x < 3 DO SET x = x + 1; END WHILE; "
                    + "LEAVE lbl; END lbl",
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW "
                    + "IF NEW.a IS NULL THEN SET NEW.a = 'a.b'; END IF",
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW CASE WHEN NEW.a > 0 THEN "
                    + "SET NEW.b = 1; ELSE SET NEW.b = CASE NEW.c WHEN 1 THEN 2 END; END CASE",
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW BEGIN END"})
    void parsesMySqlTriggerForms(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> consumer : List.<Consumer<CCJSqlParser>>of(
                parser -> parser.withDialect(Dialect.MYSQL),
                parser -> parser.withDialect(Dialect.MARIADB), parser -> {
                })) {
            CreateTrigger trigger = assertInstanceOf(CreateTrigger.class,
                    assertSqlCanBeParsedAndDeparsed(sql, false, consumer));
            StringBuilder deparsed = new StringBuilder();
            trigger.accept(new StatementDeParser(deparsed));
            assertEquals(sql, deparsed.toString());
            assertEquals(sql, CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());
            Statements script = CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", consumer);
            assertEquals(2, script.size());
            assertEquals(sql, script.get(0).toString());
        }
    }

    @Test
    void parsesIfNotExistsAndOrReplace() throws JSQLParserException {
        CreateTrigger trigger = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE DEFINER = CURRENT_USER TRIGGER IF NOT EXISTS trg AFTER UPDATE ON t1 "
                        + "FOR EACH ROW INSERT INTO audit (id) VALUES (NEW.id)");
        assertTrue(trigger.isIfNotExists());
        assertFalse(trigger.isOrReplace());
        assertEquals("trg", trigger.getTrigger().getFullyQualifiedName());
        assertEquals("CURRENT_USER", trigger.getDefiner().getUser().getValue());

        trigger.setIfNotExists(false);
        trigger.setOrReplace(true);
        assertEquals("CREATE OR REPLACE DEFINER = CURRENT_USER TRIGGER trg AFTER UPDATE ON t1 "
                + "FOR EACH ROW INSERT INTO audit (id) VALUES (NEW.id)", trigger.toString());

        CreateTrigger replaced = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE OR REPLACE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW SET NEW.a = 1",
                parser -> parser.withDialect(Dialect.MARIADB));
        assertTrue(replaced.isOrReplace());
        assertFalse(replaced.isIfNotExists());

        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "CREATE OR REPLACE TRIGGER IF NOT EXISTS trg BEFORE INSERT ON t1 "
                        + "FOR EACH ROW SET NEW.a = 1"));
    }

    @Test
    void keepsStatementBlocksStructured() throws JSQLParserException {
        String sql = "CREATE TRIGGER trg AFTER INSERT ON t1 FOR EACH ROW BEGIN "
                + "INSERT INTO audit (id) VALUES (NEW.id); "
                + "UPDATE counters SET n = CASE WHEN NEW.a > 0 THEN n + 1 ELSE n END; "
                + "DELETE FROM pending WHERE id = NEW.id; END;";
        CreateTrigger trigger = (CreateTrigger) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL));
        Block body = assertInstanceOf(Block.class, trigger.getBody());
        assertEquals(3, body.getStatements().size());
        assertInstanceOf(Insert.class, body.getStatements().get(0));
        // The semicolon after END ends the CREATE TRIGGER statement.
        assertFalse(body.hasSemicolonAfterEnd());
        assertEquals(Set.of("t1", "audit", "counters", "pending"),
                new HashSet<>(new TablesNamesFinder().getTables((Statement) trigger)));
    }

    @Test
    void keepsProceduralBodiesAsTokens() throws JSQLParserException {
        CreateTrigger trigger = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE TRIGGER trg3 BEFORE UPDATE ON t1 FOR EACH ROW "
                        + "BEGIN IF NEW.a < 0 THEN SET NEW.a = 0; END IF; END",
                parser -> parser.withDialect(Dialect.MYSQL));
        UnsupportedStatement body = assertInstanceOf(UnsupportedStatement.class, trigger.getBody());
        assertEquals("BEGIN IF NEW.a < 0 THEN SET NEW.a = 0; END IF; END", body.toString());
        assertEquals(Set.of("t1"),
                new HashSet<>(new TablesNamesFinder().getTables((Statement) trigger)));
    }

    @Test
    void splitsScriptsAfterCompoundBodies() throws JSQLParserException {
        String script = "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW "
                + "BEGIN SET NEW.a = 1; SET NEW.b = 2; END;\n"
                + "CREATE TABLE b (y INT);\n"
                + "CREATE TRIGGER trg3 BEFORE UPDATE ON t1 FOR EACH ROW "
                + "BEGIN IF NEW.a < 0 THEN SET NEW.a = 0; END IF; END;\n"
                + "CREATE TABLE c (z INT);";
        Statements statements = CCJSqlParserUtil.parseStatements(script,
                parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(4, statements.size());
        assertInstanceOf(Block.class, ((CreateTrigger) statements.get(0)).getBody());
        assertInstanceOf(CreateTable.class, statements.get(1));
        assertInstanceOf(UnsupportedStatement.class, ((CreateTrigger) statements.get(2)).getBody());
        assertInstanceOf(CreateTable.class, statements.get(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW BEGIN IF NEW.a < 0 THEN SET NEW.a = 0;",
            "CREATE TRIGGER trg BEFORE INSERT ON t1 FOR EACH ROW BEGIN SET NEW.a = 1"})
    void rejectsUnterminatedCompoundBodies(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }
}
