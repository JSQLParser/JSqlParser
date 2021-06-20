/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

public class AlterSessionTest {
    @Test
    public void testAlterSessionAdvise() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE COMMIT", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE ROLLBACK", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE NOTHING", true);
    }
    
    @Test
    public void testAlterSessionCloseDatabaseLink() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION CLOSE DATABASE LINK mylink", true);
    }
    
    @Test
    public void testAlterSessionEnable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE COMMIT IN PROCEDURE", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE GUARD", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DML", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DML PARALLEL 10", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DDL", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DDL PARALLEL 10", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL QUERY", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL QUERY PARALLEL 10", true);
    }
    
    @Test
    public void testAlterSessionDisable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE COMMIT IN PROCEDURE", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE GUARD", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL DML", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL DDL", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL QUERY", true);
    }
    
    @Test
    public void testAlterSessionForceParallel() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DML", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DML PARALLEL 10", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DDL", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DDL PARALLEL 10", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL QUERY", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL QUERY PARALLEL 10", true);
    }
    
    
    
    @Test
    public void testAlterSessionSet() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION SET ddl_lock_timeout=7200", true);
        assertSqlCanBeParsedAndDeparsed("ALTER SESSION SET ddl_lock_timeout = 7200", true);
    }
}
