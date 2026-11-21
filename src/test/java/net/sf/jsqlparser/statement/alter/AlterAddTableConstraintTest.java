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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.NotNullConstraint;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ALTER TABLE ADD accepts the table constraint forms of CREATE TABLE in every dialect, not only
 * with the MySQL and PostgreSQL dialects.
 */
class AlterAddTableConstraintTest {
    private static final List<Dialect> DIALECTS =
            Arrays.asList(null, Dialect.ANSI_SQL, Dialect.ORACLE, Dialect.SQLSERVER, Dialect.H2);

    @ParameterizedTest
    @ValueSource(strings = {
            "CHECK (id > 0)",
            "CHECK (id > 0) NOT VALID",
            "CONSTRAINT ck CHECK (id > 0) NOT VALID",
            "FOREIGN KEY (id) REFERENCES parent (id) DEFERRABLE INITIALLY DEFERRED",
            "CONSTRAINT fk FOREIGN KEY (id) REFERENCES parent (id) NOT VALID",
            "CONSTRAINT pk PRIMARY KEY (id) DEFERRABLE INITIALLY DEFERRED",
            "CONSTRAINT uq UNIQUE (id) DEFERRABLE INITIALLY IMMEDIATE",
            "CONSTRAINT ex EXCLUDE USING gist (id WITH =)"})
    void createAndAlterExposeTheSameConstraintInEveryDialect(String definition)
            throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            Consumer<CCJSqlParser> configuration = parser -> {
                if (dialect != null) {
                    parser.withDialect(dialect);
                }
            };
            CreateTable create = (CreateTable) CCJSqlParserUtil
                    .parse("CREATE TABLE t (id INT, " + definition + ")", configuration);
            Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                    "ALTER TABLE t ADD " + definition + ", ADD COLUMN other INT", true,
                    configuration);
            NamedConstraint expected = create.getTableConstraints().get(0);
            NamedConstraint actual = alter.getAlterExpressions().get(0).getConstraint();
            assertEquals(expected.getClass(), actual.getClass(), String.valueOf(dialect));
            assertEquals(expected.toString(), actual.toString(), String.valueOf(dialect));
            assertEquals(2, alter.getAlterExpressions().size());
        }
    }

    @Test
    void attributesAndReferencesAreStructuredWithoutADialect() throws JSQLParserException {
        String sql = "ALTER TABLE t ADD CONSTRAINT fk FOREIGN KEY (id) REFERENCES parent (id) "
                + "DEFERRABLE INITIALLY DEFERRED";
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        ForeignKeyConstraint foreignKey = assertInstanceOf(ForeignKeyConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
        ConstraintAttributes attributes = foreignKey.getConstraintAttributes();
        assertEquals(Boolean.TRUE, attributes.getDeferrable());
        assertEquals(ConstraintAttributes.Initially.DEFERRED, attributes.getInitially());
        assertThat(TablesNamesFinder.findTables(sql)).containsExactlyInAnyOrder("t", "parent");

        CheckConstraint check = assertInstanceOf(CheckConstraint.class,
                ((Alter) CCJSqlParserUtil.parse("ALTER TABLE t ADD CHECK (id > 0) NOT VALID"))
                        .getAlterExpressions().get(0).getConstraint());
        assertNull(check.getName());
        assertTrue(check.getConstraintAttributes().isNotValid());
    }

    @Test
    void formsThatAlreadyParsedKeepTheGenericRepresentation() throws JSQLParserException {
        AlterExpression deferrable = ((Alter) CCJSqlParserUtil
                .parse("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (id) DEFERRABLE"))
                .getAlterExpressions().get(0);
        assertThat(deferrable.getConstraints()).hasSize(1);
        assertInstanceOf(DeferrableConstraint.class, deferrable.getConstraints().get(0));

        AlterExpression unique = ((Alter) CCJSqlParserUtil
                .parse("ALTER TABLE t ADD CONSTRAINT UNIQUE KEY uk (id)"))
                .getAlterExpressions().get(0);
        assertEquals("UNIQUE KEY", unique.getConstraintType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ADD CONSTRAINT nn NOT NULL id",
            "ALTER TABLE t ADD NOT NULL id",
            "ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NO INHERIT",
            "ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NOT VALID",
            "ALTER TABLE t ADD CONSTRAINT uk UNIQUE USING INDEX t_idx",
            "ALTER TABLE t ADD PRIMARY KEY USING INDEX t_idx",
            "ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY USING INDEX t_idx DEFERRABLE",
            "ALTER TABLE t ADD COLUMN c INT, ADD CONSTRAINT nn NOT NULL id, ADD CONSTRAINT uk UNIQUE USING INDEX t_idx"})
    void postgreSqlOnlyFormsParseWithoutADialectAsWithPostgreSql(String sql)
            throws JSQLParserException {
        Alter withoutDialect = (Alter) CCJSqlParserUtil.parse(sql);
        Alter postgreSql = (Alter) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(sql, withoutDialect.toString());
        assertEquals(postgreSql.toString(), withoutDialect.toString());
        for (int i = 0; i < postgreSql.getAlterExpressions().size(); i++) {
            AlterExpression expected = postgreSql.getAlterExpressions().get(i);
            AlterExpression actual = withoutDialect.getAlterExpressions().get(i);
            assertEquals(expected.getOperation(), actual.getOperation());
            assertEquals(
                    expected.getConstraint() == null ? null : expected.getConstraint().getClass(),
                    actual.getConstraint() == null ? null : actual.getConstraint().getClass());
        }
    }

    @Test
    void postgreSqlOnlyFormsExposeTheirConstraintWithoutADialect() throws JSQLParserException {
        AlterExpression notNull = ((Alter) CCJSqlParserUtil
                .parse("ALTER TABLE t ADD CONSTRAINT nn NOT NULL id"))
                .getAlterExpressions().get(0);
        NotNullConstraint notNullConstraint =
                assertInstanceOf(NotNullConstraint.class, notNull.getConstraint());
        assertEquals("nn", notNullConstraint.getName());
        assertEquals("id", notNullConstraint.getColumn().getColumnName());

        AlterExpression usingIndex = ((Alter) CCJSqlParserUtil
                .parse("ALTER TABLE t ADD CONSTRAINT uk UNIQUE USING INDEX t_idx"))
                .getAlterExpressions().get(0);
        ConstraintUsingIndex usingIndexConstraint =
                assertInstanceOf(ConstraintUsingIndex.class, usingIndex.getConstraint());
        assertEquals("uk", usingIndexConstraint.getName());
        assertEquals("t_idx", usingIndexConstraint.getExistingIndexName());
    }

    @Test
    void tableLevelNotNullParsesInCreateTableWithoutADialect() throws JSQLParserException {
        String sql = "CREATE TABLE t (id INT, CONSTRAINT nn NOT NULL id NO INHERIT)";
        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
        assertEquals(sql, createTable.toString());
        assertEquals(CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL))
                .toString(), createTable.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ADD CONSTRAINT nn NOT NULL id",
            "ALTER TABLE t ADD CONSTRAINT uk UNIQUE USING INDEX t_idx"})
    void postgreSqlOnlyFormsAreStillRejectedWithAnotherDialect(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.ORACLE}) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)));
        }
    }

    @Test
    void enforcedPrimaryKeyIsRejected() {
        JSQLParserException exception = assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil
                        .parse("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (id) ENFORCED"));
        assertThat(exception).hasStackTraceContaining(
                "ENFORCED is supported only for CHECK and foreign key constraints");
    }
}
