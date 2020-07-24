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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Adapter class always throwing {@link UnsupportedOperationException} for all
 * exists - methods.
 *
 * @author gitmotte
 *
 */
public abstract class AbstractDatabaseMetaDataCapability implements DatabaseMetaDataValidation {

    protected Connection connection;
    protected boolean cacheResults;
    protected Map<CacheKey, Boolean> results = new HashMap<>();

    protected static class CacheKey {
        final NamedObject o;
        final String name;

        public CacheKey(NamedObject o, String name) {
            this.o = o;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return o.hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                CacheKey other = (CacheKey) obj;
                return o.equals(other.o) && name.equals(other.name);
            } else {
                return false;
            }
        }

    }

    public AbstractDatabaseMetaDataCapability(Connection connection) {
        this(connection, true);
    }

    public AbstractDatabaseMetaDataCapability(Connection connection, boolean cacheResults) {
        this.connection = connection;
        this.cacheResults = cacheResults;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isCacheResults() {
        return cacheResults;
    }

    public AbstractDatabaseMetaDataCapability clearCache() {
        this.results.clear();
        return this;
    }

    @Override
    public final boolean exists(NamedObject o, String name) {
        Objects.requireNonNull(o);
        Objects.requireNonNull(name);
        switch (o) {
        case table:
            return cache(o, name, this::tableExists);
        case column:
            return cache(o, name, this::columnExists);
        case schema:
            return cache(o, name, this::schemaExists);
        case index:
            return cache(o, name, this::indexExists);
        case database:
            return cache(o, name, this::databaseExists);
        case constraint:
        case uniqueConstraint:
            return cache(o, name, this::constraintExists);
        case view:
            return cache(o, name, this::viewExists);
        case procedure:
            return cache(o, name, this::procedureExists);
        case user:
            return cache(o, name, this::userExists);
        case role:
            return cache(o, name, this::roleExists);
        default:
        }
        throw new UnsupportedOperationException(name + ": evaluation of " + o + "-name not implemented.");
    }

    protected boolean cache(NamedObject o, String name, Predicate<String> fn) {
        if (cacheResults) {
            return results.computeIfAbsent(new CacheKey(o, name), k -> fn.test(k.name));
        } else {
            return fn.test(name);
        }
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
