/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlForeignKeyActionColumnsTest {
    @ParameterizedTest
    @ValueSource(strings = {"NULL", "DEFAULT"})
    void sharesActionsAcrossCreateAndAlter(String value) throws JSQLParserException {
        for (String columns : new String[] {"author_id", "tenant_id, author_id", "\"Author Id\""}) {
            for (boolean updateFirst : new boolean[] {false, true}) {
                String delete = "ON DELETE SET " + value + " (" + columns + ")";
                String actions = updateFirst ? "ON UPDATE CASCADE " + delete
                        : delete + " ON UPDATE CASCADE";
                for (String prefix : new String[] {
                        "CREATE TABLE posts (tenant_id INT, author_id INT, ",
                        "ALTER TABLE posts ADD "}) {
                    String sql = prefix + "CONSTRAINT fk FOREIGN KEY (tenant_id, author_id) "
                            + "REFERENCES users (tenant_id, id) MATCH SIMPLE " + actions
                            + (prefix.startsWith("CREATE") ? ")" : "");
                    Statement statement = CCJSqlParserUtil.parse(sql);
                    ForeignKeyIndex index = (ForeignKeyIndex) (statement instanceof CreateTable
                            ? ((CreateTable) statement).getIndexes().get(0)
                            : ((Alter) statement).getAlterExpressions().get(0).getIndex());
                    ReferentialAction action =
                            index.getReferentialAction(ReferentialAction.Type.DELETE);
                    assertEquals(List.of(columns.split(", ")), action.getColumnNames());
                    assertNull(index.getReferentialAction(ReferentialAction.Type.UPDATE)
                            .getColumnNames());
                    roundTrip(statement);
                    action.setColumnNames(List.of("author_id"));
                    roundTrip(statement);
                    assertTrue(statement.toString().contains("SET " + value + " (author_id)"));
                    action.setColumnNames(null);
                    roundTrip(statement);
                }
            }
        }
    }

    @Test
    void supportsColumnReferencesAndStatementBoundaries() throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE posts (author_id INT REFERENCES users ON DELETE SET NULL (author_id) NOT NULL)");
        ForeignKeyReference reference = table.getColumnDefinitions().get(0)
                .getColumnOptions().get(0).getForeignKeyReference();
        assertEquals(List.of("author_id"),
                reference.getReferentialAction(ReferentialAction.Type.DELETE).getColumnNames());
        roundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(table + "; SELECT 1").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ON UPDATE SET NULL (a)", "ON UPDATE SET DEFAULT (a)",
            "ON DELETE CASCADE (a)", "ON DELETE SET NULL ()", "ON DELETE SET NULL (a,)",
            "ON DELETE SET NULL (a + 1)"})
    void rejectsInvalidActionColumns(String action) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "CREATE TABLE t (a INT, FOREIGN KEY (a) REFERENCES p (a) " + action + ")"));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), CCJSqlParserUtil.parse(out.toString()).toString());
    }
}
