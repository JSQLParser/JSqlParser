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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class AlterKeyStateTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void legacyBuildersCreateOneKeyAndExposeOnlySnapshots(boolean primary) throws Exception {
        AlterExpression action = new AlterExpression();
        assertNull(action.getKeyConstraint());
        assertNull(action.getPkColumns());
        assertNull(action.getUkColumns());
        setColumns(action, primary, null);
        action.setUkName(null);
        action.setUkTypeSpecified(false);
        assertNull(action.getConstraint());

        List<String> input = new ArrayList<>(List.of("\"key.name\""));
        setColumns(action, primary, input);
        input.add("detached");
        KeyConstraint key = action.getKeyConstraint();
        assertSame(key, action.getConstraint());
        assertEquals(primary ? ConstraintKind.PRIMARY_KEY : ConstraintKind.UNIQUE, key.getKind());
        columns(action, primary).add("also_detached");
        assertEquals(List.of("\"key.name\""), key.getColumnsNames());
        assertNull(action.getOperation());
        action.setOperation(AlterOperation.ADD);
        roundTrip(statement(action), Dialect.POSTGRESQL);
        setColumns(action, primary, List.of("other"));
        assertSame(key, action.getKeyConstraint());
        assertEquals(List.of("other"), key.getColumnsNames());
        roundTrip(statement(action), Dialect.POSTGRESQL);
        setColumns(action, primary, null);
        assertSame(key, action.getKeyConstraint());
        assertTrue(key.getColumns().isEmpty());
    }

    @Test
    void parsedPrimaryKeyPreservesAttributesAndUsesTheSameEditableElements() throws Exception {
        Alter statement = parse("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (id) "
                + "INCLUDE (payload) DEFERRABLE", Dialect.POSTGRESQL);
        AlterExpression action = first(statement);
        KeyConstraint key = action.getKeyConstraint();
        assertEquals(Boolean.TRUE, key.getConstraintAttributes().getDeferrable());
        assertEquals(List.of("payload"), key.getIncludeColumns());
        action.setPkColumns(List.of("\"new.id\""));
        assertEquals(List.of("\"new.id\""), key.getColumnsNames());
        KeyElement replacement = new KeyElement("final_id");
        key.setColumns(List.of(replacement));
        assertSame(replacement, action.getKeyConstraint().getColumns().get(0));
        assertEquals(List.of("final_id"), action.getPkColumns());
        assertEquals("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (final_id) "
                + "INCLUDE (payload) DEFERRABLE", statement.toString());
        roundTrip(statement, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE", "UNIQUE KEY", "UNIQUE INDEX"})
    void uniqueNameAndKeywordAliasesFollowTheStructuredDefinition(String type) throws Exception {
        Alter statement = parse("ALTER TABLE t ADD " + type + " uq (id)", Dialect.MYSQL);
        AlterExpression action = first(statement);
        KeyConstraint key = action.getKeyConstraint();
        assertEquals(type.endsWith("KEY"), action.getUk());
        assertEquals(!"UNIQUE".equals(type), action.isUkTypeSpecified());
        key.setIndexName("typed_name");
        assertEquals("typed_name", action.getUkName());
        action.setUkName("legacy_name");
        assertEquals("legacy_name", key.getIndexName());
        action.setUk(true);
        assertTrue(action.getUk());
        assertTrue(action.isUkTypeSpecified());
        assertEquals("ALTER TABLE t ADD UNIQUE KEY legacy_name (id)", statement.toString());
        roundTrip(statement, Dialect.MYSQL);
        action.setUk(false);
        assertFalse(action.getUk());
        assertEquals("ALTER TABLE t ADD UNIQUE INDEX legacy_name (id)", statement.toString());
        roundTrip(statement, Dialect.MYSQL);
        action.setUkTypeSpecified(false);
        assertFalse(action.isUkTypeSpecified());
        assertEquals("ALTER TABLE t ADD UNIQUE legacy_name (id)", statement.toString());
        roundTrip(statement, Dialect.MYSQL);
        action.setUkTypeSpecified(true);
        assertFalse(action.getUk());
        assertTrue(action.isUkTypeSpecified());
        roundTrip(statement, Dialect.MYSQL);
    }

    @Test
    void genericNamedUniqueKeywordEditsKeepTheLegacyHeaderInSync() throws Exception {
        Alter statement = parse("ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uq (id)", null);
        AlterExpression action = first(statement);
        action.setUk(false);
        assertEquals("UNIQUE INDEX", action.getConstraintType());
        assertFalse(action.getUk());
        assertTrue(action.isUkTypeSpecified());
        roundTrip(statement, null);
        action.setUkTypeSpecified(false);
        assertEquals("UNIQUE", action.getConstraintType());
        assertFalse(action.isUkTypeSpecified());
        roundTrip(statement, null);
    }

    @Test
    void replacementsAndClearsCannotRestoreOldKeyNamesOrKeywords() {
        AlterExpression action = new AlterExpression().withUkName("old_name").withUk(true)
                .withUkColumns(List.of("old_column"));
        KeyConstraint old = action.getKeyConstraint();
        KeyConstraint replacement = new KeyConstraint().withType("UNIQUE")
                .withColumnsNames(List.of("new_column"));
        action.setConstraint(replacement);
        assertNull(action.getUkName());
        assertFalse(action.getUk());
        assertFalse(action.isUkTypeSpecified());
        assertEquals(List.of("new_column"), action.getUkColumns());
        assertEquals("old_name", old.getIndexName());
        action.setConstraint(null);
        assertNull(action.getKeyConstraint());
        assertNull(action.getUkColumns());
        assertNull(action.getUkName());
        action.setUkName("fresh");
        assertNotSame(old, action.getKeyConstraint());
        assertNotSame(replacement, action.getKeyConstraint());
        assertNull(action.getKeyConstraint().getColumns());
        assertFalse(action.isUkTypeSpecified());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void settersRejectCompetingDefinitionsAndNullClearsLeaveThemIntact(boolean primary) {
        List<NamedConstraint> competitors = List.of(
                new KeyConstraint().withType(primary ? "UNIQUE" : "PRIMARY KEY"),
                new CheckConstraint(), new ForeignKeyConstraint(), new ConstraintUsingIndex());
        for (NamedConstraint competitor : competitors) {
            AlterExpression action = new AlterExpression().withConstraint(competitor);
            setColumns(action, primary, null);
            assertSame(competitor, action.getConstraint());
            assertThrows(IllegalStateException.class,
                    () -> setColumns(action, primary, List.of("id")));
            assertSame(competitor, action.getConstraint());
        }
        AlterExpression action = new AlterExpression();
        Index index = new Index().withType("INDEX").withName("ix");
        action.setIndex(index);
        setColumns(action, primary, null);
        assertThrows(IllegalStateException.class,
                () -> setColumns(action, primary, List.of("id")));
        assertSame(index, action.getIndex());
        assertNull(action.getKeyConstraint());
        action.setIndex(null);
        action.setFkColumns(List.of("fk_column"));
        assertThrows(IllegalStateException.class,
                () -> setColumns(action, primary, List.of("id")));
        assertEquals(List.of("fk_column"), action.getFkColumns());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP (\"a.b\", c) CASCADE CONSTRAINTS",
            "DROP UNIQUE (\"a.b\", c)", "DROP FOREIGN KEY (\"a.b\", c)"})
    void parsedDropTargetsAreOwnedAndSeparateFromKeyDefinitions(String body) throws Exception {
        Alter statement = parse("ALTER TABLE t " + body, null);
        AlterExpression action = first(statement);
        assertNull(action.getConstraint());
        assertNull(action.getKeyConstraint());
        assertEquals(List.of("\"a.b\"", "c"), action.getDropColumns());
        List<String> snapshot = action.getPkColumns();
        snapshot.set(0, "detached");
        assertEquals("\"a.b\"", action.getDropColumns().get(0));
        action.getDropColumns().set(0, "\"new.name\"");
        assertEquals(List.of("\"new.name\"", "c"), action.getPkColumns());
        roundTrip(statement, null);
        List<String> replacement = new ArrayList<>(List.of("other"));
        action.setPkColumns(replacement);
        replacement.add("detached");
        assertEquals(List.of("other"), action.getDropColumns());
        action.addDropColumns("next").addDropColumns(List.of("last"));
        assertEquals(List.of("other", "next", "last"), action.getDropColumns());
        roundTrip(statement, null);
        action.setPkColumns(null);
        assertNull(action.getDropColumns());
        assertNull(action.getConstraint());
    }

    @ParameterizedTest
    @EnumSource(value = AlterOperation.class, names = {"DROP", "DROP_UNIQUE", "DROP_FOREIGN_KEY"})
    void newDropBuilderDoesNotDependOnSetterOrder(AlterOperation operation) throws Exception {
        AlterExpression action = new AlterExpression().withDropColumns(List.of("\"a.b\""));
        assertNull(action.getConstraint());
        assertNull(action.getPkColumns());
        action.setOperation(operation);
        assertEquals(List.of("\"a.b\""), action.getPkColumns());
        roundTrip(statement(action), null);
    }

    @Test
    void changingOperationDoesNotConvertDefinitionsIntoDropTargets() throws Exception {
        AlterExpression action = new AlterExpression().withPkColumns(List.of("id"))
                .withDropColumns(List.of("old_id")).withOperation(AlterOperation.ADD);
        KeyConstraint key = action.getKeyConstraint();
        assertEquals("ADD PRIMARY KEY (id)", action.toString());
        action.setOperation(AlterOperation.DROP);
        assertEquals("DROP (old_id)", action.toString());
        assertEquals(List.of("old_id"), action.getPkColumns());
        action.setPkColumns(List.of("replacement"));
        assertEquals(List.of("id"), key.getColumnsNames());
        assertEquals("DROP (replacement)", action.toString());
        action.setOperation(AlterOperation.ADD);
        assertEquals("ADD PRIMARY KEY (id)", action.toString());
        assertEquals(List.of("replacement"), action.getDropColumns());
        roundTrip(statement(action), Dialect.POSTGRESQL);
        action.setDropColumns(null);
        assertSame(key, action.getKeyConstraint());
        assertNull(action.getDropColumns());
    }

    @Test
    void namedAndPrimaryKeyDropsDoNotInventColumnTargets() throws Exception {
        Alter statement =
                parse("ALTER TABLE t DROP FOREIGN KEY fk, DROP PRIMARY KEY", Dialect.MYSQL);
        for (AlterExpression action : statement.getAlterExpressions()) {
            assertNull(action.getDropColumns());
            assertNull(action.getPkColumns());
            assertNull(action.getKeyConstraint());
        }
        roundTrip(statement, Dialect.MYSQL);
    }

    private static List<String> columns(AlterExpression action, boolean primary) {
        return primary ? action.getPkColumns() : action.getUkColumns();
    }

    private static void setColumns(AlterExpression action, boolean primary, List<String> columns) {
        if (primary) {
            action.setPkColumns(columns);
        } else {
            action.setUkColumns(columns);
        }
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
    }

    private static AlterExpression first(Alter statement) {
        return statement.getAlterExpressions().get(0);
    }

    private static Alter statement(AlterExpression action) {
        Alter statement = new Alter();
        statement.setTable(new Table("t"));
        statement.addAlterExpression(action);
        return statement;
    }

    private static void roundTrip(Alter statement, Dialect dialect) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql));
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString(), dialect).toString());
    }
}
