/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ConnectByPriorOperator;
import net.sf.jsqlparser.expression.JsonAggregateOnNullType;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.TernaryExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.WindowOffset;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.MapExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.IsUnknownExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.MySqlSelectModifier;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that all non-reserved keywords can be used as unquoted identifiers (schema, table,
 * column, alias, function names).
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class KeywordsTest {

    public static Stream<String> nonReservedKeywords() {
        return ParserKeywordsUtils.getNonReservedKeywords().stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameWithoutValue(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s AS %1$s from %1$s.%1$s AS %1$s", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameExt(String keyword) throws JSQLParserException {
        String sqlStr = String.format(
                "SELECT %1$s.%1$s.%1$s \"%1$s\" from %1$s \"%1$s\" ORDER BY %1$s ", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCombinedTokenKeywords() throws JSQLParserException {
        String sqlStr = "SELECT current_date(3)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // #2473: GROUPS is a non-reserved keyword in PostgreSQL and must stay usable as an
    // identifier, although it also introduces the GROUPS window frame unit.
    @Test
    public void testGroupsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM groups", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (groups INT)", true);
    }

    // sample and extend are non-reserved in the common dialects and reach the
    // parser as ordinary table and column names in downstream rewriting tools.
    @Test
    public void testSampleExtendAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT sample, extend FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT mytable.extend FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE extend = 'x'", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET sample = 1, extend = 'x' WHERE id = 1",
                true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (sample, extend) VALUES (1, 'x')",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM sample", true);
        assertSqlCanBeParsedAndDeparsed("DELETE FROM sample WHERE extend = 'x'", true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO sample (extend) VALUES ('x')", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS sample, b AS extend FROM mytable", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendImplicitAliases(String keyword) throws JSQLParserException {
        // keyword is a non-reserved keyword in the common dialects and reaches the
        // parser as an ordinary column name in downstream rewriting tools.
        assertSqlCanBeParsedAndDeparsed("SELECT " + keyword + " FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT mytable.\" + keyword + \" FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE \" + keyword + \" = 1", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable ORDER BY \" + keyword + \"", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET \" + keyword + \" = 1 WHERE id = 1",
                true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (\" + keyword + \") VALUES (1)", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS \" + keyword + \" FROM mytable", true);
    }

    // the guarded keyword form keeps its operator meaning
    @Test
    public void testExistsPredicateStaysPredicate() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS (SELECT 1 FROM x) FROM mytable", true);
        assertInstanceOf(ExistsExpression.class, select.getSelectItem(0).getExpression());
        PlainSelect negated = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE NOT EXISTS (SELECT 1 FROM x)", true);
        assertInstanceOf(ExistsExpression.class,
                assertInstanceOf(NotExpression.class, negated.getWhere()).getExpression());
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE EXISTS (SELECT 1 FROM x)",
                true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs"})
    void testExistsImplicitAliases(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT a " + keyword + " FROM mytable " + keyword, true);
        assertEquals(keyword, select.getSelectItem(0).getAlias().getName());
        assertEquals(keyword, select.getFromItem().getAlias().getName());
        assertEquals("a", assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs", "sample", "extend", "SaMpLe", "ExTeNd"})
    void testExistsCte(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "WITH " + keyword + "(x) AS (SELECT 1) SELECT x FROM " + keyword, true);
        WithItem<?> withItem = select.getWithItemsList().get(0);
        assertEquals(keyword, withItem.getAliasName());
        assertNotNull(withItem.getParenthesedStatement());
        assertNull(withItem.getExpression());
        assertEquals(keyword, assertInstanceOf(Table.class, select.getFromItem()).getName());
        assertSqlCanBeParsedAndDeparsed(
                "WITH RECURSIVE " + keyword + "(x) AS (SELECT 1) SELECT * FROM " + keyword, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs", "sample", "extend", "SaMpLe", "ExTeNd"})
    void testExistsExpressionContexts(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT ALL " + keyword + " FROM mytable");
        assertEquals(keyword, assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
        assertSqlCanBeParsedAndDeparsed("SELECT CASE " + keyword
                + " WHEN 1 THEN " + keyword + " ELSE 0 END FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE " + keyword + " SET a = 1", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs", "sample", "extend", "SaMpLe", "ExTeNd"})
    void testExistsTernaryOperand(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? " + keyword + " : 0 FROM mytable", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(keyword, assertInstanceOf(Column.class, ternary.getThenExpression())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SAMPLE (10)", "SAMPLE BLOCK (10) SEED (1)",
            "SAMPLE 0.1 OFFSET 1000", "USING SAMPLE 10 PERCENT (BERNOULLI)",
            "TABLESAMPLE SYSTEM (10)"})
    void testSampleClauseStaysClause(String clause) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable " + clause, true);
        Table table = assertInstanceOf(Table.class, select.getFromItem());
        assertNull(table.getAlias());
        SampleClause sample = table.getSampleClause();
        assertNotNull(sample);
        assertEquals(" " + clause, sample.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendPipeExpression(String keyword) throws JSQLParserException {
        FromQuery query = (FromQuery) assertSqlCanBeParsedAndDeparsed(
                "FROM " + keyword + " |> EXTEND " + keyword + " AS x", true);
        SelectPipeOperator operator = assertInstanceOf(SelectPipeOperator.class,
                query.getPipeOperators().get(0));
        assertEquals("EXTEND", operator.getOperatorName());
        assertEquals(keyword, assertInstanceOf(Column.class,
                operator.getSelectItems().get(0).getExpression()).getColumnName());
        assertSqlCanBeParsedAndDeparsed(
                "FROM mytable |> EXTEND " + keyword + "(x) AS x", true);
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT ALL " + keyword + "(x) FROM mytable");
        assertEquals(keyword, assertInstanceOf(Function.class,
                select.getSelectItem(0).getExpression()).getName());
    }

    @Test
    void testExistsColumnWithOracleJoin() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE exists(+) = 1", true);
        EqualsTo equals = assertInstanceOf(EqualsTo.class, select.getWhere());
        assertEquals("exists", assertInstanceOf(Column.class, equals.getLeftExpression())
                .getColumnName());
        assertEquals(SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT,
                ((Column) equals.getLeftExpression()).getOldOracleJoinSyntax());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1", "VALUES (1)"})
    void testExistsUnparenthesizedSubSelect(String query) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS " + query, true, p -> p.withUnparenthesizedSubSelects(true));
        assertInstanceOf(ExistsExpression.class, select.getSelectItem(0).getExpression());
        PlainSelect where = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE EXISTS " + query, true,
                p -> p.withUnparenthesizedSubSelects(true));
        assertInstanceOf(ExistsExpression.class, where.getWhere());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "t.a", "[1]", "+1", "(1)", "(+1)", ":arr", "limit", "offset"})
    void testExistsBareOperandStaysPredicate(String operand) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS " + operand + " FROM t", true);
        ExistsExpression exists = assertInstanceOf(ExistsExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(operand, exists.getRightExpression().toString());
        PlainSelect where = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE EXISTS " + operand, true);
        assertInstanceOf(ExistsExpression.class, where.getWhere());
    }

    @Test
    void testExistsNamedParameterInTernary() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? EXISTS :arr : 0 FROM t", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        ExistsExpression exists = assertInstanceOf(ExistsExpression.class,
                ternary.getThenExpression());
        assertEquals("arr", assertInstanceOf(JdbcNamedParameter.class,
                exists.getRightExpression()).getName());
    }

    @Test
    void testExistsMapKey() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT MAP {exists: 1}", true);
        MapExpression map = assertInstanceOf(MapExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, map.getEntries().get(0).getKey())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN (1, 2)", "NOT IN (1, 2)"})
    void testExistsColumnInCondition(String suffix) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE exists " + suffix, true);
        InExpression in = assertInstanceOf(InExpression.class, select.getWhere());
        assertEquals("exists", assertInstanceOf(Column.class, in.getLeftExpression())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER BY id", "ORDER SIBLINGS BY id", "GROUP BY id", "LIMIT 1",
            "LIMIT 1.0", "LIMIT ALL", "LIMIT NULL", "LIMIT ?", "LIMIT $1", "LIMIT :n", "LIMIT &n",
            "OFFSET 1", "OFFSET 1.0", "OFFSET ?", "OFFSET $1", "OFFSET :n", "OFFSET &n"})
    void testExistsColumnBeforeClause(String suffix) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE exists " + suffix, true);
        assertEquals("exists", assertInstanceOf(Column.class, select.getWhere()).getColumnName());
        if (suffix.startsWith("LIMIT")) {
            assertNotNull(select.getLimit());
            assertEquals(suffix, select.getLimit().toString().trim());
        } else if (suffix.startsWith("OFFSET")) {
            assertNotNull(select.getOffset());
            assertEquals(suffix, select.getOffset().toString().trim());
        }
    }

    @Test
    void testExistsBeforeNestedTernary() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? exists : b ? c : d FROM t", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, ternary.getThenExpression())
                .getColumnName());
        assertInstanceOf(TernaryExpression.class, ternary.getElseExpression());
        PlainSelect mapSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT MAP {exists: b ? c : d}", true);
        MapExpression map = assertInstanceOf(MapExpression.class,
                mapSelect.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, map.getEntries().get(0).getKey())
                .getColumnName());
        assertInstanceOf(TernaryExpression.class, map.getEntries().get(0).getValue());
    }

    // ERRORS introduces Exasol's IMPORT/EXPORT error clauses but is an ordinary name elsewhere,
    // e.g. a DuckDB column holding the errors of an upstream step.
    @Test
    public void testErrorsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT errors FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT t.errors AS errors FROM mytable t", true);
        assertSqlCanBeParsedAndDeparsed("WITH errors AS (SELECT 1) SELECT * FROM errors", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (errors VARCHAR)", true);
    }

    // Dialect-specific clause words (Exasol IMPORT/EXPORT, ClickHouse PREWHERE/SETTINGS, CREATE
    // FUNCTION ... RETURNS) are ordinary names in other dialects, e.g. PostgreSQL or DuckDB.
    @Test
    public void testDialectClauseWordsAsIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT csv, fbv, file, prewhere, returns, script, settings, verify FROM mytable",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT t.file, t.settings FROM mytable t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM settings", true);
        assertSqlCanBeParsedAndDeparsed("WITH file AS (SELECT 1) SELECT * FROM file", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (csv VARCHAR, settings JSON)", true);
    }

    @Test
    public void testClickHouseClausesAfterTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM mytable PREWHERE b = 1 WHERE c = 2", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM mytable SETTINGS max_threads = 8", true);
    }

    // MySQL 8.4 and PostgreSQL 18 accept these keywords as unquoted column and table names.
    public static Stream<Arguments> serverKeywordNames() {
        return Stream.of("absent", "any", "between", "current", "exclude", "force", "full",
                "grouping", "if", "import", "offset", "only", "prior", "returning", "set", "some",
                "sql_no_cache", "unbounded", "unknown", "value", "values", "xmlserialize")
                .flatMap(keyword -> Stream.of(Arguments.of(keyword, null),
                        Arguments.of(keyword, Dialect.MYSQL),
                        Arguments.of(keyword, Dialect.POSTGRESQL)));
    }

    private static Statement assertRoundTrip(String sql, Dialect dialect)
            throws JSQLParserException {
        Consumer<CCJSqlParser> consumer = parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        };
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, consumer);
        assertEquals(statement.toString(),
                CCJSqlParserUtil.parse(statement.toString(), consumer).toString());
        return statement;
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("serverKeywordNames")
    void testServerKeywordAsColumnAndTableName(String keyword, Dialect dialect)
            throws JSQLParserException {
        CreateTable createTable = (CreateTable) assertRoundTrip(
                "CREATE TABLE mytable (id INT, " + keyword + " INT)", dialect);
        assertEquals(keyword, createTable.getColumnDefinitions().get(1).getColumnName());

        Alter alter = (Alter) assertRoundTrip(
                "ALTER TABLE mytable ADD COLUMN " + keyword + " INT", dialect);
        assertEquals(keyword,
                alter.getAlterExpressions().get(0).getColDataTypeList().get(0).getColumnName());

        // A keyword table name made CREATE TABLE fall back to an UnsupportedStatement
        CreateTable namedTable = (CreateTable) assertRoundTrip(
                "CREATE TABLE " + keyword + " (a INT)", dialect);
        assertEquals(keyword, namedTable.getTable().getName());

        assertRoundTrip("SELECT " + keyword + ", t." + keyword + " AS " + keyword
                + " FROM mytable t WHERE " + keyword + " = 1 ORDER BY " + keyword, dialect);
        assertRoundTrip("SELECT a FROM " + keyword + " WHERE a = 1", dialect);
        assertRoundTrip("UPDATE mytable SET " + keyword + " = 1", dialect);
        assertRoundTrip("INSERT INTO mytable (" + keyword + ") VALUES (1)", dialect);
    }

    @Test
    void testTableAndColumnKeepTheirOptionalClauses() throws JSQLParserException {
        CreateTable createTable = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE IF NOT EXISTS if (a INT)", true);
        assertTrue(createTable.isIfNotExists());
        assertEquals("if", createTable.getTable().getName());

        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE IF EXISTS value ADD COLUMN IF NOT EXISTS set INT", true);
        assertTrue(alter.isUseTableIfExists());
        assertEquals("value", alter.getTable().getName());
        assertEquals("set",
                alter.getAlterExpressions().get(0).getColDataTypeList().get(0).getColumnName());
    }

    @Test
    void testJoinAndIndexHintKeywordsStayClauses() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM a FULL JOIN b ON a.id = b.id", true);
        assertNull(((Table) select.getFromItem()).getAlias());
        assertTrue(select.getJoins().get(0).isFull());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM a full FULL OUTER JOIN b ON full.id = b.id", true);
        assertEquals("full", select.getFromItem().getAlias().getName());
        Join join = select.getJoins().get(0);
        assertTrue(join.isFull() && join.isOuter());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable FORCE INDEX (idx) WHERE a = 1", true,
                parser -> parser.withDialect(Dialect.MYSQL));
        Table table = (Table) select.getFromItem();
        assertNull(table.getAlias());
        assertNotNull(table.getIndexHint());
    }

    @Test
    void testReturningAndAbsentStayClauses() throws JSQLParserException {
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(
                "DELETE FROM mytable RETURNING id", true);
        assertNull(delete.getTable().getAlias());
        assertNotNull(delete.getReturningClause());

        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO mytable SELECT a FROM other RETURNING id", true);
        assertNull(((PlainSelect) insert.getSelect()).getFromItem().getAlias());
        assertNotNull(insert.getReturningClause());

        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_ARRAY(a ABSENT ON NULL RETURNING JSON) FROM mytable", true);
        JsonFunction function = (JsonFunction) select.getSelectItem(0).getExpression();
        assertEquals(1, function.getExpressions().size());
        assertEquals(JsonAggregateOnNullType.ABSENT, function.getOnNullType());
        assertEquals("JSON", function.getReturningType().getDataType());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAY(RETURNING JSON)",
                true);
        function = (JsonFunction) select.getSelectItem(0).getExpression();
        assertTrue(function.getExpressions().isEmpty());
        assertEquals("JSON", function.getReturningType().getDataType());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECT(ABSENT ON NULL)",
                true);
        function = (JsonFunction) select.getSelectItem(0).getExpression();
        assertTrue(function.getKeyValuePairs().isEmpty());
        assertEquals(JsonAggregateOnNullType.ABSENT, function.getOnNullType());
    }

    @Test
    void testModifierAndOperatorKeywordsStayKeywords() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT SQL_NO_CACHE a FROM mytable", true);
        assertTrue(select.getMySqlSelectModifiers().contains(MySqlSelectModifier.SQL_NO_CACHE));
        assertEquals("a", ((Column) select.getSelectItem(0).getExpression()).getColumnName());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT PRIOR ename FROM emp CONNECT BY PRIOR empno = mgr", true);
        assertInstanceOf(ConnectByPriorOperator.class, select.getSelectItem(0).getExpression());
        EqualsTo connect = (EqualsTo) select.getOracleHierarchical().getConnectExpression();
        assertEquals(SupportsOldOracleJoinSyntax.ORACLE_PRIOR_START,
                connect.getOraclePriorPosition());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT SUM(a) OVER (ORDER BY b ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)"
                        + " FROM mytable",
                true);
        WindowElement frame =
                ((AnalyticExpression) select.getSelectItem(0).getExpression()).getWindowElement();
        assertEquals(WindowOffset.Type.PRECEDING, frame.getRange().getStart().getType());
        assertNull(frame.getRange().getStart().getExpression());
        assertEquals(WindowOffset.Type.CURRENT, frame.getRange().getEnd().getType());

        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT XMLSERIALIZE(CONTENT x AS VARCHAR(100)) FROM mytable WHERE a IS NOT UNKNOWN",
                true);
        assertInstanceOf(XMLSerializeExpr.class, select.getSelectItem(0).getExpression());
        assertInstanceOf(IsUnknownExpression.class, select.getWhere());
    }

    @Test
    void testPostgreSqlOnlyStaysKeyword() throws JSQLParserException {
        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE ONLY public.mytable ADD COLUMN a INT", true);
        assertTrue(alter.isUseOnly());
        assertEquals("public.mytable", alter.getTable().getFullyQualifiedName());

        PlainSelect select =
                (PlainSelect) assertSqlCanBeParsedAndDeparsed("SELECT * FROM ONLY mytable", true);
        assertTrue(select.isUsingOnly());

        CreateIndex createIndex = (CreateIndex) assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX idx ON ONLY mytable (a)", true);
        assertTrue(createIndex.isOnly());

        // Still unsupported forms must not be read as a table named only
        for (String sql : new String[] {"DELETE FROM ONLY mytable",
                "SELECT * FROM a JOIN ONLY b ON a.id = b.id", "SELECT * FROM a, ONLY b",
                "UPDATE a SET x = 1 FROM ONLY b"}) {
            assertFalse(readsTableNamedOnly(sql), sql);
        }
    }

    private static boolean readsTableNamedOnly(String sql) {
        try {
            return new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse(sql)).stream()
                    .anyMatch("only"::equalsIgnoreCase);
        } catch (JSQLParserException exception) {
            return false;
        }
    }

    @Test
    void testExcludeColumnAndPathType() throws JSQLParserException {
        CreateTable createTable = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE mytable (exclude INT, p path, EXCLUDE USING gist (p WITH &&))", true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("exclude", createTable.getColumnDefinitions().get(0).getColumnName());
        assertEquals("path",
                createTable.getColumnDefinitions().get(1).getColDataType().getDataType());
        assertEquals(1, createTable.getTableElements(ExcludeConstraint.class).size());

        // PATH still introduces the path of a JSON_TABLE column without a type
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM JSON_TABLE(doc, '$' COLUMNS (a PATH '$.a', b INT PATH '$.b')) jt",
                true);
    }
}
