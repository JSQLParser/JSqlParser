/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.schema.Table;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.2/en/table.html"></a> `TABLE table_name [ORDER
 *      BY column_name] [LIMIT number [OFFSET number]]` Union not currently supported
 *
 * @author jxnu-liguobin
 */
public class TableStatement extends Select {

    private Table table;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("TABLE ").append(table.getName());
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    @Override
    public StringBuilder appendTo(StringBuilder builder) {

        appendSelectBodyTo(builder);

        builder.append(orderByToString(false, orderByElements));

        if (limit != null) {
            builder.append(limit);
        }
        if (offset != null) {
            builder.append(offset);
        }
        return builder;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }
}
