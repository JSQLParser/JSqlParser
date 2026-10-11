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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterConstraintSeparationTest {
    @Test
    void uniqueConstraintAndOrdinaryIndexHaveSeparateEditableDefinitions() throws Exception {
        Alter statement = parse("ALTER TABLE t ADD UNIQUE INDEX uq (id), ADD INDEX ix (other)",
                Dialect.MYSQL);
        AlterExpression uniqueAction = statement.getAlterExpressions().get(0);
        KeyConstraint unique = assertInstanceOf(KeyConstraint.class, uniqueAction.getConstraint());
        assertNull(uniqueAction.getIndex());
        assertEquals(ConstraintKind.UNIQUE, uniqueAction.getConstraintKind());
        assertEquals("uq", unique.getIndexName());
        assertNull(unique.getName());

        AlterExpression indexAction = statement.getAlterExpressions().get(1);
        assertNull(indexAction.getConstraint());
        assertEquals(ConstraintKind.OTHER, indexAction.getConstraintKind());
        assertEquals(Index.Kind.INDEX, indexAction.getIndex().getKind());
        unique.setName("unique_symbol");
        unique.setIndexName("new_uq");
        assertEquals("new_uq", uniqueAction.getUkName());
        uniqueAction.setUkName("legacy_uq");
        assertEquals("legacy_uq", unique.getIndexName());
        uniqueAction.setUkColumns(List.of("new_id"));
        indexAction.getIndex().setName("new_ix");
        assertRoundTrip(statement,
                "ALTER TABLE t ADD CONSTRAINT unique_symbol UNIQUE INDEX legacy_uq (new_id), "
                        + "ADD  INDEX new_ix (other)",
                Dialect.MYSQL);
    }

    @Test
    void genericConstraintKeywordPathExposesACompleteUniqueDefinition() throws Exception {
        Alter statement = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uq (id)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        KeyConstraint key = assertInstanceOf(KeyConstraint.class, action.getConstraint());
        assertNull(action.getIndex());
        assertEquals(ConstraintKind.UNIQUE, key.getKind());
        assertEquals("UNIQUE KEY", key.getType());
        assertEquals("uq", key.getIndexName());
        assertEquals("CONSTRAINT UNIQUE KEY uq (id)", key.toString());
        action.setUkColumns(List.of("new_id"));
        key.setIndexName("renamed_uq");
        key.setType("UNIQUE INDEX");
        assertEquals("renamed_uq", action.getConstraintSymbol());
        assertEquals("UNIQUE INDEX", action.getConstraintType());
        assertEquals("CONSTRAINT UNIQUE INDEX renamed_uq (new_id)", key.toString());
        action.setConstraintSymbol("legacy_uq");
        assertEquals("legacy_uq", key.getIndexName());
        action.setConstraintType("UNIQUE KEY");
        assertEquals("UNIQUE KEY", key.getType());
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(statement.toString(), deparsed.toString());
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(deparsed.toString()).toString());
    }

    @Test
    void replacingDefinitionChangesTheRenderedActionAndItsClassification() throws Exception {
        Alter statement = parse("ALTER TABLE t ADD INDEX ix (id)", Dialect.MYSQL);
        AlterExpression action = statement.getAlterExpressions().get(0);
        action.setConstraint(new KeyConstraint().withType("PRIMARY KEY")
                .withColumnsNames(List.of("id")));
        assertNull(action.getIndex());
        assertEquals(ConstraintKind.PRIMARY_KEY, action.getConstraintKind());
        action.setIndex(null);
        assertRoundTrip(statement, "ALTER TABLE t ADD PRIMARY KEY (id)", Dialect.MYSQL);

        action.setIndex(
                new Index().withIndexKeyword("INDEX").withKind(Index.Kind.INDEX).withName("ix_new")
                        .withColumnsNames(List.of("id")));
        assertNull(action.getConstraint());
        action.setConstraint(null);
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        assertRoundTrip(statement, "ALTER TABLE t ADD  INDEX ix_new (id)", Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void replacingLegacyUniqueDefinitionClearsItsOldHeader(boolean replaceWithIndex)
            throws Exception {
        Alter statement = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uq (id)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        String original = statement.toString();
        action.setConstraint(action.getConstraint());
        assertEquals(original, statement.toString());
        if (replaceWithIndex) {
            action.setIndex(new Index().withIndexKeyword("INDEX").withKind(Index.Kind.INDEX)
                    .withName("ix").withColumnsNames(List.of("id")));
            assertNull(action.getConstraint());
            assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
            assertRoundTrip(statement, "ALTER TABLE t ADD  INDEX ix (id)", Dialect.MYSQL);
        } else {
            action.setConstraint(new CheckConstraint()
                    .withExpression(CCJSqlParserUtil.parseCondExpression("id > 0")));
            assertNull(action.getIndex());
            assertEquals(ConstraintKind.CHECK, action.getConstraintKind());
            assertRoundTrip(statement, "ALTER TABLE t ADD CHECK (id > 0)", Dialect.MYSQL);
        }
        assertNull(action.getConstraintType());
        assertNull(action.getConstraintSymbol());
    }

    @Test
    void clearingLegacyUniqueDefinitionClearsItsHeaderAliases() throws Exception {
        Alter statement = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uq (id)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        action.setConstraint(null);
        assertNull(action.getConstraint());
        assertNull(action.getConstraintType());
        assertNull(action.getConstraintSymbol());
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
    }

    @Test
    void namedConstraintDropDoesNotInferDefinitionFromItsNameOrInactiveFields() throws Exception {
        Alter statement = parse("ALTER TABLE t DROP CONSTRAINT pk_t", Dialect.POSTGRESQL);
        AlterExpression action = statement.getAlterExpressions().get(0);
        assertNull(action.getConstraint());
        assertNull(action.getIndex());
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        action.setConstraint(new CheckConstraint());
        action.setConstraintName("new_constraint");
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        assertRoundTrip(statement, "ALTER TABLE t DROP CONSTRAINT new_constraint",
                Dialect.POSTGRESQL);
    }

    @Test
    void constraintRenameUsesNamesWithoutCreatingIndexDefinitions() throws Exception {
        Alter statement = parse("ALTER TABLE t RENAME CONSTRAINT old_name TO new_name",
                Dialect.POSTGRESQL);
        AlterExpression action = statement.getAlterExpressions().get(0);
        assertNull(action.getIndex());
        assertNull(action.getOldIndex());
        assertNull(action.getConstraint());
        assertEquals("old_name", action.getConstraintName());
        assertEquals("new_name", action.getNewConstraintName());
        action.setConstraintName("source_name");
        action.setNewConstraintName("target_name");
        assertEquals(ConstraintKind.OTHER, action.getConstraintKind());
        assertRoundTrip(statement, "ALTER TABLE t RENAME CONSTRAINT source_name TO target_name",
                Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "KEY"})
    void indexRenameRetainsIndexTargetsAndKeyword(String keyword) throws Exception {
        Alter statement = parse("ALTER TABLE t RENAME " + keyword + " old_name TO new_name",
                Dialect.MYSQL);
        AlterExpression action = statement.getAlterExpressions().get(0);
        assertNull(action.getConstraint());
        assertNull(action.getConstraintName());
        assertNull(action.getNewConstraintName());
        action.getOldIndex().setName("source_name");
        action.getIndex().setName("target_name");
        assertRoundTrip(statement,
                "ALTER TABLE t RENAME " + keyword + " source_name TO target_name", Dialect.MYSQL);
    }

    private static Alter parse(String sql, Dialect dialect) throws Exception {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static void assertRoundTrip(Alter statement, String sql, Dialect dialect)
            throws Exception {
        assertEquals(sql, statement.toString());
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, deparsed.toString());
        assertEquals(sql, parse(deparsed.toString(), dialect).toString());
    }
}
