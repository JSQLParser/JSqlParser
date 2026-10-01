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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnSetDefault;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnDefaultActionTest {
    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "VIEW"})
    void commonAccessMutatesTheExistingModel(String objectType) throws JSQLParserException {
        Statement statement = parse("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT 1");
        AlterExpression action = actions(statement).get(0);
        ColumnDefaultAction column = action.getColumnDefaults().get(0);
        assertEquals("a", column.getColumnName());
        assertEquals("1", column.getDefaultValue());
        assertSame(action instanceof RelationAlterAction ? action
                : action.getColumnSetDefaultList().get(0), column);

        Expression replacement = CCJSqlParserUtil.parseExpression("(2 + 3)");
        column.setDefaultExpression(replacement);
        column.setColumnName("renamed");
        assertSame(replacement, column.getDefaultExpression());
        assertEquals("(2 + 3)", column.getDefaultValue());
        assertEquals("ALTER " + objectType + " t ALTER COLUMN renamed SET DEFAULT (2 + 3)",
                statement.toString());
        roundTrip(statement);

        List<Expression> expressions = new ArrayList<>();
        TableDefinitionTraversal.visit(action, expressions::add, ignored -> {
        });
        assertEquals(List.of(replacement), expressions);

        if (action instanceof RelationAlterAction) {
            ((RelationAlterAction) action).setDefaultExpression(new LongValue(4));
        } else {
            action.getColumnSetDefaultList().get(0).setDefaultExpression(new LongValue(4));
        }
        assertEquals("4", column.getDefaultValue());
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "VIEW"})
    void sharedRenderingPreservesCustomExpressionVisitors(String objectType)
            throws JSQLParserException {
        Statement statement = parse("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT 0");
        actions(statement).get(0).getColumnDefaults().get(0)
                .setDefaultExpression(CCJSqlParserUtil.parseExpression("(1 + 2)"));
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT (101 + 102)",
                output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
        assertEquals("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT (1 + 2)",
                statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "VIEW"})
    void quotedNamesAndExplicitSqlNullRoundTrip(String objectType) throws JSQLParserException {
        Statement statement =
                parse("ALTER " + objectType + " t ALTER COLUMN \"Old Name\" SET DEFAULT 1");
        ColumnDefaultAction column = actions(statement).get(0).getColumnDefaults().get(0);
        column.setColumnName("\"New Name\"");
        column.setDefaultExpression(new NullValue());
        assertEquals("NULL", column.getDefaultValue());
        assertEquals("ALTER " + objectType + " t ALTER COLUMN \"New Name\" SET DEFAULT NULL",
                statement.toString());
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ADD COLUMN a INT",
            "ALTER TABLE t ALTER COLUMN a DROP DEFAULT", "ALTER VIEW v ALTER COLUMN a DROP DEFAULT",
            "ALTER VIEW v RENAME COLUMN a TO b",
            "ALTER MATERIALIZED VIEW v ALTER COLUMN a SET STATISTICS 100"})
    void unrelatedActionsDoNotAppearAsDefaults(String sql) throws JSQLParserException {
        assertTrue(actions(parse(sql)).get(0).getColumnDefaults().isEmpty());
    }

    @Test
    void inactiveRelationPropertiesDoNotAppearAsDefaults() {
        RelationAlterAction action = new RelationAlterAction();
        action.setColumnName("a");
        action.setDefaultExpression(new LongValue(1));
        action.setColumnAction(RelationAlterAction.ColumnAction.SET_DEFAULT);
        assertTrue(action.getColumnDefaults().isEmpty());
        action.setKind(RelationAlterAction.Kind.ALTER_COLUMN);
        assertSame(action, action.getColumnDefaults().get(0));
        action.setColumnAction(RelationAlterAction.ColumnAction.DROP_DEFAULT);
        assertTrue(action.getColumnDefaults().isEmpty());
        action.setColumnAction(RelationAlterAction.ColumnAction.SET_DEFAULT);
        action.setKind(RelationAlterAction.Kind.RENAME_COLUMN);
        assertTrue(action.getColumnDefaults().isEmpty());
    }

    @Test
    void constraintAlterationTakesPrecedenceOverPreviouslySetDefaults() throws JSQLParserException {
        Alter statement =
                (Alter) CCJSqlParserUtil.parse("ALTER TABLE t ALTER COLUMN a SET DEFAULT 1");
        AlterExpression action = statement.getAlterExpressions().get(0);
        ColumnDefaultAction previousDefault = action.getColumnDefaults().get(0);
        action.setConstraintType("CHECK");
        action.setConstraintSymbol("c");
        action.setEnforced(true);

        assertTrue(action.getColumnDefaults().isEmpty());
        assertEquals("ALTER TABLE t ALTER CHECK c ENFORCED", statement.toString());
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(sql.toString()).toString());

        action.setConstraintSymbol(null);
        assertSame(previousDefault, action.getColumnDefaults().get(0));
        roundTrip(statement);
    }

    @Test
    void opaqueLegacyConstructorIsNotParsedOrPassedToAnExpressionVisitor() {
        ColumnSetDefault legacy = new ColumnSetDefault("a", "vendor_specific(?)::opaque");
        ColumnDefaultAction column = legacy;
        assertNull(column.getDefaultExpression());
        assertEquals("vendor_specific(?)::opaque", column.getDefaultValue());
        StringBuilder sql = new StringBuilder();
        column.appendDefaultValueTo(sql, ignored -> {
            throw new AssertionError("Opaque defaults must not enter the expression visitor");
        });
        assertEquals("vendor_specific(?)::opaque", sql.toString());
        column.setColumnName("b");
        assertEquals("b SET DEFAULT vendor_specific(?)::opaque", legacy.toString());
        column.setDefaultExpression(new LongValue(2));
        assertEquals("b SET DEFAULT 2", legacy.toString());
        column.setDefaultExpression(null);
        assertNull(column.getDefaultValue());
        assertEquals("b SET DEFAULT null", legacy.toString());
        assertEquals("a SET DEFAULT null", new ColumnSetDefault("a", null).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "VIEW"})
    void commonLookupDoesNotExposeStructuralMutations(String objectType)
            throws JSQLParserException {
        Statement statement = parse("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT 1");
        List<ColumnDefaultAction> defaults = actions(statement).get(0).getColumnDefaults();
        assertThrows(UnsupportedOperationException.class, defaults::clear);
        defaults.get(0).setDefaultExpression(new LongValue(2));
        assertEquals("ALTER " + objectType + " t ALTER COLUMN a SET DEFAULT 2",
                statement.toString());
        roundTrip(statement);
    }

    @Test
    void lookupUsesTheExistingTableListWithoutCopyingItsEntries() {
        AlterExpression action = new AlterExpression();
        action.setOperation(AlterOperation.ALTER);
        ColumnSetDefault first = new ColumnSetDefault("a", "1");
        action.addColSetDefault(first);
        List<ColumnDefaultAction> defaults = action.getColumnDefaults();
        ColumnSetDefault second = ColumnSetDefault.fromExpression("b", new LongValue(2));
        action.addColSetDefault(second);
        assertEquals(List.of(first, second), defaults);
        action.getColumnSetDefaultList().remove(first);
        assertEquals(List.of(second), defaults);
        action.setOperation(AlterOperation.DROP);
        assertTrue(action.getColumnDefaults().isEmpty());
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static List<? extends AlterExpression> actions(Statement statement) {
        return statement instanceof Alter ? ((Alter) statement).getAlterExpressions()
                : ((AlterRelation) statement).getActions();
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}
