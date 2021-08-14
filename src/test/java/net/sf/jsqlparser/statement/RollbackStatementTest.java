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

import org.junit.Assert;
import org.junit.Test;

public class RollbackStatementTest {

    @Test
    public void testObject() {
        RollbackStatement rollbackStatement = new RollbackStatement()
                .withUsingWorkKeyword(true)
                .withUsingSavepointKeyword(true)
                .withSavepointName("mySavePoint")
                .withForceDistributedTransactionIdentifier("$ForceDistributedTransactionIdentifier");

        Assert.assertTrue(rollbackStatement.isUsingSavepointKeyword());
        Assert.assertEquals("mySavePoint", rollbackStatement.getSavepointName());
        Assert.assertEquals("$ForceDistributedTransactionIdentifier", rollbackStatement.getForceDistributedTransactionIdentifier());
    }

}
