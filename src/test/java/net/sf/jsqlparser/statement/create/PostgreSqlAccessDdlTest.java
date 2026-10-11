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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.create.trigger.TransitionRelation;
import net.sf.jsqlparser.statement.create.trigger.TriggerEvent;
import net.sf.jsqlparser.statement.create.user.CreateUser;
import net.sf.jsqlparser.statement.grant.AlterDefaultPrivileges;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.grant.GrantOption;
import net.sf.jsqlparser.statement.grant.Privilege;
import net.sf.jsqlparser.statement.grant.PrivilegeTarget;
import net.sf.jsqlparser.statement.grant.Revoke;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.statement.role.CreateRole;
import net.sf.jsqlparser.statement.role.RoleOption;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlAccessDdlTest {
    static Stream<String> statements() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                PostgreSqlAccessDdlTest.class.getResourceAsStream("/postgresql/access-ddl.sql"),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
                    .collect(Collectors.toList()).stream();
        }
    }

    @ParameterizedTest
    @MethodSource("statements")
    void testRoundTripAndValidation(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql);
        assertThat(statement).isInstanceOfAny(CreateRole.class, AlterRole.class, Grant.class,
                Revoke.class, AlterDefaultPrivileges.class, CreateTrigger.class);
        assertThat(CCJSqlParserUtil.parse(statement.toString()).toString())
                .isEqualTo(statement.toString());
        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(statement.getFeatures().getUncertain())
                .doesNotContain(StmtFeature.MODIFIES_DATA);
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
    }

    @Test
    void testRoleOptionsAndAliases() throws Exception {
        CreateRole role = (CreateRole) CCJSqlParserUtil
                .parse("CREATE ROLE app WITH REPLICATION LOGIN CONNECTION LIMIT -1 PASSWORD NULL");
        assertThat(role.getCommand()).isEqualTo(CreateRole.Command.ROLE);
        assertThat(role.isUseWith()).isTrue();
        assertThat(role.getOptions()).extracting(RoleOption::getKind).containsExactly(
                RoleOption.Kind.REPLICATION, RoleOption.Kind.LOGIN,
                RoleOption.Kind.CONNECTION_LIMIT, RoleOption.Kind.PASSWORD);
        assertThat(((LongValue) role.getOptions().get(2).getValue()).getValue()).isEqualTo(-1);
        assertThat(((CreateRole) CCJSqlParserUtil.parse("CREATE ROLE app")).getOptions()).isEmpty();
        assertThat(CCJSqlParserUtil.parse("CREATE USER app")).isInstanceOf(CreateUser.class);
        CreateRole user = (CreateRole) CCJSqlParserUtil.parse("CREATE USER app",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertThat(user.getCommand()).isEqualTo(CreateRole.Command.USER);
        assertThat(user.getOptions()).isEmpty();
        assertThat(CCJSqlParserUtil
                .parse("CREATE USER 'app'@'localhost' IDENTIFIED BY 'example-only'"))
                .isInstanceOf(CreateUser.class);
        assertThat(TablesNamesFinder.findTables(role.toString())).isEmpty();
    }

    @Test
    void testAlterRoleSettings() throws Exception {
        AlterRole role = (AlterRole) CCJSqlParserUtil
                .parse("ALTER ROLE ALL IN DATABASE appdb SET search_path TO app, public");
        assertThat(role.getAction()).isEqualTo(AlterRole.Action.SET);
        assertThat(role.getName()).isEqualTo("ALL");
        assertThat(role.getDatabase()).isEqualTo("appdb");
        assertThat(role.getValues()).hasSize(2);
        role.getValues().set(0, new Column("new_schema"));
        assertThat(role.toString()).endsWith("TO new_schema, public");
        assertThat(TablesNamesFinder.findTables(role.toString())).isEmpty();
    }

    @Test
    void testTypedPrivilegesAndLegacyMutation() throws Exception {
        Grant grant = (Grant) CCJSqlParserUtil.parse(
                "GRANT SELECT (id), UPDATE (name) ON TABLE app.users, app.orders TO app WITH GRANT OPTION");
        assertThat(grant.getPrivilegeItems().get(0).getKind()).isEqualTo(Privilege.Kind.SELECT);
        assertThat(grant.getPrivilegeItems().get(0).getColumns().get(0).getColumnName())
                .isEqualTo("id");
        assertThat(grant.getTarget().getKind()).isEqualTo(PrivilegeTarget.Kind.TABLE);
        assertThat(grant.getTarget().getNames()).hasSize(2);
        assertThat(grant.getOption().getKind()).isEqualTo(GrantOption.Kind.GRANT);
        grant.addPrivileges("INSERT");
        assertThat(grant.getPrivilegeItems().get(0).getColumns()).hasSize(1);
        grant.getPrivileges().set(1, "DELETE");
        grant.getUsers().add("reader");
        grant.getObjectNameParts().set(1, "new_users");
        assertThat(grant.getPrivilegeItems().get(1).getKind()).isEqualTo(Privilege.Kind.DELETE);
        assertThat(grant.toString()).contains("app.new_users, app.orders TO app, reader");
        assertThat(TablesNamesFinder.findTables(grant.toString()))
                .containsExactlyInAnyOrder("app.new_users", "app.orders");
        Grant created = new Grant().addPrivileges("SELECT")
                .withObjectName(Arrays.asList("app", "users")).addUsers("app");
        assertThat(created.toString()).isEqualTo("GRANT SELECT ON app.users TO app");
        assertThat(created.getTarget().getNames()).containsExactly(Arrays.asList("app", "users"));
        created.setObjectName(Arrays.asList("catalog", null, "users"));
        assertThat(created.toString()).isEqualTo("GRANT SELECT ON catalog..users TO app");
    }

    @Test
    void testRoutineSignaturesAndNonRelationTargets() throws Exception {
        Grant grant = (Grant) CCJSqlParserUtil
                .parse("GRANT EXECUTE ON FUNCTION app.f(IN arg integer), app.g(), app.h TO app");
        List<RoutineReference> routines = grant.getTarget().getRoutines();
        assertThat(routines).hasSize(3);
        assertThat(routines.get(0).getArguments().get(0).getMode())
                .isEqualTo(RoutineReference.Argument.Mode.IN);
        assertThat(routines.get(0).getArguments().get(0).getName()).isEqualTo("arg");
        assertThat(routines.get(1).getArguments()).isEmpty();
        assertThat(routines.get(2).getArguments()).isNull();
        assertThat(TablesNamesFinder.findTables(grant.toString())).isEmpty();
        assertThat(
                TablesNamesFinder.findTables("GRANT SELECT ON ALL TABLES IN SCHEMA app TO reader"))
                .isEmpty();
        assertThat(TablesNamesFinder.findTables("GRANT USAGE ON SCHEMA app TO reader")).isEmpty();
    }

    @Test
    void testRevokeAndDefaults() throws Exception {
        Revoke revoke = (Revoke) CCJSqlParserUtil
                .parse("REVOKE ADMIN OPTION FOR reader, writer FROM app GRANTED BY owner CASCADE");
        assertThat(revoke.getOptionFor()).isEqualTo(GrantOption.Kind.ADMIN);
        assertThat(revoke.getClause().getRoles()).containsExactly("reader", "writer");
        assertThat(revoke.getClause().getGrantedBy()).isEqualTo("owner");
        assertThat(revoke.getBehavior()).isEqualTo(CascadeBehavior.CASCADE);
        AlterDefaultPrivileges defaults = (AlterDefaultPrivileges) CCJSqlParserUtil.parse(
                "ALTER DEFAULT PRIVILEGES FOR USER owner IN SCHEMA app GRANT SELECT ON TABLES TO reader");
        assertThat(defaults.getRoleKeyword()).isEqualTo(AlterDefaultPrivileges.RoleKeyword.USER);
        assertThat(defaults.getGrant().getTarget().isDefaultPrivileges()).isTrue();
        assertThat(defaults.getSchemas()).containsExactly("app");
        assertThat(TablesNamesFinder.findTables(defaults.toString())).isEmpty();
    }

    @Test
    void testTriggerFields() throws Exception {
        CreateTrigger trigger = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE CONSTRAINT TRIGGER check_parent AFTER INSERT OR UPDATE ON child FROM parent DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (NEW.id > 0) EXECUTE PROCEDURE app.check_parent('id')");
        assertThat(trigger.getEvent()).isEqualTo(CreateTrigger.Event.INSERT);
        assertThat(trigger.getEvents()).extracting(TriggerEvent::getEvent)
                .containsExactly(CreateTrigger.Event.INSERT, CreateTrigger.Event.UPDATE);
        assertThat(trigger.getConstraintAttributes().getInitially())
                .isEqualTo(ConstraintAttributes.Initially.DEFERRED);
        assertThat(trigger.getBody()).isNull();
        assertThat(trigger.getRoutine().getName()).isEqualTo("app.check_parent");
        assertThat(trigger.getExecuteKeyword()).isEqualTo(CreateTrigger.ExecuteKeyword.PROCEDURE);
        assertThat(TablesNamesFinder.findTables(trigger.toString()))
                .containsExactlyInAnyOrder("child", "parent");
        CreateTrigger transition = (CreateTrigger) CCJSqlParserUtil.parse(
                "CREATE TRIGGER audit AFTER INSERT ON users REFERENCING NEW TABLE AS inserted EXECUTE FUNCTION audit_all()");
        assertThat(transition.getOrientation()).isNull();
        assertThat(transition.getTransitionRelations().get(0).getImage())
                .isEqualTo(TransitionRelation.Image.NEW);
        assertThat(transition.getTransitionRelations().get(0).isUseAs()).isTrue();
        assertThat(TablesNamesFinder.findTables(transition.toString())).containsExactly("users");
    }

    @Test
    void testVisitorsAndDeparser() throws Exception {
        Statement trigger = CCJSqlParserUtil.parse(
                "CREATE TRIGGER audit BEFORE UPDATE OF id ON users FOR EACH ROW WHEN (NEW.id > 0) EXECUTE FUNCTION audit_row('example')");
        List<String> columns = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                assertThat(context).isEqualTo("access");
                columns.add(column.getColumnName());
                return null;
            }

            @Override
            public <S> Void visit(StringValue value, S context) {
                assertThat(context).isEqualTo("access");
                strings.add(value.getValue());
                return null;
            }
        };
        StatementVisitorAdapter<Void> visitor =
                new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions));
        trigger.accept(visitor, "access");
        CCJSqlParserUtil.parse("GRANT SELECT (name) ON users TO app").accept(visitor, "access");
        CCJSqlParserUtil.parse("CREATE ROLE app PASSWORD 'example-only'").accept(visitor, "access");
        assertThat(columns).containsExactly("id", "id", "name");
        assertThat(strings).containsExactly("example", "example-only");
        StringBuilder output = new StringBuilder();
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                return getBuilder().append("'changed'");
            }
        };
        trigger.accept(new StatementDeParser(deparser, new SelectDeParser(), output));
        assertThat(output.toString()).endsWith("audit_row('changed')");
        assertThat(trigger.toString()).endsWith("audit_row('example')");
    }

    @Test
    void testProgrammaticTriggerAndRole() throws Exception {
        CreateTrigger trigger = new CreateTrigger();
        trigger.setTrigger(new Table("audit"));
        trigger.setTable(new Table("users"));
        trigger.setTiming(CreateTrigger.Timing.AFTER);
        trigger.setEvent(CreateTrigger.Event.INSERT);
        Function routine = new Function();
        routine.setName("audit_row");
        routine.setParameters(new ExpressionList<>());
        trigger.setRoutine(routine);
        assertThat(CCJSqlParserUtil.parse(trigger.toString()).toString())
                .isEqualTo(trigger.toString());
        CreateRole role = new CreateRole();
        role.setName("reader");
        role.getOptions().add(new RoleOption(RoleOption.Kind.NOLOGIN));
        assertThat(CCJSqlParserUtil.parse(role.toString()).toString())
                .isEqualTo("CREATE ROLE reader NOLOGIN");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OR REPLACE ROLE app", "CREATE ROLE app WITH", "CREATE ROLE app CONNECTION 10",
            "CREATE ROLE app PASSWORD", "ALTER ROLE ALL LOGIN",
            "ALTER ROLE app IN DATABASE appdb LOGIN",
            "ALTER ROLE app IN ROLE reader", "ALTER GROUP staff LOGIN",
            "GRANT SELECT ON users FROM app", "REVOKE SELECT ON users TO app",
            "GRANT SELECT () ON users TO app", "GRANT SELECT ON users TO app WITH ADMIN OPTION",
            "GRANT reader TO app WITH GRANT OPTION",
            "REVOKE ADMIN OPTION FOR SELECT ON users FROM app",
            "ALTER DEFAULT PRIVILEGES GRANT SELECT ON TABLE users TO app",
            "ALTER DEFAULT PRIVILEGES GRANT SELECT ON TABLES TO app GRANTED BY owner",
            "CREATE TRIGGER audit BEFORE INSERT ON users FOR EACH ROW EXECUTE FUNCTION audit_row(id + 1)",
            "CREATE CONSTRAINT TRIGGER audit BEFORE INSERT ON users FOR EACH ROW EXECUTE FUNCTION audit_row()",
            "CREATE TRIGGER audit AFTER INSERT ON users DEFERRABLE EXECUTE FUNCTION audit_row()",
            "CREATE OR REPLACE CONSTRAINT TRIGGER audit AFTER INSERT ON users FOR EACH ROW EXECUTE FUNCTION audit_row()"
    })
    void testInvalidSyntax(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }
}
