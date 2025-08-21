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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

public abstract class SetOperation extends ASTNodeAccessImpl {
    String modifier = "";

    public boolean isAll() {
        return modifier.contains("ALL");
    }

    public void setAll(boolean all) {
        this.modifier = "ALL";
    }

    public boolean isDistinct() {
        return modifier.contains("DISTINCT");
    }

    public void setDistinct(boolean distinct) {
        this.modifier = "DISTINCT";
    }

    private SetOperationType type;

    public SetOperation(SetOperationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return modifier == null || modifier.isEmpty() ? type.name() : type.name() + " " + modifier;
    }
}
