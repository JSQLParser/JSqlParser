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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DdlVisitorTraversalTest {
    static Stream<Arguments> definitions() {
        return Stream.of(
                Arguments.of("ALTER VIEW v ALTER COLUMN id SET DEFAULT 7", List.of("7"),
                        List.of("v")),
                Arguments.of("ALTER INDEX ix SET (fillfactor = 70)", List.of("70"), List.of()),
                Arguments.of("ALTER POLICY p ON t USING (id > 7) WITH CHECK (id < 9)",
                        List.of("7", "9"), List.of("t")),
                Arguments.of("DROP POLICY p ON t", List.of(), List.of("t")),
                Arguments.of("CREATE STATISTICS s ON (id + 7), label FROM t", List.of("7"),
                        List.of("t")),
                Arguments.of("CREATE FOREIGN DATA WRAPPER w OPTIONS (host 'before')",
                        List.of("before"), List.of()),
                Arguments.of("ALTER FOREIGN DATA WRAPPER w OPTIONS (SET host 'before')",
                        List.of("before"), List.of()),
                Arguments.of(
                        "CREATE SERVER s TYPE 'pg' VERSION '18' FOREIGN DATA WRAPPER w OPTIONS (host 'before')",
                        List.of("before", "pg", "18"), List.of()),
                Arguments.of("ALTER SERVER s VERSION '18' OPTIONS (SET host 'before')",
                        List.of("before", "18"), List.of()),
                Arguments.of(
                        "CREATE USER MAPPING FOR CURRENT_USER SERVER s OPTIONS (user 'before')",
                        List.of("before"), List.of()),
                Arguments.of(
                        "ALTER USER MAPPING FOR CURRENT_USER SERVER s OPTIONS (SET user 'before')",
                        List.of("before"), List.of()),
                Arguments.of("CREATE COLLATION c (locale = 'C')", List.of("C"), List.of()),
                Arguments.of(
                        "CREATE RULE r AS ON INSERT TO t WHERE NEW.id > 7 DO (INSERT INTO log VALUES (9); NOTIFY ch, 'before')",
                        List.of("7", "9", "before"), List.of("t", "log")));
    }

    @Test
    void valuesChildrenIncludingWithAndTailKeepTheVisitorContext() throws Exception {
        Object context = new Object();
        List<Long> values = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S actualContext) {
                assertSame(context, actualContext);
                values.add(value.getValue());
                return null;
            }
        };
        Statement statement = CCJSqlParserUtil.parse(
                "WITH c AS (SELECT 11) VALUES (22) ORDER BY (33 + 0) LIMIT 44 OFFSET 55");
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                context);
        assertEquals(List.of(11L, 22L, 33L, 0L, 44L, 55L), values);
    }

    @ParameterizedTest
    @MethodSource("definitions")
    void visitsAndMutatesChildrenWithContext(String sql, List<String> expectedValues,
            List<String> expectedTables) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        List<String> values = new ArrayList<>();
        List<String> tables = new ArrayList<>();
        Object context = new Object();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S actualContext) {
                assertSame(context, actualContext);
                values.add(value.getStringValue());
                value.setValue(value.getValue() + 1);
                return null;
            }

            @Override
            public <S> Void visit(StringValue value, S actualContext) {
                assertSame(context, actualContext);
                values.add(value.getValue());
                value.setValue("changed");
                return null;
            }
        };
        FromItemVisitorAdapter<Void> fromItems = new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S actualContext) {
                assertSame(context, actualContext);
                tables.add(table.getName());
                table.setName("changed_" + table.getName());
                return null;
            }
        };
        SelectVisitorAdapter<Void> selects =
                new SelectVisitorAdapter<>(expressions, fromItems);
        statement.accept(new StatementVisitorAdapter<>(selects), context);
        assertEquals(expectedValues, values);
        assertEquals(expectedTables, tables);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER VIEW v ALTER COLUMN id SET DEFAULT abs(7)",
            "ALTER POLICY p ON t USING (abs(id) > 7)",
            "CREATE STATISTICS s ON (abs(id)) FROM t"})
    void validatesNestedExpressions(String sql) {
        FeaturesAllowed withoutFunctions = new FeaturesAllowed().add(FeaturesAllowed.DDL);
        assertFalse(new Validation(List.of(withoutFunctions), sql).validate().isEmpty());
        FeaturesAllowed withFunctions =
                new FeaturesAllowed().add(FeaturesAllowed.DDL).add(Feature.function);
        assertTrue(new Validation(List.of(withFunctions), sql).validate().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE RULE r AS ON INSERT TO t DO INSERT INTO log VALUES (7)",
            "CREATE RULE r AS ON SELECT TO v DO INSTEAD SELECT * FROM t",
            "ALTER POLICY p ON t USING (id > 7)",
            "CREATE STATISTICS s ON (id + 7), label FROM t"})
    void storedDefinitionsAreSchemaChangesRatherThanExecutedBodies(String sql) throws Exception {
        StatementFeatures features = CCJSqlParserUtil.parse(sql).getFeatures();
        assertTrue(features.is(StmtFeature.MODIFIES_SCHEMA));
        assertFalse(features.is(StmtFeature.MODIFIES_DATA));
        assertFalse(features.is(StmtFeature.RETURNS_RESULT_SET));
        assertFalse(features.is(StmtFeature.READS_DATA));
    }
}
