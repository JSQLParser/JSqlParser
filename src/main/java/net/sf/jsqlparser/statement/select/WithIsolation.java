/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;

public class WithIsolation implements Serializable {

    private String isolation = "UR";

    public String getIsolation() {
        return this.isolation;
    }

    public void setIsolation(String s) {
        this.isolation = s;
    }

    @Override
    public String toString() {
        return " WITH " + this.isolation;
    }
}
