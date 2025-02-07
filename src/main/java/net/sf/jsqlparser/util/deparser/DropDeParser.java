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

import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class DropDeParser extends AbstractDeParser<Drop> {

    public DropDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(Drop drop) {
        builder.append("DROP ");
        if (drop.isUsingTemporary()) {
            builder.append("TEMPORARY ");
        }
        if (drop.isMaterialized()) {
            builder.append("MATERIALIZED ");
        }
        builder.append(drop.getType());
        if (drop.isIfExists()) {
            builder.append(" IF EXISTS");
        }

        builder.append(" ").append(drop.getName());

        if (drop.getType().equals("FUNCTION")) {
            builder.append(Drop.formatFuncParams(drop.getParamsByType("FUNCTION")));
        }

        if (drop.getParameters() != null && !drop.getParameters().isEmpty()) {
            builder.append(" ")
                    .append(PlainSelect.getStringList(drop.getParameters(), false, false));
        }
    }

}
