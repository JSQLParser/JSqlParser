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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.AlterExpression.TableRenameKeyword;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AlterActionRenderingTest {

    static Stream<Arguments> drops() {
        return Stream.of(
                drop("DROP old_column", action -> action.setColumnName("old_column")),
                drop("DROP COLUMN IF EXISTS old_column CASCADE", action -> {
                    action.hasColumn(true);
                    action.setUsingIfExists(true);
                    action.setColumnName("old_column");
                    action.addParameters("CASCADE");
                }),
                drop("DROP CONSTRAINT IF EXISTS old_constraint RESTRICT", action -> {
                    action.setUsingIfExists(true);
                    action.setConstraintName("old_constraint");
                    action.addParameters("RESTRICT");
                }),
                drop("DROP INDEX old_index", action -> action.setIndex(
                        new Index().withType("INDEX").withName("old_index"))),
                drop("DROP KEY old_key", action -> action.setIndex(
                        new Index().withType("KEY").withName("old_key"))),
                drop("DROP (a, b) CASCADE CONSTRAINTS", action -> {
                    action.setPkColumns(List.of("a", "b"));
                    action.addParameters("CASCADE", "CONSTRAINTS");
                }));
    }

    private static Arguments drop(String sql, Consumer<AlterExpression> configure) {
        return Arguments.of(sql, configure);
    }

    @ParameterizedTest
    @MethodSource("drops")
    void legacyAndTypedDropActionsShareRendering(String body, Consumer<AlterExpression> configure)
            throws JSQLParserException {
        for (AlterExpression action : List.of(new AlterExpression(), new AlterExpressionDrop())) {
            action.setOperation(AlterOperation.DROP);
            configure.accept(action);
            assertRoundTrip(action, body, AlterExpressionDrop.class);
        }
    }

    @ParameterizedTest
    @EnumSource(TableRenameKeyword.class)
    void legacyAndTypedTableRenamesHonorTheSelectedKeyword(TableRenameKeyword keyword)
            throws JSQLParserException {
        String body = "RENAME" + (keyword == TableRenameKeyword.NONE ? "" : " " + keyword)
                + " db2.renamed";
        for (AlterExpression action : List.of(new AlterExpression(), new AlterExpressionRename())) {
            action.setOperation(AlterOperation.RENAME_TABLE);
            action.setNewTable(new Table("db2", "renamed"));
            action.setTableRenameKeyword(keyword);
            assertRoundTrip(action, body, AlterExpressionRename.class);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void legacyAndTypedColumnRenamesShareRendering(boolean hasColumn) throws JSQLParserException {
        for (AlterExpression action : List.of(new AlterExpression(), new AlterExpressionRename())) {
            action.setOperation(AlterOperation.RENAME);
            action.hasColumn(hasColumn);
            action.setColumnOldName("old_column");
            action.setColumnName("new_column");
            assertRoundTrip(action, "RENAME " + (hasColumn ? "COLUMN " : "")
                    + "old_column TO new_column", AlterExpressionRename.class);
        }
    }

    @ParameterizedTest
    @EnumSource(value = AlterOperation.class,
            names = {"RENAME_INDEX", "RENAME_KEY", "RENAME_CONSTRAINT"})
    void legacyAndTypedIndexRenamesShareRendering(AlterOperation operation)
            throws JSQLParserException {
        for (AlterExpression action : List.of(new AlterExpression(), new AlterExpressionRename())) {
            action.setOperation(operation);
            action.setOldIndex(new Index().withName("old_name"));
            action.setIndex(new Index().withName("new_name"));
            assertRoundTrip(action, operation.toString().replace('_', ' ')
                    + " old_name TO new_name", AlterExpressionRename.class);
        }
    }

    @Test
    void commonTailIsAppendedOnceForTypedAndLegacyActions() {
        for (AlterExpression action : List.of(new AlterExpression(), new AlterExpressionDrop())) {
            Index index = new Index().withType("INDEX").withName("old_index");
            index.setCommentText("'index comment'");
            action.setOperation(AlterOperation.DROP);
            action.setIndex(index);
            action.addParameters("CASCADE");
            assertEquals("DROP INDEX old_index CASCADE COMMENT 'index comment'", action.toString());
            StringBuilder output = new StringBuilder();
            statement(action).accept(new StatementDeParser(output), null);
            assertEquals("ALTER TABLE t DROP INDEX old_index CASCADE COMMENT 'index comment'",
                    output.toString());
        }
    }

    @Test
    void mixedActionsStillUseTheCustomExpressionDeparser() throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t DROP COLUMN obsolete, "
                + "RENAME COLUMN old_column TO new_column, ADD COLUMN amount INT DEFAULT (1 + 2)");
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        alter.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        String expected = "ALTER TABLE t DROP COLUMN obsolete, "
                + "RENAME COLUMN old_column TO new_column, ADD COLUMN amount INT DEFAULT (101 + 102)";
        assertEquals(expected, output.toString());
        assertEquals(expected, CCJSqlParserUtil.parse(output.toString()).toString());
    }

    private static Alter statement(AlterExpression action) {
        Alter alter = new Alter();
        alter.setTable(new Table("t"));
        alter.addAlterExpression(action);
        return alter;
    }

    private static void assertRoundTrip(AlterExpression action, String expectedBody,
            Class<? extends AlterExpression> parsedType) throws JSQLParserException {
        String expected = "ALTER TABLE t " + expectedBody;
        Alter alter = statement(action);
        assertEquals(expected, alter.toString());
        StringBuilder output = new StringBuilder();
        alter.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        Alter reparsed = (Alter) CCJSqlParserUtil.parse(output.toString());
        assertInstanceOf(parsedType, reparsed.getAlterExpressions().get(0));
        assertEquals(expected, reparsed.toString());
    }
}
