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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.alter.AlterExpressionPrimaryKey;
import net.sf.jsqlparser.statement.alter.AlterExpressionDrop;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.ConstraintDeclaration;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.KeyColumnSource;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintInspectionTest {
    @Test
    void combinesInlineAndTableDeclarationsInSourceOrder() throws Exception {
        CreateTable table = table("CREATE TABLE child (id INT CONSTRAINT pk PRIMARY KEY, "
                + "UNIQUE (id), parent_id INT REFERENCES parent(id), CHECK (id > 0), "
                + "code INT NOT NULL DEFAULT 1)", Dialect.POSTGRESQL);
        String before = table.toString();
        List<ConstraintDeclaration> declarations = table.getConstraints();
        assertEquals(List.of(ConstraintKind.PRIMARY_KEY, ConstraintKind.UNIQUE,
                ConstraintKind.FOREIGN_KEY, ConstraintKind.CHECK,
                ConstraintKind.NOT_NULL), kinds(declarations));
        assertSame(table.getColumnDefinitions().get(0), declarations.get(0).getColumn());
        assertEquals("pk", declarations.get(0).getConstraint().getName());
        assertSame(table.getTableConstraints().get(0), declarations.get(1).getConstraint());
        assertNull(declarations.get(1).getColumn());
        assertNull(declarations.get(1).getColumnOption());
        assertNull(declarations.get(2).getConstraint());
        assertEquals("parent", declarations.get(2).getColumnOption().getForeignKeyReference()
                .getTable().getName());
        assertEquals(2, table.getTableConstraints().size());
        assertTrue(table.getIndexes().isEmpty());
        assertEquals(before, table.toString());
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE", "UNIQUE KEY", "UNIQUE INDEX"})
    void includesMySqlTableUniquenessDeclarationsButNotOrdinaryIndexes(String keyword)
            throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT, " + keyword
                + " uk (id), KEY ix (id), FULLTEXT KEY ft (body), body TEXT)", Dialect.MYSQL);
        assertEquals(List.of(ConstraintKind.UNIQUE), kinds(table.getConstraints()));
        assertEquals(keyword, table.getConstraints().get(0).getConstraint().getType());
        assertRoundTrip(table, Dialect.MYSQL);
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void keepsStandaloneUniqueIndexesInTheirStatementContext(Dialect dialect) throws Exception {
        Statement statement = parse("CREATE UNIQUE INDEX uq ON t (id)", dialect);
        CreateIndex index = assertInstanceOf(CreateIndex.class, statement);
        assertEquals(Index.Kind.UNIQUE, index.getIndex().getKind());
        AlterExpression drop = action("ALTER TABLE t DROP INDEX uq", Dialect.MYSQL);
        assertEquals(ConstraintKind.OTHER, drop.getConstraintKind());
        assertRoundTrip(statement, dialect);
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void callersCanFindAndEditPrimaryKeyOwnersWithoutMovingInlineDeclarations(Dialect dialect)
            throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT PRIMARY KEY, label TEXT)", dialect);
        ConstraintDeclaration primary = table.getConstraints().stream()
                .filter(declaration -> declaration.getKind() == ConstraintKind.PRIMARY_KEY)
                .findFirst().get();
        primary.getColumn().setColumnName("new_id");
        assertEquals("new_id", table.getColumnDefinitions().get(0).getColumnName());
        assertTrue(table.getIndexes() == null || table.getIndexes().isEmpty());
        assertTrue(table.toString().contains("new_id INT PRIMARY KEY"));
        assertRoundTrip(table, dialect);
    }

    @Test
    void membershipIsASnapshotWhileKindsAndOwnersRemainLive() throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT, UNIQUE (id))", Dialect.POSTGRESQL);
        List<ConstraintDeclaration> snapshot = table.getConstraints();
        ConstraintDeclaration declaration = snapshot.get(0);
        NamedConstraint index = declaration.getConstraint();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        index.setType("PRIMARY KEY");
        assertEquals(ConstraintKind.PRIMARY_KEY, declaration.getKind());
        assertRoundTrip(table, Dialect.POSTGRESQL);
        String type = index.getType();
        index.setKind(ConstraintKind.OTHER);
        assertEquals(type, index.getType());
        assertEquals(ConstraintKind.OTHER, declaration.getKind());
        assertEquals(1, snapshot.size());
        assertEquals(List.of(ConstraintKind.OTHER), kinds(table.getConstraints()));
        assertSame(index, table.getTableConstraints().get(0));
        index.setType("UNIQUE");
        ColumnOption notNull = ColumnOption.nullability(false);
        table.getColumnDefinitions().get(0).addColumnOptions(notNull);
        assertEquals(1, snapshot.size());
        List<ConstraintDeclaration> refreshed = table.getConstraints();
        assertEquals(List.of(ConstraintKind.NOT_NULL, ConstraintKind.UNIQUE), kinds(refreshed));
        notNull.setNullable(true);
        assertEquals(ConstraintKind.OTHER, refreshed.get(0).getKind());
        assertEquals(List.of(ConstraintKind.UNIQUE), kinds(table.getConstraints()));
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    @Test
    void physicalIndexAndConstraintViewsMutateIndependently() throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT, KEY ix (id), "
                + "CONSTRAINT uq UNIQUE (id), CONSTRAINT ck CHECK (id > 0))", Dialect.MYSQL);
        Index index = table.getIndexes().get(0);
        NamedConstraint check = table.getTableConstraints().get(1);
        table.getTableConstraints().remove(0);
        assertSame(index, table.getTableElements().get(1));
        assertSame(check, table.getTableConstraints().get(0));
        assertEquals(List.of(ConstraintKind.CHECK), kinds(table.getConstraints()));
        KeyConstraint primary = new KeyConstraint().withType("PRIMARY KEY")
                .withColumnsNames(List.of("id"));
        table.getTableConstraints().add(0, primary);
        table.setIndexes(table.getIndexes());
        assertEquals(List.of(ConstraintKind.PRIMARY_KEY, ConstraintKind.CHECK),
                kinds(table.getConstraints()));
        assertSame(index, table.getTableElements().get(1));
        assertSame(primary, table.getTableElements().get(2));
        assertRoundTrip(table, Dialect.MYSQL);
        table.getIndexes().clear();
        assertEquals(2, table.getTableConstraints().size());
        assertRoundTrip(table, Dialect.MYSQL);
    }

    @Test
    void inlineStructuredConstraintsRemainVisibleWithUnknownKind() throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT PRIMARY KEY)", Dialect.POSTGRESQL);
        NamedConstraint primary = table.getConstraints().get(0).getConstraint();
        primary.setKind(ConstraintKind.OTHER);
        assertEquals(List.of(ConstraintKind.OTHER), kinds(table.getConstraints()));
        assertSame(primary, table.getConstraints().get(0).getConstraint());
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    @Test
    void legacyConstructionHasDocumentedOrderingAndDoesNotInterpretRawTokens() {
        CreateTable table = new CreateTable();
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"));
        column.setColumnOptions(List.of(ColumnOption.raw("PRIMARY", "KEY"),
                ColumnOption.nullability(false), ColumnOption.nullability(true)));
        table.setColumnDefinitions(List.of(column));
        KeyConstraint primary =
                new KeyConstraint().withType("PRIMARY KEY").withColumnsNames(List.of("id"));
        table.setIndexes(List.of(new Index().withType("SPATIAL INDEX")));
        table.setTableConstraints(List.of(primary));
        assertEquals(List.of(ConstraintKind.NOT_NULL, ConstraintKind.PRIMARY_KEY),
                kinds(table.getConstraints()));
        assertSame(column, table.getConstraints().get(0).getColumn());
        assertSame(primary, table.getConstraints().get(1).getConstraint());
        assertTrue(!Index.class.isAssignableFrom(KeyConstraint.class));
        assertTrue(!Index.class.isAssignableFrom(NamedConstraint.class));
        assertEquals(ConstraintKind.OTHER, ColumnOption.constraint(null).getConstraintKind());
    }

    static Stream<Arguments> alterConstraints() {
        return Stream.of(
                Arguments.of("ADD PRIMARY KEY (id)", ConstraintKind.PRIMARY_KEY,
                        Dialect.POSTGRESQL),
                Arguments.of("ADD UNIQUE (id)", ConstraintKind.UNIQUE, Dialect.POSTGRESQL),
                Arguments.of("ADD UNIQUE KEY uq (id)", ConstraintKind.UNIQUE, Dialect.MYSQL),
                Arguments.of("ADD FOREIGN KEY (id) REFERENCES parent(id)",
                        ConstraintKind.FOREIGN_KEY,
                        Dialect.POSTGRESQL),
                Arguments.of("ADD CHECK (id > 0)", ConstraintKind.CHECK, Dialect.POSTGRESQL),
                Arguments.of("ADD CONSTRAINT pk PRIMARY KEY USING INDEX ix",
                        ConstraintKind.PRIMARY_KEY,
                        Dialect.POSTGRESQL),
                Arguments.of("ADD CONSTRAINT ex EXCLUDE USING gist (id WITH =)",
                        ConstraintKind.EXCLUDE,
                        Dialect.POSTGRESQL),
                Arguments.of("DROP PRIMARY KEY", ConstraintKind.PRIMARY_KEY, Dialect.MYSQL),
                Arguments.of("DROP FOREIGN KEY fk", ConstraintKind.FOREIGN_KEY, Dialect.MYSQL),
                Arguments.of("DROP CHECK ck", ConstraintKind.CHECK, Dialect.MYSQL),
                Arguments.of("DROP CONSTRAINT pk", ConstraintKind.OTHER, Dialect.POSTGRESQL),
                Arguments.of("ALTER CONSTRAINT pk DEFERRABLE", ConstraintKind.OTHER,
                        Dialect.POSTGRESQL),
                Arguments.of("ALTER CHECK ck NOT ENFORCED", ConstraintKind.CHECK, Dialect.MYSQL),
                Arguments.of("ALTER CONSTRAINT ck NOT ENFORCED", ConstraintKind.OTHER,
                        Dialect.MYSQL),
                Arguments.of("ADD INDEX ix (id)", ConstraintKind.OTHER, Dialect.MYSQL),
                Arguments.of("DROP INDEX ix", ConstraintKind.OTHER, Dialect.MYSQL),
                Arguments.of("DROP COLUMN id", ConstraintKind.OTHER, Dialect.POSTGRESQL),
                Arguments.of("ADD COLUMN id INT PRIMARY KEY", ConstraintKind.OTHER,
                        Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @MethodSource("alterConstraints")
    void classifiesOnlyTheExplicitKindOfTheActiveAction(String sql, ConstraintKind expected,
            Dialect dialect)
            throws Exception {
        Alter alter = (Alter) parse("ALTER TABLE t " + sql, dialect);
        String before = alter.toString();
        assertEquals(expected, alter.getAlterExpressions().get(0).getConstraintKind());
        assertEquals(before, alter.toString());
        assertRoundTrip(alter, dialect);
    }

    @Test
    void changingOperationDoesNotGuessANamedDropFromAnOldDefinition() throws Exception {
        AlterExpression action = action("ALTER TABLE t ADD PRIMARY KEY (id)", Dialect.POSTGRESQL);
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP);
        action.setConstraintName("unrelated_constraint");
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        assertEquals(ConstraintKind.FOREIGN_KEY, action.getConstraintKind());
        action.setOperation(AlterOperation.ADD);
        action.setConstraintName(null);
        action.getConstraint().setType("UNIQUE");
        assertEquals(ConstraintKind.UNIQUE, action.getConstraintKind());
    }

    @Test
    void activeColumnAndCommentPayloadsHideStaleIndexMetadata() throws Exception {
        List<Consumer<AlterExpression>> edits = List.of(
                action -> action.addColSetNotNull(new AlterExpression.ColumnSetNotNull("id")),
                action -> action.addColDropNotNull(new AlterExpression.ColumnDropNotNull("id")),
                action -> action.addColDropDefault(new AlterExpression.ColumnDropDefault("id")),
                action -> action.setCommentText("'description'"));
        for (Consumer<AlterExpression> edit : edits) {
            AlterExpression action = action("ALTER TABLE t ADD PRIMARY KEY (id)",
                    Dialect.POSTGRESQL);
            edit.accept(action);
            assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
            assertTrue(!action.toString().contains("PRIMARY KEY"));
        }
    }

    static Stream<Arguments> specializedActions() {
        return Stream.of(
                Arguments.of("ALTER TABLE t ALTER CONSTRAINT ck DEFERRABLE", Dialect.POSTGRESQL),
                Arguments.of("ALTER TABLE t OWNER TO alice", Dialect.POSTGRESQL),
                Arguments.of("ALTER FOREIGN TABLE t OPTIONS (ADD filename 'file')",
                        Dialect.POSTGRESQL),
                Arguments.of("ALTER TABLE t ORDER BY id", Dialect.MYSQL),
                Arguments.of("ALTER TABLE t CONVERT TO CHARACTER SET utf8mb4", Dialect.MYSQL),
                Arguments.of("ALTER TABLE t DETACH PARTITION p", Dialect.POSTGRESQL),
                Arguments.of("ALTER TABLE t ENGINE = InnoDB", Dialect.MYSQL),
                Arguments.of("ALTER TABLE t RENAME COLUMN id TO new_id", Dialect.MYSQL));
    }

    @ParameterizedTest
    @MethodSource("specializedActions")
    void specializedActionsDoNotUseUnrelatedInheritedConstraintFields(String sql, Dialect dialect)
            throws Exception {
        Alter statement = (Alter) parse(sql, dialect);
        AlterExpression action = statement.getAlterExpressions().get(0);
        assertRoundTrip(statement, dialect);
        action.setConstraint(
                new KeyConstraint().withType("PRIMARY KEY").withColumnsNames(List.of("id")));
        action.setConstraintType("CHECK");
        action.setConstraintSymbol("unrelated");
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        action.setOperation(null);
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
    }

    @Test
    void fixedPrimaryKeyActionKeepsItsKindWhenInheritedMetadataChanges() throws Exception {
        AlterExpressionPrimaryKey action = assertInstanceOf(AlterExpressionPrimaryKey.class,
                action("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id)", Dialect.COCKROACHDB));
        String before = action.toString();
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        action.getConstraint().setKind(ConstraintKind.UNIQUE);
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraintKind());
        assertEquals(before, action.toString());
        action.setOperation(null);
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraintKind());
        assertEquals(before, action.toString());
    }

    @Test
    void dropColumnActionDoesNotBecomeAPrimaryKeyDeclarationFromStaleFields() throws Exception {
        AlterExpressionDrop action = assertInstanceOf(AlterExpressionDrop.class,
                action("ALTER TABLE t DROP COLUMN id", Dialect.POSTGRESQL));
        String before = action.toString();
        action.setConstraint(
                new KeyConstraint().withType("PRIMARY KEY").withColumnsNames(List.of("id")));
        action.setOperation(AlterOperation.ADD);
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        assertEquals(before, action.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP PRIMARY KEY", "DROP FOREIGN KEY fk", "DROP CHECK ck"})
    void distinguishesBaseRenamePrecedenceFromSpecializedDropRendering(String sql)
            throws Exception {
        AlterExpression drop = action("ALTER TABLE t " + sql, Dialect.MYSQL);
        ConstraintKind expected = drop.getConstraintKind();
        String before = drop.toString();
        Index oldName = new Index().withName("old_name");
        Index newName = new Index().withName("new_name");
        drop.setOldIndex(oldName);
        drop.setIndex(newName);
        assertEquals(expected, drop.getConstraintKind());
        assertEquals(before, drop.toString());

        AlterExpression base = new AlterExpression().withOperation(drop.getOperation());
        base.setOldIndex(oldName);
        base.setIndex(newName);
        assertTrue(base.toString().startsWith("RENAME"));
        assertEquals(ConstraintKind.OTHER, base.getConstraintKind());

        base.setOperation(AlterOperation.ALTER);
        base.setConstraintType("CHECK");
        base.setConstraintSymbol("ck");
        assertTrue(base.toString().startsWith("ALTER CHECK ck"));
        assertEquals(ConstraintKind.CHECK, base.getConstraintKind());
    }

    @Test
    void supportsLegacyOnlyKeysWithoutMistakingDropColumnsForPrimaryKeys() {
        AlterExpression legacy = new AlterExpression().withOperation(AlterOperation.ADD)
                .withPkColumns(new ArrayList<>(List.of("id")));
        assertEquals(ConstraintKind.PRIMARY_KEY, legacy.getConstraintKind());
        legacy.setOperation(AlterOperation.DROP);
        assertEquals(ConstraintKind.OTHER, legacy.getConstraintKind());
        legacy.setOperation(AlterOperation.DROP_UNIQUE);
        assertEquals(ConstraintKind.UNIQUE, legacy.getConstraintKind());
        assertEquals(ConstraintKind.OTHER, new AlterExpression().getConstraintKind());
    }

    @Test
    void existingIndexConstraintsRetainTheirEditableSourceAndHaveNoInventedKeys() throws Exception {
        AlterExpression action =
                action("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY USING INDEX ix",
                        Dialect.POSTGRESQL);
        ConstraintUsingIndex constraint = assertInstanceOf(ConstraintUsingIndex.class,
                action.getConstraint());
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraintKind());
        assertTrue(!KeyColumnSource.class.isAssignableFrom(ConstraintUsingIndex.class));
        constraint.setExistingIndexName("new_ix");
        assertTrue(action.toString().contains("USING INDEX new_ix"));
    }

    private static List<ConstraintKind> kinds(List<ConstraintDeclaration> declarations) {
        return declarations.stream().map(ConstraintDeclaration::getKind)
                .collect(Collectors.toList());
    }

    private static Statement parse(String sql, Dialect dialect) throws Exception {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static CreateTable table(String sql, Dialect dialect) throws Exception {
        return (CreateTable) parse(sql, dialect);
    }

    private static AlterExpression action(String sql, Dialect dialect) throws Exception {
        return ((Alter) parse(sql, dialect)).getAlterExpressions().get(0);
    }

    private static void assertRoundTrip(Statement statement, Dialect dialect) throws Exception {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString(), dialect).toString());
    }
}
