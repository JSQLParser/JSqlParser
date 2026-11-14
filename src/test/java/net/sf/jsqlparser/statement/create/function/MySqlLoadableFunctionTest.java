/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.function.LoadableFunction.ReturnType;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL CREATE [AGGREGATE] FUNCTION [IF NOT EXISTS] name RETURNS type SONAME 'library'. */
class MySqlLoadableFunctionTest {
    /** The loadable function is read with these dialects or without one. */
    private static final Dialect[] MYSQL = {Dialect.MYSQL, Dialect.MARIADB, null};
    private static final Dialect[] OTHERS =
            {Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER, Dialect.H2};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertSpans(String expected, String source, ASTNodeAccess access) {
        Node node = access.getASTNode();
        assertNotNull(node, expected);
        assertEquals(expected, source.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    /** Parses, prints through toString and the deparser, reparses and finds no tables. */
    private static CreateFunction roundTrip(String sql, Dialect dialect) throws Exception {
        CreateFunction function = assertInstanceOf(CreateFunction.class, parse(sql, dialect), sql);
        assertNotNull(function.getLoadableFunction(), sql);
        assertEquals(sql, function.toString());
        assertSpans(sql, sql, function);
        StringBuilder output = new StringBuilder();
        function.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        CreateFunction reparsed = assertInstanceOf(CreateFunction.class, parse(sql, dialect));
        assertEquals(function.isAggregate(), reparsed.isAggregate());
        assertEquals(sql, reparsed.toString());
        assertTrue(new TablesNamesFinder<>().getTables((Statement) function).isEmpty());
        return function;
    }

    private static void assertRejected(String sql, Dialect... dialects) {
        for (Dialect dialect : dialects) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect),
                    dialect + ": " + sql);
        }
    }

    @Test
    void aggregateFunctionIsACreateFunction() throws Exception {
        for (Dialect dialect : MYSQL) {
            String sql = "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'";
            CreateFunction function = roundTrip(sql, dialect);
            assertTrue(function.isAggregate());
            assertEquals("FUNCTION", function.getKind());
            LoadableFunction definition = function.getLoadableFunction();
            assertFalse(definition.isIfNotExists());
            assertEquals("f", definition.getName());
            assertEquals(ReturnType.INTEGER, definition.getReturnType());
            assertEquals("INTEGER", definition.getReturnTypeKeyword());
            assertEquals("udf.so", definition.getLibrary().getValue());
            assertSpans("'udf.so'", sql, definition.getLibrary());
            assertEquals(Arrays.asList("f", "RETURNS", "INTEGER", "SONAME", "'udf.so'"),
                    function.getFunctionDeclarationParts());
            assertEquals("f RETURNS INTEGER SONAME 'udf.so'", function.formatDeclaration());
            assertTrue(function.getCharacteristics().isEmpty());
            assertNull(function.getDefiner());

            definition = roundTrip(
                    "CREATE AGGREGATE FUNCTION IF NOT EXISTS f RETURNS REAL SONAME 'udf.so'",
                    dialect).getLoadableFunction();
            assertTrue(definition.isIfNotExists());
            assertEquals(ReturnType.REAL, definition.getReturnType());
        }
    }

    @Test
    void plainLoadableFunctionGetsTheSameDefinition() throws Exception {
        for (Dialect dialect : MYSQL) {
            CreateFunction function = roundTrip(
                    "CREATE FUNCTION IF NOT EXISTS f RETURNS STRING SONAME 'udf.so'", dialect);
            assertFalse(function.isAggregate());
            assertTrue(function.getLoadableFunction().isIfNotExists());
            assertEquals(ReturnType.STRING, function.getLoadableFunction().getReturnType());
            assertEquals(ReturnType.DECIMAL,
                    roundTrip("CREATE FUNCTION f RETURNS DECIMAL SONAME 'udf.so'", dialect)
                            .getLoadableFunction().getReturnType());
        }
    }

    @Test
    void returnTypeSynonymsKeepTheirSpelling() throws Exception {
        for (Dialect dialect : MYSQL) {
            for (String keyword : new String[] {"INT", "INT4", "INTEGER", "integer"}) {
                LoadableFunction definition = roundTrip(
                        "CREATE AGGREGATE FUNCTION f RETURNS " + keyword + " SONAME 'udf.so'",
                        dialect).getLoadableFunction();
                assertEquals(ReturnType.INTEGER, definition.getReturnType());
                assertEquals(keyword, definition.getReturnTypeKeyword());
            }
            assertEquals(ReturnType.DECIMAL,
                    roundTrip("CREATE FUNCTION f RETURNS DEC SONAME 'udf.so'", dialect)
                            .getLoadableFunction().getReturnType());
            assertEquals(ReturnType.STRING,
                    roundTrip("CREATE AGGREGATE FUNCTION f RETURNS STRING SONAME 'udf.so'", dialect)
                            .getLoadableFunction().getReturnType());
        }
    }

    @Test
    void namesAndLibrarySpellings() throws Exception {
        for (Dialect dialect : MYSQL) {
            assertEquals("`my f`",
                    roundTrip("CREATE AGGREGATE FUNCTION `my f` RETURNS INTEGER SONAME 'udf.so'",
                            dialect).getLoadableFunction().getName());
            assertEquals("aggregate",
                    roundTrip("CREATE AGGREGATE FUNCTION aggregate RETURNS INTEGER SONAME 'udf.so'",
                            dialect).getLoadableFunction().getName());
            assertEquals("soname",
                    roundTrip("CREATE FUNCTION soname RETURNS REAL SONAME 'udf.so'", dialect)
                            .getLoadableFunction().getName());
            StringValue library =
                    roundTrip("CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME \"udf.so\"",
                            dialect).getLoadableFunction().getLibrary();
            assertEquals("udf.so", library.getValue());
            assertEquals("\"", library.getQuoteStr());
        }
        Statement statement = parse("create aggregate function f returns integer soname 'udf.so'",
                Dialect.MYSQL);
        assertEquals("CREATE AGGREGATE FUNCTION f RETURNS integer SONAME 'udf.so'",
                statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE AGGREGATE FUNCTION f RETURNS DOUBLE SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS FLOAT SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS VARCHAR(10) SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS NUMERIC SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS FIXED SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS BIGINT SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER(10) SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS DECIMAL(10,2) SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION d.f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME udf",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME N'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME _utf8mb4'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf' '.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 0x7564662e736f",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER",
            "CREATE AGGREGATE FUNCTION f SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f() RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f (a INT) RETURNS INTEGER RETURN 1",
            "CREATE AGGREGATE FUNCTION IF EXISTS f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so' COMMENT 'x'",
            "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so', 'b.so'",
            "CREATE OR REPLACE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE DEFINER = root@localhost FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE PROCEDURE f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE AGGREGATE f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE FUNCTION f RETURNS DOUBLE SONAME 'udf.so'",
            "CREATE FUNCTION f RETURNS VARCHAR(10) SONAME 'udf.so'",
            "CREATE FUNCTION d.f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE FUNCTION f RETURNS INTEGER SONAME udf",
            "CREATE FUNCTION f RETURNS INTEGER SONAME 'udf.so' COMMENT 'x'",
            "CREATE FUNCTION f RETURNS INTEGER",
            "CREATE OR REPLACE FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
            "CREATE FUNCTION IF NOT EXISTS f RETURNS FLOAT SONAME 'udf.so'"})
    void mySqlRejectsWhatTheServerRejects(String sql) {
        assertRejected(sql, Dialect.MYSQL);
    }

    @Test
    void mariaDbKeepsOrReplaceAndItsOtherAggregateForms() throws Exception {
        CreateFunction function = roundTrip(
                "CREATE OR REPLACE AGGREGATE FUNCTION IF NOT EXISTS f RETURNS DEC SONAME 'udf.so'",
                Dialect.MARIADB);
        assertTrue(function.isAggregate());
        function = roundTrip("CREATE OR REPLACE FUNCTION f RETURNS INT SONAME 'udf.so'", null);
        assertFalse(function.isAggregate());
        // not a loadable function: today's reading
        for (Dialect dialect : new Dialect[] {Dialect.MARIADB, null}) {
            assertInstanceOf(UnsupportedStatement.class,
                    parse("CREATE AGGREGATE FUNCTION f(x INT) RETURNS INT RETURN x", dialect));
            assertInstanceOf(UnsupportedStatement.class,
                    parse("CREATE AGGREGATE FUNCTION f RETURNS DOUBLE SONAME 'udf.so'", dialect));
            // The PostgreSQL aggregate parser rejects a definition without its signature.
            assertRejected("CREATE AGGREGATE f RETURNS INTEGER SONAME 'udf.so'", dialect);
            CreateFunction tokens = assertInstanceOf(CreateFunction.class,
                    parse("CREATE FUNCTION f RETURNS DOUBLE SONAME 'udf.so'", dialect));
            assertNull(tokens.getLoadableFunction());
            assertEquals("CREATE FUNCTION f RETURNS DOUBLE SONAME 'udf.so'", tokens.toString());
        }
    }

    @Test
    void otherDialectsKeepTheirReading() throws Exception {
        for (Dialect dialect : OTHERS) {
            String aggregate = "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'";
            if (dialect == Dialect.POSTGRESQL) {
                assertRejected(aggregate, dialect);
            } else {
                Statement statement = parse(aggregate, dialect);
                assertInstanceOf(UnsupportedStatement.class, statement, String.valueOf(dialect));
                assertEquals(aggregate, statement.toString());
            }
            String plain = "CREATE FUNCTION f RETURNS STRING SONAME 'udf.so'";
            if (dialect == Dialect.SQLSERVER) {
                // a SQL Server function needs its parameter parentheses
                assertRejected(plain, dialect);
                continue;
            }
            CreateFunction function = assertInstanceOf(CreateFunction.class, parse(plain, dialect));
            assertNull(function.getLoadableFunction());
            assertFalse(function.isAggregate());
            assertEquals(plain, function.toString());
            assertThrows(UnsupportedOperationException.class,
                    () -> new TablesNamesFinder<>().getTables((Statement) function));
        }
    }

    @Test
    void storedFunctionsAreNotLoadableFunctions() throws Exception {
        for (Dialect dialect : MYSQL) {
            String sql = "CREATE FUNCTION f ( a INT ) RETURNS INT DETERMINISTIC RETURN a + 1";
            CreateFunction function = assertInstanceOf(CreateFunction.class, parse(sql, dialect));
            assertNull(function.getLoadableFunction());
            assertFalse(function.isAggregate());
            assertEquals(sql, function.toString());
            function = assertInstanceOf(CreateFunction.class, parse(
                    "CREATE DEFINER = root@localhost FUNCTION d.f () RETURNS INT RETURN 1",
                    dialect));
            assertNull(function.getLoadableFunction());
            assertNotNull(function.getDefiner());
        }
    }

    @Test
    void statementBoundaries() throws Exception {
        for (Dialect dialect : MYSQL) {
            String script = "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so';"
                    + " CREATE FUNCTION g RETURNS STRING SONAME 'udf.so';"
                    + " DROP FUNCTION IF EXISTS f; SELECT f(a) FROM t";
            Statements statements = dialect == null ? CCJSqlParserUtil.parseStatements(script)
                    : CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(dialect));
            assertEquals(4, statements.size());
            assertTrue(((CreateFunction) statements.get(0)).isAggregate());
            assertEquals("g",
                    ((CreateFunction) statements.get(1)).getLoadableFunction().getName());
            assertInstanceOf(Drop.class, statements.get(2));
            assertEquals("CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
                    statements.get(0).toString());
            assertEquals("CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'",
                    parse("CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so';", dialect)
                            .toString());
        }
    }

    @Test
    void modelBuildsAndReplacesTheDefinition() {
        CreateFunction function = new CreateFunction().withAggregate(true)
                .withLoadableFunction(new LoadableFunction("f", ReturnType.DECIMAL, "'udf.so'")
                        .withIfNotExists(true));
        assertEquals("CREATE AGGREGATE FUNCTION IF NOT EXISTS f RETURNS DECIMAL SONAME 'udf.so'",
                function.toString());
        assertEquals("IF NOT EXISTS f RETURNS DECIMAL SONAME 'udf.so'",
                function.getLoadableFunction().toString());

        LoadableFunction definition = function.getLoadableFunction();
        definition.setReturnTypeKeyword("int");
        assertEquals(ReturnType.INTEGER, definition.getReturnType());
        definition.setReturnType(ReturnType.STRING);
        assertEquals("STRING", definition.getReturnTypeKeyword());
        definition.setLibrary(new StringValue("'other.so'"));
        definition.setName("g");
        definition.setIfNotExists(false);
        assertEquals("CREATE AGGREGATE FUNCTION g RETURNS STRING SONAME 'other.so'",
                function.toString());
        assertThrows(IllegalArgumentException.class,
                () -> definition.setReturnTypeKeyword("DOUBLE"));
        assertThrows(NullPointerException.class, () -> definition.setReturnType(null));
        assertNull(new LoadableFunction().getReturnType());
        assertNull(ReturnType.from("FLOAT"));

        // declaration parts are a snapshot; setting parts replaces the definition
        List<String> parts = new ArrayList<>(function.getFunctionDeclarationParts());
        function.getFunctionDeclarationParts().clear();
        assertEquals(parts, function.getFunctionDeclarationParts());
        function.addFunctionDeclarationParts("COMMENT", "'x'");
        assertNull(function.getLoadableFunction());
        assertEquals("CREATE AGGREGATE FUNCTION g RETURNS STRING SONAME 'other.so' COMMENT 'x'",
                function.toString());
        function.setAggregate(false);
        function.setLoadableFunction(definition);
        assertEquals("CREATE FUNCTION g RETURNS STRING SONAME 'other.so'", function.toString());
        function.setLoadableFunction(null);
        assertNull(function.getFunctionDeclarationParts());
        function.setFunctionDeclarationParts(Arrays.asList("h", "()"));
        assertEquals("CREATE FUNCTION h ()", function.toString());
    }

    @Test
    void visitorsAndValidation() throws Exception {
        String sql = "CREATE AGGREGATE FUNCTION f RETURNS INTEGER SONAME 'udf.so'";
        Statement statement = parse(sql, Dialect.MYSQL);
        List<String> visited = new ArrayList<>();
        statement.accept(new StatementVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(CreateFunctionalStatement createFunctionalStatement, S context) {
                visited.add(createFunctionalStatement.getKind());
                return null;
            }
        }, null);
        assertEquals(Arrays.asList("FUNCTION"), visited);
        assertTrue(Validation.validate(
                Arrays.asList(new FeaturesAllowed(Feature.functionalStatement,
                        Feature.createFunction)),
                sql).isEmpty());
        assertFalse(Validation.validate(Arrays.asList(new FeaturesAllowed(Feature.select)), sql)
                .isEmpty());
    }
}
