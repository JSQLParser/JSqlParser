/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class JsonFunctionExpression implements Serializable {
    private final Expression expression;

    private boolean usingFormatJson = false;

    public JsonFunctionExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression, "The EXPRESSION must not be null");
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isUsingFormatJson() {
        return usingFormatJson;
    }

    public void setUsingFormatJson(boolean usingFormatJson) {
        this.usingFormatJson = usingFormatJson;
    }

    public JsonFunctionExpression withUsingFormatJson(boolean usingFormatJson) {
        this.setUsingFormatJson(usingFormatJson);
        return this;
    }

    public StringBuilder append(StringBuilder builder) {
        return builder.append(getExpression()).append(isUsingFormatJson() ? " FORMAT JSON" : "");
    }

    @Override
    public String toString() {
        return append(new StringBuilder()).toString();
    }
}
