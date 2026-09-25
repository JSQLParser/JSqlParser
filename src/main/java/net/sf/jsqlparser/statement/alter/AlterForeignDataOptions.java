/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;

/** Foreign table OPTIONS, optionally applied to one column. */
public class AlterForeignDataOptions extends AlterExpression {
    private List<ForeignDataOption> options;

    public AlterForeignDataOptions() {
        setOperation(AlterOperation.FOREIGN_OPTIONS);
    }

    public List<ForeignDataOption> getOptions() {
        return options;
    }

    public void setOptions(List<ForeignDataOption> options) {
        this.options = options;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (getColumnName() != null) {
            builder.append(hasColumn() ? "ALTER COLUMN " : "ALTER ").append(getColumnName())
                    .append(' ');
        }
        ForeignDataOption.appendOptionsTo(builder, options, expressionPrinter);
        return builder;
    }

    @Override
    protected void appendBody(StringBuilder builder) {
        appendTo(builder, builder::append);
    }
}
