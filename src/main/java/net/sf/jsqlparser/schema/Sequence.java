/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Represents the database type for a {@code SEQUENCE}
 */
public class Sequence extends ASTNodeAccessImpl implements MultiPartName {

    private String name;
    private String schemaName;
    private Database database;

    private Long incrementBy;

    private CacheMode cacheMode;
    private Long cache;


    public Sequence() {
    }

    public Sequence(String name) {
        this.name = name;
    }

    public Sequence(String schemaName, String name) {
        this.schemaName = schemaName;
        this.schemaName = schemaName;
        this.name = name;
    }

    public Sequence(Database database, String schemaName, String name) {
        this.database = database;
        this.schemaName = schemaName;
        this.name = name;
    }

    public void setIncrementBy(Long incrementBy) {
        this.incrementBy = incrementBy;
    }

    public Long getIncrementBy() {
        return incrementBy;
    }

    public void setCacheMode(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    /**
     * @return the Cache attribute, null if one was not specified.
     */
    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCache(Long cache) {
        this.cache = cache;
    }

    /**
     * @return the value for Cache, returns null if a {@code CACHE|NOCACHE} clause was not specified or the {@link #getCacheMode()} is #NOCACHE
     */
    public Long getCache() {
        return cache;
    }

    @Override
    public String getFullyQualifiedName() {
        String fqn = "";

        if (database != null) {
            fqn += database.getFullyQualifiedName();
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (schemaName != null) {
            fqn += schemaName;
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (name != null) {
            fqn += name;
        }

        return fqn;
    }

    /**
     * Caching mode between CACHE and NOCACHE, when the mode is CACHE a value will exist in Cache
     */
    public enum CacheMode {
        CACHE, NOCACHE
    }
}
