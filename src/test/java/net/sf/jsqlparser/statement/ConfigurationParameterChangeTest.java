/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.assertj.core.api.Assertions.assertThat;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.ConfigurationParameterChange.Action;
import net.sf.jsqlparser.statement.ConfigurationParameterChange.ValueKind;
import net.sf.jsqlparser.statement.alter.database.AlterDatabase;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigurationParameterChangeTest {

    @ParameterizedTest
    @ValueSource(strings = {"SET search_path TO app, public", "SET work_mem = '64MB'",
            "SET work_mem TO DEFAULT", "SET work_mem = DEFAULT", "SET work_mem FROM CURRENT",
            "SET myext.setting = 1", "RESET work_mem", "RESET ALL"})
    void testAlterRoleAndDatabaseShareTheClause(String clause) throws JSQLParserException {
        AlterRole role = (AlterRole) CCJSqlParserUtil.parse("ALTER ROLE app " + clause);
        AlterDatabase database =
                (AlterDatabase) CCJSqlParserUtil.parse("ALTER DATABASE app " + clause);

        assertThat(role.getParameterChange().toString()).isEqualTo(clause);
        assertThat(database.getParameterChange().toString()).isEqualTo(clause);
        assertThat(database.getAction())
                .isEqualTo(AlterDatabase.Action.CONFIGURATION_PARAMETER);
        assertThat(role.getAction().name())
                .isEqualTo(role.getParameterChange().getAction().name());
        assertThat(deparse(role)).isEqualTo("ALTER ROLE app " + clause);
        assertThat(deparse(database)).isEqualTo("ALTER DATABASE app " + clause);
    }

    @Test
    void testParsedState() throws JSQLParserException {
        ConfigurationParameterChange set = parseChange("SET search_path TO app, public");
        assertThat(set.getAction()).isEqualTo(Action.SET);
        assertThat(set.getParameter()).isEqualTo("search_path");
        assertThat(set.getValueKind()).isEqualTo(ValueKind.VALUES);
        assertThat(set.isUseEquals()).isFalse();
        assertThat(set.getValues()).extracting(Object::toString).containsExactly("app", "public");

        ConfigurationParameterChange setDefault = parseChange("SET work_mem = DEFAULT");
        assertThat(setDefault.getValueKind()).isEqualTo(ValueKind.DEFAULT);
        assertThat(setDefault.isUseEquals()).isTrue();
        assertThat(setDefault.getValues()).isNull();

        assertThat(parseChange("SET work_mem FROM CURRENT").getValueKind())
                .isEqualTo(ValueKind.FROM_CURRENT);

        ConfigurationParameterChange resetAll = parseChange("RESET ALL");
        assertThat(resetAll.getAction()).isEqualTo(Action.RESET);
        assertThat(resetAll.getParameter()).isEqualTo("ALL");
    }

    @Test
    void testBuildAndEdit() {
        ConfigurationParameterChange change =
                new ConfigurationParameterChange(Action.SET, "statement_timeout")
                        .withUseEquals(true)
                        .withValues(new ExpressionList<Expression>(new StringValue("5s")));
        assertThat(change.toString()).isEqualTo("SET statement_timeout = '5s'");

        change.setValueKind(ValueKind.DEFAULT);
        assertThat(change.toString()).isEqualTo("SET statement_timeout = DEFAULT");
        change.setValueKind(ValueKind.VALUES);
        assertThat(change.toString()).isEqualTo("SET statement_timeout = '5s'");
        change.setValueKind(ValueKind.FROM_CURRENT);
        assertThat(change.toString()).isEqualTo("SET statement_timeout FROM CURRENT");

        change.setAction(Action.RESET);
        assertThat(change.toString()).isEqualTo("RESET statement_timeout");

        AlterDatabase database = new AlterDatabase();
        database.setDatabaseName("app");
        database.setAction(AlterDatabase.Action.CONFIGURATION_PARAMETER);
        database.setParameterChange(change);
        assertThat(database.toString()).isEqualTo("ALTER DATABASE app RESET statement_timeout");
    }

    @Test
    void testAlterRoleLegacyAccessorsReadTheChange() throws JSQLParserException {
        AlterRole role = (AlterRole) CCJSqlParserUtil
                .parse("ALTER ROLE ALL IN DATABASE appdb SET search_path = app, public");
        assertThat(role.getAction()).isEqualTo(AlterRole.Action.SET);
        assertThat(role.getParameter()).isEqualTo("search_path");
        assertThat(role.isUseEquals()).isTrue();
        assertThat(role.isUseDefault()).isFalse();
        assertThat(role.isFromCurrent()).isFalse();
        assertThat(role.getValues()).isSameAs(role.getParameterChange().getValues());

        role.getValues().set(0, new Column("new_schema"));
        role.setUseEquals(false);
        assertThat(role.toString())
                .isEqualTo(
                        "ALTER ROLE ALL IN DATABASE appdb SET search_path TO new_schema, public");

        role.setUseDefault(true);
        assertThat(role.getParameterChange().getValueKind()).isEqualTo(ValueKind.DEFAULT);
        assertThat(role.toString()).endsWith("SET search_path TO DEFAULT");
        role.setFromCurrent(true);
        assertThat(role.isUseDefault()).isFalse();
        assertThat(role.toString()).endsWith("SET search_path FROM CURRENT");
        role.setFromCurrent(false);
        assertThat(role.toString()).endsWith("SET search_path TO new_schema, public");

        role.setAction(AlterRole.Action.RESET);
        assertThat(role.getParameterChange().getAction()).isEqualTo(Action.RESET);
        assertThat(role.toString()).isEqualTo("ALTER ROLE ALL IN DATABASE appdb RESET search_path");

        role.getParameterChange().setAction(Action.SET);
        assertThat(role.getAction()).isEqualTo(AlterRole.Action.SET);
    }

    @Test
    void testAlterRoleLegacySetters() {
        AlterRole role = new AlterRole();
        role.setName("app");
        assertThat(role.getParameterChange()).isNull();
        assertThat(role.getParameter()).isNull();
        assertThat(role.getValues()).isNull();
        assertThat(role.isUseEquals()).isFalse();

        role.setParameter("work_mem");
        role.setAction(AlterRole.Action.SET);
        role.setValues(new ExpressionList<Expression>(new StringValue("64MB")));
        assertThat(role.toString()).isEqualTo("ALTER ROLE app SET work_mem TO '64MB'");

        role.setAction(AlterRole.Action.RENAME);
        role.setNewName("app2");
        assertThat(role.toString()).isEqualTo("ALTER ROLE app RENAME TO app2");
        assertThat(role.getParameter()).isEqualTo("work_mem");

        role.setParameterChange(new ConfigurationParameterChange(Action.RESET, "ALL"));
        assertThat(role.getAction()).isEqualTo(AlterRole.Action.RESET);
        assertThat(role.toString()).isEqualTo("ALTER ROLE app RESET ALL");
    }

    private static ConfigurationParameterChange parseChange(String clause)
            throws JSQLParserException {
        return ((AlterDatabase) CCJSqlParserUtil.parse("ALTER DATABASE d " + clause))
                .getParameterChange();
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder));
        return builder.toString();
    }
}
