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

import java.util.function.Consumer;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.drop.Drop;

public class DropDeParser extends AbstractDeParser<Drop> {
    private final Consumer<Table> tablePrinter;

    public DropDeParser(StringBuilder buffer) {
        this(buffer, buffer::append);
    }

    public DropDeParser(StringBuilder buffer, Consumer<Table> tablePrinter) {
        super(buffer);
        this.tablePrinter = tablePrinter;
    }

    @Override
    public void deParse(Drop drop) {
        drop.appendTo(builder, tablePrinter);
    }
}
