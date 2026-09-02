/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.List;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class PartitionDefinition implements Serializable {

    public enum ValueOperator {
        LESS_THAN("VALUES LESS THAN"), IN("VALUES IN");

        private final String sql;

        ValueOperator(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return sql;
        }
    }

    private String partitionName;
    private String partitionOperation;
    private List<String> values;
    private String storageEngine;
    private ValueOperator valueOperator;
    private boolean maxValue;
    private boolean subPartition;
    private boolean storageKeyword;
    private boolean storageEngineUseEquals = true;
    private String comment;
    private boolean commentUseEquals;
    private String dataDirectory;
    private boolean dataDirectoryUseEquals;
    private String indexDirectory;
    private boolean indexDirectoryUseEquals;
    private String maxRows;
    private boolean maxRowsUseEquals;
    private String minRows;
    private boolean minRowsUseEquals;
    private String tablespace;
    private boolean tablespaceUseEquals;
    private List<PartitionDefinition> subPartitionDefinitions;

    public PartitionDefinition() {}

    public PartitionDefinition(String partitionName, String partitionOperation,
            List<String> values, String storageEngine) {
        this.partitionName = partitionName;
        setPartitionOperation(partitionOperation);
        this.values = values;
        this.storageEngine = storageEngine;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getPartitionOperation() {
        return partitionOperation;
    }

    public void setPartitionOperation(String partitionOperation) {
        this.partitionOperation = partitionOperation;
        if (ValueOperator.LESS_THAN.toString().equalsIgnoreCase(partitionOperation)) {
            valueOperator = ValueOperator.LESS_THAN;
        } else if (ValueOperator.IN.toString().equalsIgnoreCase(partitionOperation)) {
            valueOperator = ValueOperator.IN;
        } else {
            valueOperator = null;
        }
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getStorageEngine() {
        return storageEngine;
    }

    public void setStorageEngine(String storageEngine) {
        this.storageEngine = storageEngine;
    }

    public ValueOperator getValueOperator() {
        return valueOperator;
    }

    public void setValueOperator(ValueOperator valueOperator) {
        this.valueOperator = valueOperator;
        this.partitionOperation = valueOperator != null ? valueOperator.toString() : null;
    }

    public boolean isMaxValue() {
        return maxValue;
    }

    public void setMaxValue(boolean maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isSubPartition() {
        return subPartition;
    }

    public void setSubPartition(boolean subPartition) {
        this.subPartition = subPartition;
    }

    public boolean isStorageKeyword() {
        return storageKeyword;
    }

    public void setStorageKeyword(boolean storageKeyword) {
        this.storageKeyword = storageKeyword;
    }

    public boolean isStorageEngineUseEquals() {
        return storageEngineUseEquals;
    }

    public void setStorageEngineUseEquals(boolean storageEngineUseEquals) {
        this.storageEngineUseEquals = storageEngineUseEquals;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCommentUseEquals() {
        return commentUseEquals;
    }

    public void setCommentUseEquals(boolean commentUseEquals) {
        this.commentUseEquals = commentUseEquals;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public boolean isDataDirectoryUseEquals() {
        return dataDirectoryUseEquals;
    }

    public void setDataDirectoryUseEquals(boolean dataDirectoryUseEquals) {
        this.dataDirectoryUseEquals = dataDirectoryUseEquals;
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    public void setIndexDirectory(String indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public boolean isIndexDirectoryUseEquals() {
        return indexDirectoryUseEquals;
    }

    public void setIndexDirectoryUseEquals(boolean indexDirectoryUseEquals) {
        this.indexDirectoryUseEquals = indexDirectoryUseEquals;
    }

    public String getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(String maxRows) {
        this.maxRows = maxRows;
    }

    public boolean isMaxRowsUseEquals() {
        return maxRowsUseEquals;
    }

    public void setMaxRowsUseEquals(boolean maxRowsUseEquals) {
        this.maxRowsUseEquals = maxRowsUseEquals;
    }

    public String getMinRows() {
        return minRows;
    }

    public void setMinRows(String minRows) {
        this.minRows = minRows;
    }

    public boolean isMinRowsUseEquals() {
        return minRowsUseEquals;
    }

    public void setMinRowsUseEquals(boolean minRowsUseEquals) {
        this.minRowsUseEquals = minRowsUseEquals;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public boolean isTablespaceUseEquals() {
        return tablespaceUseEquals;
    }

    public void setTablespaceUseEquals(boolean tablespaceUseEquals) {
        this.tablespaceUseEquals = tablespaceUseEquals;
    }

    public List<PartitionDefinition> getSubPartitionDefinitions() {
        return subPartitionDefinitions;
    }

    public void setSubPartitionDefinitions(
            List<PartitionDefinition> subPartitionDefinitions) {
        this.subPartitionDefinitions = subPartitionDefinitions;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(subPartition ? "SUBPARTITION " : "PARTITION ").append(partitionName);
        if (partitionOperation != null) {
            b.append(" ").append(partitionOperation);
            if (maxValue) {
                b.append(" MAXVALUE");
            } else {
                b.append(" (").append(PlainSelect.getStringList(values)).append(")");
            }
        }
        if (storageEngine != null) {
            if (storageKeyword) {
                b.append(" STORAGE");
            }
            appendOption(b, "ENGINE", storageEngine, storageEngineUseEquals);
        }
        appendOption(b, "COMMENT", comment, commentUseEquals);
        appendOption(b, "DATA DIRECTORY", dataDirectory, dataDirectoryUseEquals);
        appendOption(b, "INDEX DIRECTORY", indexDirectory, indexDirectoryUseEquals);
        appendOption(b, "MAX_ROWS", maxRows, maxRowsUseEquals);
        appendOption(b, "MIN_ROWS", minRows, minRowsUseEquals);
        appendOption(b, "TABLESPACE", tablespace, tablespaceUseEquals);
        if (subPartitionDefinitions != null && !subPartitionDefinitions.isEmpty()) {
            b.append(" ").append(PlainSelect.getStringList(subPartitionDefinitions, true, true));
        }
        return b.toString();
    }

    private static void appendOption(StringBuilder builder, String name, String value,
            boolean useEquals) {
        if (value != null) {
            builder.append(" ").append(name).append(useEquals ? " = " : " ").append(value);
        }
    }
}
