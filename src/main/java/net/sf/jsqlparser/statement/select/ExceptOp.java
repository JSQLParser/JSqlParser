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

public class ExceptOp extends SetOperation {

    public ExceptOp() {
        this("");
    }

    public ExceptOp(String modifier) {
        super(SetOperationType.EXCEPT);
        this.modifier = modifier;
    }

    public ExceptOp withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public ExceptOp withAll(boolean all) {
        this.setAll(all);
        return this;
    }

    public ExceptOp withModifier(String modifier) {
        this.modifier = modifier;
        return this;
    }
}
