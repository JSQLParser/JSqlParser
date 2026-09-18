/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TemporalExpressionInfo.Kind;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TemporalExpressionInfoTest {

    // Accepted forms from an execution matrix on MySQL 8.4.11 and PostgreSQL 18.6.
    @ParameterizedTest
    @CsvFileSource(resources = "temporal-expression-cases.tsv", delimiter = '\t')
    void recognizesServerAcceptedFormsWithoutChangingTheirAst(Dialect dialect, String expression,
            Kind kind, Integer precision) throws JSQLParserException {
        PlainSelect select = parse(expression, dialect);
        Expression node = select.getSelectItem(0).getExpression();
        String original = select.toString();
        TemporalExpressionInfo info = TemporalExpressionInfo.from(node, dialect).orElseThrow();
        assertEquals(kind, info.getKind());
        assertEquals(precision, info.getPrecision());
        assertEquals(expression.contains("("), info.hasParentheses());
        assertEquals(expression.split("\\(")[0], info.getName());
        assertEquals(original, select.toString());

        StringBuilder visitor = new StringBuilder();
        select.accept(new StatementDeParser(visitor));
        for (String sql : List.of(select.toString(), visitor.toString())) {
            PlainSelect reparsed = (PlainSelect) CCJSqlParserUtil.parse(sql,
                    p -> p.withDialect(dialect));
            Expression again = reparsed.getSelectItem(0).getExpression();
            assertEquals(node.getClass(), again.getClass());
            TemporalExpressionInfo after =
                    TemporalExpressionInfo.from(again, dialect).orElseThrow();
            assertEquals(kind, after.getKind());
            assertEquals(precision, after.getPrecision());
            assertEquals(info.hasParentheses(), after.hasParentheses());
        }
    }

    @Test
    void preservesLegacyNodeClassesAndNormalizesTheirMetadata() throws JSQLParserException {
        assertEquals(TimeKeyExpression.class,
                node("CURRENT_TIMESTAMP", Dialect.POSTGRESQL).getClass());
        assertEquals(Function.class,
                node("CURRENT_TIMESTAMP(6)", Dialect.POSTGRESQL).getClass());
        assertEquals(Column.class, node("LOCALTIME", Dialect.POSTGRESQL).getClass());
        assertEquals(Kind.LOCAL_TIME, info("LOCALTIME", Dialect.POSTGRESQL).getKind());
        assertEquals(Kind.CURRENT_TIMESTAMP, info("LOCALTIME", Dialect.MYSQL).getKind());
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void distinguishesOmittedAndZeroPrecisionAndUnwrapsGrouping(Dialect dialect)
            throws JSQLParserException {
        assertNull(info("CURRENT_TIMESTAMP", dialect).getPrecision());
        assertEquals(0, info("CURRENT_TIMESTAMP(0)", dialect).getPrecision());
        assertEquals(6, info("((current_timestamp(6)))", dialect).getPrecision());
        assertEquals("current_timestamp", info("((current_timestamp(6)))", dialect).getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"LOCALTIME\"", "t.localtime", "t.current_timestamp",
            "app.now()", "\"now\"()", "'CURRENT_TIMESTAMP'", "CURRENT_TIMEZONE",
            "CURRENT_TIMESTAMP + INTERVAL '1' HOUR", "COALESCE(CURRENT_TIMESTAMP, NULL)",
            "CURRENT_TIMESTAMP()", "CURRENT_DATE(1)", "CURRENT_TIMESTAMP('6')",
            "CURRENT_TIMESTAMP(1, 2)", "CURRENT_TIMESTAMP(-1)", "NOW(6)", "CURDATE()"})
    void doesNotClassifyUnrelatedOrUnsupportedPostgreSqlExpressions(String expression)
            throws JSQLParserException {
        assertTrue(TemporalExpressionInfo.from(node(expression, Dialect.POSTGRESQL),
                Dialect.POSTGRESQL).isEmpty(), expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"`LOCALTIME`", "\"LOCALTIME\"", "t.localtime", "app.now()",
            "'NOW()'", "CURRENT_DATE(6)", "CURRENT_TIMESTAMP(1 + 1)",
            "CURRENT_TIMESTAMP(1.5)", "CURRENT_TIMESTAMP(?)", "CURDATE(1)"})
    void doesNotClassifyUnrelatedOrUnsupportedMySqlExpressions(String expression)
            throws JSQLParserException {
        assertTrue(TemporalExpressionInfo.from(node(expression, Dialect.MYSQL),
                Dialect.MYSQL).isEmpty(), expression);
    }

    @Test
    void recognizesRequestedPrecisionWithoutApplyingDatabaseRangeRules()
            throws JSQLParserException {
        // PostgreSQL accepts 7 with a warning and clamps to 6; MySQL rejects 7. Metadata retains
        // the requested value, rather than pretending to perform execution-time validation.
        assertEquals(7, info("CURRENT_TIMESTAMP(7)", Dialect.POSTGRESQL).getPrecision());
        assertEquals(7, info("CURRENT_TIMESTAMP(7)", Dialect.MYSQL).getPrecision());
    }

    @Test
    void returnsSnapshotsAndIgnoresFunctionModifiers() {
        Function function = new Function("CURRENT_TIMESTAMP", new LongValue(6));
        TemporalExpressionInfo before = TemporalExpressionInfo.from(function, Dialect.MYSQL)
                .orElseThrow();
        ((LongValue) function.getParameters().get(0)).setValue(0);
        assertEquals(6, before.getPrecision());
        assertEquals(0, TemporalExpressionInfo.from(function, Dialect.MYSQL)
                .orElseThrow().getPrecision());
        function.setDistinct(true);
        assertTrue(TemporalExpressionInfo.from(function, Dialect.MYSQL).isEmpty());
        assertTrue(TemporalExpressionInfo.from(new Column("NOW"), Dialect.POSTGRESQL).isEmpty());
        assertTrue(TemporalExpressionInfo.from(new Column("CURTIME"), Dialect.MYSQL).isEmpty());
        assertTrue(TemporalExpressionInfo.from(null, Dialect.MYSQL).isEmpty());
        assertTrue(TemporalExpressionInfo.from(function, Dialect.ORACLE).isEmpty());
    }

    private static TemporalExpressionInfo info(String expression, Dialect dialect)
            throws JSQLParserException {
        return TemporalExpressionInfo.from(node(expression, dialect), dialect).orElseThrow();
    }

    private static Expression node(String expression, Dialect dialect) throws JSQLParserException {
        return parse(expression, dialect).getSelectItem(0).getExpression();
    }

    private static PlainSelect parse(String expression, Dialect dialect)
            throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse("SELECT " + expression,
                p -> p.withDialect(dialect));
    }
}
