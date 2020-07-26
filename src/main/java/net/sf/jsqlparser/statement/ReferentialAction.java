/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

public enum ReferentialAction {
    CASCADE("CASCADE"),
    RESTRICT("RESTRICT"),
    NO_ACTION("NO ACTION"),
    SET_DEFAULT("SET DEFAULT"),
    SET_NULL("SET NULL");

    private ReferentialAction(String action) {
        this.action = action;
    }

    private String action;

    public static ReferentialAction byAction(String action) {
        for (ReferentialAction a : values()) {
            if (a.getAction().equals(action)) {
                return a;
            }
        }
        return ReferentialAction.valueOf(action);
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return action;
    }
}
