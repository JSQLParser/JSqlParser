/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * MySQL table partitioning options used by {@code CREATE TABLE}.
 */
public class TablePartitioning implements Serializable {

    public enum Type {
        HASH, KEY, RANGE, LIST
    }

    private Type type;
    private boolean linear;
    private boolean columnsSyntax;
    private Expression expression;
    private ExpressionList<Column> columns;
    private Integer algorithm;
    private boolean algorithmUseEquals;
    private Long partitions;
    private TablePartitioning subPartitioning;
    private List<PartitionDefinition> partitionDefinitions;
    private List<String> partitionOptions;

    public TablePartitioning() {}

    public TablePartitioning(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isLinear() {
        return linear;
    }

    public void setLinear(boolean linear) {
        this.linear = linear;
    }

    public boolean isColumnsSyntax() {
        return columnsSyntax;
    }

    public void setColumnsSyntax(boolean columnsSyntax) {
        this.columnsSyntax = columnsSyntax;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    public Integer getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Integer algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isAlgorithmUseEquals() {
        return algorithmUseEquals;
    }

    public void setAlgorithmUseEquals(boolean algorithmUseEquals) {
        this.algorithmUseEquals = algorithmUseEquals;
    }

    public Long getPartitions() {
        return partitions;
    }

    public void setPartitions(Long partitions) {
        this.partitions = partitions;
    }

    public TablePartitioning getSubPartitioning() {
        return subPartitioning;
    }

    public void setSubPartitioning(TablePartitioning subPartitioning) {
        this.subPartitioning = subPartitioning;
    }

    public List<PartitionDefinition> getPartitionDefinitions() {
        return partitionDefinitions;
    }

    public void setPartitionDefinitions(List<PartitionDefinition> partitionDefinitions) {
        this.partitionDefinitions = partitionDefinitions;
    }

    public List<String> getPartitionOptions() {
        return partitionOptions;
    }

    public void setPartitionOptions(List<String> partitionOptions) {
        this.partitionOptions = partitionOptions;
    }

    public TablePartitioning withType(Type type) {
        setType(type);
        return this;
    }

    public TablePartitioning withLinear(boolean linear) {
        setLinear(linear);
        return this;
    }

    public TablePartitioning withColumnsSyntax(boolean columnsSyntax) {
        setColumnsSyntax(columnsSyntax);
        return this;
    }

    public TablePartitioning withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public TablePartitioning withColumns(ExpressionList<Column> columns) {
        setColumns(columns);
        return this;
    }

    public TablePartitioning withAlgorithm(Integer algorithm) {
        setAlgorithm(algorithm);
        return this;
    }

    public TablePartitioning withAlgorithmUseEquals(boolean algorithmUseEquals) {
        setAlgorithmUseEquals(algorithmUseEquals);
        return this;
    }

    public TablePartitioning withPartitions(Long partitions) {
        setPartitions(partitions);
        return this;
    }

    public TablePartitioning withSubPartitioning(TablePartitioning subPartitioning) {
        setSubPartitioning(subPartitioning);
        return this;
    }

    public TablePartitioning withPartitionDefinitions(
            List<PartitionDefinition> partitionDefinitions) {
        setPartitionDefinitions(partitionDefinitions);
        return this;
    }

    public TablePartitioning withPartitionOptions(List<String> partitionOptions) {
        setPartitionOptions(partitionOptions);
        return this;
    }

    public TablePartitioning addColumns(Column... columns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getColumns()).orElseGet(ExpressionList::new);
        Collections.addAll(collection, columns);
        return withColumns(collection);
    }

    public TablePartitioning addColumns(Collection<? extends Column> columns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getColumns()).orElseGet(ExpressionList::new);
        collection.addAll(columns);
        return withColumns(collection);
    }

    public TablePartitioning addPartitionDefinitions(
            PartitionDefinition... partitionDefinitions) {
        List<PartitionDefinition> collection = Optional.ofNullable(getPartitionDefinitions())
                .orElseGet(ArrayList::new);
        Collections.addAll(collection, partitionDefinitions);
        return withPartitionDefinitions(collection);
    }

    public TablePartitioning addPartitionDefinitions(
            Collection<? extends PartitionDefinition> partitionDefinitions) {
        List<PartitionDefinition> collection = Optional.ofNullable(getPartitionDefinitions())
                .orElseGet(ArrayList::new);
        collection.addAll(partitionDefinitions);
        return withPartitionDefinitions(collection);
    }

    public TablePartitioning addPartitionOptions(String... partitionOptions) {
        List<String> collection =
                Optional.ofNullable(getPartitionOptions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, partitionOptions);
        return withPartitionOptions(collection);
    }

    public TablePartitioning addPartitionOptions(Collection<String> partitionOptions) {
        List<String> collection =
                Optional.ofNullable(getPartitionOptions()).orElseGet(ArrayList::new);
        collection.addAll(partitionOptions);
        return withPartitionOptions(collection);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("PARTITION BY ");
        appendMethod(builder);
        if (partitions != null) {
            builder.append(" PARTITIONS ").append(partitions);
        }
        if (subPartitioning != null) {
            builder.append(" SUBPARTITION BY ");
            subPartitioning.appendMethod(builder);
            if (subPartitioning.getPartitions() != null) {
                builder.append(" SUBPARTITIONS ").append(subPartitioning.getPartitions());
            }
        }
        if (partitionDefinitions != null && !partitionDefinitions.isEmpty()) {
            builder.append(" ")
                    .append(PlainSelect.getStringList(partitionDefinitions, true, true));
        }
        if (partitionOptions != null && !partitionOptions.isEmpty()) {
            builder.append(" ").append(PlainSelect.getStringList(partitionOptions, false, false));
        }
        return builder.toString();
    }

    private void appendMethod(StringBuilder builder) {
        if (linear) {
            builder.append("LINEAR ");
        }
        builder.append(type);
        if (algorithm != null) {
            builder.append(" ALGORITHM");
            builder.append(algorithmUseEquals ? " = " : " ").append(algorithm);
        }
        if (columnsSyntax) {
            builder.append(" COLUMNS");
        }
        if (expression != null) {
            builder.append(" (").append(expression).append(")");
        } else if (columns != null) {
            builder.append(" ").append(PlainSelect.getStringList(columns, true, true));
        }
    }
}
