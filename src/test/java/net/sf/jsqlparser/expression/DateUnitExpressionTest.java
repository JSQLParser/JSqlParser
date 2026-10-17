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
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
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

    @Test
    void testUnitArgumentKeepsItsSpelling() throws JSQLParserException {
        String sqlStr = "SELECT DATEADD(day, 1, d) FROM t";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Function f = select.getSelectItem(0).getExpression(Function.class);
        DateUnitExpression unit = (DateUnitExpression) f.getParameters().get(0);
        Assertions.assertThat(unit.getType()).isEqualTo(DateUnitExpression.DateUnit.DAY);
        Assertions.assertThat(unit).hasToString("day");
    }

    @Test
    void testUnitWordOutsideFunctionArgumentsIsColumn() throws JSQLParserException {
        String sqlStr = "SELECT year FROM t";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Column column = select.getSelectItem(0).getExpression(Column.class);
        Assertions.assertThat(column.getColumnName()).isEqualTo("year");
    }

    @Test
    void testUnitWordAsQualifierIsColumn() throws JSQLParserException {
        String sqlStr = "SELECT month.amount FROM sales month";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Column column = select.getSelectItem(0).getExpression(Column.class);
        Assertions.assertThat(column.getFullyQualifiedName()).isEqualTo("month.amount");
    }

    @Test
    void testUnitWordWithinArgumentExpressionIsColumn() throws JSQLParserException {
        String sqlStr = "SELECT COALESCE(year + 1, 0) FROM t";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Function f = select.getSelectItem(0).getExpression(Function.class);
        Addition addition = (Addition) f.getParameters().get(0);
        Assertions.assertThat(addition.getLeftExpression()).isInstanceOf(Column.class);
    }

    @Test
    void testQuotedUnitWordArgumentIsColumn() throws JSQLParserException {
        String sqlStr = "SELECT DATEADD(\"day\", 1, d) FROM t";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Function f = select.getSelectItem(0).getExpression(Function.class);
        Assertions.assertThat(f.getParameters().get(0)).isInstanceOf(Column.class);
    }

    @Test
    void testUnitWordInUpdateSetTupleIsColumn() throws JSQLParserException {
        String sqlStr = "UPDATE t SET (a, year) = (1, 2)";

        Update update = (Update) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        Assertions.assertThat(update.getUpdateSets().get(0).getColumn(1).getColumnName())
                .isEqualTo("year");
    }
}
