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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SetConstraintsStatementTest {
    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static SetConstraintsStatement roundTrip(String sql, Dialect dialect)
            throws Exception {
        SetConstraintsStatement statement =
                assertInstanceOf(SetConstraintsStatement.class, parse(sql, dialect));
        TransactionStatementTest.assertSpans(sql, sql, statement);
        assertEquals(sql, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        assertEquals(sql, parse(sql, dialect).toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_TRANSACTION),
                statement.getFeatures().getCertain());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        return statement;
    }

    @Test
    void allConstraints() throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.ORACLE, null}) {
            SetConstraintsStatement statement = roundTrip("SET CONSTRAINTS ALL DEFERRED", dialect);
            assertTrue(statement.isAll());
            assertTrue(statement.getConstraintNames().isEmpty());
            assertEquals(ConstraintAttributes.Initially.DEFERRED, statement.getTiming());
        }
    }

    @Test
    void namedConstraintsKeepQualifiersAndQuotes() throws Exception {
        SetConstraintsStatement statement = roundTrip(
                "SET CONSTRAINTS c1, app.c2, \"Mixed Case\" IMMEDIATE", Dialect.POSTGRESQL);
        assertFalse(statement.isAll());
        assertEquals(List.of("c1", "app.c2", "\"Mixed Case\""), statement.getConstraintNames());
        assertEquals(ConstraintAttributes.Initially.IMMEDIATE, statement.getTiming());
    }

    @Test
    void namedConstraintsAreNoLongerReadAsAssignments() throws Exception {
        // master read this as the assignments CONSTRAINTS = c1 and c2 = IMMEDIATE
        String sql = "SET CONSTRAINTS c1, c2 IMMEDIATE";
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            assertEquals(List.of("c1", "c2"), roundTrip(sql, dialect).getConstraintNames());
        }
        assertInstanceOf(SetStatement.class, parse("SET constraints = 1", Dialect.POSTGRESQL));
        assertInstanceOf(SetStatement.class, parse("SET constraints TO 1", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET CONSTRAINTS ALL LATER", "SET CONSTRAINTS ALL",
            "SET CONSTRAINTS c1,", "SET CONSTRAINTS c1, IMMEDIATE",
            "SET CONSTRAINTS ALL, c1 DEFERRED", "SET CONSTRAINTS ALL DEFERRED garbage"})
    void malformedFormsAreRejected(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET CONSTRAINTS ALL DEFERRED", "SET CONSTRAINTS c1, c2 IMMEDIATE"})
    void mysqlRejectsSetConstraints(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL));
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MARIADB));
    }

    @Test
    void modelCanBeBuiltAndChanged() {
        SetConstraintsStatement statement = new SetConstraintsStatement();
        assertEquals("SET CONSTRAINTS ALL IMMEDIATE", statement.toString());
        statement.addConstraintName("c1").addConstraintName("s.c2")
                .withTiming(ConstraintAttributes.Initially.DEFERRED);
        assertEquals("SET CONSTRAINTS c1, s.c2 DEFERRED", statement.toString());
        statement.getConstraintNames().clear();
        assertEquals("SET CONSTRAINTS ALL DEFERRED", statement.toString());
        assertThrows(NullPointerException.class, () -> statement.setTiming(null));
        assertThrows(NullPointerException.class, () -> statement.addConstraintName(null));
    }

    @Test
    void visitorsBoundariesAndValidation() throws Exception {
        Statement statement = parse("SET CONSTRAINTS ALL DEFERRED", Dialect.POSTGRESQL);
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(SetConstraintsStatement set, S context) {
                return context + ":" + set.getTiming();
            }
        };
        assertEquals("ctx:DEFERRED", statement.accept(visitor, "ctx"));
        assertNull(statement.accept(new StatementVisitorAdapter<>(), null));
        Statements script = CCJSqlParserUtil.parseStatements(
                "BEGIN; SET CONSTRAINTS ALL DEFERRED; INSERT INTO t VALUES (1); COMMIT",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(4, script.size());
        assertInstanceOf(SetConstraintsStatement.class, script.get(1));
        FeatureConfiguration none = new FeatureConfiguration();
        assertTrue(new Validation(none, List.of(PostgresqlVersion.V10),
                "SET CONSTRAINTS c1 IMMEDIATE").validate().isEmpty());
        assertFalse(new Validation(none, List.of(MySqlVersion.V8_0),
                "SET CONSTRAINTS ALL DEFERRED").validate().isEmpty());
    }
}
