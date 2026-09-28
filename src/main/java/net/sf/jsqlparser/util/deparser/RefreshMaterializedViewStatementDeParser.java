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
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;

/**
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementDeParser
        extends AbstractDeParser<RefreshMaterializedViewStatement> {

    private final Consumer<Table> tablePrinter;

    public RefreshMaterializedViewStatementDeParser(StringBuilder buffer) {
        super(buffer);
        this.tablePrinter = table -> builder.append(table);
    }

    public RefreshMaterializedViewStatementDeParser(StringBuilder buffer,
            Consumer<Table> tablePrinter) {
        super(buffer);
        this.tablePrinter = tablePrinter;
    }

    @Override
    public void deParse(RefreshMaterializedViewStatement view) {
        view.appendTo(builder, tablePrinter);
    }

}
