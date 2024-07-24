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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

public class TruncateMultipleTablesTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testTruncate2Tables() throws Exception {
        String statement = "TRUncATE TABLE myschema.mytab, myschema2.mytab2";
        Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("myschema2", truncate.getTable().getSchemaName());
        assertEquals("myschema2.mytab2", truncate.getTable().getFullyQualifiedName());
        assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());
        assertEquals("myschema.mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("myschema2.mytab2", truncate.getTables().get(1).getFullyQualifiedName());

        statement = "TRUncATE   TABLE    mytab,     my2ndtab";
        String toStringStatement = "TRUncATE TABLE mytab, my2ndtab";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("my2ndtab", truncate.getTable().getName());
        assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());
        assertEquals("mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("my2ndtab", truncate.getTables().get(1).getFullyQualifiedName());

        statement = "TRUNCATE TABLE mytab, my2ndtab CASCADE";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertNull(truncate.getTables().get(0).getSchemaName());
        assertEquals("mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("my2ndtab", truncate.getTables().get(1).getFullyQualifiedName());
        assertTrue(truncate.getCascade());
        assertEquals(statement, truncate.toString());
    }

    @Test
    public void testTruncatePostgresqlWithoutTableNames() throws Exception {
        String statement = "TRUncATE myschema.mytab, myschema2.mytab2";
        Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("myschema2", truncate.getTable().getSchemaName());
        assertEquals("myschema2.mytab2", truncate.getTable().getFullyQualifiedName());
        assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());
        assertEquals("myschema.mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("myschema2.mytab2", truncate.getTables().get(1).getFullyQualifiedName());

        statement = "TRUncATE      mytab,     my2ndtab";
        String toStringStatement = "TRUncATE mytab, my2ndtab";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("my2ndtab", truncate.getTable().getName());
        assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());
        assertEquals("mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("my2ndtab", truncate.getTables().get(1).getFullyQualifiedName());

        statement = "TRUNCATE mytab, my2ndtab CASCADE";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertNull(truncate.getTables().get(0).getSchemaName());
        assertEquals("mytab", truncate.getTables().get(0).getFullyQualifiedName());
        assertEquals("my2ndtab", truncate.getTables().get(1).getFullyQualifiedName());
        assertTrue(truncate.getCascade());
        assertEquals(statement, truncate.toString());
    }

    @Test
    public void testTruncateDeparse() throws JSQLParserException {
        String statement = "TRUNCATE TABLE foo, bar";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new Truncate()
            .withTables(List.of(new Table("foo"), new Table("bar")))
            .withTableToken(true), statement);
    }

    @Test
    public void testTruncateCascadeDeparse() throws JSQLParserException {
        String statement = "TRUNCATE TABLE foo, bar CASCADE";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new Truncate()
            .withTables(List.of(new Table("foo"), new Table("bar")))
            .withTableToken(true)
            .withCascade(true), statement);
    }

    @Test
    public void testTruncateDoesNotAllowOnlyWithMultipleTables() {
        String statement = "TRUNCATE TABLE ONLY foo, bar";
        assertThrows(JSQLParserException.class,
            () -> parserManager.parse(new StringReader(statement)));
    }

}
