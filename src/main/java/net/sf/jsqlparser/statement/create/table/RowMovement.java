/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

/**
 * Holds data for the {@code row_movement} clause: https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_7002.htm#i2204697
 */
public class RowMovement {

    private RowMovementMode mode;

    public RowMovementMode getMode() {
        return mode;
    }

    public void setMode(RowMovementMode mode) {
        this.mode = mode;
    }

    public RowMovement mode(RowMovementMode mode) {
        this.setMode(mode);
        return this;
    }
}
