/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.MySqlDefiner;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.stream.Stream;

class MySqlAccountNameTest {

    private static final Consumer<CCJSqlParser> MYSQL = p -> p.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = p -> {
    };

    /** SQL spelling, user value, user quote, host value, host quote. */
    static Stream<Arguments> accounts() {
        return Stream.of(
                Arguments.of("app", "app", "", null, null),
                Arguments.of("app@localhost", "app", "", "localhost", ""),
                Arguments.of("app@'%'", "app", "", "%", "'"),
                Arguments.of("app@'10.0.0.%'", "app", "", "10.0.0.%", "'"),
                Arguments.of("'app'@localhost", "app", "'", "localhost", ""),
                Arguments.of("'app'@'%'", "app", "'", "%", "'"),
                Arguments.of("`app`@`%`", "app", "`", "%", "`"),
                Arguments.of("\"app\"@\"%\"", "app", "\"", "%", "\""),
                Arguments.of("'app'", "app", "'", null, null),
                Arguments.of("`my app`@'%'", "my app", "`", "%", "'"));
    }

    @ParameterizedTest
    @MethodSource("accounts")
    void createUserSplitsEveryAccountSpelling(String account, String user, String userQuote,
            String host, String hostQuote) throws JSQLParserException {
        for (Consumer<CCJSqlParser> mode : java.util.Arrays.asList(MYSQL, NO_DIALECT)) {
            String sql = "CREATE USER " + account + " IDENTIFIED BY 'x'";
            CreateUser createUser = assertInstanceOf(CreateUser.class,
                    assertSqlCanBeParsedAndDeparsed(sql, true, mode));
            assertEquals(sql, createUser.toString());
            assertEquals(sql, deparse(createUser));
            assertPart(createUser.getAccounts().get(0).getUser(), user, userQuote);
            assertPart(createUser.getAccounts().get(0).getHost(), host, hostQuote);
        }
    }

    @ParameterizedTest
    @MethodSource("accounts")
    void definerSplitsEveryAccountSpelling(String account, String user, String userQuote,
            String host, String hostQuote) throws JSQLParserException {
        String view = "CREATE DEFINER = " + account + " VIEW v AS SELECT 1";
        CreateView createView = assertInstanceOf(CreateView.class,
                assertSqlCanBeParsedAndDeparsed(view, true, MYSQL));
        MySqlDefiner definer = createView.getMySqlOptions().getDefiner();
        assertPart(definer.getUser(), user, userQuote);
        assertPart(definer.getHost(), host, hostQuote);

        String trigger = "CREATE DEFINER = " + account
                + " TRIGGER trg BEFORE INSERT ON t FOR EACH ROW SET NEW.a = 1";
        CreateTrigger createTrigger = assertInstanceOf(CreateTrigger.class,
                assertSqlCanBeParsedAndDeparsed(trigger, true, MYSQL));
        assertEquals(trigger, createTrigger.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE USER app@", "CREATE USER @host", "CREATE USER 'app'@"})
    void rejectsMalformedAccounts(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @org.junit.jupiter.api.Test
    void keepsDoubleQuotesWithoutDialect() throws JSQLParserException {
        // before, the double quotes were dropped and the account rendered as app@%
        String sql = "CREATE USER \"app\"@\"%\" IDENTIFIED BY 'x'";
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        assertEquals(sql, CCJSqlParserUtil.parse(sql, MYSQL).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP USER IF EXISTS CURRENT_USER()",
            "RENAME USER CURRENT_USER() TO app2", "SET PASSWORD FOR CURRENT_USER() = 'x'",
            "GRANT 'reader' TO CURRENT_USER()"})
    void userListsTakeCurrentUserWithParentheses(String sql) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL);
        assertEquals(sql, CCJSqlParserUtil.parse(sql, MYSQL).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE ROLE CURRENT_USER()", "DROP ROLE CURRENT_USER()",
            "SET ROLE CURRENT_USER()", "SET DEFAULT ROLE ALL TO CURRENT_USER()",
            "GRANT CURRENT_USER() TO app", "ALTER USER app DEFAULT ROLE CURRENT_USER()"})
    void roleListsRejectCurrentUserWithParentheses(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @org.junit.jupiter.api.Test
    void definerCurrentUserIsUnchanged() throws JSQLParserException {
        CreateView view = assertInstanceOf(CreateView.class, assertSqlCanBeParsedAndDeparsed(
                "CREATE DEFINER = CURRENT_USER() VIEW v AS SELECT 1", true, MYSQL));
        assertNull(view.getMySqlOptions().getDefiner().getHost());
    }

    private static void assertPart(StringValue part, String value, String quote) {
        if (value == null) {
            assertNull(part);
            return;
        }
        assertEquals(value, part.getValue());
        assertEquals(quote, part.getQuoteStr());
    }

    private static String deparse(CreateUser createUser) {
        StringBuilder builder = new StringBuilder();
        createUser.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }
}
