/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.database;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.schema.AlterSchema;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterDatabaseTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER DATABASE d CHARACTER SET utf8mb4 COLLATE utf8mb4_bin",
            "ALTER SCHEMA `d` DEFAULT CHARACTER SET = utf8mb4 DEFAULT COLLATE = utf8mb4_bin",
            "ALTER DATABASE d DEFAULT ENCRYPTION = 'N'",
            "ALTER DATABASE d READ ONLY = 0", "ALTER DATABASE d READ ONLY 1",
            "ALTER DATABASE READ ONLY = DEFAULT", "ALTER SCHEMA DEFAULT CHARACTER SET utf8mb4"})
    void roundTripAndStatementBoundary(String sql) throws JSQLParserException {
        AlterDatabase statement = parse(sql);
        assertEquals(sql, statement.toString());
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(sql, buffer.toString());
        assertEquals(sql, parse(buffer.toString()).toString());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.MYSQL)).size());
    }

    @Test
    void optionMutationAndVisitorDispatch() throws JSQLParserException {
        AlterDatabase statement = parse("ALTER DATABASE old_db READ ONLY = 0");
        statement.setDatabaseName("new_db");
        statement.getOption(DatabaseOption.Kind.READ_ONLY).orElseThrow().setValue("1");
        assertEquals("ALTER DATABASE new_db READ ONLY = 1", statement.toString());
        assertEquals("context:new_db", statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterDatabase database, S context) {
                return context + ":" + database.getDatabaseName();
            }
        }, "context"));
    }

    @Test
    void rejectIncompleteAndInvalidReadOnly() {
        for (String sql : new String[] {"ALTER DATABASE d", "ALTER DATABASE d READ ONLY = 2",
                "ALTER DATABASE d DEFAULT READ ONLY = 0"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER DATABASE db1 DEFAULT CHARSET = utf8mb4",
            "ALTER SCHEMA db1 DEFAULT ENCRYPTION = 'Y'", "ALTER SCHEMA db1 CHAR SET utf8mb4",
            "ALTER DATABASE CHARSET utf8mb4 COLLATE utf8mb4_bin",
            "ALTER DATABASE charset CHARSET = utf8mb4",
            "ALTER DATABASE READ ONLY = DEFAULT", "ALTER SCHEMA DEFAULT CHARACTER SET utf8mb4"})
    void testMySqlOptionsWithAndWithoutDialect(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.MYSQL);
        assertRoundTrip(sql, null);
    }

    @Test
    void testCharsetSpellingAndOmittedName() throws JSQLParserException {
        AlterDatabase statement =
                (AlterDatabase) CCJSqlParserUtil.parse("ALTER SCHEMA DEFAULT CHARSET utf8mb4");
        assertTrue(statement.isUseSchemaKeyword());
        assertNull(statement.getDatabaseName());
        DatabaseOption charset =
                statement.getOption(DatabaseOption.Kind.CHARACTER_SET).orElseThrow();
        assertEquals("CHARSET", charset.getName());
        assertEquals("utf8mb4", charset.getValue());
        assertTrue(charset.isUseDefault());

        // CHARSET is also a valid database name.
        statement =
                (AlterDatabase) CCJSqlParserUtil.parse("ALTER DATABASE charset CHARSET utf8mb4");
        assertEquals("charset", statement.getDatabaseName());
        statement = (AlterDatabase) CCJSqlParserUtil.parse("ALTER DATABASE charset RENAME TO c2",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("charset", statement.getDatabaseName());
        assertEquals("c2", statement.getNewName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER DATABASE mydb RENAME TO mydb2",
            "ALTER DATABASE \"My Db\" RENAME TO \"Other Db\"", "ALTER DATABASE mydb OWNER TO u1",
            "ALTER DATABASE mydb OWNER TO CURRENT_USER",
            "ALTER DATABASE mydb OWNER TO CURRENT_ROLE",
            "ALTER DATABASE mydb OWNER TO SESSION_USER", "ALTER DATABASE mydb SET TABLESPACE fast",
            "ALTER DATABASE mydb REFRESH COLLATION VERSION",
            "ALTER DATABASE mydb SET search_path TO app, public",
            "ALTER DATABASE mydb SET search_path = \"$user\", public",
            "ALTER DATABASE mydb SET work_mem = '64MB'",
            "ALTER DATABASE mydb SET work_mem TO DEFAULT",
            "ALTER DATABASE mydb SET work_mem FROM CURRENT", "ALTER DATABASE mydb RESET work_mem",
            "ALTER DATABASE mydb RESET ALL", "ALTER DATABASE mydb WITH ALLOW_CONNECTIONS false",
            "ALTER DATABASE mydb CONNECTION LIMIT = -1 IS_TEMPLATE true",
            "ALTER DATABASE mydb WITH IS_TEMPLATE = true ALLOW_CONNECTIONS = false CONNECTION LIMIT 5"})
    void testPostgreSqlForms(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.POSTGRESQL);
        assertRoundTrip(sql, null);
    }

    @Test
    void testPostgreSqlActions() throws JSQLParserException {
        AlterDatabase rename = parsePostgreSql("ALTER DATABASE mydb RENAME TO mydb2");
        assertEquals(AlterDatabase.Action.RENAME, rename.getAction());
        assertEquals("mydb", rename.getDatabaseName());
        assertEquals("mydb2", rename.getNewName());
        assertTrue(rename.getOptions().isEmpty());

        AlterDatabase owner = parsePostgreSql("ALTER DATABASE mydb OWNER TO CURRENT_USER");
        assertEquals(AlterDatabase.Action.OWNER, owner.getAction());
        assertEquals("CURRENT_USER", owner.getOwner());

        AlterDatabase tablespace = parsePostgreSql("ALTER DATABASE mydb SET TABLESPACE fast");
        assertEquals(AlterDatabase.Action.SET_TABLESPACE, tablespace.getAction());
        assertEquals("fast", tablespace.getTablespace());
        assertNull(tablespace.getParameterChange());

        assertEquals(AlterDatabase.Action.REFRESH_COLLATION_VERSION,
                parsePostgreSql("ALTER DATABASE mydb REFRESH COLLATION VERSION").getAction());

        // A configuration parameter named "tablespace" is a SET, not SET TABLESPACE.
        AlterDatabase parameter = parsePostgreSql("ALTER DATABASE mydb SET tablespace = fast");
        assertEquals(AlterDatabase.Action.CONFIGURATION_PARAMETER, parameter.getAction());
        assertEquals("tablespace", parameter.getParameterChange().getParameter());
    }

    @Test
    void testPostgreSqlOptions() throws JSQLParserException {
        AlterDatabase statement = parsePostgreSql(
                "ALTER DATABASE mydb WITH ALLOW_CONNECTIONS false CONNECTION LIMIT = -1 IS_TEMPLATE true");
        assertEquals(AlterDatabase.Action.OPTIONS, statement.getAction());
        assertTrue(statement.isUseWith());
        List<DatabaseOption> options = statement.getOptions();
        assertEquals(3, options.size());
        assertOption(options.get(0), DatabaseOption.Kind.ALLOW_CONNECTIONS, "ALLOW_CONNECTIONS",
                "false", false);
        assertOption(options.get(1), DatabaseOption.Kind.CONNECTION_LIMIT, "CONNECTION LIMIT",
                "-1", true);
        assertOption(options.get(2), DatabaseOption.Kind.IS_TEMPLATE, "IS_TEMPLATE", "true",
                false);

        statement.getOption(DatabaseOption.Kind.CONNECTION_LIMIT).orElseThrow().setValue("10");
        statement.setUseWith(false);
        assertEquals("ALTER DATABASE mydb ALLOW_CONNECTIONS false CONNECTION LIMIT = 10 "
                + "IS_TEMPLATE true", statement.toString());
    }

    @Test
    void testSwitchingActionsKeepsPayloads() throws JSQLParserException {
        AlterDatabase statement = parsePostgreSql("ALTER DATABASE mydb RENAME TO mydb2");
        statement.setAction(AlterDatabase.Action.OWNER);
        statement.setOwner("SESSION_USER");
        assertEquals("ALTER DATABASE mydb OWNER TO SESSION_USER", statement.toString());
        statement.setAction(AlterDatabase.Action.RENAME);
        assertEquals("ALTER DATABASE mydb RENAME TO mydb2", statement.toString());

        statement.setAction(AlterDatabase.Action.CONFIGURATION_PARAMETER);
        assertThrows(IllegalStateException.class, statement::toString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER DATABASE d", "ALTER DATABASE d SET TIME ZONE 'UTC'",
            "ALTER DATABASE d SET RECOVERY SIMPLE", "ALTER DATABASE d MODIFY NAME = d2",
            "ALTER DATABASE WITH ALLOW_CONNECTIONS true"})
    void testUnmodelledFormsRemainUnsupportedOutsideMySql(String sql)
            throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql));
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void testPostgreSqlAlterSchemaIsUnchanged() throws JSQLParserException {
        assertInstanceOf(AlterSchema.class, CCJSqlParserUtil.parse("ALTER SCHEMA s RENAME TO t"));
        assertInstanceOf(AlterSchema.class,
                CCJSqlParserUtil.parse("ALTER SCHEMA s OWNER TO CURRENT_USER"));
        assertInstanceOf(AlterDatabase.class,
                CCJSqlParserUtil.parse("ALTER SCHEMA s DEFAULT ENCRYPTION = 'Y'"));
    }

    @Test
    void testValidation() {
        validateNoErrors("ALTER DATABASE mydb SET work_mem = '64MB'", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER DATABASE mydb OWNER TO CURRENT_USER", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER DATABASE d CHARACTER SET utf8mb4", 1, DatabaseType.MYSQL,
                DatabaseType.MARIADB);
        assertFalse(new Validation(List.of(FeaturesAllowed.SELECT),
                "ALTER DATABASE mydb RESET ALL").validate().isEmpty());
        assertTrue(new Validation(List.of(new FeaturesAllowed().add(Feature.alterDatabase)),
                "ALTER DATABASE mydb RESET ALL").validate().isEmpty());
    }

    private static void assertOption(DatabaseOption option, DatabaseOption.Kind kind, String name,
            String value, boolean useEquals) {
        assertEquals(kind, option.getKind());
        assertEquals(name, option.getName());
        assertEquals(value, option.getValue());
        assertEquals(useEquals, option.isUseEquals());
        assertFalse(option.isUseDefault());
    }

    /** Parses, deparses and reparses with the dialect, or without one if it is null. */
    private static void assertRoundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Consumer<CCJSqlParser> consumer =
                dialect == null ? parser -> {
                } : parser -> parser.withDialect(dialect);
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, consumer);
        assertInstanceOf(AlterDatabase.class, statement);
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1", consumer).size());
    }

    private static AlterDatabase parsePostgreSql(String sql) throws JSQLParserException {
        return (AlterDatabase) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static AlterDatabase parse(String sql) throws JSQLParserException {
        return (AlterDatabase) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }
}
