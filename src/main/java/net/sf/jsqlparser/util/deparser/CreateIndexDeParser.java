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

        builder.append("CREATE ");

        if (index.getType() != null) {
            builder.append(index.getType());
            builder.append(" ");
        }

        builder.append("INDEX ");
        if (createIndex.isUsingIfNotExists()) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(index.getName());

        String using = index.getUsing();
        if (using != null && createIndex.isIndexTypeBeforeOn()) {
            builder.append(" USING ");
            builder.append(using);
        }

        builder.append(" ON ");
        builder.append(createIndex.getTable().getFullyQualifiedName());

        if (using != null && !createIndex.isIndexTypeBeforeOn()) {
            builder.append(" USING ");
            builder.append(using);
        }

        if (index.getColumnsNames() != null) {
            builder.append(" (");
            builder.append(index.getColumnWithParams().stream()
                    .map(cp -> cp.columnName
                            + (cp.getParams() != null ? " " + String.join(" ", cp.getParams())
                                    : ""))
                    .collect(joining(", ")));
            builder.append(")");
        }

        if (createIndex.getTailParameters() != null) {
            for (String param : createIndex.getTailParameters()) {
                builder.append(" ").append(param);
            }
        }
    }

}
