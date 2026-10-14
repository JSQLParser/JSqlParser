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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AlterDropBehaviorTest {
    private static final Dialect[] DIALECTS = {Dialect.POSTGRESQL, null};

    @ParameterizedTest
    @CsvSource(value = {"DROP COLUMN b RESTRICT|RESTRICT", "DROP b RESTRICT|RESTRICT",
            "DROP COLUMN IF EXISTS b RESTRICT|RESTRICT", "DROP IF EXISTS b CASCADE|CASCADE",
            "DROP COLUMN b CASCADE|CASCADE", "DROP COLUMN b|"}, delimiter = '|')
    void columnDropsExposeTheirBehavior(String action, CascadeBehavior behavior)
            throws JSQLParserException {
        String sql = "ALTER TABLE users " + action;
        for (Dialect dialect : DIALECTS) {
            Alter alter = parse(sql, dialect);
            AlterExpressionDrop drop =
                    assertInstanceOf(AlterExpressionDrop.class, alter.getAlterExpressions().get(0));
            assertEquals("b", drop.getColumnName());
            assertEquals(action.contains("IF EXISTS"), drop.isUsingIfExists());
            assertEquals(behavior, drop.getBehavior());
            assertEquals(behavior == null ? null : List.of(behavior.name()),
                    drop.getParameters());
            assertRoundTrip(alter, sql, dialect);
        }
    }

    @Test
    void restrictAppliesToItsOwnAction() throws JSQLParserException {
        String sql = "ALTER TABLE users DROP a, DROP COLUMN b RESTRICT";
        for (Dialect dialect : DIALECTS) {
            Alter alter = parse(sql, dialect);
            assertNull(((AlterExpressionDrop) alter.getAlterExpressions().get(0)).getBehavior());
            assertEquals(CascadeBehavior.RESTRICT,
                    ((AlterExpressionDrop) alter.getAlterExpressions().get(1)).getBehavior());
            assertRoundTrip(alter, sql, dialect);
        }
    }

    @ParameterizedTest
    @CsvSource(value = {"DROP CONSTRAINT IF EXISTS c RESTRICT|RESTRICT",
            "DROP CONSTRAINT c CASCADE|CASCADE", "DROP PRIMARY KEY CASCADE|CASCADE",
            "DROP UNIQUE (a, b) RESTRICT|RESTRICT", "DROP FOREIGN KEY (a) CASCADE|CASCADE"},
            delimiter = '|')
    void constraintDropsShareTheBehavior(String action, CascadeBehavior behavior)
            throws JSQLParserException {
        String sql = "ALTER TABLE t " + action;
        AlterExpressionDrop drop = assertInstanceOf(AlterExpressionDrop.class,
                parse(sql, null).getAlterExpressions().get(0));
        assertEquals(behavior, drop.getBehavior());
        assertEquals(List.of(behavior.name()), drop.getParameters());
        assertRoundTrip(parse(sql, null), sql, null);
    }

    @ParameterizedTest
    @CsvSource(value = {"DROP COLUMN c CASCADE CONSTRAINTS||CASCADE,CONSTRAINTS",
            "DROP COLUMN c INVALIDATE CASCADE CONSTRAINTS||INVALIDATE,CASCADE,CONSTRAINTS",
            "DROP COLUMN c INVALIDATE CASCADE|CASCADE|INVALIDATE,CASCADE",
            "DROP (a, b) CASCADE CONSTRAINTS||CASCADE,CONSTRAINTS"}, delimiter = '|')
    void oracleClausesRemainRawParameters(String action, CascadeBehavior behavior,
            String parameters) throws JSQLParserException {
        String sql = "ALTER TABLE t " + action;
        for (Dialect dialect : new Dialect[] {Dialect.ORACLE, null}) {
            Alter alter = parse(sql, dialect);
            AlterExpressionDrop drop = (AlterExpressionDrop) alter.getAlterExpressions().get(0);
            assertEquals(behavior, drop.getBehavior());
            assertEquals(List.of(parameters.split(",")), drop.getParameters());
            assertRoundTrip(alter, sql, dialect);
        }
    }

    @Test
    void editingTheBehaviorUpdatesBothRenderersAndLegacyParameters()
            throws JSQLParserException {
        Alter alter = parse("ALTER TABLE users DROP COLUMN b RESTRICT", Dialect.POSTGRESQL);
        AlterExpressionDrop drop = (AlterExpressionDrop) alter.getAlterExpressions().get(0);
        assertThrows(UnsupportedOperationException.class, () -> drop.getParameters().add("X"));

        drop.setBehavior(CascadeBehavior.CASCADE);
        assertEquals(List.of("CASCADE"), drop.getParameters());
        assertRoundTrip(alter, "ALTER TABLE users DROP COLUMN b CASCADE", Dialect.POSTGRESQL);

        drop.setBehavior(null);
        assertNull(drop.getParameters());
        assertRoundTrip(alter, "ALTER TABLE users DROP COLUMN b", Dialect.POSTGRESQL);

        AlterExpressionDrop built =
                new AlterExpressionDrop().withBehavior(CascadeBehavior.RESTRICT);
        built.setOperation(AlterOperation.DROP);
        built.hasColumn(true);
        built.setColumnName("b");
        Alter statement = new Alter();
        statement.setTable(new Table("users"));
        statement.addAlterExpression(built);
        assertRoundTrip(statement, "ALTER TABLE users DROP COLUMN b RESTRICT", Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP COLUMN b RESTRICT CASCADE", "DROP COLUMN b CASCADE RESTRICT",
            "DROP COLUMN b RESTRICT RESTRICT"})
    void rejectsRepeatedBehaviors(String action) {
        for (Dialect dialect : DIALECTS) {
            assertThrows(JSQLParserException.class,
                    () -> parse("ALTER TABLE users " + action, dialect));
        }
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) (dialect == null
                ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)));
    }

    private static void assertRoundTrip(Alter alter, String expected, Dialect dialect)
            throws JSQLParserException {
        assertEquals(expected, alter.toString());
        StringBuilder deparsed = new StringBuilder();
        alter.accept(new StatementDeParser(deparsed), null);
        assertEquals(expected, deparsed.toString());
        assertEquals(expected, parse(deparsed.toString(), dialect).toString());
    }
}
