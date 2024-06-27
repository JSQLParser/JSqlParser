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

import net.sf.jsqlparser.statement.show.ShowIndexStatement;

/**
 * @author Jayant Kumar Yadav
 */

public class ShowIndexStatementDeParser extends AbstractDeParser<ShowIndexStatement> {

    public ShowIndexStatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(ShowIndexStatement show) {
        buffer.append("SHOW INDEX FROM ").append(show.getTableName());
    }

}
