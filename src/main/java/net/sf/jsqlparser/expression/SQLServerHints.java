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

public class SQLServerHints {

    private Boolean noLock;

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

    @Override
    public String toString() {
        return " WITH("
                + (noLock != null ? "nolock" : "")
                + ")";
    }
}
