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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.rule.CreateRule;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL ALTER RULE name ON table RENAME TO new_name, see
 * https://www.postgresql.org/docs/18/sql-alterrule.html.
 */
class AlterRuleTest extends ValidationTestAsserts {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER RULE r1 ON t RENAME TO r2",
            "ALTER RULE r1 ON s.t RENAME TO r2",
            "ALTER RULE \"Rule 1\" ON public.t RENAME TO \"Rule 2\"",
            "ALTER RULE r9 ON d.public.t RENAME TO r2",
            "ALTER RULE rule ON t RENAME TO rename",
            "ALTER RULE int ON t RENAME TO between"})
    void testRenameRule(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isInstanceOf(AlterRule.class);
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(AlterRule.class);
        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        statement.accept(deparser);
        assertThat(deparser.getBuilder()).hasToString(sql);
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);

        Statements script = CCJSqlParserUtil.parseStatements(
                sql + "; CREATE RULE r AS ON INSERT TO t DO NOTHING; " + sql, POSTGRESQL);
        assertThat(script).hasSize(3);
        assertThat(script.get(0)).hasToString(sql);
        assertThat(script.get(1)).isInstanceOf(CreateRule.class);
        assertThat(script.get(2)).hasToString(sql);
    }

    @Test
    void testFieldsTablesAndVisitors() throws Exception {
        AlterRule rule = (AlterRule) CCJSqlParserUtil.parse(
                "ALTER RULE \"Rule 1\" ON app.t RENAME TO r2", POSTGRESQL);
        assertThat(rule.getName()).isEqualTo("\"Rule 1\"");
        assertThat(rule.getTable().getFullyQualifiedName()).isEqualTo("app.t");
        assertThat(rule.getNewName()).isEqualTo("r2");
        assertThat(new TablesNamesFinder<Void>().getTables((Statement) rule))
                .containsExactly("app.t");

        List<String> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<>();
        FromItemVisitorAdapter<Void> fromItems = new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S context) {
                visited.add(table.getFullyQualifiedName());
                return null;
            }
        };
        rule.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions,
                new PivotVisitorAdapter<>(expressions), new SelectItemVisitorAdapter<>(expressions),
                fromItems)));
        assertThat(visited).containsExactly("app.t");
        assertThat(rule.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterRule statement, S context) {
                return context + ":" + statement.getName();
            }
        }, "context")).isEqualTo("context:\"Rule 1\"");

        rule.getTable().setName("u");
        rule.setNewName("r3");
        assertThat(rule).hasToString("ALTER RULE \"Rule 1\" ON app.u RENAME TO r3");
        assertThat(new AlterRule().withName("a").withTable(new Table("t")).withNewName("b"))
                .hasToString("ALTER RULE a ON t RENAME TO b");
    }

    @Test
    void testValidation() throws Exception {
        String sql = "ALTER RULE r1 ON t RENAME TO r2";
        validateNoErrors(sql, 1, FeaturesAllowed.ALTER);
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT, Feature.alterRule);
        validate(sql, 1, 1, MySqlVersion.V8_0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER RULE r9 ON a.b.c.t RENAME TO r2",
            "ALTER RULE IF EXISTS r9 ON t RENAME TO r2",
            "ALTER RULE r9 ON ONLY t RENAME TO r2",
            "ALTER RULE r9 ON t* RENAME TO r2",
            "ALTER RULE r9 ON t RENAME r2",
            "ALTER RULE r9 RENAME TO r2",
            "ALTER RULE s.r9 ON t RENAME TO r2",
            "ALTER RULE r9 ON t RENAME TO s.r2",
            "ALTER RULE r9 ON t OWNER TO postgres",
            "ALTER RULE r9 ON t SET SCHEMA s",
            "ALTER RULE r9 ON t RENAME TO r2 CASCADE",
            "ALTER RULE select ON t RENAME TO r2",
            "ALTER RULE r9 ON t RENAME TO select",
            "ALTER RULE left ON t RENAME TO r2",
            "ALTER RULE r9 ON t RENAME TO inner",
            "ALTER RULE 'r9' ON t RENAME TO r2",
            "ALTER RULE r9 ON t RENAME TO 1"})
    void testRejectedForms(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql, POSTGRESQL))
                .isInstanceOf(JSQLParserException.class);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }
}
