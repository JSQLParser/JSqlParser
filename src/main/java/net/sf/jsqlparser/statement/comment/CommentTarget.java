/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.comment;

import java.io.Serializable;
import java.util.function.Consumer;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;

/** A catalog object addressed by COMMENT, rather than a function invocation or a query. */
public class CommentTarget implements Serializable {
    public enum Kind {
        INDEX, SCHEMA, SEQUENCE, DOMAIN, TYPE, MATERIALIZED_VIEW, FUNCTION, CONSTRAINT,

        TRIGGER, POLICY, RULE, PROCEDURE, ROUTINE, AGGREGATE, CAST,

        COLLATION, FOREIGN_TABLE, STATISTICS, DATABASE, ROLE, TABLESPACE, ACCESS_METHOD, EXTENSION,

        FOREIGN_DATA_WRAPPER, LANGUAGE, SERVER, EVENT_TRIGGER, PUBLICATION, SUBSCRIPTION,

        OPERATOR, OPERATOR_CLASS, OPERATOR_FAMILY, LARGE_OBJECT, CONVERSION, TRANSFORM,

        TEXT_SEARCH_CONFIGURATION, TEXT_SEARCH_DICTIONARY, TEXT_SEARCH_PARSER, TEXT_SEARCH_TEMPLATE;

        /**
         * Whether the target is a routine signature, exposed by {@link CommentTarget#getRoutine()}.
         */
        public boolean isRoutine() {
            return this == FUNCTION || this == PROCEDURE || this == ROUTINE || this == AGGREGATE;
        }

        /**
         * Whether the target is named within the table or domain of
         * {@link CommentTarget#getRelation()}.
         */
        public boolean isOwnedByRelation() {
            return this == CONSTRAINT || this == TRIGGER || this == POLICY || this == RULE;
        }

        /**
         * Whether the target is named within the index access method of
         * {@link CommentTarget#getAccessMethod()}.
         */
        public boolean isOwnedByAccessMethod() {
            return this == OPERATOR_CLASS || this == OPERATOR_FAMILY;
        }
    }

    private Kind kind;
    private Table name;
    private RoutineReference routine;
    private Table relation;
    private boolean onDomain;
    private boolean procedural;
    private ColDataType sourceType;
    private ColDataType targetType;
    private ExclusionOperator operator;
    private ColDataType leftOperandType;
    private ColDataType rightOperandType;
    private String accessMethod;
    private Long largeObjectOid;
    private ColDataType transformType;
    private String language;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * The object's identifier; using Table preserves the individual name components. Routine, cast,
     * operator, large object and transform targets have no name.
     */
    public Table getName() {
        return name;
    }

    public void setName(Table name) {
        this.name = name;
    }

    public RoutineReference getRoutine() {
        return routine;
    }

    public void setRoutine(RoutineReference routine) {
        this.routine = routine;
    }

    /**
     * The table owning a constraint, trigger, policy or rule, or the domain owning a constraint,
     * distinguished by {@link #isOnDomain()}.
     */
    public Table getRelation() {
        return relation;
    }

    public void setRelation(Table relation) {
        this.relation = relation;
    }

    public boolean isOnDomain() {
        return onDomain;
    }

    public void setOnDomain(boolean onDomain) {
        this.onDomain = onDomain;
    }

    /** Whether a {@link Kind#LANGUAGE} target was written as PROCEDURAL LANGUAGE. */
    public boolean isProcedural() {
        return procedural;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    /** The source type of a {@link Kind#CAST} target. */
    public ColDataType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ColDataType sourceType) {
        this.sourceType = sourceType;
    }

    /** The target type of a {@link Kind#CAST} target. */
    public ColDataType getTargetType() {
        return targetType;
    }

    public void setTargetType(ColDataType targetType) {
        this.targetType = targetType;
    }

    /**
     * The possibly schema-qualified operator of an {@link Kind#OPERATOR} target, read by the
     * operator reference shared with exclusion constraints and ALTER EXTENSION.
     */
    public ExclusionOperator getOperator() {
        return operator;
    }

    public void setOperator(ExclusionOperator operator) {
        this.operator = operator;
    }

    /** The left operand type of an {@link Kind#OPERATOR} target; null stands for NONE. */
    public ColDataType getLeftOperandType() {
        return leftOperandType;
    }

    public void setLeftOperandType(ColDataType leftOperandType) {
        this.leftOperandType = leftOperandType;
    }

    /** The right operand type of an {@link Kind#OPERATOR} target; null stands for NONE. */
    public ColDataType getRightOperandType() {
        return rightOperandType;
    }

    public void setRightOperandType(ColDataType rightOperandType) {
        this.rightOperandType = rightOperandType;
    }

    /**
     * The index access method of an {@link Kind#OPERATOR_CLASS} or {@link Kind#OPERATOR_FAMILY}
     * target.
     */
    public String getAccessMethod() {
        return accessMethod;
    }

    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
    }

    /** The OID of a {@link Kind#LARGE_OBJECT} target. */
    public Long getLargeObjectOid() {
        return largeObjectOid;
    }

    public void setLargeObjectOid(Long largeObjectOid) {
        this.largeObjectOid = largeObjectOid;
    }

    /** The data type of a {@link Kind#TRANSFORM} target. */
    public ColDataType getTransformType() {
        return transformType;
    }

    public void setTransformType(ColDataType transformType) {
        this.transformType = transformType;
    }

    /** The procedural language of a {@link Kind#TRANSFORM} target. */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns only an explicitly named table, foreign table or view, never an index, type,
     * function, statistics object or domain.
     */
    public Table getReferencedRelation() {
        if (namesRelation()) {
            return name;
        }
        return kind != null && kind.isOwnedByRelation() && !onDomain ? relation : null;
    }

    private boolean namesRelation() {
        return kind == Kind.MATERIALIZED_VIEW || kind == Kind.FOREIGN_TABLE;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> relationWriter) {
        if (kind == Kind.LANGUAGE && procedural) {
            builder.append("PROCEDURAL ");
        }
        builder.append(kind.name().replace('_', ' ')).append(' ');
        if (kind.isRoutine()) {
            builder.append(routine);
        } else if (kind == Kind.CAST) {
            builder.append('(').append(sourceType).append(" AS ").append(targetType).append(')');
        } else if (kind == Kind.OPERATOR) {
            builder.append(operator).append(" (").append(operandType(leftOperandType))
                    .append(", ").append(operandType(rightOperandType)).append(')');
        } else if (kind == Kind.LARGE_OBJECT) {
            builder.append(largeObjectOid);
        } else if (kind == Kind.TRANSFORM) {
            builder.append("FOR ").append(transformType).append(" LANGUAGE ").append(language);
        } else if (namesRelation()) {
            relationWriter.accept(name);
        } else {
            builder.append(name);
            if (kind.isOwnedByRelation()) {
                builder.append(" ON ");
                if (onDomain) {
                    builder.append("DOMAIN ").append(relation);
                } else {
                    relationWriter.accept(relation);
                }
            } else if (kind.isOwnedByAccessMethod()) {
                builder.append(" USING ").append(accessMethod);
            }
        }
        return builder;
    }

    private static Object operandType(ColDataType type) {
        return type == null ? "NONE" : type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
