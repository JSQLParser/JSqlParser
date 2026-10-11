/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeUpdate;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.SetPipeOperator;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateSetParsingTest {

    // These are consumers of the shared grammar, not claims of dialect-specific SQL validity.
    enum EntryPoint {
        UPDATE("UPDATE t SET "), INSERT_SET("INSERT INTO t SET "), INSERT_DUPLICATE(
                "INSERT INTO t VALUES (0) ON DUPLICATE KEY UPDATE "), INSERT_CONFLICT(
                        "INSERT INTO t VALUES (0) ON CONFLICT (id) DO UPDATE SET "), MERGE_UPDATE(
                                "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET "), UPSERT_SET(
                                        "UPSERT INTO t SET "), PIPE_SET(
                                                "FROM t |> SET "), SELECT_SETTINGS(
                                                        "SELECT * FROM t SETTINGS ");

        private final String prefix;

        EntryPoint(String prefix) {
            this.prefix = prefix;
        }

        List<UpdateSet> getUpdateSets(Statement statement) {
            switch (this) {
                case UPDATE:
                    return ((Update) statement).getUpdateSets();
                case INSERT_SET:
                    return ((Insert) statement).getSetUpdateSets();
                case INSERT_DUPLICATE:
                    return ((Insert) statement).getDuplicateAction().getUpdateSets();
                case INSERT_CONFLICT:
                    return ((Insert) statement).getConflictAction().getUpdateSets();
                case MERGE_UPDATE:
                    return ((MergeUpdate) ((Merge) statement).getOperations().get(0))
                            .getUpdateSets();
                case UPSERT_SET:
                    return ((Upsert) statement).getUpdateSets();
                case PIPE_SET:
                    return ((SetPipeOperator) ((FromQuery) statement).getPipeOperators().get(0))
                            .getUpdateSets();
                case SELECT_SETTINGS:
                    return ((PlainSelect) statement).getSettings();
                default:
                    throw new AssertionError(this);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(EntryPoint.class)
    void scalarAssignments(EntryPoint entryPoint) throws JSQLParserException {
        assertAssignments(entryPoint, "a = 1, b = 2, c = 3",
                new String[][] {{"a"}, {"b"}, {"c"}},
                new String[][] {{"1"}, {"2"}, {"3"}});
    }

    @ParameterizedTest
    @EnumSource(EntryPoint.class)
    void singleTupleAssignment(EntryPoint entryPoint) throws JSQLParserException {
        assertAssignments(entryPoint, "(b, c) = (2, 3)",
                new String[][] {{"b", "c"}}, new String[][] {{"2", "3"}});
    }

    @ParameterizedTest
    @EnumSource(EntryPoint.class)
    void tupleAssignmentFirst(EntryPoint entryPoint) throws JSQLParserException {
        assertAssignments(entryPoint, "(b, c) = (2, 3), a = 1",
                new String[][] {{"b", "c"}, {"a"}}, new String[][] {{"2", "3"}, {"1"}});
    }

    static Stream<Arguments> assignmentsAfterScalar() {
        return Arrays.stream(EntryPoint.values()).flatMap(entryPoint -> Stream.of(
                Arguments.of(entryPoint, "a = 1, (b, c) = (2, 3)",
                        new String[][] {{"a"}, {"b", "c"}},
                        new String[][] {{"1"}, {"2", "3"}}),
                Arguments.of(entryPoint, "a = 1, (b, c) = (2, 3), d = 4",
                        new String[][] {{"a"}, {"b", "c"}, {"d"}},
                        new String[][] {{"1"}, {"2", "3"}, {"4"}}),
                Arguments.of(entryPoint, "a = 1, (b, c) = (2, 3), (d, e) = (4, 5)",
                        new String[][] {{"a"}, {"b", "c"}, {"d", "e"}},
                        new String[][] {{"1"}, {"2", "3"}, {"4", "5"}}),
                Arguments.of(entryPoint, "a = 1, (b, c) = (COALESCE(b, 2), c + 3), d = ?",
                        new String[][] {{"a"}, {"b", "c"}, {"d"}},
                        new String[][] {{"1"}, {"COALESCE(b, 2)", "c + 3"}, {"?"}}),
                Arguments.of(entryPoint, "a = 1, (b, c) = (SELECT x, y FROM s), d = 4",
                        new String[][] {{"a"}, {"b", "c"}, {"d"}},
                        new String[][] {{"1"}, {"(SELECT x, y FROM s)"}, {"4"}})));
    }

    @ParameterizedTest
    @MethodSource("assignmentsAfterScalar")
    void tupleAssignmentsAfterScalar(EntryPoint entryPoint, String assignments,
            String[][] columns, String[][] values) throws JSQLParserException {
        assertAssignments(entryPoint, assignments, columns, values);
    }

    @Test
    void tupleSubqueryAndFollowingClauses() throws JSQLParserException {
        String sql = "UPDATE t SET a = DEFAULT, (b, c) = (SELECT x, y FROM s WHERE s.id = t.id), "
                + "d = 4 WHERE t.id = 1 RETURNING a, b, c, d";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql);
        assertUpdateSets(EntryPoint.UPDATE.getUpdateSets(statement),
                new String[][] {{"a"}, {"b", "c"}, {"d"}},
                new String[][] {{"DEFAULT"}, {"(SELECT x, y FROM s WHERE s.id = t.id)"}, {"4"}});
        Update update = (Update) statement;
        assertEquals("t.id = 1", update.getWhere().toString());
        assertEquals(" RETURNING a, b, c, d", update.getReturningClause().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a = 1 (b, c) = (2, 3)", "(b, c) = (2, 3) (d, e) = (4, 5)",
            "a = 1,", "a = 1,, (b, c) = (2, 3)"})
    void rejectsMissingOrExtraSeparators(String assignments) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(EntryPoint.UPDATE.prefix + assignments));
    }

    private static void assertAssignments(EntryPoint entryPoint, String assignments,
            String[][] columns, String[][] values) throws JSQLParserException {
        Statement statement =
                assertSqlCanBeParsedAndDeparsed(entryPoint.prefix + assignments, true);
        assertUpdateSets(entryPoint.getUpdateSets(statement), columns, values);
        assertUpdateSets(entryPoint.getUpdateSets(CCJSqlParserUtil.parse(statement.toString())),
                columns, values);
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder));
        assertUpdateSets(entryPoint.getUpdateSets(CCJSqlParserUtil.parse(builder.toString())),
                columns, values);
    }

    private static void assertUpdateSets(List<UpdateSet> updateSets, String[][] columns,
            String[][] values) {
        assertEquals(columns.length, updateSets.size());
        for (int i = 0; i < columns.length; i++) {
            UpdateSet updateSet = updateSets.get(i);
            assertEquals(columns[i].length, updateSet.getColumns().size());
            assertEquals(values[i].length, updateSet.getValues().size());
            for (int j = 0; j < columns[i].length; j++) {
                assertEquals(columns[i][j], updateSet.getColumn(j).getColumnName());
            }
            for (int j = 0; j < values[i].length; j++) {
                assertEquals(values[i][j], updateSet.getValue(j).toString());
            }
            if (columns[i].length > 1) {
                assertInstanceOf(ParenthesedExpressionList.class, updateSet.getColumns());
                if (values[i][0].startsWith("(SELECT")) {
                    assertInstanceOf(ParenthesedSelect.class, updateSet.getValue(0));
                } else {
                    assertInstanceOf(ParenthesedExpressionList.class, updateSet.getValues());
                }
            }
        }
    }
}
