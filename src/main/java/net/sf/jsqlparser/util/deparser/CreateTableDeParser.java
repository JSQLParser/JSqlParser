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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class CreateTableDeParser extends AbstractDeParser<CreateTable> {

    private StatementDeParser statementDeParser;

    public CreateTableDeParser(StringBuilder buffer) {
        super(buffer);
    }

    public CreateTableDeParser(StatementDeParser statementDeParser, StringBuilder buffer) {
        super(buffer);
        this.statementDeParser = statementDeParser;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(CreateTable createTable) {
        builder.append("CREATE ");
        if (createTable.isOrReplace()) {
            builder.append("OR REPLACE ");
        }
        if (createTable.isUnlogged()) {
            builder.append("UNLOGGED ");
        }
        String params =
                PlainSelect.getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!params.isEmpty()) {
            builder.append(params).append(' ');
        }

        builder.append("TABLE ");
        if (createTable.isIfNotExists()) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(createTable.getTable().getFullyQualifiedName());

        if (createTable.getColumns() != null && !createTable.getColumns().isEmpty()) {
            builder.append(" (");
            Iterator<String> columnIterator = createTable.getColumns().iterator();
            builder.append(columnIterator.next());
            while (columnIterator.hasNext()) {
                builder.append(", ").append(columnIterator.next());
            }
            builder.append(")");
        }
        if (createTable.getColumnDefinitions() != null) {
            builder.append(" (");
            for (Iterator<ColumnDefinition> iter =
                    createTable.getColumnDefinitions().iterator(); iter.hasNext();) {
                ColumnDefinition columnDefinition = iter.next();
                builder.append(columnDefinition.getColumnName());
                builder.append(" ");
                builder.append(columnDefinition.getColDataType().toString());
                if (columnDefinition.getColumnSpecs() != null) {
                    for (String s : columnDefinition.getColumnSpecs()) {
                        builder.append(" ");
                        builder.append(s);
                    }
                }

                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }

            if (createTable.getIndexes() != null) {
                for (Index index : createTable.getIndexes()) {
                    builder.append(", ");
                    builder.append(index.toString());
                }
            }

            builder.append(")");
        }

        params = PlainSelect.getStringList(createTable.getTableOptionsStrings(), false, false);
        if (!"".equals(params)) {
            builder.append(' ').append(params);
        }

        if (createTable.getRowMovement() != null) {
            builder.append(' ').append(createTable.getRowMovement().getMode().toString())
                    .append(" ROW MOVEMENT");
        }
        if (createTable.getSelect() != null) {
            builder.append(" AS ");
            if (createTable.isSelectParenthesis()) {
                builder.append("(");
            }
            Select sel = createTable.getSelect();
            sel.accept(this.statementDeParser, null);
            if (createTable.isSelectParenthesis()) {
                builder.append(")");
            }
        }
        if (createTable.getLikeTable() != null) {
            builder.append(" LIKE ");
            if (createTable.isSelectParenthesis()) {
                builder.append("(");
            }
            Table table = createTable.getLikeTable();
            builder.append(table.getFullyQualifiedName());
            if (createTable.isSelectParenthesis()) {
                builder.append(")");
            }
        }
        if (createTable.getSpannerInterleaveIn() != null) {
            builder.append(", ").append(createTable.getSpannerInterleaveIn());
        }
    }

}
