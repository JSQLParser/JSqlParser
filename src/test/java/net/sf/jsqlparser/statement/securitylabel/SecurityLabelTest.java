/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.securitylabel;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.securitylabel.SecurityLabel.ObjectType;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SecurityLabelTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static SecurityLabel roundTrip(String sql) throws JSQLParserException {
        SecurityLabel label = assertInstanceOf(SecurityLabel.class,
                assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL));
        assertEquals(sql, CCJSqlParserUtil.parse(label.toString(), POSTGRESQL).toString());
        return label;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SECURITY LABEL FOR selinux ON TABLE t1 IS 'system_u:object_r:sepgsql_table_t:s0'",
            "SECURITY LABEL FOR 'dummy' ON TABLE app.t1 IS NULL",
            "SECURITY LABEL ON COLUMN app.t1.c IS 'x'",
            "SECURITY LABEL ON SEQUENCE app.s IS 'x'",
            "SECURITY LABEL ON VIEW v IS 'x'",
            "SECURITY LABEL ON MATERIALIZED VIEW app.mv IS 'x'",
            "SECURITY LABEL ON FOREIGN TABLE ft IS 'x'",
            "SECURITY LABEL ON FUNCTION app.f(int, text) IS 'x'",
            "SECURITY LABEL ON FUNCTION f IS 'x'",
            "SECURITY LABEL ON PROCEDURE p() IS 'x'",
            "SECURITY LABEL ON ROUTINE r(IN a int) IS 'x'",
            "SECURITY LABEL ON AGGREGATE agg(*) IS 'x'",
            "SECURITY LABEL ON SCHEMA app IS 'x'",
            "SECURITY LABEL ON DATABASE mydb IS 'x'",
            "SECURITY LABEL ON ROLE app_user IS 'x'",
            "SECURITY LABEL ON TABLESPACE fast_space IS 'x'",
            "SECURITY LABEL ON DOMAIN app.positive IS 'x'",
            "SECURITY LABEL ON TYPE app.mood IS 'x'",
            "SECURITY LABEL ON EVENT TRIGGER etg IS 'x'",
            "SECURITY LABEL ON LARGE OBJECT 16385 IS 'x'",
            "SECURITY LABEL ON LANGUAGE plpgsql IS 'x'",
            "SECURITY LABEL ON PROCEDURAL LANGUAGE plpgsql IS 'x'",
            "SECURITY LABEL ON PUBLICATION pub IS 'x'",
            "SECURITY LABEL ON SUBSCRIPTION sub IS 'x'"})
    void parsesEveryDocumentedObjectKind(String sql) throws JSQLParserException {
        roundTrip(sql);
        assertInstanceOf(SecurityLabel.class, CCJSqlParserUtil.parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1", POSTGRESQL).size());
    }

    @Test
    void keepsProviderTargetAndLabel() throws JSQLParserException {
        SecurityLabel label = roundTrip(
                "SECURITY LABEL FOR selinux ON TABLE app.t1 IS 'system_u:object_r:sepgsql_table_t:s0'");
        assertEquals("selinux", label.getProvider());
        assertEquals(ObjectType.TABLE, label.getObjectType());
        assertEquals("app.t1", label.getObjectName().getFullyQualifiedName());
        assertEquals("system_u:object_r:sepgsql_table_t:s0", label.getLabel().getValue());

        SecurityLabel column = roundTrip("SECURITY LABEL ON COLUMN app.t1.c IS NULL");
        assertNull(column.getProvider());
        assertEquals(ObjectType.COLUMN, column.getObjectType());
        assertEquals("c", column.getColumn().getColumnName());
        assertEquals("app.t1", column.getColumn().getTable().getFullyQualifiedName());
        assertNull(column.getLabel());

        SecurityLabel function = roundTrip("SECURITY LABEL ON FUNCTION f(int, text) IS 'x'");
        assertEquals(ObjectType.FUNCTION, function.getObjectType());
        assertEquals("f", function.getRoutine().getName());
        assertEquals(2, function.getRoutine().getArguments().size());
        assertNull(function.getObjectName());

        SecurityLabel largeObject = roundTrip("SECURITY LABEL ON LARGE OBJECT 16385 IS 'x'");
        assertEquals(ObjectType.LARGE_OBJECT, largeObject.getObjectType());
        assertEquals(16385L, largeObject.getLargeObjectOid());

        SecurityLabel language =
                roundTrip("SECURITY LABEL ON PROCEDURAL LANGUAGE plpgsql IS 'x'");
        assertEquals(ObjectType.LANGUAGE, language.getObjectType());
        assertTrue(language.isProcedural());
    }

    @Test
    void tableVisitorsSeeOnlyRelations() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("SECURITY LABEL ON TABLE app.t1 IS 'x'"))
                .containsExactly("app.t1");
        assertThat(TablesNamesFinder.findTables("SECURITY LABEL ON COLUMN app.t1.c IS 'x'"))
                .containsExactly("app.t1");
        assertThat(TablesNamesFinder.findTables("SECURITY LABEL ON FOREIGN TABLE ft IS 'x'"))
                .containsExactly("ft");
        for (String sql : List.of("SECURITY LABEL ON FUNCTION f(int) IS 'x'",
                "SECURITY LABEL ON SCHEMA app IS 'x'", "SECURITY LABEL ON ROLE r IS 'x'",
                "SECURITY LABEL ON TYPE app.t IS 'x'")) {
            assertTrue(TablesNamesFinder.findTables(sql).isEmpty(), sql);
        }
    }

    @Test
    void settersSelectTheObjectType() {
        SecurityLabel label = new SecurityLabel().withProvider("selinux")
                .withObjectName(ObjectType.VIEW, new Table("app", "v"))
                .withLabel(new StringValue("x"));
        assertDeparse(label, "SECURITY LABEL FOR selinux ON VIEW app.v IS 'x'");

        RoutineReference routine = new RoutineReference();
        routine.setName("f");
        label.setRoutine(ObjectType.PROCEDURE, routine);
        assertNull(label.getObjectName());
        assertDeparse(label, "SECURITY LABEL FOR selinux ON PROCEDURE f IS 'x'");

        label.setColumn(new Column(new Table("t"), "c"));
        assertNull(label.getRoutine());
        label.setLabel(null);
        assertDeparse(label, "SECURITY LABEL FOR selinux ON COLUMN t.c IS NULL");

        assertThrows(IllegalArgumentException.class,
                () -> label.setObjectName(ObjectType.FUNCTION, new Table("f")));
        assertThrows(IllegalArgumentException.class,
                () -> label.setRoutine(ObjectType.TABLE, routine));
    }

    @Test
    void labelUsesTheExpressionDeparser() throws JSQLParserException {
        SecurityLabel label = (SecurityLabel) CCJSqlParserUtil
                .parse("SECURITY LABEL ON TABLE t IS 'before'");
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
        label.accept(new StatementDeParser(expressions, selects, output));
        assertEquals("SECURITY LABEL ON TABLE t IS 'after'", output.toString());
    }

    @Test
    void validationAndFeatures() throws JSQLParserException {
        String sql = "SECURITY LABEL FOR selinux ON TABLE t1 IS 'x'";
        assertTrue(new Validation(List.of(PostgresqlVersion.V10), sql).validate().isEmpty());
        assertThat(new Validation(List.of(MySqlVersion.V8_0), sql).validate()).isNotEmpty();
        assertThat(CCJSqlParserUtil.parse(sql).getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertFalse(CCJSqlParserUtil.parse(sql).getFeatures().may(StmtFeature.READS_DATA));
    }

    @Test
    void wordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT security, label FROM label", false, POSTGRESQL);
    }

    @Test
    void startsAStatementAmongUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "VACUUM t; SECURITY LABEL ON TABLE t IS 'x'",
                parser -> parser.withUnsupportedStatements(true));

        assertEquals(2, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        assertInstanceOf(SecurityLabel.class, statements.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SECURITY LABEL ON TABLE t",
            "SECURITY LABEL ON TABLE t IS 1",
            "SECURITY LABEL ON INDEX i IS 'x'",
            "SECURITY LABEL ON ROLE app.r IS 'x'",
            "SECURITY LABEL ON AGGREGATE agg IS 'x'",
            "SECURITY LABEL ON LARGE OBJECT lo IS 'x'",
            "SECURITY LABEL FOR p IS 'x'",
            "SECURITY TABLE t IS 'x'"})
    void rejectsInvalidSecurityLabels(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }
}
