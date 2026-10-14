/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.show.ShowCreateStatement.ObjectType;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ShowCreateStatementTest {

    private static final List<Consumer<CCJSqlParser>> DIALECTS = List.of(parser -> {
    }, parser -> parser.withDialect(Dialect.MYSQL),
            parser -> parser.withDialect(Dialect.MARIADB));

    /** Parses without a dialect and with MySQL and MariaDB, then deparses and reparses. */
    private static ShowCreateStatement roundTrip(String sql) throws JSQLParserException {
        ShowCreateStatement result = null;
        for (Consumer<CCJSqlParser> dialect : DIALECTS) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(ShowCreateStatement.class, statement);
            assertDeparse(statement, sql);
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
            result = (ShowCreateStatement) statement;
        }
        return result;
    }

    @Test
    void createTable() throws JSQLParserException {
        ShowCreateStatement show = roundTrip("SHOW CREATE TABLE c20");

        assertEquals(ObjectType.TABLE, show.getObjectType());
        assertEquals("c20", show.getName().getFullyQualifiedName());
        assertNull(show.getDatabaseName());
        assertNull(show.getAccount());
    }

    @ParameterizedTest
    @CsvSource({
            "SHOW CREATE TABLE `db`.`t`, TABLE, `db`.`t`",
            "SHOW CREATE VIEW db.v, VIEW, db.v",
            "SHOW CREATE PROCEDURE gp13, PROCEDURE, gp13",
            "SHOW CREATE FUNCTION db.f, FUNCTION, db.f",
            "SHOW CREATE TRIGGER tr, TRIGGER, tr",
            "SHOW CREATE EVENT db.e, EVENT, db.e"
    })
    void namedObjects(String sql, ObjectType type, String name) throws JSQLParserException {
        ShowCreateStatement show = roundTrip(sql);

        assertEquals(type, show.getObjectType());
        assertTrue(type.isNamedByTable());
        assertEquals(name, show.getName().getFullyQualifiedName());
    }

    @Test
    void createDatabase() throws JSQLParserException {
        ShowCreateStatement show = roundTrip("SHOW CREATE DATABASE `shop`");

        assertEquals(ObjectType.DATABASE, show.getObjectType());
        assertFalse(show.isSchemaKeyword());
        assertFalse(show.isIfNotExists());
        assertEquals("`shop`", show.getDatabaseName());
        assertNull(show.getName());
    }

    @Test
    void createSchemaIfNotExists() throws JSQLParserException {
        ShowCreateStatement show = roundTrip("SHOW CREATE SCHEMA IF NOT EXISTS shop");

        assertEquals(ObjectType.DATABASE, show.getObjectType());
        assertTrue(show.isSchemaKeyword());
        assertTrue(show.isIfNotExists());
        assertEquals("shop", show.getDatabaseName());
    }

    @Test
    void createUserWithHost() throws JSQLParserException {
        ShowCreateStatement show = roundTrip("SHOW CREATE USER 'app'@'%'");

        assertEquals(ObjectType.USER, show.getObjectType());
        assertEquals("app", show.getAccount().getUser().getNotExcapedValue());
        assertEquals("%", show.getAccount().getHost().getNotExcapedValue());
        assertNull(show.getAccount().getAuthentication());
        assertFalse(show.isCurrentUser());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW CREATE USER app",
            "SHOW CREATE USER 'app'",
            "SHOW CREATE USER `app`@`localhost`",
            "SHOW CREATE USER app@localhost",
            "SHOW CREATE USER app@'%'",
            "SHOW CREATE USER 'app'@localhost",
            "SHOW CREATE USER \"app\"@\"%\""
    })
    void accountSpellings(String sql) throws JSQLParserException {
        ShowCreateStatement show = roundTrip(sql);

        assertEquals(ObjectType.USER, show.getObjectType());
        assertFalse(show.isCurrentUser());
        assertEquals("app", show.getAccount().getUser().getValue());
    }

    @Test
    void currentUser() throws JSQLParserException {
        ShowCreateStatement show = roundTrip("SHOW CREATE USER CURRENT_USER");
        assertTrue(show.isCurrentUser());
        assertFalse(show.isCurrentUserParentheses());

        show = roundTrip("SHOW CREATE USER CURRENT_USER()");
        assertTrue(show.isCurrentUser());
        assertTrue(show.isCurrentUserParentheses());
        assertNull(show.getAccount().getHost());
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        assertEquals("SHOW CREATE SCHEMA IF NOT EXISTS db",
                CCJSqlParserUtil.parse("show create schema if not exists db").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW CREATE TEMPORARY TABLE t",
            "SHOW CREATE TABLE t FORMAT TabSeparated",
            "SHOW CREATE TABLE t AS SERDE",
            "SHOW CREATE SEQUENCE s",
            "SHOW CREATE PACKAGE BODY p",
            "SHOW CREATE VIEW IF NOT EXISTS v",
            "SHOW CREATE DATABASE IF NOT EXISTS",
            "SHOW CREATE USER u IDENTIFIED BY 'x'",
            "SHOW CREATE TABLE"
    })
    void otherFormsRemainUnsupported(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        assertInstanceOf(UnsupportedStatement.class, statement);
    }

    @Test
    void endsAtTheStatementBoundary() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW CREATE TABLE a;\nSHOW CREATE USER CURRENT_USER();\nSELECT 1",
                parser -> parser.withDialect(Dialect.MYSQL));

        assertEquals(3, statements.size());
        assertInstanceOf(ShowCreateStatement.class, statements.get(0));
        assertInstanceOf(ShowCreateStatement.class, statements.get(1));
    }

    @Test
    void findsTablesAndViewsOnly() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("SHOW CREATE TABLE s.t")).containsExactly("s.t");
        assertThat(TablesNamesFinder.findTables("SHOW CREATE VIEW v")).containsExactly("v");
        assertThat(TablesNamesFinder.findTables("SHOW CREATE TRIGGER tr")).isEmpty();
        assertThat(TablesNamesFinder.findTables("SHOW CREATE DATABASE db")).isEmpty();
        assertThat(TablesNamesFinder.findTables("SHOW CREATE USER u")).isEmpty();
    }

    @Test
    void buildsAndDeparses() {
        assertDeparse(new ShowCreateStatement(ObjectType.VIEW, new Table("s", "v")),
                "SHOW CREATE VIEW s.v");
        assertDeparse(new ShowCreateStatement().withObjectType(ObjectType.DATABASE)
                .withSchemaKeyword(true).withIfNotExists(true).withDatabaseName("db"),
                "SHOW CREATE SCHEMA IF NOT EXISTS db");
        UserAccount account = new UserAccount().withUser(new StringValue("'app'"))
                .withHost(new StringValue("'localhost'"));
        assertDeparse(new ShowCreateStatement().withObjectType(ObjectType.USER)
                .withAccount(account), "SHOW CREATE USER 'app'@'localhost'");
    }

    @Test
    void reportsMetadataFeatures() throws JSQLParserException {
        StatementFeatures features = CCJSqlParserUtil.parse("SHOW CREATE TABLE t").getFeatures();

        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
    }
}
