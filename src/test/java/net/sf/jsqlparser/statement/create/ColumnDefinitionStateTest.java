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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnDefinitionStateTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id INT COLUMN_FORMAT DYNAMIC)",
            "ALTER TABLE t ADD COLUMN id INT COLUMN_FORMAT DYNAMIC",
            "CREATE TABLE t (id INT DEFAULT 1)", "ALTER TABLE t ADD COLUMN id INT DEFAULT 1"})
    void specificationSnapshotsAreReadOnlyForRawAndStructuredOptions(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnDefinition column = column(statement);
        List<String> snapshot = column.getColumnSpecs();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("NOT NULL"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, "changed"));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        ColumnOption option = column.getColumnOptions().get(0);
        if (option.getKind() == ColumnOption.Kind.OTHER) {
            assertTrue(column.getColumnOptions().stream()
                    .allMatch(raw -> raw.getKind() == ColumnOption.Kind.OTHER));
            ColumnOption rawOption = column.getColumnOptions().stream()
                    .filter(raw -> raw.getTokens().contains("DYNAMIC"))
                    .findFirst().orElseThrow();
            List<String> rawTokens = new ArrayList<>(rawOption.getTokens());
            rawTokens.set(rawTokens.indexOf("DYNAMIC"), "FIXED");
            rawOption.setTokens(rawTokens);
            assertEquals(List.of("COLUMN_FORMAT", "DYNAMIC"), snapshot);
            assertEquals(List.of("COLUMN_FORMAT", "FIXED"), column.getColumnSpecs());
        } else {
            option.setDefaultExpression(new LongValue(2));
            assertEquals(List.of("DEFAULT", "1"), snapshot);
            assertEquals(List.of("DEFAULT", "2"), column.getColumnSpecs());
        }
        roundTrip(statement);
    }

    @Test
    void constructorAndRawSettersCopyTokensWithoutParsingThem() {
        List<String> input = new ArrayList<>(List.of("DEFAULT", "1"));
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"), input);
        input.set(1, "99");
        assertEquals("id INT DEFAULT 1", column.toString());
        assertEquals(ColumnOption.Kind.OTHER, column.getColumnOptions().get(0).getKind());
        assertNull(column.getColumnOptions().get(0).getDefaultExpression());
        List<String> appended = new ArrayList<>(List.of("NOT", "NULL"));
        column.addColumnSpecs(appended);
        appended.clear();
        assertEquals("id INT DEFAULT 1 NOT NULL", column.toString());
        column.setColumnSpecs(input);
        input.clear();
        assertEquals("id INT DEFAULT 99", column.toString());
        assertEquals(1, column.getColumnOptions().size());
        column.setColumnSpecs(column.getColumnSpecs());
        assertEquals("id INT DEFAULT 99", column.toString());
    }

    @Test
    void constructorDoesNotDispatchToOverriddenSpecificationSetter() {
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"),
                List.of("NOT", "NULL")) {
            @Override
            public void setColumnSpecs(List<String> specs) {
                fail("The constructor must not call an overridable setter");
            }
        };
        assertEquals(List.of("NOT", "NULL"), column.getColumnSpecs());
        assertEquals("id INT NOT NULL", column.toString());
    }

    @Test
    void optionSettersCopyMembershipAndKeepTheOriginalMutableNodes() {
        ColumnOption defaultOption = ColumnOption.defaultValue(new LongValue(1));
        List<ColumnOption> input = new ArrayList<>(List.of(defaultOption));
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"))
                .withColumnOptions(input);
        input.clear();
        assertSame(defaultOption, column.getColumnOptions().get(0));
        List<ColumnOption> liveOptions = column.getColumnOptions();
        defaultOption.setDefaultExpression(new LongValue(2));
        column.addColumnSpecs("NOT", "NULL");
        column.addColumnOptions(ColumnOption.comment(
                new net.sf.jsqlparser.expression.StringValue("description")));
        assertSame(liveOptions, column.getColumnOptions());
        assertEquals(3, liveOptions.size());
        assertEquals("id INT DEFAULT 2 NOT NULL COMMENT 'description'", column.toString());
        column.setColumnOptions(List.of(defaultOption));
        column.addColumnSpecs("NOT", "NULL");
        assertEquals("id INT DEFAULT 2 NOT NULL", column.toString());
        column.getColumnOptions().remove(0);
        assertEquals(List.of("NOT", "NULL"), column.getColumnSpecs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id INT DEFAULT 1 REFERENCES parent(id))",
            "ALTER TABLE t ADD COLUMN id INT DEFAULT 1 REFERENCES parent(id)"})
    void rawAppendPreservesStructuredChildrenVisitorsAndCustomRendering(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnDefinition column = column(statement);
        ColumnOption defaultOption = column.getColumnOptions().get(0);
        ForeignKeyReference reference = column.getForeignKeyReference();
        column.addColumnSpecs("NOT", "NULL");
        assertSame(defaultOption, column.getColumnOptions().get(0));
        assertSame(reference, column.getForeignKeyReference());
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(column, visited::add, ignored -> {
        });
        assertEquals(List.of(defaultOption.getDefaultExpression()), visited);
        assertEquals(Set.of("t", "parent"), new TablesNamesFinder().getTables(statement));
        StringBuilder rewritten = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(7);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), rewritten), null);
        assertEquals(statement.toString().replace("DEFAULT 1", "DEFAULT 7"), rewritten.toString());
        assertEquals(rewritten.toString(), parse(rewritten.toString()).toString());
        roundTrip(statement);

        column.setColumnSpecs(List.of("DEFAULT", "2"));
        assertNull(column.getForeignKeyReference());
        visited.clear();
        TableDefinitionTraversal.visit(column, visited::add, ignored -> {
        });
        assertTrue(visited.isEmpty());
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(statement));
        defaultOption.setDefaultExpression(new LongValue(9));
        assertEquals(List.of("DEFAULT", "2"), column.getColumnSpecs());
        roundTrip(statement);
    }

    @Test
    void nullEmptyAndAppendContractsRemainDistinct() {
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"));
        assertNull(column.getColumnSpecs());
        assertNull(column.getColumnOptions());
        assertThrows(NullPointerException.class,
                () -> column.addColumnSpecs((Collection<String>) null));
        assertThrows(NullPointerException.class,
                () -> column.addColumnOptions((ColumnOption[]) null));
        assertNull(column.getColumnOptions());
        column.setColumnSpecs(List.of());
        assertTrue(column.getColumnSpecs().isEmpty());
        assertTrue(column.getColumnOptions().isEmpty());
        assertEquals("id INT", column.toString());
        column.setColumnOptions(null);
        assertNull(column.getColumnSpecs());
        column.addColumnSpecs(List.of());
        assertTrue(column.getColumnSpecs().isEmpty());
        column.setColumnSpecs(null);
        assertNull(column.getColumnOptions());
        column.setColumnOptions(List.of());
        assertTrue(column.getColumnSpecs().isEmpty());
        assertEquals("INT", column.toStringDataTypeAndSpec());
    }

    @Test
    void parserKeepsExistingAbsentAndEmptySpecificationBoundaries() throws JSQLParserException {
        ColumnDefinition create = column(parse("CREATE TABLE t (id INT)"));
        ColumnDefinition alter = column(parse("ALTER TABLE t ADD COLUMN id INT"));
        assertNull(create.getColumnSpecs());
        assertNull(create.getColumnOptions());
        assertTrue(alter.getColumnSpecs().isEmpty());
        assertTrue(alter.getColumnOptions().isEmpty());
        ColumnDataType dataType = new ColumnDataType(false);
        assertSame(dataType, dataType.withColumnName("id").withColDataType(new ColDataType("INT"))
                .withColumnSpecs(List.of("NOT", "NULL")));
        assertSame(dataType, dataType.addColumnSpecs("DEFAULT", "1"));
        assertSame(dataType, dataType.addColumnSpecs(List.of("UNIQUE")));
        assertEquals("id INT NOT NULL DEFAULT 1 UNIQUE", dataType.toString());
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static ColumnDefinition column(Statement statement) {
        return statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql));
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}
