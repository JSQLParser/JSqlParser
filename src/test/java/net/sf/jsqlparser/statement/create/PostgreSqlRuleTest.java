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
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.rule.CreateRule;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.notify.NotifyStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlRuleTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE RULE r AS ON INSERT TO t DO ALSO NOTIFY ddl_probe",
            "CREATE OR REPLACE RULE r AS ON UPDATE TO app.t WHERE NEW.id > 0 DO INSTEAD NOTHING",
            "CREATE RULE r AS ON DELETE TO t DO NOTHING",
            "CREATE RULE r AS ON INSERT TO t DO INSERT INTO log SELECT NEW.id",
            "CREATE RULE r AS ON UPDATE TO t DO UPDATE log SET id=NEW.id WHERE id=OLD.id",
            "CREATE RULE r AS ON DELETE TO t DO DELETE FROM log WHERE id=OLD.id",
            "CREATE RULE \"_RETURN\" AS ON SELECT TO v DO INSTEAD SELECT * FROM t",
            "CREATE RULE r AS ON INSERT TO t DO ALSO (NOTIFY ch, 'updated'; INSERT INTO log VALUES (1);)",
            "CREATE RULE r AS ON INSERT TO t DO (;;;NOTIFY ch;;)",
            "CREATE RULE r AS ON INSERT TO t DO ()",
            "CREATE RULE r AS ON INSERT TO t DO (;)",
            "CREATE RULE r AS ON INSERT TO t DO (SELECT 1 UNION ALL SELECT 2)",
            "CREATE RULE r AS ON INSERT TO t DO (SELECT 1) UNION ALL SELECT 2",
            "CREATE RULE r AS ON INSERT TO t DO TABLE log",
            "CREATE RULE r AS ON INSERT TO t DO ((SELECT 1) UNION ALL SELECT 2; NOTIFY ch)",
            "CREATE RULE r AS ON INSERT TO t DO WITH x AS (SELECT 1) INSERT INTO log SELECT * FROM x",
            "CREATE RULE r AS ON INSERT TO t DO NOTIFY \"Channel\", 'semi;colon'",
            "NOTIFY ch", "NOTIFY \"Channel\", 'it''s ready'"})
    void ruleActionsAndStatementBoundaries(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertTrue(statement instanceof CreateRule || statement instanceof NotifyStatement);
        roundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        assertTrue(new Validation(List.of(PostgresqlVersion.V14), sql)
                .validate().isEmpty());
    }

    @Test
    void actionAndPredicateMutationsUseTheExistingAst() throws JSQLParserException {
        CreateRule rule = (CreateRule) CCJSqlParserUtil.parse(
                "CREATE RULE r AS ON INSERT TO app.t WHERE NEW.id > 0 DO (INSERT INTO log SELECT NEW.id; NOTIFY ch, 'original')");
        assertEquals(Set.of("app.t", "log"), new TablesNamesFinder().getTables(rule));
        rule.setWhereExpression(CCJSqlParserUtil.parseCondExpression("NEW.id > 10"));
        Insert insert = (Insert) rule.getActions().get(0);
        insert.getTable().setName("audit_log");
        NotifyStatement notify = (NotifyStatement) rule.getActions().get(1);
        notify.setPayload(new StringValue("changed"));
        assertTrue(rule.toString().contains("NEW.id > 10"));
        assertTrue(rule.toString().contains("INSERT INTO audit_log"));
        assertTrue(rule.toString().contains("'changed'"));
        assertEquals(Set.of("app.t", "audit_log"), new TablesNamesFinder().getTables(rule));
        roundTrip(rule);
        rule.setNothing(true);
        assertTrue(rule.getActions().isEmpty());
        assertTrue(rule.toString().endsWith("DO NOTHING"));
        roundTrip(rule);
    }

    @Test
    void nestedActionsUseCustomExpressionDeparser() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(
                "CREATE RULE r AS ON INSERT TO t DO (INSERT INTO log VALUES ('before'); NOTIFY ch, 'before')");
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                return getBuilder().append("'after'");
            }
        };
        SelectDeParser selects = new SelectDeParser(expressions, output);
        expressions.setBuilder(output);
        expressions.setSelectVisitor(selects);
        statement.accept(new StatementDeParser(expressions, selects, output));
        assertFalse(output.toString().contains("'before'"));
        assertTrue(output.toString().contains("VALUES ('after')"));
        assertTrue(output.toString().contains("NOTIFY ch, 'after'"));
        assertTrue(new TablesNamesFinder().getTables(CCJSqlParserUtil.parse("NOTIFY table_name"))
                .isEmpty());
    }

    @Test
    void notifyRemainsUsableAsAnIdentifier() throws JSQLParserException {
        roundTrip(CCJSqlParserUtil.parse("SELECT notify FROM notify"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE RULE r AS ON TRUNCATE TO t DO NOTHING",
            "CREATE RULE r AS ON INSERT TO t DO", "CREATE RULE r AS ON INSERT TO t DO (NOTIFY ch",
            "CREATE RULE r AS ON INSERT TO t DO CREATE TABLE x(id int)",
            "CREATE RULE r AS ON INSERT TO t DO (NOTIFY ch NOTIFY ch2)",
            "CREATE RULE r AS ON INSERT TO t DO (NOTIFY ch; DROP TABLE t)",
            "CREATE RULE r AS ON INSERT TO t DO ALSO INSTEAD NOTHING",
            "NOTIFY", "NOTIFY ch, 123", "NOTIFY ch, 'a' || 'b'", "NOTIFY app.ch"})
    void rejectsInvalidActionsAndPayloads(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
