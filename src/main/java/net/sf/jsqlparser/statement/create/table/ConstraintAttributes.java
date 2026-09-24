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

/** Shared PostgreSQL constraint attributes; null values preserve omitted clauses. */
public class ConstraintAttributes implements Serializable {
    public enum Initially {
        IMMEDIATE, DEFERRED
    }

    private Boolean deferrable;
    private Initially initially;
    private boolean notValid;
    private Boolean enforced;

    public Boolean getDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }

    public Initially getInitially() {
        return initially;
    }

    public void setInitially(Initially initially) {
        this.initially = initially;
    }

    public boolean isNotValid() {
        return notValid;
    }

    public void setNotValid(boolean notValid) {
        this.notValid = notValid;
    }

    /** Null preserves an omitted ENFORCED clause. */
    public Boolean getEnforced() {
        return enforced;
    }

    public void setEnforced(Boolean enforced) {
        this.enforced = enforced;
    }

    public void appendTo(StringBuilder sql) {
        if (deferrable != null) {
            sql.append(deferrable ? " DEFERRABLE" : " NOT DEFERRABLE");
        }
        if (initially != null) {
            sql.append(" INITIALLY ").append(initially);
        }
        if (enforced != null) {
            sql.append(enforced ? " ENFORCED" : " NOT ENFORCED");
        }
        if (notValid) {
            sql.append(" NOT VALID");
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql);
        return sql.toString().trim();
    }
}
