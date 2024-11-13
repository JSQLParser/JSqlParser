package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.List;

public class PartitionDefinition implements Serializable {
    private String partitionName;
    private String partitionOperation;
    private List<String> values;
    private String storageEngine;

    public PartitionDefinition(String partitionName, String partitionOperation,
            List<String> values, String storageEngine) {
        this.partitionName = partitionName;
        this.partitionOperation = partitionOperation;
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
}
