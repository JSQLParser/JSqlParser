/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StatementListDeParserTest {
    @Test
    void rendersEmptyListIntoExistingBuilder() {
        StringBuilder output = new StringBuilder("prefix");
        Statements statements = new Statements();
        assertSame(output, statements.accept(new StatementDeParser(output), null));
        assertEquals("prefix", output.toString());
        assertEquals("", statements.toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SELECT 1 | SELECT 1;",
            "SELECT 1; SELECT 2; | SELECT 1; SELECT 2;",
            ";;SELECT 1;;; SELECT 2;; | SELECT 1; SELECT 2;"
    })
    void rendersEveryStatementWithSeparators(String sql, String expected) throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(sql);
        StringBuilder output = new StringBuilder();
        assertSame(output, statements.accept(new StatementDeParser(output), null));
        assertEquals(expected.replace("; ", ";\n") + "\n", output.toString());
        assertEquals(statements.toString(), output.toString());
        assertRoundTrip(statements, output.toString(), null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (id integer); INSERT INTO t VALUES (1); SELECT id FROM t;",
            "BEGIN SELECT 1; END;",
            "BEGIN SELECT 1; END",
            "SELECT 0; BEGIN SELECT 1; END;",
            "IF 1 = 1 SELECT 2; SELECT 3;",
            "IF 1 = 1 SELECT 2; ELSE SELECT 3; SELECT 4;",
            "IF 1 = 1 BEGIN SELECT 2; END ELSE BEGIN SELECT 3; END; SELECT 4;",
            "IF 1 = 1 BEGIN SELECT 2; SELECT 3; END;"
    })
    void preservesCompoundStatementBoundaries(String sql) throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(sql);
        String output = render(statements);
        assertEquals(statements.toString(), output);
        assertRoundTrip(statements, output, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blockOwnsItsEndSemicolon(boolean terminated) throws Exception {
        Block block = new Block().withStatements(CCJSqlParserUtil.parseStatements("SELECT 1;"));
        block.setSemicolonAfterEnd(terminated);
        Statements statements = new Statements();
        statements.add(block);
        statements.add(CCJSqlParserUtil.parse("SELECT 2"));
        String expected = "BEGIN\nSELECT 1;\nEND" + (terminated ? ";" : "") + "\nSELECT 2;\n";
        assertEquals(expected, render(statements));
        assertEquals(expected, statements.toString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rendersBlocksWithoutStatements(boolean terminated) {
        Block block = new Block();
        block.setSemicolonAfterEnd(terminated);
        Statements statements = new Statements();
        statements.add(block);
        String expected = "BEGIN\nEND" + (terminated ? ";" : "") + "\n";
        assertEquals(expected, render(statements));
        block.setStatements(new Statements());
        assertEquals(expected, render(statements));
    }

    @Test
    void nestedBlocksDoNotAddSeparatorsAfterTerminatedChildren() throws Exception {
        Statements body = CCJSqlParserUtil.parseStatements("IF 1 = 1 SELECT 2; ELSE SELECT 3;");
        Block inner = new Block().withStatements(CCJSqlParserUtil.parseStatements("SELECT 4;"));
        inner.setSemicolonAfterEnd(true);
        body.add(inner);
        Block outer = new Block().withStatements(body);
        outer.setSemicolonAfterEnd(true);
        Statements statements = new Statements();
        statements.add(outer);
        statements.add(CCJSqlParserUtil.parse("SELECT 5"));
        String expected = "BEGIN\nIF 1 = 1 SELECT 2; ELSE SELECT 3;\n"
                + "BEGIN\nSELECT 4;\nEND;\nEND;\nSELECT 5;\n";
        assertEquals(expected, render(statements));
        assertEquals(expected, statements.toString());
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void ifElseOwnsItsBranchSemicolons(boolean thenTerminated, boolean elseTerminated)
            throws Exception {
        IfElseStatement conditional = new IfElseStatement(
                CCJSqlParserUtil.parseCondExpression("1 = 1"), CCJSqlParserUtil.parse("SELECT 2"));
        conditional.setElseStatement(CCJSqlParserUtil.parse("SELECT 3"));
        conditional.setUsingSemicolonForIfStatement(thenTerminated);
        conditional.setUsingSemicolonForElseStatement(elseTerminated);
        Statements statements = new Statements();
        statements.add(conditional);
        String expected = "IF 1 = 1 SELECT 2" + (thenTerminated ? ";" : "")
                + " ELSE SELECT 3" + (elseTerminated ? ";" : "") + "\n";
        assertEquals(expected, render(statements));
        assertEquals(expected, statements.toString());
    }

    @Test
    void appliesCustomVisitorsInsideBranchesAndNestedListsWithContext() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "IF flag = 1 BEGIN SELECT 2; END ELSE BEGIN SELECT 3; END; SELECT 4;");
        String original = statements.toString();
        Object context = new Object();
        List<EqualsTo> visitedConditions = new ArrayList<>();
        List<Long> visitedValues = new ArrayList<>();
        List<Select> visitedSelects = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(EqualsTo condition, S suppliedContext) {
                assertSame(context, suppliedContext);
                visitedConditions.add(condition);
                return getBuilder().append("flag = 11");
            }

            @Override
            public <S> StringBuilder visit(LongValue value, S suppliedContext) {
                visitedValues.add(value.getValue());
                return getBuilder().append(value.getValue() + 10);
            }
        };
        StatementDeParser deparser =
                new StatementDeParser(expressions, new SelectDeParser(), output) {
                    @Override
                    public <S> StringBuilder visit(Select select, S suppliedContext) {
                        assertSame(context, suppliedContext);
                        visitedSelects.add(select);
                        return super.visit(select, suppliedContext);
                    }
                };
        assertSame(output, statements.accept(deparser, context));
        assertEquals(1, visitedConditions.size());
        assertSame(((IfElseStatement) statements.get(0)).getCondition(), visitedConditions.get(0));
        assertEquals(List.of(2L, 3L, 4L), visitedValues);
        assertEquals(3, visitedSelects.size());
        assertEquals(
                "IF flag = 11 BEGIN\nSELECT 12;\nEND ELSE BEGIN\nSELECT 13;\nEND;;\nSELECT 14;\n",
                output.toString());
        assertEquals(original, statements.toString());
        assertEquals(2, CCJSqlParserUtil.parseStatements(output.toString()).size());
    }

    @Test
    void appendsToExistingBuilderAndKeepsVisitorStateAcrossCalls() throws Exception {
        StringBuilder output = new StringBuilder("-- prefix\n");
        StatementDeParser deparser = new StatementDeParser(output);
        CCJSqlParserUtil.parseStatements("SELECT 1;").accept(deparser, "first");
        CCJSqlParserUtil.parseStatements("SELECT 2;").accept(deparser, "second");
        assertEquals("-- prefix\nSELECT 1;\nSELECT 2;\n", output.toString());
    }

    @Test
    void rendersUnsupportedStatementsWithoutDroppingAdjacentStatements() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "select 1; select from; select 2;", parser -> parser.withUnsupportedStatements());
        assertInstanceOf(UnsupportedStatement.class, statements.get(1));
        assertEquals("SELECT 1;\nselect from;\nSELECT 2;\n", render(statements));
    }

    @Test
    void preservesErrorRecoveryPlaceholders() throws Exception {
        Statements statements = new CCJSqlParser("SELECT 1; SELECT FROM; SELECT 2;")
                .withErrorRecovery().Statements();
        assertEquals(3, statements.size());
        assertNull(statements.get(1));
        assertEquals("SELECT 1;\nnull;\nSELECT 2;\n", render(statements));
        assertEquals(statements.toString(), render(statements));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "MYSQL | CREATE TABLE t (id int); INSERT INTO t VALUES (1); SELECT id FROM t; | 3",
            "POSTGRESQL | CREATE FUNCTION f() RETURNS int AS $f$BEGIN RETURN 1; END;$f$ LANGUAGE plpgsql; SELECT 2; | 2",
            "POSTGRESQL | DO $$BEGIN PERFORM 1; END;$$; SELECT 2; | 2",
            "ORACLE | BEGIN NULL; END; SELECT 2 FROM dual; | 2",
            "SQLSERVER | IF 1 = 1 BEGIN SELECT 2; END; SELECT 3; | 2"
    })
    void preservesDialectStatementsAndOpaqueBodies(Dialect dialect, String sql, int count)
            throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(sql,
                parser -> parser.withDialect(dialect));
        assertEquals(count, statements.size());
        String output = render(statements);
        assertEquals(statements.toString(), output);
        assertRoundTrip(statements, output, dialect);
    }

    private static String render(Statements statements) {
        StringBuilder output = new StringBuilder();
        statements.accept(new StatementDeParser(output), null);
        return output.toString();
    }

    private static void assertRoundTrip(Statements original, String sql, Dialect dialect)
            throws Exception {
        Statements reparsed = CCJSqlParserUtil.parseStatements(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
        assertEquals(original.size(), reparsed.size());
        for (int i = 0; i < original.size(); i++) {
            Statement expected = original.get(i);
            assertEquals(expected.getClass(), reparsed.get(i).getClass());
            assertEquals(expected.toString(), reparsed.get(i).toString());
        }
    }
}
