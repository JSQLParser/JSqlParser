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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tw
 */
public class CreateSchemaTest {

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
}
