/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.schema;

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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author tw
 */
public class CreateSchemaTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static CreateSchema roundTrip(String sql) throws JSQLParserException {
        CreateSchema schema = assertInstanceOf(CreateSchema.class,
                assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL));
        assertEquals(sql, CCJSqlParserUtil.parse(schema.toString(), POSTGRESQL).toString());
        return schema;
    }

    @Test
    public void testSimpleCreateSchema() throws JSQLParserException {
        String statement = "CREATE SCHEMA myschema";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateSchema().withSchemaName("myschema"), statement);
    }

    @Test
    public void testCreateSchemaWithcatalog() throws JSQLParserException {
        String statement = "CREATE SCHEMA unnamed.myschema";
        assertSqlCanBeParsedAndDeparsed(statement);

        statement = "CREATE SCHEMA unnamed.session1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testSimpleCreateWithAuth() throws JSQLParserException {
        String statement = "CREATE SCHEMA myschema AUTHORIZATION myauth";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateSchema().withSchemaName("myschema").withAuthorization("myauth"),
                statement);
    }

    @Test
    void testIfNotExistsIssue2061() throws JSQLParserException {
        String sqlStr = "CREATE SCHEMA IF NOT EXISTS sales_kpi";
        assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @Test
    void testMySqlCreateSchemaOptions() throws JSQLParserException {
        String sql = "CREATE SCHEMA IF NOT EXISTS schema_4 DEFAULT CHARACTER SET = utf8 "
                + "COLLATE = utf8_general_ci DEFAULT ENCRYPTION = 'N'";
        CreateSchema schema = (CreateSchema) assertSqlCanBeParsedAndDeparsed(sql);

        DatabaseOption characterSet = schema
                .getDatabaseOption(DatabaseOption.Kind.CHARACTER_SET).orElseThrow();
        assertEquals("utf8", characterSet.getValue());
        assertTrue(characterSet.isUseDefault());
        assertTrue(characterSet.isUseEquals());

        DatabaseOption collate =
                schema.getDatabaseOption(DatabaseOption.Kind.COLLATE).orElseThrow();
        assertEquals("utf8_general_ci", collate.getValue());

        DatabaseOption encryption =
                schema.getDatabaseOption(DatabaseOption.Kind.ENCRYPTION).orElseThrow();
        assertEquals("'N'", encryption.getValue());

        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
    }

    @Test
    void testMySqlCreateSchemaCharset() throws JSQLParserException {
        String sql = "CREATE SCHEMA db1 DEFAULT CHARSET = utf8mb4 DEFAULT ENCRYPTION = 'N'";
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, null}) {
            CreateSchema schema = (CreateSchema) assertSqlCanBeParsedAndDeparsed(sql, false,
                    dialect == null ? parser -> {
                    } : parser -> parser.withDialect(dialect));
            DatabaseOption charset =
                    schema.getDatabaseOption(DatabaseOption.Kind.CHARACTER_SET).orElseThrow();
            assertEquals("CHARSET", charset.getName());
            assertEquals("utf8mb4", charset.getValue());
            assertTrue(charset.isUseDefault());
        }
        assertSqlCanBeParsedAndDeparsed("CREATE SCHEMA db1 CHAR SET utf8mb4");
    }

    @Test
    void keepsSchemaElementsAsWritten() throws JSQLParserException {
        CreateSchema schema = roundTrip("CREATE SCHEMA hollywood "
                + "CREATE TABLE films (title text, release date, awards text[]) "
                + "CREATE VIEW winners AS SELECT title, release FROM films WHERE awards IS NOT NULL");

        assertEquals("hollywood", schema.getSchemaName());
        assertEquals(2, schema.getStatements().size());
        CreateTable films = assertInstanceOf(CreateTable.class, schema.getStatements().get(0));
        assertEquals("films", films.getTable().getFullyQualifiedName());
        assertNull(films.getTable().getSchemaName());
        CreateView winners = assertInstanceOf(CreateView.class, schema.getStatements().get(1));
        assertEquals("winners", winners.getView().getFullyQualifiedName());
        assertThat(new TablesNamesFinder().getTables(schema)).containsExactlyInAnyOrder("films",
                "winners");
    }

    @Test
    void acceptsEveryPostgreSqlSchemaElement() throws JSQLParserException {
        CreateSchema schema = roundTrip("CREATE SCHEMA app AUTHORIZATION app_owner "
                + "CREATE TABLE t (id int) "
                + "CREATE VIEW v AS SELECT id FROM t "
                + "GRANT SELECT ON v TO PUBLIC "
                + "CREATE UNIQUE INDEX i ON t (id) "
                + "CREATE SEQUENCE s START WITH 10 "
                + "CREATE TRIGGER trg BEFORE INSERT ON t FOR EACH ROW EXECUTE FUNCTION f() "
                + "GRANT USAGE ON SEQUENCE s TO app_user");

        assertEquals("app_owner", schema.getAuthorization());
        assertEquals(List.of(CreateTable.class, CreateView.class, Grant.class, CreateIndex.class,
                CreateSequence.class, CreateTrigger.class, Grant.class),
                schema.getStatements().stream().map(Object::getClass)
                        .collect(java.util.stream.Collectors.toList()));
        assertThat(new TablesNamesFinder().getTables(schema)).contains("t", "v");
        assertThat(schema.getFeatures().getCertain()).containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertTrue(new Validation(List.of(PostgresqlVersion.V10), schema.toString()).validate()
                .isEmpty());
        assertThat(new Validation(List.of(new FeaturesAllowed(Feature.createSchema)),
                "CREATE SCHEMA app CREATE TABLE t (id int)").validate()).isNotEmpty();
    }

    @Test
    void schemaElementsWithoutAName() throws JSQLParserException {
        CreateSchema schema = roundTrip("CREATE SCHEMA AUTHORIZATION joe CREATE TABLE t (a int)");
        assertNull(schema.getSchemaName());
        assertEquals("t", ((CreateTable) schema.getStatements().get(0)).getTable().getName());
    }

    @Test
    void grantRemainsAnAliasOutsideSchemaElements() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t grant");
        assertEquals(2, CCJSqlParserUtil
                .parseStatements("CREATE SCHEMA s;\nCREATE TABLE t (a int);").size());
    }

    @Test
    void rendersSchemaPathAndElements() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SCHEMA s PATH a, b");
        CreateSchema schema = new CreateSchema().withSchemaName("s");
        schema.addStatement(CCJSqlParserUtil.parse("CREATE TABLE t (a int)"));
        assertDeparse(schema, "CREATE SCHEMA s CREATE TABLE t (a int)");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE SCHEMA IF NOT EXISTS s CREATE TABLE t (a int)",
            "CREATE SCHEMA s CREATE MATERIALIZED VIEW v AS SELECT 1",
            "CREATE SCHEMA s CREATE FUNCTION f() RETURNS int AS 'SELECT 1' LANGUAGE sql",
            "CREATE SCHEMA s CREATE SCHEMA t",
            "CREATE SCHEMA s VIEW v AS SELECT 1",
            "CREATE SCHEMA s CREATE TABLE t (a int) REVOKE SELECT ON t FROM u"})
    void rejectsOtherSchemaElements(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }
}
