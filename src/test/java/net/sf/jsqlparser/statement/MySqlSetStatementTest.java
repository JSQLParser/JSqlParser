/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.SetStatement.AssignmentOperator;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySqlSetStatementTest {
    private SetStatement parse(String sql) throws Exception {
        return (SetStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET @a := 1", "SET @a=1, @b:=2", "SET @a:=1, @b=2, @c:=3",
            "SET @@session.sql_mode := ''", "SET SESSION sql_mode = ''",
            "SET @a = (@b := 2), @c := 3", "SET CHARACTER SET utf8mb4",
            "SET CHARACTER SET 'utf8mb4'", "SET CHARACTER SET DEFAULT", "SET CHARSET utf8mb4",
            "SET CHARSET DEFAULT", "SET NAMES utf8mb4 COLLATE utf8mb4_bin",
            "SET CHARSET DEFAULT, @a=1", "SET @a=1, CHARACTER SET utf8mb4"})
    void preservesSetSyntax(String sql) throws Exception {
        SetStatement statement = parse(sql);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(statement.toString(), parse(output.toString()).toString());
    }

    @Test
    void exposesEachAssignmentAndOperator() throws Exception {
        SetStatement statement = parse("SET @a=1, @b:=2, @c=3");
        assertEquals(3, statement.getCount());
        assertEquals("@b", statement.getName(1).toString());
        assertEquals(AssignmentOperator.COLON_EQUALS, statement.getAssignmentOperator(1));
        assertEquals(1, statement.getExpressions(1).size());
        assertInstanceOf(LongValue.class, statement.getExpressions(1).get(0));
        statement.setExpressions(1, new ExpressionList<>(new LongValue(9)));
        statement.setAssignmentOperator(2, AssignmentOperator.COLON_EQUALS);
        assertEquals("SET @a = 1, @b := 9, @c := 3", statement.toString());
        statement.setUseEqual(1, true);
        assertEquals(AssignmentOperator.EQUALS, statement.getAssignmentOperator(1));
        assertTrue(statement.isUseEqual(1));
        statement.remove(0);
        assertEquals("SET @b = 9, @c := 3", statement.toString());
    }

    @Test
    void exposesCharsetSelectionAlongsideAssignments() throws Exception {
        SetStatement statement = parse("SET @a=1, CHARACTER SET utf8mb4, @b:=2");
        assertEquals(3, statement.getCount());
        assertEquals("CHARACTER SET", statement.getName(1));
        assertEquals(AssignmentOperator.NONE, statement.getAssignmentOperator(1));
        assertEquals(AssignmentOperator.COLON_EQUALS, statement.getAssignmentOperator(2));
    }

    @Test
    void keepsNestedAssignmentInItsValue() throws Exception {
        SetStatement statement = parse("SET @a = (@b := 2), @c := 3");
        assertEquals(2, statement.getCount());
        assertEquals("@a", statement.getName().toString());
        assertEquals(1, statement.getExpressions().size());
        assertEquals("(@b := 2)", statement.getExpressions().get(0).toString());
    }

    @Test
    void retainsPostgresqlValueListsAndLegacyApi() throws Exception {
        SetStatement statement = (SetStatement) CCJSqlParserUtil.parse(
                "SET search_path = public, extensions", p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(1, statement.getCount());
        assertEquals(2, statement.getExpressions().size());
        assertEquals("SET search_path = public, extensions", statement.toString());
        SetStatement legacy = new SetStatement("x", new ExpressionList<>(new LongValue(1)));
        assertEquals(AssignmentOperator.EQUALS, legacy.getAssignmentOperator());
        legacy.setUseEqual(false);
        assertEquals("SET x 1", legacy.toString());
        legacy.setAssignmentOperator(AssignmentOperator.COLON_EQUALS);
        assertEquals("SET x := 1", legacy.toString());
    }

    @Test
    void visitsAllAssignmentValues() throws Exception {
        List<Long> values = new java.util.ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                values.add(value.getValue());
                return getBuilder().append(value.getValue() + 10);
            }
        };
        StringBuilder output = new StringBuilder();
        parse("SET @a:=1, @b=2").accept(
                new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(List.of(1L, 2L), values);
        assertEquals("SET @a := 11, @b = 12", output.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET @a :=", "SET @a := 1,", "SET CHARACTER SET",
            "SET CHARACTER SET utf8mb4 COLLATE utf8mb4_bin", "SET CHARSET DEFAULT,"})
    void rejectsIncompleteAssignmentsAndInvalidCharsetClauses(String sql) {
        assertThrows(Exception.class, () -> parse(sql));
    }
}
