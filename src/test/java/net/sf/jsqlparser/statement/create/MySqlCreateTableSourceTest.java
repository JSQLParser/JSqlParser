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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlCreateTableSourceTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t AS TABLE db.s", "CREATE TABLE t TABLE db.s",
            "CREATE TABLE t (id BIGINT) TABLE db.s",
            "CREATE TEMPORARY TABLE IF NOT EXISTS t ENGINE=InnoDB TABLE `db`.`s`",
            "CREATE TABLE t (extra INT) ENGINE=InnoDB, COMMENT='source' AS TABLE s ORDER BY id DESC LIMIT 2 OFFSET 1",
            "CREATE TABLE t AS SELECT 1 AS id", "CREATE TABLE t SELECT 1 AS id",
            "CREATE TABLE t AS (SELECT 1 AS id)"})
    void roundTripsQuerySources(String sql) throws Exception {
        CreateTable table = parse(sql);
        assertNotNull(table.getSelect());
        if (sql.contains("TABLE db.") || sql.contains("TABLE `db`")) {
            assertInstanceOf(TableStatement.class, table.getSelect());
        }
        assertRoundTrip(table);
    }

    @Test
    void retainsSourceStructureAndAsChoiceAfterMutation() throws Exception {
        CreateTable table = parse("CREATE TABLE target TABLE old.source");
        assertFalse(table.isUseAsKeyword());
        TableStatement source = assertInstanceOf(TableStatement.class, table.getSelect());
        source.getTable().setSchemaName("newdb");
        assertThat(new TablesNamesFinder().getTables(table))
                .containsExactlyInAnyOrder("target", "newdb.source");
        assertEquals("CREATE TABLE target TABLE newdb.source", table.toString());
        table.setUseAsKeyword(true);
        assertEquals("CREATE TABLE target AS TABLE newdb.source", table.toString());
        assertRoundTrip(table);
        assertTrue(new CreateTable().isUseAsKeyword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t AS TABLE", "CREATE TABLE t TABLE",
            "CREATE TABLE t ENGINE=InnoDB, TABLE s", "CREATE TABLE t AS"})
    void rejectsMissingSourcesAndDanglingOptionSeparators(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(CreateTable table) throws Exception {
        StringBuilder output = new StringBuilder();
        table.accept(new StatementDeParser(output), null);
        assertEquals(table.toString(), output.toString());
        CreateTable reparsed = parse(output.toString());
        assertEquals(table.toString(), reparsed.toString());
        assertEquals(table.isUseAsKeyword(), reparsed.isUseAsKeyword());
    }
}
