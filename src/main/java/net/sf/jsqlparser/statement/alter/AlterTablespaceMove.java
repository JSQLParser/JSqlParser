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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** Moves all matching relations from one PostgreSQL tablespace to another. */
public class AlterTablespaceMove implements Statement {
    public enum ObjectType {
        TABLE, INDEX, MATERIALIZED_VIEW
    }

    private ObjectType objectType;
    private String sourceTablespace;
    private String targetTablespace;
    private List<String> owners = new ArrayList<>();
    private boolean noWait;

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getSourceTablespace() {
        return sourceTablespace;
    }

    public void setSourceTablespace(String sourceTablespace) {
        this.sourceTablespace = sourceTablespace;
    }

    public String getTargetTablespace() {
        return targetTablespace;
    }

    public void setTargetTablespace(String targetTablespace) {
        this.targetTablespace = targetTablespace;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public boolean isNoWait() {
        return noWait;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER ").append(objectType.name().replace('_', ' '))
                .append(" ALL IN TABLESPACE ").append(sourceTablespace);
        if (owners != null && !owners.isEmpty()) {
            builder.append(" OWNED BY ").append(PlainSelect.getStringList(owners));
        }
        builder.append(" SET TABLESPACE ").append(targetTablespace);
        if (noWait) {
            builder.append(" NOWAIT");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
