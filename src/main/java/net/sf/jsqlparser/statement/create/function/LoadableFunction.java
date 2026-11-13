/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.sf.jsqlparser.expression.StringValue;

/**
 * The definition of a MySQL loadable function:
 * {@code [IF NOT EXISTS] name RETURNS {STRING | INTEGER | REAL | DECIMAL} SONAME 'library'}.
 */
public class LoadableFunction implements Serializable {

    /** The result type of a loadable function. */
    public enum ReturnType {
        STRING, INTEGER, REAL, DECIMAL;

        /**
         * @param keyword a type keyword as MySQL spells it, including the synonyms {@code INT},
         *        {@code INT4} and {@code DEC}
         * @return the result type, or {@code null} if the keyword names none
         */
        public static ReturnType from(String keyword) {
            switch (keyword.toUpperCase(Locale.ROOT)) {
                case "STRING":
                    return STRING;
                case "INT":
                case "INT4":
                case "INTEGER":
                    return INTEGER;
                case "REAL":
                    return REAL;
                case "DEC":
                case "DECIMAL":
                    return DECIMAL;
                default:
                    return null;
            }
        }
    }

    private boolean ifNotExists;
    private String name;
    private String returnTypeKeyword;
    private StringValue library;

    public LoadableFunction() {
        // filled by the setters
    }

    public LoadableFunction(String name, ReturnType returnType, String library) {
        setName(name);
        setReturnType(returnType);
        setLibrary(new StringValue(library));
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReturnType getReturnType() {
        return returnTypeKeyword == null ? null : ReturnType.from(returnTypeKeyword);
    }

    public void setReturnType(ReturnType returnType) {
        returnTypeKeyword = Objects.requireNonNull(returnType, "returnType").name();
    }

    /**
     * @return the result type as written, which may be one of the synonyms {@code INT},
     *         {@code INT4} and {@code DEC}
     */
    public String getReturnTypeKeyword() {
        return returnTypeKeyword;
    }

    /**
     * @param keyword a keyword that {@link ReturnType#from(String)} resolves
     */
    public void setReturnTypeKeyword(String keyword) {
        if (ReturnType.from(keyword) == null) {
            throw new IllegalArgumentException(
                    "A loadable function returns STRING, INTEGER, REAL or DECIMAL, not " + keyword);
        }
        returnTypeKeyword = keyword;
    }

    /**
     * @return the shared library file named by {@code SONAME}
     */
    public StringValue getLibrary() {
        return library;
    }

    public void setLibrary(StringValue library) {
        this.library = library;
    }

    public LoadableFunction withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }

    /**
     * @return the definition as the declaration parts of the CREATE FUNCTION statement
     */
    public List<String> toDeclarationParts() {
        List<String> parts = new ArrayList<>();
        if (ifNotExists) {
            parts.add("IF");
            parts.add("NOT");
            parts.add("EXISTS");
        }
        parts.add(name);
        parts.add("RETURNS");
        parts.add(returnTypeKeyword);
        parts.add("SONAME");
        parts.add(String.valueOf(library));
        return parts;
    }

    @Override
    public String toString() {
        return String.join(" ", toDeclarationParts());
    }
}
