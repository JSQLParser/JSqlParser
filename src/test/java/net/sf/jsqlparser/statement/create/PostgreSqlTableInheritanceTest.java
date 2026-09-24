/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTableInheritanceTest {
    @ParameterizedTest
    @ValueSource(strings = {"parent", "public.parent", "public.parent, other.parent2",
            "\"Odd.Schema\".\"Parent.Name\""})
    void modelsAndVisitsParentTables(String parents) throws JSQLParserException {
        for (String definition : new String[] {"()", "(extra INT)", "(LIKE source, extra INT)"}) {
            CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                    "CREATE TABLE child " + definition + " INHERITS (" + parents
                            + ") WITH (fillfactor = 80)");
            List<Table> inherited = table.getInherits();
            assertEquals(parents.split(", ").length, inherited.size());
            assertEquals(parents.split(", ")[0], inherited.get(0).getFullyQualifiedName());
            List<Table> visited = new ArrayList<>();
            TableDefinitionTraversal.visit(table, e -> {
            }, visited::add);
            for (Table parent : inherited) {
                assertTrue(visited.stream().anyMatch(t -> t == parent));
                assertTrue(new TablesNamesFinder().getTables(table)
                        .contains(parent.getFullyQualifiedName()));
            }
            roundTrip(table);
            inherited.set(0, new Table("replacement"));
            assertTrue(table.toString().contains("INHERITS (replacement"));
            roundTrip(table);
        }
    }

    @Test
    void preservesEmptyDefinitionsAndCanRemoveInheritance() throws JSQLParserException {
        CreateTable table =
                (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE child () INHERITS (parent)");
        assertNotNull(table.getTableElements());
        assertTrue(table.getTableElements().isEmpty());
        table.setInherits(null);
        assertEquals("CREATE TABLE child ()", table.toString());
        roundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(table + "; SELECT 1").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"()", "(parent,)", "(parent AS p)", "(parent + 1)"})
    void rejectsMalformedParentLists(String parents) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("CREATE TABLE child () INHERITS " + parents));
    }

    private static void roundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        table.accept(new StatementDeParser(out));
        assertEquals(table.toString(), out.toString());
        assertEquals(out.toString(), CCJSqlParserUtil.parse(out.toString()).toString());
    }
}
