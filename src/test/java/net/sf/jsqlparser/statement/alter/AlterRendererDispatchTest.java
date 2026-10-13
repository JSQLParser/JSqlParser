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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnSetDefault;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class AlterRendererDispatchTest {
    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void parsedDefaultsRemainEditableAndUseTheExpressionVisitor(Dialect dialect)
            throws JSQLParserException {
        Alter statement = parse("ALTER TABLE t ALTER COLUMN amount SET DEFAULT (1 + 2)", dialect);
        AlterExpression action = statement.getAlterExpressions().get(0);
        addInactiveColumn(action);
        ColumnDefaultAction defaultAction = action.getColumnDefaults().get(0);
        assertSame(action.getColumnSetDefaultList().get(0), defaultAction);
        defaultAction.setColumnName("renamed");
        defaultAction.setDefaultExpression(CCJSqlParserUtil.parseExpression("(3 + 4)"));
        assertFalse(action.hasActiveColumnDefinitions());
        String expected = "ALTER TABLE t ALTER COLUMN renamed SET DEFAULT (3 + 4)";
        assertEquals(expected, statement.toString());
        assertRenderersAgree(statement);
        assertEquals(expected, parse(expected, dialect).toString());

        List<Long> visited = new ArrayList<>();
        assertEquals("ALTER TABLE t ALTER COLUMN renamed SET DEFAULT (103 + 104)",
                deparse(statement, visited));
        assertEquals(List.of(3L, 4L), visited);
    }

    @Test
    void dropFallbackKeepsIdentityBodyInsteadOfInactiveDefaultMetadata() {
        AlterExpressionDrop action = new AlterExpressionDrop();
        action.setOperation(AlterOperation.ALTER);
        action.hasColumn(true);
        ColumnDataType column = new ColumnDataType(false).withColumnName("id");
        column.setIdentityAlterations(
                List.of(new IdentityAlteration(IdentityAlteration.Kind.RESTART)));
        action.addColDataType(column);
        action.addColSetDefault(ColumnSetDefault.fromExpression("other", new LongValue(1)));
        Alter statement = statement(action);
        assertTrue(action.hasActiveColumnDefinitions());
        assertTrue(action.getColumnDefaults().isEmpty());
        assertEquals("ALTER TABLE t ALTER COLUMN id RESTART", statement.toString());
        assertRenderersAgree(statement);
        List<Long> visited = new ArrayList<>();
        assertEquals(statement.toString(), deparse(statement, visited));
        assertTrue(visited.isEmpty());
    }

    @Test
    void dropFallbackStillRewritesItsActiveColumnExpressionAndAppendsTailOnce()
            throws JSQLParserException {
        AlterExpressionDrop action = new AlterExpressionDrop();
        action.setOperation(AlterOperation.ALTER);
        action.hasColumn(true);
        ColumnDataType column = new ColumnDataType(true).withColumnName("id")
                .withColDataType(new ColDataType("INT"));
        column.setUsingExpression(CCJSqlParserUtil.parseExpression("id + 2"));
        action.addColDataType(column);
        action.addColSetDefault(ColumnSetDefault.fromExpression("other", new LongValue(1)));
        action.addParameters("CASCADE");
        Alter statement = statement(action);
        assertRenderersAgree(statement);
        List<Long> visited = new ArrayList<>();
        assertEquals("ALTER TABLE t ALTER COLUMN id TYPE INT USING id + 102 CASCADE",
                deparse(statement, visited));
        assertEquals(List.of(2L), visited);
    }

    static Stream<Arguments> fixedBodies() {
        return Stream.of(
                Arguments.of("ALTER TABLE t RENAME TO renamed", Dialect.MYSQL,
                        AlterExpressionRename.class),
                Arguments.of("ALTER TABLE t CONVERT TO CHARACTER SET utf8mb4", Dialect.MYSQL,
                        AlterExpressionCharset.class),
                Arguments.of("ALTER TABLE t ENGINE = InnoDB", Dialect.MYSQL,
                        AlterExpressionTableOption.class),
                Arguments.of("ALTER TABLE t ALTER CONSTRAINT ck DEFERRABLE", Dialect.POSTGRESQL,
                        AlterConstraintAttributes.class),
                Arguments.of("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id)",
                        Dialect.COCKROACHDB, AlterExpressionPrimaryKey.class),
                Arguments.of("ALTER TABLE t DETACH PARTITION p", Dialect.POSTGRESQL,
                        AlterExpressionPartition.class),
                Arguments.of("ALTER TABLE t ORDER BY id", Dialect.MYSQL,
                        AlterExpressionOrderBy.class),
                Arguments.of("ALTER TABLE t OWNER TO alice", Dialect.POSTGRESQL,
                        RelationAlterAction.class),
                Arguments.of("ALTER FOREIGN TABLE t OPTIONS (ADD filename 'file')",
                        Dialect.POSTGRESQL, AlterForeignDataOptions.class));
    }

    @ParameterizedTest
    @MethodSource("fixedBodies")
    void specializedBodiesIgnoreUnrelatedDefaultAndColumnMetadata(String sql, Dialect dialect,
            Class<? extends AlterExpression> type) throws JSQLParserException {
        Alter statement = parse(sql, dialect);
        AlterExpression action = assertInstanceOf(type, statement.getAlterExpressions().get(0));
        String expected = statement.toString();
        action.addColSetDefault(ColumnSetDefault.fromExpression("inactive", new LongValue(99)));
        addInactiveColumn(action);
        assertTrue(action.getColumnDefaults().isEmpty());
        assertFalse(action.hasActiveColumnDefinitions());
        assertEquals(expected, statement.toString());
        assertRenderersAgree(statement);
        List<Long> visited = new ArrayList<>();
        assertEquals(expected, deparse(statement, visited));
        assertTrue(visited.isEmpty());

        // A changed operation does not replace a subclass's own body dispatch.
        action.setOperation(AlterOperation.ALTER);
        assertTrue(action.getColumnDefaults().isEmpty());
        assertFalse(action.hasActiveColumnDefinitions());
        assertRenderersAgree(statement);
        assertEquals(statement.toString(), deparse(statement, visited));
        assertTrue(visited.isEmpty());
    }

    static Stream<Arguments> prioritizedBodies() {
        return Stream.of(
                body(action -> action
                        .addColDropDefault(new AlterExpression.ColumnDropDefault("id"))),
                body(action -> action.addColSetVisibility(
                        new AlterExpression.ColumnSetVisibility("id", true))),
                body(action -> {
                    action.setConstraintType("CHECK");
                    action.setConstraintSymbol("ck");
                    action.setEnforced(true);
                }),
                body(action -> {
                    action.setOperation(AlterOperation.RENAME_TABLE);
                    action.setNewTableName("renamed");
                }),
                body(action -> action.setOperation(AlterOperation.DROP_PRIMARY_KEY)),
                body(action -> {
                    action.setOperation(AlterOperation.UNSPECIFIC);
                    action.setOptionalSpecifier("vendor_action");
                }),
                body(action -> {
                    action.setOperation(AlterOperation.COMMENT);
                    action.setCommentText("'description'");
                }));
    }

    private static Arguments body(Consumer<AlterExpression> configure) {
        return Arguments.of(configure);
    }

    @ParameterizedTest
    @MethodSource("prioritizedBodies")
    void baseBodyPrecedencePreventsInactiveColumnExpressionsFromBeingRendered(
            Consumer<AlterExpression> configure) {
        AlterExpression action = new AlterExpression();
        action.setOperation(AlterOperation.ALTER);
        configure.accept(action);
        Alter statement = statement(action);
        String expected = statement.toString();
        addInactiveColumn(action);
        assertFalse(action.hasActiveColumnDefinitions());
        assertTrue(action.getColumnDefaults().isEmpty());
        assertEquals(expected, statement.toString());
        assertRenderersAgree(statement);
        List<Long> visited = new ArrayList<>();
        assertEquals(expected, deparse(statement, visited));
        assertTrue(visited.isEmpty());
    }

    @Test
    void activeDefaultKeepsPriorityOverStaleRenameMetadata() {
        AlterExpression action = new AlterExpression();
        action.setOperation(AlterOperation.ALTER);
        action.hasColumn(true);
        action.addColSetDefault(ColumnSetDefault.fromExpression("id", new LongValue(1)));
        action.setOldIndex(new Index().withName("old_index"));
        action.setIndex(new Index().withName("new_index"));
        addInactiveColumn(action);
        assertEquals(1, action.getColumnDefaults().size());
        assertFalse(action.hasActiveColumnDefinitions());
        Alter statement = statement(action);
        assertEquals("ALTER TABLE t ALTER COLUMN id SET DEFAULT 1", statement.toString());
        assertRenderersAgree(statement);
        List<Long> visited = new ArrayList<>();
        assertEquals("ALTER TABLE t ALTER COLUMN id SET DEFAULT 101", deparse(statement, visited));
        assertEquals(List.of(1L), visited);
    }

    private static void addInactiveColumn(AlterExpression action) {
        ColumnDataType column = new ColumnDataType(true).withColumnName("inactive")
                .withColDataType(new ColDataType("INT"));
        column.setUsingExpression(new LongValue(999));
        action.addColDataType(column);
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static Alter statement(AlterExpression action) {
        Alter statement = new Alter();
        statement.setTable(new Table("t"));
        statement.addAlterExpression(action);
        return statement;
    }

    private static void assertRenderersAgree(Alter statement) {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
    }

    private static String deparse(Alter statement, List<Long> visited) {
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                assertNull(context);
                visited.add(value.getValue());
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), sql), null);
        return sql.toString();
    }
}
