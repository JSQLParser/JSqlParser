/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.role;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlCreateRoleTest {

    private static CreateRole parse(String sql, Dialect dialect) throws JSQLParserException {
        CreateRole role = assertInstanceOf(CreateRole.class, TestUtils
                .assertSqlCanBeParsedAndDeparsed(sql, false,
                        parser -> parser.withDialect(dialect)));
        assertEquals(sql, CCJSqlParserUtil
                .parse(role.toString(), parser -> parser.withDialect(dialect)).toString());
        return role;
    }

    private static List<String> names(CreateRole role) {
        return role.getRoles().stream().map(UserAccount::toString).collect(Collectors.toList());
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "MARIADB"})
    void parsesAccountStyleRoleNames(Dialect dialect) throws JSQLParserException {
        CreateRole role = parse("CREATE ROLE 'reader', 'writer'@'localhost'", dialect);

        assertEquals(List.of("'reader'", "'writer'@'localhost'"), names(role));
        assertEquals("localhost", role.getRoles().get(1).getHost().getValue());
        assertNull(role.getName());
        assertFalse(role.isIfNotExists());
        assertTrue(role.getOptions().isEmpty());
    }

    @Test
    void parsesIfNotExists() throws JSQLParserException {
        CreateRole role = parse("CREATE ROLE IF NOT EXISTS app_read, app_write", Dialect.MYSQL);

        assertTrue(role.isIfNotExists());
        assertEquals(List.of("app_read", "app_write"), names(role));
        assertInstanceOf(CreateRole.class, TestUtils
                .assertSqlCanBeParsedAndDeparsed("CREATE ROLE IF NOT EXISTS 'reader'", false));
    }

    @Test
    void keepsPostgreSqlRoleName() throws JSQLParserException {
        // a single unquoted name means the same in both databases and keeps getName()
        for (Dialect dialect : List.of(Dialect.MYSQL, Dialect.POSTGRESQL)) {
            CreateRole role = parse("CREATE ROLE reader", dialect);
            assertEquals("reader", role.getName());
            assertTrue(role.getRoles().isEmpty());
        }
        CreateRole role = parse("CREATE ROLE reader WITH LOGIN", Dialect.POSTGRESQL);
        assertEquals(RoleOption.Kind.LOGIN, role.getOptions().get(0).getKind());
    }

    @Test
    void splitsAnUnquotedNameAtHostOnlyWithMySqlDialects() throws JSQLParserException {
        CreateRole mySql = parse("CREATE ROLE r@localhost", Dialect.MYSQL);
        assertEquals("r", mySql.getRoles().get(0).getUser().getValue());
        assertEquals("localhost", mySql.getRoles().get(0).getHost().getValue());

        // without a dialect the lexer's single identifier stays the PostgreSQL role name
        CreateRole role = assertInstanceOf(CreateRole.class,
                TestUtils.assertSqlCanBeParsedAndDeparsed("CREATE ROLE r@localhost", false));
        assertEquals("r@localhost", role.getName());
        assertEquals(List.of("r", "'%'"), List.of(
                parse("CREATE ROLE r@'%'", Dialect.ANSI_SQL).getRoles().get(0).getUser().getValue(),
                parse("CREATE ROLE r@'%'", Dialect.ANSI_SQL).getRoles().get(0).getHost()
                        .toString()));
    }

    @Test
    void buildsAndDeparses() {
        CreateRole role = new CreateRole();
        role.setIfNotExists(true);
        role.setRoles(List.of(new UserAccount().withUser(new StringValue("r1")),
                new UserAccount().withUser(new StringValue("r2")).withHost(new StringValue("%"))));

        assertDeparse(role, "CREATE ROLE IF NOT EXISTS 'r1', 'r2'@'%'");
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE OR REPLACE ROLE 'r'", "CREATE ROLE 'r' WITH LOGIN",
            "CREATE ROLE IF NOT EXISTS", "CREATE ROLE 'r'@'h' IDENTIFIED BY 'x'",
            "CREATE ROLE r1, r2 LOGIN"})
    void rejectsMixedSyntax(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }
}
