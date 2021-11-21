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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.jupiter.api.Test;

public class SavepointRollbackCommitTest {
    @Test
    public void testSavepoint() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SAVEPOINT banda_sal", true);        
    }
    
    @Test
    public void testRollback() throws Exception {
        assertSqlCanBeParsedAndDeparsed("ROLLBACK", true);
        assertSqlCanBeParsedAndDeparsed("ROLLBACK WORK", true);
        assertSqlCanBeParsedAndDeparsed("ROLLBACK TO banda_sal", true);  
        assertSqlCanBeParsedAndDeparsed("ROLLBACK TO SAVEPOINT banda_sal", true);  
        assertSqlCanBeParsedAndDeparsed("ROLLBACK WORK TO banda_sal", true); 
        assertSqlCanBeParsedAndDeparsed("ROLLBACK WORK TO SAVEPOINT banda_sal", true); 
        assertSqlCanBeParsedAndDeparsed("ROLLBACK FORCE '25.32.87'", true);  
        assertSqlCanBeParsedAndDeparsed("ROLLBACK WORK FORCE '25.32.87'", true);  
    }
    
    
    @Test
    public void testCommit() throws Exception {
        assertSqlCanBeParsedAndDeparsed("COMMIT");        
    }
}
