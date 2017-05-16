package net.sf.jsqlparser.test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import junit.framework.TestCase;

public class CommitTest extends TestCase {
    public CommitTest(String name) {
        super(name);
    }

    public void testCommit() throws Exception {
        assertSqlCanBeParsedAndDeparsed("COMMIT");        
    }
}
