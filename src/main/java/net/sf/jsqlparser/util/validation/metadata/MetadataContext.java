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
