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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.ColumnOption.ColumnFormat;
import net.sf.jsqlparser.statement.create.table.ConstraintDeclaration;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlColumnAttributeTest {
    private static final Dialect[] DIALECTS = {null, Dialect.MYSQL};

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t4 (id INT PRIMARY KEY, g GEOMETRY NOT NULL SRID 4326)",
            "CREATE TABLE ty5 (c3 MULTIPOLYGON SRID 0, c4 LINESTRING)",
            "CREATE TABLE t1 (id INT PRIMARY KEY AUTO_INCREMENT, g POINT SRID 4326 NOT NULL)",
            "CREATE TABLE t18 (a INT COLUMN_FORMAT FIXED STORAGE DISK, b INT COLUMN_FORMAT DYNAMIC, "
                    + "c INT COLUMN_FORMAT DEFAULT STORAGE MEMORY)",
            "CREATE TABLE t19 (a INT ENGINE_ATTRIBUTE '{\"k\":1}' SECONDARY_ENGINE_ATTRIBUTE '{}', "
                    + "b JSON ENGINE_ATTRIBUTE = '{}' SECONDARY_ENGINE_ATTRIBUTE = '{}')",
            "CREATE TABLE t6 (id INT NOT NULL PRIMARY KEY, code VARCHAR (10) UNIQUE KEY, "
                    + "qty INT CHECK (qty > 0))",
            "CREATE TABLE t1 (code VARCHAR (10) UNIQUE)", "CREATE TABLE t1 (id INT KEY)",
            "CREATE TABLE t1 (id INT KEY AUTO_INCREMENT, code INT UNIQUE KEY NOT NULL)",
            "ALTER TABLE t1 ADD COLUMN geo POINT NOT NULL SRID 4326",
            "ALTER TABLE t1 MODIFY COLUMN c INT COLUMN_FORMAT FIXED ENGINE_ATTRIBUTE = '{}'",
            "ALTER TABLE t1 ADD COLUMN code INT UNIQUE KEY",
            "ALTER TABLE t1 CHANGE COLUMN a id INT KEY"})
    void roundTripsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, dialect(dialect));
            assertTrue(columns(statement).stream().flatMap(MySqlColumnAttributeTest::options)
                    .noneMatch(option -> option.getKind() == ColumnOption.Kind.OTHER), sql);
        }
    }

    @Test
    void exposesTypedAttributesWithLegacyTokens() throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            CreateTable table = (CreateTable) parse("CREATE TABLE t ("
                    + "g GEOMETRY NOT NULL SRID 4326, a INT COLUMN_FORMAT FIXED STORAGE DISK, "
                    + "b INT COLUMN_FORMAT DEFAULT, c JSON ENGINE_ATTRIBUTE '{\"k\":1}' "
                    + "SECONDARY_ENGINE_ATTRIBUTE = '{}')", dialect);
            List<ColumnDefinition> columns = table.getColumnDefinitions();

            ColumnOption srid = columns.get(0).getColumnOptions().get(1);
            assertEquals(ColumnOption.Kind.SRID, srid.getKind());
            assertEquals(4326L, srid.getSrid());
            assertEquals(List.of("NOT", "NULL", "SRID", "4326"), columns.get(0).getColumnSpecs());

            ColumnOption fixed = columns.get(1).getColumnOptions().get(0);
            assertEquals(ColumnOption.Kind.COLUMN_FORMAT, fixed.getKind());
            assertEquals(ColumnFormat.FIXED, fixed.getColumnFormat());
            assertEquals(ColumnOption.Kind.STORAGE,
                    columns.get(1).getColumnOptions().get(1).getKind());
            assertEquals(ColumnFormat.DEFAULT,
                    columns.get(2).getColumnOptions().get(0).getColumnFormat());
            assertEquals(List.of("COLUMN_FORMAT", "DEFAULT"), columns.get(2).getColumnSpecs());

            ColumnOption engine = columns.get(3).getColumnOptions().get(0);
            ColumnOption secondary = columns.get(3).getColumnOptions().get(1);
            assertEquals(ColumnOption.Kind.ENGINE_ATTRIBUTE, engine.getKind());
            assertEquals("{\"k\":1}", engine.getEngineAttribute().getValue());
            assertFalse(engine.isUseEquals());
            assertEquals(ColumnOption.Kind.SECONDARY_ENGINE_ATTRIBUTE, secondary.getKind());
            assertTrue(secondary.isUseEquals());
            assertEquals(List.of("ENGINE_ATTRIBUTE", "'{\"k\":1}'", "SECONDARY_ENGINE_ATTRIBUTE",
                    "=", "'{}'"), columns.get(3).getColumnSpecs());
            assertEquals(List.of(ConstraintKind.NOT_NULL), table.getConstraints().stream()
                    .map(ConstraintDeclaration::getKind).collect(Collectors.toList()));
        }
    }

    @Test
    void typedSettersChangeBothRenderersAndLeaveSnapshotsDetached() throws JSQLParserException {
        Statement statement = parse("CREATE TABLE t (g POINT SRID 4326, a INT COLUMN_FORMAT FIXED, "
                + "c JSON SECONDARY_ENGINE_ATTRIBUTE '{}')", Dialect.MYSQL);
        List<ColumnDefinition> columns = columns(statement);
        ColumnOption srid = columns.get(0).getColumnOptions().get(0);
        ColumnOption format = columns.get(1).getColumnOptions().get(0);
        ColumnOption attribute = columns.get(2).getColumnOptions().get(0);
        List<String> snapshot = attribute.getTokens();
        assertReadOnly(snapshot);

        srid.setSrid(0);
        format.setColumnFormat(ColumnFormat.DYNAMIC);
        attribute.setEngineAttribute(new StringValue("{\"k\":2}"));
        attribute.setUseEquals(true);

        assertEquals(List.of("SECONDARY_ENGINE_ATTRIBUTE", "'{}'"), snapshot);
        assertEquals(List.of("SECONDARY_ENGINE_ATTRIBUTE", "=", "'{\"k\":2}'"),
                attribute.getTokens());
        assertEquals("CREATE TABLE t (g POINT SRID 0, a INT COLUMN_FORMAT DYNAMIC, "
                + "c JSON SECONDARY_ENGINE_ATTRIBUTE = '{\"k\":2}')", statement.toString());
        assertRoundTrip(statement);
        assertThrows(IllegalStateException.class, () -> srid.setTokens(List.of("SRID", "1")));
        assertThrows(NullPointerException.class, () -> format.setColumnFormat(null));
        assertThrows(NullPointerException.class, () -> attribute.setEngineAttribute(null));
    }

    @Test
    void engineAttributeLiteralsAreVisitedLikeComments() throws JSQLParserException {
        Statement statement = parse(
                "ALTER TABLE t ADD COLUMN c JSON COMMENT 'c' ENGINE_ATTRIBUTE '{}'", Dialect.MYSQL);
        ColumnDefinition column = columns(statement).get(0);
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(column, visited::add, ignored -> {
        });
        assertEquals(2, visited.size());
        assertSame(column.getColumnOptions().get(1).getEngineAttribute(), visited.get(1));
    }

    @ParameterizedTest
    @MethodSource("factories")
    void factoriesRenderCanonicalSql(ColumnOption option, String sql, List<String> tokens) {
        assertEquals(sql, option.toString());
        assertEquals(tokens, option.getTokens());
        assertReadOnly(option.getTokens());
        assertThrows(IllegalStateException.class, () -> option.setTokens(List.of("raw")));
    }

    private static Stream<Object[]> factories() {
        ColumnOption withEquals = ColumnOption.engineAttribute(new StringValue("{}"));
        withEquals.setUseEquals(true);
        return Stream.of(
                new Object[] {ColumnOption.srid(4326), "SRID 4326", List.of("SRID", "4326")},
                new Object[] {ColumnOption.columnFormat(ColumnFormat.FIXED), "COLUMN_FORMAT FIXED",
                        List.of("COLUMN_FORMAT", "FIXED")},
                new Object[] {ColumnOption.engineAttribute(new StringValue("{}")),
                        "ENGINE_ATTRIBUTE '{}'", List.of("ENGINE_ATTRIBUTE", "'{}'")},
                new Object[] {withEquals, "ENGINE_ATTRIBUTE = '{}'",
                        List.of("ENGINE_ATTRIBUTE", "=", "'{}'")},
                new Object[] {ColumnOption.secondaryEngineAttribute(new StringValue("{}")),
                        "SECONDARY_ENGINE_ATTRIBUTE '{}'",
                        List.of("SECONDARY_ENGINE_ATTRIBUTE", "'{}'")},
                new Object[] {
                        ColumnOption.constraint(new KeyConstraint().withType("KEY")
                                .withKind(ConstraintKind.PRIMARY_KEY)),
                        "KEY", List.of("KEY")},
                new Object[] {ColumnOption.constraint(new KeyConstraint().withType("UNIQUE KEY")),
                        "UNIQUE KEY", List.of("UNIQUE", "KEY")});
    }

    @Test
    void unsupportedColumnFormatsStayOpaque() throws JSQLParserException {
        // Percona Server's compressed columns are not modeled.
        Statement statement =
                parse("CREATE TABLE t (c BLOB COLUMN_FORMAT COMPRESSED)", Dialect.MYSQL);
        assertTrue(options(columns(statement).get(0))
                .allMatch(option -> option.getKind() == ColumnOption.Kind.OTHER));
        assertEquals("CREATE TABLE t (c BLOB COLUMN_FORMAT COMPRESSED)", statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (code VARCHAR(10) UNIQUE KEY NOT NULL)",
            "ALTER TABLE t ADD COLUMN code VARCHAR(10) UNIQUE KEY NOT NULL"})
    void uniqueKeyIsOneConstraint(String sql) throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            Statement statement = parse(sql, dialect);
            ColumnDefinition column = columns(statement).get(0);
            assertEquals(2, column.getColumnOptions().size());
            ColumnOption unique = column.getColumnOptions().get(0);
            assertEquals(ColumnOption.Kind.CONSTRAINT, unique.getKind());
            assertEquals(ConstraintKind.UNIQUE, unique.getConstraintKind());
            assertEquals("UNIQUE KEY", unique.getConstraint().getType());
            assertEquals(List.of("UNIQUE", "KEY", "NOT", "NULL"), column.getColumnSpecs());
            assertRoundTrip(statement);
        }
    }

    @Test
    void bareKeyIsAPrimaryKeyThatKeepsItsSpelling() throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            CreateTable table =
                    (CreateTable) parse("CREATE TABLE t (id INT KEY, name TEXT)", dialect);
            ColumnOption key = table.getColumnDefinitions().get(0).getColumnOptions().get(0);
            KeyConstraint constraint = assertInstanceOf(KeyConstraint.class, key.getConstraint());
            assertEquals("KEY", constraint.getType());
            assertEquals(ConstraintKind.PRIMARY_KEY, constraint.getKind());
            assertEquals(ConstraintKind.PRIMARY_KEY, key.getConstraintKind());
            assertEquals(List.of("KEY"), key.getTokens());
            ConstraintDeclaration declaration = table.getConstraints().get(0);
            assertEquals(ConstraintKind.PRIMARY_KEY, declaration.getKind());
            assertSame(key, declaration.getColumnOption());
            assertEquals("CREATE TABLE t (id INT KEY, name TEXT)", table.toString());
            assertRoundTrip(table);

            constraint.setType("PRIMARY KEY");
            assertEquals(ConstraintKind.PRIMARY_KEY, key.getConstraintKind());
            assertEquals("CREATE TABLE t (id INT PRIMARY KEY, name TEXT)", table.toString());
        }
    }

    @Test
    void inlineForeignKeyPrefixIsNotAPrimaryKey() throws JSQLParserException {
        CreateTable table = (CreateTable) parse(
                "CREATE TABLE t (user_id INT FOREIGN KEY REFERENCES ra_user(id))", null);
        List<ColumnOption> options = table.getColumnDefinitions().get(0).getColumnOptions();
        assertEquals(List.of(ColumnOption.Kind.OTHER, ColumnOption.Kind.REFERENCE),
                options.stream().map(ColumnOption::getKind).collect(Collectors.toList()));
        assertEquals(List.of("FOREIGN", "KEY"), options.get(0).getTokens());
        assertEquals(List.of(ConstraintKind.FOREIGN_KEY), table.getConstraints().stream()
                .map(ConstraintDeclaration::getKind).collect(Collectors.toList()));
        assertRoundTrip(table);
    }

    private static Consumer<CCJSqlParser> dialect(Dialect dialect) {
        return parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        };
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, dialect(dialect));
    }

    private static List<ColumnDefinition> columns(Statement statement) {
        if (statement instanceof CreateTable) {
            return ((CreateTable) statement).getColumnDefinitions();
        }
        return new ArrayList<>(
                ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList());
    }

    private static Stream<ColumnOption> options(ColumnDefinition column) {
        return column.getColumnOptions() == null ? Stream.empty()
                : column.getColumnOptions().stream();
    }

    private static void assertReadOnly(List<String> tokens) {
        assertThrows(UnsupportedOperationException.class, () -> tokens.set(0, "changed"));
        assertThrows(UnsupportedOperationException.class, () -> tokens.add("changed"));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString(), Dialect.MYSQL).toString());
        assertEquals(sql.toString(), parse(sql.toString(), null).toString());
    }
}
