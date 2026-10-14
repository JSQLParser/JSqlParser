/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlColumnPositionTest {
    private static final Dialect[] DIALECTS = {null, Dialect.MYSQL};

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t1 ADD COLUMN c1 INT NOT NULL DEFAULT 0 COMMENT 'x' AFTER id",
            "ALTER TABLE t1 ADD c2 INT FIRST",
            "ALTER TABLE t1 ADD COLUMN c5 INT, ADD COLUMN c6 INT AFTER c5, ADD INDEX idx_c5 (c5), "
                    + "ALGORITHM = INPLACE, LOCK = NONE",
            "ALTER TABLE t1 MODIFY COLUMN name VARCHAR (200) CHARACTER SET utf8mb4 "
                    + "COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'n' AFTER id",
            "ALTER TABLE t1 CHANGE name name VARCHAR (10) FIRST",
            "ALTER TABLE t1 ADD COLUMN geo POINT NOT NULL SRID 4326 AFTER id",
            "ALTER TABLE t1 MODIFY c1 INT FIRST, MODIFY c2 INT AFTER `c1`",
            "ALTER TABLE t1 CHANGE COLUMN a b INT UNIQUE KEY AFTER c"})
    void roundTripsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(sql, true, dialect(dialect));
            for (AlterExpression action : alter.getAlterExpressions()) {
                if (action.getColDataTypeList() == null) {
                    continue;
                }
                for (AlterExpression.ColumnDataType column : action.getColDataTypeList()) {
                    assertTrue(column.getColumnOptions().stream()
                            .noneMatch(option -> option.getKind() == ColumnOption.Kind.OTHER),
                            sql);
                }
            }
        }
    }

    @Test
    void exposesPositionSeparatelyFromOptions() throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            Alter alter = parse("ALTER TABLE t1 ADD COLUMN c1 INT NOT NULL AFTER `id`, "
                    + "MODIFY c2 INT FIRST, MODIFY c3 INT", dialect);
            AlterExpression.ColumnDataType after = column(alter, 0);
            assertEquals(ColumnPosition.Kind.AFTER, after.getPosition().getKind());
            assertEquals("`id`", after.getPosition().getColumnName());
            assertEquals(List.of(ColumnOption.Kind.NULLABILITY), after.getColumnOptions().stream()
                    .map(ColumnOption::getKind).collect(Collectors.toList()));
            assertEquals(List.of("NOT", "NULL", "AFTER", "`id`"), after.getColumnSpecs());

            AlterExpression.ColumnDataType first = column(alter, 1);
            assertEquals(ColumnPosition.first(), first.getPosition());
            assertNull(first.getPosition().getColumnName());
            assertTrue(first.getColumnOptions().isEmpty());
            assertEquals(List.of("FIRST"), first.getColumnSpecs());
            assertNull(column(alter, 2).getPosition());
            assertEquals(List.of(), column(alter, 2).getColumnSpecs());
        }
    }

    @Test
    void editsRenderAfterOptionsInBothRenderers() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t1 MODIFY c1 INT NOT NULL FIRST", Dialect.MYSQL);
        AlterExpression.ColumnDataType column = column(alter, 0);
        List<String> snapshot = column.getColumnSpecs();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("AFTER"));

        column.setPosition(ColumnPosition.after("c0"));
        column.addColumnSpecs("INVISIBLE");
        assertEquals(List.of("NOT", "NULL", "FIRST"), snapshot);
        assertEquals(List.of("NOT", "NULL", "INVISIBLE", "AFTER", "c0"), column.getColumnSpecs());
        assertEquals("ALTER TABLE t1 MODIFY c1 INT NOT NULL INVISIBLE AFTER c0", alter.toString());
        assertRoundTrip(alter);

        column.setPosition(null);
        assertEquals("ALTER TABLE t1 MODIFY c1 INT NOT NULL INVISIBLE", alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void replacingSpecificationsAlsoReplacesThePosition() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t1 ADD COLUMN c1 INT NOT NULL AFTER id", Dialect.MYSQL);
        AlterExpression.ColumnDataType column = column(alter, 0);
        column.setColumnSpecs(column.getColumnSpecs());
        assertNull(column.getPosition());
        assertEquals(1, column.getColumnOptions().size());
        assertEquals(ColumnOption.Kind.OTHER, column.getColumnOptions().get(0).getKind());
        assertEquals("ALTER TABLE t1 ADD COLUMN c1 INT NOT NULL AFTER id", alter.toString());

        column.withColumnSpecs(null).withPosition(ColumnPosition.first());
        assertEquals(List.of("FIRST"), column.getColumnSpecs());
        assertEquals("ALTER TABLE t1 ADD COLUMN c1 INT FIRST", alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void buildsPositionedDefinitions() throws JSQLParserException {
        AlterExpression action = new AlterExpression();
        action.setOperation(AlterOperation.ADD);
        action.hasColumn(true);
        action.addColDataType(new AlterExpression.ColumnDataType("c1", false,
                new ColDataType("INT"), null).withPosition(ColumnPosition.after("id")));
        Alter alter = new Alter().withTable(new Table("t1")).addAlterExpressions(action);
        assertEquals("ALTER TABLE t1 ADD COLUMN c1 INT AFTER id", alter.toString());
        assertRoundTrip(alter);

        assertEquals(ColumnPosition.after("id"), ColumnPosition.after("id"));
        assertEquals(ColumnPosition.after("id").hashCode(),
                ColumnPosition.after("id").hashCode());
        assertNotEquals(ColumnPosition.after("id"), ColumnPosition.first());
        assertThrows(NullPointerException.class, () -> ColumnPosition.after(null));
        assertThrows(UnsupportedOperationException.class,
                () -> ColumnPosition.after("id").getTokens().set(0, "BEFORE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t1 ADD COLUMN c1 INT AFTER id NOT NULL",
            "ALTER TABLE t1 MODIFY c1 INT FIRST AFTER id"})
    void requiresThePositionToEndTheDefinition(String sql) {
        for (Dialect dialect : DIALECTS) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    private static AlterExpression.ColumnDataType column(Alter alter, int action) {
        return alter.getAlterExpressions().get(action).getColDataTypeList().get(0);
    }

    private static Consumer<CCJSqlParser> dialect(Dialect dialect) {
        return parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        };
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, dialect(dialect));
    }

    private static void assertRoundTrip(Alter alter) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        alter.accept(new StatementDeParser(sql), null);
        assertEquals(alter.toString(), sql.toString());
        for (Dialect dialect : DIALECTS) {
            assertEquals(sql.toString(), parse(sql.toString(), dialect).toString());
        }
    }
}
