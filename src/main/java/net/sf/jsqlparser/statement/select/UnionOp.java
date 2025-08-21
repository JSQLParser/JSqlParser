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
    public UnionOp() {
        this("");
    }

    public UnionOp(String modifier) {
        super(SetOperationType.UNION);
        this.modifier = modifier;
    }

    public UnionOp withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public UnionOp withAll(boolean all) {
        this.setAll(all);
        return this;
    }

    public UnionOp withModifier(String modifier) {
        this.modifier = modifier;
        return this;
    }
}
