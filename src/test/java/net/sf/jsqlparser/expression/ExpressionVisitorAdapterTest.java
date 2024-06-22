/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.ExcludesExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IncludesExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author tw
 */
public class ExpressionVisitorAdapterTest {

    @Test
    public void testInExpressionProblem() throws JSQLParserException {
        final List<Object> exprList = new ArrayList<>();
        PlainSelect plainSelect =
                (PlainSelect) CCJSqlParserUtil.parse("select * from foo where x in (?,?,?)");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(InExpression expr, S parameters) {
                super.visit(expr, parameters);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getRightExpression());
                return null;
            }
        }, null);

        assertInstanceOf(Column.class, exprList.get(0));
        assertInstanceOf(ExpressionList.class, exprList.get(1));
    }

    @Test
    public void testInExpression() throws JSQLParserException {
        final List<Object> exprList = new ArrayList<>();
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil
                .parse("select * from foo where (a,b) in (select a,b from foo2)");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(InExpression expr, S parameters) {
                super.visit(expr, parameters);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getRightExpression());
                return null;
            }
        }, null);

        assertInstanceOf(ExpressionList.class, exprList.get(0));
        assertInstanceOf(Select.class, exprList.get(1));
    }

    @Test
    public void testXorExpression() throws JSQLParserException {
        final List<Expression> exprList = new ArrayList<>();
        PlainSelect plainSelect =
                (PlainSelect) CCJSqlParserUtil.parse("SELECT * FROM table WHERE foo XOR bar");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(XorExpression expr, S parameters) {
                super.visit(expr, parameters);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getRightExpression());
                return null;
            }
        }, null);

        assertEquals(2, exprList.size());
        assertInstanceOf(Column.class, exprList.get(0));
        assertEquals("foo", ((Column) exprList.get(0)).getColumnName());
        assertInstanceOf(Column.class, exprList.get(1));
        assertEquals("bar", ((Column) exprList.get(1)).getColumnName());
    }

    @Test
    public void testOracleHintExpressions() throws JSQLParserException {
        testOracleHintExpression("select --+ MYHINT \n * from foo", "MYHINT", true);
        testOracleHintExpression("select /*+ MYHINT */ * from foo", "MYHINT", false);
    }

    public static void testOracleHintExpression(String sql, String hint, boolean singleLine)
            throws JSQLParserException {
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil.parse(sql);
        final OracleHint[] holder = new OracleHint[1];
        assertNotNull(plainSelect.getOracleHint());
        plainSelect.getOracleHint().accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(OracleHint hint, S parameters) {
                super.visit(hint, parameters);
                holder[0] = hint;
                return null;
            }
        }, null);

        assertNotNull(holder[0]);
        assertEquals(singleLine, holder[0].isSingleLine());
        assertEquals(hint, holder[0].getValue());
    }

    @Test
    public void testCurrentTimestampExpression() throws JSQLParserException {
        final List<String> columnList = new ArrayList<String>();
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil
                .parse("select * from foo where bar < CURRENT_TIMESTAMP");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(Column column, S parameters) {
                super.visit(column, parameters);
                columnList.add(column.getColumnName());
                return null;
            }
        }, null);

        assertEquals(1, columnList.size());
        assertEquals("bar", columnList.get(0));
    }

    @Test
    public void testCurrentDateExpression() throws JSQLParserException {
        final List<String> columnList = new ArrayList<String>();
        PlainSelect plainSelect =
                (PlainSelect) CCJSqlParserUtil.parse("select * from foo where bar < CURRENT_DATE");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(Column column, S parameters) {
                super.visit(column, parameters);
                columnList.add(column.getColumnName());
                return null;
            }
        }, null);

        assertEquals(1, columnList.size());
        assertEquals("bar", columnList.get(0));
    }

    @Test
    public void testSubSelectExpressionProblem() throws JSQLParserException {
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil
                .parse("SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE t2.col2 = t1.col1)");
        Expression where = plainSelect.getWhere();
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        adapter.setSelectVisitor(new SelectVisitorAdapter<>());
        try {
            where.accept(adapter, null);
        } catch (NullPointerException npe) {
            fail();
        }
    }

    @Test
    public void testCaseWithoutElse() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("CASE WHEN 1 then 0 END");
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        expr.accept(adapter, null);
    }

    @Test
    public void testCaseWithoutElse2() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("CASE WHEN 1 then 0 ELSE -1 END");
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        expr.accept(adapter, null);
    }

    @Test
    public void testCaseWithoutElse3() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("CASE 3+4 WHEN 1 then 0 END");
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        expr.accept(adapter, null);
    }

    @Test
    public void testAnalyticFunctionWithoutExpression502() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("row_number() over (order by c)");
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        expr.accept(adapter, null);
    }

    @Test
    public void testAtTimeZoneExpression() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil
                .parseExpression("DATE(date1 AT TIME ZONE 'UTC' AT TIME ZONE 'australia/sydney')");
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        expr.accept(adapter, null);
    }

    @Test
    public void testJsonFunction() throws JSQLParserException {
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        CCJSqlParserUtil.parseExpression("JSON_OBJECT( KEY 'foo' VALUE bar, KEY 'foo' VALUE bar)")
                .accept(adapter, null);
        CCJSqlParserUtil.parseExpression("JSON_ARRAY( (SELECT * from dual) )").accept(adapter,
                null);
    }

    @Test
    public void testJsonAggregateFunction() throws JSQLParserException {
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        CCJSqlParserUtil.parseExpression(
                "JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name )")
                .accept(adapter, null);
        CCJSqlParserUtil.parseExpression(
                "JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name )")
                .accept(adapter, null);
    }

    @Test
    public void testConnectedByRootExpression() throws JSQLParserException {
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        CCJSqlParserUtil.parseExpression("CONNECT_BY_ROOT last_name as name").accept(adapter, null);
    }

    @Test
    public void testRowConstructor() throws JSQLParserException {
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        CCJSqlParserUtil.parseExpression(
                "CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR))")
                .accept(adapter, null);
    }

    @Test
    public void testAllTableColumns() throws JSQLParserException {
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil.parse("select a.* from foo a");
        final AllTableColumns[] holder = new AllTableColumns[1];
        Expression from = plainSelect.getSelectItems().get(0).getExpression();
        from.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(AllTableColumns all, S parameters) {
                holder[0] = all;
                return null;
            }
        }, null);

        assertNotNull(holder[0]);
        assertEquals("a.*", holder[0].toString());
    }

    @Test
    public void testAnalyticExpressionWithPartialWindowElement() throws JSQLParserException {
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<>();
        Expression expression = CCJSqlParserUtil.parseExpression(
                "SUM(\"Spent\") OVER (PARTITION BY \"ID\" ORDER BY \"Name\" ASC ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)");

        expression.accept(adapter, null);
    }

    @Test
    public void testIncludesExpression() throws JSQLParserException {
        final List<Object> exprList = new ArrayList<>();
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil
                .parse("select id from foo where b includes ('A', 'B')");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(IncludesExpression expr, S parameters) {
                super.visit(expr, parameters);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getRightExpression());
                return null;
            }
        }, null);

        assertInstanceOf(Column.class, exprList.get(0));
        assertInstanceOf(ParenthesedExpressionList.class, exprList.get(1));
    }

    @Test
    public void testExcludesExpression() throws JSQLParserException {
        final List<Object> exprList = new ArrayList<>();
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil
                .parse("select id from foo where b Excludes ('A', 'B')");
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter<Void>() {

            @Override
            public <S> Void visit(ExcludesExpression expr, S parameters) {
                super.visit(expr, parameters);
                exprList.add(expr.getLeftExpression());
                exprList.add(expr.getRightExpression());
                return null;
            }
        }, null);

        assertInstanceOf(Column.class, exprList.get(0));
        assertInstanceOf(ParenthesedExpressionList.class, exprList.get(1));
    }
}
