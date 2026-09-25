/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.expression.SpannerInterleaveIn;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.LikeClause;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class CreateTable implements Statement {

    private boolean tableOptionsAfterPartition;

    public boolean isTableOptionsAfterPartition() {
        return tableOptionsAfterPartition;
    }

    public void setTableOptionsAfterPartition(boolean tableOptionsAfterPartition) {
        this.tableOptionsAfterPartition = tableOptionsAfterPartition;
    }

    private net.sf.jsqlparser.statement.execute.Execute execute;

    /** Prepared statement source of PostgreSQL CREATE TABLE AS EXECUTE. */
    public net.sf.jsqlparser.statement.execute.Execute getExecute() {
        return execute;
    }

    public void setExecute(net.sf.jsqlparser.statement.execute.Execute execute) {
        this.execute = execute;
        select = null;
        selectParenthesis = false;
    }

    private Table table;
    private boolean unlogged = false;
    private List<String> createOptionsStrings;
    private List<String> tableOptionsStrings;
    private List<TableOption> tableOptions;
    private List<ColumnDefinition> columnDefinitions;
    private List<String> columns;
    private List<Index> indexes;
    private List<TableElement> tableElements;
    private Select select;
    private DuplicateHandling duplicateHandling;
    private Table likeTable;
    private Table cloneTable;
    private List<Table> inherits;
    private ColDataType ofType;
    private boolean selectParenthesis;
    private boolean useAsKeyword = true;
    private Boolean withData;
    private boolean ifNotExists = false;
    private boolean orReplace = false;
    private TablePartitioning partitioning;
    private Table partitionOf;
    private PartitionBound partitionBound;

    private RowMovement rowMovement;

    private SpannerInterleaveIn interleaveIn = null;

    public enum DuplicateHandling {
        IGNORE, REPLACE
    }

    /** MySQL's duplicate-key handling when creating a table from a query; null if omitted. */
    public DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(DuplicateHandling duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isUnlogged() {
        return unlogged;
    }

    public void setUnlogged(boolean unlogged) {
        this.unlogged = unlogged;
    }

    /**
     * @return a list of {@link ColumnDefinition}s of this table. When ordered table elements are
     *         present, this is a mutable view of the column definitions in that list.
     */
    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List<ColumnDefinition> list) {
        if (tableElements == null) {
            columnDefinitions = list;
        } else {
            TableElementList.replace(tableElements, ColumnDefinition.class, list);
        }
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * @return a list of options (as simple strings) of this table definition, as ("TYPE", "=",
     *         "MYISAM"). For typed options, this is a snapshot of their current tokens; use
     *         {@link #getTableOptions()} to edit the structured options or
     *         {@link #setTableOptionsStrings(List)} to replace them with raw options.
     */
    public List<String> getTableOptionsStrings() {
        if (tableOptions == null) {
            return tableOptionsStrings;
        }
        List<String> tokens = new ArrayList<>();
        for (TableOption option : tableOptions) {
            tokens.addAll(option.getTokens());
        }
        return tokens;
    }

    public void setTableOptionsStrings(List<String> tableOptionsStrings) {
        this.tableOptionsStrings = tableOptionsStrings;
        tableOptions = null;
    }

    /** Returns typed table options in source order. */
    public List<TableOption> getTableOptions() {
        return tableOptions;
    }

    public void setTableOptions(List<TableOption> tableOptions) {
        this.tableOptions = tableOptions;
        tableOptionsStrings = null;
    }

    /** Returns the first option of the requested kind, if present. */
    public Optional<TableOption> getTableOption(TableOption.Kind kind) {
        return Optional.ofNullable(tableOptions).orElseGet(Collections::emptyList).stream()
                .filter(option -> option.getKind() == kind)
                .findFirst();
    }

    public List<String> getCreateOptionsStrings() {
        return createOptionsStrings;
    }

    public void setCreateOptionsStrings(List<String> createOptionsStrings) {
        this.createOptionsStrings = createOptionsStrings;
    }

    /**
     * @return a list of {@link Index}es (for example "PRIMARY KEY") of this table.<br>
     *         Indexes created with column definitions (as in mycol INT PRIMARY KEY) are not
     *         inserted into this list. When ordered table elements are present, this is a mutable
     *         view of their indexes.
     */
    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> list) {
        if (tableElements == null) {
            indexes = list;
        } else {
            TableElementList.replace(tableElements, Index.class, list);
        }
    }

    /**
     * Returns columns, constraints, and indexes in the order in which they were declared.
     */
    public List<TableElement> getTableElements() {
        return tableElements;
    }

    public void setTableElements(List<TableElement> tableElements) {
        this.tableElements = tableElements;
        if (tableElements == null) {
            columnDefinitions = null;
            indexes = null;
            return;
        }
        columnDefinitions = new TableElementList<>(tableElements, ColumnDefinition.class);
        indexes = new TableElementList<>(tableElements, Index.class);
    }

    /** Returns table elements of a requested AST type while preserving their declaration order. */
    public <E extends TableElement> List<E> getTableElements(Class<E> type) {
        if (tableElements == null) {
            return Collections.emptyList();
        }
        List<E> result = new ArrayList<>();
        for (TableElement element : tableElements) {
            if (type.isInstance(element)) {
                result.add(type.cast(element));
            }
        }
        return result;
    }

    public Select getSelect() {
        return select;
    }

    public boolean isUseAsKeyword() {
        return useAsKeyword;
    }

    public void setUseAsKeyword(boolean useAsKeyword) {
        this.useAsKeyword = useAsKeyword;
    }

    /** Returns null for omission, true for WITH DATA, and false for WITH NO DATA. */
    public Boolean getWithData() {
        return withData;
    }

    public void setWithData(Boolean withData) {
        this.withData = withData;
    }

    public StringBuilder appendSelectTo(StringBuilder builder,
            java.util.function.Consumer<Select> selectRenderer) {
        return appendQueryTo(builder, selectRenderer, builder::append);
    }

    public StringBuilder appendQueryTo(StringBuilder builder,
            java.util.function.Consumer<Select> selectRenderer,
            java.util.function.Consumer<net.sf.jsqlparser.statement.execute.Execute> executeRenderer) {
        if (select != null || execute != null) {
            if (duplicateHandling != null) {
                builder.append(' ').append(duplicateHandling);
            }
            builder.append(useAsKeyword ? " AS " : " ");
            if (selectParenthesis) {
                builder.append("(");
            }
            if (execute != null) {
                executeRenderer.accept(execute);
            } else {
                selectRenderer.accept(select);
            }
            if (selectParenthesis) {
                builder.append(")");
            }
            if (withData != null) {
                builder.append(withData ? " WITH DATA" : " WITH NO DATA");
            }
        }
        return builder;
    }

    public void setSelect(Select select, boolean parenthesis) {
        this.select = select;
        this.execute = null;
        this.selectParenthesis = parenthesis;
    }

    public Table getLikeTable() {
        if (likeTable != null) {
            return likeTable;
        }
        List<LikeClause> clauses = getTableElements(LikeClause.class);
        return clauses.isEmpty() ? null : clauses.get(0).getTable();
    }

    /** Returns the legacy trailing LIKE source, excluding LIKE clauses inside the definition. */
    public Table getTrailingLikeTable() {
        return likeTable;
    }

    public ColDataType getOfType() {
        return ofType;
    }

    public void setOfType(ColDataType ofType) {
        this.ofType = ofType;
    }

    public void setLikeTable(Table likeTable, boolean parenthesis) {
        List<LikeClause> clauses = getTableElements(LikeClause.class);
        if (this.likeTable == null && !clauses.isEmpty()) {
            if (likeTable != null) {
                clauses.get(0).setTable(likeTable);
            } else {
                List<TableElement> elements = new ArrayList<>(tableElements);
                elements.remove(clauses.get(0));
                setTableElements(elements);
            }
            return;
        }
        this.likeTable = likeTable;
        this.selectParenthesis = parenthesis;
    }

    /**
     * BigQuery's {@code CREATE [SNAPSHOT] TABLE target CLONE source [FOR SYSTEM_TIME AS OF ...]}.
     */
    public Table getCloneTable() {
        return cloneTable;
    }

    public void setCloneTable(Table cloneTable) {
        this.cloneTable = cloneTable;
    }

    public CreateTable withCloneTable(Table cloneTable) {
        setCloneTable(cloneTable);
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public TablePartitioning getPartitioning() {
        return partitioning;
    }

    public void setPartitioning(TablePartitioning partitioning) {
        this.partitioning = partitioning;
    }

    public Table getPartitionOf() {
        return partitionOf;
    }

    public void setPartitionOf(Table partitionOf) {
        this.partitionOf = partitionOf;
    }

    public PartitionBound getPartitionBound() {
        return partitionBound;
    }

    public void setPartitionBound(PartitionBound partitionBound) {
        this.partitionBound = partitionBound;
    }

    /** PostgreSQL parent tables, in declaration order; null if INHERITS is absent. */
    public List<Table> getInherits() {
        return inherits;
    }

    public void setInherits(List<Table> inherits) {
        this.inherits = inherits;
    }

    public CreateTable withInherits(List<Table> inherits) {
        setInherits(inherits);
        return this;
    }

    /** Shared rendering of the structured parent references. */
    public void appendInheritanceTo(StringBuilder builder) {
        if (inherits != null) {
            builder.append(" INHERITS ").append(PlainSelect.getStringList(inherits, true, true));
        }
    }

    public boolean isSelectParenthesis() {
        return selectParenthesis;
    }

    public void setSelectParenthesis(boolean selectParenthesis) {
        this.selectParenthesis = selectParenthesis;
    }

    public RowMovement getRowMovement() {
        return rowMovement;
    }

    public void setRowMovement(RowMovement rowMovement) {
        this.rowMovement = rowMovement;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder b = new StringBuilder();
        appendCreateClause(b);
        appendColumnDefinitions(b);
        appendInheritanceTo(b);
        if (partitionBound != null) {
            b.append(" ").append(partitionBound);
        }
        appendTableOptions(b);
        appendTableProperties(b);
        return b.toString();
    }

    private void appendCreateClause(StringBuilder b) {
        String createOps = PlainSelect.getStringList(createOptionsStrings, false, false);

        b.append("CREATE ");
        if (unlogged) {
            b.append("UNLOGGED ");
        }
        if (!"".equals(createOps)) {
            b.append(createOps).append(" ");
        }
        if (orReplace) {
            b.append("OR REPLACE ");
        }
        b.append("TABLE ");
        if (ifNotExists) {
            b.append("IF NOT EXISTS ");
        }
        b.append(table);
        if (ofType != null) {
            b.append(" OF ").append(ofType);
        }
        if (partitionOf != null) {
            b.append(" PARTITION OF ").append(partitionOf);
        }
        if (cloneTable != null) {
            b.append(" CLONE ").append(cloneTable);
        }
    }

    private void appendColumnDefinitions(StringBuilder b) {
        if (columns != null && !columns.isEmpty()) {
            b.append(" ");
            b.append(PlainSelect.getStringList(columns, true, true));
        }
        if (tableElements != null) {
            b.append(" (");
            b.append(PlainSelect.getStringList(tableElements, true, false));
            b.append(")");
        } else if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
            b.append(" (");
            b.append(PlainSelect.getStringList(columnDefinitions, true, false));
            if (indexes != null && !indexes.isEmpty()) {
                b.append(", ");
                b.append(PlainSelect.getStringList(indexes));
            }
            b.append(")");
        }
    }

    public void appendTableOptionsTo(StringBuilder builder,
            java.util.function.Consumer<net.sf.jsqlparser.expression.Expression> expressionPrinter) {
        if (tableOptions != null) {
            for (TableOption option : tableOptions) {
                builder.append(' ');
                option.appendTo(builder, expressionPrinter);
            }
        } else {
            String options = PlainSelect.getStringList(tableOptionsStrings, false, false);
            if (options != null && !options.isEmpty()) {
                builder.append(' ').append(options);
            }
        }
    }

    private void appendTableOptions(StringBuilder b) {
        if (partitioning != null && tableOptionsAfterPartition) {
            b.append(" ").append(partitioning);
        }
        appendTableOptionsTo(b, b::append);
        if (partitioning != null && !tableOptionsAfterPartition) {
            b.append(" ").append(partitioning);
        }
    }

    private void appendTableProperties(StringBuilder b) {
        if (rowMovement != null) {
            b.append(" ").append(rowMovement.getMode()).append(" ROW MOVEMENT");
        }
        appendSelectTo(b, b::append);
        if (likeTable != null) {
            b.append(" LIKE ");
            if (selectParenthesis) {
                b.append("(");
            }
            b.append(likeTable);
            if (selectParenthesis) {
                b.append(")");
            }
        }
        if (interleaveIn != null) {
            b.append(", ").append(interleaveIn);
        }
    }

    public CreateTable withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public CreateTable withUnlogged(boolean unlogged) {
        this.setUnlogged(unlogged);
        return this;
    }

    public CreateTable withCreateOptionsStrings(List<String> createOptionsStrings) {
        this.setCreateOptionsStrings(createOptionsStrings);
        return this;
    }

    public CreateTable withSelectParenthesis(boolean selectParenthesis) {
        this.setSelectParenthesis(selectParenthesis);
        return this;
    }

    public CreateTable withIfNotExists(boolean ifNotExists) {
        this.setIfNotExists(ifNotExists);
        return this;
    }

    public CreateTable withPartitioning(TablePartitioning partitioning) {
        this.setPartitioning(partitioning);
        return this;
    }

    public CreateTable withPartitionOf(Table partitionOf) {
        setPartitionOf(partitionOf);
        return this;
    }

    public CreateTable withPartitionBound(PartitionBound partitionBound) {
        setPartitionBound(partitionBound);
        return this;
    }

    public CreateTable withRowMovement(RowMovement rowMovement) {
        this.setRowMovement(rowMovement);
        return this;
    }

    public CreateTable withTableOptionsStrings(List<String> tableOptionsStrings) {
        this.setTableOptionsStrings(tableOptionsStrings);
        return this;
    }

    public CreateTable withTableOptions(List<TableOption> tableOptions) {
        this.setTableOptions(tableOptions);
        return this;
    }

    public CreateTable withColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.setColumnDefinitions(columnDefinitions);
        return this;
    }

    public CreateTable withColumns(List<String> columns) {
        this.setColumns(columns);
        return this;
    }

    public CreateTable withIndexes(List<Index> indexes) {
        this.setIndexes(indexes);
        return this;
    }

    public CreateTable withTableElements(List<TableElement> tableElements) {
        this.setTableElements(tableElements);
        return this;
    }

    public CreateTable addCreateOptionsStrings(String... createOptionsStrings) {
        List<String> collection =
                Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        Collections.addAll(collection, createOptionsStrings);
        return this.withCreateOptionsStrings(collection);
    }

    public CreateTable addCreateOptionsStrings(Collection<String> createOptionsStrings) {
        List<String> collection =
                Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        collection.addAll(createOptionsStrings);
        return this.withCreateOptionsStrings(collection);
    }

    public CreateTable addColumnDefinitions(ColumnDefinition... columnDefinitions) {
        List<ColumnDefinition> collection =
                Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnDefinitions);
        return this.withColumnDefinitions(collection);
    }

    public CreateTable addColumnDefinitions(
            Collection<? extends ColumnDefinition> columnDefinitions) {
        List<ColumnDefinition> collection =
                Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        collection.addAll(columnDefinitions);
        return this.withColumnDefinitions(collection);
    }

    public CreateTable addColumns(String... columns) {
        List<String> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public CreateTable addColumns(Collection<String> columns) {
        List<String> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public CreateTable addIndexes(Index... indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        Collections.addAll(collection, indexes);
        return this.withIndexes(collection);
    }

    public CreateTable addIndexes(Collection<? extends Index> indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        collection.addAll(indexes);
        return this.withIndexes(collection);
    }

    public SpannerInterleaveIn getSpannerInterleaveIn() {
        return interleaveIn;
    }

    public void setSpannerInterleaveIn(SpannerInterleaveIn spannerInterleaveIn) {
        this.interleaveIn = spannerInterleaveIn;
    }

    public CreateTable withSpannerInterleaveIn(SpannerInterleaveIn spannerInterleaveIn) {
        this.interleaveIn = spannerInterleaveIn;
        return this;
    }
}
