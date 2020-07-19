package net.sf.jsqlparser.util.validation.metadata;

import net.sf.jsqlparser.util.validation.ContextKey;

public enum MetadataContext implements ContextKey {
    /**
     * @see NamedObject
     */
    namedobject,
    /**
     * the fully qualified name
     */
    fqn
}