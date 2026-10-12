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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnSetDefault;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import net.sf.jsqlparser.statement.create.table.TableElement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.validator.AlterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ActiveTableDefinitionTraversalTest {
    @ParameterizedTest
    @ValueSource(strings = {"FOREIGN KEY (parent_id) REFERENCES old_parent (id)",
            "INDEX old_index (old_id)", "CONSTRAINT hidden_check CHECK (old_id > 0)"})
    void addedColumnHidesStoredDefinitionFromAllConsumers(String definition) throws Exception {
        Alter alter = parse("ALTER TABLE child ADD " + definition);
        AlterExpression action = first(alter);
        TableElement original = action.getActiveTableDefinition();
        action.addColDataType("added", new ColDataType("INT"));
        assertEquals("ALTER TABLE child ADD added INT", alter.toString());
        assertNull(action.getActiveTableDefinition());
        assertTrue(original == action.getConstraint() || original == action.getIndex());
        assertEquals(Set.of("child"), new TablesNamesFinder<>().getTables(alter));
        assertTrue(expressions(action).isEmpty());
        assertEquals(List.of("child", "added"), validatedNames(alter, Set.of("child")));
        assertRoundTrip(alter, null);
    }

    @Test
    void operationChangesKeepForeignKeyStoredButDoNotVisitIt() throws Exception {
        Alter alter = parse("ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES parent (id)");
        AlterExpression action = first(alter);
        Object original = action.getConstraint();
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        action.setConstraintName("fk");
        assertSame(original, action.getConstraint());
        assertNull(action.getActiveTableDefinition());
        assertEquals(Set.of("child"), new TablesNamesFinder<>().getTables(alter));
        assertEquals(List.of("child", "fk"), validatedNames(alter, Set.of("child", "fk")));
        assertRoundTrip(alter, Dialect.MYSQL);
    }

    @Test
    void activeForeignKeyStillVisitsItsTableAndBothColumnLists() throws Exception {
        Alter alter = parse("ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES parent (id)");
        assertEquals(Set.of("child", "parent"), new TablesNamesFinder<>().getTables(alter));
        assertEquals(List.of("child", "parent", "id", "parent_id"),
                validatedNames(alter, Set.of("child", "parent", "id", "parent_id")));
        assertRoundTrip(alter, null);
    }

    @Test
    void storedTableRenameDestinationIsInactiveWhenIndexNamesTakePrecedence() throws Exception {
        Alter alter = parse("ALTER TABLE child RENAME INDEX original TO replacement");
        AlterExpression action = first(alter);
        action.setOperation(AlterOperation.RENAME_TABLE);
        action.setNewTable(new Table("hidden_table"));
        assertFalse(alter.toString().contains("hidden_table"));
        assertEquals(Set.of("child"), new TablesNamesFinder<>().getTables(alter));
        assertEquals("hidden_table", action.getNewTable().getName());
    }

    @Test
    void defaultTraversalUsesTheSameBodyAsTheRenderer() throws Exception {
        Alter alter = parse("ALTER TABLE child ALTER COLUMN id SET DEFAULT 1");
        AlterExpression action = first(alter);
        ColumnDataType inactive = new ColumnDataType("hidden", false, new ColDataType("INT"), null);
        inactive.addColumnOptions(ColumnOption.defaultValue(new LongValue(2)));
        action.addColDataType(inactive);
        assertEquals(List.of("1"), expressions(action));
        assertFalse(action.hasActiveColumnDefinitions());
        assertRoundTrip(alter, null);
    }

    @Test
    void dropSubclassCanSelectTheGeneralBodyAfterMutation() throws Exception {
        Alter alter = parse("ALTER TABLE child DROP INDEX old_index");
        AlterExpression action = first(alter);
        action.setOperation(AlterOperation.ADD);
        action.setConstraint(first(parse("ALTER TABLE child ADD FOREIGN KEY (parent_id) "
                + "REFERENCES parent (id)")).getConstraint());
        action.addColSetDefault(ColumnSetDefault.fromExpression("hidden", new LongValue(99)));
        assertSame(action.getConstraint(), action.getActiveTableDefinition());
        assertEquals(Set.of("child", "parent"), new TablesNamesFinder<>().getTables(alter));
        assertTrue(expressions(action).isEmpty());
        assertEquals(List.of("child", "parent", "id", "parent_id"),
                validatedNames(alter, Set.of("child", "parent", "id", "parent_id")));
        assertRoundTrip(alter, null);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "ALTER FOREIGN TABLE child OPTIONS (ADD optname 'active')|POSTGRESQL",
            "ALTER TABLE child OWNER TO new_owner|POSTGRESQL",
            "ALTER TABLE child ORDER BY active_id|MYSQL"})
    void fixedBodiesIgnoreInheritedDefinitionsAndDefaults(String sql, Dialect dialect)
            throws Exception {
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
        AlterExpression action = first(alter);
        List<String> expectedExpressions = expressions(action);
        action.setConstraint(first(parse("ALTER TABLE child ADD FOREIGN KEY (hidden_id) "
                + "REFERENCES hidden_parent (id)")).getConstraint());
        action.addColDataType("hidden", new ColDataType("INT"));
        action.addColSetDefault(ColumnSetDefault.fromExpression("hidden", new LongValue(99)));
        action.setConstraintType("UNIQUE");
        action.setConstraintSymbol("hidden_key");
        action.setOperation(AlterOperation.RENAME_TABLE);
        action.setNewTable(new Table("hidden_table"));
        assertNull(action.getActiveTableDefinition());
        assertEquals(expectedExpressions, expressions(action));
        assertEquals(Set.of("child"), new TablesNamesFinder<>().getTables(alter));
        assertTrue(validatedNames(alter, Set.of("child", "active_id")).stream()
                .noneMatch(name -> name.startsWith("hidden")));
        assertRoundTrip(alter, dialect);
    }

    @Test
    void identityAlterationDoesNotVisitDormantColumnOptionsOrUsingExpression() throws Exception {
        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE child ALTER COLUMN id RESTART",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        ColumnDataType column = first(alter).getColDataTypeList().get(0);
        column.addColumnOptions(ColumnOption.defaultValue(new LongValue(99)));
        column.setUsingExpression(new LongValue(100));
        assertEquals("100", column.getUsingExpression().toString());
        assertTrue(expressions(first(alter)).isEmpty());
        assertRoundTrip(alter, Dialect.POSTGRESQL);
    }

    @Test
    void legacyConstraintHeaderVisitsKeysWithoutDormantStorageOptions() throws Exception {
        Alter alter = parse("ALTER TABLE child ADD CONSTRAINT UNIQUE KEY uq (id)");
        AlterExpression action = first(alter);
        KeyConstraint key = (KeyConstraint) action.getConstraint();
        key.setColumns(List.of(new KeyElement(new LongValue(7))));
        key.setStorageParameters(List.of(new IndexOption("hidden", new LongValue(99), true)));
        assertTrue(action.hasActiveConstraintHeader());
        assertSame(key, action.getActiveTableDefinition());
        assertEquals(List.of("7"), expressions(action));
        assertFalse(alter.toString().contains("99"));
    }

    @Test
    void specializedPrimaryKeyKeepsActiveKeysAndHashOptions() throws Exception {
        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE child ALTER PRIMARY KEY USING COLUMNS (id) "
                        + "USING HASH WITH BUCKET_COUNT = 8 WITH (fillfactor = 70)",
                parser -> parser.withDialect(Dialect.COCKROACHDB));
        assertEquals(List.of("70", "8"), expressions(first(alter)));
        assertSame(first(alter).getConstraint(), first(alter).getActiveTableDefinition());
        assertRoundTrip(alter, Dialect.COCKROACHDB);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECK (hidden_id > 0)", "REFERENCES hidden_parent (id)"})
    void columnOptionKindChangesHidePriorConstraintPayload(String optionSql) throws Exception {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE FOREIGN TABLE child (id INT " + optionSql + ") SERVER srv");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        ColumnOption option = column.getColumnOptions().get(0);
        Object stored = option.getConstraint() != null ? option.getConstraint()
                : option.getForeignKeyReference();
        option.setForeignOptions(
                List.of(new ForeignDataOption("column_name", new StringValue("active"))));
        assertTrue(stored == option.getConstraint() || stored == option.getForeignKeyReference());
        List<String> actual = new ArrayList<>();
        TableDefinitionTraversal.visit(table, expression -> actual.add(expression.toString()),
                source -> fail("Inactive reference visited: " + source));
        assertEquals(List.of("'active'"), actual);
        assertEquals(Set.of("child"), new TablesNamesFinder<>().getTables(table));
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    private static Alter parse(String sql) throws Exception {
        return (Alter) CCJSqlParserUtil.parse(sql);
    }

    private static AlterExpression first(Alter alter) {
        return alter.getAlterExpressions().get(0);
    }

    private static List<String> expressions(AlterExpression action) {
        List<String> result = new ArrayList<>();
        TableDefinitionTraversal.visit(action, expression -> result.add(expression.toString()),
                table -> {
                });
        return result;
    }

    private static List<String> validatedNames(Alter alter, Set<String> existing) {
        List<String> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name.getFqn());
            return existing.contains(name.getFqn());
        };
        AlterValidator validator = new AlterValidator();
        validator.setContext(new ValidationContext().setCapabilities(List.of(metadata)));
        validator.validate(alter);
        assertTrue(validator.getValidationErrors().isEmpty(),
                validator.getValidationErrors().toString());
        return names;
    }

    private static void assertRoundTrip(Statement statement, Dialect dialect) throws Exception {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql));
        assertEquals(statement.toString(), sql.toString());
        Statement reparsed = CCJSqlParserUtil.parse(sql.toString(), parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
        assertEquals(sql.toString(), reparsed.toString());
        assertEquals(new TablesNamesFinder<>().getTables(reparsed),
                new TablesNamesFinder<>().getTables(statement));
    }
}
