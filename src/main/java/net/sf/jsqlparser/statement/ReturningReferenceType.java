/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.schema.MultiPartName;

public enum ReturningReferenceType {
    OLD, NEW;

    public static ReturningReferenceType from(String name) {
        String unquoted = MultiPartName.unquote(name);
        if (unquoted == null) {
            return null;
        }
        if ("OLD".equalsIgnoreCase(unquoted)) {
            return OLD;
        }
        if ("NEW".equalsIgnoreCase(unquoted)) {
            return NEW;
        }
        return null;
    }
}
