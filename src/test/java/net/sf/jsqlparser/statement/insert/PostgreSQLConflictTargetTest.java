/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.statement.create.table.KeyElement;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgreSQLConflictTargetTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "(id, (lower(email)))",
            "(id, lower(email))",
            "(pg_catalog.lower(email) COLLATE pg_catalog.\"C\" pg_catalog.text_pattern_ops, id)",
            "((lower(email)), id, (abs(region))) WHERE active",
            "(email COLLATE \"C\" text_pattern_ops, id)",
            "((lower(email)) COLLATE pg_catalog.\"C\" pg_catalog.text_pattern_ops)",
            "ON CONSTRAINT users_pkey"
    })
    void parsesAndReparsesConflictTargets(String target) throws Exception {
        String sql = "INSERT INTO users (id, email) VALUES (1, 'a') ON CONFLICT "
                + target + " DO UPDATE SET email = excluded.email RETURNING id";
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        assertEquals(insert.toString(), CCJSqlParserUtil.parse(insert.toString()).toString());
    }

    @Test
    void preservesFunctionKeyParentheses() throws Exception {
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO users VALUES (1) ON CONFLICT (lower(email), (abs(region))) DO NOTHING",
                true);
        List<KeyElement> keys = insert.getConflictTarget().getIndexElements();
        assertInstanceOf(Function.class, keys.get(0).getExpression());
        assertFalse(keys.get(0).isExpressionParenthesized());
        assertTrue(keys.get(1).isExpressionParenthesized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM", "USER"})
    void acceptsOverridingWithoutColumnList(String mode) throws Exception {
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO users OVERRIDING " + mode + " VALUE VALUES (42, 'a@example.org', 'A')",
                true);
        assertNull(insert.getTable().getAlias());
        assertEquals(Insert.OverridingMode.valueOf(mode), insert.getOverridingMode());
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO users overriding VALUES (1)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO users AS overriding OVERRIDING " + mode
                        + " VALUE SELECT * FROM source",
                true);
    }

    @Test
    void exposesOrderedTypedKeys() throws Exception {
        Insert insert = (Insert) CCJSqlParserUtil.parse("INSERT INTO users VALUES (1) "
                + "ON CONFLICT (id, (lower(email)) COLLATE pg_catalog.\"C\" "
                + "pg_catalog.text_pattern_ops, region) WHERE active DO NOTHING");
        InsertConflictTarget target = insert.getConflictTarget();
        List<KeyElement> keys = target.getIndexElements();
        assertEquals(3, keys.size());
        assertEquals("id", keys.get(0).getColumnName());
        assertInstanceOf(Function.class, keys.get(1).getExpression());
        assertEquals("pg_catalog.\"C\"", keys.get(1).getCollation());
        assertEquals("pg_catalog.text_pattern_ops", keys.get(1).getOperatorClass());
        assertEquals(Arrays.asList("id", "region"), target.getIndexColumnNames());
        assertNotNull(target.getWhereExpression());
    }

    @Test
    void retainsMutableLegacyColumnViewAndReplacementMethods() throws Exception {
        InsertConflictTarget target = new InsertConflictTarget("id", null, null, null);
        List<String> names = target.getIndexColumnNames();
        names.add("region");
        names.set(0, "tenant");
        names.remove(1);
        assertEquals(" (tenant)", target.toString());
        target.setIndexExpression(CCJSqlParserUtil.parseExpression("lower(email)"));
        assertTrue(names.isEmpty());
        assertNotNull(target.getIndexExpression());
        target.withIndexColumnName("id").addAllIndexColumnNames(Arrays.asList("region", "tenant"));
        assertNull(target.getIndexExpression());
        assertEquals(Arrays.asList("id", "region", "tenant"), names);
        names.clear();
        assertTrue(target.getIndexElements().isEmpty());
    }

    @Test
    void bulkColumnAdditionsSnapshotTheMutableView() {
        InsertConflictTarget target = new InsertConflictTarget("id", null, null, null);
        List<String> names = target.getIndexColumnNames();
        assertTrue(target.addAllIndexColumnNames(names));
        assertEquals(List.of("id", "id"), names);
        assertTrue(names.addAll(names));
        assertEquals(4, names.size());
        assertTrue(names.addAll(1, names.subList(0, 2)));
        assertEquals(6, names.size());
        assertFalse(names.addAll(List.of()));
    }

    @Test
    void statementVisitorTraversesTargetExpressions() throws Exception {
        Insert insert = (Insert) CCJSqlParserUtil.parse("INSERT INTO users VALUES (1) "
                + "ON CONFLICT ((lower(email))) WHERE active DO NOTHING");
        List<String> columns = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions =
                new ExpressionVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(Column column, S context) {
                        columns.add(column.getColumnName());
                        return null;
                    }
                };
        insert.accept(new StatementVisitorAdapter<Void>(
                new SelectVisitorAdapter<>(expressions)), null);
        assertEquals(List.of("email", "active"), columns);
    }

    @Test
    void deparserVisitsConflictExpressionsAndPredicate() throws Exception {
        Insert insert = (Insert) CCJSqlParserUtil.parse("INSERT INTO users VALUES (1) "
                + "ON CONFLICT ((lower(email))) WHERE active DO NOTHING");
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                getBuilder().append("x_").append(column.getColumnName());
                return getBuilder();
            }
        };
        insert.accept(new StatementDeParser(expressions,
                new SelectDeParser(), sql), null);
        assertTrue(sql.toString().contains("lower(x_email)"), sql.toString());
        assertTrue(sql.toString().contains("WHERE x_active"), sql.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"VALUES (1)", "SELECT id FROM source"})
    void distinguishesOverridingModes(String source) throws Exception {
        for (Insert.OverridingMode mode : Arrays.asList(Insert.OverridingMode.SYSTEM,
                Insert.OverridingMode.USER)) {
            Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "INSERT INTO users (id) OVERRIDING " + mode + " VALUE " + source, true);
            assertEquals(mode, insert.getOverridingMode());
            assertTrue(insert.isOverriding());
            insert.setOverriding(false);
            assertEquals(Insert.OverridingMode.NONE, insert.getOverridingMode());
            insert.setOverriding(true);
            assertEquals(Insert.OverridingMode.SYSTEM, insert.getOverridingMode());
        }
    }
}
