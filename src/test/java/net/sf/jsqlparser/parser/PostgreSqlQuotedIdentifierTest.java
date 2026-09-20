/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlQuotedIdentifierTest {
    static Stream<Arguments> names() {
        return Stream.of(Arguments.of("plain", null, "plain"),
                Arguments.of("\"normal\"", null, "\"normal\""),
                Arguments.of("\"a.b\"", null, "\"a.b\""),
                Arguments.of("public.\"a.b\"", "public", "\"a.b\""),
                Arguments.of("\"s.p\".\"a.b\"", "\"s.p\"", "\"a.b\""),
                Arguments.of("\"a\"\"b.c\"", null, "\"a\"\"b.c\""),
                Arguments.of("\"한.글\"", null, "\"한.글\""),
                Arguments.of("\"a.\"", null, "\"a.\""),
                Arguments.of("\".b\"", null, "\".b\""),
                Arguments.of("\"a..b\"", null, "\"a..b\""),
                Arguments.of("\".\"", null, "\".\""));
    }

    @ParameterizedTest
    @MethodSource("names")
    void preservesObjectNamesAcrossStatements(String name, String schema, String leaf)
            throws Exception {
        for (String sql : List.of("DROP INDEX " + name, "DROP TABLE IF EXISTS " + name,
                "CREATE TABLE " + name + " (c int)", "SELECT c FROM " + name,
                "INSERT INTO " + name + " (c) VALUES (1)", "UPDATE " + name + " SET c = 1",
                "DELETE FROM " + name, "ALTER TABLE " + name + " ADD COLUMN d int",
                "COMMENT ON TABLE " + name + " IS $tag$body$tag$")) {
            for (Statement statement : roundTrip(sql)) {
                Table table = tableOf(statement);
                assertEquals(leaf, table.getName(), sql);
                assertEquals(schema, table.getSchemaName(), sql);
                assertNull(table.getDatabaseName(), sql);
                assertEquals(name, table.getFullyQualifiedName(), sql);
                assertEquals(table.getNameParts(), table.clone().getNameParts(), sql);
                assertEquals(name, table.clone().getFullyQualifiedName(), sql);
                Table reference = new Table("ref").setResolvedTable(table);
                assertNotSame(table, reference.getResolvedTable());
                assertEquals(table.getNameParts(), reference.getResolvedTable().getNameParts());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"c", "\"a.b\"", "\"a\"\"b.c\"", "\"한.글\"", "\"a.\"", "\".\""})
    void preservesColumnAndQualifierComponents(String name) throws Exception {
        for (String ref : List.of(name, "\"t.x\"." + name, "\"s.p\".\"t.x\"." + name)) {
            for (Statement statement : roundTrip("SELECT " + ref + " FROM \"s.p\".\"t.x\"")) {
                Column column =
                        ((PlainSelect) statement).getSelectItem(0).getExpression(Column.class);
                assertEquals(name, column.getColumnName());
                assertEquals(ref, column.getFullyQualifiedName());
                if (ref.equals(name)) {
                    assertNull(column.getTable());
                } else {
                    assertEquals("\"t.x\"", column.getTable().getName());
                }
            }
        }
        for (Statement statement : roundTrip(
                "COMMENT ON COLUMN \"t.x\"." + name + " IS $$body$$")) {
            Column column = ((Comment) statement).getColumn();
            assertEquals(name, column.getColumnName());
            assertEquals("\"t.x\"", column.getTable().getName());
        }
        for (Statement statement : roundTrip("INSERT INTO \"t.x\" (" + name + ") VALUES (1)")) {
            assertEquals(name, ((Insert) statement).getColumns().get(0).getColumnName());
        }
    }

    @Test
    void preservesEveryNameInDropList() throws Exception {
        for (Statement statement : roundTrip(
                "DROP INDEX IF EXISTS \"a.b\", public.\"c.d\" CASCADE")) {
            Drop drop = (Drop) statement;
            assertEquals("\"a.b\"", drop.getNames().get(0).getName());
            assertNull(drop.getNames().get(0).getSchemaName());
            assertEquals("\"c.d\"", drop.getNames().get(1).getName());
            assertEquals("public", drop.getNames().get(1).getSchemaName());
        }
    }

    @Test
    void keepsLegacyBigQuerySplittingAndParserIsolation() throws Exception {
        PlainSelect postgres = (PlainSelect) parse("SELECT \"a.b\" FROM \"s.t\"");
        assertEquals("\"a.b\"",
                postgres.getSelectItem(0).getExpression(Column.class).getColumnName());
        for (boolean explicitDialect : List.of(false, true)) {
            PlainSelect bigquery = (PlainSelect) CCJSqlParserUtil.parse("SELECT * FROM `d.s.t`",
                    parser -> {
                        if (explicitDialect) {
                            parser.withDialect(Dialect.BIGQUERY);
                        }
                    });
            Table table = (Table) bigquery.getFromItem();
            assertEquals("d", table.getUnquotedDatabaseName());
            assertEquals("s", table.getUnquotedSchemaName());
            assertEquals("t", table.getUnquotedName());
        }
    }

    private static Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static List<Statement> roundTrip(String sql) throws Exception {
        Statement statement = parse(sql);
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(statement.toString(), buffer.toString());
        return List.of(statement, parse(statement.toString()), parse(buffer.toString()));
    }

    private static Table tableOf(Statement statement) {
        if (statement instanceof Drop) {
            return ((Drop) statement).getName();
        }
        if (statement instanceof CreateTable) {
            return ((CreateTable) statement).getTable();
        }
        if (statement instanceof PlainSelect) {
            return (Table) ((PlainSelect) statement).getFromItem();
        }
        if (statement instanceof Insert) {
            return ((Insert) statement).getTable();
        }
        if (statement instanceof Update) {
            return ((Update) statement).getTable();
        }
        if (statement instanceof Delete) {
            return ((Delete) statement).getTable();
        }
        if (statement instanceof Alter) {
            return ((Alter) statement).getTable();
        }
        return ((Comment) statement).getTable();
    }
}
