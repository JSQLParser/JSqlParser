/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNotAllowed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AliasedExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class XmlForestTest {
    @ParameterizedTest
    @ValueSource(strings = {"XMLFOREST(name AS \"Name\")", "xmlforest(a, b AS item)",
            "XMLFOREST(1 + 2 AS total, COALESCE(a, 'default') AS \"Value\")",
            "XMLFOREST(XMLFOREST(a AS inner_name) AS outer_name)",
            "XMLFOREST(name a)", "XMLFOREST(name)", "XMLPARSE(CONTENT a)"})
    void parsesAndRoundTripsNamedAndUnnamedArguments(String expression) throws Exception {
        var statement = assertSqlCanBeParsedAndDeparsed("SELECT " + expression + " FROM t");
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(statement.toString()).toString());
    }

    @Test
    void aliasIsStructuredAndCanBeEditedWithoutReparsingSql() throws Exception {
        var statement =
                (PlainSelect) CCJSqlParserUtil.parse("SELECT XMLFOREST(name AS \"Name\") FROM t");
        Function function = (Function) statement.getSelectItem(0).getExpression();
        AliasedExpression argument = (AliasedExpression) function.getParameters().get(0);
        assertEquals("name", ((Column) argument.getExpression()).getColumnName());
        assertEquals("\"Name\"", argument.getAlias().getName());
        argument.setExpression(new LongValue(7));
        argument.setAlias(new Alias("value", true));
        assertEquals("SELECT XMLFOREST(7 AS value) FROM t", statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
    }

    @Test
    void customVisitorReachesValuesWithContextAndDeparserKeepsAliases() throws Exception {
        var statement = (PlainSelect) CCJSqlParserUtil
                .parse("SELECT XMLFOREST(1 + 2 AS total, 3 AS n) FROM t");
        List<Long> values = new ArrayList<>();
        statement.getSelectItem(0).getExpression().accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("context", context);
                values.add(value.getValue());
                return null;
            }
        }, "context");
        assertEquals(Arrays.asList(1L, 2L, 3L), values);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals("SELECT XMLFOREST(101 + 102 AS total, 103 AS n) FROM t", output.toString());
    }

    @Test
    void traversalAndValidationReachTheUnderlyingArgument() throws Exception {
        assertEquals(Set.of("outer_t", "inner_t"), TablesNamesFinder.findTables(
                "SELECT XMLFOREST((SELECT a FROM inner_t) AS item) FROM outer_t"));
        validateNotAllowed("SELECT a FROM t WHERE XMLFOREST(? AS item) IS NULL", 1, 1,
                new FeaturesAllowed().add(FeaturesAllowed.SELECT).remove(Feature.jdbcParameter),
                Feature.jdbcParameter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT XMLFOREST(a AS) FROM t", "SELECT XMLFOREST(a AS b.c) FROM t",
            "SELECT f(a AS b) FROM t"})
    void rejectsMissingAliasesAndKeepsOrdinaryFunctionArgumentsStrict(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }
}
