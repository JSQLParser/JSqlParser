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
        buffer.append("DROP ");
        buffer.append(drop.getType());
        if (drop.isIfExists()) {
            buffer.append(" IF EXISTS");
        }

        buffer.append(" ").append(drop.getName());

        if (drop.getType().equals("FUNCTION")) {
            buffer.append(Drop.formatFuncParams(drop.getParamsByType("FUNCTION")));
        }

        if (drop.getParameters() != null && !drop.getParameters().isEmpty()) {
            buffer.append(" ").append(PlainSelect.getStringList(drop.getParameters()));
        }
    }

}
