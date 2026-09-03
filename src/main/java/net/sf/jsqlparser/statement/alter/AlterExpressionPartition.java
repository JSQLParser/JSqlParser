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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.PartitionBound;

/**
 * Internal subclass for partition maintenance operations within ALTER TABLE. Handles TRUNCATE,
 * COALESCE, REORGANIZE, EXCHANGE, ANALYZE, CHECK, OPTIMIZE, REBUILD, REPAIR PARTITION, PARTITION
 * BY, and REMOVE PARTITIONING.
 */
public class AlterExpressionPartition extends AlterExpression {

    public enum DetachMode {
        CONCURRENTLY, FINALIZE
    }

    private Table partitionTable;
    private PartitionBound partitionBound;
    private DetachMode detachMode;

    public Table getPartitionTable() {
        return partitionTable;
    }

    public void setPartitionTable(Table partitionTable) {
        this.partitionTable = partitionTable;
    }

    public PartitionBound getPartitionBound() {
        return partitionBound;
    }

    public void setPartitionBound(PartitionBound partitionBound) {
        this.partitionBound = partitionBound;
    }

    public DetachMode getDetachMode() {
        return detachMode;
    }

    public void setDetachMode(DetachMode detachMode) {
        this.detachMode = detachMode;
    }

    public AlterExpressionPartition withPartitionTable(Table partitionTable) {
        setPartitionTable(partitionTable);
        return this;
    }

    public AlterExpressionPartition withPartitionBound(PartitionBound partitionBound) {
        setPartitionBound(partitionBound);
        return this;
    }

    public AlterExpressionPartition withDetachMode(DetachMode detachMode) {
        setDetachMode(detachMode);
        return this;
    }

    @Override
    protected void appendBody(StringBuilder b) {
        if (getOperation() == AlterOperation.ATTACH_PARTITION) {
            b.append("ATTACH PARTITION ").append(partitionTable).append(" ")
                    .append(partitionBound);
        } else if (getOperation() == AlterOperation.DETACH_PARTITION) {
            b.append("DETACH PARTITION ").append(partitionTable);
            if (detachMode != null) {
                b.append(" ").append(detachMode);
            }
        } else {
            toStringPartition(b);
        }
    }
}
