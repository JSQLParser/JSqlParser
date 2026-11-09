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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 * A PostgreSQL transform, named by the data type and the procedural language it connects:
 * {@code FOR type_name LANGUAGE lang_name}, as in
 * {@code DROP TRANSFORM FOR hstore LANGUAGE plpython3u}.
 */
public class TransformSignature extends ASTNodeAccessImpl {

    private ColDataType dataType;
    private String language;

    public TransformSignature() {
        // the type and language are set by the parser or the setters
    }

    public TransformSignature(ColDataType dataType, String language) {
        this.dataType = dataType;
        this.language = language;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColDataType dataType) {
        this.dataType = dataType;
    }

    public TransformSignature withDataType(ColDataType dataType) {
        setDataType(dataType);
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public TransformSignature withLanguage(String language) {
        setLanguage(language);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("FOR ").append(dataType).append(" LANGUAGE ").append(language);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
