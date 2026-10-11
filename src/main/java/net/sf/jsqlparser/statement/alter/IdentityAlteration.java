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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.create.table.IdentityDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** An action on an identity column, as opposed to a change of its SQL data type. */
public class IdentityAlteration implements Serializable {
    public enum Kind {
        ADD, SET_GENERATED, SET_PARAMETER, RESTART, DROP
    }

    private final Kind kind;
    private IdentityDefinition identityDefinition;
    private IdentityDefinition.GenerationMode generationMode;
    private List<Sequence.Parameter> parameters;
    private Long restartWith;
    private boolean ifExists;

    public IdentityAlteration(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public IdentityDefinition getIdentityDefinition() {
        return identityDefinition;
    }

    public void setIdentityDefinition(IdentityDefinition identityDefinition) {
        this.identityDefinition = identityDefinition;
    }

    /**
     * Returns ADD's definition mode or SET GENERATED's mode. An ADD without a definition returns
     * null; no definition is created by reading it.
     */
    public IdentityDefinition.GenerationMode getGenerationMode() {
        return kind == Kind.ADD
                ? identityDefinition == null ? null : identityDefinition.getGenerationMode()
                : generationMode;
    }

    /**
     * Updates ADD's existing definition or SET GENERATED's mode. A non-null ADD mode creates a
     * definition when absent; a null mode never creates one. Other kinds retain the value as
     * inactive metadata and do not render it.
     */
    public void setGenerationMode(IdentityDefinition.GenerationMode generationMode) {
        if (kind == Kind.ADD) {
            if (identityDefinition != null) {
                identityDefinition.setGenerationMode(generationMode);
            } else if (generationMode != null) {
                identityDefinition = new IdentityDefinition(generationMode);
            }
        } else {
            this.generationMode = generationMode;
        }
    }

    /**
     * Returns ADD's definition parameters or SET_PARAMETER's parameters. The returned list belongs
     * to the active payload and can be edited directly; an omitted list is null.
     */
    public List<Sequence.Parameter> getParameters() {
        return kind == Kind.ADD
                ? identityDefinition == null ? null : identityDefinition.getParameters()
                : parameters;
    }

    /**
     * Updates ADD's definition parameters or SET_PARAMETER's parameters. A non-null ADD list
     * creates a BY DEFAULT definition when absent; a null list never creates one. Other kinds
     * retain the value as inactive metadata and do not render it.
     */
    public void setParameters(List<Sequence.Parameter> parameters) {
        if (kind == Kind.ADD) {
            if (identityDefinition == null && parameters != null) {
                identityDefinition =
                        new IdentityDefinition(IdentityDefinition.GenerationMode.BY_DEFAULT);
            }
            if (identityDefinition != null) {
                identityDefinition.setParameters(parameters);
            }
        } else {
            this.parameters = parameters == null ? null : new ArrayList<>(parameters);
        }
    }

    /** Returns the restart value, or null for a bare RESTART using the sequence start value. */
    public Long getRestartWith() {
        return restartWith;
    }

    public void setRestartWith(Long restartWith) {
        this.restartWith = restartWith;
    }

    public IdentityAlteration withRestartWith(Long restartWith) {
        setRestartWith(restartWith);
        return this;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public String toString() {
        switch (kind) {
            case ADD:
                return "ADD " + identityDefinition;
            case SET_GENERATED:
                return "SET GENERATED "
                        + (generationMode == IdentityDefinition.GenerationMode.ALWAYS
                                ? "ALWAYS"
                                : "BY DEFAULT");
            case SET_PARAMETER:
                return "SET " + PlainSelect.getStringList(parameters, false, false);
            case RESTART:
                return "RESTART" + (restartWith == null ? "" : " WITH " + restartWith);
            case DROP:
                return "DROP IDENTITY" + (ifExists ? " IF EXISTS" : "");
            default:
                throw new IllegalStateException("Unknown identity action: " + kind);
        }
    }
}
