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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.PartitionBound;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Structured model for partition operations within {@code ALTER TABLE}.
 *
 * <p>
 * MySQL partition names and definitions remain available through the inherited
 * {@link #getPartitions()} and {@link #getPartitionDefinitions()} methods. This subtype adds
 * structured access to the complete {@code PARTITION BY} clause, the table used by
 * {@code EXCHANGE PARTITION}, and its validation mode. It also models PostgreSQL
 * {@code ATTACH/DETACH PARTITION} operations.
 */
public class AlterExpressionPartition extends AlterExpression {

    public enum DetachMode {
        CONCURRENTLY, FINALIZE
    }

    public enum ExchangeValidationMode {
        WITH, WITHOUT
    }

    private Table partitionTable;
    private PartitionBound partitionBound;
    private DetachMode detachMode;
    private TablePartitioning partitioning;
    private Integer coalescePartitionCount;
    private Table exchangeTable;
    private ExchangeValidationMode exchangeValidationMode;

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

    /**
     * Returns the full MySQL partitioning clause for a {@link AlterOperation#PARTITION_BY}
     * operation.
     */
    public TablePartitioning getPartitioning() {
        return partitioning;
    }

    public void setPartitioning(TablePartitioning partitioning) {
        this.partitioning = partitioning;
        if (partitioning != null) {
            setPartitionType(partitioning.getType() != null
                    ? partitioning.getType().toString()
                    : null);
            setPartitionExpression(partitioning.getExpression() != null
                    ? partitioning.getExpression()
                    : partitioning.getExpressionList());
            if (partitioning.getColumns() != null) {
                List<String> partitionColumns = new ArrayList<>();
                partitioning.getColumns().forEach(
                        column -> partitionColumns.add(column.getFullyQualifiedName()));
                setPartitionColumns(partitionColumns);
            } else {
                setPartitionColumns(null);
            }
            setPartitionDefinitions(partitioning.getPartitionDefinitions());
        }
    }

    /**
     * Convenience alias that names the inherited MySQL partition-name list explicitly.
     */
    public List<String> getPartitionNames() {
        return getPartitions();
    }

    public void setPartitionNames(List<String> partitionNames) {
        setPartitions(partitionNames);
    }

    /**
     * Returns whether the operation targets every partition rather than named partitions.
     */
    public boolean isAllPartitions() {
        List<String> partitionNames = getPartitionNames();
        return partitionNames != null && partitionNames.size() == 1
                && "ALL".equalsIgnoreCase(partitionNames.get(0));
    }

    public void setAllPartitions(boolean allPartitions) {
        if (allPartitions) {
            setPartitionNames(Collections.singletonList("ALL"));
        } else if (isAllPartitions()) {
            setPartitionNames(null);
        }
    }

    /**
     * Returns the number of partitions added by a MySQL {@code COALESCE PARTITION} operation.
     * Unlike the legacy primitive accessor, this is {@code null} when no count was supplied.
     */
    public Integer getCoalescePartitionCount() {
        return coalescePartitionCount;
    }

    public void setCoalescePartitionCount(Integer coalescePartitionCount) {
        this.coalescePartitionCount = coalescePartitionCount;
        if (coalescePartitionCount != null) {
            super.setCoalescePartitionNumber(coalescePartitionCount);
        }
    }

    @Override
    public void setCoalescePartitionNumber(int coalescePartitionNumber) {
        super.setCoalescePartitionNumber(coalescePartitionNumber);
        this.coalescePartitionCount = coalescePartitionNumber;
    }

    public Table getExchangeTable() {
        return exchangeTable;
    }

    public void setExchangeTable(Table exchangeTable) {
        this.exchangeTable = exchangeTable;
        super.setExchangePartitionTableName(
                exchangeTable != null ? exchangeTable.getFullyQualifiedName() : null);
    }

    @Override
    public void setExchangePartitionTableName(String exchangePartitionTableName) {
        super.setExchangePartitionTableName(exchangePartitionTableName);
        this.exchangeTable = exchangePartitionTableName != null
                ? new Table(exchangePartitionTableName)
                : null;
    }

    public ExchangeValidationMode getExchangeValidationMode() {
        return exchangeValidationMode;
    }

    public void setExchangeValidationMode(ExchangeValidationMode exchangeValidationMode) {
        this.exchangeValidationMode = exchangeValidationMode;
        super.setExchangePartitionWithValidation(
                exchangeValidationMode == ExchangeValidationMode.WITH);
        super.setExchangePartitionWithoutValidation(
                exchangeValidationMode == ExchangeValidationMode.WITHOUT);
    }

    @Override
    public void setExchangePartitionWithValidation(boolean exchangePartitionWithValidation) {
        super.setExchangePartitionWithValidation(exchangePartitionWithValidation);
        if (exchangePartitionWithValidation) {
            exchangeValidationMode = ExchangeValidationMode.WITH;
            super.setExchangePartitionWithoutValidation(false);
        } else if (exchangeValidationMode == ExchangeValidationMode.WITH) {
            exchangeValidationMode = null;
        }
    }

    @Override
    public void setExchangePartitionWithoutValidation(boolean exchangePartitionWithoutValidation) {
        super.setExchangePartitionWithoutValidation(exchangePartitionWithoutValidation);
        if (exchangePartitionWithoutValidation) {
            exchangeValidationMode = ExchangeValidationMode.WITHOUT;
            super.setExchangePartitionWithValidation(false);
        } else if (exchangeValidationMode == ExchangeValidationMode.WITHOUT) {
            exchangeValidationMode = null;
        }
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
    public AlterExpressionPartition withOperation(AlterOperation operation) {
        setOperation(operation);
        return this;
    }

    public AlterExpressionPartition withPartitioning(TablePartitioning partitioning) {
        setPartitioning(partitioning);
        return this;
    }

    public AlterExpressionPartition withPartitionNames(List<String> partitionNames) {
        setPartitionNames(partitionNames);
        return this;
    }

    public AlterExpressionPartition withAllPartitions(boolean allPartitions) {
        setAllPartitions(allPartitions);
        return this;
    }

    public AlterExpressionPartition withCoalescePartitionCount(Integer coalescePartitionCount) {
        setCoalescePartitionCount(coalescePartitionCount);
        return this;
    }

    public AlterExpressionPartition withExchangeTable(Table exchangeTable) {
        setExchangeTable(exchangeTable);
        return this;
    }

    public AlterExpressionPartition withExchangeValidationMode(
            ExchangeValidationMode exchangeValidationMode) {
        setExchangeValidationMode(exchangeValidationMode);
        return this;
    }

    public AlterExpressionPartition withPartitionDefinitions(
            List<PartitionDefinition> partitionDefinitions) {
        setPartitionDefinitions(partitionDefinitions);
        return this;
    }

    public AlterExpressionPartition addPartitionNames(String... partitionNames) {
        List<String> collection =
                Optional.ofNullable(getPartitionNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, partitionNames);
        return withPartitionNames(collection);
    }

    public AlterExpressionPartition addPartitionNames(Collection<String> partitionNames) {
        List<String> collection =
                Optional.ofNullable(getPartitionNames()).orElseGet(ArrayList::new);
        collection.addAll(partitionNames);
        return withPartitionNames(collection);
    }

    public AlterExpressionPartition addPartitionDefinitions(
            PartitionDefinition... partitionDefinitions) {
        List<PartitionDefinition> collection = Optional.ofNullable(getPartitionDefinitions())
                .orElseGet(ArrayList::new);
        Collections.addAll(collection, partitionDefinitions);
        return withPartitionDefinitions(collection);
    }

    public AlterExpressionPartition addPartitionDefinitions(
            Collection<? extends PartitionDefinition> partitionDefinitions) {
        List<PartitionDefinition> collection = Optional.ofNullable(getPartitionDefinitions())
                .orElseGet(ArrayList::new);
        collection.addAll(partitionDefinitions);
        return withPartitionDefinitions(collection);
    }

    @Override
    protected void appendBody(StringBuilder b) {
        switch (getOperation()) {
            case ADD_PARTITION:
                b.append("ADD PARTITION ")
                        .append(PlainSelect.getStringList(getPartitionDefinitions(), true, true));
                break;
            case DROP_PARTITION:
                b.append("DROP PARTITION ")
                        .append(PlainSelect.getStringList(getPartitionNames()));
                break;
            case ATTACH_PARTITION:
                b.append("ATTACH PARTITION ").append(partitionTable).append(" ")
                        .append(partitionBound);
                break;
            case DETACH_PARTITION:
                b.append("DETACH PARTITION ").append(partitionTable);
                if (detachMode != null) {
                    b.append(" ").append(detachMode);
                }
                break;
            case PARTITION_BY:
                if (partitioning != null) {
                    b.append(partitioning);
                } else {
                    toStringPartition(b);
                }
                break;
            case EXCHANGE_PARTITION:
                if (exchangeTable != null) {
                    b.append("EXCHANGE PARTITION ").append(getPartitionNames().get(0))
                            .append(" WITH TABLE ").append(exchangeTable);
                    if (exchangeValidationMode != null) {
                        b.append(" ").append(exchangeValidationMode).append(" VALIDATION");
                    }
                } else {
                    toStringPartition(b);
                }
                break;
            default:
                toStringPartition(b);
                break;
        }
    }
}
