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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ColumnRenameActionTest {
    @ParameterizedTest
    @CsvSource({"MYSQL, ALTER TABLE t RENAME COLUMN old_col TO new_col",
            "POSTGRESQL, ALTER TABLE t RENAME COLUMN old_col TO new_col",
            "POSTGRESQL, ALTER TABLE t RENAME old_col TO new_col",
            "POSTGRESQL, ALTER VIEW v RENAME COLUMN old_col TO new_col",
            "POSTGRESQL, ALTER VIEW v RENAME old_col TO new_col",
            "POSTGRESQL, ALTER MATERIALIZED VIEW v RENAME COLUMN old_col TO new_col"})
    void commonAndLegacyAccessorsEditTheSameNames(Dialect dialect, String sql)
            throws JSQLParserException {
        Statement statement = parse(sql, dialect);
        AlterExpression action = action(statement);
        ColumnRenameAction rename = action.getColumnRename();
        assertNotNull(rename);
        assertEquals("old_col", rename.getSourceName());
        assertEquals("new_col", rename.getTargetName());

        String quote = dialect == Dialect.MYSQL ? "`" : "\"";
        String sourceName = quote + "old column" + quote;
        String targetName = quote + "new column" + quote;
        rename.setSourceName(sourceName);
        rename.setTargetName(targetName);
        if (action instanceof RelationAlterAction) {
            RelationAlterAction relation = (RelationAlterAction) action;
            assertEquals(sourceName, relation.getColumnName());
            assertEquals(targetName, relation.getNewName());
        } else {
            assertEquals(sourceName, action.getColumnOldName());
            assertEquals(targetName, action.getColumnName());
        }
        assertRoundTrip(sql.replace("old_col", sourceName)
                .replace("new_col", targetName), statement, dialect);

        if (action instanceof RelationAlterAction) {
            RelationAlterAction relation = (RelationAlterAction) action;
            relation.setColumnName("legacy_source");
            relation.setNewName("legacy_target");
        } else {
            action.setColumnOldName("legacy_source");
            action.setColumnName("legacy_target");
        }
        assertEquals("legacy_source", rename.getSourceName());
        assertEquals("legacy_target", rename.getTargetName());
        assertSame(rename, action.getColumnRename());
        assertRoundTrip(sql.replace("old_col", "legacy_source")
                .replace("new_col", "legacy_target"), statement, dialect);
    }

    @ParameterizedTest
    @CsvSource({"POSTGRESQL, ALTER TABLE t RENAME TO other_table",
            "MYSQL, ALTER TABLE t RENAME INDEX old_index TO new_index",
            "MYSQL, ALTER TABLE t RENAME KEY old_key TO new_key",
            "POSTGRESQL, ALTER TABLE t RENAME CONSTRAINT old_constraint TO new_constraint",
            "POSTGRESQL, ALTER VIEW v RENAME TO other_view",
            "POSTGRESQL, ALTER INDEX ix RENAME TO other_index",
            "POSTGRESQL, ALTER TABLE t ADD COLUMN c INT",
            "POSTGRESQL, ALTER VIEW v SET SCHEMA other_schema"})
    void otherOperationsDoNotExposeAColumnRename(Dialect dialect, String sql)
            throws JSQLParserException {
        Statement statement = parse(sql, dialect);
        assertNull(action(statement).getColumnRename());
        assertRoundTrip(statement.toString(), statement, dialect);
    }

    @Test
    void switchingTableOperationsKeepsInactiveNamesWithoutExposingThem()
            throws JSQLParserException {
        Statement statement = parse("ALTER TABLE t RENAME COLUMN old_col TO new_col");
        AlterExpression action = action(statement);
        ColumnRenameAction rename = action.getColumnRename();
        action.setOperation(AlterOperation.RENAME_TABLE);
        action.setNewTableName("other_table");
        assertNull(action.getColumnRename());
        assertEquals("old_col", action.getColumnOldName());
        assertEquals("new_col", action.getColumnName());
        assertRoundTrip("ALTER TABLE t RENAME TO other_table", statement);

        action.setOperation(AlterOperation.RENAME);
        assertSame(rename, action.getColumnRename());
        assertRoundTrip("ALTER TABLE t RENAME COLUMN old_col TO new_col", statement);
    }

    @Test
    void switchingRelationKindsKeepsInactiveNamesWithoutExposingThem()
            throws JSQLParserException {
        Statement statement = parse("ALTER VIEW v RENAME COLUMN old_col TO new_col");
        RelationAlterAction action = (RelationAlterAction) action(statement);
        ColumnRenameAction rename = action.getColumnRename();
        action.setKind(RelationAlterAction.Kind.SET_SCHEMA);
        action.setValue("other_schema");
        assertNull(action.getColumnRename());
        assertEquals("old_col", action.getColumnName());
        assertEquals("new_col", action.getNewName());
        assertRoundTrip("ALTER VIEW v SET SCHEMA other_schema", statement);

        action.setKind(RelationAlterAction.Kind.RENAME_COLUMN);
        assertSame(rename, action.getColumnRename());
        assertRoundTrip("ALTER VIEW v RENAME COLUMN old_col TO new_col", statement);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return parse(sql, Dialect.POSTGRESQL);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static AlterExpression action(Statement statement) {
        return statement instanceof Alter
                ? ((Alter) statement).getAlterExpressions().get(0)
                : ((AlterRelation) statement).getActions().get(0);
    }

    private static void assertRoundTrip(String expected, Statement statement)
            throws JSQLParserException {
        assertRoundTrip(expected, statement, Dialect.POSTGRESQL);
    }

    private static void assertRoundTrip(String expected, Statement statement, Dialect dialect)
            throws JSQLParserException {
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(expected, output.toString());
        Statement reparsed = parse(output.toString(), dialect);
        assertEquals(statement.getClass(), reparsed.getClass());
        assertEquals(action(statement).getClass(), action(reparsed).getClass());
        assertEquals(expected, reparsed.toString());
        ColumnRenameAction rename = action(statement).getColumnRename();
        ColumnRenameAction reparsedRename = action(reparsed).getColumnRename();
        if (rename == null) {
            assertNull(reparsedRename);
        } else {
            assertNotNull(reparsedRename);
            assertEquals(rename.getSourceName(), reparsedRename.getSourceName());
            assertEquals(rename.getTargetName(), reparsedRename.getTargetName());
        }
    }
}
