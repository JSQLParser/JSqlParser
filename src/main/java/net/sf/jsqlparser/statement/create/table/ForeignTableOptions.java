/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** PostgreSQL foreign server and optional table-level FDW options. */
public class ForeignTableOptions implements Serializable {
    private String server;
    private List<ForeignDataOption> options;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<ForeignDataOption> getOptions() {
        return options;
    }

    public void setOptions(List<ForeignDataOption> options) {
        this.options = options;
    }

    public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append("SERVER ").append(server);
        if (options != null && !options.isEmpty()) {
            builder.append(' ');
            ForeignDataOption.appendOptionsTo(builder, options, expressionPrinter);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append);
        return builder.toString();
    }
}
