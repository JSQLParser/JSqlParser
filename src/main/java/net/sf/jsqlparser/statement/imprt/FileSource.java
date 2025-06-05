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

import net.sf.jsqlparser.statement.*;

import java.io.Serializable;

public class FileSource extends FileSourceDestination implements ImportFromItem, Serializable {
    private ErrorClause errorClause;

    public SourceDestinationType getSourceType() {
        return getType();
    }

    public void setSourceType(SourceDestinationType sourceType) {
        setType(sourceType);
    }

    @Override
    public ErrorClause getErrorClause() {
        return errorClause;
    }

    @Override
    public void setErrorClause(ErrorClause errorClause) {
        this.errorClause = errorClause;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(super.toString());

        if (errorClause != null) {
            sql.append(" ");
            sql.append(errorClause);
        }

        return sql.toString();
    }
}
