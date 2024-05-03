/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionDelimiterTest {

    @Test
    public void testColumnWithDifferentDelimiters() throws JSQLParserException {
        String statement = "SELECT mytable.mycolumn:parent:child FROM mytable";
        PlainSelect parsed = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement);
        Column column = parsed.getSelectItem(0).getExpression(Column.class);
        assertEquals(":", column.getTableDelimiter());
        assertEquals(List.of(":", "."), column.getTable().getNamePartDelimiters());
    }

    @Test
    public void testColumnWithEmptyNameParts() throws JSQLParserException {
        String statement = "SELECT mytable.:.child FROM mytable";
        PlainSelect parsed = (PlainSelect) assertSqlCanBeParsedAndDeparsed(statement);
        Column column = parsed.getSelectItem(0).getExpression(Column.class);
        assertEquals(".", column.getTableDelimiter());
        assertEquals(List.of(":", "."), column.getTable().getNamePartDelimiters());
    }
}
