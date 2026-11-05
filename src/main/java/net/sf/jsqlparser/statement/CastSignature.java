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
 * A PostgreSQL cast, named by the types it converts between: {@code (source_type AS target_type)},
 * as in {@code DROP CAST (text AS integer)}.
 */
public class CastSignature extends ASTNodeAccessImpl {

    private ColDataType sourceType;
    private ColDataType targetType;

    public CastSignature() {
        // the types are set by the parser or the setters
    }

    public CastSignature(ColDataType sourceType, ColDataType targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public ColDataType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ColDataType sourceType) {
        this.sourceType = sourceType;
    }

    public CastSignature withSourceType(ColDataType sourceType) {
        setSourceType(sourceType);
        return this;
    }

    public ColDataType getTargetType() {
        return targetType;
    }

    public void setTargetType(ColDataType targetType) {
        this.targetType = targetType;
    }

    public CastSignature withTargetType(ColDataType targetType) {
        setTargetType(targetType);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append('(').append(sourceType).append(" AS ").append(targetType)
                .append(')');
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
