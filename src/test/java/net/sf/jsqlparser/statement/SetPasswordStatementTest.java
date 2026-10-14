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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class SetPasswordStatementTest {

    private static SetPasswordStatement parse(String sql, Dialect dialect)
            throws JSQLParserException {
        SetPasswordStatement statement = assertInstanceOf(SetPasswordStatement.class, TestUtils
                .assertSqlCanBeParsedAndDeparsed(sql, false,
                        parser -> parser.withDialect(dialect)));
        assertEquals(sql, assertInstanceOf(SetPasswordStatement.class, CCJSqlParserUtil
                .parse(statement.toString(), parser -> parser.withDialect(dialect))).toString());
        return statement;
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "MARIADB"})
    void setsPasswordForAccount(Dialect dialect) throws JSQLParserException {
        SetPasswordStatement statement = parse("SET PASSWORD FOR 'app'@'%' = 'x'", dialect);

        assertEquals("app", statement.getAccount().getUser().getValue());
        assertEquals("%", statement.getAccount().getHost().getValue());
        assertEquals("x", ((StringValue) statement.getPassword()).getValue());
        assertFalse(statement.isRandom());

        SetPasswordStatement current = parse("SET PASSWORD = 'x'", dialect);
        assertNull(current.getAccount());
    }

    @Test
    void generatesRandomPasswordAndRetainsCurrent() throws JSQLParserException {
        SetPasswordStatement statement = parse(
                "SET PASSWORD FOR app TO RANDOM REPLACE 'old' RETAIN CURRENT PASSWORD",
                Dialect.MYSQL);

        assertTrue(statement.isRandom());
        assertNull(statement.getPassword());
        assertEquals("old", statement.getCurrentCredential().getValue());
        assertTrue(statement.isRetainCurrentPassword());

        assertTrue(parse("SET PASSWORD TO RANDOM", Dialect.MYSQL).isRandom());
        assertTrue(parse("SET PASSWORD = 'new' REPLACE 'old'", Dialect.MYSQL)
                .getCurrentCredential() != null);
    }

    @Test
    void keepsMariaDbPasswordFunction() throws JSQLParserException {
        SetPasswordStatement statement =
                parse("SET PASSWORD FOR 'app'@'%' = PASSWORD('x')", Dialect.MARIADB);

        assertEquals("PASSWORD", ((Function) statement.getPassword()).getName());
    }

    @Test
    void keepsGenericAssignmentWithoutMySqlDialect() throws JSQLParserException {
        for (String sql : List.of("SET PASSWORD = 'x'", "SET PASSWORD TO RANDOM")) {
            assertInstanceOf(SetStatement.class, CCJSqlParserUtil.parse(sql), sql);
            assertInstanceOf(SetStatement.class,
                    CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL)),
                    sql);
        }
        // FOR names an account and is typed in every configuration
        assertInstanceOf(SetPasswordStatement.class, TestUtils
                .assertSqlCanBeParsedAndDeparsed("SET PASSWORD FOR 'app'@'%' = 'x'", false));
    }

    @Test
    void buildsAndDeparses() {
        SetPasswordStatement statement = new SetPasswordStatement()
                .withAccount(new UserAccount().withUser(new StringValue("app")))
                .withPassword(new StringValue("new")).withRetainCurrentPassword(true);

        assertDeparse(statement, "SET PASSWORD FOR 'app' = 'new' RETAIN CURRENT PASSWORD");
        assertDeparse(statement.withRandom(true).withPassword(null),
                "SET PASSWORD FOR 'app' TO RANDOM RETAIN CURRENT PASSWORD");
    }

    @Test
    void reportsSchemaChangeWithoutTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SET PASSWORD FOR app = 'x'");

        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(TablesNamesFinder.findTables(statement.toString())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET PASSWORD FOR app", "SET PASSWORD = 'x' REPLACE",
            "SET PASSWORD TO 'x'", "SET PASSWORD FOR app TO RANDOM RETAIN PASSWORD",
            "SET PASSWORD FOR app IDENTIFIED BY 'x' = 'y'"})
    void rejectsSyntaxOutsideTheStatementDefinition(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }
}
