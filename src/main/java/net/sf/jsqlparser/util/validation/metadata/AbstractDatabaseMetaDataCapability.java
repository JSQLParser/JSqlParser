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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

/**
 * Adapter class always throwing {@link UnsupportedOperationException} for all
 * exists - methods.
 *
 * @author gitmotte
 */
public abstract class AbstractDatabaseMetaDataCapability implements DatabaseMetaDataValidation {

    protected Connection connection;

    protected boolean cacheResults;

    protected Map<Named, Boolean> results = new HashMap<>();

    protected UnaryOperator<String> namesLookup = NamesLookup.NO_TRANSFORMATION;

    /**
     * With caching enabled - see {@link #isCacheResults()}
     *
     * @param connection
     * @param namesLookup - see {@link NamesLookup}
     * @see #AbstractDatabaseMetaDataCapability(Connection, UnaryOperator, boolean)
     */
    public AbstractDatabaseMetaDataCapability(Connection connection, UnaryOperator<String> namesLookup) {
        this(connection, namesLookup, true);
    }

    /**
     * @param connection
     * @param namesLookup  - see {@link NamesLookup}
     * @param cacheResults - whether the results should be cached for later lookups
     * @see #AbstractDatabaseMetaDataCapability(Connection, UnaryOperator)
     */
    public AbstractDatabaseMetaDataCapability(Connection connection, UnaryOperator<String> namesLookup, boolean cacheResults) {
        this.connection = connection;
        this.namesLookup = namesLookup;
        this.cacheResults = cacheResults;
    }

    public UnaryOperator<String> getNamesLookup() {
        return namesLookup;
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
    @SuppressWarnings({ "PMD.CyclomaticComplexity" })
    public final boolean exists(Named named) {
        Objects.requireNonNull(named);
        named.setFqnLookup(getNamesLookup().apply(named.getFqn()));
        named.setAliasLookup(getNamesLookup().apply(named.getAlias()));
        switch(named.getNamedObject()) {
            case table:
                return cache(named, this::tableExists);
            case column:
                return cache(named, this::columnExists);
            case schema:
                return cache(named, this::schemaExists);
            case index:
                return cache(named, this::indexExists);
            case database:
                return cache(named, this::databaseExists);
            case constraint:
            case uniqueConstraint:
                return cache(named, this::constraintExists);
            case view:
                return cache(named, this::viewExists);
            case procedure:
                return cache(named, this::procedureExists);
            case user:
                return cache(named, this::userExists);
            case role:
                return cache(named, this::roleExists);
            default:
        }
        throw new UnsupportedOperationException(named.getFqn() + ": evaluation of " + named.getNamedObject() + "-name not implemented.");
    }

    protected boolean cache(Named named, BiPredicate<Map<Named, Boolean>, Named> fn) {
        Map<Named, Boolean> m = Collections.unmodifiableMap(results);
        if (cacheResults) {
            return results.computeIfAbsent(named, k -> fn.test(m, k));
        } else {
            return fn.test(m, named);
        }
    }

    protected boolean roleExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean userExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean procedureExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean databaseExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean constraintExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean viewExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean indexExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean schemaExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean columnExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected boolean tableExists(Map<Named, Boolean> results, Named name) {
        throw unsupported(name);
    }

    protected UnsupportedOperationException unsupported(Named name) {
        return new UnsupportedOperationException(name.getFqn() + ": evaluation of " + name.getNamedObject() + "-name not supported.");
    }
}
