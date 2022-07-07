/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.truncate;

import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TruncateTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testTruncate() throws Exception {
        String statement = "TRUncATE TABLE myschema.mytab";
        Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("myschema", truncate.getTable().getSchemaName());
        assertEquals("myschema.mytab", truncate.getTable().getFullyQualifiedName());
        assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());

        statement = "TRUncATE   TABLE    mytab";
        String toStringStatement = "TRUncATE TABLE mytab";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("mytab", truncate.getTable().getName());
        assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());

        statement = "TRUNCATE TABLE mytab CASCADE";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals(statement, truncate.toString());
    }

    @Test
    public void testTruncatePostgresqlWithoutTableName() throws Exception {
        String statement = "TRUncATE myschema.mytab";
        Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("myschema", truncate.getTable().getSchemaName());
        assertEquals("myschema.mytab", truncate.getTable().getFullyQualifiedName());
        assertEquals("TRUNCATE TABLE MYSCHEMA.MYTAB", truncate.toString().toUpperCase());

        statement = "TRUncATE       mytab";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("mytab", truncate.getTable().getName());
        assertEquals("TRUNCATE TABLE MYTAB", truncate.toString().toUpperCase());

        statement = "TRUNCATE  mytab CASCADE";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("TRUNCATE TABLE MYTAB CASCADE", truncate.toString().toUpperCase());
    }
    @Test
    public void testTruncateDeparse() throws JSQLParserException {
        String statement = "TRUNCATE TABLE foo";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new Truncate().withTable(new Table("foo")), statement);
    }

    @Test
    public void testTruncateCascadeDeparse() throws JSQLParserException {
        String statement = "TRUNCATE TABLE foo CASCADE";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new Truncate().withTable(new Table("foo")).withCascade(true), statement);
    }

}
