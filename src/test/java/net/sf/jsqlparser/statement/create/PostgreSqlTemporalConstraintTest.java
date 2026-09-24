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

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTemporalConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {"PRIMARY KEY", "UNIQUE", "UNIQUE NULLS NOT DISTINCT"})
    void modelsTemporalKeysAcrossCreateAndAlter(String kind) throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (id INT, valid DATERANGE, ",
                "ALTER TABLE t ADD ")) {
            Statement statement = parse(prefix + "CONSTRAINT temporal_key " + kind
                    + " (id, valid WITHOUT OVERLAPS) INCLUDE (label) DEFERRABLE INITIALLY DEFERRED"
                    + (prefix.startsWith("CREATE") ? ", label TEXT)" : ", ADD COLUMN extra INT"));
            Index index = index(statement);
            assertEquals("temporal_key", index.getName());
            assertTrue(statement.toString().contains("CONSTRAINT temporal_key"));
            assertEquals(2, index.getColumns().size());
            assertFalse(index.getColumns().get(0).isWithoutOverlaps());
            Index.ColumnParams last = index.getColumns().get(1);
            assertTrue(last.isWithoutOverlaps());
            assertEquals("valid", last.getColumnName());
            assertNull(last.getOperatorClass());
            assertEquals(List.of("label"), index.getIncludeColumns());
            roundTrip(statement);
            last.setWithoutOverlaps(false);
            assertFalse(statement.toString().contains("WITHOUT OVERLAPS"));
            roundTrip(statement);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " (id, PERIOD valid)", " (id, PERIOD \"Valid Range\")"})
    void sharesReferencingAndReferencedPeriodLists(String referenced) throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (id INT, valid DATERANGE, ",
                "ALTER TABLE t ADD ")) {
            Statement statement =
                    parse(prefix + "FOREIGN KEY (id, PERIOD valid) REFERENCES app.parent"
                            + referenced + " ON DELETE NO ACTION ON UPDATE NO ACTION"
                            + (prefix.startsWith("CREATE") ? ")" : ", ADD COLUMN extra INT"));
            ForeignKeyIndex foreign = (ForeignKeyIndex) index(statement);
            assertFalse(foreign.getColumns().get(0).isPeriod());
            assertTrue(foreign.getColumns().get(1).isPeriod());
            assertEquals("valid", foreign.getColumns().get(1).getColumnName());
            assertEquals(!referenced.isEmpty(), foreign.getReference().isUsingPeriod());
            assertEquals(Set.of("t", "app.parent"), new TablesNamesFinder().getTables(statement));
            roundTrip(statement);
            foreign.getColumns().get(1).setPeriod(false);
            foreign.getReference().setUsingPeriod(false);
            assertFalse(statement.toString().contains("PERIOD"));
            roundTrip(statement);
        }
    }

    @Test
    void preservesPeriodAndEnforcementTogether() throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (id INT, valid DATERANGE, ",
                "ALTER TABLE t ADD ")) {
            Statement statement = parse(prefix
                    + "CONSTRAINT fk FOREIGN KEY (id, PERIOD valid) REFERENCES p(id, PERIOD valid)"
                    + " ON DELETE NO ACTION NOT ENFORCED"
                    + (prefix.startsWith("CREATE") ? ")" : ""));
            ForeignKeyIndex foreign = (ForeignKeyIndex) index(statement);
            assertTrue(foreign.getColumns().get(1).isPeriod());
            assertTrue(foreign.getReference().isUsingPeriod());
            assertEquals(false, foreign.getConstraintAttributes().getEnforced());
            assertNull(foreign.getReference().getConstraintAttributes());
            roundTrip(statement);
            foreign.getConstraintAttributes().setEnforced(true);
            assertTrue(statement.toString().endsWith(prefix.startsWith("CREATE")
                    ? " ENFORCED)"
                    : " ENFORCED"));
            roundTrip(statement);
        }
    }

    @Test
    void retainsQuotedAndUnquotedIdentifiersAndLegacyProjections() throws JSQLParserException {
        CreateTable table = (CreateTable) parse("CREATE TABLE t (id INT, period DATERANGE, "
                + "FOREIGN KEY (id, period) REFERENCES p(id, period))");
        ForeignKeyIndex foreign = (ForeignKeyIndex) index(table);
        assertFalse(foreign.getColumns().get(1).isPeriod());
        assertFalse(foreign.getReference().isUsingPeriod());
        roundTrip(table);
        table = (CreateTable) parse("CREATE TABLE t (id INT, \"PERIOD\" DATERANGE, "
                + "FOREIGN KEY (id, PERIOD \"PERIOD\") REFERENCES p(id, PERIOD \"PERIOD\"))");
        foreign = (ForeignKeyIndex) index(table);
        assertEquals(List.of("id", "\"PERIOD\""), foreign.getReferencedColumnNames());
        foreign.getReferencedColumnNames().set(1, "replacement");
        assertTrue(table.toString().contains("PERIOD replacement"));
        roundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(table + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void preservesNamedUniqueConstraintsWithAndWithoutTemporalKeys() throws JSQLParserException {
        Statement ordinary = parse("ALTER TABLE t ADD CONSTRAINT uq UNIQUE (id)");
        assertEquals("uq", index(ordinary).getName());
        assertEquals("ALTER TABLE t ADD CONSTRAINT uq UNIQUE (id)", ordinary.toString());
        roundTrip(ordinary);
        Statement temporal = parse("ALTER TABLE t ADD CONSTRAINT uq "
                + "UNIQUE (id, valid WITHOUT OVERLAPS)");
        assertEquals("uq", index(temporal).getName());
        assertTrue(index(temporal).getColumns().get(1).isWithoutOverlaps());
        roundTrip(temporal);
    }

    @Test
    void keepsOrdinaryIndexesAndCrossDialectForeignKeys() throws JSQLParserException {
        String mysql = "CREATE TABLE t (id INT, FOREIGN KEY (id) REFERENCES p (id))";
        assertNotNull(CCJSqlParserUtil.parse(mysql));
        assertEquals("valid WITHOUT OVERLAPS",
                new Index.ColumnParams("valid").withWithoutOverlaps(true).toString());
        assertEquals("PERIOD valid", new Index.ColumnParams("valid").withPeriod(true).toString());
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE INDEX ix ON t (id, valid WITHOUT OVERLAPS)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE (valid WITHOUT OVERLAPS, id)",
            "PRIMARY KEY (id, valid WITHOUT)",
            "UNIQUE (id, valid WITHOUT OVERLAPS WITHOUT OVERLAPS)",
            "FOREIGN KEY (PERIOD valid, id) REFERENCES p(id, valid)",
            "FOREIGN KEY (id, PERIOD valid) REFERENCES p(PERIOD valid, id)"})
    void rejectsMalformedTemporalMarkers(String constraint) {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t (id INT, valid DATERANGE, " + constraint + ")"));
    }

    private static Index index(Statement statement) {
        return statement instanceof CreateTable ? ((CreateTable) statement).getIndexes().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getIndex();
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString()).toString());
    }
}
