/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatementIdentifierTest {
    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"POSTGRESQL", "MYSQL"})
    void statementIsAnUnquotedIdentifier(Dialect dialect) throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE statement (statement INT)", p -> p.withDialect(dialect));
        assertEquals("statement", table.getTable().getName());
        assertEquals("statement", table.getColumnDefinitions().get(0).getColumnName());
        for (String sql : new String[] {"SELECT statement FROM statement",
                "ALTER TABLE statement ADD COLUMN statement INT"}) {
            Statement statement = CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
            StringBuilder buffer = new StringBuilder();
            statement.accept(new StatementDeParser(buffer), null);
            assertEquals(sql, buffer.toString());
        }
    }

    @Test
    void triggerOrientationRemainsAKeyword() throws JSQLParserException {
        CreateTrigger trigger = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE TRIGGER tr AFTER INSERT ON t FOR EACH STATEMENT EXECUTE FUNCTION f()",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(CreateTrigger.Orientation.STATEMENT, trigger.getOrientation());
        assertTrue(trigger.toString().contains("FOR EACH STATEMENT"));
    }
}
