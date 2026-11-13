/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.conversion;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL
 * {@code CREATE [DEFAULT] CONVERSION name FOR 'source_encoding' TO 'dest_encoding' FROM function}.
 * Names retain SQL identifier quoting and their schema.
 */
public class CreateConversion extends ASTNodeAccessImpl implements Statement {
    private boolean defaultConversion;
    private String name;
    private StringValue sourceEncoding;
    private StringValue destinationEncoding;
    private String function;

    /**
     * Whether {@code DEFAULT} makes this the default conversion of the two encodings.
     *
     * @return whether the statement has {@code DEFAULT}
     */
    public boolean isDefaultConversion() {
        return defaultConversion;
    }

    public void setDefaultConversion(boolean defaultConversion) {
        this.defaultConversion = defaultConversion;
    }

    public CreateConversion withDefaultConversion(boolean defaultConversion) {
        setDefaultConversion(defaultConversion);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateConversion withName(String name) {
        setName(name);
        return this;
    }

    public StringValue getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(StringValue sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public CreateConversion withSourceEncoding(StringValue sourceEncoding) {
        setSourceEncoding(sourceEncoding);
        return this;
    }

    public StringValue getDestinationEncoding() {
        return destinationEncoding;
    }

    public void setDestinationEncoding(StringValue destinationEncoding) {
        this.destinationEncoding = destinationEncoding;
    }

    public CreateConversion withDestinationEncoding(StringValue destinationEncoding) {
        setDestinationEncoding(destinationEncoding);
        return this;
    }

    /**
     * The name of the conversion function. The statement names it without an argument list.
     *
     * @return the function name, with its schema if it has one
     */
    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public CreateConversion withFunction(String function) {
        setFunction(function);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE ");
        if (defaultConversion) {
            builder.append("DEFAULT ");
        }
        return builder.append("CONVERSION ").append(name).append(" FOR ").append(sourceEncoding)
                .append(" TO ").append(destinationEncoding).append(" FROM ").append(function);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
