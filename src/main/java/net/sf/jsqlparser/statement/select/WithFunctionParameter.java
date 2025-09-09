/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;

public class WithFunctionParameter implements Serializable {
    private String name;
    private String type; // e.g., INT

    public WithFunctionParameter() {
    }

    public WithFunctionParameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WithFunctionParameter withName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WithFunctionParameter withType(String type) {
        this.type = type;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(name).append(" ").append(type);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
