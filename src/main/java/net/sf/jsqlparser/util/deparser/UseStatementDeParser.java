/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.UseStatement;

public class UseStatementDeParser {

    protected StringBuilder buffer;

    public UseStatementDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(UseStatement set) {
        buffer.append("USE ").append(set.getName());
    }
}
