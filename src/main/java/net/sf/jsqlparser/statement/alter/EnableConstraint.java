/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

public class EnableConstraint implements ConstraintState {

    private boolean disable;

    public EnableConstraint(boolean disable) {
        this.disable = disable;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    @Override
    public String toString() {
        return disable ? "DISABLE" : "ENABLE";
    }
}
