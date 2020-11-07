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

import java.util.List;
import java.util.Objects;

public class Named {

    private final NamedObject namedObject;
    private final String fqn;
    private String alias;
    private List<NamedObject> parents;

    private String fqnLookup;
    private String aliasLookup;

    public Named(NamedObject namedObject, String fqn) {
        Objects.requireNonNull(namedObject, "named object must not be null");
        Objects.requireNonNull(fqn, "fully qualified name must not be null");
        this.namedObject = namedObject;
        this.fqn = fqn;
    }

    public String getFqn() {
        return fqn;
    }

    public String getAlias() {
        return alias;
    }

    public Named setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public NamedObject getNamedObject() {
        return namedObject;
    }

    public List<NamedObject> getParents() {
        return parents;
    }

    public Named setParents(List<NamedObject> parents) {
        this.parents = parents;
        return this;
    }

    public Named setFqnLookup(String fqnLookup) {
        this.fqnLookup = fqnLookup;
        return this;
    }

    public Named setAliasLookup(String aliasLookup) {
        this.aliasLookup = aliasLookup;
        return this;
    }

    /**
     * @return the fqn transformed for catalog-lookup (uppercase/lowercase/..
     *         depends on database)
     */
    public String getFqnLookup() {
        return fqnLookup;
    }

    /**
     * @return the alias transformed for catalog-lookup (uppercase/lowercase/..
     *         depends on database)
     */
    public String getAliasLookup() {
        return aliasLookup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + fqn.hashCode();
        result = prime * result + namedObject.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Named other = (Named) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (!fqn.equals(other.fqn)) {
            return false;
        }
        if (namedObject != other.namedObject) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Named [namedObject=" + namedObject + ", fqn=" + fqn + ", alias=" + alias + ", parents=" + parents + "]";
    }


}
