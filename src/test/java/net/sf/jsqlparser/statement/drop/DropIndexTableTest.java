/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.DropDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DropIndexTableTest {
    @ParameterizedTest
    @ValueSource(strings = {"t", "app.t", "`t.x`", "`s.p`.`t.x`", "`한글`"})
    void mysqlOwnersAndOptions(String owner) throws Exception {
        for (String suffix : List.of("", " ALGORITHM = INPLACE LOCK = NONE", " LOCK SHARED",
                " ALGORITHM COPY", " LOCK EXCLUSIVE ALGORITHM DEFAULT", " LOCK DEFAULT")) {
            String sql = "DROP INDEX `i.x` ON " + owner + suffix;
            for (Drop drop : roundTrip(sql, Dialect.MYSQL)) {
                assertEquals("`i.x`", drop.getName().getName());
                assertEquals(owner, drop.getTable().getFullyQualifiedName());
                assertEquals(List.of("ON", owner), drop.getParameters().subList(0, 2));
                assertEquals(Set.of(owner), new TablesNamesFinder().getTables(drop));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"[t]", "[dbo].[t]", "[db].[dbo].[t]"})
    void sqlServerOwners(String owner) throws Exception {
        for (Drop drop : roundTrip("DROP INDEX IF EXISTS [ix] ON " + owner, Dialect.SQLSERVER)) {
            assertEquals(owner, drop.getTable().getFullyQualifiedName());
            assertTrue(drop.isIfExists());
        }
    }

    @Test
    void mutationAndLegacyParametersStayConsistent() throws Exception {
        Drop drop = parse("DROP INDEX i ON t LOCK NONE", Dialect.MYSQL);
        drop.getTable().setName("renamed");
        assertEquals(List.of("ON", "renamed", "LOCK", "NONE"), drop.getParameters());
        List<String> snapshot = drop.getParameters();
        snapshot.set(1, "ignored");
        assertEquals("renamed", drop.getTable().getName());
        drop.addParameters("ALGORITHM", "DEFAULT");
        assertEquals("DROP INDEX i ON renamed LOCK NONE ALGORITHM DEFAULT", drop.toString());
        drop.setTable(null);
        assertEquals("DROP INDEX i LOCK NONE ALGORITHM DEFAULT", drop.toString());
        drop.withTable(new Table("next"));
        drop.setParameters(List.of("ON", "legacy"));
        assertNull(drop.getTable());
        assertEquals("DROP INDEX i ON legacy", drop.toString());
        assertThrows(IllegalArgumentException.class, () -> drop.setTable(new Table("t"), 99));
    }

    @Test
    void builderAndParameterOrder() {
        Drop drop = new Drop().withType("INDEX").withName(new Table("i"))
                .addParameters("EXTENSION");
        drop.setTable(new Table("t"), 1);
        drop.addParameters(List.of("LOCK", "NONE"));
        assertEquals("DROP INDEX i EXTENSION ON t LOCK NONE", drop.toString());
        assertEquals(List.of("EXTENSION", "ON", "t", "LOCK", "NONE"), drop.getParameters());
    }

    @Test
    void doesNotInventOwnersOrDiscoverCatalogNamesAsTables() throws Exception {
        for (String sql : List.of("DROP INDEX public.i", "DROP SCHEMA s", "DROP SEQUENCE seq",
                "DROP FUNCTION f(integer)", "DROP TRIGGER tr")) {
            Drop drop = parse(sql, Dialect.POSTGRESQL);
            assertNull(drop.getTable());
            assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        }
        assertEquals(Set.of("a", "b"), new TablesNamesFinder().getTables(
                parse("DROP TABLE a, b", Dialect.POSTGRESQL)));
        assertEquals(Set.of("v"), new TablesNamesFinder().getTables(
                parse("DROP MATERIALIZED VIEW v", Dialect.POSTGRESQL)));
    }

    @Test
    void customDeparserRewritesOwnerOnly() throws Exception {
        Drop drop = parse("DROP INDEX i ON t LOCK NONE", Dialect.MYSQL);
        StringBuilder builder = new StringBuilder();
        SelectDeParser tables = new SelectDeParser() {
            @Override
            public <S> StringBuilder visit(Table table, S context) {
                assertEquals("ctx", context);
                return getBuilder().append("new_t");
            }
        };
        drop.accept(new StatementDeParser(new ExpressionDeParser(), tables, builder), "ctx");
        assertEquals("DROP INDEX i ON new_t LOCK NONE", builder.toString());
        assertEquals("DROP INDEX i ON t LOCK NONE", drop.toString());
    }

    @Test
    void malformedOwnersAndStatementBoundaries() throws Exception {
        for (String sql : List.of("DROP INDEX i ON", "DROP INDEX i ON t ON u")) {
            assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL));
        }
        assertEquals(2, CCJSqlParserUtil.parseStatements(
                "DROP INDEX i ON t; LOCK TABLE u IN SHARE MODE").size());
    }

    private static Drop parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static List<Drop> roundTrip(String sql, Dialect dialect) throws Exception {
        Drop drop = parse(sql, dialect);
        StringBuilder builder = new StringBuilder();
        new DropDeParser(builder).deParse(drop);
        assertEquals(drop.toString(), builder.toString());
        return List.of(drop, parse(drop.toString(), dialect), parse(builder.toString(), dialect));
    }
}
