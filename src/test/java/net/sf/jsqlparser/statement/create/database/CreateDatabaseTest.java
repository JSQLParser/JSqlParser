/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.database;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CreateDatabaseTest {

    @Test
    void testCreateDatabaseIssue2070() throws JSQLParserException {
        String statement = "CREATE DATABASE USERS";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateDatabase().withDatabaseName("USERS"), statement);
    }

    @Test
    void testCreateDatabaseIfNotExists() throws JSQLParserException {
        String statement = "CREATE DATABASE IF NOT EXISTS mydb";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateDatabase().withDatabaseName("mydb").withIfNotExists(true),
                statement);
    }

    @Test
    void testCreateDatabaseQuotedName() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE DATABASE `my db`");
        assertSqlCanBeParsedAndDeparsed("CREATE DATABASE \"my db\"");
    }

    @Test
    void testCreateDatabaseWithOptions() throws JSQLParserException {
        String statement =
                "CREATE DATABASE IF NOT EXISTS mydb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        assertSqlCanBeParsedAndDeparsed(statement);

        Statement parsed = CCJSqlParserUtil.parse(statement);
        CreateDatabase createDatabase = assertInstanceOf(CreateDatabase.class, parsed);
        assertEquals("mydb", createDatabase.getDatabaseName());
        assertTrue(createDatabase.hasIfNotExists());
        assertEquals(
                Arrays.asList("DEFAULT", "CHARACTER", "SET", "utf8mb4", "COLLATE",
                        "utf8mb4_unicode_ci"),
                createDatabase.getDatabaseOptions());
        DatabaseOption characterSet =
                createDatabase.getOption(DatabaseOption.Kind.CHARACTER_SET).orElseThrow();
        assertEquals("utf8mb4", characterSet.getValue());
        assertTrue(characterSet.isUseDefault());
        assertEquals("utf8mb4_unicode_ci",
                createDatabase.getOption(DatabaseOption.Kind.COLLATE).orElseThrow().getValue());
    }

    @Test
    void testCreateDatabaseAST() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("CREATE DATABASE USERS");
        CreateDatabase createDatabase = assertInstanceOf(CreateDatabase.class, statement);
        assertEquals("USERS", createDatabase.getDatabaseName());
        assertFalse(createDatabase.hasIfNotExists());

        statement = CCJSqlParserUtil.parse("CREATE DATABASE IF NOT EXISTS mydb");
        createDatabase = assertInstanceOf(CreateDatabase.class, statement);
        assertEquals("mydb", createDatabase.getDatabaseName());
        assertTrue(createDatabase.hasIfNotExists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE DATABASE mydb2",
            "CREATE DATABASE db1 DEFAULT CHARSET = utf8mb4",
            "CREATE DATABASE db1 CHAR SET utf8mb4 DEFAULT ENCRYPTION = 'N'",
            "CREATE DATABASE IF NOT EXISTS db1 CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"})
    void testMySqlForms(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.MYSQL);
        assertRoundTrip(sql, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE DATABASE mydb WITH OWNER = app ENCODING = 'UTF8' LC_COLLATE = 'C' "
                    + "LC_CTYPE = 'C' TEMPLATE = template0 CONNECTION LIMIT = 10",
            "CREATE DATABASE mydb OWNER app STRATEGY = file_copy LOCALE 'C' "
                    + "BUILTIN_LOCALE = 'C.UTF-8' LOCALE_PROVIDER = builtin "
                    + "COLLATION_VERSION = '1' TABLESPACE = fast ALLOW_CONNECTIONS = true "
                    + "CONNECTION LIMIT = -1 IS_TEMPLATE false OID = 16384",
            "CREATE DATABASE mydb WITH TEMPLATE DEFAULT ENCODING UTF8 LOCALE_PROVIDER icu "
                    + "ICU_LOCALE 'de-DE' ICU_RULES = '&a < b'"})
    void testPostgreSqlForms(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.POSTGRESQL);
        assertRoundTrip(sql, null);
    }

    @Test
    void testPostgreSqlOptionsAreTyped() throws JSQLParserException {
        CreateDatabase createDatabase = parse("CREATE DATABASE mydb WITH OWNER = app "
                + "ENCODING = 'UTF8' TEMPLATE template0 CONNECTION LIMIT = -1 IS_TEMPLATE false",
                Dialect.POSTGRESQL);
        assertTrue(createDatabase.isUseWith());
        List<DatabaseOption> options = createDatabase.getOptions();
        assertEquals(Arrays.asList(DatabaseOption.Kind.OWNER, DatabaseOption.Kind.ENCODING,
                DatabaseOption.Kind.TEMPLATE, DatabaseOption.Kind.CONNECTION_LIMIT,
                DatabaseOption.Kind.IS_TEMPLATE),
                options.stream().map(DatabaseOption::getKind).collect(
                        java.util.stream.Collectors.toList()));
        assertEquals("app", options.get(0).getValue());
        assertTrue(options.get(0).isUseEquals());
        assertEquals("'UTF8'", options.get(1).getValue());
        assertFalse(options.get(2).isUseEquals());
        assertEquals("CONNECTION LIMIT", options.get(3).getName());
        assertEquals("-1", options.get(3).getValue());
        assertEquals("false", options.get(4).getValue());

        createDatabase.getOption(DatabaseOption.Kind.OWNER).orElseThrow().setValue("CURRENT_USER");
        options.remove(4);
        assertEquals("CREATE DATABASE mydb WITH OWNER = CURRENT_USER ENCODING = 'UTF8' "
                + "TEMPLATE template0 CONNECTION LIMIT = -1", createDatabase.toString());
    }

    @Test
    void testCharsetSpellingIsTyped() throws JSQLParserException {
        CreateDatabase createDatabase =
                parse("CREATE DATABASE db1 DEFAULT CHARSET = utf8mb4", Dialect.MYSQL);
        assertEquals(1, createDatabase.getOptions().size());
        DatabaseOption charset = createDatabase.getOptions().get(0);
        assertEquals(DatabaseOption.Kind.CHARACTER_SET, charset.getKind());
        assertEquals("CHARSET", charset.getName());
        assertEquals("utf8mb4", charset.getValue());
        assertTrue(charset.isUseDefault());
        assertTrue(charset.isUseEquals());
    }

    @Test
    void testNoEmptyRawOption() throws JSQLParserException {
        CreateDatabase createDatabase = parse("CREATE DATABASE mydb2", null);
        assertTrue(createDatabase.getOptions().isEmpty());
        assertNull(createDatabase.getDatabaseOptions());
        assertEquals("CREATE DATABASE mydb2", createDatabase.toString());

        createDatabase = parse("CREATE DATABASE db1 CHARACTER SET utf8mb4", Dialect.MYSQL);
        assertEquals(1, createDatabase.getOptions().size());
        assertEquals("CREATE DATABASE db1 CHARACTER SET utf8mb4", createDatabase.toString());
    }

    @Test
    void testUnknownOptionTextStaysRaw() throws JSQLParserException {
        CreateDatabase createDatabase =
                assertInstanceOf(CreateDatabase.class, assertSqlCanBeParsedAndDeparsed(
                        "CREATE DATABASE mydb WITH OWNER app LOCATION 'x'"));
        assertEquals(2, createDatabase.getOptions().size());
        assertEquals(DatabaseOption.Kind.OWNER, createDatabase.getOptions().get(0).getKind());
        DatabaseOption raw = createDatabase.getOptions().get(1);
        assertEquals(DatabaseOption.Kind.OTHER, raw.getKind());
        assertEquals(Arrays.asList("LOCATION", "'x'"), raw.getTokens());

        // WITH is kept with the raw text when no known option follows it.
        createDatabase = assertInstanceOf(CreateDatabase.class,
                assertSqlCanBeParsedAndDeparsed("CREATE DATABASE mydb WITH LOCATION 'x'"));
        assertFalse(createDatabase.isUseWith());
        assertEquals(Arrays.asList("WITH", "LOCATION", "'x'"),
                createDatabase.getOptions().get(0).getTokens());
    }

    @Test
    void testLegacyDatabaseOptionTokens() throws JSQLParserException {
        CreateDatabase createDatabase = parse(
                "CREATE DATABASE mydb WITH OWNER = app CONNECTION LIMIT 5", Dialect.POSTGRESQL);
        List<String> tokens = createDatabase.getDatabaseOptions();
        assertEquals(Arrays.asList("WITH", "OWNER", "=", "app", "CONNECTION", "LIMIT", "5"),
                tokens);
        assertThrows(UnsupportedOperationException.class, () -> tokens.add("x"));

        createDatabase.getOptions().get(1).setValue("10");
        assertEquals("10", createDatabase.getDatabaseOptions().get(6));

        createDatabase.addDatabaseOptions("IS_TEMPLATE", "true");
        assertEquals(DatabaseOption.Kind.OWNER, createDatabase.getOptions().get(0).getKind());
        assertEquals(DatabaseOption.Kind.OTHER, createDatabase.getOptions().get(2).getKind());
        assertEquals("CREATE DATABASE mydb WITH OWNER = app CONNECTION LIMIT 10 IS_TEMPLATE true",
                createDatabase.toString());

        List<String> raw = new ArrayList<>(Arrays.asList("ENCODING", "'UTF8'"));
        createDatabase.setDatabaseOptions(raw);
        assertFalse(createDatabase.isUseWith());
        assertEquals(DatabaseOption.Kind.OTHER, createDatabase.getOptions().get(0).getKind());
        assertEquals("CREATE DATABASE mydb ENCODING 'UTF8'", createDatabase.toString());
        createDatabase.setDatabaseOptions(null);
        assertTrue(createDatabase.getOptions().isEmpty());
        assertEquals("CREATE DATABASE mydb", createDatabase.toString());
    }

    @Test
    void testOptionListOwnership() {
        List<DatabaseOption> options = new ArrayList<>();
        options.add(new DatabaseOption(DatabaseOption.Kind.OWNER, "OWNER", "app", false, true));
        CreateDatabase createDatabase =
                new CreateDatabase().withDatabaseName("mydb").withUseWith(true)
                        .withOptions(options);
        options.clear();
        assertEquals("CREATE DATABASE mydb WITH OWNER = app", createDatabase.toString());
        createDatabase.getOptions().add(
                new DatabaseOption(DatabaseOption.Kind.IS_TEMPLATE, "IS_TEMPLATE", "true", false,
                        false));
        assertEquals("CREATE DATABASE mydb WITH OWNER = app IS_TEMPLATE true",
                createDatabase.toString());
        createDatabase.setOptions(createDatabase.getOptions());
        assertEquals(2, createDatabase.getOptions().size());
        createDatabase.setOptions(null);
        assertTrue(createDatabase.getOptions().isEmpty());
    }

    @Test
    void testValidation() {
        validateNoErrors("CREATE DATABASE mydb WITH OWNER = app", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("CREATE DATABASE db1 DEFAULT CHARSET = utf8mb4", 1, DatabaseType.MYSQL,
                DatabaseType.MARIADB);
    }

    /** Parses, deparses and reparses with the dialect, or without one if it is null. */
    private static void assertRoundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Consumer<CCJSqlParser> consumer = consumer(dialect);
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, consumer);
        CreateDatabase createDatabase = assertInstanceOf(CreateDatabase.class, statement);
        assertTrue(createDatabase.getOptions().stream()
                .noneMatch(option -> option.getKind() == DatabaseOption.Kind.OTHER));
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1", consumer).size());
    }

    private static CreateDatabase parse(String sql, Dialect dialect) throws JSQLParserException {
        return (CreateDatabase) CCJSqlParserUtil.parse(sql, consumer(dialect));
    }

    private static Consumer<CCJSqlParser> consumer(Dialect dialect) {
        return dialect == null ? parser -> {
        } : parser -> parser.withDialect(dialect);
    }
}
