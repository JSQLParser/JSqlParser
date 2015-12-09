package net.sf.jsqlparser.test.select;

import junit.framework.*;
import net.sf.jsqlparser.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;

import static net.sf.jsqlparser.test.TestUtils.*;

public class SelectTest extends TestCase {

    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public SelectTest(String arg0) {
        super(arg0);
    }

    // From statement multipart
    public void testMultiPartTableNameWithServerNameAndDatabaseNameAndSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM [server-name\\server-instance].databaseName.schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithServerNameAndDatabaseName() throws Exception {
        final String statement = "SELECT columnName FROM [server-name\\server-instance].databaseName..tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithServerNameAndSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM [server-name\\server-instance]..schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithServerProblem() throws Exception {
        final String statement = "SELECT * FROM LINK_100.htsac.dbo.t_transfer_num a";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testMultiPartTableNameWithServerName() throws Exception {
        final String statement = "SELECT columnName FROM [server-name\\server-instance]...tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithDatabaseNameAndSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM databaseName.schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithDatabaseName() throws Exception {
        final String statement = "SELECT columnName FROM databaseName..tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartTableNameWithColumnName() throws Exception {
        final String statement = "SELECT columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    // Select statement statement multipart
    public void testMultiPartColumnNameWithDatabaseNameAndSchemaNameAndTableName() throws Exception {
        final String statement = "SELECT databaseName.schemaName.tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testMultiPartColumnNameWithDatabaseNameAndSchemaName() {
        final String statement = "SELECT databaseName.schemaName..columnName FROM tableName";
        Select select;
        try {
            select = (Select) parserManager.parse(new StringReader(statement));
            fail("must not work");
        } catch (JSQLParserException ex) {
            //Logger.getLogger(SelectTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testMultiPartColumnNameWithDatabaseNameAndTableName() throws Exception {
        final String statement = "SELECT databaseName..tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "columnName", "databaseName..tableName.columnName");
    }

    public void testMultiPartColumnNameWithDatabaseName() {
        final String statement = "SELECT databaseName...columnName FROM tableName";
        Select select;
        try {
            select = (Select) parserManager.parse(new StringReader(statement));
            fail("must not work");
        } catch (JSQLParserException ex) {
            //Logger.getLogger(SelectTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testMultiPartColumnNameWithSchemaNameAndTableName() throws Exception {
        final String statement = "SELECT schemaName.tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "columnName", "schemaName.tableName.columnName");
    }

    public void testMultiPartColumnNameWithSchemaName() {
        final String statement = "SELECT schemaName..columnName FROM tableName";
        Select select;
        try {
            select = (Select) parserManager.parse(new StringReader(statement));
            fail("must not work");
        } catch (JSQLParserException ex) {
            //Logger.getLogger(SelectTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testMultiPartColumnNameWithTableName() throws Exception {
        final String statement = "SELECT tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "columnName", "tableName.columnName");
    }

    public void testMultiPartColumnName() throws Exception {
        final String statement = "SELECT columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "columnName", "columnName");
    }

    void checkMultipartIdentifier(Select select, String columnName, String fullColumnName) {
        final Expression expr = ((SelectExpressionItem) ((PlainSelect) select.getSelectBody()).getSelectItems().get(0)).getExpression();
        assertTrue(expr instanceof Column);
        Column col = (Column) expr;
        assertEquals(columnName, col.getColumnName());
        assertEquals(fullColumnName, col.getFullyQualifiedName());
    }

    public void testAllColumnsFromTable() throws Exception {
        final String statement = "SELECT tableName.* FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        assertTrue(((PlainSelect) select.getSelectBody()).getSelectItems().get(0) instanceof AllTableColumns);
    }

    public void testSimpleSigns() throws JSQLParserException {
        final String statement = "SELECT +1, -1 FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testSimpleAdditionsAndSubtractionsWithSigns() throws JSQLParserException {
        final String statement = "SELECT 1 - 1, 1 + 1, -1 - 1, -1 + 1, +1 + 1, +1 - 1 FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testOperationsWithSigns() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("1 - -1");
        assertEquals("1 - -1", expr.toString());
        assertTrue(expr instanceof Subtraction);
        Subtraction sub = (Subtraction) expr;
        assertTrue(sub.getLeftExpression() instanceof LongValue);
        assertTrue(sub.getRightExpression() instanceof SignedExpression);

        SignedExpression sexpr = (SignedExpression) sub.getRightExpression();
        assertEquals('-', sexpr.getSign());
        assertEquals("1", sexpr.getExpression().toString());
    }

    public void testSignedColumns() throws JSQLParserException {
        final String statement = "SELECT -columnName, +columnName, +(columnName), -(columnName) FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testSigns() throws Exception {
        final String statement = "SELECT (-(1)), -(1), (-(columnName)), -(columnName), (-1), -1, (-columnName), -columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testLimit() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(3, ((PlainSelect) select.getSelectBody()).getLimit().getOffset());
        assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());

        // toString uses standard syntax
        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(((PlainSelect) select.getSelectBody()).getLimit());
        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertTrue(((PlainSelect) select.getSelectBody()).getOffset().isOffsetJdbcParameter());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
        select = (Select) parserManager.parse(new StringReader(statement));
        SetOperationList setList = (SetOperationList) select.getSelectBody();
        assertEquals(3, setList.getLimit().getOffset());
        assertEquals(4, setList.getLimit().getRowCount());

        // toString uses standard syntax
        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable3 WHERE mytable4.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

    }

    public void testLimit2() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(3, ((PlainSelect) select.getSelectBody()).getLimit().getOffset());
        assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitNull());

        // toString uses standard syntax
        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT NULL OFFSET 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertEquals(-1, ((PlainSelect) select.getSelectBody()).getLimit().getRowCount());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());
        assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isLimitNull());
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 0 OFFSET 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertEquals(0, ((PlainSelect) select.getSelectBody()).getLimit().getRowCount());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());
        assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitNull());
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(((PlainSelect) select.getSelectBody()).getLimit());
        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertTrue(((PlainSelect) select.getSelectBody()).getOffset().isOffsetJdbcParameter());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
        select = (Select) parserManager.parse(new StringReader(statement));
        SetOperationList setList = (SetOperationList) select.getSelectBody();
        assertEquals(3, setList.getLimit().getOffset());
        assertEquals(4, setList.getLimit().getRowCount());

        // toString uses standard syntax
        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable3 WHERE mytable4.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testLimitSqlServer1() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROWS FETCH NEXT 5 ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetParam());
        assertNotNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getFetch().getFetchParam());
        assertFalse(((PlainSelect) select.getSelectBody()).getFetch().isFetchParamFirst());
        assertFalse(((PlainSelect) select.getSelectBody()).getOffset().isOffsetJdbcParameter());
        assertFalse(((PlainSelect) select.getSelectBody()).getFetch().isFetchJdbcParameter());
        assertEquals(3, ((PlainSelect) select.getSelectBody()).getOffset().getOffset());
        assertEquals(5, ((PlainSelect) select.getSelectBody()).getFetch().getRowCount());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testLimitSqlServer2() throws JSQLParserException {
        // Alternative with the other keywords
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROW FETCH FIRST 5 ROW ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertNotNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROW", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetParam());
        assertEquals("ROW", ((PlainSelect) select.getSelectBody()).getFetch().getFetchParam());
        assertTrue(((PlainSelect) select.getSelectBody()).getFetch().isFetchParamFirst());
        assertEquals(3, ((PlainSelect) select.getSelectBody()).getOffset().getOffset());
        assertEquals(5, ((PlainSelect) select.getSelectBody()).getFetch().getRowCount());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testLimitSqlServer3() throws JSQLParserException {
        // Query with no Fetch
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROWS";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetParam());
        assertEquals(3, ((PlainSelect) select.getSelectBody()).getOffset().getOffset());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testLimitSqlServer4() throws JSQLParserException {
        // For Oracle syntax, query with no offset
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id FETCH NEXT 5 ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertNotNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getFetch().getFetchParam());
        assertFalse(((PlainSelect) select.getSelectBody()).getFetch().isFetchParamFirst());
        assertEquals(5, ((PlainSelect) select.getSelectBody()).getFetch().getRowCount());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testLimitSqlServerJdbcParameters() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(((PlainSelect) select.getSelectBody()).getOffset());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getOffset().getOffsetParam());
        assertNotNull(((PlainSelect) select.getSelectBody()).getFetch());
        assertEquals("ROWS", ((PlainSelect) select.getSelectBody()).getFetch().getFetchParam());
        assertFalse(((PlainSelect) select.getSelectBody()).getFetch().isFetchParamFirst());
        assertTrue(((PlainSelect) select.getSelectBody()).getOffset().isOffsetJdbcParameter());
        assertTrue(((PlainSelect) select.getSelectBody()).getFetch().isFetchJdbcParameter());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testTop() throws JSQLParserException {
        String statement = "SELECT TOP 3 * FROM mytable WHERE mytable.col = 9";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(3, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "select top 5 foo from bar";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertEquals(5, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());
    }

    public void testTopWithParenthesis() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT TOP (5) PERCENT " + firstColumnName + ", " + secondColumnName + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();

        final Top top = selectBody.getTop();
        assertEquals(5, top.getRowCount());
        assertFalse(top.isRowCountJdbcParameter());
        assertTrue(top.hasParenthesis());
        assertTrue(top.isPercentage());

        final List<SelectItem> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testSkip() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT SKIP 5 " + firstColumnName + ", " + secondColumnName + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();

        final Skip skip = selectBody.getSkip();
        assertEquals((long) 5, (long) skip.getRowCount());
        assertNull(skip.getJdbcParameter());
        assertNull(skip.getVariable());

        final List<SelectItem> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);

        final String statement2 = "SELECT SKIP skipVar c1, c2 FROM t";
        final Select select2 = (Select) parserManager.parse(new StringReader(statement2));

        final PlainSelect selectBody2 = (PlainSelect) select2.getSelectBody();

        final Skip skip2 = selectBody2.getSkip();
        assertNull(skip2.getRowCount());
        assertNull(skip2.getJdbcParameter());
        assertEquals("skipVar", skip2.getVariable());

        final List<SelectItem> selectItems2 = selectBody2.getSelectItems();
        assertEquals(2, selectItems2.size());
        assertEquals("c1", selectItems2.get(0).toString());
        assertEquals("c2", selectItems2.get(1).toString());

        assertStatementCanBeDeparsedAs(select2, statement2);
    }

    public void testFirst() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT FIRST 5 " + firstColumnName + ", " + secondColumnName + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();

        final First limit = selectBody.getFirst();
        assertEquals((long) 5, (long) limit.getRowCount());
        assertNull(limit.getJdbcParameter());
        assertEquals(First.Keyword.FIRST, limit.getKeyword());

        final List<SelectItem> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);

        final String statement2 = "SELECT FIRST firstVar c1, c2 FROM t";
        final Select select2 = (Select) parserManager.parse(new StringReader(statement2));

        final PlainSelect selectBody2 = (PlainSelect) select2.getSelectBody();

        final First first2 = selectBody2.getFirst();
        assertNull(first2.getRowCount());
        assertNull(first2.getJdbcParameter());
        assertEquals("firstVar", first2.getVariable());

        final List<SelectItem> selectItems2 = selectBody2.getSelectItems();
        assertEquals(2, selectItems2.size());
        assertEquals("c1", selectItems2.get(0).toString());
        assertEquals("c2", selectItems2.get(1).toString());

        assertStatementCanBeDeparsedAs(select2, statement2);
    }

    public void testFirstWithKeywordLimit() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT LIMIT ? " + firstColumnName + ", " + secondColumnName + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();

        final First limit = selectBody.getFirst();
        assertNull(limit.getRowCount());
        assertNotNull(limit.getJdbcParameter());
        assertNull(limit.getJdbcParameter().getIndex());
        assertEquals(First.Keyword.LIMIT, limit.getKeyword());

        final List<SelectItem> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testSkipFirst() throws JSQLParserException {
        final String statement = "SELECT SKIP ?1 FIRST f1 c1, c2 FROM t1";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();

        final Skip skip = selectBody.getSkip();
        assertNotNull(skip.getJdbcParameter());
        assertNotNull(skip.getJdbcParameter().getIndex());
        assertEquals((int) 1, (int) skip.getJdbcParameter().getIndex());
        assertNull(skip.getVariable());
        final First first = selectBody.getFirst();
        assertNull(first.getJdbcParameter());
        assertNull(first.getRowCount());
        assertEquals("f1", first.getVariable());

        final List<SelectItem> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals("c1", selectItems.get(0).toString());
        assertEquals("c2", selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testSelectItems() throws JSQLParserException {
        String statement = "SELECT myid AS MYID, mycol, tab.*, schema.tab.*, mytab.mycol2, myschema.mytab.mycol, myschema.mytab.* FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        final List<SelectItem> selectItems = plainSelect.getSelectItems();
        assertEquals("MYID", ((SelectExpressionItem) selectItems.get(0)).getAlias().getName());
        assertEquals("mycol", ((Column) ((SelectExpressionItem) selectItems.get(1)).getExpression()).getColumnName());
        assertEquals("tab", ((AllTableColumns) selectItems.get(2)).getTable().getName());
        assertEquals("schema", ((AllTableColumns) selectItems.get(3)).getTable().getSchemaName());
        assertEquals("schema.tab", ((AllTableColumns) selectItems.get(3)).getTable().getFullyQualifiedName());
        assertEquals("mytab.mycol2", ((Column) ((SelectExpressionItem) selectItems.get(4)).getExpression()).getFullyQualifiedName());
        assertEquals("myschema.mytab.mycol", ((Column) ((SelectExpressionItem) selectItems.get(5)).getExpression()).getFullyQualifiedName());
        assertEquals("myschema.mytab", ((AllTableColumns) selectItems.get(6)).getTable().getFullyQualifiedName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT myid AS MYID, (SELECT MAX(ID) AS myid2 FROM mytable2) AS myalias FROM mytable WHERE mytable.col = 9";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("myalias", ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT (myid + myid2) AS MYID FROM mytable WHERE mytable.col = 9";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("MYID", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testUnion() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 UNION "
                + "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION " + "SELECT * FROM mytable2 LIMIT 3,4";

        Select select = (Select) parserManager.parse(new StringReader(statement));
        SetOperationList setList = (SetOperationList) select.getSelectBody();
        assertEquals(3, setList.getSelects().size());
        assertEquals("mytable", ((Table) ((PlainSelect) setList.getSelects().get(0)).getFromItem()).getName());
        assertEquals("mytable3", ((Table) ((PlainSelect) setList.getSelects().get(1)).getFromItem()).getName());
        assertEquals("mytable2", ((Table) ((PlainSelect) setList.getSelects().get(2)).getFromItem()).getName());
        assertEquals(3, ((PlainSelect) setList.getSelects().get(2)).getLimit().getOffset());

        // use brakets for toString
        // use standard limit syntax
        String statementToString = "(SELECT * FROM mytable WHERE mytable.col = 9) UNION "
                + "(SELECT * FROM mytable3 WHERE mytable3.col = ?) UNION "
                + "(SELECT * FROM mytable2 LIMIT 4 OFFSET 3)";
        assertStatementCanBeDeparsedAs(select, statementToString);
    }

    public void testDistinct() throws JSQLParserException {
        String statement = "SELECT DISTINCT ON (myid) myid, mycol FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("myid",
                ((Column) ((SelectExpressionItem) plainSelect.getDistinct().getOnSelectItems().get(0)).getExpression())
                .getColumnName());
        assertEquals("mycol",
                ((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testDistinctTop() throws JSQLParserException {
        String statement = "SELECT DISTINCT TOP 5 myid, mycol FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("myid",
                ((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression())
                .getColumnName());
        assertEquals("mycol",
                ((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
        assertNotNull(plainSelect.getTop());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testDistinctTop2() {
        String statement = "SELECT TOP 5 DISTINCT myid, mycol FROM mytable WHERE mytable.col = 9";
        try {
            parserManager.parse(new StringReader(statement));
            fail("sould not work");
        } catch (JSQLParserException ex) {
        }
    }

    public void testFrom() throws JSQLParserException {
        String statement = "SELECT * FROM mytable as mytable0, mytable1 alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";
        String statementToString = "SELECT * FROM mytable AS mytable0, mytable1 alias_tab1, mytable2 AS alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";

        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(3, plainSelect.getJoins().size());
        assertEquals("mytable0", plainSelect.getFromItem().getAlias().getName());
        assertEquals("alias_tab1", plainSelect.getJoins().get(0).getRightItem().getAlias().getName());
        assertEquals("alias_tab2", plainSelect.getJoins().get(1).getRightItem().getAlias().getName());
        assertEquals("mytable4", plainSelect.getJoins().get(2).getRightItem().getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statementToString);
    }

    public void testJoin() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("tab2", ((Table) plainSelect.getJoins().get(0).getRightItem()).getFullyQualifiedName());
        assertEquals("tab1.id",
                ((Column) ((EqualsTo) plainSelect.getJoins().get(0).getOnExpression()).getLeftExpression())
                .getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isOuter());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getJoins().size());
        assertEquals("tab3", ((Table) plainSelect.getJoins().get(1).getRightItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(1).isOuter());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id JOIN tab3";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getJoins().size());
        assertEquals("tab3", ((Table) plainSelect.getJoins().get(1).getRightItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(1).isOuter());
        assertStatementCanBeDeparsedAs(select, statement);

        // implicit INNER
        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM TA2 LEFT OUTER JOIN O USING (col1, col2) WHERE D.OasSD = 'asdf' AND (kj >= 4 OR l < 'sdf')";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 INNER JOIN tab2 USING (id, id2)";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("tab2", ((Table) plainSelect.getJoins().get(0).getRightItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(0).isOuter());
        assertEquals(2, plainSelect.getJoins().get(0).getUsingColumns().size());
        assertEquals("id2",
                plainSelect.getJoins().get(0).getUsingColumns().get(1).getFullyQualifiedName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 RIGHT OUTER JOIN tab2 USING (id, id2)";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM foo AS f LEFT OUTER JOIN (bar AS b RIGHT OUTER JOIN baz AS z ON f.id = z.id) ON f.id = b.id";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);

    }

    public void testFunctions() throws JSQLParserException {
        String statement = "SELECT MAX(id) AS max FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("max", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT MAX(id), AVG(pro) AS myavg FROM mytable WHERE mytable.col = 9 GROUP BY pro";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("myavg", ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT MAX(a, b, c), COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        Function fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
        assertEquals("MAX", fun.getName());
        assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getFullyQualifiedName());
        assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression())
                .isAllColumns());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT {fn MAX(a, b, c)}, COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
        assertTrue(fun.isEscaped());
        assertEquals("MAX", fun.getName());
        assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getFullyQualifiedName());
        assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression())
                .isAllColumns());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT ab.MAX(a, b, c), cd.COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
        assertEquals("ab.MAX", fun.getName());
        assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getFullyQualifiedName());
        fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression();
        assertEquals("cd.COUNT", fun.getName());
        assertTrue(fun.isAllColumns());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testWhere() throws JSQLParserException {

        final String statement = "SELECT * FROM tab1 WHERE";
        String whereToString = "(a + b + c / d + e * f) * (a / b * (a + b)) > ?";
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " "
                + whereToString))).getSelectBody();
        assertTrue(plainSelect.getWhere() instanceof GreaterThan);
        assertTrue(((GreaterThan) plainSelect.getWhere()).getLeftExpression() instanceof Multiplication);
        assertEquals(statement + " " + whereToString, plainSelect.toString());

        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

        whereToString = "(7 * s + 9 / 3) NOT BETWEEN 3 AND ?";
        plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " " + whereToString)))
                .getSelectBody();

        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
        assertEquals(statement + " " + whereToString, plainSelect.toString());

        whereToString = "a / b NOT IN (?, 's''adf', 234.2)";
        plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " " + whereToString)))
                .getSelectBody();

        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
        assertEquals(statement + " " + whereToString, plainSelect.toString());

        whereToString = " NOT 0 = 0";
        plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + whereToString)))
                .getSelectBody();

        whereToString = " NOT (0 = 0)";
        plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + whereToString)))
                .getSelectBody();

        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
        assertEquals(statement + whereToString, plainSelect.toString());
    }

    public void testGroupBy() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(1, plainSelect.getGroupByColumnReferences().size());
        assertEquals("tab1.b", ((Column) plainSelect.getGroupByColumnReferences().get(0)).getFullyQualifiedName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY 2, 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getGroupByColumnReferences().size());
        assertEquals(2, ((LongValue) plainSelect.getGroupByColumnReferences().get(0)).getValue());
        assertEquals(3, ((LongValue) plainSelect.getGroupByColumnReferences().get(1)).getValue());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testHaving() throws JSQLParserException {
        String statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 GROUP BY tab1.b HAVING MAX(tab1.b) > 56";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertTrue(plainSelect.getHaving() instanceof GreaterThan);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 HAVING MAX(tab1.b) IN (56, 32, 3, ?)";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertTrue(plainSelect.getHaving() instanceof InExpression);
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testExists() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE ";
        String where = "EXISTS (SELECT * FROM tab2)";
        statement += where;
        Statement parsed = parserManager.parse(new StringReader(statement));

        assertEquals(statement, parsed.toString());

        PlainSelect plainSelect = (PlainSelect) ((Select) parsed).getSelectBody();
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), where);
    }

    public void testOrderBy() throws JSQLParserException {
        // TODO: should there be a DESC marker in the OrderByElement class?
        String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
        String statementToString = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("tab1.a",
                ((Column) plainSelect.getOrderByElements().get(0).getExpression())
                .getFullyQualifiedName());
        assertEquals("b",
                ((Column) plainSelect.getOrderByElements().get(1).getExpression()).getColumnName());
        assertTrue(plainSelect.getOrderByElements().get(1).isAsc());
        assertFalse(plainSelect.getOrderByElements().get(0).isAsc());
        assertStatementCanBeDeparsedAs(select, statementToString);

        statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a, 2";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("a",
                ((Column) plainSelect.getOrderByElements().get(0).getExpression()).getColumnName());
        assertEquals(2,
                ((LongValue) plainSelect.getOrderByElements().get(1).getExpression()).getValue());
        assertStatementCanBeDeparsedAs(select, statement);

    }

    public void testOrderByNullsFirst() throws JSQLParserException {
        String statement = "SELECT a FROM tab1 ORDER BY a NULLS FIRST";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testTimestamp() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > {ts '2004-04-30 04:05:34.56'}";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("2004-04-30 04:05:34.56",
                ((TimestampValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue().toString());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testTime() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > {t '04:05:34'}";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("04:05:34",
                (((TimeValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
        assertStatementCanBeDeparsedAs(select, statement);
    }
    
    public void testBetweenDate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE col BETWEEN {d '2015-09-19'} AND {d '2015-09-24'}");
    }

    public void testCase() throws JSQLParserException {
        String statement = "SELECT a, CASE b WHEN 1 THEN 2 END FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE WHEN (a > 2) THEN 3 END) AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE WHEN a > 2 THEN 3 ELSE 4 END) AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' " + "END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' " + "END) FROM tab1 "
                + "WHERE c = (CASE " + "WHEN d <> 3 THEN 5 " + "ELSE 10 " + "END)";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, CASE a " + "WHEN 'b' THEN 'BBB' " + "WHEN 'a' THEN 'AAA' " + "END AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END > 34";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 + 3 ELSE 4 END > 34";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE " + "WHEN (CASE a WHEN 1 THEN 10 ELSE 20 END) > 15 THEN 'BBB' "
                + // "WHEN (SELECT c FROM tab2 WHERE d = 2) = 3 THEN 'AAA' " +
                "END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

    }

    public void testReplaceAsFunction() throws JSQLParserException {
        String statement = "SELECT REPLACE(a, 'b', c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        Statement stmt = CCJSqlParserUtil.parse(statement);
        Select select = (Select) stmt;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        assertEquals(1, plainSelect.getSelectItems().size());
        Expression expression = ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
        assertTrue(expression instanceof Function);
        Function func = (Function) expression;
        assertEquals("REPLACE", func.getName());
        assertEquals(3, func.getParameters().getExpressions().size());
    }

    public void testLike() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a LIKE 'test'";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("test", ((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue());

        statement = "SELECT * FROM tab1 WHERE a LIKE 'test' ESCAPE 'test2'";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
        plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("test", ((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue());
        assertEquals("test2", ((LikeExpression) plainSelect.getWhere()).getEscape());
    }

    public void testIlike() throws JSQLParserException {
        String statement = "SELECT col1 FROM table1 WHERE col1 ILIKE '%hello%'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testSelectOrderHaving() throws JSQLParserException {
        String statement = "SELECT units, count(units) AS num FROM currency GROUP BY units HAVING count(units) > 1 ORDER BY num";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testDouble() throws JSQLParserException {
        String statement = "SELECT 1e2, * FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e2, ((DoubleValue) ((SelectExpressionItem) ((PlainSelect) select.getSelectBody())
                .getSelectItems().get(0)).getExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 1.e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e2,
                ((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
                .getRightExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 1.2e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.2e2,
                ((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
                .getRightExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 2e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(2e2,
                ((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
                .getRightExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testDouble2() throws JSQLParserException {
        String statement = "SELECT 1.e22 FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e22, ((DoubleValue) ((SelectExpressionItem) ((PlainSelect) select.getSelectBody())
                .getSelectItems().get(0)).getExpression()).getValue(), 0);
    }

    public void testDouble3() throws JSQLParserException {
        String statement = "SELECT 1. FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.0, ((DoubleValue) ((SelectExpressionItem) ((PlainSelect) select.getSelectBody())
                .getSelectItems().get(0)).getExpression()).getValue(), 0);
    }

    public void testDouble4() throws JSQLParserException {
        String statement = "SELECT 1.2e22 FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.2e22, ((DoubleValue) ((SelectExpressionItem) ((PlainSelect) select.getSelectBody())
                .getSelectItems().get(0)).getExpression()).getValue(), 0);
    }

    public void testWith() throws JSQLParserException {
        String statement = "WITH DINFO (DEPTNO, AVGSALARY, EMPCOUNT) AS "
                + "(SELECT OTHERS.WORKDEPT, AVG(OTHERS.SALARY), COUNT(*) FROM EMPLOYEE AS OTHERS "
                + "GROUP BY OTHERS.WORKDEPT), DINFOMAX AS (SELECT MAX(AVGSALARY) AS AVGMAX FROM DINFO) "
                + "SELECT THIS_EMP.EMPNO, THIS_EMP.SALARY, DINFO.AVGSALARY, DINFO.EMPCOUNT, DINFOMAX.AVGMAX "
                + "FROM EMPLOYEE AS THIS_EMP INNER JOIN DINFO INNER JOIN DINFOMAX "
                + "WHERE THIS_EMP.JOB = 'SALESREP' AND THIS_EMP.WORKDEPT = DINFO.DEPTNO";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testWithRecursive() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("WITH RECURSIVE t (n) AS ((SELECT 1) UNION ALL (SELECT n + 1 FROM t WHERE n < 100)) SELECT sum(n) FROM t");
    }

    public void testSelectAliasInQuotes() throws JSQLParserException {
        String statement = "SELECT mycolumn AS \"My Column Name\" FROM mytable";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testSelectAliasWithoutAs() throws JSQLParserException {
        String statement = "SELECT mycolumn \"My Column Name\" FROM mytable";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testSelectJoinWithComma() throws JSQLParserException {
        String statement = "SELECT cb.Genus, cb.Species FROM Coleccion_de_Briofitas AS cb, unigeoestados AS es "
                + "WHERE es.nombre = \"Tamaulipas\" AND cb.the_geom = es.geom";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testDeparser() throws JSQLParserException {
        String statement = "SELECT a.OWNERLASTNAME, a.OWNERFIRSTNAME " + "FROM ANTIQUEOWNERS AS a, ANTIQUES AS b "
                + "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT count(DISTINCT f + 4) FROM a";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT count(DISTINCT f, g, h) FROM a";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCount2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(ALL col1 + col2) FROM mytable");
    }

    public void testMysqlQuote() throws JSQLParserException {
        String statement = "SELECT `a.OWNERLASTNAME`, `OWNERFIRSTNAME` " + "FROM `ANTIQUEOWNERS` AS a, ANTIQUES AS b "
                + "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testConcat() throws JSQLParserException {
        String statement = "SELECT a || b || c + 4 FROM t";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testConcatProblem2() throws JSQLParserException {
        String stmt = "SELECT MAX((((("
                + "(SPA.SOORTAANLEVERPERIODE)::VARCHAR (2) || (VARCHAR(SPA.AANLEVERPERIODEJAAR))::VARCHAR (4)"
                + ") || TO_CHAR(SPA.AANLEVERPERIODEVOLGNR, 'FM09'::VARCHAR)"
                + ") || TO_CHAR((10000 - SPA.VERSCHIJNINGSVOLGNR), 'FM0999'::VARCHAR)"
                + ") || (SPA.GESLACHT)::VARCHAR (1))) AS GESLACHT_TMP FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_1() throws JSQLParserException {
        String stmt = "SELECT TO_CHAR(SPA.AANLEVERPERIODEVOLGNR, 'FM09'::VARCHAR) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_2() throws JSQLParserException {
        String stmt = "SELECT MAX((SPA.SOORTAANLEVERPERIODE)::VARCHAR (2) || (VARCHAR(SPA.AANLEVERPERIODEJAAR))::VARCHAR (4)) AS GESLACHT_TMP FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_3() throws JSQLParserException {
        String stmt = "SELECT TO_CHAR((10000 - SPA.VERSCHIJNINGSVOLGNR), 'FM0999'::VARCHAR) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_4() throws JSQLParserException {
        String stmt = "SELECT (SPA.GESLACHT)::VARCHAR (1) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_5() throws JSQLParserException {
        String stmt = "SELECT max((a || b) || c) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_5_1() throws JSQLParserException {
        String stmt = "SELECT (a || b) || c FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_5_2() throws JSQLParserException {
        String stmt = "SELECT (a + b) + c FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testConcatProblem2_6() throws JSQLParserException {
        String stmt = "SELECT max(a || b || c) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testMatches() throws JSQLParserException {
        String statement = "SELECT * FROM team WHERE team.search_column @@ to_tsquery('new & york & yankees')";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testGroupByExpression() throws JSQLParserException {
        String statement = "SELECT col1, col2, col1 + col2, sum(col8)" + " FROM table1 "
                + "GROUP BY col1, col2, col1 + col2";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testBitwise() throws JSQLParserException {
        String statement = "SELECT col1 & 32, col2 ^ col1, col1 | col2" + " FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testSelectFunction() throws JSQLParserException {
        String statement = "SELECT 1 + 2 AS sum";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testWeirdSelect() throws JSQLParserException {
        String sql = "select r.reviews_id, substring(rd.reviews_text, 100) as reviews_text, r.reviews_rating, r.date_added, r.customers_name from reviews r, reviews_description rd where r.products_id = '19' and r.reviews_id = rd.reviews_id and rd.languages_id = '1' and r.reviews_status = 1 order by r.reviews_id desc limit 0, 6";
        parserManager.parse(new StringReader(sql));
    }

    public void testCast() throws JSQLParserException {
        String stmt = "SELECT CAST(a AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
        stmt = "SELECT CAST(a AS varchar2) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastInCast() throws JSQLParserException {
        String stmt = "SELECT CAST(CAST(a AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastInCast2() throws JSQLParserException {
        String stmt = "SELECT CAST('test' + CAST(assertEqual AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem() throws JSQLParserException {
        String stmt = "SELECT CAST(col1 AS varchar (256)) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem2() throws JSQLParserException {
        String stmt = "SELECT col1::varchar FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem3() throws JSQLParserException {
        String stmt = "SELECT col1::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem4() throws JSQLParserException {
        String stmt = "SELECT 5::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem5() throws JSQLParserException {
        String stmt = "SELECT 5.67::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem6() throws JSQLParserException {
        String stmt = "SELECT 'test'::character varying FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCastTypeProblem7() throws JSQLParserException {
        String stmt = "SELECT CAST('test' AS character varying) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testCaseElseAddition() throws JSQLParserException {
        String stmt = "SELECT CASE WHEN 1 + 3 > 20 THEN 0 ELSE 1000 + 1 END AS d FROM dual";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testBrackets() throws JSQLParserException {
        String stmt = "SELECT table_a.name AS [Test] FROM table_a";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testBrackets2() throws JSQLParserException {
        String stmt = "SELECT [a] FROM t";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlServer_Modulo_Proz() throws Exception {
        String stmt = "SELECT 5 % 2 FROM A";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlServer_Modulo_mod() throws Exception {
        String stmt = "SELECT mod(5, 2) FROM A";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlServer_Modulo() throws Exception {
        String stmt = "SELECT convert(varchar(255), DATEDIFF(month, year1, abc_datum) / 12) + ' year, ' + convert(varchar(255), DATEDIFF(month, year2, abc_datum) % 12) + ' month' FROM test_table";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testIsNot() throws JSQLParserException {
        String stmt = "SELECT * FROM test WHERE a IS NOT NULL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testIsNot2() throws JSQLParserException {
        //the deparser delivers always a IS NOT NULL even for NOT a IS NULL
        String stmt = "SELECT * FROM test WHERE NOT a IS NULL";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "SELECT * FROM test WHERE a IS NOT NULL");
    }

    public void testProblemSqlAnalytic() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (ORDER BY a) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic2() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic3() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic4EmptyOver() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic5AggregateColumnValue() throws JSQLParserException {
        String stmt = "SELECT a, sum(b) OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic6AggregateColumnValue() throws JSQLParserException {
        String stmt = "SELECT a, sum(b + 5) OVER (ORDER BY a) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic7Count() throws JSQLParserException {
        String stmt = "SELECT count(*) OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic8Complex() throws JSQLParserException {
        String stmt = "SELECT ID, NAME, SALARY, SUM(SALARY) OVER () AS SUM_SAL, AVG(SALARY) OVER () AS AVG_SAL, MIN(SALARY) OVER () AS MIN_SAL, MAX(SALARY) OVER () AS MAX_SAL, COUNT(*) OVER () AS ROWS2 FROM STAFF WHERE ID < 60 ORDER BY ID";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic9CommaListPartition() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (PARTITION BY c, d ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic10Lag() throws JSQLParserException {
        String stmt = "SELECT a, lag(a, 1) OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlAnalytic11Lag() throws JSQLParserException {
        String stmt = "SELECT a, lag(a, 1, 0) OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testAnalyticFunction12() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (PARTITION BY b ORDER BY c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction13() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER () FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction14() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (PARTITION BY b ) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction15() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (ORDER BY c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction16() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (ORDER BY c NULLS FIRST) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction17() throws JSQLParserException {
        String statement = "SELECT AVG(sal) OVER (PARTITION BY deptno ORDER BY sal ROWS BETWEEN 0 PRECEDING AND 0 PRECEDING) AS avg_of_current_sal FROM emp";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunction18() throws JSQLParserException {
        String statement = "SELECT AVG(sal) OVER (PARTITION BY deptno ORDER BY sal RANGE CURRENT ROW) AS avg_of_current_sal FROM emp";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunctionProblem1() throws JSQLParserException {
        String statement = "SELECT last_value(s.revenue_hold) OVER (PARTITION BY s.id_d_insertion_order, s.id_d_product_ad_attr, trunc(s.date_id, 'mm') ORDER BY s.date_id) AS col FROM s";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testAnalyticFunctionProblem1b() throws JSQLParserException {
        String statement = "SELECT last_value(s.revenue_hold) OVER (PARTITION BY s.id_d_insertion_order, s.id_d_product_ad_attr, trunc(s.date_id, 'mm') ORDER BY s.date_id ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS col FROM s";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testFunctionLeft() throws JSQLParserException {
        String statement = "SELECT left(table1.col1, 4) FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testFunctionRight() throws JSQLParserException {
        String statement = "SELECT right(table1.col1, 4) FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testOracleJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a = tabelle2.b(+)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleJoin2() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) = tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleJoin2_1() throws JSQLParserException {
        String[] values = new String[]{"(+)", "( +)", "(+ )", "( + )", " (+) "};
        for (String value : values) {
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a" + value + " = tabelle2.b", true);
        }
    }

    public void testOracleJoin2_2() throws JSQLParserException {
        String[] values = new String[]{"(+)", "( +)", "(+ )", "( + )", " (+) "};
        for (String value : values) {
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a = tabelle2.b" + value, true);
        }
    }

    public void testOracleJoin3() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) > tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleJoin3_1() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a > tabelle2.b(+)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleJoin4() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) = tabelle2.b AND tabelle1.b(+) IN ('A', 'B')";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemSqlIntersect() throws Exception {
        String stmt = "(SELECT * FROM a) INTERSECT (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a INTERSECT SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "(SELECT * FROM a) INTERSECT (SELECT * FROM b)");
    }

    public void testProblemSqlExcept() throws Exception {
        String stmt = "(SELECT * FROM a) EXCEPT (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a EXCEPT SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "(SELECT * FROM a) EXCEPT (SELECT * FROM b)");
    }

    public void testProblemSqlMinus() throws Exception {
        String stmt = "(SELECT * FROM a) MINUS (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a MINUS SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "(SELECT * FROM a) MINUS (SELECT * FROM b)");
    }

    public void testProblemSqlCombinedSets() throws Exception {
        String stmt = "(SELECT * FROM a) INTERSECT (SELECT * FROM b) UNION (SELECT * FROM c)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithStatement() throws JSQLParserException {
        String stmt = "WITH test AS (SELECT mslink FROM feature) SELECT * FROM feature WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithUnionProblem() throws JSQLParserException {
        String stmt = "WITH test AS ((SELECT mslink FROM tablea) UNION (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithUnionAllProblem() throws JSQLParserException {
        String stmt = "WITH test AS ((SELECT mslink FROM tablea) UNION ALL (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithUnionProblem3() throws JSQLParserException {
        String stmt = "WITH test AS ((SELECT mslink, CAST(tablea.fname AS varchar) FROM tablea INNER JOIN tableb ON tablea.mslink = tableb.mslink AND tableb.deleted = 0 WHERE tablea.fname IS NULL AND 1 = 0) UNION ALL (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithUnionProblem4() throws JSQLParserException {
        String stmt = "WITH hist AS ((SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, 0 AS level, CAST(gl.mslink AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 WHERE gl.parent IS NULL AND gl.mslink <> 0) UNION ALL (SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, hist.level + 1 AS level, CAST(hist.path + '.' + CAST(gl.mslink AS VARCHAR) AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 INNER JOIN hist ON gl.parent = hist.mslink WHERE gl.mslink <> 0)) SELECT mslink, space(level * 4) + txt AS txt, nr, feature, path FROM hist WHERE EXISTS (SELECT feature FROM tablec WHERE mslink = 0 AND ((feature IN (1, 2) AND hist.feature = 3) OR (feature IN (4) AND hist.feature = 2)))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testWithUnionProblem5() throws JSQLParserException {
        String stmt = "WITH hist AS ((SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, 0 AS level, CAST(gl.mslink AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 WHERE gl.parent IS NULL AND gl.mslink <> 0) UNION ALL (SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, hist.level + 1 AS level, CAST(hist.path + '.' + CAST(gl.mslink AS VARCHAR) AS VARCHAR) AS path, 5 AS feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 INNER JOIN hist ON gl.parent = hist.mslink WHERE gl.mslink <> 0)) SELECT * FROM hist";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testExtractFrom1() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(month FROM datecolumn) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testExtractFrom2() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(year FROM now()) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testExtractFrom3() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(year FROM (now() - 2)) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testExtractFrom4() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(minutes FROM now() - '01:22:00') FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemFunction() throws JSQLParserException {
        String stmt = "SELECT test() FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
        Statement parsed = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) parsed;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        SelectItem get = plainSelect.getSelectItems().get(0);
        SelectExpressionItem item = (SelectExpressionItem) get;
        assertTrue(item.getExpression() instanceof Function);
        assertEquals("test", ((Function) item.getExpression()).getName());
    }

    public void testProblemFunction2() throws JSQLParserException {
        String stmt = "SELECT sysdate FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testProblemFunction3() throws JSQLParserException {
        String stmt = "SELECT TRUNCATE(col) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testAdditionalLettersGerman() throws JSQLParserException {
        String stmt = "SELECT colä, colö, colü FROM testtableäöü";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT colA, colÖ, colÜ FROM testtableÄÖÜ";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT Äcol FROM testtableÄÖÜ";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT ßcolß FROM testtableß";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testAdditionalLettersSpanish() throws JSQLParserException {
        String stmt = "SELECT * FROM años";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testMultiTableJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM taba INNER JOIN tabb ON taba.a = tabb.a, tabc LEFT JOIN tabd ON tabc.c = tabd.c";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testTableCrossJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM taba CROSS JOIN tabb";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testLateral1() throws JSQLParserException {
        String stmt = "SELECT O.ORDERID, O.CUSTNAME, OL.LINETOTAL FROM ORDERS AS O, LATERAL(SELECT SUM(NETAMT) AS LINETOTAL FROM ORDERLINES AS LINES WHERE LINES.ORDERID = O.ORDERID) AS OL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testLateralComplex1() throws IOException, JSQLParserException {
        String stmt = IOUtils.toString(SelectTest.class.getResourceAsStream("complex-lateral-select-request.txt"));
        Select select = (Select) parserManager.parse(new StringReader(stmt));
        assertEquals("SELECT O.ORDERID, O.CUSTNAME, OL.LINETOTAL, OC.ORDCHGTOTAL, OT.TAXTOTAL FROM ORDERS O, LATERAL(SELECT SUM(NETAMT) AS LINETOTAL FROM ORDERLINES LINES WHERE LINES.ORDERID = O.ORDERID) AS OL, LATERAL(SELECT SUM(CHGAMT) AS ORDCHGTOTAL FROM ORDERCHARGES CHARGES WHERE LINES.ORDERID = O.ORDERID) AS OC, LATERAL(SELECT SUM(TAXAMT) AS TAXTOTAL FROM ORDERTAXES TAXES WHERE TAXES.ORDERID = O.ORDERID) AS OT", select.toString());
    }

    public void testValues() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES (1, 2), (3, 4)) AS test";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testValues2() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES 1, 2, 3, 4) AS test";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testValues3() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES 1, 2, 3, 4) AS test(a)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testValues4() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES (1, 2), (3, 4)) AS test(a, b)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testValues5() throws JSQLParserException {
        String stmt = "SELECT X, Y FROM (VALUES (0, 'a'), (1, 'b')) AS MY_TEMP_TABLE(X, Y)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testValues6BothVariants() throws JSQLParserException {
        String stmt = "SELECT I FROM (VALUES 1, 2, 3) AS MY_TEMP_TABLE(I) WHERE I IN (SELECT * FROM (VALUES 1, 2) AS TEST)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testInterval1() throws JSQLParserException {
        String stmt = "SELECT 5 + INTERVAL '3 days'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testInterval2() throws JSQLParserException {
        String stmt = "SELECT to_timestamp(to_char(now() - INTERVAL '45 MINUTE', 'YYYY-MM-DD-HH24:')) AS START_TIME FROM tab1";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        assertEquals(1, plainSelect.getSelectItems().size());
        SelectExpressionItem item = (SelectExpressionItem) plainSelect.getSelectItems().get(0);
        Function function = (Function) item.getExpression();

        assertEquals("to_timestamp", function.getName());

        assertEquals(1, function.getParameters().getExpressions().size());

        Function func2 = (Function) function.getParameters().getExpressions().get(0);

        assertEquals("to_char", func2.getName());

        assertEquals(2, func2.getParameters().getExpressions().size());
        Subtraction sub = (Subtraction) func2.getParameters().getExpressions().get(0);
        assertTrue(sub.getRightExpression() instanceof IntervalExpression);
        IntervalExpression iexpr = (IntervalExpression) sub.getRightExpression();

        assertEquals("'45 MINUTE'", iexpr.getParameter());
    }

    public void testInterval3() throws JSQLParserException {
        String stmt = "SELECT 5 + INTERVAL '3' day";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
    
    public void testInterval4() throws JSQLParserException {
        String stmt = "SELECT '2008-12-31 23:59:59' + INTERVAL 1 SECOND";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
     
    public void testMultiValueIn() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (a, b, c) IN (SELECT a, b, c FROM mytable2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testMultiValueIn2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (trim(a), trim(b)) IN (SELECT a, b FROM mytable2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivot1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) FOR b IN ('val1'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivot2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) FOR b IN (10, 20, 30))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivot3() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) AS vals FOR b IN (10 AS d1, 20, 30 AS d3))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivot4() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a), sum(b) FOR b IN (10, 20, 30))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivot5() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) FOR (b, c) IN ((10, 'a'), (20, 'b'), (30, 'c')))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivotXml1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN ('val1'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivotXml2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN (SELECT vals FROM myothertable))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivotXml3() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN (ANY))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPivotXmlSubquery1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (SELECT times_purchased, state_code FROM customers t) PIVOT (count(state_code) FOR state_code IN ('NY', 'CT', 'NJ', 'FL', 'MO')) ORDER BY times_purchased");
    }

    public void testPivotFunction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT to_char((SELECT col1 FROM (SELECT times_purchased, state_code FROM customers t) PIVOT (count(state_code) FOR state_code IN ('NY', 'CT', 'NJ', 'FL', 'MO')) ORDER BY times_purchased)) FROM DUAL");
    }

    public void testRegexpLike1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE REGEXP_LIKE(first_name, '^Ste(v|ph)en$')";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testRegexpLike2() throws JSQLParserException {
        String stmt = "SELECT CASE WHEN REGEXP_LIKE(first_name, '^Ste(v|ph)en$') THEN 1 ELSE 2 END FROM mytable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testRegexpMySQL() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE first_name REGEXP '^Ste(v|ph)en$'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testRegexpBinaryMySQL() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE first_name REGEXP BINARY '^Ste(v|ph)en$'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testBooleanFunction1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE test_func(col1)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testNamedParameter() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE b = :param";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression exp = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        assertTrue(exp instanceof JdbcNamedParameter);
        JdbcNamedParameter namedParameter = (JdbcNamedParameter) exp;
        assertEquals("param", namedParameter.getName());

    }

    public void testNamedParameter2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        Expression exp_l = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        Expression exp_rl = ((BinaryExpression) exp_r).getLeftExpression();
        Expression exp_rr = ((BinaryExpression) exp_r).getRightExpression();

        Expression exp_param1 = ((BinaryExpression) exp_l).getRightExpression();
        Expression exp_param2 = ((BinaryExpression) exp_rl).getRightExpression();
        Expression exp_param3 = ((BinaryExpression) exp_rr).getRightExpression();

        assertTrue(exp_param1 instanceof JdbcNamedParameter);
        assertTrue(exp_param2 instanceof JdbcNamedParameter);
        assertTrue(exp_param3 instanceof JdbcNamedParameter);

        JdbcNamedParameter namedParameter1 = (JdbcNamedParameter) exp_param1;
        JdbcNamedParameter namedParameter2 = (JdbcNamedParameter) exp_param2;
        JdbcNamedParameter namedParameter3 = (JdbcNamedParameter) exp_param3;

        assertEquals("param", namedParameter1.getName());
        assertEquals("param2", namedParameter2.getName());
        assertEquals("param3", namedParameter3.getName());
    }

    public void testComplexUnion1() throws IOException, JSQLParserException {
        String stmt = "(SELECT 'abc-' || coalesce(mytab.a::varchar, '') AS a, mytab.b, mytab.c AS st, mytab.d, mytab.e FROM mytab WHERE mytab.del = 0) UNION (SELECT 'cde-' || coalesce(mytab2.a::varchar, '') AS a, mytab2.b, mytab2.bezeichnung AS c, 0 AS d, 0 AS e FROM mytab2 WHERE mytab2.del = 0)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleHierarchicalQuery() throws JSQLParserException {
        String stmt = "SELECT last_name, employee_id, manager_id FROM employees CONNECT BY employee_id = manager_id ORDER BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleHierarchicalQuery2() throws JSQLParserException {
        String stmt = "SELECT employee_id, last_name, manager_id FROM employees CONNECT BY PRIOR employee_id = manager_id";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleHierarchicalQuery3() throws JSQLParserException {
        String stmt = "SELECT last_name, employee_id, manager_id, LEVEL FROM employees START WITH employee_id = 100 CONNECT BY PRIOR employee_id = manager_id ORDER SIBLINGS BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testOracleHierarchicalQuery4() throws JSQLParserException {
        String stmt = "SELECT last_name, employee_id, manager_id, LEVEL FROM employees CONNECT BY PRIOR employee_id = manager_id START WITH employee_id = 100 ORDER SIBLINGS BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
    
    public void testOracleHierarchicalQueryIssue196() throws JSQLParserException {
        String stmt = "SELECT num1, num2, level FROM carol_tmp START WITH num2 = 1008 CONNECT BY num2 = PRIOR num1 ORDER BY level DESC";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPostgreSQLRegExpCaseSensitiveMatch() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a ~ '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPostgreSQLRegExpCaseSensitiveMatch2() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a ~* '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPostgreSQLRegExpCaseSensitiveMatch3() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a !~ '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testPostgreSQLRegExpCaseSensitiveMatch4() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a !~* '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testReservedKeyword() throws JSQLParserException {
        final String statement = "SELECT cast, do, extract, first, following, last, materialized, nulls, partition, range, row, rows, siblings, value, xml FROM tableName"; // all of these are legal in SQL server; 'row' and 'rows' are not legal on Oracle, though;
        final Select select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testReservedKeyword2() throws JSQLParserException {
        final String stmt = "SELECT open FROM tableName";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
    
    public void testReservedKeyword3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable1 t JOIN mytable2 AS prior ON t.id = prior.id");
    }

    public void testCharacterSetClause() throws JSQLParserException {
        String stmt = "SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    public void testNotEqualsTo() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a != b");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a <> b");
    }

    public void testJsonExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT data->'images'->'thumbnail'->'url' AS thumb FROM instagram");
    }

    public void testSelectInto1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * INTO user_copy FROM user");
    }

    public void testSelectForUpdate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM user_table FOR UPDATE");
    }

    public void testSelectJoin() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT pg_class.relname, pg_attribute.attname, pg_constraint.conname "
                + "FROM pg_constraint JOIN pg_class ON pg_class.oid = pg_constraint.conrelid"
                + " JOIN pg_attribute ON pg_attribute.attrelid = pg_constraint.conrelid"
                + " WHERE pg_constraint.contype = 'u' AND (pg_attribute.attnum = ANY(pg_constraint.conkey))"
                + " ORDER BY pg_constraint.conname");
    }

    public void testSelectJoin2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM pg_constraint WHERE pg_attribute.attnum = ANY(pg_constraint.conkey)");
    }

    public void testAnyConditionSubSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT e1.empno, e1.sal FROM emp e1 WHERE e1.sal > ANY (SELECT e2.sal FROM emp e2 WHERE e2.deptno = 10)");
    }

    public void testAllConditionSubSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT e1.empno, e1.sal FROM emp e1 WHERE e1.sal > ALL (SELECT e2.sal FROM emp e2 WHERE e2.deptno = 10)");
    }

    public void testSelectOracleColl() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM the_table tt WHERE TT.COL1 = lines(idx).COL1");
    }

    public void testSelectInnerWith() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (WITH actor AS (SELECT 'a' aid FROM DUAL) SELECT aid FROM actor)");
    }
    
    public void testSelectWithinGroup() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT LISTAGG(col1, '##') WITHIN GROUP (ORDER BY col1) FROM table1");
    }

    public void testSelectUserVariable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @col FROM t1");
    }

    public void testSelectNumericBind() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM b WHERE c = :1");
    }

    public void testSelectBrackets() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT avg((123.250)::numeric)");
    }

    public void testSelectBrackets2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (EXTRACT(epoch FROM age(d1, d2)) / 2)::numeric");
    }

    public void testSelectBrackets3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT avg((EXTRACT(epoch FROM age(d1, d2)) / 2)::numeric)");
    }

    public void testSelectBrackets4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (1 / 2)::numeric");
    }

    public void testSelectForUpdateOfTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT foo.*, bar.* FROM foo, bar WHERE foo.id = bar.foo_id FOR UPDATE OF foo");
    }

    public void testSelectWithBrackets() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("(SELECT 1 FROM mytable)");
    }

    public void testSelectWithBrackets2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("(SELECT 1)");
    }

    public void testSelectWithoutFrom() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT footable.foocolumn");
    }

    public void testSelectKeywordPercent() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT percent FROM MY_TABLE");
    }

    public void testSelectJPQLPositionalParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT email FROM users WHERE (type LIKE 'B') AND (username LIKE ?1)");
    }

    public void testSelectKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT col1, min(col2) KEEP (DENSE_RANK FIRST ORDER BY col3), col4 FROM table1 GROUP BY col5 ORDER BY col3");
    }

    public void testSelectKeepOver() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT MIN(salary) KEEP (DENSE_RANK FIRST ORDER BY commission_pct) OVER (PARTITION BY department_id ) \"Worst\" FROM employees ORDER BY department_id, salary");
    }

    public void testGroupConcat() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT student_name, GROUP_CONCAT(DISTINCT test_score ORDER BY test_score DESC SEPARATOR ' ') FROM student GROUP BY student_name");
    }

    public void testRowConstructor1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t1 WHERE (col1, col2) = (SELECT col3, col4 FROM t2 WHERE id = 10)");
    }

    public void testRowConstructor2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t1 WHERE ROW(col1, col2) = (SELECT col3, col4 FROM t2 WHERE id = 10)");
    }

    public void testIssue154() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT d.id, d.uuid, d.name, d.amount, d.percentage, d.modified_time FROM discount d LEFT OUTER JOIN discount_category dc ON d.id = dc.discount_id WHERE merchant_id = ? AND deleted = ? AND dc.discount_id IS NULL AND modified_time < ? AND modified_time >= ? ORDER BY modified_time");
    }

    public void testIssue154_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT r.id, r.uuid, r.name, r.system_role FROM role r WHERE r.merchant_id = ? AND r.deleted_time IS NULL ORDER BY r.id DESC");
    }

    public void testIssue160_signedParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT start_date WHERE start_date > DATEADD(HH, -?, GETDATE())");
    }

    public void testIssue160_signedParameter2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE -? = 5");
    }
    
    public void testIssue162_doubleUserVar() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @@SPID AS ID, SYSTEM_USER AS \"Login Name\", USER AS \"User Name\"");
    }
	
	public void testIssue167_singleQuoteEscape() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT 'a'");
		assertSqlCanBeParsedAndDeparsed("SELECT ''''");
		assertSqlCanBeParsedAndDeparsed("SELECT '\\''");
		assertSqlCanBeParsedAndDeparsed("SELECT 'ab''ab'");
		assertSqlCanBeParsedAndDeparsed("SELECT 'ab\\'ab'");
    }
	
	/**
	 * These are accepted due to reading one backslash and a double quote.
	 */
	public void testIssue167_singleQuoteEscape2() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("SELECT '\\'''");
		assertSqlCanBeParsedAndDeparsed("SELECT '\\\\''");
	}
	
	public void testIssue77_singleQuoteEscape2() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("SELECT 'test\\'' FROM dual");
	}
        
    public void testOracleHint() throws JSQLParserException {
        assertOracleHintExists("SELECT /*+ SOMEHINT */ * FROM mytable", true, "SOMEHINT");
        assertOracleHintExists("SELECT /*+ MORE HINTS POSSIBLE */ * FROM mytable", true, "MORE HINTS POSSIBLE");
        assertOracleHintExists("SELECT /*+   MORE\nHINTS\t\nPOSSIBLE  */ * FROM mytable", true, "MORE\nHINTS\t\nPOSSIBLE");
        assertOracleHintExists("SELECT /*+ leading(sn di md sh ot) cardinality(ot 1000) */ c, b FROM mytable", true, "leading(sn di md sh ot) cardinality(ot 1000)");
        assertOracleHintExists("SELECT /*+ ORDERED INDEX (b, jl_br_balances_n1) USE_NL (j b) \n" +
                "           USE_NL (glcc glf) USE_MERGE (gp gsb) */\n" +
                " b.application_id\n" +
                "FROM  jl_br_journals j,\n" +
                "      po_vendors p", true, "ORDERED INDEX (b, jl_br_balances_n1) USE_NL (j b) \n" +
                "           USE_NL (glcc glf) USE_MERGE (gp gsb)");
        assertOracleHintExists("SELECT /*+ROWID(emp)*/ /*+ THIS IS NOT HINT! ***/ * \n" +
                "FROM emp \n" +
                "WHERE rowid > 'AAAAtkAABAAAFNTAAA' AND empno = 155", false, "ROWID(emp)");
        assertOracleHintExists("SELECT /*+ INDEX(patients sex_index) use sex_index because there are few\n" +
                "   male patients  */ name, height, weight\n" +
                "FROM patients\n" +
                "WHERE sex = 'm'", true, "INDEX(patients sex_index) use sex_index because there are few\n   male patients");
        assertOracleHintExists("SELECT /*+INDEX_COMBINE(emp sal_bmi hiredate_bmi)*/ * \n" +
                "FROM emp  \n" +
                "WHERE sal < 50000 AND hiredate < '01-JAN-1990'", true, "INDEX_COMBINE(emp sal_bmi hiredate_bmi)");
        assertOracleHintExists("SELECT --+ CLUSTER \n" +
                "emp.ename, deptno\n" +
                "FROM emp, dept\n" +
                "WHERE deptno = 10 \n" +
                "AND emp.deptno = dept.deptno", true, "CLUSTER");
        assertOracleHintExists("SELECT --+ CLUSTER \n --+ some other comment, not hint\n /* even more comments */ * from dual", false, "CLUSTER");
        assertOracleHintExists("(SELECT * from t1) UNION (select /*+ CLUSTER */ * from dual)", true, null, "CLUSTER");
        assertOracleHintExists("(SELECT * from t1) UNION (select /*+ CLUSTER */ * from dual) UNION (select * from dual)", true, null, "CLUSTER", null);
        assertOracleHintExists("(SELECT --+ HINT1 HINT2 HINT3\n * from t1) UNION (select /*+ HINT4 HINT5 */ * from dual)", true, "HINT1 HINT2 HINT3", "HINT4 HINT5");

    }
    
    public void testOracleHintExpression() throws JSQLParserException {
        String statement = "SELECT --+ HINT\n * FROM tab1";
        Statement parsed = parserManager.parse(new StringReader(statement));

        assertEquals(statement, parsed.toString());
        PlainSelect plainSelect = (PlainSelect) ((Select) parsed).getSelectBody();
        assertExpressionCanBeDeparsedAs(plainSelect.getOracleHint(), "--+ HINT\n");
    }

    public void testTableFunctionWithNoParams() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION()";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getFunction();
        assertNotNull(function);
        assertEquals("SOME_FUNCTION", function.getName());
        assertNull(function.getParameters());
        assertNull(fromItem.getAlias());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testTableFunctionWithParams() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION(1, 'val')";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getFunction();
        assertNotNull(function);
        assertEquals("SOME_FUNCTION", function.getName());

        // verify params
        assertNotNull(function.getParameters());
        List<Expression> expressions = function.getParameters().getExpressions();
        assertEquals(2, expressions.size());

        Expression firstParam = expressions.get(0);
        assertNotNull(firstParam);
        assertTrue(firstParam instanceof LongValue);
        assertEquals(1l, ((LongValue) firstParam).getValue());

        Expression secondParam = expressions.get(1);
        assertNotNull(secondParam);
        assertTrue(secondParam instanceof StringValue);
        assertEquals("val", ((StringValue) secondParam).getValue());

        assertNull(fromItem.getAlias());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    public void testTableFunctionWithAlias() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION() AS z";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getFunction();
        assertNotNull(function);

        assertEquals("SOME_FUNCTION", function.getName());
        assertNull(function.getParameters());
        assertNotNull(fromItem.getAlias());
        assertEquals("z", fromItem.getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);
    }
}
