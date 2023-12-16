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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertExpressionCanBeDeparsedAs;
import static net.sf.jsqlparser.test.TestUtils.assertExpressionCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertStatementCanBeDeparsedAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Server;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.test.TestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
public class SelectTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testMultiPartTableNameWithServerNameAndDatabaseNameAndSchemaName()
            throws Exception {
        final String statement =
                "SELECT columnName FROM [server-name\\server-instance].databaseName.schemaName.tableName";
        assertSqlCanBeParsedAndDeparsed(statement, false,
                parser -> parser.withSquareBracketQuotation(true));
        assertDeparse(
                new PlainSelect()
                        .addSelectItem(
                                new Column().withColumnName("columnName"))
                        .withFromItem(new Table()
                                .withDatabase(new Database("databaseName")
                                        .withServer(new Server("[server-name\\server-instance]")))
                                .withSchemaName("schemaName").withName("tableName")),
                statement);
    }

    @Test
    public void testMultiPartTableNameWithServerNameAndDatabaseName() throws Exception {
        final String statement =
                "SELECT columnName FROM [server-name\\server-instance].databaseName..tableName";

        assertSqlCanBeParsedAndDeparsed(statement, false,
                parser -> parser.withSquareBracketQuotation(true));
        assertDeparse(new PlainSelect()
                .addSelectItem(new Column().withColumnName("columnName"))
                .withFromItem(new Table()
                        .withDatabase(new Database("databaseName")
                                .withServer(new Server("[server-name\\server-instance]")))
                        .withName("tableName")),
                statement);
    }

    @Test
    public void testMultiPartTableNameWithServerNameAndSchemaName() throws Exception {
        final String statement =
                "SELECT columnName FROM [server-name\\server-instance]..schemaName.tableName";
        assertSqlCanBeParsedAndDeparsed(statement, false,
                parser -> parser.withSquareBracketQuotation(true));
    }

    @Test
    public void testMultiPartTableNameWithServerProblem() throws Exception {
        final String statement = "SELECT * FROM LINK_100.htsac.dbo.t_transfer_num a";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testMultiPartTableNameWithServerName() throws Exception {
        final String statement =
                "SELECT columnName FROM [server-name\\server-instance]...tableName";
        assertSqlCanBeParsedAndDeparsed(statement, false,
                parser -> parser.withSquareBracketQuotation(true));
    }

    @Test
    public void testMultiPartTableNameWithDatabaseNameAndSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM databaseName.schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testMultiPartTableNameWithDatabaseName() throws Exception {
        final String statement = "SELECT columnName FROM databaseName..tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testMultiPartTableNameWithSchemaName() throws Exception {
        final String statement = "SELECT columnName FROM schemaName.tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testMultiPartTableNameWithColumnName() throws Exception {
        final String statement = "SELECT columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    // Select statement statement multipart
    @Test
    public void testMultiPartColumnNameWithDatabaseNameAndSchemaNameAndTableName()
            throws Exception {
        final String statement =
                "SELECT databaseName.schemaName.tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testMultiPartColumnNameWithDatabaseNameAndSchemaName() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT databaseName.schemaName..columnName FROM tableName");
    }

    @Test
    public void testMultiPartColumnNameWithDatabaseNameAndTableName() throws Exception {
        final String statement = "SELECT databaseName..tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "databaseName..tableName.columnName");
    }

    @Test
    @Disabled
    public void testMultiPartColumnNameWithDatabaseName() {
        final String statement = "SELECT databaseName...columnName FROM tableName";
        Assertions.assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                parserManager.parse(new StringReader(statement));
            }
        });
    }

    @Test
    public void testMultiPartColumnNameWithSchemaNameAndTableName() throws Exception {
        final String statement = "SELECT schemaName.tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "schemaName.tableName.columnName");
    }

    @Test
    @Disabled
    public void testMultiPartColumnNameWithSchemaName() {
        final String statement = "SELECT schemaName..columnName FROM tableName";
        Assertions.assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                parserManager.parse(new StringReader(statement));
            }
        });
    }

    @Test
    public void testMultiPartColumnNameWithTableName() throws Exception {
        final String statement = "SELECT tableName.columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "tableName.columnName");
    }

    @Test
    public void testMultiPartColumnName() throws Exception {
        final String statement = "SELECT columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        checkMultipartIdentifier(select, "columnName");
    }

    void checkMultipartIdentifier(Select select, String fullColumnName) {
        final Expression expr = (((PlainSelect) select)
                .getSelectItems().get(0)).getExpression();
        assertTrue(expr instanceof Column);
        Column col = (Column) expr;
        assertEquals("columnName", col.getColumnName());
        assertEquals(fullColumnName, col.getFullyQualifiedName());
    }

    @Test
    public void testAllColumnsFromTable() throws Exception {
        final String statement = "SELECT tableName.* FROM tableName";
        PlainSelect select = (PlainSelect) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
        assertTrue(select.getSelectItems()
                .get(0).getExpression() instanceof AllTableColumns);

        Table t = new Table("tableName");
        assertDeparse(
                new PlainSelect()
                        .addSelectItems(new AllTableColumns(t)).withFromItem(t),
                statement);
    }

    @Test
    public void testSimpleSigns() throws JSQLParserException {
        final String statement = "SELECT +1, -1 FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testSimpleAdditionsAndSubtractionsWithSigns() throws JSQLParserException {
        final String statement =
                "SELECT 1 - 1, 1 + 1, -1 - 1, -1 + 1, +1 + 1, +1 - 1 FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testOperationsWithSigns() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("1 - -1");
        assertEquals("1 - -1", expr.toString());
        assertTrue(expr instanceof Subtraction);
        Subtraction sub = (Subtraction) expr;
        assertTrue(sub.getLeftExpression() instanceof LongValue);
        assertTrue(sub.getRightExpression() instanceof SignedExpression);

        SignedExpression sexpr = sub.getRightExpression(SignedExpression.class);
        assertEquals('-', sexpr.getSign());
        assertEquals("1", sexpr.getExpression(LongValue.class).toString());
    }

    @Test
    public void testSignedColumns() throws JSQLParserException {
        final String statement =
                "SELECT -columnName, +columnName, +(columnName), -(columnName) FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testSigns() throws Exception {
        final String statement =
                "SELECT (-(1)), -(1), (-(columnName)), -(columnName), (-1), -1, (-columnName), -columnName FROM tableName";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimit() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        Expression offset = select.getLimit().getOffset();
        Expression rowCount = select.getLimit().getRowCount();

        assertEquals(3, ((LongValue) offset).getValue());
        assertTrue(rowCount instanceof JdbcParameter);
        assertFalse(select.getLimit().isLimitAll());

        // toString uses standard syntax
        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(select.getLimit());
        assertNotNull(select.getOffset());
        assertEquals("?",
                select.getOffset().getOffset().toString());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
        SetOperationList setList =
                (SetOperationList) parserManager.parse(new StringReader(statement));
        offset = setList.getLimit().getOffset();
        rowCount = setList.getLimit().getRowCount();

        assertEquals(3, ((LongValue) offset).getValue());
        assertEquals(4, ((LongValue) rowCount).getValue());

        // toString uses standard syntax
        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable3 WHERE mytable4.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

    }

    @Test
    public void testLimit2() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        Expression offset = select.getLimit().getOffset();
        Expression rowCount = select.getLimit().getRowCount();

        assertEquals(3, ((LongValue) offset).getValue());
        assertNotNull(((JdbcParameter) rowCount).getIndex());
        assertFalse(((JdbcParameter) rowCount).isUseFixedIndex());
        assertFalse(select.getLimit().isLimitAll());
        assertFalse(select.getLimit().isLimitNull());

        // toString uses standard syntax
        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT NULL OFFSET 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();

        assertNull(offset);
        assertTrue(rowCount instanceof NullValue);
        assertEquals(new LongValue(3),
                select.getOffset().getOffset());
        assertFalse(select.getLimit().isLimitAll());
        assertTrue(select.getLimit().isLimitNull());
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ALL OFFSET 5";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();

        assertNull(offset);
        assertTrue(rowCount instanceof AllValue);

        assertEquals(new LongValue(5),
                select.getOffset().getOffset());
        assertTrue(select.getLimit().isLimitAll());
        assertFalse(select.getLimit().isLimitNull());
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 0 OFFSET 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();

        assertNull(offset);
        assertEquals(0, ((LongValue) rowCount).getValue());
        assertEquals(new LongValue(3),
                select.getOffset().getOffset());
        assertFalse(select.getLimit().isLimitAll());
        assertFalse(select.getLimit().isLimitNull());
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(select.getLimit());
        assertNotNull(select.getOffset());
        assertEquals("?",
                select.getOffset().getOffset().toString());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
        select = (Select) parserManager.parse(new StringReader(statement));
        SetOperationList setList = (SetOperationList) select;
        assertEquals(3, ((LongValue) (setList.getLimit().getOffset())).getValue());
        assertEquals(4, ((LongValue) (setList.getLimit().getRowCount())).getValue());

        // toString uses standard syntax
        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) UNION ALL "
                + "(SELECT * FROM mytable3 WHERE mytable4.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testLimit3() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?1, 2";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        Expression offset = select.getLimit().getOffset();
        Expression rowCount = select.getLimit().getRowCount();

        assertEquals(1, (int) ((JdbcParameter) offset).getIndex());
        assertEquals(2, ((LongValue) rowCount).getValue());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 1, ?2";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(1, ((LongValue) offset).getValue());
        assertEquals(2, (int) ((JdbcParameter) rowCount).getIndex());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?1, ?2";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(2, (int) (((JdbcParameter) rowCount).getIndex()));
        assertEquals(1, (int) ((JdbcParameter) offset).getIndex());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 1, ?";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(1, ((LongValue) offset).getValue());
        assertNotNull(((JdbcParameter) rowCount).getIndex());
        assertFalse(((JdbcParameter) rowCount).isUseFixedIndex());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?, ?";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertNotNull(((JdbcParameter) offset).getIndex());
        assertFalse(((JdbcParameter) offset).isUseFixedIndex());
        assertNotNull(((JdbcParameter) rowCount).getIndex());
        assertFalse(((JdbcParameter) rowCount).isUseFixedIndex());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?1";
        select = (Select) parserManager.parse(new StringReader(statement));

        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();

        assertNull(offset);
        assertEquals(1, ((JdbcParameter) rowCount).getIndex().intValue());
        assertFalse(select.getLimit().isLimitAll());
    }

    @Test
    public void testLimit4() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT :some_name, 2";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        Expression offset = select.getLimit().getOffset();
        Expression rowCount = select.getLimit().getRowCount();

        assertEquals("some_name", ((JdbcNamedParameter) offset).getName());
        assertEquals(2, ((LongValue) rowCount).getValue());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 1, :some_name";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(1, ((LongValue) offset).getValue());
        assertEquals("some_name", ((JdbcNamedParameter) rowCount).getName());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT :name1, :name2";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals("name2", ((JdbcNamedParameter) rowCount).getName());
        assertEquals("name1", ((JdbcNamedParameter) offset).getName());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?1, :name1";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(1, (int) ((JdbcParameter) offset).getIndex());
        assertEquals("name1", ((JdbcNamedParameter) rowCount).getName());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT :name1, ?1";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertEquals(1, (int) ((JdbcParameter) rowCount).getIndex());
        assertEquals("name1", ((JdbcNamedParameter) offset).getName());
        assertFalse(select.getLimit().isLimitAll());

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT :param_name";
        select = (Select) parserManager.parse(new StringReader(statement));
        offset = select.getLimit().getOffset();
        rowCount = select.getLimit().getRowCount();
        assertNull(offset);
        assertEquals("param_name", ((JdbcNamedParameter) rowCount).getName());
        assertFalse(select.getLimit().isLimitAll());
    }

    @Test
    public void testLimitSqlServer1() throws JSQLParserException {
        String statement =
                "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROWS FETCH NEXT 5 ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(select.getOffset());
        assertEquals("3",
                select.getOffset().getOffset().toString());
        assertEquals("ROWS", select.getOffset().getOffsetParam());

        assertNotNull(select.getFetch());
        assertFalse(select.getFetch().isFetchParamFirst());
        assertEquals("5", select.getFetch().getExpression().toString());
        org.assertj.core.api.Assertions
                .assertThat(select.getFetch().getFetchParameters())
                .containsExactly("ROWS", "ONLY");

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimitSqlServer2() throws JSQLParserException {
        // Alternative with the other keywords
        String statement =
                "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROW FETCH FIRST 5 ROW ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(select.getOffset());
        assertEquals("ROW", select.getOffset().getOffsetParam());

        assertNotNull(select.getFetch());
        assertTrue(select.getFetch().isFetchParamFirst());
        assertEquals("5", select.getFetch().getExpression().toString());
        org.assertj.core.api.Assertions
                .assertThat(select.getFetch().getFetchParameters())
                .containsExactly("ROW", "ONLY");

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimitSqlServer3() throws JSQLParserException {
        // Query with no Fetch
        String statement =
                "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET 3 ROWS";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(select.getOffset());
        assertNull(select.getFetch());
        assertEquals("ROWS", select.getOffset().getOffsetParam());
        assertEquals(new LongValue(3),
                select.getOffset().getOffset());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimitSqlServer4() throws JSQLParserException {
        // For Oracle syntax, query with no offset
        String statement =
                "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id FETCH NEXT 5 ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNull(select.getOffset());
        assertNotNull(select.getFetch());
        assertFalse(select.getFetch().isFetchParamFirst());
        assertEquals("5", select.getFetch().getExpression().toString());
        org.assertj.core.api.Assertions
                .assertThat(select.getFetch().getFetchParameters())
                .containsExactly("ROWS", "ONLY");
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimitSqlServerJdbcParameters() throws JSQLParserException {
        String statement =
                "SELECT * FROM mytable WHERE mytable.col = 9 ORDER BY mytable.id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertNotNull(select.getOffset());
        assertEquals("ROWS", select.getOffset().getOffsetParam());
        assertNotNull(select.getFetch());
        assertFalse(select.getFetch().isFetchParamFirst());
        assertEquals("?", select.getFetch().getExpression().toString());
        org.assertj.core.api.Assertions
                .assertThat(select.getFetch().getFetchParameters())
                .containsExactly("ROWS", "ONLY");
        assertEquals("?",
                select.getOffset().getOffset().toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testLimitPR404() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ?1");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT :param_name");
    }

    @Test
    public void testLimitOffsetIssue462() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable LIMIT ?1");
    }

    @Test
    public void testLimitOffsetIssue462_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable LIMIT ?1 OFFSET ?2");
    }

    @Test
    public void testLimitOffsetKeyWordAsNamedParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable LIMIT :limit");
    }

    @Test
    public void testLimitOffsetKeyWordAsNamedParameter2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable LIMIT :limit OFFSET :offset");
    }

    @Test
    public void testTop() throws JSQLParserException {
        String statement = "SELECT TOP 3 * FROM mytable WHERE mytable.col = 9";

        PlainSelect select = (PlainSelect) parserManager.parse(new StringReader(statement));

        assertEquals(3, select.getTop()
                .getExpression(LongValue.class).getValue());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "select top 5 foo from bar";
        select = (PlainSelect) parserManager.parse(new StringReader(statement));
        assertEquals(5, select.getTop()
                .getExpression(LongValue.class).getValue());
    }

    @Test
    public void testTopWithParenthesis() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement =
                "SELECT TOP (5) PERCENT " + firstColumnName + ", " + secondColumnName
                        + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final Top top = selectBody.getTop();
        assertEquals("5", top.getExpression().toString());
        assertTrue(top.hasParenthesis());
        assertTrue(top.isPercentage());

        final List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTopWithTies() throws JSQLParserException {
        final String statement =
                "SELECT TOP (5) PERCENT WITH TIES columnName1, columnName2 FROM tableName";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final Top top = selectBody.getTop();
        assertEquals("5", top.getExpression().toString());
        assertTrue(top.hasParenthesis());
        assertTrue(top.isPercentage());
        assertTrue(top.isWithTies());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTopWithJdbcParameter() throws JSQLParserException {
        String statement = "SELECT TOP ?1 * FROM mytable WHERE mytable.col = 9";

        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1, (int) ((JdbcParameter) ((PlainSelect) select).getTop()
                .getExpression()).getIndex());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "select top :name1 foo from bar";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertEquals("name1", ((JdbcNamedParameter) ((PlainSelect) select).getTop()
                .getExpression()).getName());

        statement = "select top ? foo from bar";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertNotNull(
                ((JdbcParameter) ((PlainSelect) select).getTop().getExpression())
                        .getIndex());
        assertFalse(
                ((JdbcParameter) ((PlainSelect) select).getTop().getExpression())
                        .isUseFixedIndex());
    }

    @Test
    public void testSkip() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT SKIP 5 " + firstColumnName + ", " + secondColumnName
                + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final Skip skip = selectBody.getSkip();
        assertEquals(5, (long) skip.getRowCount());
        assertNull(skip.getJdbcParameter());
        assertNull(skip.getVariable());

        final List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);

        final String statement2 = "SELECT SKIP skipVar c1, c2 FROM t";
        final Select select2 = (Select) parserManager.parse(new StringReader(statement2));

        final PlainSelect selectBody2 = (PlainSelect) select2;

        final Skip skip2 = selectBody2.getSkip();
        assertNull(skip2.getRowCount());
        assertNull(skip2.getJdbcParameter());
        assertEquals("skipVar", skip2.getVariable());

        final List<SelectItem<?>> selectItems2 = selectBody2.getSelectItems();
        assertEquals(2, selectItems2.size());
        assertEquals("c1", selectItems2.get(0).toString());
        assertEquals("c2", selectItems2.get(1).toString());

        assertStatementCanBeDeparsedAs(select2, statement2);
    }

    @Test
    public void testFirst() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT FIRST 5 " + firstColumnName + ", " + secondColumnName
                + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final First limit = selectBody.getFirst();
        assertEquals(5, (long) limit.getRowCount());
        assertNull(limit.getJdbcParameter());
        assertEquals(First.Keyword.FIRST, limit.getKeyword());

        final List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);

        final String statement2 = "SELECT FIRST firstVar c1, c2 FROM t";
        final Select select2 = (Select) parserManager.parse(new StringReader(statement2));

        final PlainSelect selectBody2 = (PlainSelect) select2;

        final First first2 = selectBody2.getFirst();
        assertNull(first2.getRowCount());
        assertNull(first2.getJdbcParameter());
        assertEquals("firstVar", first2.getVariable());

        final List<SelectItem<?>> selectItems2 = selectBody2.getSelectItems();
        assertEquals(2, selectItems2.size());
        assertEquals("c1", selectItems2.get(0).toString());
        assertEquals("c2", selectItems2.get(1).toString());

        assertStatementCanBeDeparsedAs(select2, statement2);
    }

    @Test
    public void testFirstWithKeywordLimit() throws JSQLParserException {
        final String firstColumnName = "alias.columnName1";
        final String secondColumnName = "alias.columnName2";
        final String statement = "SELECT LIMIT ? " + firstColumnName + ", " + secondColumnName
                + " FROM schemaName.tableName alias ORDER BY " + secondColumnName + " DESC";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final First limit = selectBody.getFirst();
        assertNull(limit.getRowCount());
        assertNotNull(limit.getJdbcParameter());
        assertNotNull(limit.getJdbcParameter().getIndex());
        assertFalse(limit.getJdbcParameter().isUseFixedIndex());
        assertEquals(First.Keyword.LIMIT, limit.getKeyword());

        final List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals(firstColumnName, selectItems.get(0).toString());
        assertEquals(secondColumnName, selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testSkipFirst() throws JSQLParserException {
        final String statement = "SELECT SKIP ?1 FIRST f1 c1, c2 FROM t1";
        final Select select = (Select) parserManager.parse(new StringReader(statement));

        final PlainSelect selectBody = (PlainSelect) select;

        final Skip skip = selectBody.getSkip();
        assertNotNull(skip.getJdbcParameter());
        assertNotNull(skip.getJdbcParameter().getIndex());
        assertTrue(skip.getJdbcParameter().isUseFixedIndex());
        assertEquals(1, (int) skip.getJdbcParameter().getIndex());
        assertNull(skip.getVariable());
        final First first = selectBody.getFirst();
        assertNull(first.getJdbcParameter());
        assertNull(first.getRowCount());
        assertEquals("f1", first.getVariable());

        final List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        assertEquals(2, selectItems.size());
        assertEquals("c1", selectItems.get(0).toString());
        assertEquals("c2", selectItems.get(1).toString());

        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testSelectItems() throws JSQLParserException {
        String statement =
                "SELECT myid AS MYID, mycol, tab.*, schema.tab.*, mytab.mycol2, myschema.mytab.mycol, myschema.mytab.* FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;

        final List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        assertEquals("MYID", selectItems.get(0).getAlias().getName());
        assertEquals("mycol", ((Column) (selectItems.get(1)).getExpression())
                .getColumnName());
        assertEquals("tab",
                ((AllTableColumns) selectItems.get(2).getExpression()).getTable().getName());
        assertEquals("schema",
                ((AllTableColumns) selectItems.get(3).getExpression()).getTable().getSchemaName());
        assertEquals("schema.tab",
                ((AllTableColumns) selectItems.get(3).getExpression()).getTable()
                        .getFullyQualifiedName());
        assertEquals("mytab.mycol2",
                ((Column) (selectItems.get(4)).getExpression())
                        .getFullyQualifiedName());
        assertEquals("myschema.mytab.mycol",
                ((Column) (selectItems.get(5)).getExpression())
                        .getFullyQualifiedName());
        assertEquals("myschema.mytab",
                ((AllTableColumns) selectItems.get(6).getExpression()).getTable()
                        .getFullyQualifiedName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement =
                "SELECT myid AS MYID, (SELECT MAX(ID) AS myid2 FROM mytable2) AS myalias FROM mytable WHERE mytable.col = 9";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        assertEquals("myalias",
                (plainSelect.getSelectItems().get(1)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT (myid + myid2) AS MYID FROM mytable WHERE mytable.col = 9";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        assertEquals("MYID",
                (plainSelect.getSelectItems().get(0)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTimezoneExpression() throws JSQLParserException {
        String stmt = "SELECT creation_date AT TIME ZONE 'UTC'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTimezoneExpressionWithTwoTransformations() throws JSQLParserException {
        String stmt =
                "SELECT DATE(date1 AT TIME ZONE 'UTC' AT TIME ZONE 'australia/sydney') AS another_date";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTimezoneExpressionWithColumnBasedTimezone() throws JSQLParserException {
        String stmt =
                "SELECT 1 FROM tbl WHERE col AT TIME ZONE timezone_col < '2021-11-05 00:00:35'::date + INTERVAL '1 day' * 0";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testUnionWithOrderByAndLimitAndNoBrackets() throws JSQLParserException {
        String stmt = "SELECT id FROM table1 UNION SELECT id FROM table2 ORDER BY id ASC LIMIT 55";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testUnion() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 UNION "
                + "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION "
                + "SELECT * FROM mytable2 LIMIT 3, 4";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        SetOperationList setList = (SetOperationList) select;
        assertEquals(3, setList.getSelects().size());
        assertEquals("mytable",
                ((Table) ((PlainSelect) setList.getSelects().get(0)).getFromItem()).getName());
        assertEquals("mytable3",
                ((Table) ((PlainSelect) setList.getSelects().get(1)).getFromItem()).getName());
        assertEquals("mytable2",
                ((Table) ((PlainSelect) setList.getSelects().get(2)).getFromItem()).getName());
        assertEquals(3,
                ((LongValue) setList.getSelects().get(2).getLimit().getOffset()).getValue());


        // with fetch and with ur
        String statement2 = "SELECT * FROM mytable WHERE mytable.col = 9 UNION "
                + "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION "
                + "SELECT * FROM mytable2 ORDER BY COL DESC FETCH FIRST 1 ROWS ONLY WITH UR";

        Select select2 = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement2, true);
        SetOperationList setList2 = (SetOperationList) select2;
        assertEquals(3, setList2.getSelects().size());
        assertEquals("mytable",
                ((Table) ((PlainSelect) setList2.getSelects().get(0)).getFromItem()).getName());
        assertEquals("mytable3",
                ((Table) ((PlainSelect) setList2.getSelects().get(1)).getFromItem()).getName());
        assertEquals("mytable2",
                ((Table) ((PlainSelect) setList2.getSelects().get(2)).getFromItem()).getName());
        assertEquals(1, setList2.getFetch().getRowCount());

        assertEquals("UR", setList2.getIsolation().getIsolation());
    }

    @Test
    public void testUnion2() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 UNION "
                + "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION "
                + "SELECT * FROM mytable2 LIMIT 3 OFFSET 4";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        SetOperationList setList = (SetOperationList) select;
        assertEquals(3, setList.getSelects().size());
        assertEquals("mytable",
                ((Table) ((PlainSelect) setList.getSelects().get(0)).getFromItem()).getName());
        assertEquals("mytable3",
                ((Table) ((PlainSelect) setList.getSelects().get(1)).getFromItem()).getName());
        assertEquals("mytable2",
                ((Table) ((PlainSelect) setList.getSelects().get(2)).getFromItem()).getName());
        assertEquals(3,
                ((LongValue) setList.getSelects().get(2).getLimit().getRowCount())
                        .getValue());
        assertNull(setList.getSelects().get(2).getLimit().getOffset());
        assertEquals(new LongValue(4),
                setList.getSelects().get(2).getOffset().getOffset());

    }

    @Test
    public void testDistinct() throws JSQLParserException {
        String statement =
                "SELECT DISTINCT ON (myid) myid, mycol FROM mytable WHERE mytable.col = 9";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);

        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("myid", ((Column) (plainSelect.getDistinct()
                .getOnSelectItems().get(0)).getExpression()).getColumnName());
        assertEquals("mycol", ((Column) (plainSelect.getSelectItems().get(1))
                .getExpression()).getColumnName());
    }

    @Test
    public void testIsDistinctFrom() throws JSQLParserException {
        String stmt = "SELECT name FROM tbl WHERE name IS DISTINCT FROM foo";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testIsNotDistinctFrom() throws JSQLParserException {
        String stmt = "SELECT name FROM tbl WHERE name IS NOT DISTINCT FROM foo";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testDistinctTop() throws JSQLParserException {
        String statement = "SELECT DISTINCT TOP 5 myid, mycol FROM mytable WHERE mytable.col = 9";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("myid", ((Column) (plainSelect.getSelectItems().get(0))
                .getExpression()).getColumnName());
        assertEquals("mycol", ((Column) (plainSelect.getSelectItems().get(1))
                .getExpression()).getColumnName());
        assertNotNull(plainSelect.getTop());
    }

    @Test
    public void testDistinctTop2() {
        String statement = "SELECT TOP 5 DISTINCT myid, mycol FROM mytable WHERE mytable.col = 9";
        try {
            parserManager.parse(new StringReader(statement));
            fail("sould not work");
        } catch (JSQLParserException ex) {
            // expected to fail
        }
    }

    @Test
    public void testDistinctWithFollowingBrackets() throws JSQLParserException {
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(
                "SELECT DISTINCT (phone), name FROM admin_user");
        PlainSelect selectBody = (PlainSelect) select;
        Distinct distinct = selectBody.getDistinct();

        assertThat(distinct).isNotNull().hasFieldOrPropertyWithValue("onSelectItems", null);
        assertThat(selectBody.getSelectItems().get(0).toString()).isEqualTo("(phone)");
    }

    @Test
    public void testFrom() throws JSQLParserException {
        String statement =
                "SELECT * FROM mytable as mytable0, mytable1 alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals(3, plainSelect.getJoins().size());
        assertEquals("mytable0", plainSelect.getFromItem().getAlias().getName());
        assertEquals("alias_tab1",
                plainSelect.getJoins().get(0).getFromItem().getAlias().getName());
        assertEquals("alias_tab2",
                plainSelect.getJoins().get(1).getFromItem().getAlias().getName());
        assertEquals("mytable4", plainSelect.getJoins().get(2).getFromItem().getAlias().getName());
    }

    @Test
    public void testJoin() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("tab2",
                ((Table) plainSelect.getJoins().get(0).getFromItem()).getFullyQualifiedName());
        assertEquals("tab1.id",
                ((Column) ((EqualsTo) plainSelect.getJoins().get(0).getOnExpression())
                        .getLeftExpression()).getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isOuter());

        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        plainSelect = (PlainSelect) select;
        assertEquals(2, plainSelect.getJoins().size());
        assertEquals("tab3",
                ((Table) plainSelect.getJoins().get(1).getFromItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(1).isOuter());

        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id JOIN tab3";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        plainSelect = (PlainSelect) select;
        assertEquals(2, plainSelect.getJoins().size());
        assertEquals("tab3",
                ((Table) plainSelect.getJoins().get(1).getFromItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(1).isOuter());

        // implicit INNER
        statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
        TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);

        statement =
                "SELECT * FROM TA2 LEFT OUTER JOIN O USING (col1, col2) WHERE D.OasSD = 'asdf' AND (kj >= 4 OR l < 'sdf')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);

        statement = "SELECT * FROM tab1 INNER JOIN tab2 USING (id, id2)";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        plainSelect = (PlainSelect) select;
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("tab2",
                ((Table) plainSelect.getJoins().get(0).getFromItem()).getFullyQualifiedName());
        assertFalse(plainSelect.getJoins().get(0).isOuter());
        assertEquals(2, plainSelect.getJoins().get(0).getUsingColumns().size());
        assertEquals("id2",
                plainSelect.getJoins().get(0).getUsingColumns().get(1).getFullyQualifiedName());

        statement = "SELECT * FROM tab1 RIGHT OUTER JOIN tab2 USING (id, id2)";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement =
                "SELECT * FROM foo AS f LEFT OUTER JOIN (bar AS b RIGHT OUTER JOIN baz AS z ON f.id = z.id) ON f.id = b.id";
        TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);

        statement = "SELECT * FROM foo AS f, OUTER bar AS b WHERE f.id = b.id";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        plainSelect = (PlainSelect) select;
        assertEquals(1, plainSelect.getJoins().size());
        assertTrue(plainSelect.getJoins().get(0).isOuter());
        assertTrue(plainSelect.getJoins().get(0).isSimple());
        assertEquals("bar",
                ((Table) plainSelect.getJoins().get(0).getFromItem()).getFullyQualifiedName());
        assertEquals("b", plainSelect.getJoins().get(0).getFromItem().getAlias().getName());
    }

    @Test
    public void testFunctions() throws JSQLParserException {
        String statement = "SELECT MAX(id) AS max FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("max",
                (plainSelect.getSelectItems().get(0)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement =
                "SELECT substring(id, 2, 3), substring(id from 2 for 3), substring(id from 2), trim(BOTH ' ' from 'foo bar '), trim(LEADING ' ' from 'foo bar '), trim(TRAILING ' ' from 'foo bar '), trim(' ' from 'foo bar '), position('foo' in 'bar'), overlay('foo' placing 'bar' from 1), overlay('foo' placing 'bar' from 1 for 2) FROM my table";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement, true);

        statement =
                "SELECT MAX(id), AVG(pro) AS myavg FROM mytable WHERE mytable.col = 9 GROUP BY pro";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        assertEquals("myavg",
                (plainSelect.getSelectItems().get(1)).getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT MAX(a, b, c), COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        Function fun = (Function) (plainSelect.getSelectItems().get(0))
                .getExpression();
        assertEquals("MAX", fun.getName());
        assertEquals("b",
                ((Column) fun.getParameters().get(1)).getFullyQualifiedName());
        assertTrue(((Function) (plainSelect.getSelectItems().get(1))
                .getExpression()).getParameters().getExpressions().get(0) instanceof AllColumns);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT {fn MAX(a, b, c)}, COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        fun = (Function) (plainSelect.getSelectItems().get(0))
                .getExpression();
        assertTrue(fun.isEscaped());
        assertEquals("MAX", fun.getName());
        assertEquals("b",
                ((Column) fun.getParameters().getExpressions().get(1)).getFullyQualifiedName());
        assertTrue(((Function) (plainSelect.getSelectItems().get(1))
                .getExpression()).getParameters().getExpressions().get(0) instanceof AllColumns);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT ab.MAX(a, b, c), cd.COUNT(*), D FROM tab1 GROUP BY D";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        fun = (Function) (plainSelect.getSelectItems().get(0))
                .getExpression();
        assertEquals("ab.MAX", fun.getName());
        assertEquals("b",
                ((Column) fun.getParameters().getExpressions().get(1)).getFullyQualifiedName());
        fun = (Function) (plainSelect.getSelectItems().get(1))
                .getExpression();
        assertEquals("cd.COUNT", fun.getName());
        assertTrue(fun.getParameters().getExpressions().get(0) instanceof AllColumns);
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testEscapedFunctionsIssue647() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT {fn test(0)} AS COL");
        // assertSqlCanBeParsedAndDeparsed("SELECT {fn current_timestamp(0)} AS COL");
        assertSqlCanBeParsedAndDeparsed("SELECT {fn concat(a, b)} AS COL");
    }

    @Test
    public void testEscapedFunctionsIssue753() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT { fn test(0)} AS COL");
        assertEquals("SELECT {fn test(0)} AS COL", stmt.toString());
        assertSqlCanBeParsedAndDeparsed("SELECT fn FROM fn");
    }

    @Test
    public void testNamedParametersPR702() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT substring(id, 2, 3), substring(id from 2 for 3), substring(id from 2), trim(BOTH ' ' from 'foo bar '), trim(LEADING ' ' from 'foo bar '), trim(TRAILING ' ' from 'foo bar '), trim(' ' from 'foo bar '), position('foo' in 'bar'), overlay('foo' placing 'bar' from 1), overlay('foo' placing 'bar' from 1 for 2) FROM my table",
                true);
    }

    @Test
    public void testNamedParametersPR702_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT substring(id, 2, 3) FROM mytable");
        assertSqlCanBeParsedAndDeparsed("SELECT substring(id from 2 for 3) FROM mytable");
    }

    @Test
    public void testQuotedCastExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT col FROM test WHERE status = CASE WHEN anothercol = 5 THEN 'pending'::\"enum_test\" END");
    }

    @Test
    public void testWhere() throws JSQLParserException {

        String whereToString = "(1 + 2) * (1+2) > ?";
        assertExpressionCanBeParsedAndDeparsed(whereToString, true);

        final String statement = "SELECT * FROM tab1 WHERE";
        whereToString = "(a + b + c / d + e * f) * (a / b * (a + b)) > ?";
        assertExpressionCanBeParsedAndDeparsed(whereToString, true);

        PlainSelect plainSelect =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement + " " + whereToString,
                        true);

        assertTrue(plainSelect.getWhere() instanceof GreaterThan);
        assertTrue(((GreaterThan) plainSelect.getWhere())
                .getLeftExpression() instanceof Multiplication);

        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

        whereToString = "(7 * s + 9 / 3) NOT BETWEEN 3 AND ?";
        plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement + " " + whereToString,
                true);
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

        whereToString = "a / b NOT IN (?, 's''adf', 234.2)";
        plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement + " " + whereToString,
                true);
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

        whereToString = "NOT 0 = 0";
        plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement + " " + whereToString,
                true);
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

        whereToString = "NOT (0 = 0)";
        plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement + " " + whereToString,
                true);
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
    }

    @Test
    public void testGroupBy() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals(1, plainSelect.getGroupBy().getGroupByExpressions().size());
        assertEquals("tab1.b", ((Column) plainSelect.getGroupBy().getGroupByExpressions().get(0))
                .getFullyQualifiedName());
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY 2, 3";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        assertEquals(2, plainSelect.getGroupBy().getGroupByExpressions().size());
        assertEquals(2,
                ((LongValue) plainSelect.getGroupBy().getGroupByExpressions().get(0)).getValue());
        assertEquals(3,
                ((LongValue) plainSelect.getGroupBy().getGroupByExpressions().get(1)).getValue());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testHaving() throws JSQLParserException {
        String statement =
                "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 GROUP BY tab1.b HAVING MAX(tab1.b) > 56";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertTrue(plainSelect.getHaving() instanceof GreaterThan);
        assertStatementCanBeDeparsedAs(select, statement);

        statement =
                "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 HAVING MAX(tab1.b) IN (56, 32, 3, ?)";
        select = (Select) parserManager.parse(new StringReader(statement));
        plainSelect = (PlainSelect) select;
        assertTrue(plainSelect.getHaving() instanceof InExpression);
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testExists() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE ";
        String where = "EXISTS (SELECT * FROM tab2)";
        statement += where;
        Statement parsed = parserManager.parse(new StringReader(statement));

        assertEquals(statement, parsed.toString());

        PlainSelect plainSelect = (PlainSelect) parsed;
        assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), where);
    }

    @Test
    public void testNotExists() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM tab1 WHERE NOT EXISTS (SELECT * FROM tab2)");
    }

    @Test
    public void testNotExistsIssue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t001 t WHERE NOT EXISTS (SELECT * FROM t002 t1 WHERE t.c1 = t1.c1 AND t.c2 = t1.c2 AND ('241' IN (t1.c3 || t1.c4)))");
    }

    @Test
    public void testOrderBy() throws JSQLParserException {
        // TODO: should there be a DESC marker in the OrderByElement class?
        String statement =
                "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("tab1.a", ((Column) plainSelect.getOrderByElements().get(0).getExpression())
                .getFullyQualifiedName());
        assertEquals("b",
                ((Column) plainSelect.getOrderByElements().get(1).getExpression()).getColumnName());
        assertTrue(plainSelect.getOrderByElements().get(1).isAsc());
        assertFalse(plainSelect.getOrderByElements().get(0).isAsc());

        statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a, 2";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        plainSelect = (PlainSelect) select;
        assertEquals(2, plainSelect.getOrderByElements().size());
        assertEquals("a",
                ((Column) plainSelect.getOrderByElements().get(0).getExpression()).getColumnName());
        assertEquals(2,
                ((LongValue) plainSelect.getOrderByElements().get(1).getExpression()).getValue());
    }

    @Test
    public void testOrderByNullsFirst() throws JSQLParserException {
        String statement = "SELECT a FROM tab1 ORDER BY a NULLS FIRST";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testOrderByWithComplexExpression() throws JSQLParserException {
        String statement = "SELECT col FROM tbl tbl_alias ORDER BY tbl_alias.id = 1 DESC";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testTimestamp() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > {ts '2004-04-30 04:05:34.56'}";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("2004-04-30 04:05:34.56",
                ((TimestampValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression())
                        .getValue().toString());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTime() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a > {t '04:05:34'}";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("04:05:34",
                (((TimeValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression())
                        .getValue()).toString());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testBetweenDate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE col BETWEEN {d '2015-09-19'} AND {d '2015-09-24'}");
    }

    @Test
    public void testCase() throws JSQLParserException {
        String statement = "SELECT a, CASE b WHEN 1 THEN 2 END FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE WHEN (a > 2) THEN 3 END) AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE WHEN a > 2 THEN 3 ELSE 4 END) AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' "
                + "END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' "
                + "END) FROM tab1 " + "WHERE c = (CASE " + "WHEN d <> 3 THEN 5 " + "ELSE 10 "
                + "END)";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a, CASE a " + "WHEN 'b' THEN 'BBB' " + "WHEN 'a' THEN 'AAA' "
                + "END AS b FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END > 34";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 + 3 ELSE 4 END > 34";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement =
                "SELECT a, (CASE " + "WHEN (CASE a WHEN 1 THEN 10 ELSE 20 END) > 15 THEN 'BBB' " + // "WHEN
                                                                                                   // (SELECT
                                                                                                   // c
                                                                                                   // FROM
                                                                                                   // tab2
                                                                                                   // WHERE
                                                                                                   // d
                                                                                                   // =
                                                                                                   // 2)
                                                                                                   // =
                                                                                                   // 3
                                                                                                   // THEN
                                                                                                   // 'AAA'
                                                                                                   // "
                                                                                                   // +
                        "END) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testNestedCaseCondition() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN CASE WHEN 1 THEN 10 ELSE 20 END > 15 THEN 'BBB' END FROM tab1");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT (CASE WHEN (CASE a WHEN 1 THEN 10 ELSE 20 END) > 15 THEN 'BBB' END) FROM tab1");
    }

    @Test
    public void testIssue371SimplifiedCase() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CASE col + 4 WHEN 2 THEN 1 ELSE 0 END");
    }

    @Test
    public void testIssue371SimplifiedCase2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CASE col > 4 WHEN true THEN 1 ELSE 0 END");
    }

    @Test
    public void testIssue235SimplifiedCase3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN (CASE WHEN (CASE WHEN (1) THEN 0 END) THEN 0 END) THEN 0 END FROM a");
    }

    @Test
    public void testIssue235SimplifiedCase4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN (CASE WHEN (CASE WHEN (CASE WHEN (1) THEN 0 END) THEN 0 END) THEN 0 END) THEN 0 END FROM a");
    }

    @Test
    public void testIssue862CaseWhenConcat() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT c1, CASE c1 || c2 WHEN '091' THEN '2' ELSE '1' END AS c11 FROM T2");
    }

    @Test
    public void testExpressionsInCaseBeforeWhen() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT a FROM tbl1 LEFT JOIN tbl2 ON CASE tbl1.col1 WHEN tbl1.col1 = 1 THEN tbl1.col2 = tbl2.col2 ELSE tbl1.col3 = tbl2.col3 END");
    }

    @Test
    @Disabled
    public void testExpressionsInIntervalExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT DATE_SUB(mydate, INTERVAL DAY(anotherdate) - 1 DAY) FROM tbl");
    }

    @Test
    public void testReplaceAsFunction() throws JSQLParserException {
        String statement = "SELECT REPLACE(a, 'b', c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);

        Statement stmt = CCJSqlParserUtil.parse(statement);
        Select select = (Select) stmt;
        PlainSelect plainSelect = (PlainSelect) select;

        assertEquals(1, plainSelect.getSelectItems().size());
        Expression expression =
                (plainSelect.getSelectItems().get(0)).getExpression();
        assertTrue(expression instanceof Function);
        Function func = (Function) expression;
        assertEquals("REPLACE", func.getName());
        assertEquals(3, func.getParameters().getExpressions().size());
    }

    @Test
    public void testLike() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a LIKE 'test'";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("test",
                ((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression())
                        .getValue());

        statement = "SELECT * FROM tab1 WHERE a LIKE 'test' ESCAPE 'test2'";
        select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
        plainSelect = (PlainSelect) select;
        assertEquals("test",
                ((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression())
                        .getValue());
        assertEquals(new StringValue("test2"),
                ((LikeExpression) plainSelect.getWhere()).getEscape());
    }

    @Test
    public void testNotLike() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE a NOT LIKE 'test'";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertEquals("test",
                ((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression())
                        .getValue());
        assertTrue(((LikeExpression) plainSelect.getWhere()).isNot());
    }

    @Test
    public void testNotLikeWithNotBeforeExpression() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 WHERE NOT a LIKE 'test'";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;
        assertTrue(plainSelect.getWhere() instanceof NotExpression);
        NotExpression notExpr = (NotExpression) plainSelect.getWhere();
        assertEquals("test",
                ((StringValue) ((LikeExpression) notExpr.getExpression()).getRightExpression())
                        .getValue());
        assertFalse(((LikeExpression) notExpr.getExpression()).isNot());
    }

    @Test
    public void testNotLikeIssue775() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mybatisplus WHERE id NOT LIKE ?");
    }

    @Test
    public void testIlike() throws JSQLParserException {
        String statement = "SELECT col1 FROM table1 WHERE col1 ILIKE '%hello%'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSelectOrderHaving() throws JSQLParserException {
        String statement =
                "SELECT units, count(units) AS num FROM currency GROUP BY units HAVING count(units) > 1 ORDER BY num";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testDouble() throws JSQLParserException {
        String statement = "SELECT 1e2, * FROM mytable WHERE mytable.col = 9";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e2, ((DoubleValue) ((PlainSelect) select)
                .getSelectItems().get(0).getExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 1.e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e2, ((DoubleValue) ((BinaryExpression) ((PlainSelect) select)
                .getWhere()).getRightExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 1.2e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.2e2,
                ((DoubleValue) ((BinaryExpression) ((PlainSelect) select)
                        .getWhere()).getRightExpression()).getValue(),
                0);
        assertStatementCanBeDeparsedAs(select, statement);

        statement = "SELECT * FROM mytable WHERE mytable.col = 2e2";
        select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(2e2, ((DoubleValue) ((BinaryExpression) ((PlainSelect) select)
                .getWhere()).getRightExpression()).getValue(), 0);
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testDouble2() throws JSQLParserException {
        String statement = "SELECT 1.e22 FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1e22,
                ((DoubleValue) ((PlainSelect) select)
                        .getSelectItems().get(0).getExpression()).getValue(),
                0);
    }

    @Test
    public void testDouble3() throws JSQLParserException {
        String statement = "SELECT 1. FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.0,
                ((DoubleValue) (((PlainSelect) select)
                        .getSelectItems().get(0)).getExpression()).getValue(),
                0);
    }

    @Test
    public void testDouble4() throws JSQLParserException {
        String statement = "SELECT 1.2e22 FROM mytable";
        Select select = (Select) parserManager.parse(new StringReader(statement));

        assertEquals(1.2e22,
                ((DoubleValue) (((PlainSelect) select)
                        .getSelectItems().get(0)).getExpression()).getValue(),
                0);
    }

    @Test
    public void testWith() throws JSQLParserException {
        String statement = "WITH DINFO (DEPTNO, AVGSALARY, EMPCOUNT) AS "
                + "(SELECT OTHERS.WORKDEPT, AVG(OTHERS.SALARY), COUNT(*) FROM EMPLOYEE AS OTHERS "
                + "GROUP BY OTHERS.WORKDEPT), DINFOMAX AS (SELECT MAX(AVGSALARY) AS AVGMAX FROM DINFO) "
                + "SELECT THIS_EMP.EMPNO, THIS_EMP.SALARY, DINFO.AVGSALARY, DINFO.EMPCOUNT, DINFOMAX.AVGMAX "
                + "FROM EMPLOYEE AS THIS_EMP INNER JOIN DINFO INNER JOIN DINFOMAX "
                + "WHERE THIS_EMP.JOB = 'SALESREP' AND THIS_EMP.WORKDEPT = DINFO.DEPTNO";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testWithRecursive() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH RECURSIVE t (n) AS ((SELECT 1) UNION ALL (SELECT n + 1 FROM t WHERE n < 100)) SELECT sum(n) FROM t");
    }

    @Test
    public void testSelectAliasInQuotes() throws JSQLParserException {
        String statement = "SELECT mycolumn AS \"My Column Name\" FROM mytable";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSelectAliasWithoutAs() throws JSQLParserException {
        String statement = "SELECT mycolumn \"My Column Name\" FROM mytable";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSelectJoinWithComma() throws JSQLParserException {
        String statement =
                "SELECT cb.Genus, cb.Species FROM Coleccion_de_Briofitas AS cb, unigeoestados AS es "
                        + "WHERE es.nombre = \"Tamaulipas\" AND cb.the_geom = es.geom";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testDeparser() throws JSQLParserException {
        String statement = "SELECT a.OWNERLASTNAME, a.OWNERFIRSTNAME "
                + "FROM ANTIQUEOWNERS AS a, ANTIQUES AS b "
                + "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT count(DISTINCT f + 4) FROM a";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "SELECT count(DISTINCT f, g, h) FROM a";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCount2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(ALL col1 + col2) FROM mytable");
    }

    @Test
    public void testCount3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(UNIQUE col) FROM mytable");
    }

    @Test
    public void testMysqlQuote() throws JSQLParserException {
        String statement = "SELECT `a.OWNERLASTNAME`, `OWNERFIRSTNAME` "
                + "FROM `ANTIQUEOWNERS` AS a, ANTIQUES AS b "
                + "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testConcat() throws JSQLParserException {
        String statement = "SELECT a || b || c + 4 FROM t";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testConcatProblem2() throws JSQLParserException {
        String stmt = "SELECT MAX((((("
                + "(SPA.SOORTAANLEVERPERIODE)::VARCHAR (2) || (VARCHAR(SPA.AANLEVERPERIODEJAAR))::VARCHAR (4)"
                + ") || TO_CHAR(SPA.AANLEVERPERIODEVOLGNR, 'FM09'::VARCHAR)"
                + ") || TO_CHAR((10000 - SPA.VERSCHIJNINGSVOLGNR), 'FM0999'::VARCHAR)"
                + ") || (SPA.GESLACHT)::VARCHAR (1))) AS GESLACHT_TMP FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_1() throws JSQLParserException {
        String stmt = "SELECT TO_CHAR(SPA.AANLEVERPERIODEVOLGNR, 'FM09'::VARCHAR) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_2() throws JSQLParserException {
        String stmt =
                "SELECT MAX((SPA.SOORTAANLEVERPERIODE)::VARCHAR (2) || (VARCHAR(SPA.AANLEVERPERIODEJAAR))::VARCHAR (4)) AS GESLACHT_TMP FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_3() throws JSQLParserException {
        String stmt =
                "SELECT TO_CHAR((10000 - SPA.VERSCHIJNINGSVOLGNR), 'FM0999'::VARCHAR) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_4() throws JSQLParserException {
        String stmt = "SELECT (SPA.GESLACHT)::VARCHAR (1) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_5() throws JSQLParserException {
        String stmt = "SELECT max((a || b) || c) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_5_1() throws JSQLParserException {
        String stmt = "SELECT (a || b) || c FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_5_2() throws JSQLParserException {
        String stmt = "SELECT (a + b) + c FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testConcatProblem2_6() throws JSQLParserException {
        String stmt = "SELECT max(a || b || c) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMatches() throws JSQLParserException {
        String statement =
                "SELECT * FROM team WHERE team.search_column @@ to_tsquery('new & york & yankees')";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testGroupByExpression() throws JSQLParserException {
        String statement = "SELECT col1, col2, col1 + col2, sum(col8)" + " FROM table1 "
                + "GROUP BY col1, col2, col1 + col2";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testBitwise() throws JSQLParserException {
        String statement = "SELECT col1 & 32, col2 ^ col1, col1 | col2" + " FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSelectFunction() throws JSQLParserException {
        String statement = "SELECT 1 + 2 AS sum";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testWeirdSelect() throws JSQLParserException {
        String sql =
                "select r.reviews_id, substring(rd.reviews_text, 100) as reviews_text, r.reviews_rating, r.date_added, r.customers_name from reviews r, reviews_description rd where r.products_id = '19' and r.reviews_id = rd.reviews_id and rd.languages_id = '1' and r.reviews_status = 1 order by r.reviews_id desc limit 0, 6";
        parserManager.parse(new StringReader(sql));
    }

    @Test
    public void testCast() throws JSQLParserException {
        String stmt = "SELECT CAST(a AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
        stmt = "SELECT CAST(a AS varchar2) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastInCast() throws JSQLParserException {
        String stmt = "SELECT CAST(CAST(a AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastInCast2() throws JSQLParserException {
        String stmt = "SELECT CAST('test' + CAST(assertEqual AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem() throws JSQLParserException {
        String stmt = "SELECT CAST(col1 AS varchar (256)) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem2() throws JSQLParserException {
        String stmt = "SELECT col1::varchar FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTryCast() throws JSQLParserException {
        String stmt = "SELECT TRY_CAST(a AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
        stmt = "SELECT CAST(a AS varchar2) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTryCastInTryCast() throws JSQLParserException {
        String stmt = "SELECT TRY_CAST(TRY_CAST(a AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTryCastInTryCast2() throws JSQLParserException {
        String stmt =
                "SELECT TRY_CAST('test' + TRY_CAST(assertEqual AS numeric) AS varchar) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTryCastTypeProblem() throws JSQLParserException {
        String stmt = "SELECT TRY_CAST(col1 AS varchar (256)) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMySQLHintStraightJoin() throws JSQLParserException {
        String stmt = "SELECT col FROM tbl STRAIGHT_JOIN tbl2 ON tbl.id = tbl2.id";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testStraightJoinInSelect() throws JSQLParserException {
        String stmt = "SELECT STRAIGHT_JOIN col, col2 FROM tbl INNER JOIN tbl2 ON tbl.id = tbl2.id";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem3() throws JSQLParserException {
        String stmt = "SELECT col1::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem4() throws JSQLParserException {
        String stmt = "SELECT 5::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem5() throws JSQLParserException {
        String stmt = "SELECT 5.67::varchar (256) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem6() throws JSQLParserException {
        String stmt = "SELECT 'test'::character varying FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem7() throws JSQLParserException {
        String stmt = "SELECT CAST('test' AS character varying) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCastTypeProblem8() throws JSQLParserException {
        String stmt = "SELECT CAST('123' AS double precision) FROM tabelle1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCaseElseAddition() throws JSQLParserException {
        String stmt = "SELECT CASE WHEN 1 + 3 > 20 THEN 0 ELSE 1000 + 1 END AS d FROM dual";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testBrackets() throws JSQLParserException {
        String stmt = "SELECT table_a.name AS [Test] FROM table_a";
        assertSqlCanBeParsedAndDeparsed(stmt, false,
                parser -> parser.withSquareBracketQuotation(true));
    }

    @Test
    public void testBrackets2() throws JSQLParserException {
        String stmt = "SELECT [a] FROM t";
        assertSqlCanBeParsedAndDeparsed(stmt, false,
                parser -> parser.withSquareBracketQuotation(true));
    }

    @Test
    public void testIssue1595() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT [id] FROM [guest].[12tableName]", false,
                parser -> parser.withSquareBracketQuotation(true));
    }

    @Test
    public void testBrackets3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM \"2016\"");
    }

    @Test
    public void testProblemSqlServer_Modulo_Proz() throws Exception {
        String stmt = "SELECT 5 % 2 FROM A";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlServer_Modulo_mod() throws Exception {
        String stmt = "SELECT mod(5, 2) FROM A";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlServer_Modulo() throws Exception {
        String stmt =
                "SELECT convert(varchar(255), DATEDIFF(month, year1, abc_datum) / 12) + ' year, ' + convert(varchar(255), DATEDIFF(month, year2, abc_datum) % 12) + ' month' FROM test_table";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testIsNot() throws JSQLParserException {
        String stmt = "SELECT * FROM test WHERE a IS NOT NULL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testIsNot2() throws JSQLParserException {
        // the deparser delivers always an IS NOT NULL even for NOT an IS NULL
        String stmt = "SELECT * FROM test WHERE NOT a IS NULL";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "SELECT * FROM test WHERE NOT a IS NULL");
    }

    @Test
    public void testProblemSqlAnalytic() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (ORDER BY a) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic2() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic3() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic4EmptyOver() throws JSQLParserException {
        String stmt = "SELECT a, row_number() OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic5AggregateColumnValue() throws JSQLParserException {
        String stmt = "SELECT a, sum(b) OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic6AggregateColumnValue() throws JSQLParserException {
        String stmt = "SELECT a, sum(b + 5) OVER (ORDER BY a) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic7Count() throws JSQLParserException {
        String stmt = "SELECT count(*) OVER () AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic8Complex() throws JSQLParserException {
        String stmt =
                "SELECT ID, NAME, SALARY, SUM(SALARY) OVER () AS SUM_SAL, AVG(SALARY) OVER () AS AVG_SAL, MIN(SALARY) OVER () AS MIN_SAL, MAX(SALARY) OVER () AS MAX_SAL, COUNT(*) OVER () AS ROWS2 FROM STAFF WHERE ID < 60 ORDER BY ID";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic9CommaListPartition() throws JSQLParserException {
        String stmt =
                "SELECT a, row_number() OVER (PARTITION BY c, d ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic10Lag() throws JSQLParserException {
        String stmt = "SELECT a, lag(a, 1) OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlAnalytic11Lag() throws JSQLParserException {
        String stmt = "SELECT a, lag(a, 1, 0) OVER (PARTITION BY c ORDER BY a, b) AS n FROM table1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testAnalyticFunction12() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (PARTITION BY b ORDER BY c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction13() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER () FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction14() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (PARTITION BY b ) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction15() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (ORDER BY c) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction16() throws JSQLParserException {
        String statement = "SELECT SUM(a) OVER (ORDER BY c NULLS FIRST) FROM tab1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction17() throws JSQLParserException {
        String statement =
                "SELECT AVG(sal) OVER (PARTITION BY deptno ORDER BY sal ROWS BETWEEN 0 PRECEDING AND 0 PRECEDING) AS avg_of_current_sal FROM emp";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction18() throws JSQLParserException {
        String statement =
                "SELECT AVG(sal) OVER (PARTITION BY deptno ORDER BY sal RANGE CURRENT ROW) AS avg_of_current_sal FROM emp";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunctionProblem1() throws JSQLParserException {
        String statement =
                "SELECT last_value(s.revenue_hold) OVER (PARTITION BY s.id_d_insertion_order, s.id_d_product_ad_attr, trunc(s.date_id, 'mm') ORDER BY s.date_id) AS col FROM s";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunction19() throws JSQLParserException {
        String statement =
                "SELECT count(DISTINCT CASE WHEN client_organic_search_drop_flag = 1 THEN brand END) OVER (PARTITION BY client, category_1, category_2, category_3, category_4 ) AS client_brand_org_drop_count FROM sometable";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunctionProblem1b() throws JSQLParserException {
        String statement =
                "SELECT last_value(s.revenue_hold) OVER (PARTITION BY s.id_d_insertion_order, s.id_d_product_ad_attr, trunc(s.date_id, 'mm') ORDER BY s.date_id ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS col FROM s";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testAnalyticFunctionIssue670() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT last_value(some_column IGNORE NULLS) OVER (PARTITION BY some_other_column_1, some_other_column_2 ORDER BY some_other_column_3 ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) column_alias FROM some_table");
    }

    @Test
    public void testAnalyticFunctionFilterIssue866() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COUNT(*) FILTER (WHERE name = 'Raj') OVER (PARTITION BY name ) FROM table");
    }

    @Test
    public void testAnalyticPartitionBooleanExpressionIssue864() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COUNT(*) OVER (PARTITION BY (event = 'admit' OR event = 'family visit') ORDER BY day ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING) family_visits FROM patients");
    }

    @Test
    public void testAnalyticPartitionBooleanExpressionIssue864_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COUNT(*) OVER (PARTITION BY (event = 'admit' OR event = 'family visit') ) family_visits FROM patients");
    }

    @Test
    public void testAnalyticFunctionFilterIssue934() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT COUNT(*) FILTER (WHERE name = 'Raj') FROM table");
    }

    @Test
    public void testFunctionLeft() throws JSQLParserException {
        String statement = "SELECT left(table1.col1, 4) FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testFunctionRight() throws JSQLParserException {
        String statement = "SELECT right(table1.col1, 4) FROM table1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testOneColumnFullTextSearchMySQL() throws JSQLParserException {
        String statement =
                "SELECT MATCH (col1) AGAINST ('test' IN NATURAL LANGUAGE MODE) relevance FROM tbl";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSeveralColumnsFullTextSearchMySQL() throws JSQLParserException {
        String statement =
                "SELECT MATCH (col1,col2,col3) AGAINST ('test' IN NATURAL LANGUAGE MODE) relevance FROM tbl";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testFullTextSearchInDefaultMode() throws JSQLParserException {
        String statement =
                "SELECT col FROM tbl WHERE MATCH (col1,col2,col3) AGAINST ('test') ORDER BY col";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testIsTrue() throws JSQLParserException {
        String statement = "SELECT col FROM tbl WHERE col IS TRUE";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testIsFalse() throws JSQLParserException {
        String statement = "SELECT col FROM tbl WHERE col IS FALSE";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testIsNotTrue() throws JSQLParserException {
        String statement = "SELECT col FROM tbl WHERE col IS NOT TRUE";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testIsNotFalse() throws JSQLParserException {
        String statement = "SELECT col FROM tbl WHERE col IS NOT FALSE";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testTSQLJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a *= tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTSQLJoin2() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a =* tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a = tabelle2.b(+)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleJoin2() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) = tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @ParameterizedTest
    @ValueSource(strings = {"(+)", "( +)", "(+ )", "( + )", " (+) "})
    public void testOracleJoin2_1(String value) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a" + value + " = tabelle2.b",
                true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"(+)", "( +)", "(+ )", "( + )", " (+) "})
    public void testOracleJoin2_2(String value) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a = tabelle2.b" + value, true);
    }

    @Test
    public void testOracleJoin3() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) > tabelle2.b";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleJoin3_1() throws JSQLParserException {
        String stmt = "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a > tabelle2.b(+)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleJoin4() throws JSQLParserException {
        String stmt =
                "SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a(+) = tabelle2.b AND tabelle1.b(+) IN ('A', 'B')";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleJoinIssue318() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM TBL_A, TBL_B, TBL_C WHERE TBL_A.ID(+) = TBL_B.ID AND TBL_C.ROOM(+) = TBL_B.ROOM");
    }

    @Test
    public void testProblemSqlIntersect() throws Exception {
        String stmt = "(SELECT * FROM a) INTERSECT (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a INTERSECT SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "SELECT * FROM a INTERSECT SELECT * FROM b");
    }

    @Test
    public void testIntegerDivOperator() throws Exception {
        String stmt = "SELECT col DIV 3";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemSqlExcept() throws Exception {
        String stmt = "(SELECT * FROM a) EXCEPT (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a EXCEPT SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "SELECT * FROM a EXCEPT SELECT * FROM b");
    }

    @Test
    public void testProblemSqlMinus() throws Exception {
        String stmt = "(SELECT * FROM a) MINUS (SELECT * FROM b)";
        assertSqlCanBeParsedAndDeparsed(stmt);

        stmt = "SELECT * FROM a MINUS SELECT * FROM b";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed, "SELECT * FROM a MINUS SELECT * FROM b");
    }

    @Test
    public void testProblemSqlCombinedSets() throws Exception {
        String stmt = "(SELECT * FROM a) INTERSECT (SELECT * FROM b) UNION (SELECT * FROM c)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithStatement() throws JSQLParserException {
        String stmt =
                "WITH test AS (SELECT mslink FROM feature) SELECT * FROM feature WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testSubjoinWithJoins() throws JSQLParserException {
        String stmt = "SELECT COUNT(DISTINCT `tbl1`.`id`) FROM (`tbl1`, `tbl2`, `tbl3`)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithUnionProblem() throws JSQLParserException {
        String stmt =
                "WITH test AS ((SELECT mslink FROM tablea) UNION (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithUnionAllProblem() throws JSQLParserException {
        String stmt =
                "WITH test AS ((SELECT mslink FROM tablea) UNION ALL (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithUnionProblem3() throws JSQLParserException {
        String stmt =
                "WITH test AS ((SELECT mslink, CAST(tablea.fname AS varchar) FROM tablea INNER JOIN tableb ON tablea.mslink = tableb.mslink AND tableb.deleted = 0 WHERE tablea.fname IS NULL AND 1 = 0) UNION ALL (SELECT mslink FROM tableb)) SELECT * FROM tablea WHERE mslink IN (SELECT mslink FROM test)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithUnionProblem4() throws JSQLParserException {
        String stmt =
                "WITH hist AS ((SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, 0 AS level, CAST(gl.mslink AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 WHERE gl.parent IS NULL AND gl.mslink <> 0) UNION ALL (SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, hist.level + 1 AS level, CAST(hist.path + '.' + CAST(gl.mslink AS VARCHAR) AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 INNER JOIN hist ON gl.parent = hist.mslink WHERE gl.mslink <> 0)) SELECT mslink, space(level * 4) + txt AS txt, nr, feature, path FROM hist WHERE EXISTS (SELECT feature FROM tablec WHERE mslink = 0 AND ((feature IN (1, 2) AND hist.feature = 3) OR (feature IN (4) AND hist.feature = 2)))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testWithUnionProblem5() throws JSQLParserException {
        String stmt =
                "WITH hist AS ((SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, 0 AS level, CAST(gl.mslink AS VARCHAR) AS path, ae.feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 WHERE gl.parent IS NULL AND gl.mslink <> 0) UNION ALL (SELECT gl.mslink, ba.gl_name AS txt, ba.gl_nummer AS nr, hist.level + 1 AS level, CAST(hist.path + '.' + CAST(gl.mslink AS VARCHAR) AS VARCHAR) AS path, 5 AS feature FROM tablea AS gl INNER JOIN tableb AS ba ON gl.mslink = ba.gl_mslink INNER JOIN tablec AS ae ON gl.mslink = ae.mslink AND ae.deleted = 0 INNER JOIN hist ON gl.parent = hist.mslink WHERE gl.mslink <> 0)) SELECT * FROM hist";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testExtractFrom1() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(month FROM datecolumn) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testExtractFrom2() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(year FROM now()) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testExtractFrom3() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(year FROM (now() - 2)) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testExtractFrom4() throws JSQLParserException {
        String stmt = "SELECT EXTRACT(minutes FROM now() - '01:22:00') FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemFunction() throws JSQLParserException {
        String stmt = "SELECT test() FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
        Statement parsed = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) parsed;
        PlainSelect plainSelect = (PlainSelect) select;
        SelectItem item = plainSelect.getSelectItems().get(0);
        assertTrue(item.getExpression() instanceof Function);
        assertEquals("test", ((Function) item.getExpression()).getName());
    }

    @Test
    public void testProblemFunction2() throws JSQLParserException {
        String stmt = "SELECT sysdate FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testProblemFunction3() throws JSQLParserException {
        String stmt = "SELECT TRUNCATE(col) FROM testtable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
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

    @Test
    public void testAdditionalLettersSpanish() throws JSQLParserException {
        String stmt = "SELECT * FROM años";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiTableJoin() throws JSQLParserException {
        String stmt =
                "SELECT * FROM taba INNER JOIN tabb ON taba.a = tabb.a, tabc LEFT JOIN tabd ON tabc.c = tabd.c";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testTableCrossJoin() throws JSQLParserException {
        String stmt = "SELECT * FROM taba CROSS JOIN tabb";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testLateral1() throws JSQLParserException {
        String stmt =
                "SELECT O.ORDERID, O.CUSTNAME, OL.LINETOTAL FROM ORDERS AS O, LATERAL(SELECT SUM(NETAMT) AS LINETOTAL FROM ORDERLINES AS LINES WHERE LINES.ORDERID = O.ORDERID) AS OL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testLateralComplex1() throws IOException, JSQLParserException {
        String stmt = IOUtils.toString(
                SelectTest.class.getResourceAsStream("complex-lateral-select-request.txt"),
                StandardCharsets.UTF_8);
        Select select = (Select) parserManager.parse(new StringReader(stmt));
        assertEquals(
                "SELECT O.ORDERID, O.CUSTNAME, OL.LINETOTAL, OC.ORDCHGTOTAL, OT.TAXTOTAL FROM ORDERS O, LATERAL(SELECT SUM(NETAMT) AS LINETOTAL FROM ORDERLINES LINES WHERE LINES.ORDERID = O.ORDERID) AS OL, LATERAL(SELECT SUM(CHGAMT) AS ORDCHGTOTAL FROM ORDERCHARGES CHARGES WHERE LINES.ORDERID = O.ORDERID) AS OC, LATERAL(SELECT SUM(TAXAMT) AS TAXTOTAL FROM ORDERTAXES TAXES WHERE TAXES.ORDERID = O.ORDERID) AS OT",
                select.toString());
    }

    @Test
    public void testValues() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES (1, 2), (3, 4)) AS test";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testValues2() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES 1, 2, 3, 4) AS test";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testValues3() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES 1, 2, 3, 4) AS test(a)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testValues4() throws JSQLParserException {
        String stmt = "SELECT * FROM (VALUES (1, 2), (3, 4)) AS test(a, b)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testValues5() throws JSQLParserException {
        String stmt = "SELECT X, Y FROM (VALUES (0, 'a'), (1, 'b')) AS MY_TEMP_TABLE(X, Y)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testValues6BothVariants() throws JSQLParserException {
        String stmt =
                "SELECT I FROM (VALUES 1, 2, 3) AS MY_TEMP_TABLE(I) WHERE I IN (SELECT * FROM (VALUES 1, 2) AS TEST)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testIntervalWithColumn() throws JSQLParserException {
        String stmt =
                "SELECT DATE_ADD(start_date, INTERVAL duration MINUTE) AS end_datetime FROM appointment";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testIntervalWithFunction() throws JSQLParserException {
        String stmt =
                "SELECT DATE_ADD(start_date, INTERVAL COALESCE(duration, 21) MINUTE) AS end_datetime FROM appointment";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testInterval1() throws JSQLParserException {
        String stmt = "SELECT 5 + INTERVAL '3 days'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testInterval2() throws JSQLParserException {
        String stmt =
                "SELECT to_timestamp(to_char(now() - INTERVAL '45 MINUTE', 'YYYY-MM-DD-HH24:')) AS START_TIME FROM tab1";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select;

        assertEquals(1, plainSelect.getSelectItems().size());
        SelectItem item = (SelectItem) plainSelect.getSelectItems().get(0);
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

    @Test
    public void testInterval3() throws JSQLParserException {
        String stmt = "SELECT 5 + INTERVAL '3' day";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testInterval4() throws JSQLParserException {
        String stmt = "SELECT '2008-12-31 23:59:59' + INTERVAL 1 SECOND";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testInterval5_Issue228() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT ADDDATE(timeColumn1, INTERVAL 420 MINUTES) AS timeColumn1 FROM tbl");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT ADDDATE(timeColumn1, INTERVAL -420 MINUTES) AS timeColumn1 FROM tbl");
    }

    @Test
    public void testMultiValueIn() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (a, b, c) IN (SELECT a, b, c FROM mytable2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueIn2() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable WHERE (trim(a), trim(b)) IN (SELECT a, b FROM mytable2)";
        assertSqlCanBeParsedAndDeparsed(stmt, true);
    }

    @Test
    public void testMultiValueIn3() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable WHERE (SSN, SSM) IN (('11111111111111', '22222222222222'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueIn_withAnd() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable WHERE (SSN, SSM) IN (('11111111111111', '22222222222222')) AND 1 = 1";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueIn4() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (a, b) IN ((1, 2), (3, 4), (5, 6), (7, 8))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void selectIsolationKeywordsAsAlias() throws JSQLParserException {
        String stmt = "SELECT col FROM tbl cs";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueInBinds() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (a, b) IN ((?, ?), (?, ?))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testUnionWithBracketsAndOrderBy() throws JSQLParserException {
        String stmt =
                "(SELECT a FROM tbl ORDER BY a) UNION DISTINCT (SELECT a FROM tbl ORDER BY a)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueNotInBinds() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE (a, b) NOT IN ((?, ?), (?, ?))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testMultiValueIn_NTuples() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable WHERE (a, b, c, d, e) IN ((1, 2, 3, 4, 5), (6, 7, 8, 9, 10), (11, 12, 13, 14, 15))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivot1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) FOR b IN ('val1'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivot2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a) FOR b IN (10, 20, 30))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivot3() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable PIVOT (count(a) AS vals FOR b IN (10 AS d1, 20, 30 AS d3))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivot4() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT (count(a), sum(b) FOR b IN (10, 20, 30))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivot5() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable PIVOT (count(a) FOR (b, c) IN ((10, 'a'), (20, 'b'), (30, 'c')))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivotXml1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN ('val1'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivotXml2() throws JSQLParserException {
        String stmt =
                "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN (SELECT vals FROM myothertable))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivotXml3() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable PIVOT XML (count(a) FOR b IN (ANY))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivotXmlSubquery1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (SELECT times_purchased, state_code FROM customers t) PIVOT (count(state_code) FOR state_code IN ('NY', 'CT', 'NJ', 'FL', 'MO')) ORDER BY times_purchased");
    }

    @Test
    public void testPivotFunction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT to_char((SELECT col1 FROM (SELECT times_purchased, state_code FROM customers t) PIVOT (count(state_code) FOR state_code IN ('NY', 'CT', 'NJ', 'FL', 'MO')) ORDER BY times_purchased)) FROM DUAL");
    }

    @Test
    public void testUnPivotWithAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT simulation_id, un_piv_alias.signal, un_piv_alias.val AS value FROM"
                        + " (SELECT simulation_id,"
                        + " convert(numeric(18, 2), sum(convert(int, init_on))) DosingOnStatus_TenMinutes_sim,"
                        + " convert(numeric(18, 2), sum(CASE WHEN pump_status = 0 THEN 10 ELSE 0 END)) AS DosingOffDurationHour_Hour_sim"
                        + " FROM ft_simulation_result"
                        + " WHERE simulation_id = 210 AND data_timestamp BETWEEN convert(datetime, '2021-09-14', 120) AND convert(datetime, '2021-09-18', 120)"
                        + " GROUP BY simulation_id) sim_data" + " UNPIVOT" + " (" + "val"
                        + " FOR signal IN (DosingOnStatus_TenMinutes_sim, DosingOnDuration_Hour_sim)"
                        + ") un_piv_alias");
    }

    @Test
    public void testUnPivot() throws JSQLParserException {
        String stmt = "SELECT * FROM sale_stats" + " UNPIVOT (" + "quantity"
                + " FOR product_code IN (product_a AS 'A', product_b AS 'B', product_c AS 'C'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testUnPivotWithMultiColumn() throws JSQLParserException {
        String stmt = "SELECT * FROM sale_stats" + " UNPIVOT (" + "(quantity, rank)"
                + " FOR product_code IN ((product_a, product_1) AS 'A', (product_b, product_2) AS 'B', (product_c, product_3) AS 'C'))";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPivotWithAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (SELECT * FROM mytable LEFT JOIN mytable2 ON Factor_ID = Id) f PIVOT (max(f.value) FOR f.factoryCode IN (ZD, COD, SW, PH))");
    }

    @Test
    public void testPivotWithAlias2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (SELECT * FROM mytable LEFT JOIN mytable2 ON Factor_ID = Id) f PIVOT (max(f.value) FOR f.factoryCode IN (ZD, COD, SW, PH)) d");
    }

    @Test
    public void testPivotWithAlias3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (SELECT * FROM mytable LEFT JOIN mytable2 ON Factor_ID = Id) PIVOT (max(f.value) FOR f.factoryCode IN (ZD, COD, SW, PH)) d");
    }

    @Test
    public void testPivotWithAlias4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM ("
                + "SELECT a.Station_ID stationId, b.Factor_Code factoryCode, a.Value value"
                + " FROM T_Data_Real a" + " LEFT JOIN T_Bas_Factor b ON a.Factor_ID = b.Id" + ") f "
                + "PIVOT (max(f.value) FOR f.factoryCode IN (ZD, COD, SW, PH)) d");
    }

    @Test
    public void testRegexpLike1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE REGEXP_LIKE(first_name, '^Ste(v|ph)en$')";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testRegexpLike2() throws JSQLParserException {
        String stmt =
                "SELECT CASE WHEN REGEXP_LIKE(first_name, '^Ste(v|ph)en$') THEN 1 ELSE 2 END FROM mytable";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testRegexpMySQL() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE first_name REGEXP '^Ste(v|ph)en$'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testNotRegexpMySQLIssue887() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE first_name NOT REGEXP '^Ste(v|ph)en$'");
    }

    @Test
    public void testNotRegexpMySQLIssue887_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE NOT first_name REGEXP '^Ste(v|ph)en$'");
    }

    @Test
    public void testRegexpBinaryMySQL() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE first_name REGEXP BINARY '^Ste(v|ph)en$'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testXorCondition() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE field = value XOR other_value");
    }

    @Test
    public void testRlike() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE first_name RLIKE '^Ste(v|ph)en$'");
    }

    @Test
    public void testBooleanFunction1() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE test_func(col1)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testNamedParameter() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE b = :param";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select;
        Expression exp = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        assertTrue(exp instanceof JdbcNamedParameter);
        JdbcNamedParameter namedParameter = (JdbcNamedParameter) exp;
        assertEquals("param", namedParameter.getName());

    }

    @Test
    public void testNamedParameter2() throws JSQLParserException {
        String stmt = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";
        assertSqlCanBeParsedAndDeparsed(stmt);

        Statement st = CCJSqlParserUtil.parse(stmt);
        Select select = (Select) st;
        PlainSelect plainSelect = (PlainSelect) select;

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

    @Test
    public void testNamedParameter3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE c = :from");
    }

    @Test
    public void testComplexUnion1() throws JSQLParserException {
        String stmt =
                "(SELECT 'abc-' || coalesce(mytab.a::varchar, '') AS a, mytab.b, mytab.c AS st, mytab.d, mytab.e FROM mytab WHERE mytab.del = 0) UNION (SELECT 'cde-' || coalesce(mytab2.a::varchar, '') AS a, mytab2.b, mytab2.bezeichnung AS c, 0 AS d, 0 AS e FROM mytab2 WHERE mytab2.del = 0)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleHierarchicalQuery() throws JSQLParserException {
        String stmt =
                "SELECT last_name, employee_id, manager_id FROM employees CONNECT BY employee_id = manager_id ORDER BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleHierarchicalQuery2() throws JSQLParserException {
        String stmt =
                "SELECT employee_id, last_name, manager_id FROM employees CONNECT BY PRIOR employee_id = manager_id";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleHierarchicalQuery3() throws JSQLParserException {
        String stmt =
                "SELECT last_name, employee_id, manager_id, LEVEL FROM employees START WITH employee_id = 100 CONNECT BY PRIOR employee_id = manager_id ORDER SIBLINGS BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleHierarchicalQuery4() throws JSQLParserException {
        String stmt =
                "SELECT last_name, employee_id, manager_id, LEVEL FROM employees CONNECT BY PRIOR employee_id = manager_id START WITH employee_id = 100 ORDER SIBLINGS BY last_name";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testOracleHierarchicalQueryIssue196() throws JSQLParserException {
        String stmt =
                "SELECT num1, num2, level FROM carol_tmp START WITH num2 = 1008 CONNECT BY num2 = PRIOR num1 ORDER BY level DESC";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPostgreSQLRegExpCaseSensitiveMatch() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a ~ '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPostgreSQLRegExpCaseSensitiveMatch2() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a ~* '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPostgreSQLRegExpCaseSensitiveMatch3() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a !~ '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testPostgreSQLRegExpCaseSensitiveMatch4() throws JSQLParserException {
        String stmt = "SELECT a, b FROM foo WHERE a !~* '[help].*'";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testReservedKeyword() throws JSQLParserException {
        final String statement =
                "SELECT cast, do, extract, first, following, last, materialized, nulls, partition, range, row, rows, siblings, value, xml FROM tableName"; // all
                                                                                                                                                           // of
                                                                                                                                                           // these
                                                                                                                                                           // are
                                                                                                                                                           // legal
                                                                                                                                                           // in
                                                                                                                                                           // SQL
                                                                                                                                                           // server;
                                                                                                                                                           // 'row'
                                                                                                                                                           // and
                                                                                                                                                           // 'rows'
                                                                                                                                                           // are
                                                                                                                                                           // not
                                                                                                                                                           // legal
                                                                                                                                                           // on
                                                                                                                                                           // Oracle,
                                                                                                                                                           // though;
        final Select select = (Select) parserManager.parse(new StringReader(statement));
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testReservedKeyword2() throws JSQLParserException {
        final String stmt = "SELECT open FROM tableName";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testReservedKeyword3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable1 t JOIN mytable2 AS prior ON t.id = prior.id");
    }

    @Test
    public void testCharacterSetClause() throws JSQLParserException {
        String stmt =
                "SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testNotEqualsTo() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a != b");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a <> b");
    }

    @Test
    public void testGeometryDistance() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo ORDER BY a <-> b");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo ORDER BY a <#> b");
    }

    @Test
    public void testJsonExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT data->'images'->'thumbnail'->'url' AS thumb FROM instagram");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM sales WHERE sale->'items'->>'description' = 'milk'");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM sales WHERE sale->'items'->>'quantity' = 12::TEXT");
        // assertSqlCanBeParsedAndDeparsed("SELECT * FROM sales WHERE
        // CAST(sale->'items'->>'quantity' AS integer) = 2");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT SUM(CAST(sale->'items'->>'quantity' AS integer)) AS total_quantity_sold FROM sales");
        assertSqlCanBeParsedAndDeparsed("SELECT sale->>'items' FROM sales");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT json_typeof(sale->'items'), json_typeof(sale->'items'->'quantity') FROM sales");

        // The following staments can be parsed but not deparsed
        for (String statement : new String[] {
                "SELECT doc->'site_name' FROM websites WHERE doc @> '{\"tags\":[{\"term\":\"paris\"}, {\"term\":\"food\"}]}'",
                "SELECT * FROM sales where sale ->'items' @> '[{\"count\":0}]'",
                "SELECT * FROM sales where sale ->'items' ? 'name'",
                "SELECT * FROM sales where sale ->'items' -# 'name'"}) {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            assertStatementCanBeDeparsedAs(select, statement, true);
        }
    }

    @Test
    public void testJsonExpressionWithCastExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT id FROM tbl WHERE p.company::json->'info'->>'country' = 'test'");
    }

    @Test
    public void testJsonExpressionWithIntegerParameterIssue909() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select uc.\"id\", u.nickname, u.avatar, b.title, uc.images, uc.created_at as createdAt from library.ugc_comment uc INNER JOIN library.book b on (uc.books_id ->> 0)::INTEGER = b.\"id\" INNER JOIN library.users u ON uc.user_id = u.user_id where uc.id = 1",
                true);
    }

    @Test
    public void testSqlNoCache() throws JSQLParserException {
        String stmt = "SELECT SQL_NO_CACHE sales.date FROM sales";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testSqlCache() throws JSQLParserException {
        String stmt = "SELECT SQL_CACHE sales.date FROM sales";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testSelectInto1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * INTO user_copy FROM user");
    }

    @Test
    public void testSelectForUpdate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM user_table FOR UPDATE");
    }

    @Test
    public void testSelectForUpdate2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM emp WHERE empno = ? FOR UPDATE");
    }

    @Test
    public void testSelectJoin() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT pg_class.relname, pg_attribute.attname, pg_constraint.conname "
                        + "FROM pg_constraint JOIN pg_class ON pg_class.oid = pg_constraint.conrelid"
                        + " JOIN pg_attribute ON pg_attribute.attrelid = pg_constraint.conrelid"
                        + " WHERE pg_constraint.contype = 'u' AND (pg_attribute.attnum = ANY(pg_constraint.conkey))"
                        + " ORDER BY pg_constraint.conname");
    }

    @Test
    public void testSelectJoin2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM pg_constraint WHERE pg_attribute.attnum = ANY(pg_constraint.conkey)");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM pg_constraint WHERE pg_attribute.attnum = ALL(pg_constraint.conkey)");
    }

    @Test
    public void testAnyConditionSubSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT e1.empno, e1.sal FROM emp e1 WHERE e1.sal > ANY (SELECT e2.sal FROM emp e2 WHERE e2.deptno = 10)",
                true);
    }

    @Test
    public void testAllConditionSubSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT e1.empno, e1.sal FROM emp e1 WHERE e1.sal > ALL (SELECT e2.sal FROM emp e2 WHERE e2.deptno = 10)",
                true);
    }

    @Test
    public void testSelectOracleColl() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM the_table tt WHERE TT.COL1 = lines(idx).COL1");
    }

    @Test
    public void testSelectInnerWith() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (WITH actor AS (SELECT 'a' aid FROM DUAL) SELECT aid FROM actor)");
    }

    // @Test
    // public void testSelectInnerWithAndUnionIssue1084() throws JSQLParserException {
    // assertSqlCanBeParsedAndDeparsed("WITH actor AS (SELECT 'b' aid FROM DUAL) SELECT aid FROM
    // actor UNION WITH actor2 AS (SELECT 'a' aid FROM DUAL) SELECT aid FROM actor2");
    // }
    @Test
    public void testSelectInnerWithAndUnionIssue1084_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH actor AS (SELECT 'b' aid FROM DUAL) SELECT aid FROM actor UNION SELECT aid FROM actor2");
    }

    @Test
    public void testSelectWithinGroup() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT LISTAGG(col1, '##') WITHIN GROUP (ORDER BY col1) FROM table1");
    }

    @Test
    public void testSelectUserVariable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @col FROM t1");
    }

    @Test
    public void testSelectNumericBind() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM b WHERE c = :1");
    }

    @Test
    public void testSelectBrackets() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT avg((123.250)::numeric)");
    }

    @Test
    public void testSelectBrackets2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (EXTRACT(epoch FROM age(d1, d2)) / 2)::numeric");
    }

    @Test
    public void testSelectBrackets3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT avg((EXTRACT(epoch FROM age(d1, d2)) / 2)::numeric)");
    }

    @Test
    public void testSelectBrackets4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (1 / 2)::numeric");
    }

    @Test
    public void testSelectForUpdateOfTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT foo.*, bar.* FROM foo, bar WHERE foo.id = bar.foo_id FOR UPDATE OF foo");
    }

    @Test
    public void testSelectWithBrackets() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("(SELECT 1 FROM mytable)");
    }

    @Test
    public void testSelectWithBrackets2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("(SELECT 1)");
    }

    @Test
    public void testSelectWithoutFrom() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT footable.foocolumn");
    }

    @Test
    public void testSelectKeywordPercent() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT percent FROM MY_TABLE");
    }

    @Test
    public void testSelectJPQLPositionalParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT email FROM users WHERE (type LIKE 'B') AND (username LIKE ?1)");
    }

    @Test
    public void testSelectKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT col1, min(col2) KEEP (DENSE_RANK FIRST ORDER BY col3), col4 FROM table1 GROUP BY col5 ORDER BY col3");
    }

    @Test
    public void testSelectKeepOver() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT MIN(salary) KEEP (DENSE_RANK FIRST ORDER BY commission_pct) OVER (PARTITION BY department_id ) \"Worst\" FROM employees ORDER BY department_id, salary");
    }

    @Test
    public void testGroupConcat() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT student_name, GROUP_CONCAT(DISTINCT test_score ORDER BY test_score DESC SEPARATOR ' ') FROM student GROUP BY student_name");
    }

    @Test
    public void testRowConstructor1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1 WHERE (col1, col2) = (SELECT col3, col4 FROM t2 WHERE id = 10)");
    }

    @Test
    public void testRowConstructor2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1 WHERE ROW(col1, col2) = (SELECT col3, col4 FROM t2 WHERE id = 10)");
    }

    @Test
    public void testIssue154() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT d.id, d.uuid, d.name, d.amount, d.percentage, d.modified_time FROM discount d LEFT OUTER JOIN discount_category dc ON d.id = dc.discount_id WHERE merchant_id = ? AND deleted = ? AND dc.discount_id IS NULL AND modified_time < ? AND modified_time >= ? ORDER BY modified_time");
    }

    @Test
    public void testIssue154_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT r.id, r.uuid, r.name, r.system_role FROM role r WHERE r.merchant_id = ? AND r.deleted_time IS NULL ORDER BY r.id DESC");
    }

    @Test
    public void testIssue160_signedParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT start_date WHERE start_date > DATEADD(HH, -?, GETDATE())");
    }

    @Test
    public void testIssue160_signedParameter2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE -? = 5");
    }

    @Test
    public void testIssue162_doubleUserVar() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT @@SPID AS ID, SYSTEM_USER AS \"Login Name\", USER AS \"User Name\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 'a'", "SELECT ''''", "SELECT '\\''", "SELECT 'ab''ab'",
            "SELECT 'ab\\'ab'"})
    public void testIssue167_singleQuoteEscape(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withBackslashEscapeCharacter(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT '\\'\\''", "SELECT '\\\\\\''"})
    public void testIssue167_singleQuoteEscape2(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withBackslashEscapeCharacter(true));
    }

    @Test
    public void testIssue77_singleQuoteEscape2() throws JSQLParserException {
        String sqlStr = "SELECT 'test\\'' FROM dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withBackslashEscapeCharacter(true));
    }

    @Test
    public void testIssue223_singleQuoteEscape() throws JSQLParserException {
        String sqlStr = "SELECT '\\'test\\''";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withBackslashEscapeCharacter(true));
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        assertOracleHintExists("SELECT /*+ SOMEHINT */ * FROM mytable", true, "SOMEHINT");
        assertOracleHintExists("SELECT /*+ MORE HINTS POSSIBLE */ * FROM mytable", true,
                "MORE HINTS POSSIBLE");
        assertOracleHintExists("SELECT /*+   MORE\nHINTS\t\nPOSSIBLE  */ * FROM mytable", true,
                "MORE\nHINTS\t\nPOSSIBLE");
        assertOracleHintExists(
                "SELECT /*+ leading(sn di md sh ot) cardinality(ot 1000) */ c, b FROM mytable",
                true, "leading(sn di md sh ot) cardinality(ot 1000)");
        assertOracleHintExists("SELECT /*+ ORDERED INDEX (b, jl_br_balances_n1) USE_NL (j b) \n"
                + "           USE_NL (glcc glf) USE_MERGE (gp gsb) */\n" + " b.application_id\n"
                + "FROM  jl_br_journals j,\n" + "      po_vendors p", true,
                "ORDERED INDEX (b, jl_br_balances_n1) USE_NL (j b) \n"
                        + "           USE_NL (glcc glf) USE_MERGE (gp gsb)");
        assertOracleHintExists(
                "SELECT /*+ROWID(emp)*/ /*+ THIS IS NOT HINT! ***/ * \n" + "FROM emp \n"
                        + "WHERE rowid > 'AAAAtkAABAAAFNTAAA' AND empno = 155",
                false, "ROWID(emp)");
        assertOracleHintExists(
                "SELECT /*+ INDEX(patients sex_index) use sex_index because there are few\n"
                        + "   male patients  */ name, height, weight\n" + "FROM patients\n"
                        + "WHERE sex = 'm'",
                true,
                "INDEX(patients sex_index) use sex_index because there are few\n   male patients");
        assertOracleHintExists(
                "SELECT /*+INDEX_COMBINE(emp sal_bmi hiredate_bmi)*/ * \n" + "FROM emp  \n"
                        + "WHERE sal < 50000 AND hiredate < '01-JAN-1990'",
                true, "INDEX_COMBINE(emp sal_bmi hiredate_bmi)");
        assertOracleHintExists("SELECT --+ CLUSTER \n" + "emp.ename, deptno\n" + "FROM emp, dept\n"
                + "WHERE deptno = 10 \n" + "AND emp.deptno = dept.deptno", true, "CLUSTER");
        assertOracleHintExists(
                "SELECT --+ CLUSTER \n --+ some other comment, not hint\n /* even more comments */ * from dual",
                false, "CLUSTER");
        assertOracleHintExists("(SELECT * from t1) UNION (select /*+ CLUSTER */ * from dual)", true,
                null, "CLUSTER");
        assertOracleHintExists(
                "(SELECT * from t1) UNION (select /*+ CLUSTER */ * from dual) UNION (select * from dual)",
                true, null, "CLUSTER", null);
        assertOracleHintExists(
                "(SELECT --+ HINT1 HINT2 HINT3\n * from t1) UNION (select /*+ HINT4 HINT5 */ * from dual)",
                true, "HINT1 HINT2 HINT3", "HINT4 HINT5");

    }

    @Test
    public void testOracleHintExpression() throws JSQLParserException {
        String statement = "SELECT --+ HINT\n * FROM tab1";
        Statement parsed = parserManager.parse(new StringReader(statement));

        assertEquals(statement, parsed.toString());
        PlainSelect plainSelect = (PlainSelect) parsed;
        assertExpressionCanBeDeparsedAs(plainSelect.getOracleHint(), "--+ HINT\n");
    }

    @Test
    public void testTableFunctionWithNoParams() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION()";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getFunction();
        assertNotNull(function);
        assertEquals("SOME_FUNCTION", function.getName());
        assertNull(function.getParameters());
        assertNull(fromItem.getAlias());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTableFunctionWithParams() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION(1, 'val')";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getExpression();
        assertNotNull(function);
        assertEquals("SOME_FUNCTION", function.getName());

        // verify params
        assertNotNull(function.getParameters());
        ExpressionList<?> expressions = function.getParameters();
        assertEquals(2, expressions.size());

        Expression firstParam = expressions.get(0);
        assertNotNull(firstParam);
        assertTrue(firstParam instanceof LongValue);
        assertEquals(1L, ((LongValue) firstParam).getValue());

        Expression secondParam = expressions.get(1);
        assertNotNull(secondParam);
        assertTrue(secondParam instanceof StringValue);
        assertEquals("val", ((StringValue) secondParam).getValue());

        assertNull(fromItem.getAlias());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testTableFunctionWithAlias() throws Exception {
        final String statement = "SELECT f2 FROM SOME_FUNCTION() AS z";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect plainSelect = (PlainSelect) select;

        assertTrue(plainSelect.getFromItem() instanceof TableFunction);
        TableFunction fromItem = (TableFunction) plainSelect.getFromItem();
        Function function = fromItem.getExpression();
        assertNotNull(function);

        assertEquals("SOME_FUNCTION", function.getName());
        assertNull(function.getParameters());
        assertNotNull(fromItem.getAlias());
        assertEquals("z", fromItem.getAlias().getName());
        assertStatementCanBeDeparsedAs(select, statement);
    }

    @Test
    public void testIssue151_tableFunction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM tables a LEFT JOIN getdata() b ON a.id = b.id");
    }

    @Test
    public void testIssue217_keywordSeparator() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT Separator");
    }

    @Test
    public void testIssue215_possibleEndlessParsing() throws JSQLParserException {
        String sqlStr =
                "SELECT (CASE WHEN ((value LIKE '%t1%') OR (value LIKE '%t2%')) THEN 't1s' WHEN ((((((((((((((((((((((((((((value LIKE '%t3%') OR (value LIKE '%t3%')) OR (value LIKE '%t3%')) OR (value LIKE '%t4%')) OR (value LIKE '%t4%')) OR (value LIKE '%t5%')) OR (value LIKE '%t6%')) OR (value LIKE '%t6%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t8%')) OR (value LIKE '%t8%')) OR (value LIKE '%CTO%')) OR (value LIKE '%cto%')) OR (value LIKE '%Cto%')) OR (value LIKE '%t9%')) OR (value LIKE '%t9%')) OR (value LIKE '%COO%')) OR (value LIKE '%coo%')) OR (value LIKE '%Coo%')) OR (value LIKE '%t10%')) OR (value LIKE '%t10%')) OR (value LIKE '%CIO%')) OR (value LIKE '%cio%')) OR (value LIKE '%Cio%')) OR (value LIKE '%t11%')) OR (value LIKE '%t11%')) THEN 't' WHEN ((((value LIKE '%t12%') OR (value LIKE '%t12%')) OR (value LIKE '%VP%')) OR (value LIKE '%vp%')) THEN 'Vice t12s' WHEN ((((((value LIKE '% IT %') OR (value LIKE '%t13%')) OR (value LIKE '%t13%')) OR (value LIKE '% it %')) OR (value LIKE '%tech%')) OR (value LIKE '%Tech%')) THEN 'IT' WHEN ((((value LIKE '%Analyst%') OR (value LIKE '%t14%')) OR (value LIKE '%Analytic%')) OR (value LIKE '%analytic%')) THEN 'Analysts' WHEN ((value LIKE '%Manager%') OR (value LIKE '%manager%')) THEN 't15' ELSE 'Other' END) FROM tab1";
        Statement stmt2 = CCJSqlParserUtil.parse(sqlStr,
                parser -> parser.withAllowComplexParsing(false).withTimeOut(20000));
    }

    @Test
    public void testIssue215_possibleEndlessParsing2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT (CASE WHEN ((value LIKE '%t1%') OR (value LIKE '%t2%')) THEN 't1s' ELSE 'Other' END) FROM tab1");
    }

    @Test
    public void testIssue215_possibleEndlessParsing3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE ((((((((((((((((((((((((((((value LIKE '%t3%') OR (value LIKE '%t3%')) OR (value LIKE '%t3%')) OR (value LIKE '%t4%')) OR (value LIKE '%t4%')) OR (value LIKE '%t5%')) OR (value LIKE '%t6%')) OR (value LIKE '%t6%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t8%')) OR (value LIKE '%t8%')) OR (value LIKE '%CTO%')) OR (value LIKE '%cto%')) OR (value LIKE '%Cto%')) OR (value LIKE '%t9%')) OR (value LIKE '%t9%')) OR (value LIKE '%COO%')) OR (value LIKE '%coo%')) OR (value LIKE '%Coo%')) OR (value LIKE '%t10%')) OR (value LIKE '%t10%')) OR (value LIKE '%CIO%')) OR (value LIKE '%cio%')) OR (value LIKE '%Cio%')) OR (value LIKE '%t11%')) OR (value LIKE '%t11%'))");
    }

    @Test
    public void testIssue215_possibleEndlessParsing4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE ((value LIKE '%t3%') OR (value LIKE '%t3%'))");
    }

    @Test
    public void testIssue215_possibleEndlessParsing5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE ((((((value LIKE '%t3%') OR (value LIKE '%t3%')) OR (value LIKE '%t3%')) OR (value LIKE '%t4%')) OR (value LIKE '%t4%')) OR (value LIKE '%t5%'))");
    }

    @Test
    public void testIssue215_possibleEndlessParsing6() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE (((((((((((((value LIKE '%t3%') OR (value LIKE '%t3%')) OR (value LIKE '%t3%')) OR (value LIKE '%t4%')) OR (value LIKE '%t4%')) OR (value LIKE '%t5%')) OR (value LIKE '%t6%')) OR (value LIKE '%t6%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t8%')) OR (value LIKE '%t8%'))");
    }

    @Test
    public void testIssue215_possibleEndlessParsing7() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE (((((((((((((((((((((value LIKE '%t3%') OR (value LIKE '%t3%')) OR (value LIKE '%t3%')) OR (value LIKE '%t4%')) OR (value LIKE '%t4%')) OR (value LIKE '%t5%')) OR (value LIKE '%t6%')) OR (value LIKE '%t6%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t7%')) OR (value LIKE '%t8%')) OR (value LIKE '%t8%')) OR (value LIKE '%CTO%')) OR (value LIKE '%cto%')) OR (value LIKE '%Cto%')) OR (value LIKE '%t9%')) OR (value LIKE '%t9%')) OR (value LIKE '%COO%')) OR (value LIKE '%coo%')) OR (value LIKE '%Coo%'))");
    }

    @Test
    public void testIssue230_cascadeKeyword() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT t.cascade AS cas FROM t");
    }

    @Test
    public void testBooleanValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT col FROM t WHERE a");
    }

    @Test
    public void testBooleanValue2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT col FROM t WHERE 3 < 5 AND a");
    }

    @Test
    public void testNotWithoutParenthesisIssue234() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT count(*) FROM \"Persons\" WHERE NOT \"F_NAME\" = 'John'");
    }

    @Test
    public void testWhereIssue240_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(*) FROM mytable WHERE 1");
    }

    @Test
    public void testWhereIssue240_0() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(*) FROM mytable WHERE 0");
    }

    @Test
    public void testCaseKeyword() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM Case");
    }

    @Test
    public void testCastToSignedInteger() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS SIGNED INTEGER) FROM contact WHERE contact_id = 20");
    }

    @Test
    public void testCastToSigned() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS SIGNED) FROM contact WHERE contact_id = 20");
    }

    @Test
    @Disabled
    public void testWhereIssue240_notBoolean() {
        Assertions.assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parse("SELECT count(*) FROM mytable WHERE 5");
            }
        });
    }

    @Test
    public void testWhereIssue240_true() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(*) FROM mytable WHERE true");
    }

    @Test
    public void testWhereIssue240_false() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(*) FROM mytable WHERE false");
    }

    @Test
    public void testWhereIssue241KeywordEnd() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT l.end FROM lessons l");
    }

    @Test
    public void testSpeedTestIssue235() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM tbl WHERE (ROUND((((((period_diff(date_format(tbl.CD, '%Y%m'), date_format(SUBTIME(CURRENT_TIMESTAMP(), 25200), '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(), 25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),25200)) - MONTH('2012-02-01')) - 1) / 3)))) = -3)",
                true);
    }

    @Test
    public void testSpeedTestIssue235_2() throws IOException, JSQLParserException {
        String stmt =
                IOUtils.toString(SelectTest.class.getResourceAsStream("large-sql-issue-235.txt"),
                        StandardCharsets.UTF_8);
        assertSqlCanBeParsedAndDeparsed(stmt, true);
    }

    @Test
    public void testCastVarCharMaxIssue245() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST('foo' AS NVARCHAR (MAX))");
    }

    @Test
    public void testNestedFunctionCallIssue253() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT (replace_regex(replace_regex(replace_regex(get_json_string(a_column, 'value'), '\\n', ' '), '\\r', ' '), '\\\\', '\\\\\\\\')) FROM a_table WHERE b_column = 'value'");
    }

    @Test
    public void testEscapedBackslashIssue253() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT replace_regex('test', '\\\\', '\\\\\\\\')");
    }

    @Test
    public void testKeywordTableIssue261() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT column_value FROM table(VARCHAR_LIST_TYPE())");
    }

    @Test
    public void testTopExpressionIssue243() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT TOP (? + 1) * FROM MyTable");
    }

    @Test
    public void testTopExpressionIssue243_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT TOP (CAST(? AS INT)) * FROM MyTable");
    }

    @Test
    public void testFunctionIssue284() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT NVL((SELECT 1 FROM DUAL), 1) AS A FROM TEST1");
    }

    @Test
    public void testFunctionDateTimeValues() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM tab1 WHERE a > TIMESTAMP '2004-04-30 04:05:34.56'");
    }

    @Test
    public void testPR73() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT date_part('day', TIMESTAMP '2001-02-16 20:38:40')");
        assertSqlCanBeParsedAndDeparsed("SELECT EXTRACT(year FROM DATE '2001-02-16')");
    }

    @Test
    public void testUniqueInsteadOfDistinctIssue299() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT UNIQUE trunc(timez(ludate)+ 8/24) bus_dt, j.object j_name , timez(j.starttime) START_TIME , timez(j.endtime) END_TIME FROM TEST_1 j",
                true);
    }

    @Test
    public void testProblemSqlIssue265() throws IOException, JSQLParserException {
        String sqls = IOUtils.toString(
                SelectTest.class.getResourceAsStream("large-sql-with-issue-265.txt"),
                StandardCharsets.UTF_8);
        Statements stmts = CCJSqlParserUtil.parseStatements(sqls);
        assertEquals(2, stmts.getStatements().size());
    }

    @Test
    public void testProblemSqlIssue330() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COUNT(*) FROM C_Invoice WHERE IsSOTrx='Y' AND (Processed='N' OR Updated>(current_timestamp - CAST('90 days' AS interval))) AND C_Invoice.AD_Client_ID IN(0,1010016) AND C_Invoice.AD_Org_ID IN(0,1010053,1010095,1010094)",
                true);
    }

    @Test
    public void testProblemSqlIssue330_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST('90 days' AS interval)");
    }
    // won't fix due to lookahead impact on parser
    // @Test public void testKeywordOrderAsColumnnameIssue333() throws JSQLParserException {
    // assertSqlCanBeParsedAndDeparsed("SELECT choice.response_choice_id AS uuid, choice.digit AS
    // digit, choice.text_response AS textResponse, choice.voice_prompt AS voicePrompt,
    // choice.action AS action, choice.contribution AS contribution, choice.order_num AS order,
    // choice.description AS description, choice.is_join_conference AS joinConference,
    // choice.voice_prompt_language_code AS voicePromptLanguageCode,
    // choice.text_response_language_code AS textResponseLanguageCode,
    // choice.description_language_code AS descriptionLanguageCode, choice.rec_phrase AS
    // recordingPhrase FROM response_choices choice WHERE choice.presentation_id = ? ORDER BY
    // choice.order_num", true);
    // }

    @Test
    public void testProblemKeywordCommitIssue341() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT id, commit FROM table1");
    }

    @Test
    public void testProblemSqlIssue352() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @rowNO from (SELECT @rowNO from dual) r", true);
    }

    @Test
    public void testProblemIsIssue331() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT C_DocType.C_DocType_ID,NULL,COALESCE(C_DocType_Trl.Name,C_DocType.Name) AS Name,C_DocType.IsActive FROM C_DocType LEFT JOIN C_DocType_TRL ON (C_DocType.C_DocType_ID=C_DocType_Trl.C_DocType_ID AND C_DocType_Trl.AD_Language='es_AR') WHERE C_DocType.AD_Client_ID=1010016 AND C_DocType.AD_Client_ID IN (0,1010016) AND C_DocType.c_doctype_id in ( select c_doctype2.c_doctype_id from c_doctype as c_doctype2 where substring( c_doctype2.printname,6, length(c_doctype2.printname) ) = ( select letra from c_letra_comprobante as clc where clc.c_letra_comprobante_id = 1010039) ) AND ( (1010094!=0 AND C_DocType.ad_org_id = 1010094) OR 1010094=0 ) ORDER BY 3 LIMIT 2000",
                true);
    }

    @Test
    public void testProblemIssue375() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select n.nspname, c.relname, a.attname, a.atttypid, t.typname, a.attnum, a.attlen, a.atttypmod, a.attnotnull, c.relhasrules, c.relkind, c.oid, pg_get_expr(d.adbin, d.adrelid), case t.typtype when 'd' then t.typbasetype else 0 end, t.typtypmod, c.relhasoids from (((pg_catalog.pg_class c inner join pg_catalog.pg_namespace n on n.oid = c.relnamespace and c.relname = 'business' and n.nspname = 'public') inner join pg_catalog.pg_attribute a on (not a.attisdropped) and a.attnum > 0 and a.attrelid = c.oid) inner join pg_catalog.pg_type t on t.oid = a.atttypid) left outer join pg_attrdef d on a.atthasdef and d.adrelid = a.attrelid and d.adnum = a.attnum order by n.nspname, c.relname, attnum",
                true);
    }

    @Test
    public void testProblemIssue375Simplified() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select * " + "from (((pg_catalog.pg_class c "
                + "   inner join pg_catalog.pg_namespace n " + "       on n.oid = c.relnamespace "
                + "           and c.relname = 'business' and n.nspname = 'public') "
                + "   inner join pg_catalog.pg_attribute a " + "       on (not a.attisdropped) "
                + "           and a.attnum > 0 and a.attrelid = c.oid) "
                + "   inner join pg_catalog.pg_type t " + "       on t.oid = a.atttypid) "
                + "   left outer join pg_attrdef d "
                + "       on a.atthasdef and d.adrelid = a.attrelid "
                + "           and d.adnum = a.attnum " + "order by n.nspname, c.relname, attnum",
                true);
    }

    @Test
    public void testProblemIssue375Simplified2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select * from (pg_catalog.pg_class c inner join pg_catalog.pg_namespace n on n.oid = c.relnamespace and c.relname = 'business' and n.nspname = 'public') inner join pg_catalog.pg_attribute a on (not a.attisdropped) and a.attnum > 0 and a.attrelid = c.oid",
                true);
    }

    // @Test public void testProblemIssue377() throws Exception {
    // try {
    // assertSqlCanBeParsedAndDeparsed("select 'yelp'::name as pktable_cat, n2.nspname as
    // pktable_schem, c2.relname as pktable_name, a2.attname as pkcolumn_name, 'yelp'::name as
    // fktable_cat, n1.nspname as fktable_schem, c1.relname as fktable_name, a1.attname as
    // fkcolumn_name, i::int2 as key_seq, case ref.confupdtype when 'c' then 0::int2 when 'n' then
    // 2::int2 when 'd' then 4::int2 when 'r' then 1::int2 else 3::int2 end as update_rule, case
    // ref.confdeltype when 'c' then 0::int2 when 'n' then 2::int2 when 'd' then 4::int2 when 'r'
    // then 1::int2 else 3::int2 end as delete_rule, ref.conname as fk_name, cn.conname as pk_name,
    // case when ref.condeferrable then case when ref.condeferred then 5::int2 else 6::int2 end else
    // 7::int2 end as deferrablity from ((((((( (select cn.oid, conrelid, conkey, confrelid,
    // confkey, generate_series(array_lower(conkey, 1), array_upper(conkey, 1)) as i, confupdtype,
    // confdeltype, conname, condeferrable, condeferred from pg_catalog.pg_constraint cn,
    // pg_catalog.pg_class c, pg_catalog.pg_namespace n where contype = 'f' and conrelid = c.oid and
    // relname = 'business' and n.oid = c.relnamespace and n.nspname = 'public' ) ref inner join
    // pg_catalog.pg_class c1 on c1.oid = ref.conrelid) inner join pg_catalog.pg_namespace n1 on
    // n1.oid = c1.relnamespace) inner join pg_catalog.pg_attribute a1 on a1.attrelid = c1.oid and
    // a1.attnum = conkey[i]) inner join pg_catalog.pg_class c2 on c2.oid = ref.confrelid) inner
    // join pg_catalog.pg_namespace n2 on n2.oid = c2.relnamespace) inner join
    // pg_catalog.pg_attribute a2 on a2.attrelid = c2.oid and a2.attnum = confkey[i]) left outer
    // join pg_catalog.pg_constraint cn on cn.conrelid = ref.confrelid and cn.contype = 'p') order
    // by ref.oid, ref.i", true);
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // throw ex;
    // }
    // }
    @Test
    public void testProblemInNotInProblemIssue379() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT rank FROM DBObjects WHERE rank NOT IN (0, 1)");
        assertSqlCanBeParsedAndDeparsed("SELECT rank FROM DBObjects WHERE rank IN (0, 1)");
    }

    @Test
    public void testProblemLargeNumbersIssue390() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM student WHERE student_no = 20161114000000035001");
    }

    @Test
    public void testKeyWorkInsertIssue393() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT insert(\"aaaabbb\", 4, 4, \"****\")");
    }

    @Test
    public void testKeyWorkReplaceIssue393() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT replace(\"aaaabbb\", 4, 4, \"****\")");
    }

    /**
     * Validates that a SELECT with FOR UPDATE WAIT <TIMEOUT> can be parsed and deparsed
     */
    @Test
    public void testForUpdateWaitParseDeparse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable FOR UPDATE WAIT 60");
    }

    /**
     * Validates that a SELECT with FOR UPDATE WAIT <TIMEOUT> correctly sets a {@link Wait} with the
     * correct timeout value.
     */
    @Test
    public void testForUpdateWaitWithTimeout() throws JSQLParserException {
        String statement = "SELECT * FROM mytable FOR UPDATE WAIT 60";
        Select select = (Select) parserManager.parse(new StringReader(statement));
        PlainSelect ps = (PlainSelect) select;
        Wait wait = ps.getWait();
        assertNotNull(wait, "wait should not be null");

        long waitTime = wait.getTimeout();
        assertEquals(waitTime, 60L, "wait time should be 60");
    }

    @Test
    public void testForUpdateNoWait() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable FOR UPDATE NOWAIT");
    }

    @Test
    public void testSubSelectFailsIssue394() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select aa.* , t.* from accenter.all aa, (select a.* from pacioli.emc_plan a) t",
                true);
    }

    @Test
    public void testSubSelectFailsIssue394_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select * from all", true);
    }

    @Test
    public void testMysqlIndexHints() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT column FROM testtable AS t0 USE INDEX (index1)");
        assertSqlCanBeParsedAndDeparsed("SELECT column FROM testtable AS t0 IGNORE INDEX (index1)");
        assertSqlCanBeParsedAndDeparsed("SELECT column FROM testtable AS t0 FORCE INDEX (index1)");
    }

    @Test
    public void testMysqlIndexHintsWithJoins() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM table0 t0 INNER JOIN table1 t1 USE INDEX (index1)");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM table0 t0 INNER JOIN table1 t1 IGNORE INDEX (index1)");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM table0 t0 INNER JOIN table1 t1 FORCE INDEX (index1)");
    }

    @Test
    public void testMysqlMultipleIndexHints() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM testtable AS t0 USE INDEX (index1,index2)");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM testtable AS t0 IGNORE INDEX (index1,index2)");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT column FROM testtable AS t0 FORCE INDEX (index1,index2)");
    }

    @Test
    public void testSqlServerHints() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM TB_Sys_Pedido WITH (NOLOCK) WHERE ID_Pedido = :ID_Pedido");
    }

    @Test
    public void testSqlServerHintsWithIndexIssue915() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT 1 FROM tableName1 WITH (INDEX (idx1), NOLOCK)");
    }

    @Test
    public void testSqlServerHintsWithIndexIssue915_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT 1 FROM tableName1 AS t1 WITH (INDEX (idx1)) JOIN tableName2 AS t2 WITH (INDEX (idx2)) ON t1.id = t2.id");
    }

    @Test
    public void testProblemIssue435() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT if(z, 'a', 'b') AS business_type FROM mytable1");
    }

    @Test
    public void testProblemIssue437Index() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select count(id) from p_custom_data ignore index(pri) where tenant_id=28257 and entity_id=92609 and delete_flg=0 and ( (dbc_relation_2 = 52701) and (dbc_relation_2 in ( select id from a_order where tenant_id = 28257 and 1=1 ) ) ) order by id desc, id desc",
                true);
    }

    @Test
    public void testProblemIssue445() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT E.ID_NUMBER, row_number() OVER (PARTITION BY E.ID_NUMBER ORDER BY E.DEFINED_UPDATED DESC) rn FROM T_EMPLOYMENT E");
    }

    @Test
    public void testProblemIssue485Date() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM tab WHERE tab.date = :date");
    }

    @Test
    public void testGroupByProblemIssue482() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT SUM(orderTotalValue) AS value, MONTH(invoiceDate) AS month, YEAR(invoiceDate) AS year FROM invoice.Invoices WHERE projectID = 1 GROUP BY MONTH(invoiceDate), YEAR(invoiceDate) ORDER BY YEAR(invoiceDate) DESC, MONTH(invoiceDate) DESC");
    }

    @Test
    public void testIssue512() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM #tab1");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM tab#tab1");
    }

    @Test
    public void testIssue512_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM $tab1");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM #$tab#tab1");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM #$tab1#");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM $#tab1#");
    }

    @Test
    public void testIssue514() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT listagg(c1, ';') WITHIN GROUP (PARTITION BY 1 ORDER BY 1) col FROM dual");
    }

    @Test
    public void testIssue508LeftRightBitwiseShift() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT 1 << 1");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 >> 1");
    }

    @Test
    public void testIssue522() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE mr.required_quantity - mr.quantity_issued WHEN 0 THEN NULL ELSE CASE SIGN(mr.required_quantity) WHEN -1 * SIGN(mr.quantity_issued) THEN mr.required_quantity - mr.quantity_issued ELSE CASE SIGN(ABS(mr.required_quantity) - ABS(mr.quantity_issued)) WHEN -1 THEN NULL ELSE mr.required_quantity - mr.quantity_issued END END END quantity_open FROM mytable",
                true);
    }

    @Test
    public void testIssue522_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT -1 * SIGN(mr.quantity_issued) FROM mytable");
    }

    @Test
    public void testIssue522_3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE SIGN(mr.required_quantity) WHEN -1 * SIGN(mr.quantity_issued) THEN mr.required_quantity - mr.quantity_issued  ELSE 5 END quantity_open FROM mytable",
                true);
    }

    @Test
    public void testIssue522_4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE a + b WHEN -1 * 5 THEN 1 ELSE CASE b + c WHEN -1 * 6 THEN 2 ELSE 3 END END");
    }

    @Test
    public void testIssue554() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT T.INDEX AS INDEX133_ FROM myTable T");
    }

    @Test
    public void testIssue567KeywordPrimary() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT primary, secondary FROM info");
    }

    @Test
    public void testIssue572TaskReplacement() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT task_id AS \"Task Id\" FROM testtable");
    }

    @Test
    public void testIssue566LargeView() throws IOException, JSQLParserException {
        String stmt =
                IOUtils.toString(SelectTest.class.getResourceAsStream("large-sql-issue-566.txt"),
                        StandardCharsets.UTF_8);
        assertSqlCanBeParsedAndDeparsed(stmt, true);
    }

    @Test
    public void testIssue566PostgreSQLEscaped() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT E'test'");
    }

    @Test
    public void testEscaped() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT _utf8'testvalue'");
    }

    @Test
    public void testIssue563MultiSubJoin() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT c FROM ((SELECT a FROM t) JOIN (SELECT b FROM t2) ON a = B JOIN (SELECT c FROM t3) ON b = c)");
    }

    @Test
    public void testIssue563MultiSubJoin_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT c FROM ((SELECT a FROM t))");
    }

    @Test
    public void testIssue582NumericConstants() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT x'009fd'");
        assertSqlCanBeParsedAndDeparsed("SELECT X'009fd'");
    }

    @Test
    public void testIssue583CharacterLiteralAsAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN T.ISC = 1 THEN T.EXTDESC WHEN T.b = 2 THEN '2' ELSE T.C END AS 'Test' FROM T");
    }

    @Test
    public void testIssue266KeywordTop() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @top");
        assertSqlCanBeParsedAndDeparsed("SELECT @TOP");
    }

    @Test
    public void testIssue584MySQLValueListExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a, b FROM T WHERE (T.a, T.b) = (c, d)");
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM T WHERE (T.a) = (SELECT b FROM T, c, d)");
    }

    @Test
    public void testIssue588NotNull() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE col1 ISNULL");
    }

    @Test
    public void testParenthesisAroundFromItem() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (mytable)");
    }

    @Test
    public void testParenthesisAroundFromItem2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (mytable myalias)");
    }

    @Test
    public void testParenthesisAroundFromItem3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (mytable) myalias");
    }

    @Test
    public void testJoinerExpressionIssue596() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM a JOIN (b JOIN c ON b.id = c.id) ON a.id = c.id");
    }

    // @Test public void testJoinerExpressionIssue596_2() throws JSQLParserException {
    // assertSqlCanBeParsedAndDeparsed("SELECT * FROM a JOIN b JOIN c ON b.id = c.id ON a.id =
    // c.id");
    // }
    @Test
    public void testProblemSqlIssue603() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN MAX(CAST(a.jobNum AS INTEGER)) IS NULL THEN '1000' ELSE MAX(CAST(a.jobNum AS INTEGER)) + 1 END FROM user_employee a");
    }

    @Test
    public void testProblemSqlIssue603_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST(col1 AS UNSIGNED INTEGER) FROM mytable");
    }

    @Test
    public void testProblemSqlFuncParamIssue605() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT p.id, pt.name, array_to_string( array( select pc.name from product_category pc ), ',' ) AS categories FROM product p",
                true);
    }

    @Test
    public void testProblemSqlFuncParamIssue605_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT func(SELECT col1 FROM mytable)");
    }

    @Test
    public void testSqlContainIsNullFunctionShouldBeParsed() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT name, age, ISNULL(home, 'earn more money') FROM person");
    }

    @Test
    public void testNestedCast() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT acolumn::bit (64)::bigint FROM mytable");
    }

    @Test
    public void testAndOperator() throws JSQLParserException {
        String stmt = "SELECT name from customers where name = 'John' && lastname = 'Doh'";
        Statement parsed = parserManager.parse(new StringReader(stmt));
        assertStatementCanBeDeparsedAs(parsed,
                "SELECT name FROM customers WHERE name = 'John' && lastname = 'Doh'");
    }

    @Test
    public void testNamedParametersIssue612() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM b LIMIT 10 OFFSET :param");
    }

    @Test
    public void testMissingOffsetIssue620() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a, b FROM test OFFSET 0");
        assertSqlCanBeParsedAndDeparsed("SELECT a, b FROM test LIMIT 1 OFFSET 0");
    }

    @Test
    public void testMultiPartNames1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.b");
    }

    @Test
    public void testMultiPartNames2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.b.*");
    }

    @Test
    public void testMultiPartNames3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.*");
    }

    @Test
    public void testMultiPartNames4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.b.c.d.e.f.g.h");
    }

    @Test
    public void testMultiPartNames5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM a.b.c.d.e.f.g.h");
    }

    @Test
    public void testMultiPartNamesIssue163() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT mymodel.name FROM com.myproject.MyModelClass AS mymodel");
    }

    @Test
    public void testMultiPartNamesIssue608() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @@sessions.tx_read_only");
    }

    @Test
    public void testMultiPartNamesForFunctionsIssue944() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT pg_catalog.now()");
    }

    // Teradata allows SEL to be used in place of SELECT
    // Deparse to the non-contracted form
    @Test
    public void testSelContraction() throws JSQLParserException {
        final String statementSrc = "SEL name, age FROM person";
        final String statementTgt = "SELECT name, age FROM person";
        Select select = (Select) parserManager.parse(new StringReader(statementSrc));
        assertStatementCanBeDeparsedAs(select, statementTgt);
    }

    @Test
    public void testMultiPartNamesIssue643() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT id, bid, pid, devnum, pointdesc, sysid, zone, sort FROM fault ORDER BY id DESC LIMIT ?, ?");
    }

    @Test
    public void testNotNotIssue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT VALUE1, VALUE2 FROM FOO WHERE NOT BAR LIKE '*%'");
    }

    @Test
    public void testCharNotParsedIssue718() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM x WHERE a LIKE '%' + char(9) + '%'");
    }

    @Test
    public void testTrueFalseLiteral() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM tbl WHERE true OR clm1 = 3");
    }

    @Test
    public void testTopKeyWord() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT top.date AS mycol1 FROM mytable top WHERE top.myid = :myid AND top.myid2 = 123");
    }

    @Test
    public void testTopKeyWord2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT top.date");
    }

    @Test
    public void testTopKeyWord3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable top");
    }

    @Test
    public void testNotProblem1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytab WHERE NOT v IN (1, 2, 3, 4, 5, 6, 7)");
    }

    @Test
    public void testNotProblem2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytab WHERE NOT func(5)");
    }

    @Test
    public void testCaseThenCondition() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE CASE WHEN a = 'c' THEN a IN (1, 2, 3) END = 1");
    }

    @Test
    public void testCaseThenCondition2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE CASE WHEN a = 'c' THEN a IN (1, 2, 3) END");
    }

    @Test
    public void testCaseThenCondition3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN a > 0 THEN b + a ELSE 0 END p FROM mytable");
    }

    @Test
    public void testCaseThenCondition4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM col WHERE CASE WHEN a = 'c' THEN a IN (SELECT id FROM mytable) END");
    }

    @Test
    public void testCaseThenCondition5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM col WHERE CASE WHEN a = 'c' THEN a IN (SELECT id FROM mytable) ELSE b IN (SELECT id FROM mytable) END");
    }

    @Test
    public void testOptimizeForIssue348() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM EMP ORDER BY SALARY DESC OPTIMIZE FOR 20 ROWS");
    }

    @Test
    public void testFuncConditionParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT if(a < b)");
    }

    @Test
    public void testFuncConditionParameter2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT if(a < b, c)");
    }

    @Test
    public void testFuncConditionParameter3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT  cast( ( Max(  cast( Iif( Isnumeric( license_no ) = 1, license_no, 0 ) AS INT ) ) + 2 ) AS VARCHAR )\n"
                        + "FROM lcps.t_license\n"
                        + "WHERE profession_id = 60\n"
                        + "    AND license_type = 100\n"
                        + "    AND Year( issue_date ) % 2 = CASE\n"
                        + "                WHEN Year( issue_date ) % 2 = 0\n"
                        + "                    THEN 0\n"
                        + "                ELSE 1\n"
                        + "            END\n"
                        + "    AND Isnumeric( license_no ) = 1",
                true);
    }

    @Test
    public void testFuncConditionParameter4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT IIF(isnumeric(license_no) = 1, license_no, 0) FROM mytable", true);
    }

    @Test
    public void testSqlContainIsNullFunctionShouldBeParsed3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT name, age FROM person WHERE NOT ISNULL(home, 'earn more money')");
    }

    @Test
    public void testForXmlPath() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT '|' + person_name FROM person JOIN person_group ON person.person_id = person_group.person_id WHERE person_group.group_id = 1 FOR XML PATH('')",
                true);
    }

    // @Test
    // public void testForXmlPath2() throws JSQLParserException {
    // assertSqlCanBeParsedAndDeparsed("SELECT ( STUFF( (SELECT '|' + person_name FROM person JOIN
    // person_group ON person.person_id = person_group.person_id WHERE person_group.group_id = 1 FOR
    // XML PATH(''), TYPE).value('.', 'varchar(max)'),1,1,'')) AS person_name");
    // }
    @Test
    public void testChainedFunctions() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT func('').func2('') AS foo FROM some_tables");
    }

    @Test
    public void testCollateExprIssue164() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT u.name COLLATE Latin1_General_CI_AS AS User FROM users u");
    }

    // @Test
    // public void testIntervalExpression() throws JSQLParserException {
    // assertSqlCanBeParsedAndDeparsed("SELECT count(emails.id) FROM emails WHERE
    // (emails.date_entered + 30 DAYS) > CURRENT_DATE");
    // }
    @Test
    public void testNotVariant() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ! (1 + 1)");
    }

    @Test
    public void testNotVariant2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ! 1 + 1");
    }

    @Test
    public void testNotVariant3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT NOT (1 + 1)");
    }

    @Test
    public void testNotVariant4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE NOT (1 = 1)");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE ! (1 = 1)");
    }

    @Test
    public void testNotVariantIssue850() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE id = 1 AND ! (id = 1 AND id = 2)");
    }

    @Test
    public void testDateArithmentic() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CURRENT_DATE + (1 DAY) FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + 1 DAY AS NEXT_DATE FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + 1 DAY NEXT_DATE FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE - 1 DAY + 1 YEAR - 1 MONTH FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN CURRENT_DATE BETWEEN (CURRENT_DATE - 1 DAY) AND ('2019-01-01') THEN 1 ELSE 0 END FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic6() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + HOURS_OFFSET HOUR AS NEXT_DATE FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic7() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + MINUTE_OFFSET MINUTE AS NEXT_DATE FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testDateArithmentic8() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + SECONDS_OFFSET SECOND AS NEXT_DATE FROM SYSIBM.SYSDUMMY1");
    }

    @Test
    public void testNotProblemIssue721() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM dual WHERE NOT regexp_like('a', '[\\w]+')");
    }

    @Test
    @Disabled
    public void testIssue699() throws JSQLParserException {
        String sql = "SELECT count(1) " + "FROM table_name " + "WHERE 1 = 1 " + "AN D uid = 1 "
                + "AND type IN (1, 2, 3) "
                + "AND time >= TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL 2 DAY),'00:00:00') "
                + "AND time < TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL (2 - 1) DAY),'00:00:00')";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testDateArithmentic9() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CURRENT_DATE + (RAND() * 12 MONTH) AS new_date FROM mytable");
    }

    @Test
    public void testDateArithmentic10() throws JSQLParserException {
        String sql =
                "select CURRENT_DATE + CASE WHEN CAST(RAND() * 3 AS INTEGER) = 1 THEN 100 ELSE 0 END DAY AS NEW_DATE from mytable";
        assertInstanceOf(Select.class, assertSqlCanBeParsedAndDeparsed(sql, true));

    }

    @Test
    public void testDateArithmentic11() throws JSQLParserException {
        String sql = "select CURRENT_DATE + (dayofweek(MY_DUE_DATE) + 5) DAY FROM mytable";
        assertSqlCanBeParsedAndDeparsed(sql, true);
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        final List<SelectItem> list = new ArrayList<>();
        select.accept(new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                list.addAll(plainSelect.getSelectItems());
            }
        });

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof SelectItem);
        SelectItem item = list.get(0);
        assertTrue(item.getExpression() instanceof Addition);
        Addition add = (Addition) item.getExpression();

        assertTrue(add.getRightExpression() instanceof IntervalExpression);
    }

    @Test
    public void testDateArithmentic12() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select CASE WHEN CAST(RAND() * 3 AS INTEGER) = 1 THEN NULL ELSE CURRENT_DATE + (month_offset MONTH) END FROM mytable",
                true);
    }

    @Test
    public void testDateArithmentic13() throws JSQLParserException {
        String sql = "SELECT INTERVAL 5 MONTH MONTH FROM mytable";
        assertSqlCanBeParsedAndDeparsed(sql);
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        final List<SelectItem> list = new ArrayList<>();
        select.accept(new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                list.addAll(plainSelect.getSelectItems());
            }
        });

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof SelectItem);
        SelectItem item = list.get(0);
        assertTrue(item.getExpression() instanceof IntervalExpression);
        IntervalExpression interval = (IntervalExpression) item.getExpression();
        assertEquals("INTERVAL 5 MONTH", interval.toString());
        assertEquals("MONTH", item.getAlias().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"u", "e", "n", "r", "b", "rb"})
    public void testRawStringExpressionIssue656(String prefix) throws JSQLParserException {
        String sql = "select " + prefix + "'test' from foo";
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertNotNull(statement);
        statement.accept(new StatementVisitorAdapter() {
            @Override
            public void visit(Select select) {
                select.accept(new SelectVisitorAdapter() {
                    @Override
                    public void visit(PlainSelect plainSelect) {
                        SelectItem typedExpression =
                                (SelectItem) plainSelect.getSelectItems().get(0);
                        assertNotNull(typedExpression);
                        assertNull(typedExpression.getAlias());
                        StringValue value = (StringValue) typedExpression.getExpression();
                        assertEquals(prefix.toUpperCase(), value.getPrefix());
                        assertEquals("test", value.getValue());
                    }
                });
            }
        });
    }

    @Test
    public void testGroupingSets1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COL_1, COL_2, COL_3, COL_4, COL_5, COL_6 FROM TABLE_1 "
                        + "GROUP BY "
                        + "GROUPING SETS ((COL_1, COL_2, COL_3, COL_4), (COL_5, COL_6))");
    }

    @Test
    public void testGroupingSets2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT COL_1 FROM TABLE_1 GROUP BY GROUPING SETS (COL_1)");
    }

    @Test
    public void testGroupingSets3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COL_1 FROM TABLE_1 GROUP BY GROUPING SETS (COL_1, ())");
    }

    @Test
    public void testLongQualifiedNamesIssue763() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT mongodb.test.test.intField, postgres.test.test.intField, postgres.test.test.datefield FROM mongodb.test.test JOIN postgres.postgres.test.test ON mongodb.test.test.intField = postgres.test.test.intField WHERE mongodb.test.test.intField = 123");
    }

    @Test
    public void testLongQualifiedNamesIssue763_2() throws JSQLParserException {
        Statement parse = CCJSqlParserUtil.parse(new StringReader(
                "SELECT mongodb.test.test.intField, postgres.test.test.intField, postgres.test.test.datefield FROM mongodb.test.test JOIN postgres.postgres.test.test ON mongodb.test.test.intField = postgres.test.test.intField WHERE mongodb.test.test.intField = 123"));
        System.out.println(parse.toString());
    }

    @Test
    public void testSubQueryAliasIssue754() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT C0 FROM T0 INNER JOIN T1 ON C1 = C0 INNER JOIN (SELECT W1 FROM T2) S1 ON S1.W1 = C0 ORDER BY C0");
    }

    @Test
    public void testSimilarToIssue789() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE (w_id SIMILAR TO '/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?')");
    }

    @Test
    public void testSimilarToIssue789_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE (w_id NOT SIMILAR TO '/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?')");
    }

    @Test
    public void testCaseWhenExpressionIssue262() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT X1, (CASE WHEN T.ID IS NULL THEN CASE P.WEIGHT * SUM(T.QTY) WHEN 0 THEN NULL ELSE P.WEIGHT END ELSE SUM(T.QTY) END) AS W FROM A LEFT JOIN T ON T.ID = ? RIGHT JOIN P ON P.ID = ?");
    }

    @Test
    public void testCaseWhenExpressionIssue200() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1, t2 WHERE CASE WHEN t1.id = 1 THEN t2.name = 'Marry' WHEN t1.id = 2 THEN t2.age = 10 END");
    }

    @Test
    public void testKeywordDuplicate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT mytable.duplicate FROM mytable");
    }

    @Test
    public void testKeywordDuplicate2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE duplicate = 5");
    }

    @Test
    public void testEmptyDoubleQuotes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE col = \"\"");
    }

    @Test
    public void testEmptyDoubleQuotes_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE col = \" \"");
    }

    @Test
    public void testInnerWithBlock() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select 1 from (with mytable1 as (select 2 ) select 3 from mytable1 ) first", true);
    }

    @Test
    public void testArrayIssue648() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select * from a join b on a.id = b.id[1]", true);
    }

    @Test
    public void testArrayIssue638() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT PAYLOAD[0] FROM MYTABLE");
    }

    @Test
    public void testArrayIssue489() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT name[1] FROM MYTABLE");
    }

    @Test
    public void testArrayIssue377() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select 'yelp'::name as pktable_cat, n2.nspname as pktable_schem, c2.relname as pktable_name, a2.attname as pkcolumn_name, 'yelp'::name as fktable_cat, n1.nspname as fktable_schem, c1.relname as fktable_name, a1.attname as fkcolumn_name, i::int2 as key_seq, case ref.confupdtype when 'c' then 0::int2 when 'n' then 2::int2 when 'd' then 4::int2 when 'r' then 1::int2 else 3::int2 end as update_rule, case ref.confdeltype when 'c' then 0::int2 when 'n' then 2::int2 when 'd' then 4::int2 when 'r' then 1::int2 else 3::int2 end as delete_rule, ref.conname as fk_name, cn.conname as pk_name, case when ref.condeferrable then case when ref.condeferred then 5::int2 else 6::int2 end else 7::int2 end as deferrablity from ((((((( (select cn.oid, conrelid, conkey, confrelid, confkey, generate_series(array_lower(conkey, 1), array_upper(conkey, 1)) as i, confupdtype, confdeltype, conname, condeferrable, condeferred from pg_catalog.pg_constraint cn, pg_catalog.pg_class c, pg_catalog.pg_namespace n where contype = 'f' and conrelid = c.oid and relname = 'business' and n.oid = c.relnamespace and n.nspname = 'public' ) ref inner join pg_catalog.pg_class c1 on c1.oid = ref.conrelid) inner join pg_catalog.pg_namespace n1 on n1.oid = c1.relnamespace) inner join pg_catalog.pg_attribute a1 on a1.attrelid = c1.oid and a1.attnum = conkey[i]) inner join pg_catalog.pg_class c2 on c2.oid = ref.confrelid) inner join pg_catalog.pg_namespace n2 on n2.oid = c2.relnamespace) inner join pg_catalog.pg_attribute a2 on a2.attrelid = c2.oid and a2.attnum = confkey[i]) left outer join pg_catalog.pg_constraint cn on cn.conrelid = ref.confrelid and cn.contype = 'p') order by ref.oid, ref.i",
                true);
    }

    @Test
    public void testArrayIssue378() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select ta.attname, ia.attnum, ic.relname, n.nspname, tc.relname from pg_catalog.pg_attribute ta, pg_catalog.pg_attribute ia, pg_catalog.pg_class tc, pg_catalog.pg_index i, pg_catalog.pg_namespace n, pg_catalog.pg_class ic where tc.relname = 'business' and n.nspname = 'public' and tc.oid = i.indrelid and n.oid = tc.relnamespace and i.indisprimary = 't' and ia.attrelid = i.indexrelid and ta.attrelid = i.indrelid and ta.attnum = i.indkey[ia.attnum-1] and (not ta.attisdropped) and (not ia.attisdropped) and ic.oid = i.indexrelid order by ia.attnum",
                true);
    }

    @Test
    public void testArrayRange() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (arr[1:3])[1] FROM MYTABLE");
    }

    @Test
    public void testIssue842() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.id lendId, "
                + "a.lend_code                                            lendCode, " + "a.amount, "
                + "a.remaining_principal                                  remainingPrincipal, "
                + "a.interest_rate                                        interestRate, "
                + "date_add(a.lend_time, INTERVAL a.repayment_period DAY) lendEndTime, "
                + "a.lend_time                                            lendTime "
                + "FROM risk_lend a " + "WHERE a.loan_id = 1", true);
    }

    @Test
    public void testIssue842_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL a.repayment_period DAY");
    }

    @Test
    public void testIssue848() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT IF(USER_ID > 10 AND SEX = 1, 1, 0)");
    }

    @Test
    public void testIssue848_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT IF(USER_ID > 10, 1, 0)");
    }

    @Test
    public void testIssue848_3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT c1, multiset(SELECT * FROM mytable WHERE cond = 10) FROM T1 WHERE cond2 = 20");
    }

    @Test
    public void testIssue848_4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select c1 from T1 where someFunc(select f1 from t2 where t2.id = T1.key) = 10",
                true);
    }

    @Test
    public void testMultiColumnAliasIssue849() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable AS mytab2(col1, col2)");
    }

    @Test
    public void testMultiColumnAliasIssue849_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM crosstab('select rowid, attribute, value from ct where attribute = ''att2'' or attribute = ''att3'' order by 1,2') AS ct(row_name text, category_1 text, category_2 text, category_3 text)");
    }

    @Test
    public void testTableStatementIssue1836() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "TABLE columns ORDER BY column_name LIMIT 10 OFFSET 10");
        assertSqlCanBeParsedAndDeparsed(
                "TABLE columns ORDER BY column_name LIMIT 10");
        assertSqlCanBeParsedAndDeparsed(
                "TABLE columns ORDER BY column_name");
        assertSqlCanBeParsedAndDeparsed(
                "TABLE columns LIMIT 10 OFFSET 10");
        assertSqlCanBeParsedAndDeparsed(
                "TABLE columns LIMIT 10");
    }

    @Test
    public void testLimitClauseDroppedIssue845() throws JSQLParserException {
        assertEquals("SELECT * FROM employee ORDER BY emp_id LIMIT 10 OFFSET 2", CCJSqlParserUtil
                .parse("SELECT * FROM employee ORDER BY emp_id OFFSET 2 LIMIT 10").toString());
    }

    @Test
    public void testLimitClauseDroppedIssue845_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM employee ORDER BY emp_id LIMIT 10 OFFSET 2");
    }

    @Test
    public void testChangeKeywordIssue859() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM CHANGE.TEST");
    }

    @Test
    public void testEndKeyword() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT end AS end_6 FROM mytable");
    }

    @Test
    public void testStartKeyword() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT c0_.start AS start_5 FROM mytable");
    }

    @Test
    public void testSizeKeywordIssue867() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT size FROM mytable");
    }

    @Test
    public void testPartitionByWithBracketsIssue865() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT subject_id, student_id, sum(mark) OVER (PARTITION BY subject_id, student_id ) FROM marks");
        assertSqlCanBeParsedAndDeparsed(
                "SELECT subject_id, student_id, sum(mark) OVER (PARTITION BY (subject_id, student_id) ) FROM marks");
    }

    @Test
    public void testWithAsRecursiveIssue874() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH rn AS (SELECT rownum rn FROM dual CONNECT BY level <= (SELECT max(cases) FROM t1)) SELECT pname FROM t1, rn WHERE rn <= cases ORDER BY pname");
    }

    @Test
    public void testSessionKeywordIssue876() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ID_COMPANY FROM SESSION.COMPANY");
    }

    @Test
    public void testWindowClauseWithoutOrderByIssue869() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT subject_id, student_id, mark, sum(mark) OVER (PARTITION BY (subject_id) ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) FROM marks");
    }

    @Test
    public void testKeywordSizeIssue880() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT b.pattern_size_id, b.pattern_id, b.variation, b.measure_remark, b.pake_name, b.ident_size, CONCAT( GROUP_CONCAT(a.size) ) AS 'title', CONCAT( '[', GROUP_CONCAT( '{\"patternSizeDetailId\":', a.pattern_size_detail_id, ',\"patternSizeId\":', a.pattern_size_id, ',\"size\":\"', a.size, '\",\"sizeValue\":', a.size_value SEPARATOR '},' ), '}]' ) AS 'designPatternSizeDetailJson' FROM design_pattern_size_detail a LEFT JOIN design_pattern_size b ON a.pattern_size_id = b.pattern_size_id WHERE b.pattern_id = 792679713905573986 GROUP BY b.pake_name,b.pattern_size_id",
                true);
    }

    @Test
    public void testKeywordCharacterIssue884() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT Character, Duration FROM actor");
    }

    @Test
    public void testCrossApplyIssue344() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select s.*, c.*, calc2.summary\n" + "from student s\n"
                + "join class c on s.class_id = c.id\n" + "cross apply (\n"
                + "  select s.first_name + ' ' + s.last_name + ' (' + s.sex + ')' as student_full_name\n"
                + ") calc1\n" + "cross apply (\n"
                + "  select case c.some_styling_type when 'A' then c.name + ' - ' + calc1.student_full_name\n"
                + "            when 'B' then calc1.student_full_name + ' - ' + c.name\n"
                + "            else calc1.student_full_name end as summary\n" + ") calc2", true);
    }

    @Test
    public void testOuterApplyIssue930() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable D OUTER APPLY (SELECT * FROM mytable2 E WHERE E.ColID = D.ColID) A");
    }

    @Test
    public void testWrongParseTreeIssue89() throws JSQLParserException {
        Select unionQuery = (Select) CCJSqlParserUtil
                .parse("SELECT * FROM table1 UNION SELECT * FROM table2 ORDER BY col");
        SetOperationList unionQueries = (SetOperationList) unionQuery;

        assertThat(unionQueries.getSelects()).extracting(select -> (PlainSelect) select)
                .allSatisfy(ps -> assertNull(ps.getOrderByElements()));

        assertThat(unionQueries.getOrderByElements()).isNotNull().hasSize(1)
                .extracting(item -> item.toString()).contains("col");
    }

    @Test
    public void testCaseWithComplexWhenExpression() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT av.app_id, MAX(av.version_no) AS version_no\n" + "FROM app_version av\n"
                        + "JOIN app_version_policy avp ON av.id = avp.app_version_id\n"
                        + "WHERE av.`status` = 1\n" + "AND CASE \n" + "WHEN avp.area IS NOT NULL\n"
                        + "AND length(avp.area) > 0 THEN avp.area LIKE CONCAT('%,', '12', ',%')\n"
                        + "OR avp.area LIKE CONCAT('%,', '13', ',%')\n" + "ELSE 1 = 1\n" + "END\n",
                true);
    }

    @Test
    public void testOrderKeywordIssue932() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT order FROM tmp3");
        assertSqlCanBeParsedAndDeparsed("SELECT tmp3.order FROM tmp3");
    }

    @Test
    public void testOrderKeywordIssue932_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT group FROM tmp3");
        assertSqlCanBeParsedAndDeparsed("SELECT tmp3.group FROM tmp3");
    }

    @Test
    public void testTableFunctionInExprIssue923() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE func(a) IN func(b)");
    }

    // @Test
    // public void testTableFunctionInExprIssue923_2() throws JSQLParserException, IOException {
    // String stmt = IOUtils.toString(
    // SelectTest.class.getResourceAsStream("large-sql-issue-923.txt"), "UTF-8")
    // .replace("@Prompt", "MyFunc");
    // assertSqlCanBeParsedAndDeparsed(stmt, true);
    // }
    @Test
    public void testTableFunctionInExprIssue923_3() throws JSQLParserException, IOException {
        String stmt = IOUtils.toString(
                SelectTest.class.getResourceAsStream("large-sql-issue-923-2.txt"),
                StandardCharsets.UTF_8);
        assertSqlCanBeParsedAndDeparsed(stmt, true);
    }

    @Test
    public void testTableFunctionInExprIssue923_4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_1_KINAL_CLAIM_STATUS",
                true);
    }

    @Test
    public void testTableFunctionInExprIssue923_5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END",
                true);
    }

    @Test
    public void testTableFunctionInExprIssue923_6() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE func(a) IN '1'");
    }

    @Test
    public void testKeyWordCreateIssue941() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT b.create FROM table b WHERE b.id = 1");
    }

    @Test
    public void testKeyWordCreateIssue941_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select f.select from `from` f", true);
    }

    @Test
    public void testCurrentIssue940() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT date(current) AS test_date FROM systables WHERE tabid = 1");
    }

    @Test
    public void testIssue1878() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM MY_TABLE1 FOR SHARE");
        // PostgreSQL ONLY
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM MY_TABLE1 FOR NO KEY UPDATE");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM MY_TABLE1 FOR KEY SHARE");
    }

    @Test
    public void testIssue1878ViaJava() throws JSQLParserException {
        String expectedSQLStr = "SELECT * FROM MY_TABLE1 FOR SHARE";

        // Step 1: generate the Java Object Hierarchy for
        Table table = new Table().withName("MY_TABLE1");

        PlainSelect select = new PlainSelect().addSelectItem(new AllColumns())
                .withFromItem(table).withForMode(ForMode.KEY_SHARE).withForMode(ForMode.SHARE);

        Assertions.assertEquals(expectedSQLStr, select.toString());
    }

    @Test
    public void testKeyWordView() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT ma.m_a_id, ma.anounsment, ma.max_view, ma.end_date, ma.view FROM member_anounsment as ma WHERE ( ( (ma.end_date > now() ) AND (ma.max_view >= ma.view) ) AND ( (ma.member_id='xxx') ) )",
                true);
    }

    @Test
    public void testPreserveAndOperator() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE 1 = 2 && 2 = 3";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new PlainSelect().addSelectItem(new AllColumns())
                        .withFromItem(new Table("mytable"))
                        .withWhere(new AndExpression().withUseOperator(true)
                                .withLeftExpression(
                                        new EqualsTo(new LongValue(1), new LongValue(2)))
                                .withRightExpression(
                                        new EqualsTo(new LongValue(2), new LongValue(3)))),
                statement);
    }

    @Test
    public void testPreserveAndOperator_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE (field_1 && ?)");
    }

    @Test
    public void testCheckDateFunctionIssue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT DATEDIFF(NOW(), MIN(s.startTime))");
    }

    @Test
    public void testCheckDateFunctionIssue_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT DATE_SUB(NOW(), INTERVAL :days DAY)");
    }

    @Test
    public void testCheckDateFunctionIssue_3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT DATE_SUB(NOW(), INTERVAL 1 DAY)");
    }

    @Test
    public void testCheckColonVariable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE (col1, col2) IN ((:qp0, :qp1), (:qp2, :qp3))");
    }

    @Test
    public void testVariableAssignment() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @SELECTVariable = 2");
    }

    @Test
    public void testVariableAssignment2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @var = 1");
    }

    @Test
    public void testVariableAssignment3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT @varname := @varname + 1 AS counter");
    }

    @Test
    public void testKeyWordOfIssue1029() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT of.Full_Name_c AS FullName FROM comdb.Offer_c AS of");
    }

    @Test
    public void testKeyWordExceptIssue1026() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM xxx WHERE exclude = 1");
    }

    @Test
    public void testSelectConditionsIssue720And991() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT column IS NOT NULL FROM table");
        assertSqlCanBeParsedAndDeparsed("SELECT 0 IS NULL");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 + 2");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 < 2");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 > 2");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 + 2 AS a, 3 < 4 AS b");
        assertSqlCanBeParsedAndDeparsed("SELECT 1 < 2 AS a, 0 IS NULL AS b");
        // assertSqlCanBeParsedAndDeparsed("SELECT 1 < 2 AS a, (0 IS NULL) AS b");
    }

    @Test
    public void testKeyWordExceptIssue1040() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT FORMAT(100000, 2)");
    }

    @Test
    public void testKeyWordExceptIssue1044() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT SP_ID FROM ST_PR WHERE INSTR(',' || SP_OFF || ',', ',' || ? || ',') > 0");
    }

    @Test
    public void testKeyWordExceptIssue1055() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL ? DAY");
    }

    @Test
    public void testKeyWordExceptIssue1055_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE A.end_time > now() AND A.end_time <= date_add(now(), INTERVAL ? DAY)");
    }

    @Test
    public void testIssue1062() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE temperature.timestamp <= @to AND temperature.timestamp >= @from");
    }

    @Test
    public void testIssue1062_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE temperature.timestamp <= @until AND temperature.timestamp >= @from");
    }

    @Test
    public void testIssue1068() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT t2.c AS div");
    }

    @Test
    public void selectWithSingleIn() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT 1 FROM dual WHERE a IN 1");
    }

    @Test
    public void testKeywordSequenceIssue1075() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a.sequence FROM all_procedures a");
    }

    @Test
    public void testKeywordSequenceIssue1074() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t_user WITH (NOLOCK)");
    }

    @Test
    public void testContionItemsSelectedIssue1077() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT 1 > 0");
    }

    @Test
    public void testExistsKeywordIssue1076() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT EXISTS (4)");
    }

    @Test
    public void testExistsKeywordIssue1076_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT mycol, EXISTS (SELECT mycol FROM mytable) mycol2 FROM mytable");
    }

    @Test
    public void testFormatKeywordIssue1078() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT FORMAT(date, 'yyyy-MM') AS year_month FROM mine_table");
    }

    @Test
    public void testConditionalParametersForFunctions() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT myFunc(SELECT mycol FROM mytable)");
    }

    @Test
    public void testCreateTableWithParameterDefaultFalseIssue1088() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT p.*, rhp.house_id FROM rel_house_person rhp INNER JOIN person p ON rhp.person_id = p.if WHERE rhp.house_id IN (SELECT house_id FROM rel_house_person WHERE person_id = :personId AND current_occupant = :current) AND rhp.current_occupant = :currentOccupant");
    }

    @Test
    public void testMissingLimitKeywordIssue1006() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT id, name FROM test OFFSET 20 LIMIT 10");
        assertEquals("SELECT id, name FROM test LIMIT 10 OFFSET 20", stmt.toString());
    }

    @Test
    public void testKeywordUnsignedIssue961() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COLUMN1, COLUMN2, CASE WHEN COLUMN1.DATA NOT IN ('1', '3') THEN CASE WHEN CAST(COLUMN2 AS UNSIGNED) IN ('1', '2', '3') THEN 'Q1' ELSE 'Q2' END END AS YEAR FROM TESTTABLE");
    }

    @Test
    public void testH2CaseWhenFunctionIssue1091() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CASEWHEN(ID = 1, 'A', 'B') FROM mytable");
    }

    @Test
    public void testMultiPartTypesIssue992() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST('*' AS pg_catalog.text)");
    }

    @Test
    public void testSetOperationWithParenthesisIssue1094() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM ((SELECT A FROM tbl) UNION DISTINCT (SELECT B FROM tbl2)) AS union1");
    }

    @Test
    public void testSetOperationWithParenthesisIssue1094_2() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM (((SELECT A FROM tbl)) UNION DISTINCT (SELECT B FROM tbl2)) AS union1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testSetOperationWithParenthesisIssue1094_3() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM (((SELECT A FROM tbl)) UNION DISTINCT ((SELECT B FROM tbl2))) AS union1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testSetOperationWithParenthesisIssue1094_4() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM (((((SELECT A FROM tbl)))) UNION DISTINCT (((((((SELECT B FROM tbl2)))))))) AS union1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testSignedKeywordIssue1100() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT signed, unsigned FROM mytable");
    }

    @Test
    public void testSignedKeywordIssue995() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT leading FROM prd_reprint");
    }

    @Test
    public void testSelectTuple() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT hyperloglog_distinct((1, 2)) FROM t");
    }

    @Test
    public void testArrayDeclare() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT ARRAY[1, f1], ARRAY[[1, 2], [3, f2 + 1]], ARRAY[]::text[] FROM t1");
    }

    @Test
    public void testColonDelimiterIssue1134() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM stores_demo:informix.accounts");
        assertEquals("SELECT * FROM stores_demo.informix.accounts", stmt.toString());
    }

    @Test
    public void testKeywordSkipIssue1136() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT skip");
    }

    @Test
    public void testKeywordAlgorithmIssue1137() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT algorithm FROM tablename");
    }

    @Test
    public void testKeywordAlgorithmIssue1138() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM in.tablename");
    }

    @Test
    public void testFunctionOrderBy() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT array_agg(DISTINCT s ORDER BY b)[1] FROM t");
    }

    @Test
    public void testProblematicDeparsingIssue1183() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT ARRAY_AGG(NAME ORDER BY ID) FILTER (WHERE NAME IS NOT NULL)");
    }

    @Test
    public void testProblematicDeparsingIssue1183_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ARRAY_AGG(ID ORDER BY ID) OVER (ORDER BY ID)");
    }

    @Test
    public void testKeywordCostsIssue1185() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH costs AS (SELECT * FROM MY_TABLE1 AS ALIAS_TABLE1) SELECT * FROM TESTSTMT");
    }

    @Test
    public void testFunctionWithComplexParameters_Issue1190() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT to_char(a = '3') FROM dual", true);
    }

    @Test
    public void testConditionsWithExtraBrackets_Issue1194() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT (col IS NULL) FROM tbl", true);
    }

    @Test
    public void testWithValueListWithExtraBrackets1135() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "with sample_data(day, value) as (values ((0, 13), (1, 12), (2, 15), (3, 4), (4, 8), (5, 16))) select day, value from sample_data",
                true);
    }

    @Test
    public void testWithValueListWithOutExtraBrackets1135() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("with sample_data(\"DAY\") as (values 0, 1, 2)\n"
                + "           select \"DAY\" from sample_data", true);
        assertSqlCanBeParsedAndDeparsed(
                "with sample_data(day, value) as (values (0, 13), (1, 12), (2, 15), (3, 4), (4, 8), (5, 16)) select day, value from sample_data",
                true);
    }

    @Test
    public void testWithInsideWithIssue1186() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH TESTSTMT1 AS ( WITH TESTSTMT2 AS (SELECT * FROM MY_TABLE2) SELECT col1, col2 FROM TESTSTMT2) SELECT * FROM TESTSTMT",
                true);
    }

    @Test
    public void testKeywordSynonymIssue1211() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select businessDate as \"bd\", synonym as \"synonym\" from sc.tab", true);
    }

    @Test
    public void testGroupedByIssue1176() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select id_instrument, count(*)\n" + "from cfe.instrument\n"
                + "group by (id_instrument)", true);
        assertSqlCanBeParsedAndDeparsed(
                "select count(*)\n" + "from cfe.instrument\n" + "group by ()", true);
    }

    @Test
    public void testGroupedByWithExtraBracketsIssue1210() throws JSQLParserException {
        // assertSqlCanBeParsedAndDeparsed("select a,b,c from table group by rollup(a,b,c)", true);
        assertSqlCanBeParsedAndDeparsed("select a,b,c from table group by rollup((a,b,c))", true);

    }

    @Test
    public void testGroupedByWithExtraBracketsIssue1168() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select sum(a) as amount, b, c from TEST_TABLE group by rollup ((a,b),c)", true);
    }

    @Test
    public void testSelectRowElement() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT (t.tup).id, (tup).name FROM t WHERE (t.tup).id IN (1, 2, 3)");
    }

    @Test
    public void testSelectCastProblemIssue1248() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST(t1.sign2 AS Nullable (char))");
    }

    @Test
    @Disabled
    public void testSelectCastProblemIssue1248_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CAST(t1.sign2 AS Nullable(decimal(30, 10)))");
    }

    @Test
    public void testMissingBracketsNestedInIssue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COUNT(DISTINCT CASE WHEN room IN (11167, 12074, 4484, 4483, 6314, 11168, 10336, 16445, 13176, 13177, 13178) THEN uid END) AS uidCount from tableName",
                true);
    }

    @Test
    public void testAnyComparisionExpressionValuesList1232() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select * from foo where id != ALL(VALUES 1,2,3)", true);

        assertSqlCanBeParsedAndDeparsed("select * from foo where id != ALL(?::uid[])", true);
    }

    @Test
    public void testSelectAllOperatorIssue1140() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM table t0 WHERE t0.id != all(5)");
    }

    @Test
    public void testSelectAllOperatorIssue1140_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM table t0 WHERE t0.id != all(?::uuid[])");
    }

    @Test
    public void testDB2SpecialRegisterDateTimeIssue1249() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT_TIME", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT TIME", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT_TIMESTAMP",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT TIMESTAMP",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT_DATE", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM test.abc WHERE col > CURRENT DATE", true);
    }

    @Test
    public void testKeywordFilterIssue1255() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT col1 AS filter FROM table");
    }

    @Test
    public void testConnectByRootIssue1255() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT last_name \"Employee\", CONNECT_BY_ROOT last_name \"Manager\",\n"
                        + "   LEVEL-1 \"Pathlen\", SYS_CONNECT_BY_PATH(last_name, '/') \"Path\"\n"
                        + "   FROM employees\n" + "   WHERE LEVEL > 1 and department_id = 110\n"
                        + "   CONNECT BY PRIOR employee_id = manager_id",
                true);

        assertSqlCanBeParsedAndDeparsed("SELECT name, SUM(salary) \"Total_Salary\" FROM (\n"
                + "   SELECT CONNECT_BY_ROOT last_name as name, Salary\n" + "      FROM employees\n"
                + "      WHERE department_id = 110\n"
                + "      CONNECT BY PRIOR employee_id = manager_id)\n" + "      GROUP BY name",
                true);

        assertSqlCanBeParsedAndDeparsed("SELECT CONNECT_BY_ROOT last_name as name" + ", salary "
                + "FROM employees " + "WHERE department_id = 110 "
                + "CONNECT BY PRIOR employee_id = manager_id", true);
    }

    @Test
    public void testUnionLimitOrderByIssue1268() throws JSQLParserException {
        String sqlStr =
                "(SELECT __time FROM traffic_protocol_stat_log LIMIT 1) UNION ALL (SELECT __time FROM traffic_protocol_stat_log ORDER BY __time LIMIT 1)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCastToRowConstructorIssue1267() throws JSQLParserException {
        String sqlStr =
                "SELECT CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR)) AS datapoints";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCollisionWithSpecialStringFunctionsIssue1284() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT test( a in (1) AND 2=2) ", true);

        assertSqlCanBeParsedAndDeparsed("select\n"
                + "sum(if(column1 in('value1', 'value2'), 1, 0)) as tcp_logs,\n"
                + "sum(if(column1 in ('value1', 'value2') and column2 = 'value3', 1, 0)) as base_tcp_logs\n"
                + "from\n" + "table1\n" + "where\n"
                + "recv_time >= toDateTime('2021-07-20 00:00:00')\n"
                + "and recv_time < toDateTime('2021-07-21 00:00:00')", true);
    }

    @Test
    public void testJoinWithTrailingOnExpressionIssue1302() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM TABLE1 tb1\n" + "INNER JOIN TABLE2 tb2\n"
                + "INNER JOIN TABLE3 tb3\n" + "INNER JOIN TABLE4 tb4\n" + "ON (tb3.aaa = tb4.aaa)\n"
                + "ON (tb2.aaa = tb3.aaa)\n" + "ON (tb1.aaa = tb2.aaa)", true);

        assertSqlCanBeParsedAndDeparsed("SELECT *\n" + "FROM\n" + "TABLE1 tbl1\n"
                + "    INNER JOIN TABLE2 tbl2\n" + "        INNER JOIN TABLE3 tbl3\n"
                + "        ON (tbl2.column1 = tbl3.column1)\n"
                + "    ON (tbl1.column2 = tbl2.column2)\n" + "WHERE\n" + "tbl1.column1 = 123",
                true);
    }

    @Test
    public void testSimpleJoinOnExpressionIssue1229() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select t1.column1,t1.column2,t2.field1,t2.field2 from T_DT_ytb_01 t1 , T_DT_ytb_02 t2 on t1.column1 = t2.field1",
                true);
    }

    @Test
    public void testNestedCaseComplexExpressionIssue1306() throws JSQLParserException {
        // with extra brackets
        assertSqlCanBeParsedAndDeparsed("SELECT CASE\n" + "WHEN 'USD' = 'USD'\n" + "THEN 0\n"
                + "ELSE CASE\n" + "WHEN 'USD' = 'EURO'\n" + "THEN ( CASE\n" + "WHEN 'A' = 'B'\n"
                + "THEN 0\n" + "ELSE 1\n" + "END * 100 )\n" + "ELSE 2\n" + "END\n"
                + "END AS \"column1\"\n" + "FROM test_schema.table_name\n" + "", true);

        // without brackets
        assertSqlCanBeParsedAndDeparsed("SELECT CASE\n" + "WHEN 'USD' = 'USD'\n" + "THEN 0\n"
                + "ELSE CASE\n" + "WHEN 'USD' = 'EURO'\n" + "THEN CASE\n" + "WHEN 'A' = 'B'\n"
                + "THEN 0\n" + "ELSE 1\n" + "END * 100 \n" + "ELSE 2\n" + "END\n"
                + "END AS \"column1\"\n" + "FROM test_schema.table_name\n" + "", true);
    }

    @Test
    public void testGroupByComplexExpressionIssue1308() throws JSQLParserException {
        // without extra brackets
        assertSqlCanBeParsedAndDeparsed("select * \n" + "from dual \n"
                + "group by case when 1=1 then 'X' else 'Y' end, column1", true);

        // with extra brackets for List
        assertSqlCanBeParsedAndDeparsed("select * \n" + "from dual \n"
                + "group by (case when 1=1 then 'X' else 'Y' end, column1)", true);

        // with extra brackets for Expression
        assertSqlCanBeParsedAndDeparsed("select * \n" + "from dual \n"
                + "group by (case when 1=1 then 'X' else 'Y' end), column1", true);
    }

    @Test
    public void testReservedKeywordsMSSQLUseIndexIssue1325() throws JSQLParserException {
        // without extra brackets
        assertSqlCanBeParsedAndDeparsed("SELECT col FROM table USE INDEX(primary)", true);
    }

    @Test
    public void testReservedKeywordsIssue1352() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT system from b1.system", true);
        assertSqlCanBeParsedAndDeparsed("SELECT query from query.query", true);
        assertSqlCanBeParsedAndDeparsed("SELECT fulltext from fulltext.fulltext", true);
    }

    @Test
    public void testTableSpaceKeyword() throws JSQLParserException {
        // without extra brackets
        assertSqlCanBeParsedAndDeparsed(
                "SELECT DDF.tablespace                                  TABLESPACE_NAME\n"
                        + "         , maxtotal / 1024 / 1024                        \"MAX_MB\"\n"
                        + "         , ( total - free ) / 1024 / 1024                \"USED_MB\"\n"
                        + "         , ( maxtotal - ( total - free ) ) / 1024 / 1024 \"AVAILABLE_MB\"\n"
                        + "         , total / 1024 / 1024                           \"ALLOCATED_MB\"\n"
                        + "         , free / 1024 / 1024                            \"ALLOCATED_FREE_MB\"\n"
                        + "         , ( ( total - free ) / maxtotal * 100 )         \"USED_PERC\"\n"
                        + "         , cnt                                           \"FILE_COUNT\"\n"
                        + "  FROM   (SELECT tablespace_name                  TABLESPACE\n"
                        + "                 , SUM(bytes)                     TOTAL\n"
                        + "                 , SUM(Greatest(maxbytes, bytes)) MAXTOTAL\n"
                        + "                 , Count(*)                       CNT\n"
                        + "          FROM   dba_data_files\n"
                        + "          GROUP  BY tablespace_name) DDF\n"
                        + "         , (SELECT tablespace_name TABLESPACE\n"
                        + "                   , SUM(bytes)    FREE\n"
                        + "                   , Max(bytes)    MAXF\n"
                        + "            FROM   dba_free_space\n"
                        + "            GROUP  BY tablespace_name) DFS\n"
                        + "  WHERE  DDF.tablespace = DFS.tablespace\n" + "  ORDER  BY 1 DESC",
                true);
    }

    @Test
    public void testTableSpecificAllColumnsIssue1346() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT count(*) from a", true);

        assertSqlCanBeParsedAndDeparsed("SELECT count(a.*) from a", true);
    }

    @Test
    public void testPostgresDollarQuotes_1372() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT UPPER($$some text$$) FROM a");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM a WHERE a.test = $$where text$$");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM a WHERE a.test = $$$$");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM a WHERE a.test = $$ $$");
        assertSqlCanBeParsedAndDeparsed("SELECT aa AS $$My Column Name$$ FROM a");
    }

    @Test
    public void testCanCallSubSelectOnWithItemEvenIfNotSetIssue1369() {
        WithItem item = new WithItem();
        assertThat(item.getSelect()).isNull();
    }

    @Test
    public void testCaseElseExpressionIssue1375() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1 WHERE CASE WHEN 1 = 1 THEN c1 = 'a' ELSE c2 = 'b' AND c4 = 'd' END",
                true);
    }

    @Test
    public void testComplexInExpressionIssue905() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM table_a\n"
                        + "WHERE other_id IN ( (   SELECT id\n"
                        + "                        FROM table_b\n"
                        + "                        WHERE name LIKE '%aa%' ), ( SELECT id\n"
                        + "                                                    FROM table_b\n"
                        + "                                                    WHERE name LIKE '%bb%' ) )\n",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM v.e\n"
                        + "WHERE cid <> rid\n"
                        + "    AND rid NOT IN (    ( SELECT DISTINCT\n"
                        + "                                rid\n"
                        + "                            FROM v.s )\n"
                        + "                        UNION (\n"
                        + "                            SELECT DISTINCT\n"
                        + "                                rid\n"
                        + "                            FROM v.p ) )\n"
                        + "    AND \"timestamp\" <= 1298505600000\n",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM table_a\n"
                        + "WHERE ( a, b, c ) IN ( ( 1, 2, 3 ), ( 3, 4, 5 ) )\n",
                true);
    }

    @Test
    public void testComplexInExpressionSimplyfied() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM dual\n"
                        + "WHERE a IN ( ( SELECT id1), ( SELECT id2) )\n",
                true);

        assertExpressionCanBeParsedAndDeparsed(
                "a IN ( ( SELECT id1) UNION (SELECT id2) )\n", true);

        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM e\n"
                        + "WHERE a IN ( ( SELECT id1) UNION (SELECT id2) )\n",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "SELECT *\n"
                        + "FROM table_a\n"
                        + "WHERE ( a, b, c ) IN ( ( 1, 2, 3 ), ( 3, 4, 5 ) )\n",
                true);
    }

    @Test
    public void testLogicalExpressionSelectItemIssue1381() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ( 1 + 1 ) = ( 1 + 2 )", true);

        assertSqlCanBeParsedAndDeparsed("SELECT ( 1 = 1 ) = ( 1 = 2 )", true);

        assertSqlCanBeParsedAndDeparsed("SELECT ( ( 1 = 1 ) AND ( 1 = 2 ) )", true);

        assertSqlCanBeParsedAndDeparsed("SELECT ( 1 = 1 ) AND ( 1 = 2 )", true);
    }

    @Test
    public void testKeywordAtIssue1414() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM table1 at");
    }

    @Test
    public void testIgnoreNullsForWindowFunctionsIssue1429() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT lag(mydata) IGNORE NULLS OVER (ORDER BY sortorder) AS previous_status FROM mytable");
    }

    @Test
    @Timeout(1000)
    public void testPerformanceIssue1438() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("" + "SELECT \t* FROM TABLE_1 t1\n" + "WHERE\n"
                + "\t(((t1.COL1 = 'VALUE2' )\n" + "\t\tAND (t1.CAL2 = 'VALUE2' ))\n"
                + "\t\tAND (((1 = 1 )\n"
                + "\t\t\tAND ((((((t1.id IN (940550 ,940600 ,940650 ,940700 ,940750 ,940800 ,940850 ,940900 ,940950 ,941000 ,941050 ,941100 ,941150 ,941200 ,941250 ,941300 ,941350 ,941400 ,941450 ,941500 ,941550 ,941600 ,941650 ,941700 ,941750 ,941800 ,941850 ,941900 ,941950 ,942000 ,942050 ,942100 ,942150 ,942200 ,942250 ,942300 ,942350 ,942400 ,942450 ,942500 ,942550 ,942600 ,942650 ,942700 ,942750 ,942800 ,942850 ,942900 ,942950 ,943000 ,943050 ,943100 ,943150 ,943200 ,943250 ,943300 ,943350 ,943400 ,943450 ,943500 ,943550 ,943600 ,943650 ,943700 ,943750 ,943800 ,943850 ,943900 ,943950 ,944000 ,944050 ,944100 ,944150 ,944200 ,944250 ,944300 ,944350 ,944400 ,944450 ,944500 ,944550 ,944600 ,944650 ,944700 ,944750 ,944800 ,944850 ,944900 ,944950 ,945000 ,945050 ,945100 ,945150 ,945200 ,945250 ,945300 ))\n"
                + "\t\t\t\tOR (t1.id IN (945350 ,945400 ,945450 ,945500 ,945550 ,945600 ,945650 ,945700 ,945750 ,945800 ,945850 ,945900 ,945950 ,946000 ,946050 ,946100 ,946150 ,946200 ,946250 ,946300 ,946350 ,946400 ,946450 ,946500 ,946550 ,946600 ,946650 ,946700 ,946750 ,946800 ,946850 ,946900 ,946950 ,947000 ,947050 ,947100 ,947150 ,947200 ,947250 ,947300 ,947350 ,947400 ,947450 ,947500 ,947550 ,947600 ,947650 ,947700 ,947750 ,947800 ,947850 ,947900 ,947950 ,948000 ,948050 ,948100 ,948150 ,948200 ,948250 ,948300 ,948350 ,948400 ,948450 ,948500 ,948550 ,948600 ,948650 ,948700 ,948750 ,948800 ,948850 ,948900 ,948950 ,949000 ,949050 ,949100 ,949150 ,949200 ,949250 ,949300 ,949350 ,949400 ,949450 ,949500 ,949550 ,949600 ,949650 ,949700 ,949750 ,949800 ,949850 ,949900 ,949950 ,950000 ,950050 ,950100 )))\n"
                + "\t\t\t\tOR (t1.id IN (950150 ,950200 ,950250 ,950300 ,950350 ,950400 ,950450 ,950500 ,950550 ,950600 ,950650 ,950700 ,950750 ,950800 ,950850 ,950900 ,950950 ,951000 ,951050 ,951100 ,951150 ,951200 ,951250 ,951300 ,951350 ,951400 ,951450 ,951500 ,951550 ,951600 ,951650 ,951700 ,951750 ,951800 ,951850 ,951900 ,951950 ,952000 ,952050 ,952100 ,952150 ,952200 ,952250 ,952300 ,952350 ,952400 ,952450 ,952500 ,952550 ,952600 ,952650 ,952700 ,952750 ,952800 ,952850 ,952900 ,952950 ,953000 ,953050 ,953100 ,953150 ,953200 ,953250 ,953300 ,953350 ,953400 ,953450 ,953500 ,953550 ,953600 ,953650 ,953700 )))\n"
                + "\t\t\t\tOR (t1.id IN (953750 ,953800 ,953850 ,953900 ,953950 ,954000 ,954050 ,954100 ,954150 ,954200 ,954250 ,954300 ,954350 ,954400 ,954450 ,954500 ,954550 ,954600 ,954650 ,954700 ,954750 ,954800 ,954850 ,954900 ,954950 ,955000 ,955050 ,955100 ,955150 ,955200 ,955250 ,955300 ,955350 ,955400 ,955450 ,955500 ,955550 ,955600 ,955650 ,955700 ,955750 ,955800 ,955850 ,955900 ,955950 ,956000 ,956050 ,956100 ,956150 ,956200 ,956250 ,956300 ,956350 ,956400 ,956450 ,956500 ,956550 ,956600 ,956650 ,956700 ,956750 ,956800 ,956850 ,956900 ,956950 ,957000 ,957050 ,957100 ,957150 ,957200 ,957250 ,957300 )))\n"
                + "\t\t\t\tOR (t1.id IN (944100, 944150, 944200, 944250, 944300, 944350, 944400, 944450, 944500, 944550, 944600, 944650, 944700, 944750, 944800, 944850, 944900, 944950, 945000 )))\n"
                + "\t\t\t\tOR (t1.id IN (957350 ,957400 ,957450 ,957500 ,957550 ,957600 ,957650 ,957700 ,957750 ,957800 ,957850 ,957900 ,957950 ,958000 ,958050 ,958100 ,958150 ,958200 ,958250 ,958300 ,958350 ,958400 ,958450 ,958500 ,958550 ,958600 ,958650 ,958700 ,958750 ,958800 ,958850 ,958900 ,958950 ,959000 ,959050 ,959100 ,959150 ,959200 ,959250 ,959300 ,959350 ,959400 ,959450 ,959500 ,959550 ,959600 ,959650 ,959700 ,959750 ,959800 ,959850 ,959900 ,959950 ,960000 ,960050 ,960100 ,960150 ,960200 ,960250 ,960300 ,960350 ,960400 ,960450 ,960500 ,960550 ,960600 ,960650 ,960700 ,960750 ,960800 ,960850 ,960900 ,960950 ,961000 ,961050 ,961100 ,961150 ,961200 ,961250 ,961300 ,961350 ,961400 ,961450 ,961500 ,961550 ,961600 ,961650 ,961700 ,961750 ,961800 ,961850 ,961900 ,961950 ,962000 ,962050 ,962100 ))))\n"
                + "\t\t\t\tOR (t1.id IN (962150 ,962200 ,962250 ,962300 ,962350 ,962400 ,962450 ,962500 ,962550 ,962600 ,962650 ,962700 ,962750 ,962800 ,962850 ,962900 ,962950 ,963000 ,963050 ,963100 ,963150 ,963200 ,963250 ,963300 ,963350 ,963400 ,963450 ,963500 ,963550 ,963600 ,963650 ,963700 ,963750 ,963800 ,963850 ,963900 ,963950 ,964000 ,964050 ,964100 ,964150 ,964200 ,964250 ,964300 ,964350 ,964400 ,964450 ,964500 ,964550 ,964600 ,964650 ,964700 ,964750 ,964800 ,964850 ,964900 ,964950 ,965000 ,965050 ,965100 ,965150 ,965200 ,965250 ,965300 ,965350 ,965400 ,965450 ,965500 ))))\n"
                + "\tAND t1.COL3 IN (\n" + "\t    SELECT\n" + "\t\t    t2.COL3\n" + "\t    FROM\n"
                + "\t\t    TABLE_6 t6,\n" + "\t\t    TABLE_1 t5,\n" + "\t\t    TABLE_4 t4,\n"
                + "\t\t    TABLE_3 t3,\n" + "\t\t    TABLE_1 t2\n" + "\t    WHERE\n"
                + "\t\t    (((((((t5.CAL3 = T6.id)\n" + "\t\t\t    AND (t5.CAL5 = t6.CAL5))\n"
                + "\t\t\t    AND (t5.CAL1 = t6.CAL1))\n" + "\t\t\t    AND (t3.CAL1 IN (108500)))\n"
                + "\t\t\t    AND (t5.id = t2.id))\n"
                + "\t\t\t    AND NOT ((t6.CAL6 IN ('VALUE'))))\n"
                + "\t\t\t    AND ((t2.id = t3.CAL2)\n" + "\t\t\t\t    AND (t4.id = t3.CAL3))))\n"
                + "ORDER BY\n" + "\tt1.id ASC", true);
    }

    @Test
    @Timeout(1000)
    public void testPerformanceIssue1397() throws Exception {
        String sqlStr = IOUtils.toString(
                SelectTest.class.getResource(
                        "/net/sf/jsqlparser/statement/select/performanceIssue1397.sql"),
                Charset.defaultCharset());
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testWithIsolation() throws JSQLParserException {
        String statement = "SELECT * FROM mytable WHERE mytable.col = 9 WITH ur";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        String isolation = select.getIsolation().getIsolation();
        assertEquals("ur", isolation);

        statement = "SELECT * FROM mytable WHERE mytable.col = 9 WITH Cs";
        select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        isolation = select.getIsolation().getIsolation();
        assertEquals("Cs", isolation);
    }

    @Test
    public void testLoclTimezone1471() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT TO_CHAR(CAST(SYSDATE AS TIMESTAMP WITH LOCAL TIME ZONE), 'HH:MI:SS AM TZD') FROM DUAL");
    }

    @Test
    public void testMissingLimitIssue1505() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("(SELECT * FROM mytable) LIMIT 1");
    }

    @Test
    public void testPostgresNaturalJoinIssue1559() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT t1.ID,t1.name, t2.DID, t2.name\n"
                + "FROM table1 as t1\n" + "NATURAL RIGHT JOIN table2 as t2", true);

        assertSqlCanBeParsedAndDeparsed("SELECT t1.ID,t1.name, t2.DID, t2.name\n"
                + "FROM table1 as t1\n" + "NATURAL RIGHT JOIN table2 as t2", true);
    }

    @Test
    public void testNamedWindowDefinitionIssue1581() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT sum(salary) OVER w, avg(salary) OVER w FROM empsalary WINDOW w AS (PARTITION BY depname ORDER BY salary DESC)");
    }

    @Test
    public void testNamedWindowDefinitionIssue1581_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT sum(salary) OVER w1, avg(salary) OVER w2 FROM empsalary WINDOW w1 AS (PARTITION BY depname ORDER BY salary DESC), w2 AS (PARTITION BY depname2 ORDER BY salary2)");
    }

    @Test
    public void testTimestamptzDateTimeLiteral() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM table WHERE x >= TIMESTAMPTZ '2021-07-05 00:00:00+00'");
    }

    @Test
    public void testFunctionComplexExpressionParametersIssue1644() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT test(1=1, 'a', 'b')", true);
        assertSqlCanBeParsedAndDeparsed("SELECT if(instr('avc','a')=0, 'avc', 'aaa')", true);
    }

    @Test
    public void testOracleDBLink() throws JSQLParserException {
        String sqlStr = "SELECT * from tablename@dblink";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Select select = (Select) CCJSqlParserUtil.parse(sqlStr);
        PlainSelect plainSelect = (PlainSelect) select;
        Table table = (Table) plainSelect.getFromItem();

        assertNotEquals("tablename@dblink", table.getName());
        assertEquals("tablename", table.getName());
        assertEquals("dblink", table.getDBLinkName());
    }

    @Test
    public void testSelectStatementWithForUpdateAndSkipLockedTokens() throws JSQLParserException {
        String sql = "SELECT * FROM test FOR UPDATE SKIP LOCKED";
        assertSqlCanBeParsedAndDeparsed(sql);

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select;
        assertSame(plainSelect.getForMode(), ForMode.UPDATE);
        assertTrue(plainSelect.isSkipLocked());
    }

    @Test
    public void testSelectStatementWithForUpdateButWithoutSkipLockedTokens()
            throws JSQLParserException {
        String sql = "SELECT * FROM test FOR UPDATE";
        assertSqlCanBeParsedAndDeparsed(sql);

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select;
        assertSame(plainSelect.getForMode(), ForMode.UPDATE);
        assertFalse(plainSelect.isSkipLocked());
    }

    @Test
    public void testSelectStatementWithoutForUpdateAndSkipLockedTokens()
            throws JSQLParserException {
        String sql = "SELECT * FROM test";
        assertSqlCanBeParsedAndDeparsed(sql);

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select;
        assertNull(plainSelect.getForMode());
        assertFalse(plainSelect.isSkipLocked());
    }

    @Test
    public void testSelectMultidimensionalArrayStatement() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT f1, f2[1][1], f3[1][2][3] FROM test");
    }

    @Test
    void testSetOperationListWithBracketsIssue1737() throws JSQLParserException {
        String sqlStr = "(SELECT z)\n" + "         UNION ALL\n" + "         (SELECT z)\n"
                + "         ORDER BY z";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);


        sqlStr = "SELECT z\n" + "FROM (\n"
                + "         (SELECT z)\n"
                + "         UNION ALL\n"
                + "         (SELECT z)\n"
                + "         ORDER BY z\n" + "     )\n"
                + // "GROUP BY z\n" +
                "ORDER BY z\n";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT z\n" + "FROM (\n" + "         (SELECT z)\n" + "         UNION ALL\n"
                + "         (SELECT z)\n" + "         ORDER BY z\n" + "     )\n" + "GROUP BY z\n"
                + "ORDER BY z";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void subJoinTest() throws JSQLParserException {
        String sqlStr =
                "select su.d\n" + "from sku su\n" + "for update of su.up\n" + "order by su.d";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testNestedWithItems() throws JSQLParserException {
        String sqlStr =
                "with a as ( with b as ( with c as (select 1) select c.* from c) select b.* from b) select a.* from a";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSubSelectParsing() throws JSQLParserException {
        String sqlStr = "(SELECT id FROM table1 WHERE find_in_set(100, ancestors))";
        Select select = (Select) CCJSqlParserUtil.parse(sqlStr);

        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column("id"));
        inExpression.setRightExpression(select);

        Assertions.assertEquals("id IN " + sqlStr, inExpression.toString());
    }

    @Test
    void testLateralView() throws JSQLParserException {
        String sqlStr1 =
                "SELECT * FROM person\n"
                        + "    LATERAL VIEW EXPLODE(ARRAY(30, 60)) tableName AS c_age\n"
                        + "    LATERAL VIEW EXPLODE(ARRAY(40, 80)) AS d_age";

        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr1, true);
        Assertions.assertEquals(2, select.getLateralViews().size());

        String sqlStr2 =
                "SELECT * FROM person\n"
                        + "    LATERAL VIEW OUTER EXPLODE(ARRAY(30, 60)) AS c_age";

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr2, true);
        Assertions.assertEquals(1, select.getLateralViews().size());

        Function function = new Function()
                .withName("Explode")
                .withParameters(new Function()
                        .withName("Array")
                        .withParameters(
                                new LongValue(30), new LongValue(60)));
        LateralView lateralView1 = new LateralView(
                true, function, null, new Alias("c_age", true));


        select = new PlainSelect()
                .addSelectItems(new AllColumns())
                .withFromItem(new Table("person"))
                .addLateralView(lateralView1);
        assertStatementCanBeDeparsedAs(select, sqlStr2, true);

        Function function2 = new Function()
                .withName("Explode")
                .withParameters(new Function()
                        .withName("Array")
                        .withParameters(
                                new LongValue(40), new LongValue(80)));
        LateralView lateralView2 = SerializationUtils
                .clone(lateralView1.withOuter(false).withTableAlias(new Alias("tableName")))
                .withOuter(false)
                .withGeneratorFunction(function2)
                .withTableAlias(null)
                .withColumnAlias(new Alias("d_age", true));
        select.addLateralView(lateralView2);
        assertStatementCanBeDeparsedAs(select, sqlStr1, true);
    }

    @Test
    void testOracleHavingBeforeGroupBy() throws JSQLParserException {
        String sqlStr = "SELECT id from a having count(*) > 1 group by id";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        Assertions.assertEquals("count(*) > 1", select.getHaving().toString());
        Assertions.assertEquals("GROUP BY id", select.getGroupBy().toString());
    }

    @Test
    void testParameterMultiPartName() throws JSQLParserException {
        String sqlStr = "SELECT 1 FROM dual WHERE a = :paramMap.aValue";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        assertEquals("paramMap.aValue", select
                .getWhere(EqualsTo.class)
                .getRightExpression(JdbcNamedParameter.class)
                .getName());
    }

    @Test
    void testInnerJoin() throws JSQLParserException {
        String sqlStr = "SELECT 1 from a inner join b on a.id=b.id";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        Join join = select.getJoins().get(0);

        assertTrue(join.isInnerJoin());
        assertTrue(join.withInner(false).isInnerJoin());
        assertFalse(join.withLeft(true).isInnerJoin());
        assertFalse(join.withRight(true).isInnerJoin());
        assertFalse(join.withInner(true).isRight());
    }

    @Test
    void testArrayColumnsIssue1757() throws JSQLParserException {
        String sqlStr = "SELECT my_map['my_key'] FROM my_table WHERE id = 123";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT cast(my_map['my_key'] as int) FROM my_table WHERE id = 123";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testQualifyClauseIssue1805() throws JSQLParserException {
        String sqlStr = "SELECT i, p, o\n" +
                "    FROM qt\n" +
                "    QUALIFY ROW_NUMBER() OVER (PARTITION BY p ORDER BY o) = 1";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testNotNullInFilter() throws JSQLParserException {
        String stmt = "SELECT count(*) FILTER (WHERE i NOTNULL) AS filtered FROM tasks";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testNotIsNullInFilter() throws JSQLParserException {
        String stmt = "SELECT count(*) FILTER (WHERE i NOT ISNULL) AS filtered FROM tasks";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    void testBackSlashQuotationIssue1812() throws JSQLParserException {
        String sqlStr = "SELECT ('\\'', 'a')";
        Statement stmt2 = CCJSqlParserUtil.parse(
                sqlStr, parser -> parser
                        .withBackslashEscapeCharacter(true));

        sqlStr = "INSERT INTO recycle_record (a,f) VALUES ('\\'anything', 'abc');";
        stmt2 = CCJSqlParserUtil.parse(
                sqlStr, parser -> parser
                        .withBackslashEscapeCharacter(true));

        sqlStr = "INSERT INTO recycle_record (a,f) VALUES ('\\'','83653692186728700711687663398101');";
        stmt2 = CCJSqlParserUtil.parse(
                sqlStr, parser -> parser
                        .withBackslashEscapeCharacter(true));
    }
}
