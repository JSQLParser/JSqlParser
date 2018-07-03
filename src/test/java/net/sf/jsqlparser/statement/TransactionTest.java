package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import org.junit.Test;

public class TransactionTest {

    @Test
    public void testCommit() throws Exception {
        assertSqlCanBeParsedAndDeparsed("COMMIT");
        assertSqlCanBeParsedAndDeparsed("COMMIT WORK"); // teradata
        assertSqlCanBeParsedAndDeparsed("COMMIT WORK RELEASE"); // teradata
        assertSqlCanBeParsedAndDeparsed("COMMIT TRAN M2"); // SQL Server
        assertSqlCanBeParsedAndDeparsed("COMMIT TRANSACTION @TranName"); // SQL Server
    }

    @Test
    public void testBeginTransaction() throws Exception {
        assertSqlCanBeParsedAndDeparsed("BT"); // Teradata 
        assertSqlCanBeParsedAndDeparsed("BEGIN TRANSACTION"); // Teradata
        assertSqlCanBeParsedAndDeparsed("BEGIN TRAN"); // SQL Server
        assertSqlCanBeParsedAndDeparsed("BEGIN TRAN M2 WITH MARK"); // SQL Server
        assertSqlCanBeParsedAndDeparsed("BEGIN TRANSACTION @TranName"); // SQL Server

    }

    @Test
    public void testEndTransaction() throws Exception {
        assertSqlCanBeParsedAndDeparsed("ET"); // Teradata
        assertSqlCanBeParsedAndDeparsed("END TRANSACTION"); // Teradata

    }

    @Test
    public void testAbortTransaction() throws Exception {
        assertSqlCanBeParsedAndDeparsed("ABORT"); // Teradata .. still TODO ...    
    }
}
