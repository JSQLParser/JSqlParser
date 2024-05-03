/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class TableTest {

    @Test
    public void tableIndexException() {
        Table table = new Table().withName("bla")
                .withDatabase(new Database(new Server("server", "instance"), "db"));
        assertEquals("[server\\instance].db..bla", table.toString());
    }

    @Test
    public void tableSetDatabase() {
        Table table = new Table();
        table.setName("testtable");
        Database database = new Database("default");
        table.setDatabase(database);
        assertEquals("default..testtable", table.toString());
    }

    @Test
    public void tableSetDatabaseIssue812() throws JSQLParserException {
        String sql =
                "SELECT * FROM MY_TABLE1 as T1, MY_TABLE2, (SELECT * FROM MY_DB.TABLE3) LEFT OUTER JOIN MY_TABLE4 "
                        + " WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        final Database database = new Database("default");
        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {

            @Override
            public void visit(Table tableName) {
                System.out.println(tableName);
                tableName.setDatabase(database); // Exception
                System.out.println(tableName.getDatabase());
            }
        };

        deparser.visit((PlainSelect) select);

    }

    @Test
    public void testTableRemoveNameParts() {
        Table table = new Table("link", "DICTIONARY");
        assertThat(table.getFullyQualifiedName()).isEqualTo("link.DICTIONARY");
        table.setSchemaName(null);
        assertThat(table.getFullyQualifiedName()).isEqualTo("DICTIONARY");
    }

    @Test
    public void testConstructorDelimitersInappropriateSize() {
        assertThatThrownBy(
                () -> new Table(List.of("a", "b", "c"), List.of("too", "many", "delimiters")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "the length of the delimiters list must be 1 less than nameParts");
    }
}
