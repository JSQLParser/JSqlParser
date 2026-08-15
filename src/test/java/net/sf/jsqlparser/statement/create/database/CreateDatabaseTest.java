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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

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
}
