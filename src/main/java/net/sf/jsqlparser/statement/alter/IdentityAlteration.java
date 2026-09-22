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

    public IdentityDefinition.GenerationMode getGenerationMode() {
        return generationMode;
    }

    public void setGenerationMode(IdentityDefinition.GenerationMode generationMode) {
        this.generationMode = generationMode;
    }

    public List<Sequence.Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Sequence.Parameter> parameters) {
        this.parameters = parameters == null ? null : new ArrayList<>(parameters);
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
