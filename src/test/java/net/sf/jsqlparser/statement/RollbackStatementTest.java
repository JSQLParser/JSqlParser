package net.sf.jsqlparser.statement;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

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