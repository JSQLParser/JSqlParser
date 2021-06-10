/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

public class DescribeTest {

    @Test
    public void testDescribe() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DESCRIBE foo.products");
    }
    
    @Test
    public void testDescribeIssue1212() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DESCRIBE file_azbs.productcategory.json");
    }
}
