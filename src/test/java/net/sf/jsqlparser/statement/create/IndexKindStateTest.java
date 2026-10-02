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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.DefaultConstraint;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.Index.Kind;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class IndexKindStateTest {
    @ParameterizedTest
    @CsvSource({"unique key, UNIQUE", "UNIQUE INDEX, UNIQUE", "KEY, INDEX", "INDEX, INDEX",
            "FULLTEXT KEY, FULLTEXT", "SPATIAL INDEX, SPATIAL", "BITMAP INDEX, INDEX"})
    void replacingIndexTypeRefreshesEveryPreviousClassification(String type, Kind expected) {
        for (Kind previous : Kind.values()) {
            Index index = new Index().withType("UNIQUE").withKind(previous);
            index.setType(type);
            assertEquals(type, index.getType());
            assertEquals(expected, index.getKind(), previous + " -> " + type);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"CUSTOM", "UNIQUE_CUSTOM", "PRIMARY_CUSTOM", "MONKEY", "INDEXED",
            "PRIMARY KEY", "FOREIGN KEY", "CHECK", "EXCLUDE", "DEFAULT", "NOT NULL"})
    void constraintAndUnknownTypesAreNotPhysicalIndexKinds(String type) {
        Index index = new Index().withType("UNIQUE").withType(type);
        assertEquals(type, index.getType());
        assertEquals(Kind.OTHER, index.getKind());
    }

    @Test
    void classificationDoesNotRewriteSpelling() {
        Index index = new Index().withType("  unique\tkey  ");
        assertEquals(Kind.UNIQUE, index.getKind());
        assertEquals("  unique\tkey  ", index.getType());
        KeyConstraint constraint = new KeyConstraint().withType("  unique\tkey  ");
        assertEquals(ConstraintKind.UNIQUE, constraint.getKind());
        constraint.setKind(ConstraintKind.PRIMARY_KEY);
        assertEquals("  unique\tkey  ", constraint.getType());
        constraint.setType("PRIMARY KEY");
        assertEquals(ConstraintKind.PRIMARY_KEY, constraint.getKind());
    }

    @Test
    void metadataAndFixedConstraintKindsRemainSeparate() {
        Index index = new Index().withKind(Kind.INDEX);
        assertNull(index.getType());
        assertEquals(Kind.INDEX, index.getKind());
        assertEquals(ConstraintKind.CHECK, new CheckConstraint().getKind());
        assertEquals(ConstraintKind.EXCLUDE, new ExcludeConstraint().getKind());
        assertEquals(ConstraintKind.DEFAULT, new DefaultConstraint().getKind());
    }

    @ParameterizedTest
    @CsvSource({"UNIQUE, PRIMARY KEY, PRIMARY_KEY", "PRIMARY KEY, UNIQUE, UNIQUE"})
    void rewritingConstraintPreservesKindThroughDeparsing(String before, String after,
            ConstraintKind expected) throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE index_state (id INT, CONSTRAINT key_state " + before + " (id))");
        KeyConstraint constraint = (KeyConstraint) table.getTableConstraints().get(0);
        constraint.setType(after);
        assertEquals(expected, constraint.getKind());
        CreateTable reparsed = (CreateTable) assertSqlCanBeParsedAndDeparsed(table.toString());
        assertEquals(expected, reparsed.getTableConstraints().get(0).getKind());
        assertEquals("key_state", reparsed.getTableConstraints().get(0).getName());
        assertEquals(0, reparsed.getIndexes().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE", "UNIQUE KEY", "UNIQUE INDEX"})
    void tableUniquenessRequiresAConstraintNodeRatherThanChangingAnIndexType(String type)
            throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE index_state (id INT, KEY key_state (id))");
        table.getIndexes().clear();
        table.getTableConstraints().add(new KeyConstraint().withType(type)
                .withIndexName("key_state").withColumnsNames(java.util.List.of("id")));
        CreateTable reparsed = (CreateTable) assertSqlCanBeParsedAndDeparsed(table.toString());
        assertEquals(0, reparsed.getIndexes().size());
        assertEquals(ConstraintKind.UNIQUE, reparsed.getTableConstraints().get(0).getKind());
    }
}
