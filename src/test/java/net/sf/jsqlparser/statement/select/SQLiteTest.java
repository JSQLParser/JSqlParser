/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLiteTest {
    @Test
    void testInsertOrReplaceUpsert() throws JSQLParserException {
        String sqlString = "INSERT OR REPLACE INTO kjobLocks VALUES (?, ?, ?)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABORT", "FAIL", "IGNORE", "REPLACE", "ROLLBACK"})
    void testInsertOrConflictAlgorithm(String algorithm) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT OR " + algorithm + " INTO t (a, b) VALUES (1, 2)", true,
                parser -> parser.withDialect(Dialect.SQLITE));
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "insert or " + algorithm.toLowerCase(Locale.ROOT) + " into t select * from s", true,
                parser -> parser.withDialect(Dialect.SQLITE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABORT", "FAIL", "IGNORE", "ROLLBACK"})
    void testInsertOrConflictAlgorithmNeedsSqliteDialect(String algorithm) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("INSERT OR " + algorithm + " INTO t VALUES (1)"));
    }

    @Test
    void testInsertOrUnknownAlgorithm() {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("INSERT OR NOTHING INTO t VALUES (1)",
                        parser -> parser.withDialect(Dialect.SQLITE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (a INTEGER PRIMARY KEY, b TEXT) WITHOUT ROWID",
            "CREATE TABLE t (a INTEGER PRIMARY KEY, b TEXT) STRICT",
            "CREATE TABLE t (a INTEGER PRIMARY KEY, b BLOB) STRICT, WITHOUT ROWID",
            "CREATE TABLE t (a INTEGER PRIMARY KEY, b TEXT) WITHOUT ROWID, STRICT",
            "create table t (a integer primary key) without rowid"})
    void testCreateTableWithoutRowidAndStrict(String sql) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    void testCreateTableOptionKinds() throws JSQLParserException {
        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (a INTEGER PRIMARY KEY) STRICT, WITHOUT ROWID");
        assertEquals(List.of(TableOption.Kind.STRICT, TableOption.Kind.WITHOUT_ROWID),
                createTable.getTableOptions().stream().map(TableOption::getKind)
                        .collect(Collectors.toList()));
    }
}

