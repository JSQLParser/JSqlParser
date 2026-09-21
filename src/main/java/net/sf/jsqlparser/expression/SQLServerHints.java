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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SQLServerHints implements Serializable {

    private Boolean noLock;
    private String indexName;
    private final List<LockHint> lockHints = new ArrayList<>();

    /** Additional argument-free locking hints; NOLOCK retains its existing accessors. */
    public enum LockHint {
        HOLDLOCK, NOWAIT, PAGLOCK, READCOMMITTED, READCOMMITTEDLOCK, READPAST, READUNCOMMITTED, REPEATABLEREAD, ROWLOCK, SERIALIZABLE, SNAPSHOT, TABLOCK, TABLOCKX, UPDLOCK, XLOCK
    }

    /** Returns the mutable list of locking hints, in their original order. */
    public List<LockHint> getLockHints() {
        return lockHints;
    }


    public SQLServerHints() {}

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
        for (LockHint hint : lockHints) {
            hints.add(hint.name());
        }
        return " WITH ("
                + String.join(", ", hints)
                + ")";
    }

    public SQLServerHints withNoLock(Boolean noLock) {
        this.setNoLock(noLock);
        return this;
    }

    public SQLServerHints withIndexName(String indexName) {
        this.setIndexName(indexName);
        return this;
    }
}
