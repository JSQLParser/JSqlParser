/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

public enum UpdateModifierPriority {
    LOW_PRIORITY;

    public static UpdateModifierPriority from(String priority) {
        return Enum.valueOf(UpdateModifierPriority.class, priority.toUpperCase());
    }
}
