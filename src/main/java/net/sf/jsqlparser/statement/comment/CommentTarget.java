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

/** A catalog object addressed by COMMENT, rather than a function invocation or a query. */
public class CommentTarget implements Serializable {
    public enum Kind {
        INDEX, SCHEMA, SEQUENCE, DOMAIN, TYPE, MATERIALIZED_VIEW, FUNCTION, CONSTRAINT
    }

    private Kind kind;
    private Table name;
    private RoutineReference routine;
    private Table relation;
    private boolean onDomain;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /** The object's identifier; using Table preserves the individual name components. */
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

    /** The table or domain owning a constraint, distinguished by {@link #isOnDomain()}. */
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

    /** Returns only an explicitly named table/view, never an index, type, function or domain. */
    public Table getReferencedRelation() {
        if (kind == Kind.MATERIALIZED_VIEW) {
            return name;
        }
        return kind == Kind.CONSTRAINT && !onDomain ? relation : null;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> relationWriter) {
        builder.append(kind.name().replace('_', ' ')).append(' ');
        if (kind == Kind.FUNCTION) {
            builder.append(routine);
        } else if (kind == Kind.MATERIALIZED_VIEW) {
            relationWriter.accept(name);
        } else {
            builder.append(name);
            if (kind == Kind.CONSTRAINT) {
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
