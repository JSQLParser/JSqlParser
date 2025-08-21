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

public class MinusOp extends SetOperation {
    public MinusOp() {
        this("");
    }

    public MinusOp(String modifier) {
        super(SetOperationType.MINUS);
        this.modifier = modifier;
    }

    public MinusOp withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public MinusOp withAll(boolean all) {
        this.setAll(all);
        return this;
    }

    public MinusOp withModifier(String modifier) {
        this.modifier = modifier;
        return this;
    }
}
