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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AlterForeignKeyStateTest {
    private static final String FK_SQL = "ALTER TABLE child ADD CONSTRAINT fk FOREIGN KEY "
            + "(parent_id) REFERENCES app.parent(id) ON DELETE CASCADE";

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void legacyEditsUpdateParsedForeignKeysAndBothRenderers(Dialect dialect) throws Exception {
        Alter statement = parse(FK_SQL, dialect);
        AlterExpression action = first(statement);
        ForeignKeyConstraint foreignKey = foreignKey(action);
        assertEquals(List.of("parent_id"), action.getFkColumns());
        assertEquals("app", action.getFkSourceSchema());
        assertEquals("parent", action.getFkSourceTable());
        assertSame(foreignKey.getReferencedColumnNames(), action.getFkSourceColumns());
        assertSame(foreignKey.getReferentialAction(Type.DELETE),
                action.getReferentialAction(Type.DELETE));

        action.setFkColumns(List.of("owner_id"));
        action.setFkSourceSchema("archive");
        action.setFkSourceTable("owners");
        action.setFkSourceColumns(new ArrayList<>(List.of("owner_key")));
        action.setReferentialAction(Type.DELETE, Action.RESTRICT);

        assertSame(foreignKey, action.getConstraint());
        assertEquals("fk", foreignKey.getName());
        assertRoundTrip("ALTER TABLE child ADD CONSTRAINT fk FOREIGN KEY (owner_id) "
                + "REFERENCES archive.owners(owner_key) ON DELETE RESTRICT", statement, dialect);
    }

    @Test
    void actionColumnListsAndAttributesSurviveLegacyAndStructuredMutations() throws Exception {
        Alter statement = parse("ALTER TABLE posts ADD CONSTRAINT fk FOREIGN KEY "
                + "(tenant_id, author_id) REFERENCES users(tenant_id, id) MATCH SIMPLE "
                + "ON DELETE SET NULL (author_id) DEFERRABLE INITIALLY DEFERRED NOT VALID",
                Dialect.POSTGRESQL);
        AlterExpression action = first(statement);
        ForeignKeyConstraint foreignKey = foreignKey(action);
        ReferentialAction delete = foreignKey.getReferentialAction(Type.DELETE);
        assertSame(delete, action.getReferentialAction(Type.DELETE));
        action.setOnDeleteSetNull(true);
        assertSame(delete, action.getReferentialAction(Type.DELETE));
        assertEquals(List.of("author_id"), delete.getColumnNames());

        action.getReferentialAction(Type.DELETE).setColumnNames(List.of("tenant_id"));
        action.setReferentialAction(Type.DELETE, Action.SET_DEFAULT);
        assertSame(delete, foreignKey.getReferentialAction(Type.DELETE));
        assertEquals(List.of("tenant_id"), delete.getColumnNames());
        assertRoundTrip("ALTER TABLE posts ADD CONSTRAINT fk FOREIGN KEY "
                + "(tenant_id, author_id) REFERENCES users(tenant_id, id) MATCH SIMPLE "
                + "ON DELETE SET DEFAULT (tenant_id) DEFERRABLE INITIALLY DEFERRED NOT VALID",
                statement, Dialect.POSTGRESQL);

        delete.setAction(Action.CASCADE);
        action.removeReferentialAction(Type.DELETE);
        assertNull(foreignKey.getReferentialAction(Type.DELETE));
        assertFalse(action.isOnDeleteSetNull());
        assertRoundTrip("ALTER TABLE posts ADD CONSTRAINT fk FOREIGN KEY "
                + "(tenant_id, author_id) REFERENCES users(tenant_id, id) MATCH SIMPLE "
                + "DEFERRABLE INITIALLY DEFERRED NOT VALID", statement, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void builderSetterOrderAndFluentSchemaPreserveOneReference(boolean schemaFirst)
            throws Exception {
        AlterExpression action = new AlterExpression();
        action.setReferentialAction(Type.DELETE, Action.CASCADE);
        if (schemaFirst) {
            action.withFkSourceSchema("archive").withFkSourceTable("parents");
        } else {
            action.withFkSourceTable("parents").withFkSourceSchema("archive");
        }
        action.withFkColumns(List.of("parent_id")).withFkSourceColumns(List.of("id"));
        assertNull(action.getOperation());
        action.setOperation(AlterOperation.ADD);
        assertEquals("parents", action.getFkSourceTable());
        assertEquals("archive", action.getFkSourceSchema());
        assertEquals(ConstraintKind.FOREIGN_KEY, action.getConstraintKind());
        assertRoundTrip("ALTER TABLE child ADD FOREIGN KEY (parent_id) "
                + "REFERENCES archive.parents(id) ON DELETE CASCADE", statement(action),
                Dialect.POSTGRESQL);
    }

    @Test
    void keySnapshotsAndFluentAdditionsUseTheCurrentStructuredColumns() throws Exception {
        Alter statement = parse(FK_SQL, Dialect.POSTGRESQL);
        AlterExpression action = first(statement);
        ForeignKeyConstraint foreignKey = foreignKey(action);
        foreignKey.setColumnsNames(List.of("account_id"));
        List<String> snapshot = action.getFkColumns();
        assertEquals(List.of("account_id"), snapshot);
        snapshot.set(0, "detached");
        assertEquals(List.of("account_id"), action.getFkColumns());
        action.addFkColumns("tenant_id").addFkColumns(List.of("region_id"));
        action.addFkSourceColumns("tenant_id").addFkSourceColumns(List.of("region_id"));
        action.getFkSourceColumns().set(0, "account_key");
        assertSame(foreignKey.getReferencedColumnNames(), action.getFkSourceColumns());
        assertEquals(List.of("account_id", "tenant_id", "region_id"),
                foreignKey.getColumnsNames());
        assertRoundTrip("ALTER TABLE child ADD CONSTRAINT fk FOREIGN KEY "
                + "(account_id, tenant_id, region_id) REFERENCES app.parent"
                + "(account_key, tenant_id, region_id) ON DELETE CASCADE", statement,
                Dialect.POSTGRESQL);
    }

    @Test
    void referenceReplacementAndNameEditsPreserveOtherQualifiedNameParts() throws Exception {
        AlterExpression action = first(parse(FK_SQL, Dialect.POSTGRESQL));
        Table table = new Table("new_schema", "new_parent");
        table.setDatabase(new Database("catalog"));
        ForeignKeyReference replacement = new ForeignKeyReference().withTable(table)
                .withReferencedColumnNames(new ArrayList<>(List.of("new_id")))
                .withReferentialAction(Type.UPDATE, Action.NO_ACTION);
        foreignKey(action).setReference(replacement);
        assertEquals("new_schema", action.getFkSourceSchema());
        assertEquals("new_parent", action.getFkSourceTable());
        assertSame(replacement.getReferencedColumnNames(), action.getFkSourceColumns());
        assertNull(action.getReferentialAction(Type.DELETE));
        assertSame(replacement.getReferentialAction(Type.UPDATE),
                action.getReferentialAction(Type.UPDATE));

        action.setFkSourceSchema("\"changed.schema\"");
        action.setFkSourceTable("\"changed.parent\"");
        assertSame(table, replacement.getTable());
        assertEquals("catalog", table.getDatabase().getDatabaseName());
        assertEquals("\"changed.schema\"", table.getSchemaName());
        assertEquals("\"changed.parent\"", table.getName());
    }

    @Test
    void replacingOrClearingTheConstraintDoesNotRestoreDetachedForeignKeyState()
            throws Exception {
        AlterExpression action = first(parse(FK_SQL, Dialect.POSTGRESQL));
        ForeignKeyConstraint old = foreignKey(action);
        ForeignKeyConstraint replacement = new ForeignKeyConstraint()
                .withColumnsNames(List.of("replacement_id"))
                .withTable(new Table("replacement_parent"))
                .withReferencedColumnNames(List.of("id"));
        action.setConstraint(replacement);
        assertEquals(List.of("replacement_id"), action.getFkColumns());
        assertEquals("replacement_parent", action.getFkSourceTable());
        assertNull(action.getFkSourceSchema());
        assertNull(action.getReferentialAction(Type.DELETE));
        assertEquals(Action.CASCADE, old.getReferentialAction(Type.DELETE).getAction());
        action.setConstraint(null);
        assertNoForeignKeyValues(action);
        action.setFkSourceTable("fresh_parent");
        assertNull(action.getFkColumns());
        assertNull(action.getFkSourceColumns());
        assertNull(action.getReferentialAction(Type.DELETE));
        assertEquals("fresh_parent", foreignKey(action).getTable().getName());
    }

    @Test
    void absentReadsClearsAndRemovalsDoNotCreateAForeignKey() {
        AlterExpression action = new AlterExpression();
        assertNoForeignKeyValues(action);
        clearForeignKeyValues(action);
        assertNull(action.getConstraint());
        assertNull(action.getIndex());
        assertNull(action.getOperation());
    }

    @Test
    void storedForeignKeyDefinitionDoesNotChangeADropTarget() throws Exception {
        Alter statement = parse("ALTER TABLE child DROP FOREIGN KEY fk", Dialect.MYSQL);
        AlterExpression action = first(statement);
        ForeignKeyConstraint stale = new ForeignKeyConstraint().withName("stale")
                .withColumnsNames(List.of("unused"))
                .withTable(new Table("unused_parent"))
                .withReferencedColumnNames(List.of("unused_id"));
        action.setConstraint(stale);
        assertSame(stale, action.getConstraint());
        assertEquals(ConstraintKind.FOREIGN_KEY, action.getConstraintKind());
        assertRoundTrip("ALTER TABLE child DROP FOREIGN KEY fk", statement, Dialect.MYSQL);
    }

    static Stream<AlterExpression> nonForeignKeyDefinitions() {
        return Stream.of(
                new AlterExpression().withOperation(AlterOperation.ADD).withIndex(
                        new Index().withType("INDEX").withName("ix")
                                .withColumnsNames(List.of("id"))),
                new AlterExpression().withOperation(AlterOperation.ADD).withConstraint(
                        new KeyConstraint().withType("UNIQUE").withColumnsNames(List.of("id"))),
                new AlterExpression().withOperation(AlterOperation.ADD)
                        .withPkColumns(List.of("id")),
                new AlterExpression().withOperation(AlterOperation.ADD)
                        .withUkColumns(List.of("id")),
                new AlterExpressionPrimaryKey().withPkColumns(List.of("id")));
    }

    @ParameterizedTest
    @MethodSource("nonForeignKeyDefinitions")
    void legacyForeignKeySettersDoNotOverwriteOtherDefinitions(AlterExpression action) {
        Index originalIndex = action.getIndex();
        NamedConstraint originalConstraint = action.getConstraint();
        List<String> originalPkColumns = action.getPkColumns();
        List<String> originalUkColumns = action.getUkColumns();
        String before = action.toString();
        assertNoForeignKeyValues(action);
        clearForeignKeyValues(action);
        assertThrows(IllegalStateException.class, () -> action.setFkColumns(List.of("fk")));
        assertThrows(IllegalStateException.class, () -> action.setFkSourceSchema("schema"));
        assertThrows(IllegalStateException.class, () -> action.setFkSourceTable("parent"));
        assertThrows(IllegalStateException.class, () -> action.setFkSourceColumns(List.of("id")));
        assertThrows(IllegalStateException.class,
                () -> action.setReferentialAction(Type.DELETE, Action.CASCADE));
        assertSame(originalIndex, action.getIndex());
        assertSame(originalConstraint, action.getConstraint());
        assertEquals(originalPkColumns, action.getPkColumns());
        assertEquals(originalUkColumns, action.getUkColumns());
        assertEquals(before, action.toString());
    }

    private static void assertNoForeignKeyValues(AlterExpression action) {
        assertNull(action.getFkColumns());
        assertNull(action.getFkSourceSchema());
        assertNull(action.getFkSourceTable());
        assertNull(action.getFkSourceColumns());
        assertNull(action.getReferentialAction(Type.DELETE));
        assertFalse(action.isOnDeleteCascade());
        assertFalse(action.isOnDeleteRestrict());
        assertFalse(action.isOnDeleteSetNull());
    }

    private static void clearForeignKeyValues(AlterExpression action) {
        action.setFkColumns(null);
        action.setFkSourceSchema(null);
        action.setFkSourceTable(null);
        action.setFkSourceColumns(null);
        action.removeReferentialAction(Type.DELETE);
        action.setOnDeleteCascade(false);
        action.setOnDeleteRestrict(false);
        action.setOnDeleteSetNull(false);
    }

    private static ForeignKeyConstraint foreignKey(AlterExpression action) {
        return assertInstanceOf(ForeignKeyConstraint.class, action.getConstraint());
    }

    private static AlterExpression first(Alter statement) {
        return statement.getAlterExpressions().get(0);
    }

    private static Alter statement(AlterExpression action) {
        Alter statement = new Alter();
        statement.setTable(new Table("child"));
        statement.addAlterExpression(action);
        return statement;
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static void assertRoundTrip(String expected, Alter statement, Dialect dialect)
            throws JSQLParserException {
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(output.toString(), dialect).toString());
    }
}
