/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.sql.Connection;

public abstract class AbstractDatabaseMetaDataCapability implements DatabaseMetaDataValidation {

    protected Connection connection;

    public AbstractDatabaseMetaDataCapability(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean exists(NamedObject o, String name) throws ValidationException {
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
        default:
        }
        throw new UnsupportedOperationException("cannot evaluate for " + o + " and " + name);
    }

    protected boolean databaseExists(String name) throws ValidationException {
        return true;
    }

    protected boolean constraintExists(String name) throws ValidationException {
        return true;
    }

    protected boolean viewExists(String name) throws ValidationException {
        return true;
    }

    protected boolean indexExists(String name) throws ValidationException {
        return true;
    }

    protected boolean schemaExists(String name) throws ValidationException {
        return true;
    }

    protected boolean columnExists(String name) throws ValidationException {
        return true;
    }

    protected boolean tableExists(String name) throws ValidationException {
        return true;
    }

}
