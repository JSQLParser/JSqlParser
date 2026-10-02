/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.AlterDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CockroachAlterPrimaryKeyTest {
    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.COCKROACHDB));
    }

    private static AlterExpressionPrimaryKey primaryKey(Alter alter) {
        return (AlterExpressionPrimaryKey) alter.getAlterExpressions().get(0);
    }

    @Test
    void parsesOriginalReproducerIssue1743() throws Exception {
        String sql = "ALTER TABLE FEATURE_SWITCH_CONFIG ALTER PRIMARY KEY "
                + "USING COLUMNS (FEATURE_NAME) USING HASH";
        Alter alter = (Alter) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.COCKROACHDB));
        AlterExpressionPrimaryKey action = primaryKey(alter);
        assertEquals(AlterOperation.ALTER_PRIMARY_KEY, action.getOperation());
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraint().getKind());
        assertEquals(List.of("FEATURE_NAME"), action.getConstraint().getColumnsNames());
        assertTrue(action.isUsingHash());
        assertNull(action.getBucketCount());
        assertEquals(Set.of("FEATURE_SWITCH_CONFIG"), new TablesNamesFinder<>().getTables(alter));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " USING HASH", " USING HASH WITH BUCKET_COUNT = 8",
            " USING HASH WITH (bucket_count = 8)", " WITH (fillfactor = 70)"})
    void preservesKeyOrderOptionsAndRoundTrips(String suffix) throws Exception {
        String sql = "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (tenant_id ASC, \"key\" DESC)"
                + suffix;
        Alter alter = (Alter) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.COCKROACHDB));
        StringBuilder output = new StringBuilder();
        alter.accept(new StatementDeParser(output), null);
        for (String rendered : List.of(alter.toString(), output.toString())) {
            AlterExpressionPrimaryKey action = primaryKey(parse(rendered));
            assertEquals(List.of("tenant_id ASC", "\"key\" DESC"),
                    action.getConstraint().getColumnsNames());
            assertEquals(suffix.contains("USING HASH"), action.isUsingHash());
            if (suffix.contains("BUCKET_COUNT")) {
                assertEquals(8, ((LongValue) action.getBucketCount()).getValue());
            } else {
                assertNull(action.getBucketCount());
            }
            if (suffix.contains("WITH (")) {
                IndexOption option = action.getConstraint().getStorageParameters().get(0);
                assertEquals(suffix.contains("bucket_count") ? "bucket_count" : "fillfactor",
                        option.getName());
                assertEquals(suffix.contains("bucket_count") ? 8 : 70,
                        ((LongValue) option.getValue()).getValue());
            }
        }
    }

    @Test
    void exposesStructuredExpressionKeysAndSupportsMutation() throws Exception {
        Alter alter = parse("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS "
                + "(lower(name) text_ops DESC, (id + 1) ASC)");
        AlterExpressionPrimaryKey action = primaryKey(alter);
        KeyElement first = action.getConstraint().getColumns().get(0);
        assertTrue(first.getExpression() instanceof Function);
        assertFalse(first.isExpressionParenthesized());
        assertEquals("text_ops", first.getOperatorClass());
        assertNull(first.getParams());
        first.setSortOrder(KeyElement.SortOrder.ASC);
        action.setUsingHash(true);
        action.setBucketCount(new LongValue(16));
        String expected = "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS "
                + "(lower(name) text_ops ASC, (id + 1) ASC) USING HASH WITH BUCKET_COUNT = 16";
        TestUtils.assertDeparse(alter, expected);
        assertEquals(expected, parse(expected).toString());
    }

    @Test
    void requiresCockroachDialectAndKeepsExistingAlterForms() throws Exception {
        String sql = "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) USING HASH";
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        for (Dialect dialect : Dialect.values()) {
            if (dialect != Dialect.COCKROACHDB) {
                assertThrows(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                        dialect.name());
            }
        }
        for (String existing : List.of("ALTER TABLE t ADD PRIMARY KEY (id)",
                "ALTER TABLE t ALTER COLUMN id TYPE BIGINT", "ALTER TABLE t DROP PRIMARY KEY")) {
            String originalOutput = CCJSqlParserUtil.parse(existing).toString();
            assertEquals(existing, originalOutput.trim());
            TestUtils.assertStatementCanBeDeparsedAs(parse(existing), originalOutput);
        }
    }

    @Test
    void visitsAndRewritesHashAndStorageExpressions() throws Exception {
        Alter alter = parse("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) "
                + "USING HASH WITH BUCKET_COUNT = 4 WITH (fillfactor = 70)");
        List<Long> values = new ArrayList<>();
        ExpressionVisitorAdapter<Void> visitor = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                values.add(value.getValue());
                return null;
            }
        };
        alter.accept(new StatementVisitorAdapter<Void>(new SelectVisitorAdapter<Void>(visitor)),
                null);
        assertEquals(2, values.size());
        assertEquals(Set.of(4L, 70L), Set.copyOf(values));
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 1);
            }
        };
        expressions.setBuilder(output);
        new AlterDeParser(output, expressions).deParse(alter);
        assertEquals("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) "
                + "USING HASH WITH BUCKET_COUNT = 5 WITH (fillfactor = 71)", output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
    }

    @Test
    void keepsFollowingActionsAndStatements() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements("ALTER TABLE t ALTER PRIMARY KEY "
                + "USING COLUMNS (id) USING HASH WITH BUCKET_COUNT = 4, ADD COLUMN note INT; SELECT 1;",
                parser -> parser.withDialect(Dialect.COCKROACHDB));
        assertEquals(2, statements.size());
        Alter alter = (Alter) statements.get(0);
        assertEquals(2, alter.getAlterExpressions().size());
        assertEquals(AlterOperation.ADD, alter.getAlterExpressions().get(1).getOperation());
        assertEquals(alter.toString(), parse(alter.toString()).toString());
    }

    @Test
    void rejectsIncompleteKeysAndHashOptions() {
        for (String sql : List.of("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS ()",
                "ALTER TABLE t ALTER PRIMARY KEY USING (id)",
                "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id,)",
                "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) USING BTREE",
                "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) USING HASH WITH BUCKET_COUNT =",
                "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) WITH BUCKET_COUNT = 8",
                "ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id) USING HASH WITH wrong = 8")) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }
}
