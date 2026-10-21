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

/** A catalog object addressed by COMMENT, rather than a function invocation or a query. */
public class CommentTarget implements Serializable {
    public enum Kind {
        INDEX, SCHEMA, SEQUENCE, DOMAIN, TYPE, MATERIALIZED_VIEW, FUNCTION, CONSTRAINT,

        TRIGGER, POLICY, RULE, PROCEDURE, ROUTINE, AGGREGATE, CAST,

        COLLATION, FOREIGN_TABLE, STATISTICS, DATABASE, ROLE, TABLESPACE, ACCESS_METHOD, EXTENSION,

        FOREIGN_DATA_WRAPPER, LANGUAGE, SERVER, EVENT_TRIGGER, PUBLICATION, SUBSCRIPTION;

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
    }

    private Kind kind;
    private Table name;
    private RoutineReference routine;
    private Table relation;
    private boolean onDomain;
    private boolean procedural;
    private ColDataType sourceType;
    private ColDataType targetType;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * The object's identifier; using Table preserves the individual name components. Routine and
     * cast targets have no name.
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
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
