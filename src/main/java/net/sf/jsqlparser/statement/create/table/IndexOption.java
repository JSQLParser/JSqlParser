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

/** A named option with an optional expression value and explicit equals spelling. */
public class IndexOption implements Serializable {
    private String name;
    private Expression value;
    private boolean useEquals;

    public IndexOption() {}

    public IndexOption(String name, Expression value, boolean useEquals) {
        this.name = name;
        this.value = value;
        this.useEquals = useEquals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    public IndexOption withName(String name) {
        setName(name);
        return this;
    }

    public IndexOption withValue(Expression value) {
        setValue(value);
        return this;
    }

    public IndexOption withUseEquals(boolean useEquals) {
        setUseEquals(useEquals);
        return this;
    }

    @Override
    public String toString() {
        if (value == null) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, expression -> builder.append(expression)).toString();
    }

    public StringBuilder appendTo(StringBuilder builder,
            Consumer<Expression> expressionPrinter) {
        builder.append(name);
        if (value != null) {
            builder.append(useEquals ? " = " : " ");
            expressionPrinter.accept(value);
        }
        return builder;
    }

    public static StringBuilder appendListTo(StringBuilder builder, List<IndexOption> options,
            Consumer<Expression> expressionPrinter) {
        builder.append('(');
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            options.get(i).appendTo(builder, expressionPrinter);
        }
        return builder.append(')');
    }
}
