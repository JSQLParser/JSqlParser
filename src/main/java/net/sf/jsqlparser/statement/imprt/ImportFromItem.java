/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.imprt;

import net.sf.jsqlparser.statement.ErrorClause;

public abstract class ImportFromItem {
    protected ErrorClause errorClause;

    public ErrorClause getErrorClause() {
        return errorClause;
    }

    public void setErrorClause(ErrorClause errorClause) {
        this.errorClause = errorClause;
    }
}
