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

import static net.sf.jsqlparser.statement.create.user.CreateUserTest.roundTrip;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.alter.AlterUser;
import net.sf.jsqlparser.statement.create.user.UserAuthentication.Mode;
import net.sf.jsqlparser.statement.create.user.UserFactorOperation.Operation;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL 8.4 multi-factor authentication clauses of CREATE USER and ALTER USER. */
class MySqlMultiFactorAuthenticationTest {

    private static List<String> operations(UserAccount account) {
        return account.getFactorOperations().stream()
                .map(operation -> operation.getOperation() + ":" + operation.getFactor())
                .collect(Collectors.toList());
    }

    private static UserAccount alteredAccount(String sql) throws JSQLParserException {
        AlterUser alterUser = roundTrip(AlterUser.class, sql, Dialect.MYSQL);
        assertEquals(1, alterUser.getAccounts().size());
        return alterUser.getAccounts().get(0);
    }

    @Test
    void createsAnAccountWithThreeFactors() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class,
                "CREATE USER u1 IDENTIFIED WITH caching_sha2_password BY 'p' "
                        + "AND IDENTIFIED WITH authentication_ldap_sasl AS 'uid=u' "
                        + "AND IDENTIFIED WITH authentication_webauthn",
                Dialect.MYSQL);

        UserAccount account = createUser.getAccounts().get(0);
        assertEquals(Mode.WITH_PLUGIN_BY_PASSWORD, account.getAuthentication().getMode());
        assertNull(account.getAuthentication().getInitialAuthentication());
        List<UserAuthentication> additional = account.getAdditionalAuthentications();
        assertEquals(2, additional.size());
        assertEquals(Mode.WITH_PLUGIN_AS_STRING, additional.get(0).getMode());
        assertEquals("authentication_ldap_sasl", additional.get(0).getPlugin());
        assertEquals("uid=u", additional.get(0).getCredential().getValue());
        assertEquals(Mode.WITH_PLUGIN, additional.get(1).getMode());
        assertEquals("authentication_webauthn", additional.get(1).getPlugin());
        assertTrue(account.getFactorOperations().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> additional.add(null));
    }

    @Test
    void createsFurtherFactorsWithoutAFirstOne() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class,
                "CREATE USER u3 AND IDENTIFIED BY 'q' AND IDENTIFIED BY RANDOM PASSWORD",
                Dialect.MYSQL);

        UserAccount account = createUser.getAccounts().get(0);
        assertNull(account.getAuthentication());
        assertEquals(Arrays.asList(Mode.BY_PASSWORD, Mode.BY_RANDOM_PASSWORD),
                account.getAdditionalAuthentications().stream().map(UserAuthentication::getMode)
                        .collect(Collectors.toList()));
    }

    @Test
    void keepsFactorsPerAccountBeforeTheOptions() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class,
                "CREATE USER IF NOT EXISTS 'u3'@'localhost' IDENTIFIED BY 'p' AND IDENTIFIED BY 'q', "
                        + "u4 IDENTIFIED BY 'p' AND IDENTIFIED WITH 'authentication_ldap_sasl' AS 0x41, "
                        + "u5 DEFAULT ROLE r1 REQUIRE SSL ACCOUNT LOCK",
                Dialect.MYSQL);

        assertEquals(3, createUser.getAccounts().size());
        assertEquals(1, createUser.getAccounts().get(0).getAdditionalAuthentications().size());
        UserAuthentication second =
                createUser.getAccounts().get(1).getAdditionalAuthentications().get(0);
        assertEquals("'", second.getPluginName().getQuoteStr());
        assertInstanceOf(HexValue.class, second.getCredentialValue());
        assertTrue(createUser.getAccounts().get(2).getAdditionalAuthentications().isEmpty());
        assertEquals("DEFAULT ROLE r1 REQUIRE SSL ACCOUNT LOCK",
                createUser.getOptions().toString());
    }

    @Test
    void createsAPasswordlessAccountWithInitialAuthentication() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class,
                "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                        + "INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
                Dialect.MYSQL);

        UserAuthentication authentication = createUser.getAccounts().get(0).getAuthentication();
        assertEquals(Mode.WITH_PLUGIN, authentication.getMode());
        assertEquals("authentication_webauthn", authentication.getPlugin());
        UserAuthentication initial = authentication.getInitialAuthentication();
        assertEquals(Mode.BY_PASSWORD, initial.getMode());
        assertEquals("p", initial.getCredential().getValue());
        assertNull(initial.getInitialAuthentication());

        assertEquals(Mode.BY_RANDOM_PASSWORD,
                roundTrip(CreateUser.class,
                        "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                                + "INITIAL AUTHENTICATION IDENTIFIED BY RANDOM PASSWORD, "
                                + "u4 IDENTIFIED BY 'x' PASSWORD EXPIRE",
                        Dialect.MYSQL).getAccounts().get(0).getAuthentication()
                        .getInitialAuthentication().getMode());
        UserAuthentication hash = roundTrip(CreateUser.class,
                "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                        + "INITIAL AUTHENTICATION IDENTIFIED WITH caching_sha2_password AS 0x41",
                Dialect.MYSQL).getAccounts().get(0).getAuthentication()
                .getInitialAuthentication();
        assertEquals(Mode.WITH_PLUGIN_AS_STRING, hash.getMode());
        assertEquals("caching_sha2_password", hash.getPlugin());
        assertInstanceOf(HexValue.class, hash.getCredentialValue());
    }

    @Test
    void addsFactors() throws JSQLParserException {
        UserAccount account = alteredAccount(
                "ALTER USER u1 ADD 2 FACTOR IDENTIFIED WITH authentication_ldap_sasl AS 'uid=u' "
                        + "ADD 3 FACTOR IDENTIFIED BY RANDOM PASSWORD");

        assertNull(account.getAuthentication());
        assertTrue(account.getAdditionalAuthentications().isEmpty());
        assertEquals(Arrays.asList("ADD:2", "ADD:3"), operations(account));
        UserFactorOperation first = account.getFactorOperations().get(0);
        assertTrue(first.getOperation().hasAuthentication());
        assertFalse(first.getOperation().isRegistration());
        assertEquals(Mode.WITH_PLUGIN_AS_STRING, first.getAuthentication().getMode());
        assertEquals("authentication_ldap_sasl", first.getAuthentication().getPlugin());
        assertNull(first.getChallengeResponse());
        assertEquals(Mode.BY_RANDOM_PASSWORD,
                account.getFactorOperations().get(1).getAuthentication().getMode());
    }

    @Test
    void modifiesFactors() throws JSQLParserException {
        UserAccount account = alteredAccount(
                "ALTER USER u1 MODIFY 2 FACTOR IDENTIFIED WITH authentication_webauthn");

        assertEquals(Arrays.asList("MODIFY:2"), operations(account));
        assertEquals(Mode.WITH_PLUGIN,
                account.getFactorOperations().get(0).getAuthentication().getMode());

        assertEquals(Arrays.asList("MODIFY:2", "MODIFY:3"),
                operations(alteredAccount("ALTER USER u1 MODIFY 2 FACTOR IDENTIFIED BY 'p' "
                        + "MODIFY 3 FACTOR IDENTIFIED WITH authentication_webauthn BY 'x'")));
    }

    @Test
    void dropsFactors() throws JSQLParserException {
        UserAccount account = alteredAccount("ALTER USER u1 DROP 2 FACTOR DROP 3 FACTOR");

        assertEquals(Arrays.asList("DROP:2", "DROP:3"), operations(account));
        UserFactorOperation drop = account.getFactorOperations().get(0);
        assertFalse(drop.getOperation().hasAuthentication());
        assertNull(drop.getAuthentication());
        assertEquals(Arrays.asList("DROP:3"),
                operations(alteredAccount("ALTER USER CURRENT_USER() DROP 3 FACTOR")));
    }

    @Test
    void changesFactorsOfSeveralAccountsBeforeTheOptions() throws JSQLParserException {
        AlterUser alterUser = roundTrip(AlterUser.class,
                "ALTER USER IF EXISTS 'u1'@'localhost' ADD 2 FACTOR IDENTIFIED BY 'p', "
                        + "u2 IDENTIFIED BY 'x' REPLACE 'y', u3 DROP 2 FACTOR DROP 3 FACTOR, u4 "
                        + "REQUIRE NONE PASSWORD EXPIRE NEVER COMMENT 'c'",
                Dialect.MYSQL);

        assertTrue(alterUser.isIfExists());
        List<UserAccount> accounts = alterUser.getAccounts();
        assertEquals(Arrays.asList("ADD:2"), operations(accounts.get(0)));
        assertTrue(accounts.get(1).getFactorOperations().isEmpty());
        assertEquals("y", accounts.get(1).getAuthentication().getCurrentCredential().getValue());
        assertEquals(Arrays.asList("DROP:2", "DROP:3"), operations(accounts.get(2)));
        assertTrue(accounts.get(3).getFactorOperations().isEmpty());
        assertEquals("REQUIRE NONE PASSWORD EXPIRE NEVER COMMENT 'c'",
                alterUser.getOptions().toString());
    }

    @Test
    void registersADevice() throws JSQLParserException {
        UserFactorOperation initiate =
                alteredAccount("ALTER USER u1 2 FACTOR INITIATE REGISTRATION")
                        .getFactorOperations().get(0);
        assertEquals(Operation.INITIATE_REGISTRATION, initiate.getOperation());
        assertEquals(2, initiate.getFactor());
        assertTrue(initiate.getOperation().isRegistration());
        assertNull(initiate.getAuthentication());
        assertNull(initiate.getChallengeResponse());

        UserFactorOperation finish = alteredAccount(
                "ALTER USER 'u1'@'localhost' 3 FACTOR FINISH REGISTRATION "
                        + "SET CHALLENGE_RESPONSE AS 'x'")
                .getFactorOperations().get(0);
        assertEquals(Operation.FINISH_REGISTRATION, finish.getOperation());
        assertEquals(3, finish.getFactor());
        assertEquals("x", ((StringValue) finish.getChallengeResponse()).getValue());
        assertInstanceOf(HexValue.class, alteredAccount(
                "ALTER USER u1 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE AS 0x41")
                .getFactorOperations().get(0).getChallengeResponse());

        AlterUser unregister = roundTrip(AlterUser.class,
                "ALTER USER IF EXISTS u1 2 FACTOR UNREGISTER", Dialect.MYSQL);
        assertTrue(unregister.isIfExists());
        assertTrue(unregister.getOptions().isEmpty());
        assertEquals(Arrays.asList("UNREGISTER:2"), operations(unregister.getAccounts().get(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER USER USER() 2 FACTOR INITIATE REGISTRATION",
            "ALTER USER USER() 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE AS 'x'",
            "ALTER USER USER() 3 FACTOR UNREGISTER"
    })
    void registersADeviceOfTheCurrentUser(String sql) throws JSQLParserException {
        AlterUser alterUser = roundTrip(AlterUser.class, sql, Dialect.MYSQL);

        assertTrue(alterUser.isUserFunction());
        UserAccount account = alterUser.getAccounts().get(0);
        assertNull(account.getAuthentication());
        assertEquals(1, account.getFactorOperations().size());
        assertTrue(account.getFactorOperations().get(0).getOperation().isRegistration());

        assertEquals(1, roundTrip(AlterUser.class, sql.replace("USER()", "CURRENT_USER()"),
                Dialect.MYSQL).getAccounts().get(0).getFactorOperations().size());
        assertEquals(1, roundTrip(AlterUser.class, sql.replace("USER()", "CURRENT_USER"),
                Dialect.MYSQL).getAccounts().get(0).getFactorOperations().size());
    }

    @Test
    void leavesTheFactorNumberToTheServer() throws JSQLParserException {
        // MySQL reads any integer here and reports "Incorrect nth factor value" on execution
        assertEquals(Arrays.asList("ADD:1"),
                operations(alteredAccount("ALTER USER u1 ADD 1 FACTOR IDENTIFIED BY 'p'")));
        assertEquals(Arrays.asList("DROP:2147483647"),
                operations(alteredAccount("ALTER USER u1 DROP 2147483647 FACTOR")));
        assertEquals(Arrays.asList("UNREGISTER:4"),
                operations(alteredAccount("ALTER USER u1 4 FACTOR UNREGISTER")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE USER u1 IDENTIFIED BY 'p' AND IDENTIFIED WITH authentication_ldap_sasl BY 'q'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED WITH caching_sha2_password AS 'h'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED WITH authentication_ldap_sasl BY RANDOM PASSWORD",
            "ALTER USER u1 MODIFY 2 FACTOR IDENTIFIED BY 'p' MODIFY 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 DROP 2 FACTOR, u2 DROP 3 FACTOR ACCOUNT UNLOCK",
            "ALTER USER u1 2 FACTOR INITIATE REGISTRATION",
            "ALTER USER u1 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE AS 'x'",
            "ALTER USER u1 2 FACTOR UNREGISTER"
    })
    void parsesInDefaultModeAndMariaDb(String sql) throws JSQLParserException {
        Class<? extends Statement> type =
                sql.startsWith("CREATE") ? CreateUser.class : AlterUser.class;
        roundTrip(type, sql, Dialect.MARIADB);
        assertInstanceOf(type, assertSqlCanBeParsedAndDeparsed(sql, false));
        assertEquals(sql.toLowerCase(), assertInstanceOf(type,
                CCJSqlParserUtil.parse(sql.toLowerCase())).toString().toLowerCase());
    }

    @Test
    void endsAtTheStatementBoundary() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "ALTER USER u1 2 FACTOR INITIATE REGISTRATION; "
                        + "ALTER USER u1 DROP 2 FACTOR DROP 3 FACTOR; "
                        + "CREATE USER u2 IDENTIFIED BY 'p' AND IDENTIFIED BY 'q'; "
                        + "CREATE USER u3 IDENTIFIED WITH authentication_webauthn "
                        + "INITIAL AUTHENTICATION IDENTIFIED BY RANDOM PASSWORD; SELECT 1",
                parser -> parser.withDialect(Dialect.MYSQL));

        assertEquals(5, statements.size());
        assertInstanceOf(AlterUser.class, statements.get(0));
        assertInstanceOf(AlterUser.class, statements.get(1));
        assertInstanceOf(CreateUser.class, statements.get(2));
        assertInstanceOf(CreateUser.class, statements.get(3));
        assertInstanceOf(PlainSelect.class, statements.get(4));
    }

    @Test
    void buildsAndDeparses() {
        CreateUser createUser = new CreateUser().addAccounts(
                new UserAccount().withUser(new StringValue("app").setQuoteStr(""))
                        .withAuthentication(new UserAuthentication().withMode(Mode.BY_PASSWORD)
                                .withCredential(new StringValue("p")))
                        .addAdditionalAuthentications(new UserAuthentication()
                                .withMode(Mode.WITH_PLUGIN).withPlugin("authentication_webauthn")),
                new UserAccount().withUser(new StringValue("key").setQuoteStr(""))
                        .withAuthentication(new UserAuthentication().withMode(Mode.WITH_PLUGIN)
                                .withPlugin("authentication_webauthn").withInitialAuthentication(
                                        new UserAuthentication()
                                                .withMode(Mode.BY_RANDOM_PASSWORD))));
        assertDeparse(createUser,
                "CREATE USER app IDENTIFIED BY 'p' AND IDENTIFIED WITH authentication_webauthn, "
                        + "key IDENTIFIED WITH authentication_webauthn "
                        + "INITIAL AUTHENTICATION IDENTIFIED BY RANDOM PASSWORD");

        UserAccount account = new UserAccount().withUser(new StringValue("app").setQuoteStr(""))
                .addFactorOperations(new UserFactorOperation(Operation.DROP, 2));
        AlterUser alterUser = new AlterUser().addAccounts(account);
        assertDeparse(alterUser, "ALTER USER app DROP 2 FACTOR");

        account.withFactorOperations(Arrays.asList(
                new UserFactorOperation().withOperation(Operation.MODIFY).withFactor(3)
                        .withAuthentication(new UserAuthentication().withMode(Mode.BY_PASSWORD)
                                .withCredential(new StringValue("p")))));
        assertDeparse(alterUser, "ALTER USER app MODIFY 3 FACTOR IDENTIFIED BY 'p'");

        account.withFactorOperations(Arrays.asList(
                new UserFactorOperation(Operation.FINISH_REGISTRATION, 2)
                        .withChallengeResponse(new HexValue("0x41"))));
        assertDeparse(alterUser,
                "ALTER USER app 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE AS 0x41");

        account.setFactorOperations(null);
        account.withAdditionalAuthentications(null);
        assertEquals("ALTER USER app", alterUser.toString());
        UserFactorOperation operation = new UserFactorOperation(Operation.UNREGISTER, 2);
        assertThrows(IllegalArgumentException.class,
                () -> operation.setChallengeResponse(new LongValue(1)));
    }

    @Test
    void reportsSchemaChangeWithoutTables() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "CREATE USER u1 IDENTIFIED BY 'p' AND IDENTIFIED BY 'q'",
                "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p'",
                "ALTER USER u1 2 FACTOR UNREGISTER")) {
            Statement statement =
                    CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));

            assertThat(statement.getFeatures().getCertain())
                    .containsExactly(StmtFeature.MODIFIES_SCHEMA);
            assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
            assertThat(Validation.validate(Arrays.asList(DatabaseType.MYSQL), sql)).isEmpty();
            String[] visited = new String[1];
            statement.accept(new StatementVisitorAdapter<Void>() {
                @Override
                public <S> Void visit(CreateUser createUser, S context) {
                    visited[0] = createUser.toString();
                    return null;
                }

                @Override
                public <S> Void visit(AlterUser alterUser, S context) {
                    visited[0] = alterUser.toString();
                    return null;
                }
            }, null);
            assertEquals(sql, visited[0]);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // CREATE USER: at most three factors, INITIAL AUTHENTICATION only after a bare plugin
            "CREATE USER u1 IDENTIFIED BY 'p' AND IDENTIFIED BY 'q' AND IDENTIFIED BY 'r' "
                    + "AND IDENTIFIED BY 's'",
            "CREATE USER u1 AND IDENTIFIED BY 'p' AND IDENTIFIED BY 'q' AND IDENTIFIED BY 'r'",
            "CREATE USER u1 IDENTIFIED BY 'p' AND BY 'q'",
            "CREATE USER u1 IDENTIFIED BY 'p' AND",
            "CREATE USER u1 IDENTIFIED BY 'p', IDENTIFIED BY 'q'",
            "CREATE USER u1 IDENTIFIED BY 'p' AND DISCARD OLD PASSWORD",
            "CREATE USER u1 IDENTIFIED BY 'p' ADD 2 FACTOR IDENTIFIED BY 'q'",
            "CREATE USER u1 ADD 2 FACTOR IDENTIFIED BY 'q'",
            "CREATE USER u1 2 FACTOR INITIATE REGISTRATION",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED WITH caching_sha2_password",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED WITH caching_sha2_password BY 'p'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn BY 'x' "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn AS 'x' "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
            "CREATE USER u2 IDENTIFIED BY 'x' INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'p' AND IDENTIFIED BY 'q'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'p' "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'q'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn INITIAL AUTHENTICATION BY 'p'",
            "CREATE USER u2 IDENTIFIED WITH authentication_webauthn INITIAL IDENTIFIED BY 'p'",
            "CREATE USER u2 INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
            // ALTER USER: one or two changes of the same kind per account
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' ADD 3 FACTOR IDENTIFIED BY 'q' "
                    + "ADD 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p', 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' MODIFY 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' DROP 3 FACTOR",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' REPLACE 'x'",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' RETAIN CURRENT PASSWORD",
            "ALTER USER u1 ADD 2 FACTOR IDENTIFIED BY 'p' AND IDENTIFIED BY 'q'",
            "ALTER USER u1 ADD 2 FACTOR",
            "ALTER USER u1 ADD 2 FACTOR DISCARD OLD PASSWORD",
            "ALTER USER u1 ADD FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD 2.0 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD '2' FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD 0x2 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD 2147483648 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD 99999999999999999999 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 ADD 2 FACTORS IDENTIFIED BY 'p'",
            "ALTER USER u1 MODIFY 2 FACTOR",
            "ALTER USER u1 MODIFY 2 FACTOR IDENTIFIED BY 'p' ADD 3 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 MODIFY 2 FACTOR IDENTIFIED BY 'p' MODIFY 3 FACTOR IDENTIFIED BY 'p' "
                    + "MODIFY 3 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 DROP 2 FACTOR 3 FACTOR",
            "ALTER USER u1 DROP 2 FACTOR, 3 FACTOR",
            "ALTER USER u1 DROP 2 FACTOR DROP 3 FACTOR DROP 3 FACTOR",
            "ALTER USER u1 DROP 2 FACTOR ADD 3 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 DROP 2 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER u1 DROP FACTOR",
            "ALTER USER u1 DROP 2",
            "ALTER USER u1 IDENTIFIED BY 'p' ADD 2 FACTOR IDENTIFIED BY 'q'",
            "ALTER USER u1 IDENTIFIED BY 'p' AND IDENTIFIED BY 'q'",
            "ALTER USER u1 IDENTIFIED WITH authentication_webauthn "
                    + "INITIAL AUTHENTICATION IDENTIFIED BY 'p'",
            "ALTER USER u1 DROP 2 FACTOR DEFAULT ROLE r1",
            "ALTER USER USER() ADD 2 FACTOR IDENTIFIED BY 'p'",
            "ALTER USER USER() DROP 2 FACTOR",
            // registration: one account, one step, nothing else
            "ALTER USER u1 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE = 'x'",
            "ALTER USER u1 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE 'x'",
            "ALTER USER u1 2 FACTOR FINISH REGISTRATION SET CHALLENGE_RESPONSE AS 1",
            "ALTER USER u1 2 FACTOR FINISH REGISTRATION",
            "ALTER USER u1 2 FACTOR INITIATE REGISTRATION SET CHALLENGE_RESPONSE AS 'x'",
            "ALTER USER u1 FACTOR UNREGISTER",
            "ALTER USER u1 2 FACTOR REGISTER",
            "ALTER USER u1 2 FACTOR INITIATE",
            "ALTER USER u1 2 FACTOR",
            "ALTER USER u1 2 FACTOR UNREGISTER, u2 2 FACTOR UNREGISTER",
            "ALTER USER u1, u2 2 FACTOR UNREGISTER",
            "ALTER USER u1 2 FACTOR UNREGISTER ACCOUNT LOCK",
            "ALTER USER u1 2 FACTOR UNREGISTER 3 FACTOR UNREGISTER",
            "ALTER USER u1 IDENTIFIED BY 'p' 2 FACTOR UNREGISTER",
            "ALTER USER u1 DROP 2 FACTOR 3 FACTOR UNREGISTER",
            "ALTER USER USER() IDENTIFIED BY 'p' 2 FACTOR UNREGISTER"
    })
    void rejectsSyntaxOutsideTheStatementDefinition(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void factorWordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT factor, initial, authentication, initiate, "
                + "registration, finish, unregister, challenge_response FROM t");
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE factor (initial INT, registration INT)");
    }
}
