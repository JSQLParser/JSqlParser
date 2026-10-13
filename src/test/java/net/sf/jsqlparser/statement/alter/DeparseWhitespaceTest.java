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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/** Renderers separate words with exactly one space and add no trailing space. */
class DeparseWhitespaceTest {

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t DROP PRIMARY KEY",
            "ALTER TABLE t DROP PRIMARY KEY CASCADE",
            "ALTER TABLE t DROP PRIMARY KEY, DROP COLUMN c",
            "RENAME TABLE t TO t2",
            "RENAME TABLE a TO b, c TO d",
            "RENAME TABLE IF EXISTS a TO b",
            "RENAME a TO b",
            "ALTER TABLE t ADD INDEX i (body (10))",
            "ALTER TABLE t ADD KEY k (a, b)",
            "CREATE TABLE t (a INT, INDEX i (a))"})
    void rendersSingleSpaces(String sql) throws JSQLParserException {
        Statement statement =
                CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(sql, statement.toString());

        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, deparsed.toString());
    }

    @Test
    void indexDeclarationHasNoLeadingSpace() {
        assertEquals("i2", new Index().withName("i2").toString());
        assertEquals("KEY k (a)", new Index().withIndexKeyword("KEY").withName("k")
                .withColumnsNames(java.util.List.of("a")).toString());
        assertEquals("UNIQUE INDEX u (a)", new Index().withType("UNIQUE").withIndexKeyword("INDEX")
                .withName("u").withColumnsNames(java.util.List.of("a")).toString());
    }
}
