/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.delete;

import java.io.StringReader;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DeleteTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testDelete() throws JSQLParserException {
        String statement = "DELETE FROM mytable WHERE mytable.col = 9";

        Delete delete = (Delete) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", delete.getTable().getName());
        assertEquals(statement, "" + delete);
    }

    @Test
    public void testDeleteWhereProblem1() throws JSQLParserException {
        String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteWithLimit() throws JSQLParserException {
        String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 LIMIT 5";
        Delete parsed = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
        AndExpression where = parsed.getWhere(AndExpression.class);
        EqualsTo left = where.getLeftExpression(EqualsTo.class);
        assertEquals("a", left.getLeftExpression(Column.class).getColumnName());
        EqualsTo right = where.getRightExpression(EqualsTo.class);
        assertEquals("b", right.getLeftExpression(Column.class).getColumnName());
        assertEquals(5, parsed.getLimit().getRowCount(LongValue.class).getValue());
    }

    @Test
    public void testDeleteDoesNotAllowLimitOffset() {
        String statement = "DELETE FROM table1 WHERE A.cod_table = 'YYY' LIMIT 3,4";
        assertThrows(JSQLParserException.class,
                () -> parserManager.parse(new StringReader(statement)));
    }

    @Test
    public void testDeleteWithOrderBy() throws JSQLParserException {
        String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteWithOrderByAndLimit() throws JSQLParserException {
        String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col LIMIT 10";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteFromTableUsingInnerJoinToAnotherTable() throws JSQLParserException {
        String stmt = "DELETE Table1 FROM Table1 INNER JOIN Table2 ON Table1.ID = Table2.ID";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteFromTableUsingLeftJoinToAnotherTable() throws JSQLParserException {
        String stmt = "DELETE g FROM Table1 AS g LEFT JOIN Table2 ON Table1.ID = Table2.ID";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteFromTableUsingInnerJoinToAnotherTableWithAlias()
            throws JSQLParserException {
        String stmt =
                "DELETE gc FROM guide_category AS gc LEFT JOIN guide AS g ON g.id_guide = gc.id_guide WHERE g.title IS NULL LIMIT 5";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDeleteMultiTableIssue878() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DELETE table1, table2 FROM table1, table2");
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        String sql = "DELETE /*+ SOMEHINT */ FROM mytable WHERE mytable.col = 9";

        assertOracleHintExists(sql, true, "SOMEHINT");

        // @todo: add a testcase supposed to not finding a misplaced hint
    }

    @Test
    public void testWith() throws JSQLParserException {
        String statement = ""
                + "WITH a\n"
                + "     AS (SELECT 1 id_instrument_ref)\n"
                + "     , b\n"
                + "       AS (SELECT 1 id_instrument_ref)\n"
                + "DELETE FROM cfe.instrument_ref\n"
                + "WHERE  id_instrument_ref = (SELECT id_instrument_ref\n"
                + "                            FROM   a)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(statement, true);
        List<WithItem<?>> withItems = delete.getWithItemsList();
        assertEquals("cfe.instrument_ref", delete.getTable().getFullyQualifiedName());
        assertEquals(2, withItems.size());
        SelectItem selectItem1 =
                withItems.get(0).getSelect().getPlainSelect().getSelectItems().get(0);
        assertEquals("1", selectItem1.getExpression().toString());
        assertEquals(" id_instrument_ref", selectItem1.getAlias().toString());
        assertEquals(" a", withItems.get(0).getAlias().toString());
        SelectItem selectItem2 =
                withItems.get(1).getSelect().getPlainSelect().getSelectItems().get(0);
        assertEquals("1", selectItem2.getExpression().toString());
        assertEquals(" id_instrument_ref", selectItem2.getAlias().toString());
        assertEquals(" b", withItems.get(1).getAlias().toString());
    }

    @Test
    public void testNoFrom() throws JSQLParserException {
        String statement = "DELETE A WHERE Z = 1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testNoFromWithSchema() throws JSQLParserException {
        String statement = "DELETE A.B WHERE Z = 1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testUsing() throws JSQLParserException {
        String statement = "DELETE A USING B.C D WHERE D.Z = 1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testDeleteLowPriority() throws JSQLParserException {
        String stmt = "DELETE LOW_PRIORITY FROM tablename";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
        assertEquals(delete.getModifierPriority(), DeleteModifierPriority.LOW_PRIORITY);
    }

    @Test
    public void testDeleteQuickModifier() throws JSQLParserException {
        String stmt = "DELETE QUICK FROM tablename";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
        assertTrue(delete.isModifierQuick());
        String stmt2 = "DELETE FROM tablename";
        Delete delete2 = (Delete) assertSqlCanBeParsedAndDeparsed(stmt2);
        assertFalse(delete2.isModifierQuick());
    }

    @Test
    public void testDeleteIgnoreModifier() throws JSQLParserException {
        String stmt = "DELETE IGNORE FROM tablename";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
        assertTrue(delete.isModifierIgnore());
        String stmt2 = "DELETE FROM tablename";
        Delete delete2 = (Delete) assertSqlCanBeParsedAndDeparsed(stmt2);
        assertFalse(delete2.isModifierIgnore());
    }

    @Test
    public void testDeleteMultipleModifiers() throws JSQLParserException {
        String stmt = "DELETE LOW_PRIORITY QUICK FROM tablename";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
        assertEquals(delete.getModifierPriority(), DeleteModifierPriority.LOW_PRIORITY);
        assertTrue(delete.isModifierQuick());

        String stmt2 = "DELETE LOW_PRIORITY QUICK IGNORE FROM tablename";
        Delete delete2 = (Delete) assertSqlCanBeParsedAndDeparsed(stmt2);
        assertEquals(delete2.getModifierPriority(), DeleteModifierPriority.LOW_PRIORITY);
        assertTrue(delete2.isModifierIgnore());
        assertTrue(delete2.isModifierQuick());
    }

    @Test
    public void testDeleteReturningIssue1527() throws JSQLParserException {
        String statement = "delete from t returning *";
        assertSqlCanBeParsedAndDeparsed(statement, true);

        statement = "delete from products\n" +
                "  WHERE price <= 99.99\n" +
                "  RETURNING name, price AS new_price";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    @Test
    public void testDeleteOutputClause() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "DELETE Sales.ShoppingCartItem OUTPUT DELETED.* FROM Sales", true);

        assertSqlCanBeParsedAndDeparsed(
                "DELETE Sales.ShoppingCartItem OUTPUT Sales.ShoppingCartItem FROM Sales", true);

        assertSqlCanBeParsedAndDeparsed(
                "DELETE Production.ProductProductPhoto  \n" +
                        "OUTPUT DELETED.ProductID,  \n" +
                        "       p.Name,  \n" +
                        "       p.ProductModelID,  \n" +
                        "       DELETED.ProductPhotoID  \n" +
                        "    INTO @MyTableVar  \n" +
                        "FROM Production.ProductProductPhoto AS ph  \n" +
                        "JOIN Production.Product as p   \n" +
                        "    ON ph.ProductID = p.ProductID   \n" +
                        "    WHERE p.ProductModelID BETWEEN 120 and 130",
                true);
    }

    @Test
    void testInsertWithinCte() throws JSQLParserException {
        String sqlStr = "WITH inserted AS ( " +
                "   INSERT INTO x (foo) " +
                "   SELECT bar FROM b " +
                "   RETURNING y " +
                ") " +
                "DELETE " +
                "  FROM z" +
                " WHERE y IN (SELECT y FROM inserted)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", delete.getTable().toString());
        List<WithItem<?>> withItems = delete.getWithItemsList();
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
                "DELETE " +
                "  FROM z" +
                " WHERE y IN (SELECT y FROM updated)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", delete.getTable().toString());
        List<WithItem<?>> withItems = delete.getWithItemsList();
        assertEquals(1, withItems.size());
        Update update = withItems.get(0).getUpdate().getUpdate();
        assertEquals("x", update.getTable().toString());
        assertEquals("foo", update.getUpdateSets().get(0).getColumn(0).toString());
        assertEquals("1", update.getUpdateSets().get(0).getValue(0).toString());
        assertEquals("bar = 2", update.getWhere().toString());
        assertEquals(" RETURNING y", update.getReturningClause().toString());
        assertEquals(" updated", withItems.get(0).getAlias().toString());
    }

    @Test
    void testDeleteWithinCte() throws JSQLParserException {
        String sqlStr = "WITH deleted AS ( " +
                "   DELETE FROM x " +
                "    WHERE bar = 2 " +
                "   RETURNING y " +
                ") " +
                "DELETE " +
                "  FROM z" +
                " WHERE y IN (SELECT y FROM deleted)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", delete.getTable().toString());
        List<WithItem<?>> withItems = delete.getWithItemsList();
        assertEquals(1, withItems.size());
        Delete innerDelete = withItems.get(0).getDelete().getDelete();
        assertEquals("x", innerDelete.getTable().toString());
        assertEquals("bar = 2", innerDelete.getWhere().toString());
        assertEquals(" RETURNING y", innerDelete.getReturningClause().toString());
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
                "DELETE " +
                "  FROM z" +
                " WHERE w IN (SELECT w FROM inserted)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", delete.getTable().toString());
        List<WithItem<?>> withItems = delete.getWithItemsList();
        assertEquals(2, withItems.size());
        Delete innerDelete = withItems.get(0).getDelete().getDelete();
        assertEquals("x", innerDelete.getTable().toString());
        assertEquals("bar = 2", innerDelete.getWhere().toString());
        assertEquals(" RETURNING y", innerDelete.getReturningClause().toString());
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
                "DELETE " +
                "  FROM z" +
                " WHERE w IN (SELECT w FROM inserted)";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(sqlStr);
        assertEquals("z", delete.getTable().toString());
        List<WithItem<?>> withItems = delete.getWithItemsList();
        assertEquals(2, withItems.size());
        PlainSelect innerSelect = withItems.get(0).getSelect().getPlainSelect();
        assertEquals("SELECT y FROM z WHERE foo = 'bar'", innerSelect.toString());
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
            "DELETE FROM mytable PREFERRING HIGH mycolumn",
            "DELETE FROM mytable PREFERRING LOW mycolumn",
            "DELETE FROM mytable PREFERRING 1 = 1",
            "DELETE FROM mytable PREFERRING (HIGH mycolumn)",
            "DELETE FROM mytable PREFERRING INVERSE (HIGH mycolumn)",
            "DELETE FROM mytable PREFERRING HIGH mycolumn1 PRIOR TO LOW mycolumn2",
            "DELETE FROM mytable PREFERRING HIGH mycolumn1 PLUS LOW mycolumn2"
    })
    public void testPreferringClause(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @Test
    public void testDeleteWithSkylineKeywords() throws JSQLParserException {
        String statement =
                "DELETE FROM mytable WHERE low = 1 AND high = 2 AND inverse = 3 AND plus = 4";
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(statement);
        assertEquals("mytable", delete.getTable().toString());
        assertEquals("low = 1 AND high = 2 AND inverse = 3 AND plus = 4",
                delete.getWhere().toString());
    }

}
