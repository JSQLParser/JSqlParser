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
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnIndexOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"PRIMARY KEY", "UNIQUE", "UNIQUE NULLS NOT DISTINCT"})
    void columnStorageAndTablespaceStayOnTheConstraint(String kind) throws JSQLParserException {
        for (boolean alter : new boolean[] {false, true}) {
            String prefix = alter ? "ALTER TABLE t ADD COLUMN " : "CREATE TABLE t (";
            Statement statement = parse(prefix + "id INT CONSTRAINT uq " + kind
                    + " WITH (fillfactor=70) USING INDEX TABLESPACE old_space DEFERRABLE INITIALLY DEFERRED"
                    + (alter ? "" : ")"));
            ColumnDefinition column = alter
                    ? ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0)
                    : ((CreateTable) statement).getColumnDefinitions().get(0);
            assertEquals(1, column.getColumnOptions().size());
            ColumnOption option = column.getColumnOptions().get(0);
            assertEquals(ColumnOption.Kind.CONSTRAINT, option.getKind());
            Index key = option.getConstraint();
            assertEquals("uq", key.getName());
            assertEquals("old_space", key.getTableSpace());
            assertEquals("70", key.getStorageParameters().get(0).getValue().toString());
            assertNotNull(key.getConstraintAttributes());
            key.setStorageParameters(
                    List.of(new Index.Option("fillfactor", new LongValue(80), true)));
            key.setTableSpace("new_space");
            String sql = statement.toString();
            assertTrue(sql.contains("WITH (fillfactor = 80) USING INDEX TABLESPACE new_space"));
            assertFalse(sql.contains("70"));
            assertFalse(sql.contains("old_space"));
            assertRoundTrip(statement);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (id INT) WITH (autovacuum_enabled,toast.autovacuum_enabled=false)",
            "CREATE TABLE t (id INT PRIMARY KEY WITH (deduplicate_items))",
            "CREATE TABLE t (id INT, CONSTRAINT uq UNIQUE(id) WITH (deduplicate_items))",
            "ALTER TABLE t ADD CONSTRAINT uq UNIQUE(id) WITH (deduplicate_items)",
            "CREATE INDEX ix ON t(id) WITH (deduplicate_items)",
            "CREATE INDEX ix ON t USING gin(tags) WITH (fastupdate)",
            "CREATE MATERIALIZED VIEW mv WITH (autovacuum_enabled) AS SELECT * FROM t"})
    void bareStorageOptionsRoundTrip(String sql) throws JSQLParserException {
        assertRoundTrip(parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void bareOptionsCanBeAssignedAndCleared() throws JSQLParserException {
        CreateIndex index = (CreateIndex) parse("CREATE INDEX ix ON t(id) WITH(deduplicate_items)");
        Index.Option option = index.getStorageParameters().get(0);
        assertNull(option.getValue());
        assertFalse(option.isUseEquals());
        option.setName("fillfactor");
        option.setValue(new LongValue(80));
        option.setUseEquals(true);
        assertTrue(index.toString().contains("WITH (fillfactor = 80)"));
        assertRoundTrip(index);
        option.setName("deduplicate_items");
        option.setValue(null);
        assertTrue(index.toString().contains("WITH (deduplicate_items)"));
        assertRoundTrip(index);
    }

    @Test
    void columnIncludeAndIncompleteParametersAreRejected() {
        for (String sql : new String[] {
                "CREATE TABLE t(id INT UNIQUE INCLUDE(label),label TEXT)",
                "CREATE TABLE t(id INT) WITH (fillfactor=)",
                "CREATE TABLE t(id INT) WITH (fillfactor 70)",
                "CREATE INDEX ix ON t(id int4_ops(deduplicate_items))"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}
