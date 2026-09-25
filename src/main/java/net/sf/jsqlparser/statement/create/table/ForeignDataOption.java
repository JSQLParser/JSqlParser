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
import net.sf.jsqlparser.expression.StringValue;

/** A foreign table or column option; absent action means the implicit ADD form. */
public class ForeignDataOption implements Serializable {
    public enum Action {
        ADD, SET, DROP
    }

    private Action action;
    private String name;
    private StringValue value;

    public ForeignDataOption(String name, StringValue value) {
        this.name = name;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
        if (action == Action.DROP) {
            value = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringValue getValue() {
        return value;
    }

    public void setValue(StringValue value) {
        this.value = value;
    }

    public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (action != null) {
            builder.append(action).append(' ');
        }
        builder.append(name);
        if (action != Action.DROP && value != null) {
            builder.append(' ');
            expressionPrinter.accept(value);
        }
    }

    public static void appendOptionsTo(StringBuilder builder, List<ForeignDataOption> options,
            Consumer<Expression> expressionPrinter) {
        builder.append("OPTIONS (");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            options.get(i).appendTo(builder, expressionPrinter);
        }
        builder.append(')');
    }

    public static void visitExpressions(List<ForeignDataOption> options,
            Consumer<Expression> visitor) {
        if (options != null) {
            for (ForeignDataOption option : options) {
                if (option.action != Action.DROP && option.value != null) {
                    visitor.accept(option.value);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append);
        return builder.toString();
    }
}
