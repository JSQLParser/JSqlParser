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

import static java.util.stream.Collectors.joining;

import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;

public class CreateIndexDeParser extends AbstractDeParser<CreateIndex> {

    public CreateIndexDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(CreateIndex createIndex) {
        Index index = createIndex.getIndex();

        buffer.append("CREATE ");

        if (index.getType() != null) {
            buffer.append(index.getType());
            buffer.append(" ");
        }

        buffer.append("INDEX ");
        if (createIndex.isUsingIfNotExists()) {
            buffer.append("IF NOT EXISTS ");
        }
        buffer.append(index.getName());

        String using = index.getUsing();
        if (using != null && createIndex.isIndexTypeBeforeOn()) {
            buffer.append(" USING ");
            buffer.append(using);
        }

        buffer.append(" ON ");
        buffer.append(createIndex.getTable().getFullyQualifiedName());

        if (using != null && !createIndex.isIndexTypeBeforeOn()) {
            buffer.append(" USING ");
            buffer.append(using);
        }

        if (index.getColumnsNames() != null) {
            buffer.append(" (");
            buffer.append(index.getColumnWithParams().stream()
                    .map(cp -> cp.columnName
                            + (cp.getParams() != null ? " " + String.join(" ", cp.getParams())
                                    : ""))
                    .collect(joining(", ")));
            buffer.append(")");
        }

        if (createIndex.getTailParameters() != null) {
            for (String param : createIndex.getTailParameters()) {
                buffer.append(" ").append(param);
            }
        }
    }

}
