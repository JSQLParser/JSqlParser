/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class DateUnitExpressionTest {

    @Test
    void testParsing() throws JSQLParserException {
        String sqlStr = "SELECT Last_Day( DATE '2024-12-31', month ) as month";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Function f = select.getSelectItem(0).getExpression(Function.class);
        Assertions.assertThat(f.getParameters().get(1)).isInstanceOf(DateUnitExpression.class);
    }
}
