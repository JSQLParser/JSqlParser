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

import net.sf.jsqlparser.statement.ShowStatement;

public class ShowStatementDeParser extends AbstractDeParser<ShowStatement> {

    public ShowStatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(ShowStatement show) {
        builder.append("SHOW ").append(show.getName());
    }
}
