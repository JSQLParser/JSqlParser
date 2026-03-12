/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.stream.Collectors;

import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Internal subclass for partition maintenance operations within ALTER TABLE.
 * Handles TRUNCATE, COALESCE, REORGANIZE, EXCHANGE, ANALYZE, CHECK, OPTIMIZE,
 * REBUILD, REPAIR PARTITION, PARTITION BY, and REMOVE PARTITIONING.
 */
public class AlterExpressionPartition extends AlterExpression {

    @Override
    protected void appendBody(StringBuilder b) {
        toStringPartition(b);
    }
}