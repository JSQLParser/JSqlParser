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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.alter.AlterDomain;
import net.sf.jsqlparser.statement.alter.AlterExtension;
import net.sf.jsqlparser.statement.alter.AlterType;
import net.sf.jsqlparser.statement.create.domain.CreateDomain;
import net.sf.jsqlparser.statement.create.domain.DomainConstraint;
import net.sf.jsqlparser.statement.create.extension.CreateExtension;
import net.sf.jsqlparser.statement.create.type.CreateType;
import net.sf.jsqlparser.statement.create.type.TypeAttribute;
import net.sf.jsqlparser.statement.create.type.EnumTypeDefinition;
import net.sf.jsqlparser.statement.create.type.CompositeTypeDefinition;
import net.sf.jsqlparser.statement.create.type.RangeTypeDefinition;
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

class PostgreSqlTypeDdlTest {
    static Stream<String> statements() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                PostgreSqlTypeDdlTest.class.getResourceAsStream("/postgresql/type-ddl.sql"),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
                    .collect(Collectors.toList())
                    .stream();
        }
    }

    @ParameterizedTest
    @MethodSource("statements")
    void testRoundTripAndValidation(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql);
        assertThat(statement).isInstanceOfAny(CreateType.class, AlterType.class, CreateDomain.class,
                AlterDomain.class, CreateExtension.class, AlterExtension.class);
        assertThat(CCJSqlParserUtil.parse(statement.toString()).toString())
                .isEqualTo(statement.toString());
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
    }

    @Test
    void testTypeDefinitionsAndConstruction() throws Exception {
        CreateType enumeration =
                (CreateType) CCJSqlParserUtil.parse("CREATE TYPE mood AS ENUM ('sad', 'ok')");
        EnumTypeDefinition definition = (EnumTypeDefinition) enumeration.getDefinition();
        assertThat(definition.getLabels()).extracting(StringValue::getValue).containsExactly("sad",
                "ok");
        definition.getLabels().add(new StringValue("happy"));
        assertThat(enumeration.toString())
                .isEqualTo("CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy')");

        CreateType composite = (CreateType) CCJSqlParserUtil
                .parse("CREATE TYPE item AS (name text COLLATE \"C\")");
        TypeAttribute attribute =
                ((CompositeTypeDefinition) composite.getDefinition()).getAttributes().get(0);
        assertThat(attribute.getName()).isEqualTo("name");
        assertThat(attribute.getDataType().getDataType()).isEqualTo("text");
        assertThat(attribute.getCollation()).isEqualTo("\"C\"");

        CreateType range = (CreateType) CCJSqlParserUtil
                .parse("CREATE TYPE r AS RANGE (SUBTYPE = numeric, SUBTYPE_DIFF = app.diff)");
        RangeTypeDefinition rangeDefinition = (RangeTypeDefinition) range.getDefinition();
        assertThat(rangeDefinition.getSubtype().getDataType()).isEqualTo("numeric");
        assertThat(rangeDefinition.getOptions().get(1).getKind())
                .isEqualTo(RangeTypeDefinition.OptionKind.SUBTYPE_DIFF);
        assertThat(rangeDefinition.getOptions().get(1).getName()).isEqualTo("app.diff");

        CreateType constructed = new CreateType();
        constructed.setName("new_mood");
        constructed.setDefinition(definition);
        assertThat(CCJSqlParserUtil.parse(constructed.toString()).toString())
                .isEqualTo(constructed.toString());
    }

    @Test
    void testTypeAlterations() throws Exception {
        AlterType alteration = (AlterType) CCJSqlParserUtil
                .parse("ALTER TYPE mood ADD VALUE IF NOT EXISTS 'fine' AFTER 'ok'");
        assertThat(alteration.getAction()).isEqualTo(AlterType.Action.ADD_VALUE);
        assertThat(alteration.isIfNotExists()).isTrue();
        assertThat(alteration.getPosition()).isEqualTo(AlterType.Position.AFTER);
        assertThat(alteration.getValue().getValue()).isEqualTo("fine");
        assertThat(alteration.getNeighborValue().getValue()).isEqualTo("ok");
        AlterType attributes = (AlterType) CCJSqlParserUtil.parse(
                "ALTER TYPE item ADD ATTRIBUTE code text, DROP ATTRIBUTE IF EXISTS price CASCADE");
        assertThat(attributes.getAttributeChanges()).hasSize(2);
        assertThat(attributes.getAttributeChanges().get(1).isIfExists()).isTrue();
        assertThat(attributes.getAttributeChanges().get(1).getBehavior())
                .isEqualTo(CascadeBehavior.CASCADE);
    }

    @Test
    void testDomainConstraintsAndTraversal() throws Exception {
        CreateDomain domain = (CreateDomain) CCJSqlParserUtil.parse(
                "CREATE DOMAIN positive AS integer DEFAULT 1 CONSTRAINT required NOT NULL CHECK (VALUE > 0)");
        assertThat(domain.isUseAs()).isTrue();
        assertThat(domain.getConstraints()).extracting(DomainConstraint::getKind)
                .containsExactly(DomainConstraint.Kind.NOT_NULL, DomainConstraint.Kind.CHECK);
        assertThat(domain.getConstraints().get(0).getName()).isEqualTo("required");
        AlterDomain altered = (AlterDomain) CCJSqlParserUtil.parse(
                "ALTER DOMAIN positive ADD CONSTRAINT positive_check CHECK (VALUE > 1) NOT VALID");
        assertThat(altered.isNotValid()).isTrue();
        assertThat(altered.getConstraint().getExpression()).isNotNull();

        List<String> names = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                assertThat(context).isEqualTo("domain");
                names.add(column.getColumnName());
                return null;
            }
        };
        StatementVisitorAdapter<Void> visitor =
                new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions));
        domain.accept(visitor, "domain");
        altered.accept(visitor, "domain");
        assertThat(names).containsExactly("VALUE", "VALUE");
        assertThat(TablesNamesFinder.findTables(domain.toString())).isEmpty();
    }

    @Test
    void testDomainExpressionDeparser() throws Exception {
        for (String sql : new String[] {"CREATE DOMAIN positive AS integer CHECK (VALUE > 0)",
                "ALTER DOMAIN positive ADD CHECK (VALUE > 0) NOT VALID"}) {
            StringBuilder output = new StringBuilder();
            ExpressionDeParser expressions = new ExpressionDeParser() {
                @Override
                public <S> StringBuilder visit(Column column, S context) {
                    return getBuilder().append("new_").append(column.getColumnName());
                }
            };
            Statement statement = CCJSqlParserUtil.parse(sql);
            statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output));
            assertThat(output.toString()).contains("new_VALUE");
            assertThat(statement.toString()).doesNotContain("new_VALUE");
            CCJSqlParserUtil.parse(output.toString());
        }
    }

    @Test
    void testExtensionOptionsAndRoutineSignatures() throws Exception {
        CreateExtension extension = (CreateExtension) CCJSqlParserUtil.parse(
                "CREATE EXTENSION IF NOT EXISTS hstore WITH VERSION '1.8' SCHEMA public CASCADE");
        assertThat(extension.isIfNotExists()).isTrue();
        assertThat(extension.isUseWith()).isTrue();
        assertThat(extension.getOptions()).extracting(CreateExtension.Option::getKind)
                .containsExactly(CreateExtension.OptionKind.VERSION,
                        CreateExtension.OptionKind.SCHEMA, CreateExtension.OptionKind.CASCADE);
        assertThat(extension.getOptions().get(0).getVersion()).isInstanceOf(StringValue.class);
        AlterExtension alteration = (AlterExtension) CCJSqlParserUtil
                .parse("ALTER EXTENSION hstore ADD FUNCTION app.f(integer, text[])");
        assertThat(alteration.getMember().getRoutine().getArguments()).hasSize(2);
        assertThat(alteration.getMember().getRoutine().getArguments().get(1).getDataType()
                .getArrayData()).hasSize(1);
        assertThat(TablesNamesFinder.findTables(alteration.toString())).isEmpty();
        assertThat(TablesNamesFinder.findTables("ALTER EXTENSION hstore ADD TABLE app.settings"))
                .containsExactly("app.settings");
        assertThat(TablesNamesFinder.findTables("CREATE TYPE item AS (id integer)")).isEmpty();
        assertThat(TablesNamesFinder.findTables("ALTER TYPE item SET SCHEMA app")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE OR REPLACE TYPE t AS ENUM ('a')",
            "CREATE OR REPLACE DOMAIN d AS integer", "CREATE OR REPLACE EXTENSION e",
            "CREATE TYPE r AS RANGE (UNKNOWN = integer)",
            "CREATE TYPE r AS RANGE (CANONICAL = f)",
            "ALTER TYPE mood ADD VALUE ok", "ALTER TYPE item ADD ATTRIBUTE x",
            "CREATE DOMAIN positive AS integer CHECK ()",
            "ALTER DOMAIN positive ADD NULL", "ALTER DOMAIN positive ADD NOT NULL NOT VALID",
            "CREATE EXTENSION hstore FROM '1.0'", "ALTER EXTENSION hstore ADD UNKNOWN app.x",
            "ALTER EXTENSION hstore ADD FUNCTION f(*)", "ALTER EXTENSION hstore ADD AGGREGATE f"})
    void testInvalidSyntax(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }
}
