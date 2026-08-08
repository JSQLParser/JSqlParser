/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlTableTest {

    @ParameterizedTest
    @ValueSource(strings = {
            // Issue #2326: full PostgreSQL XMLTABLE example
            "SELECT x.* FROM xmldata, XMLTABLE('//ROWS/ROW' PASSING data COLUMNS\n"
                    + "id int PATH '@id',\n"
                    + "ordinality FOR ORDINALITY,\n"
                    + "\"COUNTRY_NAME\" text,\n"
                    + "country_id text PATH 'COUNTRY_ID',\n"
                    + "size_sq_km float PATH 'SIZE[@unit = \"sq_km\"]') x",
            // Minimal forms
            "SELECT * FROM XMLTABLE('//ROWS/ROW') x",
            "SELECT * FROM XMLTABLE('//ROWS/ROW' COLUMNS id int) x",
            "SELECT * FROM XMLTABLE('//ROWS/ROW' PASSING data) x",
            "SELECT * FROM XMLTABLE('//ROWS/ROW' PASSING data COLUMNS id int) x",
            // Passing alias, PATH and DEFAULT
            "SELECT * FROM XMLTABLE('/a' PASSING data AS d COLUMNS c1 int PATH 'b' DEFAULT 0) x",
            // DEFAULT without PATH
            "SELECT * FROM XMLTABLE('/a' COLUMNS c1 int DEFAULT 5) x",
            // Multiple passing arguments
            "SELECT * FROM XMLTABLE('/a' PASSING data AS d, doc AS x COLUMNS c1 int) x"
    })
    void testXmlTableRoundTrip(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testXmlTableStructure() throws JSQLParserException {
        XmlTableFunction table = parseXmlTable(
                "XMLTABLE('//ROWS/ROW' PASSING data AS d COLUMNS\n"
                        + "id int PATH '@id', ordinality FOR ORDINALITY, country text PATH 'COUNTRY_ID')");

        assertThat(table.getRowPathExpression().toString()).isEqualTo("'//ROWS/ROW'");
        assertThat(table.getPassingClauses()).hasSize(1);
        assertThat(table.getPassingClauses().get(0).getName()).isEqualTo("d");

        assertThat(table.getColumnDefinitions()).hasSize(3);

        XmlTableFunction.XmlTableColumnDefinition ordinality = table.getColumnDefinitions().get(1);
        assertThat(ordinality.getColumnName()).isEqualTo("ordinality");
        assertThat(ordinality.isForOrdinality()).isTrue();
        assertThat(ordinality.getDataType()).isNull();

        XmlTableFunction.XmlTableColumnDefinition valueColumn = table.getColumnDefinitions().get(2);
        assertThat(valueColumn.getColumnName()).isEqualTo("country");
        assertThat(valueColumn.isForOrdinality()).isFalse();
        assertThat(valueColumn.getDataType()).isInstanceOf(ColDataType.class);
        assertThat(valueColumn.getDataType().getDataType()).isEqualTo("text");
        assertThat(valueColumn.getPathExpression().toString()).isEqualTo("'COUNTRY_ID'");
    }

    @Test
    void testXmlTableDefaultExpression() throws JSQLParserException {
        XmlTableFunction table = parseXmlTable(
                "XMLTABLE('/a' COLUMNS c1 int PATH 'b' DEFAULT 0)");

        XmlTableFunction.XmlTableColumnDefinition column = table.getColumnDefinitions().get(0);
        assertThat(column.getPathExpression().toString()).isEqualTo("'b'");
        assertThat(column.getDefaultExpression().toString()).isEqualTo("0");
    }

    private XmlTableFunction parseXmlTable(String xmlTableStr) throws JSQLParserException {
        String sql = "SELECT * FROM " + xmlTableStr;
        Statement stmt = CCJSqlParserUtil.parse(sql);

        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        FromItem fromItem = ((PlainSelect) stmt).getFromItem();
        assertThat(fromItem).isInstanceOf(TableFunction.class);
        Function function = ((TableFunction) fromItem).getFunction();
        assertThat(function).isInstanceOf(XmlTableFunction.class);

        return (XmlTableFunction) function;
    }
}
