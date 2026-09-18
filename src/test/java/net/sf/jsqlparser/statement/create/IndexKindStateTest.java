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
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.DefaultConstraint;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.Index.Kind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class IndexKindStateTest {

    @ParameterizedTest
    @CsvSource({"PRIMARY KEY, PRIMARY_KEY", "unique key, UNIQUE", "UNIQUE INDEX, UNIQUE",
            "KEY, INDEX", "INDEX, INDEX", "FULLTEXT KEY, FULLTEXT", "SPATIAL INDEX, SPATIAL",
            "FOREIGN KEY, FOREIGN_KEY", "CHECK, CHECK", "EXCLUDE, EXCLUDE", "DEFAULT, DEFAULT",
            "BITMAP INDEX, INDEX"})
    void replacingTypeRefreshesEveryPreviousClassification(String type, Kind expected) {
        for (Kind previous : Kind.values()) {
            Index index = new Index().withType("UNIQUE").withKind(previous);
            index.setType(type);
            assertEquals(type, index.getType());
            assertEquals(expected, index.getKind(), previous + " -> " + type);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"CUSTOM", "UNIQUE_CUSTOM", "PRIMARY_CUSTOM", "MONKEY", "INDEXED"})
    void clearingOrReplacingWithCustomTypeClearsStaleKind(String type) {
        Index index = new Index().withType("UNIQUE").withType(type);
        assertEquals(type, index.getType());
        assertEquals(Kind.OTHER, index.getKind());
    }

    @Test
    void classificationDoesNotRewriteSpellingOrDependOnLeadingWhitespace() {
        Index index = new Index().withType("  unique\tkey  ");
        assertEquals(Kind.UNIQUE, index.getKind());
        assertEquals("  unique\tkey  ", index.getType());
    }

    @Test
    void explicitMetadataSupportsOmittedTypeAndSpecializedConstraints() {
        Index index = new Index().withKind(Kind.INDEX);
        assertNull(index.getType());
        assertEquals(Kind.INDEX, index.getKind());
        index.setType("UNIQUE");
        assertEquals(Kind.UNIQUE, index.getKind());
        assertEquals(Kind.CHECK, new CheckConstraint().getKind());
        assertEquals(Kind.EXCLUDE, new ExcludeConstraint().getKind());
        assertEquals(Kind.DEFAULT, new DefaultConstraint().getKind());
    }

    // Both the original and rewritten SQL execute on MySQL 8.4 and PostgreSQL 18.
    @ParameterizedTest
    @CsvSource({"UNIQUE, PRIMARY KEY, PRIMARY_KEY", "PRIMARY KEY, UNIQUE, UNIQUE"})
    void rewritingPostgreSqlConstraintPreservesKindThroughDeparsing(String before, String after,
            Kind expected) throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE index_state (id INT, CONSTRAINT key_state " + before + " (id))");
        Index index = table.getIndexes().get(0);
        index.setType(after);
        assertEquals(expected, index.getKind());
        CreateTable reparsed = (CreateTable) assertSqlCanBeParsedAndDeparsed(table.toString());
        assertEquals(expected, reparsed.getIndexes().get(0).getKind());
        assertEquals("key_state", reparsed.getIndexes().get(0).getName());
    }

    // MySQL KEY and INDEX are aliases; UNIQUE changes the constraint's meaning.
    @ParameterizedTest
    @CsvSource({"UNIQUE KEY, KEY, INDEX", "KEY, UNIQUE KEY, UNIQUE",
            "UNIQUE INDEX, INDEX, INDEX", "INDEX, UNIQUE INDEX, UNIQUE"})
    void rewritingMySqlIndexPreservesKindThroughDeparsing(String before, String after,
            Kind expected) throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE index_state (id INT, " + before + " key_state (id))");
        table.getIndexes().get(0).setType(after);
        assertEquals(expected, table.getIndexes().get(0).getKind());
        CreateTable reparsed = (CreateTable) assertSqlCanBeParsedAndDeparsed(table.toString());
        assertEquals(expected, reparsed.getIndexes().get(0).getKind());
    }
}
