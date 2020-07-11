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
    public void deParse(CreateTable createTable) {
        buffer.append("CREATE ");
        if (createTable.isUnlogged()) {
            buffer.append("UNLOGGED ");
        }
        String params = PlainSelect.getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(' ');
        }

        buffer.append("TABLE ");
        if (createTable.isIfNotExists()) {
            buffer.append("IF NOT EXISTS ");
        }
        buffer.append(createTable.getTable().getFullyQualifiedName());

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

        params = PlainSelect.getStringList(createTable.getTableOptionsStrings(), false, false);
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
    }

}
