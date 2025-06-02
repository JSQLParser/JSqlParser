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

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("PARTITION ").append(partitionName)
                .append(" ").append(partitionOperation)
                .append(" (").append(PlainSelect.getStringList(values))
                .append(")");
        if (storageEngine != null) {
            b.append(" ENGINE = ").append(storageEngine);
        }
        return b.toString();
    }
}
