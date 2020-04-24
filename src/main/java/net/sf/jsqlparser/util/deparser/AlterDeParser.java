/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.alter.Alter;

public class AlterDeParser {

    protected StringBuilder buffer;

    public AlterDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(Alter alter) {
        buffer.append(alter.toString());
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
