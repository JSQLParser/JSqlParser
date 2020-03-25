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

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

/**
 *
 * @author tw
 */
public class CreateSchemaTest {  
    @Test
    public void testSimpleCreateSchema() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SCHEMA myschema");
    }
}
