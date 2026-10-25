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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.alter.AlterEventTrigger;
import net.sf.jsqlparser.statement.alter.RelationAlterAction.EnableState;
import net.sf.jsqlparser.statement.create.event.AlterEvent;
import net.sf.jsqlparser.statement.create.event.CreateEvent;
import net.sf.jsqlparser.statement.create.trigger.CreateEventTrigger;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlEventTriggerTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static <T extends Statement> T roundTrip(String sql, Class<T> type)
            throws JSQLParserException {
        T statement =
                assertInstanceOf(type, assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL));
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), POSTGRESQL).toString());
        return statement;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE EVENT TRIGGER etg ON ddl_command_start WHEN TAG IN ('CREATE TABLE') "
                    + "EXECUTE FUNCTION abort_any_command()",
            "CREATE EVENT TRIGGER etg ON sql_drop EXECUTE FUNCTION f()",
            "CREATE EVENT TRIGGER \"On Login\" ON login EXECUTE FUNCTION app.on_login()",
            "CREATE EVENT TRIGGER etg ON table_rewrite EXECUTE PROCEDURE f()"})
    void parsesEventTriggerDefinitions(String sql) throws JSQLParserException {
        roundTrip(sql, CreateEventTrigger.class);
        assertInstanceOf(CreateEventTrigger.class, CCJSqlParserUtil.parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1", POSTGRESQL).size());
    }

    @Test
    void keepsEventFiltersAndFunction() throws JSQLParserException {
        CreateEventTrigger trigger = roundTrip("CREATE EVENT TRIGGER etg ON ddl_command_end "
                + "WHEN TAG IN ('CREATE TABLE', 'DROP TABLE') AND tag IN ('ALTER TABLE') "
                + "EXECUTE PROCEDURE audit.log_ddl()", CreateEventTrigger.class);

        assertEquals("etg", trigger.getName());
        assertEquals("ddl_command_end", trigger.getEvent());
        assertEquals(2, trigger.getFilters().size());
        CreateEventTrigger.Filter first = trigger.getFilters().get(0);
        assertEquals("TAG", first.getVariable());
        assertEquals(List.of("CREATE TABLE", "DROP TABLE"), first.getValues().stream()
                .map(StringValue::getValue).collect(Collectors.toList()));
        assertEquals("tag", trigger.getFilters().get(1).getVariable());
        assertEquals(CreateTrigger.ExecuteKeyword.PROCEDURE, trigger.getExecuteKeyword());
        assertEquals("audit.log_ddl", trigger.getFunction().getName());
        assertTrue(trigger.getFunction().getParameters().isEmpty());
    }

    @Test
    void buildsAnEventTrigger() {
        CreateEventTrigger trigger = new CreateEventTrigger().withName("etg")
                .withEvent("ddl_command_start")
                .withFunction(new Function().withName("guard")
                        .withParameters(new ExpressionList<>()))
                .addFilters(new CreateEventTrigger.Filter("TAG",
                        List.of(new StringValue("DROP TABLE"))));

        assertDeparse(trigger, "CREATE EVENT TRIGGER etg ON ddl_command_start "
                + "WHEN TAG IN ('DROP TABLE') EXECUTE FUNCTION guard()");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DISABLE", "ENABLE", "ENABLE REPLICA", "ENABLE ALWAYS"})
    void alterEventTriggerSharesTheEnableStates(String state) throws JSQLParserException {
        AlterEventTrigger alter =
                roundTrip("ALTER EVENT TRIGGER etg " + state, AlterEventTrigger.class);

        assertEquals("etg", alter.getName());
        assertEquals(AlterEventTrigger.Action.STATE, alter.getAction());
        assertEquals(EnableState.valueOf(state.replace(' ', '_')), alter.getEnableState());
    }

    @Test
    void alterEventTriggerOwnerAndName() throws JSQLParserException {
        AlterEventTrigger owner =
                roundTrip("ALTER EVENT TRIGGER etg OWNER TO CURRENT_USER", AlterEventTrigger.class);
        assertEquals(AlterEventTrigger.Action.OWNER, owner.getAction());
        assertEquals("CURRENT_USER", owner.getOwner());

        AlterEventTrigger rename =
                roundTrip("ALTER EVENT TRIGGER etg RENAME TO etg2", AlterEventTrigger.class);
        assertEquals(AlterEventTrigger.Action.RENAME, rename.getAction());
        assertEquals("etg2", rename.getNewName());
    }

    @Test
    void settersSelectTheAction() {
        AlterEventTrigger alter = new AlterEventTrigger().withName("etg").withOwner("admin");
        alter.setNewName("etg2");
        assertEquals(AlterEventTrigger.Action.RENAME, alter.getAction());
        assertNull(alter.getOwner());
        assertDeparse(alter, "ALTER EVENT TRIGGER etg RENAME TO etg2");

        alter.setEnableState(EnableState.ENABLE_ALWAYS);
        assertNull(alter.getNewName());
        assertDeparse(alter, "ALTER EVENT TRIGGER etg ENABLE ALWAYS");
    }

    @Test
    void mySqlEventsNamedTriggerStayEvents() throws JSQLParserException {
        Consumer<CCJSqlParser> mysql = parser -> parser.withDialect(Dialect.MYSQL);
        assertInstanceOf(CreateEvent.class, CCJSqlParserUtil.parse(
                "CREATE EVENT trigger ON SCHEDULE EVERY 1 DAY DO SELECT 1", mysql));
        assertInstanceOf(AlterEvent.class,
                CCJSqlParserUtil.parse("ALTER EVENT trigger RENAME TO e2", mysql));
        assertInstanceOf(AlterEvent.class,
                CCJSqlParserUtil.parse("ALTER EVENT trigger ENABLE", mysql));
        assertInstanceOf(AlterEvent.class, CCJSqlParserUtil.parse("ALTER EVENT e DISABLE", mysql));
    }

    @Test
    void visitorsValidationAndFeatures() throws JSQLParserException {
        String create = "CREATE EVENT TRIGGER etg ON ddl_command_start EXECUTE FUNCTION f()";
        String alter = "ALTER EVENT TRIGGER etg DISABLE";
        for (String sql : List.of(create, alter)) {
            Statement statement = CCJSqlParserUtil.parse(sql, POSTGRESQL);
            assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
            assertThat(statement.getFeatures().getCertain())
                    .containsExactly(StmtFeature.MODIFIES_SCHEMA);
            assertTrue(new Validation(List.of(PostgresqlVersion.V10), sql).validate().isEmpty());
            assertTrue(new Validation(List.of(FeaturesAllowed.DDL), sql).validate().isEmpty());
            assertThat(new Validation(List.of(MySqlVersion.V8_0), sql).validate()).isNotEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE EVENT TRIGGER etg ON ddl_command_start EXECUTE FUNCTION f(1)",
            "CREATE EVENT TRIGGER etg ON ddl_command_start EXECUTE FUNCTION f",
            "CREATE OR REPLACE EVENT TRIGGER etg ON ddl_command_start EXECUTE FUNCTION f()",
            "CREATE EVENT TRIGGER etg ON ddl_command_start WHEN TAG IN () EXECUTE FUNCTION f()",
            "CREATE EVENT TRIGGER etg ON ddl_command_start WHEN TAG IN (1) EXECUTE FUNCTION f()",
            "CREATE EVENT TRIGGER etg ON ddl_command_start WHEN TAG = 'x' EXECUTE FUNCTION f()",
            "ALTER EVENT TRIGGER etg",
            "ALTER EVENT TRIGGER etg DISABLE ALWAYS",
            "ALTER EVENT TRIGGER etg OWNER admin"})
    void rejectsInvalidEventTriggers(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }
}
