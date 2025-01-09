/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IsUnknownExpressionTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM mytable WHERE 1 IS UNKNOWN",
            "SELECT * FROM mytable WHERE 1 IS NOT UNKNOWN",
    })
    public void testIsUnknownExpression(String sqlStr) {
        assertDoesNotThrow(() -> TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr));
    }

    @Test
    void testStringConstructor() {
        Column column = new Column("x");

        IsUnknownExpression defaultIsUnknownExpression =
                new IsUnknownExpression().withLeftExpression(column);
        TestUtils.assertExpressionCanBeDeparsedAs(defaultIsUnknownExpression, "x IS UNKNOWN");

        IsUnknownExpression isUnknownExpression =
                new IsUnknownExpression().withLeftExpression(column).withNot(false);
        TestUtils.assertExpressionCanBeDeparsedAs(isUnknownExpression, "x IS UNKNOWN");

        IsUnknownExpression isNotUnknownExpression =
                new IsUnknownExpression().withLeftExpression(column).withNot(true);
        TestUtils.assertExpressionCanBeDeparsedAs(isNotUnknownExpression, "x IS NOT UNKNOWN");
    }
}
