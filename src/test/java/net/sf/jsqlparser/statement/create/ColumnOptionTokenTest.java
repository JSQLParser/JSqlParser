/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.statement.create.table.GeneratedColumnDefinition;
import net.sf.jsqlparser.statement.create.table.IdentityDefinition;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnOptionTokenTest {
    // Percona Server's COLUMN_FORMAT COMPRESSED is not modeled and stays opaque.
    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id INT COLUMN_FORMAT COMPRESSED)",
            "ALTER TABLE t ADD COLUMN id INT COLUMN_FORMAT COMPRESSED"})
    void parsedRawOptionsUseExplicitTokenReplacement(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnOption option = column(statement).getColumnOptions().stream()
                .filter(raw -> raw.getTokens().contains("COMPRESSED")).findFirst().orElseThrow();
        List<String> snapshot = option.getTokens();
        assertReadOnly(snapshot);
        List<String> replacement = new ArrayList<>(snapshot);
        replacement.set(replacement.indexOf("COMPRESSED"), "FIXED");
        option.setTokens(replacement);
        replacement.clear();
        assertTrue(snapshot.contains("COMPRESSED"));
        assertTrue(option.getTokens().contains("FIXED"));
        assertEquals(ColumnOption.Kind.OTHER, option.getKind());
        assertTrue(statement.toString().contains("COLUMN_FORMAT FIXED"));
        assertRoundTrip(statement);
    }

    @Test
    void rawFactoriesAndSetterOwnTheirTokensWithoutParsingThem() {
        List<String> input = new ArrayList<>(List.of("DEFAULT", "1"));
        ColumnOption option = ColumnOption.raw(input);
        input.set(1, "99");
        List<String> snapshot = option.getTokens();
        assertReadOnly(snapshot);
        assertEquals(List.of("DEFAULT", "1"), snapshot);
        assertNull(option.getDefaultExpression());
        assertEquals(ColumnOption.Kind.OTHER, option.getKind());
        option.setTokens(input);
        input.clear();
        assertEquals("DEFAULT 99", option.toString());
        assertEquals(List.of("DEFAULT", "1"), snapshot);
        option.setTokens(option.getTokens());
        assertEquals("DEFAULT 99", option.toString());

        String[] array = {"NOT", "NULL"};
        ColumnOption fromArray = ColumnOption.raw(array);
        array[0] = "changed";
        assertEquals("NOT NULL", fromArray.toString());
        assertReadOnly(fromArray.getTokens());
    }

    @Test
    void unsetAndEmptyRawTokensRemainDistinct() {
        ColumnOption option = ColumnOption.raw((List<String>) null);
        assertNull(option.getTokens());
        assertEquals("", option.toString());
        option.setTokens(List.of());
        List<String> empty = option.getTokens();
        assertTrue(empty.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> empty.add("changed"));
        option.setTokens(List.of("CUSTOM"));
        assertTrue(empty.isEmpty());
        option.setTokens(null);
        assertNull(option.getTokens());
        assertEquals("", option.toString());
    }

    @ParameterizedTest
    @MethodSource("structuredOptions")
    void tokenReplacementRejectsStructuredOptionsWithoutChangingThem(ColumnOption option) {
        ColumnOption.Kind kind = option.getKind();
        String sql = option.toString();
        List<String> snapshot = option.getTokens();
        assertReadOnly(snapshot);
        assertThrows(IllegalStateException.class, () -> option.setTokens(List.of("raw")));
        assertThrows(IllegalStateException.class, () -> option.setTokens(null));
        assertEquals(kind, option.getKind());
        assertEquals(sql, option.toString());
        assertEquals(snapshot, option.getTokens());
    }

    private static Stream<ColumnOption> structuredOptions() {
        return Stream.of(ColumnOption.serialDefaultValue(),
                ColumnOption.reference(new ForeignKeyReference().withTable(new Table("parent"))
                        .withReferencedColumnNames(List.of("id"))),
                ColumnOption
                        .identity(new IdentityDefinition(IdentityDefinition.GenerationMode.ALWAYS)),
                ColumnOption.constraint(new KeyConstraint().withType("PRIMARY KEY")),
                ColumnOption.constraint(new KeyConstraint().withType("UNIQUE")),
                ColumnOption.defaultValue(new LongValue(1)),
                ColumnOption.nullability(false), ColumnOption.nullability(true),
                ColumnOption.collate("utf8mb4_bin"),
                ColumnOption.comment(new StringValue("comment")),
                ColumnOption.onUpdate(new LongValue(1)),
                ColumnOption.generated(new GeneratedColumnDefinition(new LongValue(1))),
                ColumnOption.autoIncrement(), ColumnOption.visibility(false),
                ColumnOption.storage(ColumnOption.Storage.MEMORY),
                ColumnOption.compression("pglz"),
                ColumnOption.foreignOptions(List.of(
                        new ForeignDataOption("column_name", new StringValue("remote_id")))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id INT DEFAULT 1)",
            "ALTER TABLE t ADD COLUMN id INT DEFAULT 1"})
    void parsedDefaultsKeepLiveExpressionsAndStableTokenSnapshots(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnDefinition column = column(statement);
        ColumnOption option = column.getColumnOptions().get(0);
        List<String> snapshot = option.getTokens();
        assertReadOnly(snapshot);
        LongValue original = (LongValue) option.getDefaultExpression();
        original.setValue(7);
        assertEquals(List.of("DEFAULT", "7"), option.getTokens());
        assertEquals(List.of("DEFAULT", "1"), snapshot);
        LongValue replacement = new LongValue(2);
        option.setDefaultExpression(replacement);
        original.setValue(99);
        assertEquals(List.of("DEFAULT", "2"), option.getTokens());
        assertVisited(column, replacement);
        assertCustomRendering(statement, "DEFAULT 2", "DEFAULT 42");
        assertRoundTrip(statement);

        ColumnOption raw = ColumnOption.raw(List.of("DEFAULT", "8"));
        column.getColumnOptions().set(0, raw);
        assertEquals(ColumnOption.Kind.OTHER, raw.getKind());
        assertNull(raw.getDefaultExpression());
        assertVisited(column);
        replacement.setValue(100);
        assertEquals(List.of("DEFAULT", "8"), raw.getTokens());
        assertRoundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (source INT, computed INT GENERATED ALWAYS AS (source + 1) STORED)",
            "ALTER TABLE t ADD COLUMN computed INT GENERATED ALWAYS AS (source + 1) STORED"})
    void generatedDefinitionAndOptionExposeReadOnlySnapshotsWithLiveChildren(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnDefinition column = column(statement);
        ColumnOption option = column.getColumnOptions().get(0);
        GeneratedColumnDefinition generated = option.getGeneratedDefinition();
        List<String> snapshot = generated.getTokens();
        List<String> optionSnapshot = option.getTokens();
        assertReadOnly(snapshot);
        assertReadOnly(optionSnapshot);
        generated.setGeneratedAlways(false);
        generated.setStorage(GeneratedColumnDefinition.Storage.VIRTUAL);
        Addition expression = (Addition) CCJSqlParserUtil.parseExpression("source + 2");
        generated.setExpression(expression);
        ((LongValue) expression.getRightExpression()).setValue(3);
        assertSame(generated, option.getGeneratedDefinition());
        assertEquals(List.of("AS", "(source + 3)", "VIRTUAL"), generated.getTokens());
        assertEquals(generated.getTokens(), option.getTokens());
        assertEquals(List.of("GENERATED", "ALWAYS", "AS", "(source + 1)", "STORED"), snapshot);
        assertEquals(snapshot, optionSnapshot);
        assertVisited(column, expression);
        assertCustomRendering(statement, "source + 3", "source + 42");
        assertRoundTrip(statement);
    }

    private static void assertReadOnly(List<String> tokens) {
        assertThrows(UnsupportedOperationException.class, () -> tokens.set(0, "changed"));
        assertThrows(UnsupportedOperationException.class, () -> tokens.add("changed"));
        assertThrows(UnsupportedOperationException.class, tokens::clear);
    }

    private static void assertVisited(ColumnDefinition column, Expression... expected) {
        List<Expression> expressions = new ArrayList<>();
        TableDefinitionTraversal.visit(column, expressions::add, ignored -> {
        });
        assertEquals(List.of(expected), expressions);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static ColumnDefinition column(Statement statement) {
        if (statement instanceof CreateTable) {
            List<ColumnDefinition> columns = ((CreateTable) statement).getColumnDefinitions();
            return columns.get(columns.size() - 1);
        }
        return ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static void assertCustomRendering(Statement statement, String before, String after)
            throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(42);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), sql), null);
        assertEquals(statement.toString().replace(before, after), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}
