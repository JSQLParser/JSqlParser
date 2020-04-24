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

import net.sf.jsqlparser.statement.ShowColumnsStatement;

public class ShowColumnsStatementDeParser {

    protected StringBuilder buffer;

    public ShowColumnsStatementDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(ShowColumnsStatement show) {
        buffer.append("SHOW COLUMNS FROM ").append(show.getTableName());
    }
}
