/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 * Verifies that all non-reserved keywords can be used as unquoted identifiers (schema, table,
 * column, alias, function names).
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class KeywordsTest {

    public static Stream<String> nonReservedKeywords() {
        return ParserKeywordsUtils.getNonReservedKeywords().stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameWithoutValue(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s AS %1$s from %1$s.%1$s AS %1$s", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameExt(String keyword) throws JSQLParserException {
        String sqlStr = String.format(
                "SELECT %1$s.%1$s.%1$s \"%1$s\" from %1$s \"%1$s\" ORDER BY %1$s ", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCombinedTokenKeywords() throws JSQLParserException {
        String sqlStr = "SELECT current_date(3)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // #2473: GROUPS is a non-reserved keyword in PostgreSQL and must stay usable as an
    // identifier, although it also introduces the GROUPS window frame unit.
    @Test
    public void testGroupsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM groups", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (groups INT)", true);
    }

    // ERRORS introduces Exasol's IMPORT/EXPORT error clauses but is an ordinary name elsewhere,
    // e.g. a DuckDB column holding the errors of an upstream step.
    @Test
    public void testErrorsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT errors FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT t.errors AS errors FROM mytable t", true);
        assertSqlCanBeParsedAndDeparsed("WITH errors AS (SELECT 1) SELECT * FROM errors", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (errors VARCHAR)", true);
    }

    // Dialect-specific clause words (Exasol IMPORT/EXPORT, ClickHouse PREWHERE/SETTINGS, CREATE
    // FUNCTION ... RETURNS) are ordinary names in other dialects, e.g. PostgreSQL or DuckDB.
    @Test
    public void testDialectClauseWordsAsIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT csv, fbv, file, prewhere, returns, script, settings, verify FROM mytable",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT t.file, t.settings FROM mytable t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM settings", true);
        assertSqlCanBeParsedAndDeparsed("WITH file AS (SELECT 1) SELECT * FROM file", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (csv VARCHAR, settings JSON)", true);
    }

    @Test
    public void testClickHouseClausesAfterTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM mytable PREWHERE b = 1 WHERE c = 2", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM mytable SETTINGS max_threads = 8", true);
    }
}
