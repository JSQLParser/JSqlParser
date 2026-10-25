/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class OracleReturningIntoTest {
    static Stream<String> dmlStatements() {
        return Stream.of("INSERT INTO employees (id, salary) VALUES (1, 100)",
                "UPDATE employees SET salary = salary + 100 WHERE id = 1",
                "DELETE FROM employees WHERE id = 1",
                "MERGE INTO employees e USING changes c ON (e.id = c.id) "
                        + "WHEN MATCHED THEN UPDATE SET e.salary = c.salary");
    }

    @ParameterizedTest
    @MethodSource("dmlStatements")
    void preservesNamedOutputBinds(String dml) throws JSQLParserException {
        String sql = dml + " RETURNING id, salary INTO :out_id, :out_salary";
        for (Statement statement : parseBoth(sql)) {
            ReturningClause clause = returningClause(statement);
            assertFalse(clause.isBulkCollect());
            assertEquals(2, clause.getDataItems().size());
            assertEquals("out_id",
                    assertInstanceOf(JdbcNamedParameter.class, clause.getDataItems().get(0))
                            .getName());
            assertEquals("out_salary",
                    assertInstanceOf(JdbcNamedParameter.class, clause.getDataItems().get(1))
                            .getName());
            roundTrip(statement);
        }
    }

    @ParameterizedTest
    @MethodSource("dmlStatements")
    void preservesBulkCollectionAndTargets(String dml) throws JSQLParserException {
        for (String targets : List.of("ids, salaries", ":ids, :salaries")) {
            String sql = dml + " RETURNING id, salary BULK COLLECT INTO " + targets;
            for (Statement statement : parseBoth(sql)) {
                ReturningClause clause = returningClause(statement);
                assertTrue(clause.isBulkCollect());
                assertEquals(2, clause.getDataItems().size());
                assertEquals(targets.startsWith(":"),
                        clause.getDataItems().get(0) instanceof JdbcNamedParameter);
                assertTrue(statement.toString().contains(" BULK COLLECT INTO "));
                roundTrip(statement);
            }
        }
    }

    @Test
    void countsJdbcOutputParametersAfterInputParameters() throws JSQLParserException {
        for (Statement statement : parseBoth("UPDATE employees SET salary = ? WHERE id = ? "
                + "RETURNING id, salary INTO ?, ?")) {
            ReturningClause clause = returningClause(statement);
            assertFalse(clause.isBulkCollect());
            assertEquals(3,
                    assertInstanceOf(JdbcParameter.class, clause.getDataItems().get(0)).getIndex());
            assertEquals(4,
                    assertInstanceOf(JdbcParameter.class, clause.getDataItems().get(1)).getIndex());
            roundTrip(statement);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"out_id", "record.out_id", "@out_id"})
    void preservesExistingOutputTargets(String target) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil
                .parse("INSERT INTO employees (id) VALUES (1) RETURNING id INTO " + target);
        Object actual = returningClause(statement).getDataItems().get(0);
        Class<?> expectedType = target.startsWith("@") ? UserVariable.class : Table.class;
        assertInstanceOf(expectedType, actual);
        assertEquals(target, actual.toString());
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bulk", "AS bulk", "\"bulk\"", "AS collect"})
    void keepsAliasesBeforeBulkCollect(String alias) throws JSQLParserException {
        for (Statement statement : parseBoth("UPDATE employees SET salary = 100 "
                + "RETURNING salary " + alias + " BULK COLLECT INTO salaries")) {
            ReturningClause clause = returningClause(statement);
            assertTrue(clause.isBulkCollect());
            assertEquals(alias.replace("AS ", ""), clause.get(0).getAlias().getName());
            roundTrip(statement);
        }
    }

    @Test
    void keepsBulkAndCollectAsOrdinaryIdentifiersAndAliases() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil
                .parse("SELECT bulk, collect, salary bulk, salary collect FROM employees");
        assertEquals("bulk", select.getSelectItem(0).getExpression().toString());
        assertEquals("collect", select.getSelectItem(1).getExpression().toString());
        assertEquals("bulk", select.getSelectItem(2).getAlias().getName());
        assertEquals("collect", select.getSelectItem(3).getAlias().getName());
        roundTrip(select);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bulk", "collect"})
    void keepsAliasesBeforeOrdinaryInto(String alias) throws JSQLParserException {
        for (Statement statement : parseBoth("DELETE FROM employees RETURNING id " + alias
                + " INTO out_id")) {
            ReturningClause clause = returningClause(statement);
            assertFalse(clause.isBulkCollect());
            assertEquals(alias, clause.get(0).getAlias().getName());
            roundTrip(statement);
        }
    }

    @Test
    void allowsChangingCollectionModeThroughTheAst() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(
                "DELETE FROM employees RETURNING id BULK COLLECT INTO ids");
        ReturningClause clause = returningClause(statement);
        clause.setBulkCollect(false);
        assertFalse(statement.toString().contains("BULK COLLECT"));
        roundTrip(statement);
        clause.setBulkCollect(true);
        assertTrue(statement.toString().contains("BULK COLLECT"));
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INTO", "INTO :", "INTO :out_id,", "BULK COLLECT INTO",
            "BULK COLLECT INTO ids,", "BULK COLLECT", "INTO :out_id + 1"})
    void rejectsIncompleteClauses(String clause) {
        String sql = "DELETE FROM employees RETURNING id " + clause;
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.ORACLE)));
    }

    private static List<Statement> parseBoth(String sql) throws JSQLParserException {
        return List.of(TestUtils.assertSqlCanBeParsedAndDeparsed(sql),
                CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.ORACLE)));
    }

    private static ReturningClause returningClause(Statement statement) {
        if (statement instanceof Insert) {
            return ((Insert) statement).getReturningClause();
        }
        if (statement instanceof Update) {
            return ((Update) statement).getReturningClause();
        }
        if (statement instanceof Delete) {
            return ((Delete) statement).getReturningClause();
        }
        return ((Merge) statement).getReturningClause();
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(statement.toString(), buffer.toString());
        Statement reparsed = CCJSqlParserUtil.parse(buffer.toString());
        assertEquals(buffer.toString(), reparsed.toString());
        if (!(statement instanceof PlainSelect)) {
            ReturningClause before = returningClause(statement);
            ReturningClause after = returningClause(reparsed);
            assertEquals(before.isBulkCollect(), after.isBulkCollect());
            assertEquals(before.getDataItems().size(), after.getDataItems().size());
            for (int i = 0; i < before.getDataItems().size(); i++) {
                assertEquals(before.getDataItems().get(i).getClass(),
                        after.getDataItems().get(i).getClass());
                assertEquals(before.getDataItems().get(i).toString(),
                        after.getDataItems().get(i).toString());
            }
        }
    }
}
