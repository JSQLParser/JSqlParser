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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** A parenthesized generated column expression, distinct from an identity declaration. */
public class GeneratedColumnDefinition implements Serializable {
    public enum Storage {
        STORED, VIRTUAL
    }

    private Expression expression;
    private boolean generatedAlways;
    private Storage storage;

    public GeneratedColumnDefinition(Expression expression) {
        setExpression(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression, "expression");
    }

    public boolean isGeneratedAlways() {
        return generatedAlways;
    }

    public void setGeneratedAlways(boolean generatedAlways) {
        this.generatedAlways = generatedAlways;
    }

    /** Returns null when the SQL does not specify STORED or VIRTUAL. */
    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public List<String> getTokens() {
        List<String> tokens = new ArrayList<>();
        if (generatedAlways) {
            tokens.add("GENERATED");
            tokens.add("ALWAYS");
        }
        tokens.add("AS");
        tokens.add("(" + expression + ")");
        if (storage != null) {
            tokens.add(storage.name());
        }
        return tokens;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (generatedAlways) {
            builder.append("GENERATED ALWAYS ");
        }
        appendExpressionTo(builder, expression, expressionPrinter);
        if (storage != null) {
            builder.append(' ').append(storage);
        }
        return builder;
    }

    /** Prints the common AS (expression) body of declarations and expression replacements. */
    public static StringBuilder appendExpressionTo(StringBuilder builder, Expression expression,
            Consumer<Expression> expressionPrinter) {
        builder.append("AS (");
        expressionPrinter.accept(expression);
        return builder.append(')');
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
