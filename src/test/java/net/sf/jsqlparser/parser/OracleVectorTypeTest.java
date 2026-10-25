/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OracleVectorTypeTest {
    @ParameterizedTest
    @ValueSource(strings = {"*, *", "*, FLOAT32", "3, *", "*, *, DENSE", "*, *, SPARSE",
            "*, FLOAT32, SPARSE", "3, *, SPARSE", "3, FLOAT32, *", "*, *, *", "3, FLOAT32",
            "3", "1024, FLOAT32, SPARSE"})
    void preservesVectorArguments(String arguments) throws JSQLParserException {
        List<String> expected = Arrays.asList(arguments.split(", "));
        for (String name : List.of("VECTOR", "vector")) {
            String typeSql = name + "(" + arguments + ")";
            for (ColDataType type : List.of(CCJSqlParserUtil.parseColDataType(typeSql),
                    CCJSqlParserUtil.parseColDataType(typeSql,
                            p -> p.withDialect(Dialect.ORACLE)))) {
                assertEquals(name, type.getDataType());
                assertEquals(expected, type.getArgumentsStringList());
                assertEquals(name + " (" + arguments + ")", type.toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"VECTOR(*, *)", "VECTOR(*, FLOAT32)", "VECTOR(3, *)",
            "VECTOR(*, *, SPARSE)", "VECTOR(3, FLOAT32, *)"})
    void roundTripsVectorColumnsBeforeOtherColumns(String typeSql) throws JSQLParserException {
        String sql = "CREATE TABLE items (embedding " + typeSql + ", label VARCHAR2(20))";
        for (Statement statement : List.of(TestUtils.assertSqlCanBeParsedAndDeparsed(sql),
                CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.ORACLE)))) {
            CreateTable table = (CreateTable) statement;
            assertEquals(2, table.getColumnDefinitions().size());
            assertEquals("label", table.getColumnDefinitions().get(1).getColumnName());
            StringBuilder buffer = new StringBuilder();
            statement.accept(new StatementDeParser(buffer), null);
            assertEquals(statement.toString(), buffer.toString());
            CreateTable reparsed = (CreateTable) CCJSqlParserUtil.parse(buffer.toString());
            assertEquals(
                    table.getColumnDefinitions().get(0).getColDataType().getArgumentsStringList(),
                    reparsed.getColumnDefinitions().get(0).getColDataType()
                            .getArgumentsStringList());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"VARCHAR(*)", "NUMERIC(3, *)", "custom_type(*)",
            "\"VECTOR\"(*, *)", "VECTOR(*,", "VECTOR(, *)", "VECTOR(*, *) NOT NULL"})
    void rejectsWildcardsOutsideVectorAndIncompleteTypes(String typeSql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseColDataType(typeSql));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseColDataType(typeSql,
                        p -> p.withDialect(Dialect.ORACLE)));
    }
}
