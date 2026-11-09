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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WeightStringFunctionTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    private static Function selectFunction(String sql, int index,
            Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true, consumer);
        return (Function) select.getSelectItem(index).getExpression();
    }

    @Test
    void castToCharWithMySqlDialect() throws JSQLParserException {
        String sql = "SELECT CHAR(77 USING utf8mb4), WEIGHT_STRING('x' AS CHAR(3))";
        Function function = selectFunction(sql, 1, MYSQL);

        assertEquals("WEIGHT_STRING", function.getName());
        assertEquals("'x'", function.getParameters().get(0).toString());
        // the same ColDataType as in CAST('x' AS CHAR(3))
        assertEquals("CHAR (3)", function.getCastType().getDataType());
        CastExpression cast =
                (CastExpression) CCJSqlParserUtil.parseExpression("CAST('x' AS CHAR(3))");
        assertEquals(cast.getColDataType().toString(), function.getCastType().toString());
        assertEquals("WEIGHT_STRING('x' AS CHAR (3))", function.toString());
        assertEquals(3, function.getCastType().getNumericPrecision().intValue());

        // the type spans its own text, inside the call
        Node node = function.getCastType().getASTNode();
        assertEquals("CHAR(3)", sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    @Test
    void castToBinaryWithoutDialect() throws JSQLParserException {
        Function function = selectFunction("SELECT weight_string(c AS BINARY(4)) FROM t", 0, null);

        assertEquals("BINARY (4)", function.getCastType().getDataType());
        assertInstanceOf(Column.class, function.getParameters().get(0));
    }

    @Test
    void plainCallHasNoCastType() throws JSQLParserException {
        Function function = selectFunction("SELECT WEIGHT_STRING('x')", 0, MYSQL);

        assertNull(function.getCastType());
        assertEquals("WEIGHT_STRING('x')", function.toString());
    }

    @Test
    void charUsingKeepsItsKeywordArgument() throws JSQLParserException {
        Function function = selectFunction("SELECT CHAR(77 USING utf8mb4)", 0, MYSQL);

        assertNull(function.getCastType());
        assertEquals("USING", function.getKeywordArguments().get(0).getKeyword());
    }

    @Test
    void nestedInExpressionsAndSubqueries() throws JSQLParserException {
        String sql = "SELECT HEX(WEIGHT_STRING(a AS CHAR(2))) FROM t"
                + " WHERE WEIGHT_STRING(b AS BINARY(8)) = (SELECT WEIGHT_STRING(c) FROM s)";
        assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL);

        assertEquals(2, TablesNamesFinder.findTables(sql).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT WEIGHT_STRING('x' AS CHAR)",
            "SELECT WEIGHT_STRING('x' AS VARCHAR(3))",
            "SELECT WEIGHT_STRING('x' AS CHAR(3)",
            "SELECT LOWER('x' AS CHAR(3))",
            "SELECT WEIGHT_STRING('x' AS CHAR(3) AS BINARY(3))",
            "SELECT WEIGHT_STRING('x' AS CHAR(3) CHARACTER SET utf8mb4)",
            "SELECT WEIGHT_STRING('x' AS CHAR(n))",
            "SELECT WEIGHT_STRING('x' AS BINARY(3, 2))",
            "SELECT WEIGHT_STRING('x' AS CHAR(3) ARRAY)",
            "SELECT WEIGHT_STRING('x', 'y' AS CHAR(3))",
            "SELECT WEIGHT_STRING(DISTINCT 'x' AS CHAR(3))",
            "SELECT WEIGHT_STRING(AS CHAR(3))",
            "SELECT db.WEIGHT_STRING('x' AS CHAR(3))"
    })
    void rejectsOtherCastForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @Test
    void statementBoundary() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT WEIGHT_STRING('x' AS CHAR(3)); SELECT 1", MYSQL);

        assertEquals(2, statements.size());
    }

    @Test
    void buildCastTypeOnTheModel() throws JSQLParserException {
        ColDataType binary = new ColDataType("BINARY");
        binary.setArgumentsStringList(Collections.singletonList("4"));
        Function function = new Function().withName("WEIGHT_STRING")
                .withParameters(new StringValue("x")).withCastType(binary);

        assertEquals("WEIGHT_STRING('x' AS BINARY (4))", function.toString());
        assertDeparse(new PlainSelect().addSelectItem(function),
                "SELECT WEIGHT_STRING('x' AS BINARY (4))");

        function.setCastType(null);
        assertEquals("WEIGHT_STRING('x')", function.toString());
    }
}
