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
import net.sf.jsqlparser.statement.select.PlainSelect;

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
        if (createIndex.isConcurrently()) {
            builder.append("CONCURRENTLY ");
        }
        if (createIndex.isUsingIfNotExists()) {
            builder.append("IF NOT EXISTS ");
        }
        if (index.getName() != null) {
            builder.append(index.getName()).append(" ");
        }

        String using = index.getUsing();
        if (using != null && createIndex.isIndexTypeBeforeOn()) {
            builder.append("USING ").append(using).append(" ");
        }

        builder.append("ON ");
        if (createIndex.isOnly()) {
            builder.append("ONLY ");
        }
        builder.append(createIndex.getTable().getFullyQualifiedName());

        if (using != null && !createIndex.isIndexTypeBeforeOn()) {
            builder.append(" USING ");
            builder.append(using);
        }

        if (index.getColumnsNames() != null) {
            builder.append(" (");
            builder.append(index.getColumnWithParams().stream()
                    .map(Index.ColumnParams::toString)
                    .collect(joining(", ")));
            builder.append(")");
        }

        if (createIndex.getIncludeColumns() != null) {
            builder.append(" INCLUDE (")
                    .append(String.join(", ", createIndex.getIncludeColumns())).append(")");
        }
        if (createIndex.getNullsDistinct() != null) {
            builder.append(" NULLS ")
                    .append(createIndex.getNullsDistinct() ? "DISTINCT" : "NOT DISTINCT");
        }
        if (createIndex.getStorageParameters() != null) {
            builder.append(" WITH ").append(PlainSelect.getStringList(
                    createIndex.getStorageParameters(), true, true));
        }
        if (createIndex.getTableSpace() != null) {
            builder.append(" TABLESPACE ").append(createIndex.getTableSpace());
        }
        if (createIndex.getWhere() != null) {
            builder.append(" WHERE ").append(createIndex.getWhere());
        }

        if (createIndex.getTailParameters() != null) {
            for (String param : createIndex.getTailParameters()) {
                builder.append(" ").append(param);
            }
        }
    }

}
