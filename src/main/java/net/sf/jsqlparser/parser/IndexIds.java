/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

public enum IndexIds {

    NAME(0),
    SCHEMA(1),
    DATABASE(2),
    SERVER(3);
    private int id;

    IndexIds(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }
}
