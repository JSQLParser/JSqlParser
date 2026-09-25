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
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTableQueryOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE LOCAL TEMP TABLE t (id INT)",
            "CREATE LOCAL TEMPORARY TABLE t(a INT) ON COMMIT DROP",
            "CREATE LOCAL TEMP TABLE t AS SELECT 1 AS id WITH NO DATA",
            "CREATE LOCAL TEMPORARY TABLE t (id INT)",
            "CREATE LOCAL TEMPORARY TABLE t AS SELECT 1 AS id WITH NO DATA",
            "CREATE TABLE t (id INT, s TEXT) WITH (toast.autovacuum_enabled=false)",
            "CREATE TABLE t(a TEXT) WITH(toast.autovacuum_enabled=false,fillfactor=80)",
            "CREATE TABLE t (id INT, s TEXT) WITHOUT OIDS",
            "CREATE TABLE t(a INT) WITHOUT OIDS",
            "CREATE TABLE t (id INT, s TEXT) PARTITION BY RANGE(id int4_ops)",
            "CREATE TABLE t(a INT,label TEXT) PARTITION BY RANGE(a int4_ops)",
            "CREATE TABLE t(a INT,label TEXT) PARTITION BY RANGE(label COLLATE \"C\" text_ops)",
            "CREATE TABLE t AS EXECUTE prepared_source(1)",
            "CREATE TABLE t AS EXECUTE q(1) WITH NO DATA"})
    void auditedFormsRoundTrip(String sql) throws JSQLParserException {
        CreateTable table = parse(sql);
        assertRoundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void storageParametersStayMutableAndVisibleToVisitors() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t (id INT) WITH (toast.autovacuum_enabled=false)");
        TableOption option =
                table.getTableOption(TableOption.Kind.STORAGE_PARAMETERS).orElseThrow();
        Index.Option parameter = option.getStorageParameters().get(0);
        assertEquals("toast.autovacuum_enabled", parameter.getName());
        parameter.setName("fillfactor");
        parameter.setValue(new LongValue(70));
        assertEquals("CREATE TABLE t (id INT) WITH (fillfactor = 70)", table.toString());
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(table, visited::add, ignored -> {
        });
        assertEquals(List.of(parameter.getValue()), visited);
        assertRoundTrip(table);
    }

    @Test
    void partitionAttributesShareIndexKeyNodes() throws JSQLParserException {
        CreateTable table = parse(
                "CREATE TABLE t (id INT, other_id INT) PARTITION BY RANGE (id pg_catalog.int4_ops)");
        Index.ColumnParams key = table.getPartitioning().getKeyColumns().get(0);
        assertEquals("pg_catalog.int4_ops", key.getOperatorClass());
        table.getPartitioning().getKeyColumns().set(0,
                new Index.ColumnParams("other_id").withOperatorClass("int4_ops"));
        assertTrue(table.toString().endsWith("PARTITION BY RANGE (other_id int4_ops)"));
        assertRoundTrip(table);
    }

    @Test
    void partitionPrecedesTableOptions() throws JSQLParserException {
        CreateTable table =
                parse("CREATE TABLE t (id INT) PARTITION BY RANGE (id int4_ops) WITHOUT OIDS");
        assertTrue(table.isTableOptionsAfterPartition());
        assertEquals("CREATE TABLE t (id INT) PARTITION BY RANGE (id int4_ops) WITHOUT OIDS",
                table.toString());
        assertRoundTrip(table);
    }

    @Test
    void executeSourceAndArgumentsRemainStructured() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t AS EXECUTE q(1) WITH NO DATA");
        assertNull(table.getSelect());
        assertEquals("q", table.getExecute().getName());
        table.getExecute().setName("q2");
        table.getExecute().getExprList().set(0, new LongValue(2));
        assertEquals("CREATE TABLE t AS EXECUTE q2 (2) WITH NO DATA", table.toString());
        assertRoundTrip(table);
        table.setSelect(
                (net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse("SELECT 1"),
                false);
        assertNull(table.getExecute());
    }

    @Test
    void indexOnlyOrderingIsRejectedForPartitionKeys() {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t (id INT) PARTITION BY RANGE (id DESC)"));
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t (id INT) PARTITION BY RANGE (id NULLS FIRST)"));
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        table.accept(new StatementDeParser(sql), null);
        assertEquals(table.toString(), sql.toString());
        assertEquals(table.toString(), parse(sql.toString()).toString());
    }
}
