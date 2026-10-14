/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.RoutineCharacteristic;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER FUNCTION}, {@code ALTER PROCEDURE} or {@code ALTER ROUTINE}, or MySQL
 * {@code ALTER FUNCTION} or {@code ALTER PROCEDURE}. Both change characteristics through the shared
 * {@link RoutineCharacteristic} model. PostgreSQL can instead rename the routine, change its owner
 * or schema, or mark it as dependent on an extension. Names retain SQL identifier quoting. SQL
 * Server's routine redefinitions remain
 * {@link net.sf.jsqlparser.statement.CreateFunctionalStatement} nodes.
 */
public class AlterRoutine implements Statement {

    public enum RoutineType {
        FUNCTION, PROCEDURE, ROUTINE
    }

    public enum Action {
        /** Characteristics, see {@link #getCharacteristics()} and {@link #isRestrict()}. */
        CHARACTERISTICS,
        /** {@code RENAME TO}, see {@link #getNewName()}. */
        RENAME,
        /** {@code OWNER TO}, see {@link #getOwner()}. */
        OWNER,
        /** {@code SET SCHEMA}, see {@link #getSchemaName()}. */
        SET_SCHEMA,
        /**
         * {@code [NO] DEPENDS ON EXTENSION}, see {@link #getExtensionName()} and
         * {@link #isNoDependency()}.
         */
        DEPENDS_ON_EXTENSION
    }

    private RoutineType routineType = RoutineType.FUNCTION;
    private RoutineReference routine;
    private Action action = Action.CHARACTERISTICS;
    private final List<RoutineCharacteristic> characteristics = new ArrayList<>();
    private boolean restrict;
    private String newName;
    private String owner;
    private String schemaName;
    private String extensionName;
    private boolean noDependency;

    public RoutineType getRoutineType() {
        return routineType;
    }

    public void setRoutineType(RoutineType routineType) {
        this.routineType = routineType;
    }

    /**
     * The routine name and, for PostgreSQL, its optional argument types.
     *
     * @return the routine
     */
    public RoutineReference getRoutine() {
        return routine;
    }

    public void setRoutine(RoutineReference routine) {
        this.routine = routine;
    }

    /**
     * The rendered alteration. Payloads of other actions are retained for a later switch back.
     *
     * @return the action, {@link Action#CHARACTERISTICS} unless changed
     */
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * The characteristics of {@link Action#CHARACTERISTICS}, as a mutable list.
     *
     * @return the characteristics in source order
     */
    public List<RoutineCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public Optional<RoutineCharacteristic> getCharacteristic(RoutineCharacteristic.Kind kind) {
        return characteristics.stream().filter(characteristic -> characteristic.getKind() == kind)
                .findFirst();
    }

    /**
     * Whether PostgreSQL's {@code RESTRICT}, which has no effect, follows the characteristics.
     *
     * @return {@code true} if {@code RESTRICT} is rendered
     */
    public boolean isRestrict() {
        return restrict;
    }

    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * The new owner, a role name or {@code CURRENT_ROLE}, {@code CURRENT_USER} or
     * {@code SESSION_USER}.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    /**
     * Whether {@code NO DEPENDS ON EXTENSION} removes the dependency.
     *
     * @return {@code true} for {@code NO DEPENDS}
     */
    public boolean isNoDependency() {
        return noDependency;
    }

    public void setNoDependency(boolean noDependency) {
        this.noDependency = noDependency;
    }

    public AlterRoutine withRoutineType(RoutineType routineType) {
        setRoutineType(routineType);
        return this;
    }

    public AlterRoutine withRoutine(RoutineReference routine) {
        setRoutine(routine);
        return this;
    }

    public AlterRoutine withAction(Action action) {
        setAction(action);
        return this;
    }

    /**
     * Visits the configuration parameter values of the characteristics.
     *
     * @param visitor receives each value
     */
    public void visitExpressions(Consumer<Expression> visitor) {
        for (RoutineCharacteristic characteristic : characteristics) {
            if (characteristic.getParameterChange() != null
                    && characteristic.getParameterChange().getValues() != null) {
                characteristic.getParameterChange().getValues().forEach(visitor);
            }
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /**
     * Appends this statement to a builder.
     *
     * @param builder the target
     * @param valueRenderer appends each configuration parameter value
     * @return the builder
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> valueRenderer) {
        builder.append("ALTER ").append(routineType).append(' ').append(routine);
        switch (action) {
            case RENAME:
                return builder.append(" RENAME TO ").append(newName);
            case OWNER:
                return builder.append(" OWNER TO ").append(owner);
            case SET_SCHEMA:
                return builder.append(" SET SCHEMA ").append(schemaName);
            case DEPENDS_ON_EXTENSION:
                return builder.append(noDependency ? " NO DEPENDS" : " DEPENDS")
                        .append(" ON EXTENSION ").append(extensionName);
            default:
                for (RoutineCharacteristic characteristic : characteristics) {
                    characteristic.appendTo(builder.append(' '), valueRenderer);
                }
                return restrict ? builder.append(" RESTRICT") : builder;
        }
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
