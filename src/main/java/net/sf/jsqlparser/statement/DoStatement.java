/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** A PostgreSQL anonymous routine. The language-specific body remains a string literal. */
public class DoStatement extends ASTNodeAccessImpl implements Statement {
    private StringValue code;
    private String language;
    private boolean languageBeforeCode;

    public StringValue getCode() {
        return code;
    }

    public void setCode(StringValue code) {
        this.code = code;
    }

    public DoStatement withCode(StringValue code) {
        setCode(code);
        return this;
    }

    /** Returns the explicit language, or null when omitted. */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public DoStatement withLanguage(String language) {
        setLanguage(language);
        return this;
    }

    public boolean isLanguageBeforeCode() {
        return languageBeforeCode;
    }

    public void setLanguageBeforeCode(boolean languageBeforeCode) {
        this.languageBeforeCode = languageBeforeCode;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<StringValue> codePrinter) {
        builder.append("DO ");
        if (languageBeforeCode && language != null) {
            builder.append("LANGUAGE ").append(language).append(' ');
        }
        codePrinter.accept(code);
        if (!languageBeforeCode && language != null) {
            builder.append(" LANGUAGE ").append(language);
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, value -> builder.append(value)).toString();
    }
}
