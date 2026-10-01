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
import net.sf.jsqlparser.statement.create.table.Index.Kind;
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
        assertEquals(List.of(Kind.PRIMARY_KEY, Kind.UNIQUE, Kind.FOREIGN_KEY, Kind.CHECK,
                Kind.NOT_NULL, Kind.DEFAULT), kinds(declarations));
        assertSame(table.getColumnDefinitions().get(0), declarations.get(0).getColumn());
        assertEquals("pk", declarations.get(0).getIndex().getName());
        assertSame(table.getIndexes().get(0), declarations.get(1).getIndex());
        assertNull(declarations.get(1).getColumn());
        assertNull(declarations.get(1).getColumnOption());
        assertNull(declarations.get(2).getIndex());
        assertEquals("parent", declarations.get(2).getColumnOption().getForeignKeyReference()
                .getTable().getName());
        assertEquals(2, table.getIndexes().size());
        assertEquals(before, table.toString());
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE", "UNIQUE KEY", "UNIQUE INDEX"})
    void includesMySqlTableUniquenessDeclarationsButNotOrdinaryIndexes(String keyword)
            throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT, " + keyword
                + " uk (id), KEY ix (id), FULLTEXT KEY ft (body), body TEXT)", Dialect.MYSQL);
        assertEquals(List.of(Kind.UNIQUE), kinds(table.getConstraints()));
        assertEquals(keyword, table.getConstraints().get(0).getIndex().getType());
        assertRoundTrip(table, Dialect.MYSQL);
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void keepsStandaloneUniqueIndexesInTheirStatementContext(Dialect dialect) throws Exception {
        Statement statement = parse("CREATE UNIQUE INDEX uq ON t (id)", dialect);
        CreateIndex index = assertInstanceOf(CreateIndex.class, statement);
        assertEquals(Kind.UNIQUE, index.getIndex().getKind());
        AlterExpression drop = action("ALTER TABLE t DROP INDEX uq", Dialect.MYSQL);
        assertEquals(Kind.OTHER, drop.getConstraintKind());
        assertRoundTrip(statement, dialect);
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void callersCanFindAndEditPrimaryKeyOwnersWithoutMovingInlineDeclarations(Dialect dialect)
            throws Exception {
        CreateTable table = table("CREATE TABLE t (id INT PRIMARY KEY, label TEXT)", dialect);
        ConstraintDeclaration primary = table.getConstraints().stream()
                .filter(declaration -> declaration.getKind() == Kind.PRIMARY_KEY).findFirst().get();
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
        Index index = declaration.getIndex();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        index.setType("PRIMARY KEY");
        assertEquals(Kind.PRIMARY_KEY, declaration.getKind());
        assertRoundTrip(table, Dialect.POSTGRESQL);
        String type = index.getType();
        index.setKind(Kind.INDEX);
        assertEquals(type, index.getType());
        assertEquals(Kind.OTHER, declaration.getKind());
        assertEquals(1, snapshot.size());
        assertTrue(table.getConstraints().isEmpty());
        index.setType("UNIQUE");
        ColumnOption notNull = ColumnOption.nullability(false);
        table.getColumnDefinitions().get(0).addColumnOptions(notNull);
        assertEquals(1, snapshot.size());
        List<ConstraintDeclaration> refreshed = table.getConstraints();
        assertEquals(List.of(Kind.NOT_NULL, Kind.UNIQUE), kinds(refreshed));
        notNull.setNullable(true);
        assertEquals(Kind.OTHER, refreshed.get(0).getKind());
        assertEquals(List.of(Kind.UNIQUE), kinds(table.getConstraints()));
        assertRoundTrip(table, Dialect.POSTGRESQL);
    }

    @Test
    void legacyConstructionHasDocumentedOrderingAndDoesNotInterpretRawTokens() {
        CreateTable table = new CreateTable();
        ColumnDefinition column = new ColumnDefinition("id", new ColDataType("INT"));
        column.setColumnOptions(List.of(ColumnOption.raw("PRIMARY", "KEY"),
                ColumnOption.nullability(false), ColumnOption.nullability(true)));
        table.setColumnDefinitions(List.of(column));
        Index primary = new Index().withType("PRIMARY KEY").withColumnsNames(List.of("id"));
        table.setIndexes(List.of(new Index().withType("SPATIAL INDEX"), primary));
        assertEquals(List.of(Kind.NOT_NULL, Kind.PRIMARY_KEY), kinds(table.getConstraints()));
        assertSame(column, table.getConstraints().get(0).getColumn());
        assertSame(primary, table.getConstraints().get(1).getIndex());
        assertEquals(Kind.OTHER, ColumnOption.constraint(new Index().withType("KEY"))
                .getConstraintKind());
        assertEquals(Kind.OTHER, ColumnOption.constraint(null).getConstraintKind());
    }

    static Stream<Arguments> alterConstraints() {
        return Stream.of(
                Arguments.of("ADD PRIMARY KEY (id)", Kind.PRIMARY_KEY, Dialect.POSTGRESQL),
                Arguments.of("ADD UNIQUE (id)", Kind.UNIQUE, Dialect.POSTGRESQL),
                Arguments.of("ADD UNIQUE KEY uq (id)", Kind.UNIQUE, Dialect.MYSQL),
                Arguments.of("ADD FOREIGN KEY (id) REFERENCES parent(id)", Kind.FOREIGN_KEY,
                        Dialect.POSTGRESQL),
                Arguments.of("ADD CHECK (id > 0)", Kind.CHECK, Dialect.POSTGRESQL),
                Arguments.of("ADD CONSTRAINT pk PRIMARY KEY USING INDEX ix", Kind.PRIMARY_KEY,
                        Dialect.POSTGRESQL),
                Arguments.of("ADD CONSTRAINT ex EXCLUDE USING gist (id WITH =)", Kind.EXCLUDE,
                        Dialect.POSTGRESQL),
                Arguments.of("DROP PRIMARY KEY", Kind.PRIMARY_KEY, Dialect.MYSQL),
                Arguments.of("DROP FOREIGN KEY fk", Kind.FOREIGN_KEY, Dialect.MYSQL),
                Arguments.of("DROP CHECK ck", Kind.CHECK, Dialect.MYSQL),
                Arguments.of("DROP CONSTRAINT pk", Kind.OTHER, Dialect.POSTGRESQL),
                Arguments.of("ALTER CONSTRAINT pk DEFERRABLE", Kind.OTHER, Dialect.POSTGRESQL),
                Arguments.of("ALTER CHECK ck NOT ENFORCED", Kind.CHECK, Dialect.MYSQL),
                Arguments.of("ALTER CONSTRAINT ck NOT ENFORCED", Kind.OTHER, Dialect.MYSQL),
                Arguments.of("ADD INDEX ix (id)", Kind.OTHER, Dialect.MYSQL),
                Arguments.of("DROP INDEX ix", Kind.OTHER, Dialect.MYSQL),
                Arguments.of("DROP COLUMN id", Kind.OTHER, Dialect.POSTGRESQL),
                Arguments.of("ADD COLUMN id INT PRIMARY KEY", Kind.OTHER, Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @MethodSource("alterConstraints")
    void classifiesOnlyTheExplicitKindOfTheActiveAction(String sql, Kind expected, Dialect dialect)
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
        assertEquals(Kind.PRIMARY_KEY, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP);
        action.setConstraintName("unrelated_constraint");
        assertEquals(Kind.OTHER, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        assertEquals(Kind.FOREIGN_KEY, action.getConstraintKind());
        action.setOperation(AlterOperation.ADD);
        action.setConstraintName(null);
        action.getIndex().setType("UNIQUE");
        assertEquals(Kind.UNIQUE, action.getConstraintKind());
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
            assertEquals(Kind.OTHER, action.getConstraintKind());
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
        action.setIndex(new Index().withType("PRIMARY KEY").withColumnsNames(List.of("id")));
        action.setConstraintType("CHECK");
        action.setConstraintSymbol("unrelated");
        assertEquals(Kind.OTHER, action.getConstraintKind());
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        assertEquals(Kind.OTHER, action.getConstraintKind());
        action.setOperation(null);
        assertEquals(Kind.OTHER, action.getConstraintKind());
    }

    @Test
    void fixedPrimaryKeyActionKeepsItsKindWhenInheritedMetadataChanges() throws Exception {
        AlterExpressionPrimaryKey action = assertInstanceOf(AlterExpressionPrimaryKey.class,
                action("ALTER TABLE t ALTER PRIMARY KEY USING COLUMNS (id)", Dialect.COCKROACHDB));
        String before = action.toString();
        action.setOperation(AlterOperation.DROP_FOREIGN_KEY);
        action.getIndex().setKind(Kind.UNIQUE);
        assertEquals(Kind.PRIMARY_KEY, action.getConstraintKind());
        assertEquals(before, action.toString());
        action.setOperation(null);
        assertEquals(Kind.PRIMARY_KEY, action.getConstraintKind());
        assertEquals(before, action.toString());
    }

    @Test
    void dropColumnActionDoesNotBecomeAPrimaryKeyDeclarationFromStaleFields() throws Exception {
        AlterExpressionDrop action = assertInstanceOf(AlterExpressionDrop.class,
                action("ALTER TABLE t DROP COLUMN id", Dialect.POSTGRESQL));
        String before = action.toString();
        action.setIndex(new Index().withType("PRIMARY KEY").withColumnsNames(List.of("id")));
        action.setOperation(AlterOperation.ADD);
        assertEquals(Kind.OTHER, action.getConstraintKind());
        assertEquals(before, action.toString());
    }

    @Test
    void supportsLegacyOnlyKeysWithoutMistakingDropColumnsForPrimaryKeys() {
        AlterExpression legacy = new AlterExpression().withOperation(AlterOperation.ADD)
                .withPkColumns(new ArrayList<>(List.of("id")));
        assertEquals(Kind.PRIMARY_KEY, legacy.getConstraintKind());
        legacy.setOperation(AlterOperation.DROP);
        assertEquals(Kind.OTHER, legacy.getConstraintKind());
        legacy.setOperation(AlterOperation.DROP_UNIQUE);
        assertEquals(Kind.UNIQUE, legacy.getConstraintKind());
        assertEquals(Kind.OTHER, new AlterExpression().getConstraintKind());
    }

    @Test
    void existingIndexConstraintsRetainTheirEditableSourceAndHaveNoInventedKeys() throws Exception {
        AlterExpression action =
                action("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY USING INDEX ix",
                        Dialect.POSTGRESQL);
        ConstraintUsingIndex constraint = assertInstanceOf(ConstraintUsingIndex.class,
                action.getIndex());
        assertEquals(Kind.PRIMARY_KEY, action.getConstraintKind());
        assertTrue(constraint.getColumns() == null || constraint.getColumns().isEmpty());
        constraint.setExistingIndexName("new_ix");
        assertTrue(action.toString().contains("USING INDEX new_ix"));
    }

    private static List<Kind> kinds(List<ConstraintDeclaration> declarations) {
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
