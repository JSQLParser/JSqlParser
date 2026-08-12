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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class IntervalExpressionTest {

    @Test
    void testExtractExpressionIssue2172() throws JSQLParserException {
        String sqlStr = "select INTERVAL Extract( DAY from Now()) - 1 DAY";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT UNIX_TIMESTAMP(date_sub(date_sub(date_format(now(),'%y-%m-%d'),interval extract(day from now())-1 day),interval 1 month))*1000";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // ---- Interval qualifier support (issue #1728 / SQL standard) ----

    @Test
    void testIntervalSingleFieldRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '1' HOUR", true);
    }

    @Test
    void testIntervalFieldToFieldRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '1' HOUR TO MINUTE", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '4 5:12' DAY TO MINUTE", true);
    }

    @Test
    void testIntervalFieldWithPrecisionRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '400' DAY(3)", true);
    }

    @Test
    void testIntervalFieldToFieldWithPrecisionRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT INTERVAL '4 5:12:10.222' DAY TO SECOND(3)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '400 5' DAY(3) TO HOUR", true);
    }

    @Test
    void testIntervalSecondWithLeadingAndFractionalPrecisionRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT INTERVAL '30.12345' SECOND(2, 4)", true);
    }

    @Test
    void testStandardSingleFieldUsesQualifier() throws JSQLParserException {
        String sql = "SELECT INTERVAL '1' DAY";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        IntervalExpression interval = (IntervalExpression) select.getPlainSelect()
                .getSelectItems().get(0).getExpression();

        // The standard field is modeled by the canonical qualifier, not the legacy type.
        IntervalQualifier qualifier = interval.getIntervalQualifier();
        assertNotNull(qualifier);
        assertEquals("DAY", qualifier.getLeadingField());
        assertNull(interval.getIntervalType());
        assertEquals("INTERVAL '1' DAY", interval.toString());
        TestUtils.assertExpressionCanBeDeparsedAs(interval, "INTERVAL '1' DAY");
    }

    @Test
    void testIntervalQualifierStructurallyAttached() throws JSQLParserException {
        String sql = "SELECT INTERVAL '1' HOUR TO MINUTE";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        IntervalExpression interval = (IntervalExpression) select.getPlainSelect()
                .getSelectItems().get(0).getExpression();
        IntervalQualifier qualifier = interval.getIntervalQualifier();
        assertNotNull(qualifier, "qualifier must be a structured property of the interval");
        assertEquals("HOUR", qualifier.getLeadingField());
        assertEquals("MINUTE", qualifier.getTrailingField());
        assertEquals("HOUR TO MINUTE", qualifier.toString());
        // The canonical qualifier replaces the legacy type, which must stay null.
        assertNull(interval.getIntervalType());
        TestUtils.assertExpressionCanBeDeparsedAs(interval, "INTERVAL '1' HOUR TO MINUTE");
    }

    @Test
    @SuppressWarnings("deprecation")
    void testLegacySetterOverridesStructuredQualifier() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("SELECT INTERVAL '1' DAY");
        IntervalExpression interval = (IntervalExpression) select.getPlainSelect()
                .getSelectItems().get(0).getExpression();

        interval.setIntervalType("HOUR");

        assertNull(interval.getIntervalQualifier());
        assertEquals("HOUR", interval.getIntervalType());
        assertEquals("INTERVAL '1' HOUR", interval.toString());
        TestUtils.assertExpressionCanBeDeparsedAs(interval, "INTERVAL '1' HOUR");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT " + interval, true);
    }

    @Test
    @SuppressWarnings("deprecation")
    void testStructuredSetterOverridesLegacyType() throws JSQLParserException {
        IntervalExpression interval = new IntervalExpression()
                .withExpression(new StringValue("1"))
                .withIntervalType("DAY");

        interval.withIntervalQualifier(new IntervalQualifier("HOUR", null, "MINUTE", null));

        assertEquals("HOUR TO MINUTE", interval.getIntervalQualifier().toString());
        assertNull(interval.getIntervalType());
        assertEquals("INTERVAL '1' HOUR TO MINUTE", interval.toString());
        TestUtils.assertExpressionCanBeDeparsedAs(interval, "INTERVAL '1' HOUR TO MINUTE");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT " + interval, true);
    }

    @Test
    void testPostfixIntervalQualifierRoundTrip() throws JSQLParserException {
        // Oracle-style postfix qualifier: (expr) DAY(9) TO SECOND
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT (systimestamp - order_date) DAY(9) TO SECOND FROM orders", true);
    }

    @Test
    @SuppressWarnings("deprecation")
    void testNonStandardIntervalTypeUsesLegacyGetter() throws JSQLParserException {
        // MySQL-style non-standard single-identifier field: INTERVAL 1 foo. The deprecated legacy
        // type is kept exactly for this case; no structured qualifier is attached.
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT INTERVAL 1 foo", true);
        IntervalExpression interval = (IntervalExpression) select.getPlainSelect()
                .getSelectItems().get(0).getExpression();

        assertNull(interval.getIntervalQualifier());
        assertEquals("foo", interval.getIntervalType());
    }

    @Test
    void testContradictoryFractionalPrecisionRejected() {
        // SECOND(2, 4) TO SECOND(3) specifies fractional seconds precision twice. Reject instead of
        // silently dropping the leading 4 (issue raised in review of PR #2456).
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT INTERVAL '1' SECOND(2, 4) TO SECOND(3)"));
    }
}
