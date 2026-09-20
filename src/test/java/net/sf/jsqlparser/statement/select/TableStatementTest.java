/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableStatementTest {
    private TableStatement parse(String sql) throws JSQLParserException {
        return (TableStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "TABLE inventory.items",
            "TABLE `inventory`.`items` ORDER BY id DESC LIMIT 2 OFFSET 1",
            "TABLE `db.with.dot`.`table.with.dot` LIMIT 1",
            "TABLE items ORDER BY id LIMIT 2"
    })
    void preservesTableIdentity(String sql) throws JSQLParserException {
        TableStatement statement = parse(sql);
        String name = statement.getTable().getFullyQualifiedName();
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);

        assertEquals(sql, statement.toString());
        assertEquals(statement.toString(), output.toString());
        assertEquals(name, parse(statement.toString()).getTable().getFullyQualifiedName());
        assertEquals(Set.of(name), new TablesNamesFinder().getTables((Statement) statement));
    }

    @Test
    void rendersChangedDatabase() throws JSQLParserException {
        TableStatement statement = parse("TABLE old_db.items ORDER BY id LIMIT 1");
        statement.getTable().setSchemaName("new_db");
        String expected = "TABLE new_db.items ORDER BY id LIMIT 1";
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(expected, statement.toString());
        assertEquals(expected, output.toString());
        assertEquals("new_db", parse(expected).getTable().getSchemaName());
    }
}
