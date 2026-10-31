/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.securitylabel;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A PostgreSQL {@code SECURITY LABEL} statement. The labeled object is a named object, a column, a
 * routine signature or a large object, selected by {@link #getObjectType()}. Each target setter
 * selects its object type and discards the previous target. A null label stands for
 * {@code IS NULL}, which removes the label.
 */
public class SecurityLabel extends ASTNodeAccessImpl implements Statement {

    public enum ObjectType {
        TABLE, COLUMN, AGGREGATE, DATABASE, DOMAIN, EVENT_TRIGGER, FOREIGN_TABLE, FUNCTION, LANGUAGE, LARGE_OBJECT, MATERIALIZED_VIEW, PROCEDURE, PUBLICATION, ROLE, ROUTINE, SCHEMA, SEQUENCE, SUBSCRIPTION, TABLESPACE, TYPE, VIEW;

        private static final Set<ObjectType> ROUTINES =
                EnumSet.of(AGGREGATE, FUNCTION, PROCEDURE, ROUTINE);
        private static final Set<ObjectType> RELATIONS =
                EnumSet.of(TABLE, FOREIGN_TABLE, MATERIALIZED_VIEW, VIEW);

        /** Returns the SQL spelling, for example {@code MATERIALIZED VIEW}. */
        public String getKeyword() {
            return name().replace('_', ' ');
        }

        /** Whether the object is addressed by a routine signature. */
        public boolean isRoutine() {
            return ROUTINES.contains(this);
        }

        /** Whether the object is a table or view that table visitors report. */
        public boolean isRelation() {
            return RELATIONS.contains(this);
        }

        /** Whether the object is addressed by its name alone. */
        public boolean isNamed() {
            return this != COLUMN && this != LARGE_OBJECT && !isRoutine();
        }
    }

    private String provider;
    private ObjectType objectType;
    private Table objectName;
    private Column column;
    private RoutineReference routine;
    private Long largeObjectOid;
    private boolean procedural;
    private StringValue label;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /** The label provider as written, an identifier or a string literal; null if omitted. */
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    /** The name of a {@linkplain ObjectType#isNamed() named} object. */
    public Table getObjectName() {
        return objectName;
    }

    /** Selects a {@linkplain ObjectType#isNamed() named} object. */
    public void setObjectName(ObjectType objectType, Table objectName) {
        if (!objectType.isNamed()) {
            throw new IllegalArgumentException(objectType + " is not addressed by a name");
        }
        select(objectType);
        this.objectName = objectName;
    }

    public Column getColumn() {
        return column;
    }

    /** Selects a column, addressed by its qualified name. */
    public void setColumn(Column column) {
        select(ObjectType.COLUMN);
        this.column = column;
    }

    public RoutineReference getRoutine() {
        return routine;
    }

    /** Selects a function, procedure, routine or aggregate signature. */
    public void setRoutine(ObjectType objectType, RoutineReference routine) {
        if (!objectType.isRoutine()) {
            throw new IllegalArgumentException(objectType + " is not addressed by a signature");
        }
        select(objectType);
        this.routine = routine;
    }

    public Long getLargeObjectOid() {
        return largeObjectOid;
    }

    /** Selects a large object, addressed by its OID. */
    public void setLargeObjectOid(long largeObjectOid) {
        select(ObjectType.LARGE_OBJECT);
        this.largeObjectOid = largeObjectOid;
    }

    /** Whether a LANGUAGE target is spelled {@code PROCEDURAL LANGUAGE}. */
    public boolean isProcedural() {
        return procedural;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    /** The label, or null for {@code IS NULL}. */
    public StringValue getLabel() {
        return label;
    }

    public void setLabel(StringValue label) {
        this.label = label;
    }

    private void select(ObjectType objectType) {
        this.objectType = objectType;
        objectName = null;
        column = null;
        routine = null;
        largeObjectOid = null;
    }

    /** Visits the table or view explicitly named by this label, including a column's table. */
    public void visitRelations(Consumer<Table> visitor) {
        Table relation = objectType == ObjectType.COLUMN && column != null ? column.getTable()
                : objectType != null && objectType.isRelation() ? objectName : null;
        if (relation != null) {
            visitor.accept(relation);
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> relationWriter,
            Consumer<Column> columnWriter, Consumer<StringValue> labelWriter) {
        builder.append("SECURITY LABEL ");
        if (provider != null) {
            builder.append("FOR ").append(provider).append(' ');
        }
        builder.append("ON ");
        if (objectType == ObjectType.LANGUAGE && procedural) {
            builder.append("PROCEDURAL ");
        }
        builder.append(objectType.getKeyword()).append(' ');
        if (objectType == ObjectType.COLUMN) {
            columnWriter.accept(column);
        } else if (objectType.isRoutine()) {
            builder.append(routine);
        } else if (objectType == ObjectType.LARGE_OBJECT) {
            builder.append(largeObjectOid);
        } else if (objectType.isRelation()) {
            relationWriter.accept(objectName);
        } else {
            builder.append(objectName);
        }
        builder.append(" IS ");
        if (label == null) {
            builder.append("NULL");
        } else {
            labelWriter.accept(label);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append, builder::append, builder::append).toString();
    }

    public SecurityLabel withProvider(String provider) {
        setProvider(provider);
        return this;
    }

    public SecurityLabel withObjectName(ObjectType objectType, Table objectName) {
        setObjectName(objectType, objectName);
        return this;
    }

    public SecurityLabel withColumn(Column column) {
        setColumn(column);
        return this;
    }

    public SecurityLabel withRoutine(ObjectType objectType, RoutineReference routine) {
        setRoutine(objectType, routine);
        return this;
    }

    public SecurityLabel withLargeObjectOid(long largeObjectOid) {
        setLargeObjectOid(largeObjectOid);
        return this;
    }

    public SecurityLabel withLabel(StringValue label) {
        setLabel(label);
        return this;
    }
}
