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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

class CreateUserTest {

    @Test
    void parsesAccountAndPasswordAsStructuredValues() throws JSQLParserException {
        String sql = "CREATE USER IF NOT EXISTS 'replicator' IDENTIFIED BY 'replpass'";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, CCJSqlParserUtil.parse(sql));

        assertTrue(createUser.isIfNotExists());
        assertEquals(1, createUser.getAccounts().size());
        UserAccount account = createUser.getAccounts().get(0);
        assertEquals("replicator", account.getUser().getValue());
        assertNull(account.getHost());
        assertEquals(UserAuthentication.Mode.BY_PASSWORD, account.getAuthentication().getMode());
        assertEquals("replpass", account.getAuthentication().getCredential().getValue());
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    void parsesHostAndOptionalAuthentication() throws JSQLParserException {
        String sql = "CREATE USER IF NOT EXISTS 'snapshot'@'%'";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, assertSqlCanBeParsedAndDeparsed(sql));

        UserAccount account = createUser.getAccounts().get(0);
        assertEquals("snapshot", account.getUser().getValue());
        assertEquals("%", account.getHost().getValue());
        assertNull(account.getAuthentication());
    }

    @Test
    void parsesMultipleAccountsAndAuthenticationPlugin() throws JSQLParserException {
        String sql = "CREATE USER alice@localhost IDENTIFIED WITH caching_sha2_password "
                + "BY 'secret', 'service'@'10.%' IDENTIFIED BY RANDOM PASSWORD";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertFalse(createUser.isIfNotExists());
        assertEquals(2, createUser.getAccounts().size());
        UserAuthentication pluginAuthentication =
                createUser.getAccounts().get(0).getAuthentication();
        assertEquals(UserAuthentication.Mode.WITH_PLUGIN_BY_PASSWORD,
                pluginAuthentication.getMode());
        assertEquals("caching_sha2_password", pluginAuthentication.getPlugin());
        assertEquals("secret", pluginAuthentication.getCredential().getValue());
        assertEquals(UserAuthentication.Mode.BY_RANDOM_PASSWORD,
                createUser.getAccounts().get(1).getAuthentication().getMode());
    }
}
