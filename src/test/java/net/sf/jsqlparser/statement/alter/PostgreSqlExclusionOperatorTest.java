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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlExclusionOperatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"OPERATOR(pg_catalog.&&)", "OPERATOR(&&)", "OPERATOR(\"My.Schema\".&&)",
            "OPERATOR(pg_catalog.=)", "&&"})
    void supportsSharedCreateAndAlterGrammar(String operator) throws JSQLParserException {
        for (String sql : new String[] {
                "CREATE TABLE t (c INT4RANGE, CONSTRAINT ex EXCLUDE USING gist (c WITH " + operator
                        + "))",
                "ALTER TABLE t ADD CONSTRAINT ex EXCLUDE USING gist (c WITH " + operator + ")"}) {
            Statement statement = parse(sql);
            assertEquals(sql, statement.toString());
            assertEquals(operator,
                    constraint(statement).getColumns().get(0).getExclusionOperator());
            roundTrip(statement);
        }
    }

    @Test
    void exposesEditableSchemaAndSymbolAndSupportsLegacySetter() throws JSQLParserException {
        Statement statement = parse(
                "ALTER TABLE t ADD CONSTRAINT ex EXCLUDE USING gist (c WITH OPERATOR(pg_catalog.&&))");
        Index.ColumnParams key = constraint(statement).getColumns().get(0);
        ExclusionOperator operator = key.getExclusionOperatorReference();
        assertEquals("pg_catalog", operator.getSchemaName());
        assertEquals("&&", operator.getName());
        assertTrue(operator.isUseOperatorKeyword());
        operator.setSchemaName("\"My.Schema\"");
        operator.setName("=");
        assertEquals("OPERATOR(\"My.Schema\".=)", key.getExclusionOperator());
        roundTrip(statement);
        key.setExclusionOperator("&&");
        assertEquals("&&", key.getExclusionOperator());
        assertNull(key.getExclusionOperatorReference().getSchemaName());
        roundTrip(statement);
        key.setExclusionOperator(null);
        assertNull(key.getExclusionOperatorReference());
        key.setExclusionOperatorReference(new ExclusionOperator().withSchemaName("pg_catalog")
                .withName("&&").withUseOperatorKeyword(true));
        roundTrip(statement);
    }

    @Test
    void preservesElementOptionsAndMultipleElementsAndDefaultSyntax() throws JSQLParserException {
        roundTrip(parse(
                "ALTER TABLE t ADD EXCLUDE USING gist (c WITH OPERATOR(pg_catalog.&&), d WITH =) WHERE (c IS NOT NULL) DEFERRABLE INITIALLY DEFERRED"
                        .replace("\n", "")));
        String legacy = "CREATE TABLE t (c INT4RANGE, EXCLUDE USING gist (c WITH &&))";
        assertEquals(legacy, CCJSqlParserUtil.parse(legacy).toString());
        roundTrip(parse(
                "ALTER TABLE t ADD EXCLUDE USING gist ((c) WITH OPERATOR(pg_catalog.&&)), ADD COLUMN extra INT"));
    }

    private static ExcludeConstraint constraint(Statement statement) {
        return (ExcludeConstraint) (statement instanceof Alter
                ? ((Alter) statement).getAlterExpressions().get(0).getIndex()
                : ((CreateTable) statement).getIndexes().get(0));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}
