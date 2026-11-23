/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.type;

import net.sf.jsqlparser.statement.create.table.ColDataType;

/** A type defined as an alias for a data type. */
public class AliasTypeDefinition implements TypeDefinition {
    private final ColDataType dataType;

    public AliasTypeDefinition(ColDataType dataType) {
        this.dataType = dataType;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        return dataType.toString();
    }
}
