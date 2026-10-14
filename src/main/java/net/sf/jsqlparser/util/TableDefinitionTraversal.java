/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.LikeClause;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.alter.AlterExpressionPartition;
import net.sf.jsqlparser.statement.alter.AlterExpressionPrimaryKey;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.alter.AlterForeignDataOptions;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.DefaultConstraint;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.KeyColumnSource;
import net.sf.jsqlparser.statement.create.table.IndexOptionSource;
import net.sf.jsqlparser.statement.create.table.NotNullConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.TableElement;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import net.sf.jsqlparser.statement.create.table.PartitionBound;

/** Traverses structured table definitions without interpreting legacy raw column options. */
public final class TableDefinitionTraversal {
    private TableDefinitionTraversal() {}

    public static void visit(CreateIndex createIndex, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        accept(createIndex.getTable(), tables);
        if (createIndex.getIndex() != null) {
            visit(createIndex.getIndex(), expressions, tables);
        } else {
            visitOptions(createIndex.getStorageParameters(), expressions);
        }
        accept(createIndex.getWhere(), expressions);
    }

    /**
     * Visits expressions and tables contributing to the rendered ALTER action. Inactive metadata is
     * retained by the AST's getters but is not visited.
     */
    public static void visit(AlterExpression action, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (visitSpecialBody(action, expressions, tables)) {
            return;
        }
        if (action.getOperation() == AlterOperation.RENAME_TABLE
                && action.getOldIndex() == null
                && !(action instanceof net.sf.jsqlparser.statement.alter.AlterExpressionDrop)) {
            accept(action.getNewTable(), tables);
        }
        action.getColumnDefaults()
                .forEach(column -> accept(column.getDefaultExpression(), expressions));
        if (action.hasActiveColumnDefinitions()) {
            action.getColDataTypeList().forEach(column -> visit(column, expressions, tables));
        }
        TableElement definition = action.getActiveTableDefinition();
        if (action.hasActiveConstraintHeader() && definition instanceof KeyColumnSource) {
            visitKeys((KeyColumnSource) definition, expressions);
        } else if (definition != null) {
            visit(definition, expressions, tables);
        }
    }

    private static boolean visitSpecialBody(AlterExpression action,
            Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (action instanceof net.sf.jsqlparser.statement.alter.AlterExpressionOrderBy) {
            ((net.sf.jsqlparser.statement.alter.AlterExpressionOrderBy) action).getOrderByElements()
                    .forEach(element -> accept(element.getExpression(), expressions));
        } else if (action instanceof net.sf.jsqlparser.statement.alter.RelationAlterAction) {
            net.sf.jsqlparser.statement.alter.RelationAlterAction relation =
                    (net.sf.jsqlparser.statement.alter.RelationAlterAction) action;
            relation.visitExpressions(expressions);
            relation.visitTables(tables);
        } else if (action instanceof net.sf.jsqlparser.statement.alter.AlterExpressionTableOption) {
            visit(((net.sf.jsqlparser.statement.alter.AlterExpressionTableOption) action)
                    .getStructuredTableOption(), expressions, tables);
        } else if (action instanceof AlterForeignDataOptions) {
            ForeignDataOption.visitExpressions(((AlterForeignDataOptions) action).getOptions(),
                    expressions);
        } else if (action instanceof AlterExpressionPartition) {
            visitPartitionAction((AlterExpressionPartition) action, expressions, tables);
        } else if (action instanceof AlterExpressionPrimaryKey) {
            AlterExpressionPrimaryKey primaryKey = (AlterExpressionPrimaryKey) action;
            visit(primaryKey.getConstraint(), expressions, tables);
            if (primaryKey.isUsingHash()) {
                accept(primaryKey.getBucketCount(), expressions);
            }
        } else {
            return action instanceof net.sf.jsqlparser.statement.alter.AlterExpressionCharset
                    || action instanceof net.sf.jsqlparser.statement.alter.AlterConstraintAttributes;
        }
        return true;
    }

    private static void visitPartitionAction(AlterExpressionPartition partition,
            Consumer<Expression> expressions, Consumer<Table> tables) {
        switch (partition.getOperation()) {
            case ATTACH_PARTITION:
                accept(partition.getPartitionTable(), tables);
                visit(partition.getPartitionBound(), expressions);
                break;
            case DETACH_PARTITION:
                accept(partition.getPartitionTable(), tables);
                break;
            case EXCHANGE_PARTITION:
                accept(partition.getExchangeTable(), tables);
                break;
            case PARTITION_BY:
                visit(partition.getPartitioning(), expressions);
                break;
            default:
                break;
        }
    }

    public static void visit(CreateTable table, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (table.getTableElements() != null) {
            table.getTableElements().forEach(element -> visit(element, expressions, tables));
        } else {
            if (table.getColumnDefinitions() != null) {
                table.getColumnDefinitions().forEach(column -> visit(column, expressions, tables));
            }
            if (table.getTableConstraints() != null) {
                table.getTableConstraints()
                        .forEach(constraint -> visit(constraint, expressions, tables));
            }
            if (table.getIndexes() != null) {
                table.getIndexes().forEach(index -> visit(index, expressions, tables));
            }
        }
        if (table.getForeignTableOptions() != null) {
            ForeignDataOption.visitExpressions(table.getForeignTableOptions().getOptions(),
                    expressions);
        }
        if (table.getTableOptions() != null) {
            table.getTableOptions().forEach(option -> visit(option, expressions, tables));
        }
        if (table.getInherits() != null) {
            table.getInherits().forEach(parent -> accept(parent, tables));
        }
        if (table.getExecute() != null && table.getExecute().getExprList() != null) {
            accept(table.getExecute().getExprList(), expressions);
        }
        accept(table.getTrailingLikeTable(), tables);
        accept(table.getPartitionOf(), tables);
        visit(table.getPartitioning(), expressions);
        visit(table.getPartitionBound(), expressions);
    }

    /** Shared CREATE/ALTER table option traversal, including COMMENT literals. */
    public static void visit(net.sf.jsqlparser.statement.create.table.TableOption option,
            Consumer<Expression> expressions, Consumer<Table> tables) {
        if (option != null) {
            visitOptions(option.getStorageParameters(), expressions);
            accept(option.getComment(), expressions);
            if (option.getUnionTables() != null) {
                option.getUnionTables().forEach(source -> accept(source, tables));
            }
        }
    }

    /** Visits the active partition key and any subpartition key. Raw bounds remain opaque. */
    public static void visit(TablePartitioning partitioning, Consumer<Expression> expressions) {
        if (partitioning == null) {
            return;
        }
        if (partitioning.getKeyColumns() != null) {
            partitioning.getKeyColumns().forEach(key -> {
                accept(key.getExpression(), expressions);
                visitOptions(key.getOperatorClassParameters(), expressions);
            });
        } else if (partitioning.getExpression() != null) {
            accept(partitioning.getExpression(), expressions);
        } else if (partitioning.getExpressionList() != null) {
            accept(partitioning.getExpressionList(), expressions);
        } else {
            accept(partitioning.getColumns(), expressions);
        }
        visit(partitioning.getSubPartitioning(), expressions);
    }

    /** Visits expressions belonging to the selected PostgreSQL bound type. */
    public static void visit(PartitionBound bound, Consumer<Expression> expressions) {
        if (bound != null) {
            bound.visitExpressions(expressions);
        }
    }

    public static void visit(TableElement element, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (element instanceof LikeClause) {
            accept(((LikeClause) element).getTable(), tables);
        } else if (element instanceof ColumnDefinition) {
            visitColumn((ColumnDefinition) element, expressions, tables);
        } else if (element instanceof NamedConstraint) {
            visitConstraint((NamedConstraint) element, expressions, tables);
        }
        if (element instanceof KeyColumnSource) {
            visitKeys((KeyColumnSource) element, expressions);
        }
        if (element instanceof IndexOptionSource) {
            visitOptions(((IndexOptionSource) element).getIndexOptions().getStorageParameters(),
                    expressions);
        }
    }

    private static void visitConstraint(NamedConstraint element, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (element instanceof NotNullConstraint) {
            accept(((NotNullConstraint) element).getColumn(), expressions);
        } else if (element instanceof CheckConstraint) {
            accept(((CheckConstraint) element).getExpression(), expressions);
        } else if (element instanceof DefaultConstraint) {
            DefaultConstraint constraint = (DefaultConstraint) element;
            accept(constraint.getExpression(), expressions);
            accept(constraint.getColumn(), expressions);
        } else if (element instanceof ExcludeConstraint) {
            accept(((ExcludeConstraint) element).getExpression(), expressions);
        } else if (element instanceof ForeignKeyConstraint) {
            accept(((ForeignKeyConstraint) element).getTable(), tables);
        }
    }

    private static void visitColumn(ColumnDefinition column, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        if (column instanceof AlterExpression.ColumnDataType
                && ((AlterExpression.ColumnDataType) column).getIdentityAlterations() != null) {
            return;
        }
        if (column.getColumnOptions() != null) {
            for (ColumnOption option : column.getColumnOptions()) {
                visitColumnOption(option, expressions, tables);
            }
        }
        if (column instanceof AlterExpression.ColumnDataType) {
            accept(((AlterExpression.ColumnDataType) column).getUsingExpression(), expressions);
        }
    }

    private static void visitColumnOption(ColumnOption option, Consumer<Expression> expressions,
            Consumer<Table> tables) {
        option.visitExpressions(expressions);
        if (option.getKind() == ColumnOption.Kind.REFERENCE
                && option.getForeignKeyReference() != null) {
            accept(option.getForeignKeyReference().getTable(), tables);
        }
        if (option.getKind() == ColumnOption.Kind.CONSTRAINT && option.getConstraint() != null) {
            visit(option.getConstraint(), expressions, tables);
        }
    }

    private static void visitKeys(KeyColumnSource source, Consumer<Expression> expressions) {
        if (source.getColumns() != null) {
            for (KeyElement key : source.getColumns()) {
                accept(key.getExpression(), expressions);
                visitOptions(key.getOperatorClassParameters(), expressions);
            }
        }
    }

    private static void visitOptions(List<IndexOption> options, Consumer<Expression> expressions) {
        if (options != null) {
            options.forEach(option -> accept(option.getValue(), expressions));
        }
    }

    private static <T> void accept(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
