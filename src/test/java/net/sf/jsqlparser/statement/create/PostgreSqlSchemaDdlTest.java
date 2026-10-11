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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.schema.SequenceOwnership;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.LikeClause;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.IdentityAlteration;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.IdentityDefinition;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @see <a href="https://www.postgresql.org/docs/18/sql-createtable.html">PostgreSQL CREATE
 *      TABLE</a>
 */
class PostgreSqlSchemaDdlTest {
    @Test
    void testExclusionConstraint() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE reservations (room integer, during tsrange,"
                        + " CONSTRAINT no_overlap EXCLUDE USING gist (room WITH =, during WITH &&)"
                        + " WHERE (room > 0) DEFERRABLE INITIALLY DEFERRED)");
        ExcludeConstraint constraint = (ExcludeConstraint) table.getTableConstraints().get(0);
        assertThat(constraint.getName()).isEqualTo("no_overlap");
        assertThat(constraint.getUsing()).isEqualTo("gist");
        assertThat(constraint.getColumns().get(0).getExclusionOperator()).isEqualTo("=");
        assertThat(constraint.getColumns().get(1).getExclusionOperator()).isEqualTo("&&");
        assertThat(constraint.getExpression()).isNotNull();
        assertThat(constraint.getConstraintAttributes().getDeferrable()).isTrue();
        assertThat(constraint.getConstraintAttributes().getInitially())
                .isEqualTo(ConstraintAttributes.Initially.DEFERRED);
    }

    @Test
    void testExclusionExpressionAndStorage() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE reservations (room text, EXCLUDE USING gist ((lower(room)) WITH =)"
                        + " INCLUDE (room) WITH (fillfactor = 70) USING INDEX TABLESPACE archive)");
        ExcludeConstraint constraint = (ExcludeConstraint) table.getTableConstraints().get(0);
        assertThat(constraint.getColumns().get(0).isExpression()).isTrue();
        assertThat(constraint.getIncludeColumns()).containsExactly("room");
        assertThat(constraint.getStorageParameters()).hasSize(1);
        assertThat(constraint.getTableSpace()).isEqualTo("archive");
    }

    @Test
    void testUniqueNullTreatment() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE contacts (email text UNIQUE NULLS NOT DISTINCT,"
                        + " CONSTRAINT contact_email UNIQUE NULLS DISTINCT (email))");
        ColumnOption option = table.getColumnDefinitions().get(0).getColumnOptions().get(0);
        assertThat(option.getKind()).isEqualTo(ColumnOption.Kind.CONSTRAINT);
        assertThat(((KeyConstraint) option.getConstraint()).getNullsDistinct()).isFalse();
        assertThat(((KeyConstraint) table.getTableConstraints().get(0)).getNullsDistinct())
                .isTrue();
    }

    @Test
    void testPrimaryKeyInclude() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE contacts (id integer, email text, PRIMARY KEY (id) INCLUDE (email))");
        assertThat(((KeyConstraint) table.getTableConstraints().get(0)).getIncludeColumns())
                .containsExactly("email");
        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE contacts ADD PRIMARY KEY (id) INCLUDE (email)");
        assertThat(((KeyConstraint) alter.getAlterExpressions().get(0).getConstraint())
                .getIncludeColumns())
                .containsExactly("email");
    }

    @Test
    void testLikeOptionsInOrder() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE contact_copy (LIKE contacts INCLUDING ALL EXCLUDING INDEXES,"
                        + " extra integer, LIKE history EXCLUDING ALL INCLUDING DEFAULTS INCLUDING COMPRESSION)");
        assertThat(table.getTableElements()).hasSize(3);
        LikeClause clause = table.getTableElements(LikeClause.class).get(0);
        assertThat(table.getLikeTable().getName()).isEqualTo("contacts");
        assertThat(clause.getOptions()).hasSize(2);
        assertThat(clause.isIncluding(LikeClause.OptionKind.DEFAULTS)).isTrue();
        assertThat(clause.isIncluding(LikeClause.OptionKind.INDEXES)).isFalse();
        assertThat(clause.isIncludingDefaults()).isNull();
        clause.addOption(LikeClause.OptionKind.INDEXES, true);
        assertThat(clause.isIncluding(LikeClause.OptionKind.INDEXES)).isTrue();
        assertSqlCanBeParsedAndDeparsed(table.toString());
    }

    @Test
    void testLegacyLikeSetters() {
        LikeClause clause = new LikeClause();
        clause.setIncludingDefaults(true);
        clause.setExcludingIdentity(true);
        assertThat(clause.isIncludingDefaults()).isTrue();
        assertThat(clause.isIncludingIdentity()).isFalse();
        clause.setExcludingDefaults(null);
        assertThat(clause.isIncludingDefaults()).isNull();
    }

    @Test
    void testLegacyLikeTableSetterUpdatesFirstSource() throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil
                .parse("CREATE TABLE copy (LIKE source INCLUDING ALL, id integer)");
        table.setLikeTable(new Table("replacement"), false);
        assertThat(table.getTableElements(LikeClause.class).get(0).getTable().getName())
                .isEqualTo("replacement");
        assertThat(table.getTrailingLikeTable()).isNull();
        assertSqlCanBeParsedAndDeparsed(table.toString());
        table.setLikeTable(null, false);
        assertThat(table.getLikeTable()).isNull();
        assertSqlCanBeParsedAndDeparsed(table.toString());
    }

    @Test
    void testTypedTable() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE employees OF employee_type (PRIMARY KEY (name), salary WITH OPTIONS DEFAULT 1000)");
        assertThat(table.getOfType().getDataType()).isEqualTo("employee_type");
        assertThat(table.getColumnDefinitions().get(0).isWithOptions()).isTrue();
        assertThat(table.getColumnDefinitions().get(0).getColDataType()).isNull();
        assertThat(table.getTableElements().get(0)).isInstanceOf(KeyConstraint.class);
    }

    @Test
    void testIdentityOptions() throws JSQLParserException {
        CreateTable table = (CreateTable) assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE counters (id bigint GENERATED BY DEFAULT AS IDENTITY"
                        + " (START WITH 100 INCREMENT BY -2 NO MINVALUE NO MAXVALUE NO CYCLE CACHE 5), value integer)");
        IdentityDefinition identity = table.getColumnDefinitions().get(0)
                .getColumnOptions().get(0).getIdentityDefinition();
        assertThat(identity.getGenerationMode())
                .isEqualTo(IdentityDefinition.GenerationMode.BY_DEFAULT);
        assertThat(identity.getParameters()).hasSize(6);
        assertThat(identity.getParameters().get(1).getOption())
                .isEqualTo(Sequence.ParameterType.INCREMENT_BY);
        assertThat(identity.getParameters().get(1).getValue()).isEqualTo(-2L);
    }

    @Test
    void testAlterColumnTypeUsing() throws JSQLParserException {
        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE contacts ALTER COLUMN id TYPE bigint USING (id::bigint)");
        AlterExpression.ColumnDataType column =
                alter.getAlterExpressions().get(0).getColDataTypeList().get(0);
        assertThat(column.isWithType()).isTrue();
        assertThat(column.getUsingExpression()).isNotNull();
        column.setUsingExpression(CCJSqlParserUtil.parseExpression("id + 1"));
        assertSqlCanBeParsedAndDeparsed(alter.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADD GENERATED ALWAYS AS IDENTITY",
            "ADD GENERATED BY DEFAULT AS IDENTITY (START WITH 50)",
            "SET GENERATED ALWAYS", "SET GENERATED BY DEFAULT", "SET INCREMENT BY -1",
            "SET NO CYCLE", "RESTART", "RESTART WITH 200", "DROP IDENTITY IF EXISTS",
            "SET GENERATED ALWAYS SET CACHE 10", "SET CACHE 10 RESTART WITH 50",
            "SET NO CYCLE RESTART"})
    void testIdentityAlterations(String action) throws JSQLParserException {
        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE counters ALTER COLUMN id " + action);
        assertThat(alter.getAlterExpressions().get(0).getColDataTypeList().get(0)
                .getIdentityAlterations())
                .isNotEmpty();
    }

    @Test
    void testIdentityAlterationKind() throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE counters ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY");
        IdentityAlteration action = alter.getAlterExpressions().get(0).getColDataTypeList().get(0)
                .getIdentityAlterations().get(0);
        assertThat(action.getKind()).isEqualTo(IdentityAlteration.Kind.ADD);
        assertThat(action.getIdentityDefinition().getGenerationMode())
                .isEqualTo(IdentityDefinition.GenerationMode.ALWAYS);
    }

    @Test
    void testSequenceOwnership() throws JSQLParserException {
        CreateSequence create = (CreateSequence) assertSqlCanBeParsedAndDeparsed(
                "CREATE SEQUENCE counter_seq AS bigint INCREMENT BY -1 NO MINVALUE NO MAXVALUE NO CYCLE OWNED BY counters.id");
        assertThat(create.getSequence().getOwnership().getColumn().getColumnName()).isEqualTo("id");
        AlterSequence alter = (AlterSequence) assertSqlCanBeParsedAndDeparsed(
                "ALTER SEQUENCE counter_seq OWNED BY NONE");
        assertThat(alter.getSequence().getOwnership().isNone()).isTrue();
        alter.getSequence().setOwnership(SequenceOwnership.ownedBy(new Column("app.counters.id")));
        assertSqlCanBeParsedAndDeparsed(alter.toString());
    }

    @Test
    void testSequenceParametersDoNotReusePreviousValues() throws JSQLParserException {
        AlterSequence alter = (AlterSequence) assertSqlCanBeParsedAndDeparsed(
                "ALTER SEQUENCE counter_seq INCREMENT BY 2 INCREMENT 3 START WITH 7 START 8 RESTART");
        assertThat(alter.getSequence().getParameters().get(1).getOption())
                .isEqualTo(Sequence.ParameterType.INCREMENT);
        assertThat(alter.getSequence().getParameters().get(3).getOption())
                .isEqualTo(Sequence.ParameterType.START);
        assertThat(alter.getSequence().getParameters().get(4).getValue()).isNull();
    }

    @Test
    void testProgrammaticIdentity() throws JSQLParserException {
        CreateTable table =
                (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE counters (id bigint)");
        IdentityDefinition identity =
                new IdentityDefinition(IdentityDefinition.GenerationMode.ALWAYS);
        identity.setParameters(Arrays.asList(
                new Sequence.Parameter(Sequence.ParameterType.START_WITH).withValue(50L),
                new Sequence.Parameter(Sequence.ParameterType.NO_CYCLE)));
        table.getColumnDefinitions().get(0)
                .setColumnOptions(Collections.singletonList(ColumnOption.identity(identity)));
        assertSqlCanBeParsedAndDeparsed(table.toString());
    }
}
