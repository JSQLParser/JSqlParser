/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import org.junit.Test;

public class CommitTest {
    @Test
    public void testCommit() throws Exception {
        assertSqlCanBeParsedAndDeparsed("COMMIT");
    }
}
