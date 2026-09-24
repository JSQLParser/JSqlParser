/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.CreateTable.DuplicateHandling;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlCreateTableDuplicatesTest {
    @ParameterizedTest
    @ValueSource(strings = {"SELECT id FROM source", "TABLE source", "VALUES ROW(1), ROW(2)",
            "(SELECT id FROM source)", "WITH c AS (SELECT 1 AS id) SELECT id FROM c"})
    void retainsModifierAndSourceAfterTableOptions(String query) throws JSQLParserException {
        for (DuplicateHandling handling : DuplicateHandling.values()) {
            for (boolean useAs : new boolean[] {false, true}) {
                CreateTable table = parse("CREATE TABLE t (id INT PRIMARY KEY) ENGINE=InnoDB "
                        + handling + (useAs ? " AS " : " ") + query);
                assertEquals(handling, table.getDuplicateHandling());
                assertEquals(useAs, table.isUseAsKeyword());
                assertNotNull(table.getSelect());
                roundTrip(table);
            }
        }
    }

    @Test
    void preservesPartitionAndAllowsAstMutation() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t (id INT PRIMARY KEY) PARTITION BY HASH(id) "
                + "PARTITIONS 2 IGNORE AS SELECT id FROM source");
        assertNotNull(table.getPartitioning());
        assertNull(table.getPartitioning().getPartitionOptions());
        table.setDuplicateHandling(DuplicateHandling.REPLACE);
        assertTrue(table.toString().contains(" REPLACE AS SELECT"));
        roundTrip(table);
        table.setDuplicateHandling(null);
        assertFalse(table.toString().contains("REPLACE"));
        roundTrip(table);
        assertNull(parse("CREATE TABLE t AS SELECT 1").getDuplicateHandling());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t IGNORE", "CREATE TABLE t REPLACE AS",
            "CREATE TABLE t IGNORE REPLACE SELECT 1",
            "CREATE TABLE t ENGINE=InnoDB, IGNORE SELECT 1"})
    void rejectsMissingQueriesAndConflictingModifiers(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void roundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        table.accept(new StatementDeParser(out));
        assertEquals(table.toString(), out.toString());
        CreateTable parsed = parse(out.toString());
        assertEquals(table.getDuplicateHandling(), parsed.getDuplicateHandling());
        assertEquals(table.isUseAsKeyword(), parsed.isUseAsKeyword());
        assertEquals(out.toString(), parsed.toString());
    }
}
