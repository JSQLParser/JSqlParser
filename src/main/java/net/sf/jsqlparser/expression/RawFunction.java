/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

/**
 * Function with a raw argument body preserved as-is for deparsing.
 */
public class RawFunction extends Function {
    private String rawArguments;

    public RawFunction() {}

    public RawFunction(String name, String rawArguments) {
        setName(name);
        this.rawArguments = rawArguments;
    }

    public String getRawArguments() {
        return rawArguments;
    }

    public void setRawArguments(String rawArguments) {
        this.rawArguments = rawArguments;
    }

    @Override
    public String toString() {
        String name = getName();
        if (rawArguments == null) {
            return name + "()";
        }
        return name + "(" + rawArguments + ")";
    }
}
