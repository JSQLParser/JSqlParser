/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayExpressionTest {

    @Test
    void testColumnArrayExpression() throws JSQLParserException {
        String sqlStr = "SELECT a[2+1] AS a";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SelectItem<?> selectItem = select.getSelectItem(0);

        Column column = selectItem.getExpression(Column.class);
        assertInstanceOf(ArrayConstructor.class, column.getArrayConstructor());
    }

}
