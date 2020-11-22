/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.metadata;

public enum NamedObject {
    /**
     * a name constisting of max. 1 identifiers, i.e. [database]
     */
    database,
    /**
     * a name constisting of max. 2 identifiers, i.e. [database].[schema]
     */
    schema,
    /**
     * a name constisting of max. 3 identifiers, i.e. [catalog].[schema].[table]
     */
    table,
    /**
     * a name constisting of max. 3 identifiers, i.e. [catalog].[schema].[view]
     */
    view,
    /**
     * a name constisting of min 2 (the table-reference) and max. 4 identifiers,
     * i.e. [catalog].[schema].[table].[columnName]
     */
    column,
    index,
    constraint,
    uniqueConstraint,
    /**
     * a name constisting of max. 3 identifiers, i.e. [catalog].[schema].[sequence]
     */
    sequence,
    synonym,
    procedure,
    user,
    role,
    trigger,
    alias;

    public boolean equalsIgnoreCase(String name) {
        return name().equalsIgnoreCase(name);
    }

    /**
     * @param name
     * @return <code>null</code>, if not found, otherwise the
     *         {@link NamedObject}
     */
    public static NamedObject forName(String name) {
        for (NamedObject o : values()) {
            if (o.equalsIgnoreCase(name)) {
                return o;
            }
        }
        return null;
    }
}
