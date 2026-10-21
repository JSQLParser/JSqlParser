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

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
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
            "CREATE TABLE t (a INT, INDEX i (a))",
            "ALTER TABLE t ALTER COLUMN c SET INVISIBLE",
            "ALTER TABLE t ALTER COLUMN c SET VISIBLE",
            "ALTER TABLE t ALTER c SET INVISIBLE",
            "ALTER TABLE t ALTER INDEX i INVISIBLE",
            "ALTER TABLE t ALTER INDEX i VISIBLE",
            "ALTER TABLE t ALTER COLUMN c SET VISIBLE, ALTER INDEX i VISIBLE",
            "ALTER TABLE t FORCE",
            "ALTER TABLE t FORCE, ALGORITHM = INPLACE",
            "CREATE TABLE t (a JSON DEFAULT (JSON_ARRAY()))",
            "ALTER TABLE t ADD COLUMN j JSON DEFAULT (JSON_OBJECT())"})
    void rendersSingleSpaces(String sql) throws JSQLParserException {
        assertRendersExactly(sql, Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ENABLE ROW LEVEL SECURITY",
            "ALTER TABLE t DISABLE ROW LEVEL SECURITY",
            "ALTER TABLE t FORCE ROW LEVEL SECURITY",
            "ALTER TABLE t NO FORCE ROW LEVEL SECURITY",
            "ALTER TABLE t ENABLE ROW LEVEL SECURITY, FORCE ROW LEVEL SECURITY",
            "CREATE INDEX ON t (a)"})
    void rendersSingleSpacesForPostgreSql(String sql) throws JSQLParserException {
        assertRendersExactly(sql, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t FORCE",
            "ALTER TABLE t MODIFY (c INT)",
            "ALTER TABLE t MODIFY COLUMN (c INT NOT NULL)",
            "ALTER TABLE t ADD (c INT), ADD d INT",
            "ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uk (a)",
            "SELECT JSON_ARRAY() FROM t",
            "SELECT JSON_OBJECT() FROM t"})
    void rendersSingleSpacesWithoutDialect(String sql) throws JSQLParserException {
        assertRendersExactly(sql, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT JSON_ARRAY(1, 2) AS a FROM t",
            "SELECT JSON_ARRAY(NULL ON NULL) AS a FROM t",
            "SELECT JSON_OBJECT(KEY 'a' VALUE 1) AS o FROM t",
            "SELECT JSON_OBJECT(ABSENT ON NULL) AS o FROM t"})
    void jsonConstructorsAddNoDoubleOrTrailingSpace(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        String rendered = statement.toString();
        assertFalse(rendered.contains("  "), rendered);
        assertEquals(rendered, deparse(statement));
    }

    @Test
    void columnVisibilityHasSingleSpaces() {
        assertEquals("c SET INVISIBLE",
                new AlterExpression.ColumnSetVisibility("c", false).toString());
        assertEquals("c SET VISIBLE",
                new AlterExpression.ColumnSetVisibility("c", true).toString());
    }

    @Test
    void exchangePartitionHasNoTrailingSpace() {
        AlterExpression expression = new AlterExpression()
                .withOperation(AlterOperation.EXCHANGE_PARTITION);
        expression.setPartitions(List.of("p0"));
        expression.setExchangePartitionTableName("t2");
        expression.setExchangePartitionWithValidation(true);
        assertEquals("EXCHANGE PARTITION p0 WITH TABLE t2 WITH VALIDATION",
                expression.toString());
    }

    @Test
    void indexWithoutDeclarationHasNoLeadingSpace() throws JSQLParserException {
        CreateIndex createIndex = (CreateIndex) CCJSqlParserUtil.parse("CREATE INDEX ON t (a)",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("(a)", createIndex.getIndex().toString());
        assertEquals("(a)", new Index().withColumnsNames(List.of("a")).toString());
        assertEquals("INDEX i INVISIBLE", new Index().withIndexKeyword("INDEX").withName("i")
                .withIndexSpec(List.of("INVISIBLE")).toString());
    }

    private static void assertRendersExactly(String sql, Dialect dialect)
            throws JSQLParserException {
        Statement statement = dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
        assertEquals(sql, statement.toString());
        assertEquals(sql, deparse(statement));
    }

    private static String deparse(Statement statement) {
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        return deparsed.toString();
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
