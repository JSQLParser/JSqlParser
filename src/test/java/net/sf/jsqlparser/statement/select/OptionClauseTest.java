/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionClauseTest {

    @Test
    public void testOptionHintIssue161() throws JSQLParserException {
        String sql =
                "SELECT CustomerID, PersonID, StoreID FROM cte OPTION (MAXRECURSION 2)";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        PlainSelect plainSelect = (PlainSelect) ((Select) statement).getPlainSelect();
        Assertions.assertNotNull(plainSelect.getOption());
        Assertions.assertEquals(1, plainSelect.getOption().getOptionHints().size());
        Assertions.assertEquals("MAXRECURSION",
                plainSelect.getOption().getOptionHints().get(0).getName());
        Assertions.assertEquals("2",
                plainSelect.getOption().getOptionHints().get(0).getValue().toString());
    }

    @Test
    public void testOptionWithoutArguments() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE x = 1 OPTION (RECOMPILE)", true);
    }

    @Test
    public void testOptionMultiWordHints() throws JSQLParserException {
        String sql =
                "SELECT * FROM t OPTION (KEEP PLAN, KEEPFIXED PLAN, FORCE ORDER, EXPAND VIEWS, NO_PERFORMANCE_SPOOL, IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX)";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals(6, plainSelect.getOption().getOptionHints().size());
        Assertions.assertEquals("KEEP PLAN",
                plainSelect.getOption().getOptionHints().get(0).getName());
        Assertions.assertEquals("FORCE ORDER",
                plainSelect.getOption().getOptionHints().get(2).getName());
    }

    @Test
    public void testOptionJoinAndUnionHints() throws JSQLParserException {
        String sql = "SELECT * FROM t OPTION (HASH JOIN, LOOP JOIN, MERGE JOIN, CONCAT UNION)";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals(4, plainSelect.getOption().getOptionHints().size());
        Assertions.assertEquals("HASH JOIN",
                plainSelect.getOption().getOptionHints().get(0).getName());
        Assertions.assertEquals("CONCAT UNION",
                plainSelect.getOption().getOptionHints().get(3).getName());
    }

    @Test
    public void testOptionNumericValueHints() throws JSQLParserException {
        String sql = "SELECT * FROM t OPTION (FAST 100, MAXDOP 4, MAXRECURSION 0)";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals("FAST", plainSelect.getOption().getOptionHints().get(0).getName());
        Assertions.assertEquals("100",
                plainSelect.getOption().getOptionHints().get(0).getValue().toString());
        Assertions.assertFalse(plainSelect.getOption().getOptionHints().get(0).isUseEquals());
    }

    @Test
    public void testOptionEqualsValueHints() throws JSQLParserException {
        String sql = "SELECT * FROM t OPTION (MAX_GRANT_PERCENT = 25.5, MIN_GRANT_PERCENT = 10)";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals("MAX_GRANT_PERCENT",
                plainSelect.getOption().getOptionHints().get(0).getName());
        Assertions.assertTrue(plainSelect.getOption().getOptionHints().get(0).isUseEquals());
        Assertions.assertEquals("25.5",
                plainSelect.getOption().getOptionHints().get(0).getValue().toString());
    }

    @Test
    public void testOptionParameterizedHints() throws JSQLParserException {
        String sql =
                "SELECT * FROM t OPTION (OPTIMIZE FOR UNKNOWN, USE HINT ('ASSUME_JOIN_PREDICATE_DEPENDS_ON_COLUMNS', 'DISABLE_PARAMETER_SNIFFING'))";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals("OPTIMIZE FOR UNKNOWN",
                plainSelect.getOption().getOptionHints().get(0).getName());
        OptionHint useHint = plainSelect.getOption().getOptionHints().get(1);
        Assertions.assertEquals("USE HINT", useHint.getName());
        Assertions.assertEquals(2, useHint.getParameters().size());
    }

    @Test
    public void testOptionOptimizeForAssignments() throws JSQLParserException {
        String sql = "SELECT * FROM t WHERE c = @p OPTION (OPTIMIZE FOR (@p = 1))";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        OptionHint optimizeFor = plainSelect.getOption().getOptionHints().get(0);
        Assertions.assertEquals("OPTIMIZE FOR", optimizeFor.getName());
        Assertions.assertEquals(1, optimizeFor.getParameters().size());
        Assertions.assertEquals("@p = 1", optimizeFor.getParameters().get(0).toString());
    }

    @Test
    public void testOptionTableHint() throws JSQLParserException {
        String sql = "SELECT * FROM t1 JOIN t2 ON t1.a = t2.a OPTION (TABLE HINT (t2, INDEX(ix2)))";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        OptionHint tableHint = plainSelect.getOption().getOptionHints().get(0);
        Assertions.assertEquals("TABLE HINT", tableHint.getName());
        Assertions.assertEquals("t2", tableHint.getParameters().get(0).toString());
        Assertions.assertEquals("INDEX(ix2)", tableHint.getParameters().get(1).toString());
    }

    @Test
    public void testOptionAfterUnion() throws JSQLParserException {
        String sql =
                "SELECT a FROM t1 UNION SELECT a FROM t2 ORDER BY a OPTION (RECOMPILE, MAXDOP 1) LIMIT 10";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        SetOperationList setOperationList =
                (SetOperationList) ((Select) statement).getSelectBody();
        Assertions.assertNotNull(setOperationList.getOption());
        Assertions.assertEquals(2, setOperationList.getOption().getOptionHints().size());
    }

    @Test
    public void testOptionAfterForXml() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t FOR XML PATH('row') OPTION (RECOMPILE)", true);
    }

    @Test
    public void testOptionInUpdateAndDelete() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE t SET a = 1 WHERE b = 2 OPTION (RECOMPILE)", true);
        assertSqlCanBeParsedAndDeparsed("DELETE FROM t WHERE a = 1 OPTION (LOOP JOIN)", true);

        Statement update = CCJSqlParserUtil.parse("UPDATE t SET a = 1 OPTION (RECOMPILE)");
        Assertions.assertNotNull(((net.sf.jsqlparser.statement.update.Update) update).getOption());
    }

    @Test
    public void testOptionOptimizeForUnknownParameter() throws JSQLParserException {
        String sql = "SELECT * FROM t WHERE c = @p OPTION (OPTIMIZE FOR (@p UNKNOWN))";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        OptionHint optimizeFor = plainSelect.getOption().getOptionHints().get(0);
        Assertions.assertEquals(1, optimizeFor.getParameters().size());
        Column parameter =
                Assertions.assertInstanceOf(Column.class, optimizeFor.getParameters().get(0));
        Assertions.assertEquals("@p UNKNOWN", parameter.getColumnName());
    }

    @Test
    public void testOptionOptimizeForMixedParameters() throws JSQLParserException {
        String sql = "SELECT * FROM t WHERE c = @p OPTION (OPTIMIZE FOR (@p = 1, @q UNKNOWN))";
        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        OptionHint optimizeFor = plainSelect.getOption().getOptionHints().get(0);
        Assertions.assertEquals(2, optimizeFor.getParameters().size());
        Assertions.assertEquals("@p = 1", optimizeFor.getParameters().get(0).toString());
        Column parameter =
                Assertions.assertInstanceOf(Column.class, optimizeFor.getParameters().get(1));
        Assertions.assertEquals("@q UNKNOWN", parameter.getColumnName());
    }

    @Test
    public void testOptionAfterInsertValuesIsRejected() {
        Assertions.assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(
                        "INSERT INTO t (a) VALUES (1) OPTION (RECOMPILE)"));
    }

    @Test
    public void testOptionAfterInsertSelect() throws JSQLParserException {
        String sql = "INSERT INTO t (a) SELECT a FROM s OPTION (RECOMPILE)";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        Insert insert = (Insert) statement;
        Assertions.assertNotNull(insert.getSelect().getOption());
    }

    @Test
    public void testOptionAfterMergeStatement() throws JSQLParserException {
        String sql =
                "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET a = s.a OPTION (HASH JOIN)";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        Merge merge = (Merge) statement;
        Assertions.assertNotNull(merge.getOption());
        Assertions.assertEquals("HASH JOIN", merge.getOption().getOptionHints().get(0).getName());
    }

    @Test
    public void testOptionAfterMergeOutputClause() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET a = s.a "
                        + "OUTPUT deleted.a OPTION (HASH JOIN)",
                false);
    }

    @Test
    public void testOptionAsIdentifierStillWorks() throws JSQLParserException {
        // OPTION stays a non-reserved keyword usable as column and table name
        assertSqlCanBeParsedAndDeparsed("SELECT option FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM option", true);
        assertSqlCanBeParsedAndDeparsed(
                "SELECT a, option AS o FROM t WHERE option = 1 ORDER BY option", true);
    }
}
