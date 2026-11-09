/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.load;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ExtensionStatement;
import net.sf.jsqlparser.statement.MySqlFileFormat;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlLoadDataStatementTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> MARIADB =
            parser -> parser.withDialect(Dialect.MARIADB);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "LOAD DATA INFILE '/tmp/a.csv' INTO TABLE t",
            "LOAD DATA LOCAL INFILE 'a.csv' REPLACE INTO TABLE db1.t PARTITION (p0, p1) "
                    + "CHARACTER SET utf8mb4 FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' "
                    + "LINES STARTING BY 'x' TERMINATED BY '\\n' IGNORE 1 LINES (a, @b) "
                    + "SET c = @b + 1, d = DEFAULT",
            "LOAD DATA LOW_PRIORITY INFILE 'a' IGNORE INTO TABLE t COLUMNS TERMINATED BY '\\t' "
                    + "IGNORE 2 ROWS",
            "LOAD DATA CONCURRENT INFILE 'a' INTO TABLE t LINES TERMINATED BY '\\r\\n'",
            "LOAD DATA CONCURRENT LOCAL INFILE 'a' INTO TABLE t",
            "LOAD DATA LOW_PRIORITY LOCAL INFILE 'a' IGNORE INTO TABLE t",
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS ENCLOSED BY '\"' ESCAPED BY '\\\\'",
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS ESCAPED BY '' LINES STARTING BY ''",
            "LOAD DATA INFILE 'a' INTO TABLE t LINES STARTING BY 'x'",
            "LOAD DATA INFILE 'a' INTO TABLE t CHARSET latin1",
            "LOAD DATA INFILE 'a' INTO TABLE t CHARACTER SET 'utf8mb4'",
            "LOAD DATA INFILE 'a' INTO TABLE t CHARACTER SET binary",
            "LOAD DATA INFILE 'a' INTO TABLE t ()",
            "LOAD DATA INFILE 'a' INTO TABLE db1.t (t.a, db1.t.b, @c, @`d e`)",
            "LOAD DATA INFILE 'a' INTO TABLE t SET a = (SELECT MAX(b) FROM u), "
                    + "c = CURRENT_TIMESTAMP",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 0 LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 2147483647 LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t ROWS IDENTIFIED BY '<r>'",
            "LOAD XML LOCAL INFILE 'a.xml' INTO TABLE t ROWS IDENTIFIED BY '<row>'",
            "LOAD XML INFILE 'a.xml' REPLACE INTO TABLE t CHARACTER SET latin1 "
                    + "ROWS IDENTIFIED BY '<r>' IGNORE 1 ROWS (a, @x) SET b = @x",
            "LOAD XML LOW_PRIORITY INFILE 'a.xml' IGNORE INTO TABLE t PARTITION (p0)",
            "LOAD XML INFILE 'a.xml' INTO TABLE t FIELDS TERMINATED BY ',' "
                    + "LINES TERMINATED BY '\\n'"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, MARIADB, NO_DIALECT)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(MySqlLoadDataStatement.class, statement);
            assertEquals(sql, deparse(statement));
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void keepsEveryClause() throws JSQLParserException {
        MySqlLoadDataStatement load = parse("LOAD DATA LOW_PRIORITY LOCAL INFILE 'a.csv' "
                + "REPLACE INTO TABLE db1.t PARTITION (p0, p1) CHARSET 'utf8mb4' "
                + "COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' ESCAPED BY '^' "
                + "LINES STARTING BY 'x' TERMINATED BY ';' IGNORE 3 ROWS (a, @b, t.c) "
                + "SET d = @b + 1, e = DEFAULT", MYSQL);

        assertEquals(MySqlLoadDataStatement.Format.DATA, load.getFormat());
        assertEquals(MySqlLoadDataStatement.LockOption.LOW_PRIORITY, load.getLockOption());
        assertTrue(load.isLocal());
        assertEquals("a.csv", load.getFileName().getValue());
        assertEquals(CreateTable.DuplicateHandling.REPLACE, load.getDuplicateHandling());
        assertEquals("db1.t", load.getTable().getFullyQualifiedName());
        assertEquals(Arrays.asList("p0", "p1"), load.getPartitions());
        assertEquals("'utf8mb4'", load.getCharacterSet());
        assertTrue(load.isUseCharsetKeyword());
        assertNull(load.getRowsIdentifiedBy());

        MySqlFileFormat format = load.getFileFormat();
        assertEquals(MySqlFileFormat.FieldsKeyword.COLUMNS, format.getFieldsKeyword());
        assertEquals(",", format.getFieldsTerminatedBy().getValue());
        assertTrue(format.isFieldsOptionallyEnclosed());
        assertEquals("\"", format.getFieldsEnclosedBy().getValue());
        assertEquals("^", format.getFieldsEscapedBy().getValue());
        assertEquals("x", format.getLinesStartingBy().getValue());
        assertEquals(";", format.getLinesTerminatedBy().getValue());

        assertEquals(3L, load.getIgnoreCount());
        assertEquals(MySqlLoadDataStatement.IgnoreUnit.ROWS, load.getIgnoreUnit());
        assertEquals(3, load.getColumns().size());
        assertInstanceOf(Column.class, load.getColumns().get(0));
        assertEquals("b", ((UserVariable) load.getColumns().get(1)).getName());
        assertEquals("t.c", ((Column) load.getColumns().get(2)).getFullyQualifiedName());
        assertEquals(2, load.getUpdateSets().size());
        assertEquals("d", load.getUpdateSets().get(0).getColumn(0).getColumnName());
        assertEquals("@b + 1", load.getUpdateSets().get(0).getValue(0).toString());
        assertEquals("DEFAULT", load.getUpdateSets().get(1).getValue(0).toString());
    }

    @Test
    void readsLoadXmlAndItsRowTag() throws JSQLParserException {
        MySqlLoadDataStatement load = parse(
                "LOAD XML CONCURRENT INFILE 'a.xml' IGNORE INTO TABLE t ROWS IDENTIFIED BY '<r>'",
                MYSQL);
        assertEquals(MySqlLoadDataStatement.Format.XML, load.getFormat());
        assertEquals(MySqlLoadDataStatement.LockOption.CONCURRENT, load.getLockOption());
        assertFalse(load.isLocal());
        assertEquals(CreateTable.DuplicateHandling.IGNORE, load.getDuplicateHandling());
        assertEquals("<r>", load.getRowsIdentifiedBy().getValue());
        assertNull(load.getPartitions());
        assertNull(load.getCharacterSet());
        assertNull(load.getFileFormat());
        assertNull(load.getIgnoreCount());
        assertNull(load.getColumns());
        assertNull(load.getUpdateSets());
    }

    @Test
    void keepsAnEmptyColumnListApartFromNone() throws JSQLParserException {
        assertTrue(parse("LOAD DATA INFILE 'a' INTO TABLE t ()", MYSQL).getColumns().isEmpty());
        assertNull(parse("LOAD DATA INFILE 'a' INTO TABLE t", MYSQL).getColumns());
    }

    @Test
    void acceptsSubclausesInAnyOrderAndKeepsTheLastValue() throws JSQLParserException {
        // the server merges repeated subclauses, the last one winning, and keeps OPTIONALLY
        assertNormalized(
                "LOAD DATA INFILE 'a' INTO TABLE t COLUMNS OPTIONALLY ENCLOSED BY '\"' "
                        + "TERMINATED BY ','",
                "LOAD DATA INFILE 'a' INTO TABLE t COLUMNS TERMINATED BY ',' "
                        + "OPTIONALLY ENCLOSED BY '\"'");
        assertNormalized(
                "LOAD DATA INFILE 'a' INTO TABLE t FIELDS ESCAPED BY '^' TERMINATED BY ',' "
                        + "TERMINATED BY ';' LINES TERMINATED BY '\\n' STARTING BY '>'",
                "LOAD DATA INFILE 'a' INTO TABLE t FIELDS TERMINATED BY ';' ESCAPED BY '^' "
                        + "LINES STARTING BY '>' TERMINATED BY '\\n'");
        assertNormalized(
                "LOAD DATA INFILE 'a' INTO TABLE t FIELDS OPTIONALLY ENCLOSED BY '\"' "
                        + "ENCLOSED BY '|'",
                "LOAD DATA INFILE 'a' INTO TABLE t FIELDS OPTIONALLY ENCLOSED BY '|'");
        assertNormalized("load data local infile 'a' replace into table t fields terminated by "
                + "',' ignore 1 lines (a) set b = 1",
                "LOAD DATA LOCAL INFILE 'a' REPLACE INTO TABLE t FIELDS TERMINATED BY ',' "
                        + "IGNORE 1 LINES (a) SET b = 1");
        assertNormalized("LOAD DATA INFILE 'a' INTO TABLE t IGNORE 007 LINES",
                "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 7 LINES");
    }

    @Test
    void readsDoubleQuotedStringsInMySql() throws JSQLParserException {
        MySqlLoadDataStatement load = parse(
                "LOAD DATA INFILE \"a.csv\" INTO TABLE t FIELDS ENCLOSED BY \"'\"", MYSQL);
        assertEquals("a.csv", load.getFileName().getValue());
        assertEquals("'", load.getFileFormat().getFieldsEnclosedBy().getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS",
            "LOAD DATA INFILE 'a' INTO TABLE t LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t COLUMNS",
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS LINES TERMINATED BY '\\n'",
            "LOAD DATA INFILE 'a' INTO TABLE t LINES TERMINATED BY '\\n' FIELDS TERMINATED BY ','",
            "LOAD DATA INFILE 'a' INTO TABLE t CHARACTER SET utf8mb4 PARTITION (p0)",
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS TERMINATED BY ',' ROWS IDENTIFIED BY '<r>'",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 1 LINES FIELDS TERMINATED BY ','",
            "LOAD DATA INFILE 'a' INTO TABLE t SET a = 1 (a)",
            "LOAD DATA INFILE 'a' INTO t",
            "LOAD DATA 'a' INTO TABLE t",
            "LOAD DATA LOCAL LOW_PRIORITY INFILE 'a' INTO TABLE t",
            "LOAD DATA LOW_PRIORITY CONCURRENT INFILE 'a' INTO TABLE t",
            "LOAD DATA INFILE 'a' REPLACE IGNORE INTO TABLE t",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 1",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 2147483648 LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE 99999999999999999999 LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t IGNORE -1 LINES",
            "LOAD DATA INFILE 'a' INTO TABLE t (a, @@b)",
            "LOAD DATA INFILE 'a' INTO TABLE t SET @x = 1",
            "LOAD DATA INFILE 'a' INTO TABLE t SET (a, b) = (1, 2)",
            "LOAD DATA INFILE a INTO TABLE t",
            "LOAD DATA INFILE 'a' INTO TABLE t FIELDS TERMINATED ','",
            "LOAD DATA INFILE 'a' INTO TABLE t PARTITION ()",
            "LOAD XML INFILE 'a' INTO TABLE t ROWS IDENTIFIED '<r>'",
            "LOAD DATA INFILE 'a' INTO TABLE t SET a = 1,",
            "LOAD DATA INFILE 'a' INTO TABLE t (a,)"})
    void rejectsWhatMySqlRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOAD DATA INFILE 'a' INTO TABLE t",
            "LOAD XML INFILE 'a.xml' INTO TABLE t",
            "LOAD DATA LOCAL INFILE 'a' INTO TABLE t"})
    void otherDialectsKeepRejectingMySqlForms(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.ORACLE,
                Dialect.SQLSERVER, Dialect.BIGQUERY}) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                    dialect.name());
        }
    }

    @Test
    void otherLoadStatementsKeepTheirReading() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, NO_DIALECT)) {
            // DuckDB's LOAD of an extension named xml or data stays an extension statement
            ExtensionStatement xml = (ExtensionStatement) CCJSqlParserUtil.parse("LOAD xml",
                    dialect);
            assertEquals("xml", xml.getExtensionName());
            assertInstanceOf(ExtensionStatement.class,
                    CCJSqlParserUtil.parse("LOAD httpfs", dialect));
            assertInstanceOf(LoadDataStatement.class, CCJSqlParserUtil.parse(
                    "LOAD DATA INTO ds.t FROM FILES (format = 'CSV')", dialect));
        }
        ExtensionStatement library = (ExtensionStatement) CCJSqlParserUtil.parse(
                "LOAD 'auto_explain'", parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("'auto_explain'", library.getExtensionName());
        assertInstanceOf(LoadDataStatement.class,
                CCJSqlParserUtil.parse("LOAD DATA OVERWRITE ds.t FROM FILES (format = 'CSV') "
                        + "WITH CONNECTION p.us.c",
                        parser -> parser.withDialect(Dialect.BIGQUERY)));
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        // CONCURRENT, XML and CHARSET are names in MySQL too
        for (String sql : new String[] {"CREATE TABLE concurrent (xml INT, charset INT)",
                "SELECT concurrent, xml, charset FROM t"}) {
            assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
        }
        MySqlLoadDataStatement load =
                parse("LOAD DATA INFILE 'a' INTO TABLE concurrent (xml, charset)", MYSQL);
        assertEquals("concurrent", load.getTable().getName());

        // MySQL reserves INFILE, but the parser adds no reserved word
        for (String sql : new String[] {"CREATE TABLE infile (a INT)", "SELECT infile FROM t",
                "LOAD DATA INFILE 'a' INTO TABLE infile (infile)"}) {
            assertSqlCanBeParsedAndDeparsed(sql, false, NO_DIALECT);
        }
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, NO_DIALECT)) {
            Statements statements = CCJSqlParserUtil.parseStatements(
                    "SELECT 1; LOAD DATA INFILE 'a' INTO TABLE t (a) SET b = 1; "
                            + "LOAD XML INFILE 'b' INTO TABLE u;\nLOAD xml; SELECT 2",
                    dialect);
            assertEquals(List.of("PlainSelect", "MySqlLoadDataStatement",
                    "MySqlLoadDataStatement", "ExtensionStatement", "PlainSelect"),
                    statements.stream().map(statement -> statement.getClass().getSimpleName())
                            .collect(Collectors.toList()));
        }
    }

    @Test
    void isAStatementStartForUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "FOO BAR; LOAD DATA INFILE 'a' INTO TABLE t; BAZ",
                parser -> parser.withDialect(Dialect.MYSQL).withUnsupportedStatements(true));
        assertEquals(3, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        assertInstanceOf(MySqlLoadDataStatement.class, statements.get(1));
        assertInstanceOf(UnsupportedStatement.class, statements.get(2));
    }

    @Test
    void findsTheTargetAndTheTablesOfSubqueries() throws JSQLParserException {
        Statement load = CCJSqlParserUtil.parse("LOAD DATA INFILE 'a' INTO TABLE db1.t (a, @b) "
                + "SET c = (SELECT MAX(x) FROM u WHERE u.k = @b)");
        assertThat(new TablesNamesFinder<Void>().getTables(load))
                .containsExactlyInAnyOrder("db1.t", "u");
        assertThat(new TablesNamesFinder<Void>().getTables(
                CCJSqlParserUtil.parse("LOAD XML INFILE 'a' INTO TABLE t"))).containsExactly("t");
    }

    @Test
    void reportsDataFeatures() throws JSQLParserException {
        Statement plain = CCJSqlParserUtil.parse("LOAD DATA INFILE 'a' INTO TABLE t");
        assertThat(plain.getFeatures().getCertain()).containsExactly(StmtFeature.MODIFIES_DATA);

        Statement subquery = CCJSqlParserUtil.parse(
                "LOAD DATA INFILE 'a' INTO TABLE t SET c = (SELECT MAX(x) FROM u)");
        assertThat(subquery.getFeatures().getCertain())
                .contains(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA);
    }

    @Test
    void adapterVisitsTheTableColumnsAndAssignments() throws JSQLParserException {
        Statement load = CCJSqlParserUtil.parse(
                "LOAD DATA INFILE 'a' INTO TABLE t (a, @b) SET c = @b + 1");
        Object context = new Object();
        List<String> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S value) {
                visited.add(column.getColumnName());
                return null;
            }

            @Override
            public <S> Void visit(UserVariable variable, S value) {
                visited.add(variable.toString());
                return null;
            }

            @Override
            public <S> Void visit(LongValue longValue, S value) {
                visited.add(longValue.getStringValue());
                return null;
            }
        };
        FromItemVisitorAdapter<Void> tables = new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S value) {
                visited.add("table " + table.getName());
                return null;
            }
        };
        load.accept(new StatementVisitorAdapter<Void>(expressions, null, null, tables, null, null),
                context);
        assertEquals(List.of("table t", "a", "@b", "c", "@b", "1"), visited);
        assertNull(load.accept(new StatementVisitorAdapter<>(), null));
    }

    @Test
    void nodesSpanTheStatementAndItsClauses() throws JSQLParserException {
        String sql = "LOAD DATA INFILE 'a' INTO TABLE t CHARSET latin1 "
                + "FIELDS TERMINATED BY ',' LINES TERMINATED BY ';' IGNORE 1 LINES (a, @b) "
                + "SET c = @b";
        MySqlLoadDataStatement load = parse(sql, MYSQL);
        assertEquals(sql, text(sql, load));
        assertEquals("'a'", text(sql, load.getFileName()));
        assertEquals("t", text(sql, load.getTable()));
        assertEquals("FIELDS TERMINATED BY ',' LINES TERMINATED BY ';'",
                text(sql, load.getFileFormat()));
        assertEquals("','", text(sql, load.getFileFormat().getFieldsTerminatedBy()));
        assertEquals("a, @b", text(sql, load.getColumns()));
        assertEquals("@b", text(sql, (ASTNodeAccess) load.getUpdateSets().get(0).getValues()));

        String xml = "LOAD XML INFILE 'a' INTO TABLE t LINES STARTING BY '<'";
        MySqlLoadDataStatement lines = parse(xml, MYSQL);
        assertEquals("LINES STARTING BY '<'", text(xml, lines.getFileFormat()));
    }

    @Test
    void buildsStatementsInCode() {
        MySqlLoadDataStatement load = new MySqlLoadDataStatement()
                .withFileName(new StringValue("data.csv"))
                .withTable(new Table("db1", "t"));
        assertEquals("LOAD DATA INFILE 'data.csv' INTO TABLE db1.t", load.toString());

        ExpressionList<Expression> columns = new ExpressionList<>();
        columns.add(new Column("a"));
        columns.add(new UserVariable("b"));
        load.withFormat(MySqlLoadDataStatement.Format.XML)
                .withLockOption(MySqlLoadDataStatement.LockOption.CONCURRENT)
                .withLocal(true)
                .withDuplicateHandling(CreateTable.DuplicateHandling.IGNORE)
                .withPartitions(Arrays.asList("p0", "p1"))
                .withCharacterSet("utf8mb4")
                .withUseCharsetKeyword(true)
                .withRowsIdentifiedBy(new StringValue("<row>"))
                .withFileFormat(new MySqlFileFormat()
                        .withFieldsTerminatedBy(new StringValue(","))
                        .withLinesTerminatedBy(new StringValue("\\n")))
                .withIgnoreCount(1L)
                .withIgnoreUnit(MySqlLoadDataStatement.IgnoreUnit.ROWS)
                .withColumns(columns)
                .withUpdateSets(List.of(new UpdateSet(new Column("c"), new UserVariable("b"))));
        String expected = "LOAD XML CONCURRENT LOCAL INFILE 'data.csv' IGNORE INTO TABLE db1.t "
                + "PARTITION (p0, p1) CHARSET utf8mb4 ROWS IDENTIFIED BY '<row>' "
                + "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 ROWS (a, @b) "
                + "SET c = @b";
        assertEquals(expected, load.toString());
        assertEquals(expected, deparse(load));

        load.setFileFormat(new MySqlFileFormat());
        load.setUseCharsetKeyword(false);
        assertEquals("LOAD XML CONCURRENT LOCAL INFILE 'data.csv' IGNORE INTO TABLE db1.t "
                + "PARTITION (p0, p1) CHARACTER SET utf8mb4 ROWS IDENTIFIED BY '<row>' "
                + "IGNORE 1 ROWS (a, @b) SET c = @b", load.toString());
    }

    private static void assertNormalized(String sql, String expected) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, NO_DIALECT)) {
            Statement statement = CCJSqlParserUtil.parse(sql, dialect);
            assertEquals(expected, statement.toString());
            assertEquals(expected, deparse(statement));
            assertEquals(expected, CCJSqlParserUtil.parse(expected, dialect).toString());
        }
    }

    private static MySqlLoadDataStatement parse(String sql, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        return (MySqlLoadDataStatement) assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    private static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}
