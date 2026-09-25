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
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.create.view.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlViewOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"ALGORITHM = MERGE", "ALGORITHM = TEMPTABLE",
            "ALGORITHM = UNDEFINED", "DEFINER = CURRENT_USER", "DEFINER = CURRENT_USER()",
            "DEFINER = 'user'@'localhost'", "DEFINER = `user`@`localhost`",
            "SQL SECURITY INVOKER", "SQL SECURITY DEFINER",
            "ALGORITHM = MERGE DEFINER = CURRENT_USER SQL SECURITY INVOKER"})
    void createAndAlterSharePrefix(String options) throws JSQLParserException {
        for (String operation : new String[] {"CREATE", "CREATE OR REPLACE", "ALTER"}) {
            String sql = operation + " " + options + " VIEW v AS SELECT id FROM t";
            Statement statement = parse(sql);
            assertEquals(sql, statement.toString());
            assertRoundTrip(statement);
            assertTrue(new TablesNamesFinder().getTables(statement).contains("t"));
            assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                    p -> p.withDialect(Dialect.MYSQL)).size());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECK OPTION", "LOCAL CHECK OPTION", "CASCADED CHECK OPTION"})
    void checkOptionTailIsShared(String tail) throws JSQLParserException {
        for (String operation : new String[] {"CREATE", "ALTER"}) {
            String sql =
                    operation + " SQL SECURITY INVOKER VIEW v AS SELECT id FROM t WITH " + tail;
            Statement statement = parse(sql);
            assertEquals(sql, statement.toString());
            assertRoundTrip(statement);
        }
    }

    @Test
    void optionsAndDefinerAreMutable() throws JSQLParserException {
        CreateView view = (CreateView) parse(
                "CREATE ALGORITHM=MERGE DEFINER=CURRENT_USER() VIEW v AS SELECT 1");
        MySqlViewOptions options = view.getMySqlOptions();
        assertEquals(MySqlViewOptions.Algorithm.MERGE, options.getAlgorithm());
        assertTrue(options.getDefiner().isCurrentUserParentheses());
        options.getDefiner().setUser(new StringValue("owner"));
        options.getDefiner().setHost(new StringValue("localhost"));
        options.setAlgorithm(MySqlViewOptions.Algorithm.TEMPTABLE);
        options.setSecurity(MySqlViewOptions.Security.INVOKER);
        assertEquals(
                "CREATE ALGORITHM = TEMPTABLE DEFINER = 'owner'@'localhost' SQL SECURITY INVOKER VIEW v AS SELECT 1",
                view.toString());
        assertRoundTrip(view);
        view.setMySqlOptions(null);
        assertEquals("CREATE VIEW v AS SELECT 1", view.toString());
    }

    @Test
    void sharedAccountParserPreservesTriggerDispatch() throws JSQLParserException {
        for (String account : new String[] {"CURRENT_USER", "CURRENT_USER()", "'u'@'localhost'"}) {
            CreateTrigger trigger = (CreateTrigger) parse("CREATE DEFINER = " + account
                    + " TRIGGER tr BEFORE INSERT ON t FOR EACH ROW SET @a = 1");
            assertEquals(account, trigger.getDefiner().toString());
            assertRoundTrip(trigger);
        }
    }

    @Test
    void invalidPrefixValuesFail() {
        for (String sql : new String[] {"CREATE ALGORITHM=INVALID VIEW v AS SELECT 1",
                "ALTER SQL SECURITY PUBLIC VIEW v AS SELECT 1"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql));
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
        if (statement instanceof net.sf.jsqlparser.statement.UnsupportedStatement) {
            throw new JSQLParserException("Expected a structured statement");
        }
        return statement;
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}
