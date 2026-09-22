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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterCommentColumnTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ADD COLUMN comment TEXT", "ADD comment TEXT", "MODIFY COLUMN comment TEXT",
            "MODIFY comment TEXT", "MODIFY COLUMN `comment` TEXT",
            "MODIFY COLUMN comment TEXT COMMENT 'note' AFTER id",
            "CHANGE COLUMN comment comment2 TEXT", "CHANGE COLUMN old_name comment TEXT",
            "DROP COLUMN comment", "DROP comment", "RENAME COLUMN comment TO comment2",
            "RENAME COLUMN old_name TO comment"
    })
    void roundTripsUnquotedCommentColumn(String action) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.MYSQL}) {
            Alter alter = parse("ALTER TABLE t " + action, dialect);
            assertEquals(1, alter.getAlterExpressions().size());
            assertRoundTrip(alter, dialect);
        }
    }

    @Test
    void exposesEditableColumnDefinitionInsteadOfColumnComment() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t MODIFY COLUMN comment TEXT", Dialect.MYSQL);
        AlterExpression action = alter.getAlterExpressions().get(0);
        assertEquals(AlterOperation.MODIFY, action.getOperation());
        assertNull(action.getCommentText());
        AlterExpression.ColumnDataType column = action.getColDataTypeList().get(0);
        assertEquals("comment", column.getColumnName());
        assertEquals("TEXT", column.getColDataType().getDataType());
        column.setColumnName("notes");
        column.getColDataType().setDataType("LONGTEXT");
        assertEquals("ALTER TABLE t MODIFY COLUMN notes LONGTEXT", alter.toString());
        assertRoundTrip(alter, Dialect.MYSQL);
    }

    @Test
    void exposesBothNamesInRenameAndChange() throws JSQLParserException {
        Alter rename = parse("ALTER TABLE t RENAME COLUMN comment TO notes", Dialect.MYSQL);
        AlterExpression action = rename.getAlterExpressions().get(0);
        assertEquals("comment", action.getColumnOldName());
        assertEquals("notes", action.getColumnName());
        action.setColumnName("remarks");
        assertEquals("ALTER TABLE t RENAME COLUMN comment TO remarks", rename.toString());
        assertRoundTrip(rename, Dialect.MYSQL);
        Alter change = parse("ALTER TABLE t CHANGE COLUMN comment notes TEXT", Dialect.MYSQL);
        assertEquals("comment", change.getAlterExpressions().get(0).getColumnOldName());
        assertEquals("notes",
                change.getAlterExpressions().get(0).getColDataTypeList().get(0).getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER c COMMENT 'description'", "COMMENT = 'table description'",
            "DROP COLUMN type", "DROP INDEX idx", "DROP CONSTRAINT ck", "RENAME TO other",
            "RENAME INDEX idx TO other_idx", "RENAME CONSTRAINT ck TO other_ck"})
    void preservesOtherAlterBranches(String action) throws JSQLParserException {
        assertRoundTrip(parse("ALTER TABLE t " + action, null), null);
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> {
            if (dialect != null) {
                p.withDialect(dialect);
            }
        });
    }

    private static void assertRoundTrip(Alter alter, Dialect dialect) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
        assertEquals(alter.toString(), parse(buffer.toString(), dialect).toString());
    }
}
