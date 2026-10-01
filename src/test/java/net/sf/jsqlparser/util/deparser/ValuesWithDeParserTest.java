/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValuesWithDeParserTest {
    private static Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WITH cte(foo) AS (VALUES (42)) VALUES ((SELECT foo FROM cte))",
            "WITH a(n) AS (VALUES (1)), b(n) AS (VALUES (2)) "
                    + "VALUES ((SELECT n FROM a), (SELECT n FROM b))",
            "WITH RECURSIVE c(n) AS (VALUES (1) UNION ALL "
                    + "SELECT n + 1 FROM c WHERE n < 3) VALUES ((SELECT max(n) FROM c))",
            "WITH c(n) AS MATERIALIZED (VALUES (1)) VALUES ((SELECT n FROM c))",
            "WITH c(n) AS NOT MATERIALIZED (VALUES (1)) VALUES ((SELECT n FROM c))",
            "WITH c AS (DELETE FROM t RETURNING id) VALUES ((SELECT count(*) FROM c))"
    })
    void preservesWithItemsInStatementAndDirectValuesDeparsers(String sql) throws Exception {
        Values values = assertInstanceOf(Values.class, assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
        String original = values.toString();
        for (boolean direct : new boolean[] {false, true}) {
            StringBuilder output = new StringBuilder();
            SelectDeParser selects = new SelectDeParser();
            ExpressionDeParser expressions = new ExpressionDeParser(selects, output);
            if (direct) {
                new ValuesStatementDeParser(expressions, output).deParse(values);
            } else {
                values.accept(new StatementDeParser(expressions, selects, output), null);
            }
            Values reparsed = assertInstanceOf(Values.class, parse(output.toString()));
            assertEquals(original, reparsed.toString());
            assertEquals(values.getWithItemsList().size(), reparsed.getWithItemsList().size());
            assertEquals(original, values.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"statement", "select", "expression", "direct", "directWithSelect"})
    void preservesCustomVisitorsAndContext(String entryPoint) throws Exception {
        Values values = assertInstanceOf(Values.class,
                parse("WITH first_cte AS (VALUES (7)), second_cte AS (VALUES (8)) VALUES (9)"));
        String original = values.toString();
        Object expectedContext = new Object();
        StringBuilder output = new StringBuilder();
        List<String> withItems = new ArrayList<>();
        List<Long> numbers = new ArrayList<>();
        SelectDeParser selects = new SelectDeParser() {
            @Override
            public <S> StringBuilder visit(WithItem<?> item, S context) {
                assertSame(expectedContext, context);
                withItems.add(item.getAlias().getName());
                return super.visit(item, context);
            }
        };
        ExpressionDeParser expressions = new ExpressionDeParser(selects, output) {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                assertSame(expectedContext, context);
                numbers.add(value.getValue());
                return output.append(value.getValue() + 100);
            }
        };
        selects.setExpressionVisitor(expressions);
        selects.setBuilder(output);
        switch (entryPoint) {
            case "statement":
                values.accept(new StatementDeParser(expressions, selects, output), expectedContext);
                break;
            case "select":
                values.accept((SelectVisitor<StringBuilder>) selects, expectedContext);
                break;
            case "expression":
                values.accept((ExpressionVisitor<StringBuilder>) expressions, expectedContext);
                break;
            case "direct":
                new ValuesStatementDeParser(expressions, output).deParse(values, expectedContext);
                break;
            default:
                new ValuesStatementDeParser(expressions, selects, output)
                        .deParse(values, expectedContext);
        }
        assertEquals(List.of("first_cte", "second_cte"), withItems);
        assertEquals(List.of(7L, 8L, 9L), numbers);
        assertEquals(parse("WITH first_cte AS (VALUES (107)), "
                + "second_cte AS (VALUES (108)) VALUES (109)").toString(),
                parse(output.toString()).toString());
        assertEquals(original, values.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT (WITH c(n) AS (VALUES (1)) VALUES ((SELECT n FROM c)))",
            "VALUES ((WITH c(n) AS (VALUES (1)) VALUES ((SELECT n FROM c))))",
            "SELECT * FROM (WITH c(n) AS (VALUES (1)) VALUES ((SELECT n FROM c))) AS v(n)"
    })
    void rendersNestedValuesWithItemsOnce(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), parse(output.toString()).toString());
    }
}
