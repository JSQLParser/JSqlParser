/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author toben
 */
public class SetStatementTest {

    @Test
    public void testSimpleSet() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET statement_timeout = 0");
    }

    @Test
    public void testIssue373() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET deferred_name_resolution true");
    }

    @Test
    public void testIssue373_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET tester 5");
    }

    @Test
    public void testMultiValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET v = 1, c = 3");
    }

    @Test
    public void testListValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET v = 1, 3");
    }

    @Test
    public void tesTimeZone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET LOCAL Time Zone 'UTC'");
    }

    @Test
    public void tesLocalWithEq() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET LOCAL cursor_tuple_fraction = 0.05");
    }

    @Test
    public void testValueOnIssue927() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET standard_conforming_strings = on");
    }

    @Test
    public void testObject() {
        SetStatement setStatement = new SetStatement();
        setStatement.add("standard_conforming_strings", new ExpressionList<>(new StringValue("ON")),
                false);
        setStatement.withUseEqual(0, true).remove(0);

        assertEquals(0, setStatement.getCount());

        setStatement.addKeyValuePairs(
                new SetStatement.NameExpr("test", new ExpressionList<>(new StringValue("1")),
                        false));
        setStatement.getKeyValuePairs().get(0).setUseEqual(true);

        assertEquals("test", setStatement.getKeyValuePairs().get(0).getName());
        assertTrue(setStatement.getKeyValuePairs().get(0).isUseEqual());

        setStatement.clear();
        assertEquals(0, setStatement.getCount());
    }

    @Test
    public void testSettingUserVariable() throws JSQLParserException {
        String sqlStr = "set @Flag = 1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        // issue #1237
        sqlStr = "SET @@global.time_zone = '01:00'";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testMultiPartVariables() throws JSQLParserException {
        String sqlStr = "set a.b.c=false";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testTypedScopeKeepsTheEffectParameterAccessors() throws JSQLParserException {
        SetStatement set = (SetStatement) CCJSqlParserUtil.parse("set local statement_timeout = 0");
        assertEquals(SetScope.LOCAL, set.getScope());
        assertEquals("LOCAL", set.getEffectParameter());
        // the scope keyword is rendered in upper case
        assertEquals("SET LOCAL statement_timeout = 0", set.toString());
        set.setEffectParameter("persist_only");
        assertEquals(SetScope.PERSIST_ONLY, set.getScope());
        assertEquals("SET PERSIST_ONLY statement_timeout = 0", set.toString());
        set.withScope(null);
        assertEquals(SetScope.NONE, set.getScope());
        assertNull(set.getEffectParameter());
        assertEquals("SET statement_timeout = 0", set.toString());
        assertThrows(IllegalArgumentException.class, () -> set.setEffectParameter("forever"));
        set.withEffectParameter("Session").clear();
        assertEquals(SetScope.NONE, set.getScope());
        assertEquals(SetScope.SESSION,
                ((SetStatement) CCJSqlParserUtil.parse("SET SESSION x = 1")).getScope());
    }

    @Test
    public void testNameExprIsPublic() throws JSQLParserException {
        assertTrue(Modifier.isPublic(SetStatement.NameExpr.class.getModifiers()));
        SetStatement set = (SetStatement) CCJSqlParserUtil.parse("SET a = 1, b := 2");
        SetStatement.NameExpr second = set.getKeyValuePairs().get(1);
        assertEquals(SetStatement.AssignmentOperator.COLON_EQUALS, second.getAssignmentOperator());
        second.setAssignmentOperator(SetStatement.AssignmentOperator.EQUALS);
        assertEquals("SET a = 1, b = 2", set.toString());
        assertThrows(NullPointerException.class, () -> second.setAssignmentOperator(null));
    }
}
