/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;

/** An exclusion operator, optionally qualified and enclosed in PostgreSQL OPERATOR(...). */
public class ExclusionOperator implements Serializable {
    private String schemaName;
    private String name;
    private boolean useOperatorKeyword;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseOperatorKeyword() {
        return useOperatorKeyword;
    }

    public void setUseOperatorKeyword(boolean useOperatorKeyword) {
        this.useOperatorKeyword = useOperatorKeyword;
    }

    public ExclusionOperator withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    public ExclusionOperator withName(String name) {
        setName(name);
        return this;
    }

    public ExclusionOperator withUseOperatorKeyword(boolean useOperatorKeyword) {
        setUseOperatorKeyword(useOperatorKeyword);
        return this;
    }

    /** Returns the qualified name without an OPERATOR wrapper, preserving identifier quotes. */
    public String getQualifiedName() {
        return (schemaName == null ? "" : schemaName + ".") + name;
    }

    @Override
    public String toString() {
        return useOperatorKeyword ? "OPERATOR(" + getQualifiedName() + ")" : getQualifiedName();
    }
}
