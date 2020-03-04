/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import java.util.List;

public class SQLServerHints {

    private Boolean noLock;
    private String indexName;

    public SQLServerHints() {
    }

    public SQLServerHints withNoLock() {
        this.noLock = true;
        return this;
    }

    public Boolean getNoLock() {
        return noLock;
    }

    public void setNoLock(Boolean noLock) {
        this.noLock = noLock;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String toString() {
        List<String> hints = new ArrayList<>();
        if (indexName != null) {
            hints.add("INDEX (" + indexName + ")");
        }
        if (Boolean.TRUE.equals(noLock)) {
            hints.add("NOLOCK");
        }
        return " WITH ("
                + String.join(", ", hints)
                + ")";
    }
}
