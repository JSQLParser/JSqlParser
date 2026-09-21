/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IsJsonExpressionTest {
    @ParameterizedTest
    @ValueSource(strings = {"", " VALUE", " SCALAR", " ARRAY", " OBJECT",
            " WITH UNIQUE KEYS", " WITHOUT UNIQUE", " OBJECT WITH UNIQUE",
            " ARRAY WITHOUT UNIQUE KEYS"})
    void modelsOptionalTypeAndUniquenessInBothParserModes(String options) throws Exception {
        for (boolean complex : List.of(false, true)) {
            for (String not : List.of("", " NOT")) {
                String sql = "SELECT payload IS" + not + " JSON" + options + " FROM events";
                PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql,
                        p -> p.withDialect(Dialect.POSTGRESQL).withAllowComplexParsing(complex));
                IsJsonExpression predicate = assertInstanceOf(IsJsonExpression.class,
                        select.getSelectItem(0).getExpression());
                assertEquals(!not.isEmpty(), predicate.isNot());
                assertEquals(sql, select.toString());
                roundTrip(select);
            }
        }
    }

    @Test
    void exposesMutableOptionsAndVisitsTheOperandWithContext() throws Exception {
        PlainSelect select = parse("SELECT '{\"a\":1}' IS JSON");
        IsJsonExpression predicate = (IsJsonExpression) select.getSelectItem(0).getExpression();
        assertNull(predicate.getType());
        assertNull(predicate.getUniqueKeys());
        predicate.setType(IsJsonExpression.Type.OBJECT);
        predicate.setUniqueKeys(IsJsonExpression.UniqueKeys.WITH);
        predicate.setNot(true);
        predicate.setLeftExpression(new StringValue("{}"));
        assertEquals("SELECT '{}' IS NOT JSON OBJECT WITH UNIQUE KEYS", select.toString());
        roundTrip(select);
        Object expected = new Object();
        List<String> seen = new ArrayList<>();
        predicate.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(StringValue value, S context) {
                assertSame(expected, context);
                seen.add(value.getValue());
                return null;
            }
        }, expected);
        assertEquals(List.of("{}"), seen);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser visitor = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                assertSame(expected, context);
                return getBuilder().append("'[]'");
            }
        };
        visitor.setBuilder(output);
        predicate.accept(visitor, expected);
        assertEquals("'[]' IS NOT JSON OBJECT WITH UNIQUE KEYS", output.toString());
    }

    @Test
    void traversesSubqueriesAndValidatesOperandFeatures() throws Exception {
        PlainSelect select = parse("SELECT 1 FROM t WHERE (SELECT payload FROM hidden) IS JSON");
        assertThat(new TablesNamesFinder().getTables((Statement) select)).containsExactlyInAnyOrder(
                "t",
                "hidden");
        FeaturesAllowed allowed =
                new FeaturesAllowed().add(FeaturesAllowed.DML).remove(Feature.jdbcParameter);
        assertFalse(
                new Validation(List.of(allowed), "SELECT 1 WHERE ? IS JSON").validate().isEmpty());
        roundTrip(parse("SELECT NULL IS JSON OR '{}' IS NOT JSON AND '1' IS JSON SCALAR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT x IS JSON WITH", "SELECT x IS JSON WITH KEYS",
            "SELECT x IS JSON WITH UNIQUE KEYS WITHOUT UNIQUE",
            "SELECT x IS NOT JSON WITH UNIQUE,"})
    void rejectsIncompleteOptions(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(PlainSelect select) throws Exception {
        StringBuilder out = new StringBuilder();
        select.accept(new StatementDeParser(out), null);
        assertEquals(select.toString(), out.toString());
        assertEquals(select.toString(), parse(out.toString()).toString());
    }
}
