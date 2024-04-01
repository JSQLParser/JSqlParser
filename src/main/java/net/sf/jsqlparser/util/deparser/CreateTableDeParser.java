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
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;

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
        buffer.append("CREATE ");
        if (createTable.isOrReplace()) {
            buffer.append("OR REPLACE ");
        }
        if (createTable.isUnlogged()) {
            buffer.append("UNLOGGED ");
        }
        String params = SelectUtils.getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(' ');
        }

        buffer.append("TABLE ");
        if (createTable.isIfNotExists()) {
            buffer.append("IF NOT EXISTS ");
        }
        buffer.append(createTable.getTable().getFullyQualifiedName());

        if (createTable.getColumns() != null && !createTable.getColumns().isEmpty()) {
            buffer.append(" (");
            Iterator<String> columnIterator = createTable.getColumns().iterator();
            buffer.append(columnIterator.next());
            while (columnIterator.hasNext()) {
                buffer.append(", ").append(columnIterator.next());
            }
            buffer.append(")");
        }
        if (createTable.getColumnDefinitions() != null) {
            buffer.append(" (");
            for (Iterator<ColumnDefinition> iter = createTable.getColumnDefinitions().iterator(); iter.hasNext();) {
                ColumnDefinition columnDefinition = iter.next();
                buffer.append(columnDefinition.getColumnName());
                buffer.append(" ");
                buffer.append(columnDefinition.getColDataType().toString());
                if (columnDefinition.getColumnSpecs() != null) {
                    for (String s : columnDefinition.getColumnSpecs()) {
                        buffer.append(" ");
                        buffer.append(s);
                    }
                }

                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }

            if (createTable.getIndexes() != null) {
                for (Index index : createTable.getIndexes()) {
                    buffer.append(", ");
                    buffer.append(index.toString());
                }
            }

            buffer.append(")");
        }

        params = SelectUtils.getStringList(createTable.getTableOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(' ').append(params);
        }

        if (createTable.getRowMovement() != null) {
            buffer.append(' ').append(createTable.getRowMovement().getMode().toString()).append(" ROW MOVEMENT");
        }
        if (createTable.getSelect() != null) {
            buffer.append(" AS ");
            if (createTable.isSelectParenthesis()) {
                buffer.append("(");
            }
            Select sel = createTable.getSelect();
            sel.accept(this.statementDeParser);
            if (createTable.isSelectParenthesis()) {
                buffer.append(")");
            }
        }
        if (createTable.getLikeTable() != null) {
            buffer.append(" LIKE ");
            if (createTable.isSelectParenthesis()) {
                buffer.append("(");
            }
            Table table = createTable.getLikeTable();
            buffer.append(table.getFullyQualifiedName());
            if (createTable.isSelectParenthesis()) {
                buffer.append(")");
            }
        }
        if (createTable.getSpannerInterleaveIn() != null) {
            buffer.append(", ").append(createTable.getSpannerInterleaveIn());
        }
    }

}
