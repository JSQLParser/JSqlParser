/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlRuleStateTest {
    @ParameterizedTest
    @ValueSource(strings = {"ENABLE", "DISABLE", "ENABLE ALWAYS", "ENABLE REPLICA"})
    void ruleAndTriggerActionsShareStates(String state) throws JSQLParserException {
        Alter rule = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t " + state
                + " RULE \"my rule\", ADD COLUMN z INT");
        RelationAlterAction action = assertInstanceOf(RelationAlterAction.class,
                rule.getAlterExpressions().get(0));
        assertEquals(RelationAlterAction.Kind.RULE_STATE, action.getKind());
        assertEquals(state.replace(' ', '_'), action.getEnableState().name());
        assertEquals("\"my rule\"", action.getValue());
        assertEquals(2, rule.getAlterExpressions().size());
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(rule));
        action.setEnableState(RelationAlterAction.EnableState.DISABLE);
        action.setValue("new_rule");
        StringBuilder output = new StringBuilder();
        rule.accept(new StatementDeParser(output));
        assertEquals(rule.toString(), output.toString());
        assertTrue(output.toString().contains("DISABLE RULE new_rule"));
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
        Alter trigger = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t " + state + " TRIGGER trg",
                p -> p.withDialect(Dialect.POSTGRESQL));
        RelationAlterAction triggerAction =
                (RelationAlterAction) trigger.getAlterExpressions().get(0);
        assertEquals(state.replace(' ', '_'), triggerAction.getTriggerState().name());
        triggerAction.setTriggerState(RelationAlterAction.TriggerState.ENABLE_ALWAYS);
        assertEquals(RelationAlterAction.EnableState.ENABLE_ALWAYS, triggerAction.getEnableState());
        assertEquals(2, CCJSqlParserUtil.parseStatements(rule + "; SELECT 1").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ENABLE RULE", "ALTER TABLE t DISABLE ALWAYS RULE r",
            "ALTER TABLE t DISABLE REPLICA RULE r", "ALTER INDEX ix ENABLE RULE r",
            "ALTER TABLE t ENABLE ALWAYS TRIGGER ALL", "ALTER TABLE t ENABLE REPLICA TRIGGER USER"})
    void rejectsWrongStateTargets(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL)));
    }
}
