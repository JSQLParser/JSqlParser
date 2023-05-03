/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

public enum InsertModifierPriority {
    LOW_PRIORITY("LOW_PRIORITY"), DELAYED("DELAYED"), HIGH_PRIORITY("HIGH_PRIORITY"), IGNORE(
            "IGNORE");

    public String getPriority() {
        return priority;
    }

    private final String priority;

    InsertModifierPriority(String priority) {
        this.priority = priority;
    }

    public final static InsertModifierPriority from(String priority) {
        return Enum.valueOf(InsertModifierPriority.class, priority.toUpperCase());
    }
}
