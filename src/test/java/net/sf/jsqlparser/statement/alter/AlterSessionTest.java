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
import net.sf.jsqlparser.test.TestUtils;
import org.junit.Test;

public class AlterSessionTest {
    @Test
    public void testAlterSessionAdvise() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE COMMIT", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE ROLLBACK", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ADVISE NOTHING", true);
    }
    
    @Test
    public void testAlterSessionCloseDatabaseLink() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION CLOSE DATABASE LINK mylink", true);
    }
    
    @Test
    public void testAlterSessionEnable() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE COMMIT IN PROCEDURE", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE GUARD", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DML", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DML PARALLEL 10", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DDL", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL DDL PARALLEL 10", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL QUERY", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION ENABLE PARALLEL QUERY PARALLEL 10", true);
    }
    
    @Test
    public void testAlterSessionDisable() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE COMMIT IN PROCEDURE", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE GUARD", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL DML", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL DDL", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION DISABLE PARALLEL QUERY", true);
    }
    
    @Test
    public void testAlterSessionForceParallel() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DML", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DML PARALLEL 10", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DDL", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL DDL PARALLEL 10", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL QUERY", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION FORCE PARALLEL QUERY PARALLEL 10", true);
    }
    
    
    
    @Test
    public void testAlterSessionSet() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION SET ddl_lock_timeout=7200", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SESSION SET ddl_lock_timeout = 7200", true);
    }
}
