/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.Test;

import java.io.StringReader;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertEquals;

public class GrantTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testGrantPrivilege() throws JSQLParserException {
        String statement = "GRANT SELECT ON t1 TO u";
        Grant grant = (Grant) parserManager.parse(new StringReader(statement));
        assertEquals("t1", grant.getObjectName());

        assertEquals(1, grant.getPrivileges().size());
        assertEquals("SELECT", grant.getPrivileges().get(0));

        assertEquals(1, grant.getUsers().size());
        assertEquals("u", grant.getUsers().get(0));

        assertEquals(statement, grant.toString());
        assertEquals(null, grant.getRole());
    }

    @Test
    public void testGrantPrivileges() throws JSQLParserException {
        String statement = "GRANT SELECT, INSERT ON t1 TO u, u2";
        Grant grant = (Grant) parserManager.parse(new StringReader(statement));
        assertEquals("t1", grant.getObjectName());
        assertEquals(2, grant.getPrivileges().size());
        assertEquals(true, grant.getPrivileges().stream().anyMatch(s -> s.equals("SELECT")));
        assertEquals(true, grant.getPrivileges().stream().anyMatch(s -> s.equals("INSERT")));

        assertEquals(2, grant.getUsers().size());
        assertEquals(true, grant.getUsers().stream().anyMatch(s -> s.equals("u")));
        assertEquals(true, grant.getUsers().stream().anyMatch(s -> s.equals("u2")));

        assertEquals(statement, grant.toString());
        assertEquals(null, grant.getRole());
    }

    @Test
    public void testDropRestrictIssue510() throws JSQLParserException {
        String statement = "GRANT role1 TO u, u2";
        Grant grant = (Grant) parserManager.parse(new StringReader(statement));

        assertEquals(null, grant.getObjectName());
        assertEquals(null, grant.getPrivileges());

        assertEquals(2, grant.getUsers().size());
        assertEquals(true, grant.getUsers().stream().anyMatch(s -> s.equals("u")));
        assertEquals(true, grant.getUsers().stream().anyMatch(s -> s.equals("u2")));

        assertEquals("role1", grant.getRole());
        assertEquals(statement, grant.toString());
    }

    @Test
    public void testDropViewIssue545() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("GRANT SELECT, INSERT, UPDATE, DELETE ON T1 TO ADMIN_ROLE");
    }

    @Test
    public void testDropSchemaIssue8556() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("GRANT ROLE_1 TO TEST_ROLE_1, TEST_ROLE_2");
    }
}
