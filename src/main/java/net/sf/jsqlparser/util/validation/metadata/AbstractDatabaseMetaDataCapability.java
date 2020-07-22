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

import java.sql.Connection;

/**
 * Adapter class always throwing {@link UnsupportedOperationException} for all
 * exists - methods.
 *
 * @author gitmotte
 *
 */
public abstract class AbstractDatabaseMetaDataCapability implements DatabaseMetaDataValidation {

    protected Connection connection;

    public AbstractDatabaseMetaDataCapability(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean exists(NamedObject o, String name) {
        switch (o) {
        case table:
            return tableExists(name);
        case column:
            return columnExists(name);
        case schema:
            return schemaExists(name);
        case index:
            return indexExists(name);
        case database:
            return databaseExists(name);
        case constraint:
        case uniqueConstraint:
            return constraintExists(name);
        case view:
            return viewExists(name);
        case procedure:
            return procedureExists(name);
        case user:
            return userExists(name);
        case role:
            return roleExists(name);
        default:
        }
        throw new UnsupportedOperationException(name + ": evaluation of " + o + "-name not implemented.");
    }

    protected boolean roleExists(String name) {
        throw unsupported(NamedObject.role, name);
    }

    protected boolean userExists(String name) {
        throw unsupported(NamedObject.user, name);
    }

    protected boolean procedureExists(String name) {
        throw unsupported(NamedObject.procedure, name);
    }

    protected boolean databaseExists(String name) {
        throw unsupported(NamedObject.database, name);
    }

    protected boolean constraintExists(String name) {
        throw unsupported(NamedObject.constraint, name);
    }

    protected boolean viewExists(String name) {
        throw unsupported(NamedObject.view, name);
    }

    protected boolean indexExists(String name) {
        throw unsupported(NamedObject.index, name);
    }

    protected boolean schemaExists(String name) {
        throw unsupported(NamedObject.schema, name);
    }

    protected boolean columnExists(String name) {
        throw unsupported(NamedObject.column, name);
    }

    protected boolean tableExists(String name) {
        throw unsupported(NamedObject.table, name);
    }

    protected UnsupportedOperationException unsupported(NamedObject namedObject, String name) {
        return new UnsupportedOperationException(name + ": evaluation of " + namedObject + "-name not supported.");
    }

}
