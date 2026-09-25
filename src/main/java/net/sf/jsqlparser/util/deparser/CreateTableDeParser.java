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
import net.sf.jsqlparser.statement.create.table.TableElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class CreateTableDeParser extends AbstractDeParser<CreateTable> {

    private StatementDeParser statementDeParser;

    public CreateTableDeParser(StringBuilder buffer) {
        this(new StatementDeParser(buffer), buffer);
    }

    public CreateTableDeParser(StatementDeParser statementDeParser, StringBuilder buffer) {
        super(buffer);
        this.statementDeParser = statementDeParser;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(CreateTable createTable) {
        TableElementDeParser elements =
                new TableElementDeParser(builder, statementDeParser.getExpressionDeParser());
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
        if (createTable.getOfType() != null) {
            builder.append(" OF ").append(createTable.getOfType());
        }
        if (createTable.getPartitionOf() != null) {
            builder.append(" PARTITION OF ")
                    .append(createTable.getPartitionOf().getFullyQualifiedName());
        }
        if (createTable.getCloneTable() != null) {
            builder.append(" CLONE ").append(createTable.getCloneTable());
        }

        if (createTable.getColumns() != null && !createTable.getColumns().isEmpty()) {
            builder.append(" (");
            Iterator<String> columnIterator = createTable.getColumns().iterator();
            builder.append(columnIterator.next());
            while (columnIterator.hasNext()) {
                builder.append(", ").append(columnIterator.next());
            }
            builder.append(")");
        }
        if (createTable.getTableElements() != null) {
            builder.append(" (");
            for (Iterator<TableElement> iter = createTable.getTableElements().iterator(); iter
                    .hasNext();) {
                elements.deParse(iter.next());
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        } else if (createTable.getColumnDefinitions() != null) {
            builder.append(" (");
            for (Iterator<ColumnDefinition> iter =
                    createTable.getColumnDefinitions().iterator(); iter.hasNext();) {
                ColumnDefinition columnDefinition = iter.next();
                elements.deParse(columnDefinition);

                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }

            if (createTable.getIndexes() != null) {
                for (Index index : createTable.getIndexes()) {
                    builder.append(", ");
                    elements.deParse(index);
                }
            }

            builder.append(")");
        }

        createTable.appendInheritanceTo(builder);
        if (createTable.getPartitionBound() != null) {
            builder.append(' ');
            createTable.getPartitionBound().appendTo(builder,
                    expression -> expression.accept(statementDeParser.getExpressionDeParser(),
                            null));
        }

        if (createTable.getPartitioning() != null && createTable.isTableOptionsAfterPartition()) {
            builder.append(' ');
            createTable.getPartitioning().appendTo(builder,
                    expression -> expression.accept(statementDeParser.getExpressionDeParser(),
                            null));
        }
        createTable.appendTableOptionsTo(builder,
                expression -> expression.accept(statementDeParser.getExpressionDeParser(), null));
        if (createTable.getPartitioning() != null && !createTable.isTableOptionsAfterPartition()) {
            builder.append(' ');
            createTable.getPartitioning().appendTo(builder,
                    expression -> expression.accept(statementDeParser.getExpressionDeParser(),
                            null));
        }

        if (createTable.getRowMovement() != null) {
            builder.append(' ').append(createTable.getRowMovement().getMode().toString())
                    .append(" ROW MOVEMENT");
        }
        createTable.appendQueryTo(builder, select -> select.accept(this.statementDeParser, null),
                execute -> execute.accept(this.statementDeParser, null));
        if (createTable.getTrailingLikeTable() != null) {
            builder.append(" LIKE ");
            if (createTable.isSelectParenthesis()) {
                builder.append("(");
            }
            Table table = createTable.getTrailingLikeTable();
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
