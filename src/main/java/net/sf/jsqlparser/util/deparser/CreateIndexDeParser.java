/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;

import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;

public class CreateIndexDeParser {

    protected StringBuilder buffer;

    public CreateIndexDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(CreateIndex createIndex) {
        Index index = createIndex.getIndex();

        buffer.append("CREATE ");

        if (index.getType() != null) {
            buffer.append(index.getType());
            buffer.append(" ");
        }

        buffer.append("INDEX ");
        buffer.append(index.getName());
        buffer.append(" ON ");
        buffer.append(createIndex.getTable().getFullyQualifiedName());

        if (index.getColumnsNames() != null) {
            buffer.append(" (");
            for (Iterator iter = index.getColumnsNames().iterator(); iter.hasNext();) {
                String columnName = (String) iter.next();
                buffer.append(columnName);

                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

}
