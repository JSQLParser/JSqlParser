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

import net.sf.jsqlparser.statement.UseStatement;

public class UseStatementDeParser extends AbstractDeParser<UseStatement> {

    public UseStatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(UseStatement set) {
        builder.append("USE ");
        if (set.hasSchemaKeyword()) {
            builder.append("SCHEMA ");
        }
        builder.append(set.getName());
    }
}
