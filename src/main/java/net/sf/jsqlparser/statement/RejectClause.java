/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.LongValue;

import java.io.Serializable;

public class RejectClause implements Serializable {
    private LongValue limit;
    private boolean errors;

    public LongValue getLimit() {
        return limit;
    }

    public void setLimit(LongValue limit) {
        this.limit = limit;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("REJECT LIMIT ");
        if (limit != null) {
            sql.append(limit);
        } else {
            sql.append("UNLIMITED");
        }

        if (errors) {
            sql.append(" ERRORS");
        }

        return sql.toString();
    }
}
