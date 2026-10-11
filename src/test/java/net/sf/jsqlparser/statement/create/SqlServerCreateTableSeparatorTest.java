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

import net.sf.jsqlparser.statement.create.table.IndexOptions;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SqlServerCreateTableSeparatorTest {
    private CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql,
                p -> p.withDialect(Dialect.SQLSERVER).withUnsupportedStatements(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id int,)",
            "CREATE TABLE t (id int, name varchar(20),)",
            "CREATE TABLE t (id int, PRIMARY KEY (id),)",
            "CREATE TABLE t (id int, CHECK (id > 0),)",
            "CREATE TABLE t (id int, /* last element */ )"})
    void acceptsOneTrailingSeparatorAndNormalizesBothRenderers(String sql) throws Exception {
        CreateTable table = parse(sql);
        String normalized = table.toString();
        assertFalse(normalized.contains(",)"));
        assertEquals(normalized, parse(normalized).toString());
        StringBuilder buffer = new StringBuilder();
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(normalized, buffer.toString());
        assertEquals(table.getTableElements().size(),
                parse(buffer.toString()).getTableElements().size());
    }

    @Test
    void preservesSakilaColumnsAndNonclusteredPrimaryKey() throws Exception {
        CreateTable table = parse("CREATE TABLE film_text (film_id INT NOT NULL, "
                + "title VARCHAR(255) NOT NULL, description TEXT, PRIMARY KEY NONCLUSTERED (film_id),)");
        assertEquals(4, table.getTableElements().size());
        assertInstanceOf(ColumnDefinition.class, table.getTableElements().get(0));
        KeyConstraint primaryKey =
                assertInstanceOf(KeyConstraint.class, table.getTableElements().get(3));
        assertEquals("PRIMARY KEY", primaryKey.getType());
        assertEquals(IndexOptions.Clustering.NONCLUSTERED, primaryKey.getClustering());
        assertEquals(3, table.getColumnDefinitions().size());
        table.getColumnDefinitions().get(0).setColumnName("renamed_id");
        assertTrue(table.toString().contains("renamed_id INT"));
        assertEquals(2, CCJSqlParserUtil.parseStatements(table + "; SELECT 1;",
                p -> p.withDialect(Dialect.SQLSERVER)).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (,)",
            "CREATE TABLE t (, id int)", "CREATE TABLE t (id int,,)",
            "CREATE TABLE t (id int,, name int)", "CREATE TABLE t (id int, PRIMARY KEY,)",
            "CREATE TABLE t (id int, PRIMARY KEY (id,))",
            "CREATE FUNCTION f() RETURNS @r TABLE (id int,) AS BEGIN RETURN; END"})
    void rejectsMissingElementsAndDoesNotWidenOtherDefinitionLists(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void retainsOtherDialectsAndCreateTableForms() throws Exception {
        String trailing = "CREATE TABLE t (id int,)";
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(trailing));
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.MYSQL, Dialect.ORACLE}) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(trailing, p -> p.withDialect(dialect)));
        }
        for (String sql : new String[] {"CREATE TABLE t (id int, PRIMARY KEY (id))",
                "CREATE TABLE t (id) AS SELECT 1", "CREATE TABLE t AS SELECT 1",
                "CREATE TABLE t ()"}) {
            assertEquals(CCJSqlParserUtil.parse(sql).toString(), parse(sql).toString());
        }
    }
}
