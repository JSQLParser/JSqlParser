package net.sf.jsqlparser.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

/**
 *
 * @author toben
 */
public class SelectUtilsTest {

    public SelectUtilsTest() {}

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    /**
     * Test of addColumn method, of class SelectUtils.
     */
    @Test
    public void testAddExpr() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("select a from mytable");
        SelectUtils.addExpression(select, new Column("b"));
        assertEquals("SELECT a, b FROM mytable", select.toString());

        Addition add = new Addition();
        add.setLeftExpression(new LongValue(5));
        add.setRightExpression(new LongValue(6));
        SelectUtils.addExpression(select, add);

        assertEquals("SELECT a, b, 5 + 6 FROM mytable", select.toString());
    }

    @Test
    public void testAddJoin() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("select a from mytable");
        final EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column("a"));
        equalsTo.setRightExpression(new Column("b"));
        Join addJoin = SelectUtils.addJoin(select, new Table("mytable2"), equalsTo);
        addJoin.setLeft(true);
        assertEquals("SELECT a FROM mytable LEFT JOIN mytable2 ON a = b", select.toString());
    }

    @Test
    public void testBuildSelectFromTableAndExpressions() {
        Select select = SelectUtils.buildSelectFromTableAndExpressions(new Table("mytable"), new Column("a"), new Column("b"));
        assertEquals("SELECT a, b FROM mytable", select.toString());
    }

    @Test
    public void testBuildSelectFromTable() {
        Select select = SelectUtils.buildSelectFromTable(new Table("mytable"));
        assertEquals("SELECT * FROM mytable", select.toString());
    }

    @Test
    public void testBuildSelectFromTableAndParsedExpression() throws JSQLParserException {
        Select select = SelectUtils.buildSelectFromTableAndExpressions(new Table("mytable"), "a+b", "test");
        assertEquals("SELECT a + b, test FROM mytable", select.toString());

        assertTrue(((SelectExpressionItem) ((PlainSelect) select.getSelectBody()).getSelectItems().get(0)).getExpression() instanceof Addition);
    }

    @Test
    public void testBuildSelectFromTableWithGroupBy() {
        Select select = SelectUtils.buildSelectFromTable(new Table("mytable"));
        SelectUtils.addGroupBy(select, new Column("b"));
        assertEquals("SELECT * FROM mytable GROUP BY b", select.toString());
    }

    @Test
    public void testTableAliasIssue311() {
        Table table1 = new Table("mytable1");
        table1.setAlias(new Alias("tab1"));
        Table table2 = new Table("mytable2");
        table2.setAlias(new Alias("tab2"));

        List<? extends Expression> colunas = Arrays.asList(new Column(table1, "col1"), new Column(table1, "col2"), new Column(table1, "col3"), new Column(table2, "b1"), new Column(table2, "b2"));

        Select select = SelectUtils.buildSelectFromTableAndExpressions(table1, colunas.toArray(new Expression[colunas.size()]));

        final EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(table1, "col1"));
        equalsTo.setRightExpression(new Column(table2, "b1"));
        Join addJoin = SelectUtils.addJoin(select, table2, equalsTo);
        addJoin.setLeft(true);

        assertEquals("SELECT tab1.col1, tab1.col2, tab1.col3, tab2.b1, tab2.b2 FROM mytable1 AS tab1 LEFT JOIN mytable2 AS tab2 ON tab1.col1 = tab2.b1",
            select.toString());
    }

    public void testTableAliasIssue311_2() {
        Table table1 = new Table("mytable1");
        table1.setAlias(new Alias("tab1"));

        Column col = new Column(table1, "col1");
        assertEquals("tab1.col1", col.toString());
        assertEquals("mytable.col1", col.getFullyQualifiedName());
    }
}
