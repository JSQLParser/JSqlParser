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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.SetStatement.AssignmentOperator;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlSetToTest {
    private static SetStatement parse(String sql) throws JSQLParserException {
        return (SetStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET search_path TO my_schema, public",
            "SET LOCAL statement_timeout TO 5000", "SET SESSION application_name TO 'worker'",
            "SET datestyle TO postgres, dmy", "SET client_encoding TO DEFAULT",
            "SET app.user_name TO 'reader'", "SET \"TimeZone\" TO 'UTC'",
            "SET standard_conforming_strings TO on", "SET enable_seqscan TO off",
            "SET enable_hashjoin TO true", "SET enable_mergejoin TO false",
            "SET search_path TO off, on"})
    void parsesScopeNamesAndValueLists(String sql) throws Exception {
        SetStatement set = parse(sql);
        assertEquals(1, set.getCount());
        assertEquals(AssignmentOperator.TO, set.getAssignmentOperator());
        assertFalse(set.isUseEqual());
        assertRoundTrip(set, sql);
        SetStatement generic = (SetStatement) CCJSqlParserUtil.parse(sql);
        assertEquals(AssignmentOperator.TO, generic.getAssignmentOperator());
    }

    @Test
    void distinguishesOneValueListFromSeveralAssignments() throws Exception {
        SetStatement set = parse("SET LOCAL search_path TO my_schema, public, '$user'");
        assertEquals("LOCAL", set.getEffectParameter());
        assertEquals("search_path", set.getName());
        assertEquals(1, set.getCount());
        assertEquals(3, set.getExpressions().size());
        assertEquals("my_schema", set.getExpressions().get(0).toString());
        assertEquals("public", set.getExpressions().get(1).toString());
        assertEquals("$user", ((StringValue) set.getExpressions().get(2)).getValue());
    }

    @Test
    void keepsLegacyAssignmentApiAndSeparators() throws Exception {
        SetStatement constructed = new SetStatement("x", new ExpressionList<>(new LongValue(1)));
        assertTrue(constructed.isUseEqual());
        constructed.setAssignmentOperator(AssignmentOperator.TO);
        assertRoundTrip(constructed, "SET x TO 1");
        assertFalse(constructed.isUseEqual());
        constructed.setUseEqual(true);
        assertEquals("SET x = 1", constructed.toString());
        constructed.setUseEqual(false);
        assertEquals("SET x 1", constructed.toString());
        SetStatement assignments = (SetStatement) CCJSqlParserUtil.parse("SET @a=1, @b:=2, @c=3",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals(3, assignments.getCount());
        assertEquals(AssignmentOperator.EQUALS, assignments.getAssignmentOperator(0));
        assertEquals(AssignmentOperator.COLON_EQUALS, assignments.getAssignmentOperator(1));
        assertEquals("SET @a = 1, @b := 2, @c = 3", assignments.toString());
        assertRoundTrip(parse("SET TIME ZONE 'UTC'"), "SET Time Zone 'UTC'");
    }

    @Test
    void keepsExistingOnExpressionsOutsideToAssignments() throws Exception {
        for (String sql : List.of("SET v = on + 1", "SET v = 1, on")) {
            for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MYSQL, Dialect.SQLSERVER)) {
                SetStatement set = (SetStatement) CCJSqlParserUtil.parse(sql,
                        p -> p.withDialect(dialect));
                assertEquals(AssignmentOperator.EQUALS, set.getAssignmentOperator());
                assertRoundTrip(set, sql);
            }
        }
    }

    @Test
    void visitsValuesInToEqualsAndSpaceForms() throws Exception {
        List<Long> values = new ArrayList<>();
        StatementVisitorAdapter<Void> visitor = new StatementVisitorAdapter<>(
                new SelectVisitorAdapter<>(new ExpressionVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(LongValue value, S context) {
                        values.add(value.getValue());
                        return null;
                    }
                }));
        parse("SET x TO 1, 2").accept(visitor, null);
        CCJSqlParserUtil.parse("SET x = 3, y 4").accept(visitor, null);
        assertEquals(List.of(1L, 2L, 3L, 4L), values);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 10);
            }
        };
        parse("SET x TO 1, 2").accept(
                new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals("SET x TO 11, 12", output.toString());
    }

    @Test
    void retainsFollowingStatements() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SET LOCAL search_path TO my_schema, public; SELECT 1; SET x = 3;",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(3, statements.size());
        assertEquals(AssignmentOperator.TO,
                ((SetStatement) statements.get(0)).getAssignmentOperator());
        assertEquals(AssignmentOperator.EQUALS,
                ((SetStatement) statements.get(2)).getAssignmentOperator());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET search_path TO", "SET search_path TO public,",
            "SET x TO = 1", "SET x TO 1, y TO 2", "SET x = 1, y TO 2",
            "SET @x TO 1", "SET TIME ZONE TO 'UTC'"})
    void rejectsMalformedOrMultipleParameterToAssignments(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void doesNotEnableToForExplicitMySqlDialect() {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("SET x TO 1",
                p -> p.withDialect(Dialect.MYSQL)));
    }

    private static void assertRoundTrip(SetStatement set, String expected) throws Exception {
        assertEquals(expected, set.toString());
        StringBuilder output = new StringBuilder();
        set.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        SetStatement reparsed = parse(output.toString());
        assertEquals(set.getAssignmentOperator(), reparsed.getAssignmentOperator());
        assertEquals(set.getEffectParameter(), reparsed.getEffectParameter());
        assertEquals(set.getName(), reparsed.getName());
        assertEquals(set.getExpressions().toString(), reparsed.getExpressions().toString());
    }
}
