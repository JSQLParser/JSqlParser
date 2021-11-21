/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class RollbackStatementTest {

    @Test
    public void testObject() {
        RollbackStatement rollbackStatement = new RollbackStatement()
                .withUsingWorkKeyword(true)
                .withUsingSavepointKeyword(true)
                .withSavepointName("mySavePoint")
                .withForceDistributedTransactionIdentifier("$ForceDistributedTransactionIdentifier");

        assertTrue(rollbackStatement.isUsingSavepointKeyword());
        assertEquals("mySavePoint", rollbackStatement.getSavepointName());
        assertEquals("$ForceDistributedTransactionIdentifier", rollbackStatement.getForceDistributedTransactionIdentifier());
    }

}
