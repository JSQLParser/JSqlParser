/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayExpressionTest {

    @Test
    void testColumnArrayExpression() throws JSQLParserException {
        String sqlStr = "SELECT a[2+1] AS a";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SelectItem<?> selectItem = select.getSelectItem(0);

        Column column = selectItem.getExpression(Column.class);
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());
    }

    @Test
    void testNavigationAfterSubscript() throws JSQLParserException {
        // Redshift SUPER navigation: subscript then field access
        String sqlStr = "SELECT recommendations[0].language_id FROM recs";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SelectItem<?> selectItem = select.getSelectItem(0);

        RowGetExpression rowGet = selectItem.getExpression(RowGetExpression.class);
        assertNotNull(rowGet);
        assertEquals("language_id", rowGet.getColumnName());
        Column column = assertInstanceOf(Column.class, rowGet.getExpression());
        assertEquals("recommendations", column.getColumnName());
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());
    }

    @Test
    void testAlternatingNavigationChain() throws JSQLParserException {
        // field accesses and subscripts may alternate freely after the primary expression
        String sqlStr = "SELECT a.b[0].c[1].d FROM t";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SelectItem<?> selectItem = select.getSelectItem(0);

        RowGetExpression outer = selectItem.getExpression(RowGetExpression.class);
        assertNotNull(outer);
        assertEquals("d", outer.getColumnName());
        ArrayExpression subscript = assertInstanceOf(ArrayExpression.class, outer.getExpression());
        assertInstanceOf(LongValue.class, subscript.getIndexExpression());
        RowGetExpression inner =
                assertInstanceOf(RowGetExpression.class, subscript.getObjExpression());
        assertEquals("c", inner.getColumnName());
        Column column = assertInstanceOf(Column.class, inner.getExpression());
        assertEquals("a", column.getTable().getName());
        assertEquals("b", column.getColumnName());
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());
    }

    @Test
    void testNavigationFieldNames() throws JSQLParserException {
        // dotted continuations accept FROM/SELECT/CURRENT and quoted names,
        // same as ColumnIdentifier's dotted continuation (RelObjectNameExt)
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0].from FROM t", true);
        RowGetExpression rowGet = select.getSelectItem(0).getExpression(RowGetExpression.class);
        assertEquals("from", rowGet.getColumnName());

        select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0].\"quoted field\" FROM t", true);
        rowGet = select.getSelectItem(0).getExpression(RowGetExpression.class);
        assertEquals("\"quoted field\"", rowGet.getColumnName());
    }

    @Test
    void testNavigationInClauses() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE a[0].b = 1", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t ORDER BY a[0].b", true);
    }

    @Test
    void testNavigationThenCastAndJson() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0].b::text FROM t", true);
        CastExpression cast = select.getSelectItem(0).getExpression(CastExpression.class);
        assertInstanceOf(RowGetExpression.class, cast.getLeftExpression());

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0].b -> 'x' FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.b[1 : 2].c FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0].b[1][2].c FROM t", true);
    }

    @Test
    void testNavigationDoesNotSwallowQualifiedColumns() throws JSQLParserException {
        // the navigation loop must not change how plain qualified columns parse
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.b FROM t", true);
        Column column = select.getSelectItem(0).getExpression(Column.class);
        assertEquals("a", column.getTable().getName());
        assertEquals("b", column.getColumnName());
        assertNull(column.getArrayConstructor());

        select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.b.c FROM t", true);
        column = select.getSelectItem(0).getExpression(Column.class);
        assertEquals("a.b.c", column.toString());

        select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.b[0] FROM t", true);
        column = select.getSelectItem(0).getExpression(Column.class);
        assertEquals("a", column.getTable().getName());
        assertEquals("b", column.getColumnName());
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());

        // a bracket group after a subscript stays on the ArrayExpression slot,
        // not on the navigation loop
        select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a[0]['k'] FROM t", true);
        ArrayExpression array = select.getSelectItem(0).getExpression(ArrayExpression.class);
        assertNotNull(array);
        column = assertInstanceOf(Column.class, array.getObjExpression());
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());
    }

    @Test
    void testMalformedNavigationRejected() {
        assertThrows(JSQLParserException.class,
                () -> TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a[0].* FROM t", true));
    }

    @Test
    void testColumnSlices() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT l[2:3] FROM t", "SELECT l[:2] FROM t",
                "SELECT l[2:] FROM t", "SELECT l[:] FROM t", "SELECT l[-5:] FROM t",
                "SELECT l[1:-1:2] FROM t", "SELECT l[1:-:2] FROM t", "SELECT l[:-:-1] FROM t",
                "SELECT l[x:y] FROM t", "SELECT s[2:] > 'a' FROM t"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }

        PlainSelect select = (PlainSelect) TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT l[1:-:2] FROM t", true);
        RangeExpression slice = (RangeExpression) select.getSelectItem(0)
                .getExpression(Column.class).getArrayConstructor().getExpressions().get(0);
        assertEquals("1", slice.getStartExpression().toString());
        assertNull(slice.getEndExpression());
        assertEquals("2", slice.getStepExpression().toString());
    }

    @Test
    void testExpressionSlices() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT f()[2:] FROM t", "SELECT (l)[:2] FROM t",
                "SELECT ([1, 2, 3])[1:-:-1]", "SELECT l[1][2:3] FROM t",
                "SELECT l[:2][1] FROM t"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }

        ArrayExpression slice = (ArrayExpression) CCJSqlParserUtil.parseExpression("f()[x:y:2]");
        assertNull(slice.getIndexExpression());
        assertEquals("x", slice.getStartIndexExpression().toString());
        assertEquals("y", slice.getStopIndexExpression().toString());
        assertEquals("2", slice.getStepExpression().toString());
    }

    @Test
    void testEmptyStepIsDropped() throws JSQLParserException {
        assertEquals("l[1:2]", CCJSqlParserUtil.parseExpression("l[1:2:]").toString());
    }

    @Test
    void testJsonPathOutsideSubscripts() throws JSQLParserException {
        assertInstanceOf(JsonExpression.class, CCJSqlParserUtil.parseExpression("c:a[1]"));
    }

    @Test
    void testJsonPathNestedInSubscript() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT l[(SELECT c : a FROM t2)] FROM t",
                "SELECT l[f(x, c : a)] FROM t", "SELECT l[CASE WHEN c : a = 1 THEN 1 END] FROM t",
                "SELECT f()[(SELECT c : a FROM t)]"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }

        PlainSelect select = (PlainSelect) TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT l[ARRAY[c : a]] FROM t", true);
        ArrayConstructor array = (ArrayConstructor) select.getSelectItem(0)
                .getExpression(Column.class).getArrayConstructor().getExpressions().get(0);
        assertInstanceOf(JsonExpression.class, array.getExpressions().get(0));
    }
}
