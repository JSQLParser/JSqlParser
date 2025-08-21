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

public class IntersectOp extends SetOperation {

    public IntersectOp() {
        this("");
    }

    public IntersectOp(String modifier) {
        super(SetOperationType.INTERSECT);
        this.modifier = modifier;
    }

    public IntersectOp withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public IntersectOp withAll(boolean all) {
        this.setAll(all);
        return this;
    }

    public IntersectOp withModifier(String modifier) {
        this.modifier = modifier;
        return this;
    }
}
