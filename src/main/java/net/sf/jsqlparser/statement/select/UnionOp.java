/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

public class UnionOp extends SetOperation {

    private boolean distinct;
    private boolean all;

    public UnionOp() {
        super(SetOperationType.UNION);
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public String toString() {
        String allDistinct = "";
        if (isAll()) {
            allDistinct = " ALL";
        } else if (isDistinct()) {
            allDistinct = " DISTINCT";
        }
        return super.toString() + allDistinct;
    }

    public UnionOp withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public UnionOp withAll(boolean all) {
        this.setAll(all);
        return this;
    }
}
