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
    private String encoding;

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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public JsonFunctionExpression withEncoding(String encoding) {
        this.setEncoding(encoding);
        return this;
    }

    public StringBuilder append(StringBuilder builder) {
        builder.append(getExpression());
        if (isUsingFormatJson()) {
            builder.append(" FORMAT JSON");
            if (encoding != null) {
                builder.append(" ENCODING ").append(encoding);
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return append(new StringBuilder()).toString();
    }
}
