/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterPolicy;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;
import net.sf.jsqlparser.statement.create.policy.PolicyOptions;
import net.sf.jsqlparser.statement.drop.DropPolicy;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlPolicyDdlTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE POLICY pol ON t",
            "CREATE POLICY pol ON t AS RESTRICTIVE FOR SELECT TO PUBLIC USING(id > 0)",
            "CREATE POLICY pol ON t FOR INSERT TO CURRENT_USER WITH CHECK(id>0)",
            "CREATE POLICY pol ON t FOR UPDATE TO CURRENT_ROLE, SESSION_USER USING(id>0) WITH CHECK(id<100)",
            "ALTER POLICY pol ON t RENAME TO pol2", "ALTER POLICY pol ON t TO PUBLIC",
            "ALTER POLICY pol ON t TO CURRENT_USER, SESSION_USER",
            "ALTER POLICY pol ON t USING(id>1)",
            "ALTER POLICY pol ON t WITH CHECK(id<10)",
            "ALTER POLICY pol ON t TO CURRENT_ROLE USING(id>0) WITH CHECK(id<100)",
            "DROP POLICY pol ON t", "DROP POLICY IF EXISTS pol ON t",
            "DROP POLICY pol ON t CASCADE",
            "DROP POLICY IF EXISTS pol ON t RESTRICT"})
    void statementsAndFollowingBoundariesRoundTrip(String sql) throws JSQLParserException {
        assertRoundTrip(parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void createAndAlterShareRolesAndPredicates() throws JSQLParserException {
        CreatePolicy create = (CreatePolicy) parse("CREATE POLICY pol ON t TO PUBLIC USING(id>0)");
        AlterPolicy alter = (AlterPolicy) parse("ALTER POLICY pol ON t TO PUBLIC USING(id>0)");
        assertEquals(create.getRoles(), alter.getOptions().getRoles());
        assertEquals(create.getUsingExpression().toString(),
                alter.getOptions().getUsingExpression().toString());
        alter.getOptions().setUsingExpression(CCJSqlParserUtil.parseCondExpression("id > 10"));
        alter.setPolicyName("new_policy");
        alter.setTable(new Table("new_table"));
        assertEquals("ALTER POLICY new_policy ON new_table TO PUBLIC USING (id > 10)",
                alter.toString());
        assertRoundTrip(alter);
        alter.setNewName("renamed");
        assertEquals("ALTER POLICY new_policy ON new_table RENAME TO renamed", alter.toString());
        alter.setOptions(new PolicyOptions()
                .setUsingExpression(CCJSqlParserUtil.parseCondExpression("id > 1")));
        assertNull(alter.getNewName());
        assertRoundTrip(alter);
    }

    @Test
    void predicatesAndTargetsParticipateInTraversal() throws JSQLParserException {
        String sql = "ALTER POLICY pol ON target USING (EXISTS (SELECT 1 FROM permissions))"
                + " WITH CHECK (EXISTS (SELECT 1 FROM allowed_values))";
        assertEquals(Set.of("target", "permissions", "allowed_values"),
                TablesNamesFinder.findTables(sql));
        assertEquals(Set.of("target"), TablesNamesFinder.findTables("DROP POLICY pol ON target"));
        assertRoundTrip(parse(sql));
    }

    @Test
    void dropPolicyNamesAndBehaviorAreMutable() throws JSQLParserException {
        DropPolicy drop = (DropPolicy) parse("DROP POLICY IF EXISTS pol ON t CASCADE");
        assertTrue(drop.isIfExists());
        drop.setPolicyName("new_pol").setTable(new Table("new_t"))
                .setBehavior(DropPolicy.Behavior.RESTRICT);
        assertEquals("DROP POLICY IF EXISTS new_pol ON new_t RESTRICT", drop.toString());
        assertRoundTrip(drop);
    }

    @Test
    void createOnlyOptionsAndMalformedDropAreRejected() {
        for (String sql : new String[] {"ALTER POLICY pol ON t AS RESTRICTIVE",
                "ALTER POLICY pol ON t FOR SELECT",
                "ALTER POLICY pol ON t RENAME TO pol2 USING(id>0)", "DROP POLICY pol",
                "DROP POLICY pol ON t, u"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql));
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        assertEquals(statement.toString(), builder.toString());
        assertEquals(statement.toString(), parse(builder.toString()).toString());
    }
}
