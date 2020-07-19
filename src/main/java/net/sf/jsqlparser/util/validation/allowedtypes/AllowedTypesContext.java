package net.sf.jsqlparser.util.validation.allowedtypes;

import net.sf.jsqlparser.util.validation.ContextKey;

public enum AllowedTypesContext implements ContextKey {
    /**
     * a collection of allowed {@link Class}es
     */
    allowed_types,
    /**
     * the object given (may be null)
     */
    argument,
    /**
     * a boolean, default = true
     */
    allow_null
}