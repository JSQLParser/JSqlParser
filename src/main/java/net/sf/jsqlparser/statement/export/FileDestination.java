/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.export;

import net.sf.jsqlparser.statement.*;

import java.io.Serializable;

public class FileDestination extends FileSourceDestination implements ExportIntoItem, Serializable {
    private ErrorClause errorClause;

    public SourceDestinationType getDestinationType() {
        return getType();
    }

    public void setDestinationType(SourceDestinationType destinationType) {
        setType(destinationType);
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
