/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

public enum Feature {

    upsert, insert, insertWithMulivalue, joinOldOracleSyntax, oraclePriorPosition;

    public boolean isEnabled() {
        return false;
    }

}
