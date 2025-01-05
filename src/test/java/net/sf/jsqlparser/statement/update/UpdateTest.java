/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;
import java.util.List;

import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertUpdateMysqlHintExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateTest {

    private static final CCJSqlParserManager PARSER_MANAGER = new CCJSqlParserManager();

    @Test
    public void testUpdate() throws JSQLParserException {
        String statement = "UPDATE mytable set col1='as', col2=?, col3=565 Where o >= 3";
        Update update = (Update) PARSER_MANAGER.parse(new StringReader(statement));
        assertEquals("mytable", update.getTable().toString());
        assertEquals(3, update.getUpdateSets().size());
        assertEquals("col1", update.getUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("col2", update.getUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("col3", update.getUpdateSets().get(2).getColumns().get(0).getColumnName());
        assertEquals("as",
                ((StringValue) update.getUpdateSets().get(0).getValues().get(0)).getValue());
        assertTrue(update.getUpdateSets().get(1).getValues().get(0) instanceof JdbcParameter);
        assertEquals(565,
                ((LongValue) update.getUpdateSets().get(2).getValues().get(0)).getValue());

        assertTrue(update.getWhere() instanceof GreaterThanEquals);
    }

    @Test
    public void testUpdateWAlias() throws JSQLParserException {
        String statement = "UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'";
        Update update = (Update) PARSER_MANAGER.parse(new StringReader(statement));
    }

    @Test
    public void testUpdateWithDeparser() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE table1 AS A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'");
    }

    @Test
    public void testUpdateWithFrom() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE table1 SET columna = 5 FROM table1 LEFT JOIN table2 ON col1 = col2");
    }

    @Test
    public void testUpdateMultiTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE T1, T2 SET T1.C2 = T2.C2, T2.C3 = 'UPDATED' WHERE T1.C1 = T2.C1 AND T1.C2 < 10");
    }

    @Test
    public void testUpdateWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE NATION SET (N_NATIONKEY) = (SELECT ? FROM SYSIBM.SYSDUMMY1)");
    }

    @Test
    public void testUpdateWithSelect2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE mytable SET (col1, col2, col3) = (SELECT a, b, c FROM mytable2)");
    }

    @Test
    public void testUpdateIssue167_SingleQuotes() throws JSQLParserException {
        String sqlStr =
                "UPDATE tablename SET NAME = 'Customer 2', ADDRESS = 'Address \\' ddad2', AUTH_KEY = 'samplekey' WHERE ID = 2";

        assertSqlCanBeParsedAndDeparsed(
                sqlStr, true, parser -> parser.withBackslashEscapeCharacter(true));
    }

    @Test
    public void testUpdateWithLimit() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = 'thing' WHERE id = 1 LIMIT 10");
    }

    @Test
    public void testUpdateWithOrderBy() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col");
    }

    @Test
    public void testUpdateWithOrderByAndLimit() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10");
    }

    @Test
    public void testUpdateWithReturningAll() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING *");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING *");
    }

    @Test
    public void testUpdateWithReturningList() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING col_1, col_2, col_3");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING col_1, col_2, col_3");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING col_1 AS Bar, col_2 AS Baz, col_3 AS Foo");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING col_1 AS Bar, col_2 AS Baz, col_3 AS Foo");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING ABS(col_1) AS Bar, ABS(col_2), col_3 AS Foo");
    }

    @Test
    public void testUpdateDoesNotAllowLimitOffset() {
        String statement =
                "UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY' LIMIT 3,4";
        assertThrows(JSQLParserException.class,
                () -> PARSER_MANAGER.parse(new StringReader(statement)));
    }

    @Test
    public void testUpdateWithFunctions() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = SUBSTRING(col2, 1, 2)");
    }

    @Test
    public void testUpdateIssue508LeftShift() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE user SET num = 1 << 1 WHERE id = 1");
    }

    @Test
    public void testUpdateIssue338() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status & ~1)");
    }

    @Test
    public void testUpdateIssue338_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status & 1)");
    }

    @Test
    public void testUpdateIssue338_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status + 1)");
    }

    @Test
    public void testUpdateIssue826() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("update message_topic inner join message_topic_config on\n"
                + " message_topic.id=message_topic_config.topic_id \n"
                + "set message_topic_config.enable_flag='N', \n"
                + "message_topic_config.updated_by='test', \n"
                + "message_topic_config.update_at='2019-07-16' \n"
                + "where message_topic.name='test' \n"
                + "AND message_topic_config.enable_flag='Y'", true);
    }

    @Test
    public void testUpdateIssue750() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "update a,(select * from c) b set a.id=b.id where a.id=b.id", true);
    }

    @Test
    public void testUpdateIssue962Validate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tbl_user_card SET validate = '1', identityCodeFlag = 1 WHERE id = 9150000293816");
    }

    @Test
    public void testUpdateVariableAssignment() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE transaction_id SET latest_id_wallet = (@cur_id_wallet := latest_id_wallet) + 1");
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        assertOracleHintExists(
                "UPDATE /*+ SOMEHINT */ mytable set col1='as', col2=?, col3=565 Where o >= 3", true,
                "SOMEHINT");

        // @todo: add a testcase supposed to not finding a misplaced hint
        // assertOracleHintExists("UPDATE mytable /*+ SOMEHINT */ set col1='as', col2=?, col3=565
        // Where o >= 3", true, "SOMEHINT");
    }

    @Test
    public void testMysqlHint() throws JSQLParserException {
        assertUpdateMysqlHintExists(
                "UPDATE demo FORCE INDEX (idx_demo) SET col1 = NULL WHERE col2 = 1", true, "FORCE",
                "INDEX", "idx_demo");
    }

    @Test
    public void testWith() throws JSQLParserException {
        String statement = ""
                + "WITH a\n"
                + "     AS (SELECT 1 id_instrument_ref)\n"
                + "     , b\n"
                + "       AS (SELECT 1 id_instrument_ref)\n"
                + "UPDATE cfe.instrument_ref\n"
                + "SET id_instrument=null\n"
                + "WHERE  id_instrument_ref = (SELECT id_instrument_ref\n"
                + "                            FROM   a)";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(statement, true);
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals("cfe.instrument_ref", update.getTable().getFullyQualifiedName());
        assertEquals(2, withItems.size());
        assertEquals("SELECT 1 id_instrument_ref",
                withItems.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" a", withItems.get(0).getAlias().toString());
        assertEquals("SELECT 1 id_instrument_ref",
                withItems.get(1).getSelect().getPlainSelect().toString());
        assertEquals(" b", withItems.get(1).getAlias().toString());
        assertEquals(1, update.getUpdateSets().size());
        assertEquals("id_instrument", update.getUpdateSets().get(0).getColumn(0).toString());
        assertEquals("NULL", update.getUpdateSets().get(0).getValue(0).toString());
        assertEquals("id_instrument_ref = (SELECT id_instrument_ref FROM a)",
                update.getWhere().toString());
    }

    @Test
    public void testUpdateSetsIssue1316() throws JSQLParserException {
        String sqlStr = "update test\n"
                + "set (a, b) = (select '1', '2')";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update test\n"
                + "set a = '1'"
                + "    , b = '2'";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update test\n"
                + "set (a, b) = ('1', '2')";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update test\n"
                + "set (a, b) = (values ('1', '2'))";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update test\n"
                + "set (a, b) = (1, (select 2))";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "UPDATE prpjpaymentbill b\n"
                + "SET (   b.packagecode\n"
                + "        , b.packageremark\n"
                + "        , b.agentcode ) =   (   SELECT  p.payrefreason\n"
                + "                                        , p.classcode\n"
                + "                                        , p.riskcode\n"
                + "                                FROM prpjcommbill p\n"
                + "                                WHERE p.policertiid = 'SDDH200937010330006366' ) -- this is supposed to be UpdateSet 1\n"
                + "     , b.payrefnotype = '05' -- this is supposed to be UpdateSet 2\n"
                + "     , b.packageunit = '4101170402' -- this is supposed to be UpdateSet 3\n"
                + "WHERE b.payrefno = 'B370202091026000005'";

        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertEquals(3, update.getUpdateSets().size());

        assertEquals(3, update.getUpdateSets().get(0).getColumns().size());
        assertEquals(1, update.getUpdateSets().get(0).getValues().size());

        assertEquals(1, update.getUpdateSets().get(1).getColumns().size());
        assertEquals(1, update.getUpdateSets().get(1).getValues().size());

        assertEquals(1, update.getUpdateSets().get(2).getColumns().size());
        assertEquals(1, update.getUpdateSets().get(2).getValues().size());
    }

    @Test
    public void testUpdateLowPriority() throws JSQLParserException {
        String stmt = "UPDATE LOW_PRIORITY table1 A SET A.columna = 'XXX'";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(stmt);
        assertEquals(update.getModifierPriority(), UpdateModifierPriority.LOW_PRIORITY);
    }

    @Test
    public void testUpdateIgnoreModifier() throws JSQLParserException {
        String stmt = "UPDATE IGNORE table1 A SET A.columna = 'XXX'";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(stmt);
        assertTrue(update.isModifierIgnore());
        String stmt2 = "UPDATE table1 A SET A.columna = 'XXX'";
        Update update2 = (Update) assertSqlCanBeParsedAndDeparsed(stmt2);
        assertFalse(update2.isModifierIgnore());
    }

    @Test
    public void testUpdateMultipleModifiers() throws JSQLParserException {
        String stmt = "UPDATE LOW_PRIORITY IGNORE table1 A SET A.columna = 'XXX'";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(stmt);
        assertEquals(update.getModifierPriority(), UpdateModifierPriority.LOW_PRIORITY);
        assertTrue(update.isModifierIgnore());
    }

    @Test
    public void testUpdateOutputClause() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE /* TOP (10) */ HumanResources.Employee  \n"
                        + "SET VacationHours = VacationHours * 1.25,  \n"
                        + "    ModifiedDate = GETDATE()   \n"
                        + "OUTPUT inserted.BusinessEntityID,  \n"
                        + "       deleted.VacationHours,  \n"
                        + "       inserted.VacationHours,  \n"
                        + "       inserted.ModifiedDate  \n"
                        + "INTO @MyTableVar",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "UPDATE Production.WorkOrder  \n"
                        + "SET ScrapReasonID = 4  \n"
                        + "OUTPUT deleted.ScrapReasonID,  \n"
                        + "       inserted.ScrapReasonID,   \n"
                        + "       inserted.WorkOrderID,  \n"
                        + "       inserted.ProductID,  \n"
                        + "       p.Name  \n"
                        + "    INTO @MyTestVar  \n"
                        + "FROM Production.WorkOrder AS wo  \n"
                        + "    INNER JOIN Production.Product AS p   \n"
                        + "    ON wo.ProductID = p.ProductID   \n"
                        + "    AND wo.ScrapReasonID= 16  \n"
                        + "    AND p.ProductID = 733",
                true);
    }

    @Test
    public void testUpdateSetsIssue1590() throws JSQLParserException {
        Update update = (Update) CCJSqlParserUtil.parse("update mytable set a=5 where b = 2");
        assertEquals(1, update.getUpdateSets().size());
        update.addColumns(new Column("y"));
        update.addExpressions(new DoubleValue("6"));

        // update.getUpdateSets().get(0).add(new Column("y"), new DoubleValue("6"));

        assertEquals("UPDATE mytable SET (a, y) = (5, 6) WHERE b = 2", update.toString());
    }

    @Test
    void testArrayColumnsIssue1083() throws JSQLParserException {
        String sqlStr = "SELECT listes[(SELECT cardinality(listes))]";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update utilisateur set listes[0] = 1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update utilisateur set listes[(select cardinality(listes))] = 1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "update utilisateur set listes[0:3] = (1,2,3,4)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1910() throws JSQLParserException {
        Update update = new Update();
        update.setTable(new Table("sys_dept"));

        UpdateSet updateSet = new UpdateSet(new Column("deleted"), new LongValue(1L));
        update.addUpdateSet(updateSet);

        TestUtils.assertStatementCanBeDeparsedAs(update, "UPDATE sys_dept SET deleted = 1", true);

        updateSet.add(new Column("created"), new LongValue(2L));

        TestUtils.assertStatementCanBeDeparsedAs(update,
                "UPDATE sys_dept SET (deleted, created) = (1,2)", true);
    }

    @Test
    void testInsertWithinCte() throws JSQLParserException {
        String sqlStr = "WITH inserted AS ( " +
                "   INSERT INTO x (foo) " +
                "   SELECT bar FROM b " +
                "   RETURNING y " +
                ") " +
                "   UPDATE z " +
                "      SET foo = 1 " +
                "    WHERE y IN (SELECT y FROM inserted) ";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", update.getTable().toString());
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals(1, withItems.size());
        Insert insert = withItems.get(0).getInsert().getInsert();
        assertEquals("x", insert.getTable().toString());
        assertEquals("SELECT bar FROM b", insert.getSelect().toString());
        assertEquals(" RETURNING y", insert.getReturningClause().toString());
        assertEquals("INSERT INTO x (foo) SELECT bar FROM b RETURNING y", insert.toString());
        assertEquals(" inserted", withItems.get(0).getAlias().toString());
    }

    @Test
    void testUpdateWithinCte() throws JSQLParserException {
        String sqlStr = "WITH updated AS ( " +
                "   UPDATE x " +
                "      SET foo = 1 " +
                "    WHERE bar = 2 " +
                "   RETURNING y " +
                ") " +
                "   UPDATE z " +
                "      SET foo = 1 " +
                "    WHERE y IN (SELECT y FROM inserted) ";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", update.getTable().toString());
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals(1, withItems.size());
        Update innerUpdate = withItems.get(0).getUpdate().getUpdate();
        assertEquals("x", innerUpdate.getTable().toString());
        assertEquals("foo", innerUpdate.getUpdateSets().get(0).getColumn(0).toString());
        assertEquals("1", innerUpdate.getUpdateSets().get(0).getValue(0).toString());
        assertEquals("bar = 2", innerUpdate.getWhere().toString());
        assertEquals(" RETURNING y", innerUpdate.getReturningClause().toString());
        assertEquals(" updated", withItems.get(0).getAlias().toString());
    }

    @Test
    void testDeleteWithinCte() throws JSQLParserException {
        String sqlStr = "WITH deleted AS ( " +
                "   DELETE FROM x " +
                "    WHERE bar = 2 " +
                "   RETURNING y " +
                ") " +
                "   UPDATE z " +
                "      SET foo = 1 " +
                "    WHERE y IN (SELECT y FROM inserted) ";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", update.getTable().toString());
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals(1, withItems.size());
        Delete delete = withItems.get(0).getDelete().getDelete();
        assertEquals("x", delete.getTable().toString());
        assertEquals("bar = 2", delete.getWhere().toString());
        assertEquals(" RETURNING y", delete.getReturningClause().toString());
        assertEquals(" deleted", withItems.get(0).getAlias().toString());
    }

    @Test
    void testDeleteAndInsertWithin2Ctes() throws JSQLParserException {
        String sqlStr = "WITH deleted AS ( " +
                "   DELETE FROM x " +
                "    WHERE bar = 2 " +
                "   RETURNING y " +
                ") " +
                ", inserted AS ( " +
                "   INSERT INTO x (foo) " +
                "   SELECT bar FROM b " +
                "    WHERE y IN (SELECT y FROM deleted) " +
                "   RETURNING w " +
                ") " +
                "   UPDATE z " +
                "      SET foo = 1 " +
                "    WHERE y IN (SELECT y FROM inserted) ";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", update.getTable().toString());
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals(2, withItems.size());
        Delete delete = withItems.get(0).getDelete().getDelete();
        assertEquals("x", delete.getTable().toString());
        assertEquals("bar = 2", delete.getWhere().toString());
        assertEquals(" RETURNING y", delete.getReturningClause().toString());
        assertEquals(" deleted", withItems.get(0).getAlias().toString());
        Insert insert = withItems.get(1).getInsert().getInsert();
        assertEquals("x", insert.getTable().toString());
        assertEquals("SELECT bar FROM b WHERE y IN (SELECT y FROM deleted)",
                insert.getSelect().toString());
        assertEquals(" RETURNING w", insert.getReturningClause().toString());
        assertEquals(
                "INSERT INTO x (foo) SELECT bar FROM b WHERE y IN (SELECT y FROM deleted) RETURNING w",
                insert.toString());
        assertEquals(" inserted", withItems.get(1).getAlias().toString());
    }

    @Test
    void testSelectAndInsertWithin2Ctes() throws JSQLParserException {
        String sqlStr = "WITH selection AS ( " +
                "   SELECT y " +
                "     FROM z " +
                "    WHERE foo = 'bar' " +
                ") " +
                ", inserted AS ( " +
                "   INSERT INTO x (foo) " +
                "   SELECT bar FROM b " +
                "    WHERE y IN (SELECT y FROM selection) " +
                "   RETURNING w " +
                ") " +
                "   UPDATE z " +
                "      SET foo = 1 " +
                "    WHERE y IN (SELECT y FROM inserted) ";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", update.getTable().toString());
        List<WithItem<?>> withItems = update.getWithItemsList();
        assertEquals(2, withItems.size());
        PlainSelect select = withItems.get(0).getSelect().getPlainSelect();
        assertEquals("SELECT y FROM z WHERE foo = 'bar'", select.toString());
        assertEquals(" selection", withItems.get(0).getAlias().toString());
        Insert insert = withItems.get(1).getInsert().getInsert();
        assertEquals("x", insert.getTable().toString());
        assertEquals("SELECT bar FROM b WHERE y IN (SELECT y FROM selection)",
                insert.getSelect().toString());
        assertEquals(" RETURNING w", insert.getReturningClause().toString());
        assertEquals(
                "INSERT INTO x (foo) SELECT bar FROM b WHERE y IN (SELECT y FROM selection) RETURNING w",
                insert.toString());
        assertEquals(" inserted", withItems.get(1).getAlias().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING HIGH mycolumn",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING LOW mycolumn",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING 1 = 1",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING (HIGH mycolumn)",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING INVERSE (HIGH mycolumn)",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING HIGH mycolumn1 PRIOR TO LOW mycolumn2",
            "UPDATE mytable SET mycolumn1 = mycolumn2 PREFERRING HIGH mycolumn1 PLUS LOW mycolumn2"
    })
    public void testPreferringClause(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @Test
    public void testUpdateWithBoolean() throws JSQLParserException {
        String statement = "UPDATE mytable set col1='as', col2=true Where o >= 3";
        Update update = (Update) PARSER_MANAGER.parse(new StringReader(statement));
        assertEquals("mytable", update.getTable().toString());
        assertEquals(2, update.getUpdateSets().size());
        assertEquals("col1", update.getUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("col2", update.getUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("as",
                ((StringValue) update.getUpdateSets().get(0).getValues().get(0)).getValue());
        assertInstanceOf(BooleanValue.class, update.getUpdateSets().get(1).getValues().get(0));
        assertTrue(((BooleanValue) update.getUpdateSets().get(1).getValues().get(0)).getValue());
        assertInstanceOf(GreaterThanEquals.class, update.getWhere());
    }

    @Test
    public void testUpdateWithSkylineKeywords() throws JSQLParserException {
        String statement =
        " UPDATE mytable " +
        "    SET low = 1, high = 2, inverse = 3, plus = 4, preferring = 5 " +
        "  WHERE id = 6";
        Update update = (Update) PARSER_MANAGER.parse(new StringReader(statement));
        assertEquals("mytable", update.getTable().toString());
        assertEquals(5, update.getUpdateSets().size());
        assertEquals("low", update.getUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("high", update.getUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("inverse", update.getUpdateSets().get(2).getColumns().get(0).getColumnName());
        assertEquals("plus", update.getUpdateSets().get(3).getColumns().get(0).getColumnName());
        assertEquals("preferring", update.getUpdateSets().get(4).getColumns().get(0).getColumnName());
        assertInstanceOf(EqualsTo.class, update.getWhere());
    }

}
