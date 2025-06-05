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

public interface ImportFromItem {
    ErrorClause getErrorClause();

    void setErrorClause(ErrorClause errorClause);
}
