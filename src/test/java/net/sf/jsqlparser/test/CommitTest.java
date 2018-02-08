package net.sf.jsqlparser.test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import org.junit.Test;

public class CommitTest {

    @Test
    public void testCommit() throws Exception {
        assertSqlCanBeParsedAndDeparsed("COMMIT");
    }
}
