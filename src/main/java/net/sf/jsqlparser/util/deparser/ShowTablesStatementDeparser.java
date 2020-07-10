/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.show.ShowTablesStatement;

public class ShowTablesStatementDeparser extends AbstractDeParser<ShowTablesStatement> {

    public ShowTablesStatementDeparser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    void deParse(ShowTablesStatement statement) {
        buffer.append(statement);
    }
}
