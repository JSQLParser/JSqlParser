/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Partition;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.ReturningClause;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.UpdateSet;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class Insert extends ASTNodeAccessImpl implements Statement {

    private Table table;
    private OracleHint oracleHint = null;
    private ExpressionList<Column> columns;
    private List<Partition> partitions;
    private Select select;
    private boolean onlyDefaultValues = false;

    public enum OverridingMode {
        NONE, SYSTEM, USER
    }

    private OverridingMode overridingMode = OverridingMode.NONE;
    private InsertModifierPriority modifierPriority = null;
    private boolean modifierIgnore = false;
    private boolean overwrite = false;
    private boolean tableKeyword = false;
    private ReturningClause returningClause;
    private List<UpdateSet> setUpdateSets = null;
    private List<WithItem<?>> withItemsList;
    private OutputClause outputClause;
    private InsertConflictTarget conflictTarget;
    private InsertConflictAction conflictAction;
    private InsertDuplicateAction duplicateAction;
    private Alias rowAlias;
    private ColumnMatching columnMatching;
    private boolean oracleMultiInsert = false;
    private boolean oracleMultiInsertFirst = false;
    private List<OracleMultiInsertBranch> oracleMultiInsertBranches;

    public List<UpdateSet> getDuplicateUpdateSets() {
        return duplicateAction != null ? duplicateAction.getUpdateSets() : null;
    }

    public List<UpdateSet> getSetUpdateSets() {
        return setUpdateSets;
    }

    public Insert withDuplicateUpdateSets(List<UpdateSet> duplicateUpdateSets) {
        if (duplicateAction != null) {
            duplicateAction.setConflictActionType(ConflictActionType.DO_UPDATE);
            duplicateAction.setUpdateSets(duplicateUpdateSets);
        } else {
            duplicateAction = new InsertDuplicateAction(ConflictActionType.DO_UPDATE);
            duplicateAction.setUpdateSets(duplicateUpdateSets);
        }
        return this;
    }

    public Insert withSetUpdateSets(List<UpdateSet> setUpdateSets) {
        this.setUpdateSets = setUpdateSets;
        return this;
    }

    public OutputClause getOutputClause() {
        return outputClause;
    }

    public void setOutputClause(OutputClause outputClause) {
        this.outputClause = outputClause;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        if (table == null && oracleMultiInsertBranches != null
                && !oracleMultiInsertBranches.isEmpty()
                && oracleMultiInsertBranches.get(0).getClauses() != null
                && !oracleMultiInsertBranches.get(0).getClauses().isEmpty()) {
            return oracleMultiInsertBranches.get(0).getClauses().get(0).getTable();
        }
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }

    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> list) {
        columns = list;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> list) {
        partitions = list;
    }

    @Deprecated
    public boolean isUseValues() {
        return select != null && select instanceof Values;
    }

    public ReturningClause getReturningClause() {
        return returningClause;
    }

    public Insert setReturningClause(ReturningClause returningClause) {
        this.returningClause = returningClause;
        return this;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public Values getValues() {
        return select.getValues();
    }

    public PlainSelect getPlainSelect() {
        return select.getPlainSelect();
    }

    public SetOperationList getSetOperationList() {
        return select.getSetOperationList();
    }

    @Deprecated
    public boolean isUseSelectBrackets() {
        return false;
    }

    @Deprecated
    public boolean isUseDuplicate() {
        return duplicateAction != null && duplicateAction.getUpdateSets() != null
                && !duplicateAction.getUpdateSets().isEmpty();
    }

    public InsertModifierPriority getModifierPriority() {
        return modifierPriority;
    }

    public void setModifierPriority(InsertModifierPriority modifierPriority) {
        this.modifierPriority = modifierPriority;
    }

    public boolean isModifierIgnore() {
        return modifierIgnore;
    }

    public void setModifierIgnore(boolean modifierIgnore) {
        this.modifierIgnore = modifierIgnore;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isTableKeyword() {
        return tableKeyword;
    }

    public void setTableKeyword(boolean tableKeyword) {
        this.tableKeyword = tableKeyword;
    }

    @Deprecated
    public boolean isUseSet() {
        return setUpdateSets != null && !setUpdateSets.isEmpty();
    }

    public List<WithItem<?>> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem<?>> withItemsList) {
        this.withItemsList = withItemsList;
    }

    /**
     * DuckDB matches the source columns of an INSERT either positionally or by name:
     * {@code INSERT INTO tbl BY NAME SELECT ...}.
     */
    public enum ColumnMatching {
        BY_NAME("BY NAME"), BY_POSITION("BY POSITION");

        private final String keywords;

        ColumnMatching(String keywords) {
            this.keywords = keywords;
        }

        @Override
        public String toString() {
            return keywords;
        }
    }

    public ColumnMatching getColumnMatching() {
        return columnMatching;
    }

    public void setColumnMatching(ColumnMatching columnMatching) {
        this.columnMatching = columnMatching;
    }

    public Insert withColumnMatching(ColumnMatching columnMatching) {
        setColumnMatching(columnMatching);
        return this;
    }

    public OverridingMode getOverridingMode() {
        return overridingMode;
    }

    public void setOverridingMode(OverridingMode overridingMode) {
        this.overridingMode = java.util.Objects.requireNonNull(overridingMode);
    }

    public Insert withOverridingMode(OverridingMode overridingMode) {
        setOverridingMode(overridingMode);
        return this;
    }

    public boolean isOverriding() {
        return overridingMode != OverridingMode.NONE;
    }

    public void setOverriding(boolean overriding) {
        this.overridingMode = overriding ? OverridingMode.SYSTEM : OverridingMode.NONE;
    }

    public Insert withOverriding(boolean overriding) {
        this.setOverriding(overriding);
        return this;
    }

    public boolean isOnlyDefaultValues() {
        return onlyDefaultValues;
    }

    public void setOnlyDefaultValues(boolean onlyDefaultValues) {
        this.onlyDefaultValues = onlyDefaultValues;
    }

    public Insert withOnlyDefaultValues(boolean onlyDefaultValues) {
        this.setOnlyDefaultValues(onlyDefaultValues);
        return this;
    }

    public InsertConflictTarget getConflictTarget() {
        return conflictTarget;
    }

    public void setConflictTarget(InsertConflictTarget conflictTarget) {
        this.conflictTarget = conflictTarget;
    }

    public Insert withConflictTarget(InsertConflictTarget conflictTarget) {
        setConflictTarget(conflictTarget);
        return this;
    }

    public InsertConflictAction getConflictAction() {
        return conflictAction;
    }

    public void setConflictAction(InsertConflictAction conflictAction) {
        this.conflictAction = conflictAction;
    }

    public Insert withConflictAction(InsertConflictAction conflictAction) {
        setConflictAction(conflictAction);
        return this;
    }

    public boolean isOracleMultiInsert() {
        return oracleMultiInsert;
    }

    public void setOracleMultiInsert(boolean oracleMultiInsert) {
        this.oracleMultiInsert = oracleMultiInsert;
    }

    public boolean isOracleMultiInsertFirst() {
        return oracleMultiInsertFirst;
    }

    public void setOracleMultiInsertFirst(boolean oracleMultiInsertFirst) {
        this.oracleMultiInsertFirst = oracleMultiInsertFirst;
    }

    public List<OracleMultiInsertBranch> getOracleMultiInsertBranches() {
        return oracleMultiInsertBranches;
    }

    public void setOracleMultiInsertBranches(
            List<OracleMultiInsertBranch> oracleMultiInsertBranches) {
        this.oracleMultiInsertBranches = oracleMultiInsertBranches;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append, builder::append, builder::append,
                builder::append).toString();
    }

    /**
     * Appends the statement in clause order. {@link #toString()} renders every part as text, while
     * {@code InsertDeParser} passes renderers that visit the WITH items, queries, expressions and
     * RETURNING items, so both produce the same SQL.
     *
     * @param builder the target
     * @param withItemRenderer renders one WITH item
     * @param selectRenderer renders the source query and the queries of Oracle's multi-table insert
     * @param expressionRenderer renders expressions, including the assignment lists
     * @param selectItemRenderer renders one RETURNING item
     * @return the builder
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<WithItem<?>> withItemRenderer,
            Consumer<Select> selectRenderer, Consumer<Expression> expressionRenderer,
            Consumer<SelectItem<?>> selectItemRenderer) {
        appendWithItems(builder, withItemRenderer);
        appendInsertPrefix(builder);
        if (oracleMultiInsert) {
            appendOracleMultiInsert(builder, selectRenderer, expressionRenderer);
            return builder;
        }
        appendTarget(builder);
        appendSource(builder, selectRenderer, expressionRenderer);
        appendActions(builder, expressionRenderer);
        if (returningClause != null) {
            returningClause.appendTo(builder, selectItemRenderer);
        }
        return builder;
    }

    private void appendWithItems(StringBuilder builder, Consumer<WithItem<?>> withItemRenderer) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (int i = 0; i < withItemsList.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                withItemRenderer.accept(withItemsList.get(i));
            }
            builder.append(" ");
        }
    }

    private void appendInsertPrefix(StringBuilder builder) {
        builder.append("INSERT");
        if (oracleHint != null) {
            builder.append(" ").append(oracleHint);
        }
        if (modifierPriority != null) {
            builder.append(" ").append(modifierPriority.name());
        }
        if (modifierIgnore) {
            builder.append(" IGNORE");
        }
    }

    private void appendOracleMultiInsert(StringBuilder builder, Consumer<Select> selectRenderer,
            Consumer<Expression> expressionRenderer) {
        builder.append(oracleMultiInsertFirst ? " FIRST" : " ALL");
        if (oracleMultiInsertBranches != null) {
            for (OracleMultiInsertBranch branch : oracleMultiInsertBranches) {
                appendOracleMultiInsertBranch(builder, branch, selectRenderer, expressionRenderer);
            }
        }
        if (select != null) {
            builder.append(" ");
            selectRenderer.accept(select);
        }
    }

    private static void appendOracleMultiInsertBranch(StringBuilder builder,
            OracleMultiInsertBranch branch, Consumer<Select> selectRenderer,
            Consumer<Expression> expressionRenderer) {
        if (branch == null || branch.getClauses() == null || branch.getClauses().isEmpty()) {
            return;
        }
        if (branch.getWhenExpression() != null) {
            builder.append(" WHEN ");
            expressionRenderer.accept(branch.getWhenExpression());
            builder.append(" THEN");
        } else if (branch.isElseClause()) {
            builder.append(" ELSE");
        }
        for (OracleMultiInsertClause clause : branch.getClauses()) {
            builder.append(" ");
            clause.appendTo(builder, selectRenderer);
        }
    }

    /** The target: table, partitions, columns and the options before the source. */
    private void appendTarget(StringBuilder builder) {
        builder.append(overwrite ? " OVERWRITE" : " INTO");
        if (tableKeyword) {
            builder.append(" TABLE");
        }
        builder.append(" ").append(table);
        // MySQL and Hive write the partitions right after the table, before the columns
        if (partitions != null) {
            builder.append(" PARTITION (");
            Partition.appendPartitionsTo(builder, partitions);
            builder.append(")");
        }
        if (columns != null) {
            builder.append(" ");
            appendColumnNamesTo(builder, columns);
        }
        if (columnMatching != null) {
            builder.append(" ").append(columnMatching);
        }
        if (isOverriding()) {
            builder.append(" OVERRIDING ").append(overridingMode).append(" VALUE");
        }
        if (outputClause != null) {
            outputClause.appendTo(builder);
            // OUTPUT ends with a space; the source adds its own separator
            if (builder.charAt(builder.length() - 1) == ' ') {
                builder.setLength(builder.length() - 1);
            }
        }
    }

    /** {@code DEFAULT VALUES}, the query, or MySQL's {@code SET} assignments. */
    private void appendSource(StringBuilder builder, Consumer<Select> selectRenderer,
            Consumer<Expression> expressionRenderer) {
        if (onlyDefaultValues) {
            builder.append(" DEFAULT VALUES");
        }
        if (select != null) {
            builder.append(" ");
            selectRenderer.accept(select);
        }
        if (setUpdateSets != null && !setUpdateSets.isEmpty()) {
            builder.append(" SET ");
            UpdateSet.appendUpdateSetsTo(builder, setUpdateSets, expressionRenderer);
            if (rowAlias != null) {
                builder.append(rowAlias);
            }
        }
    }

    private void appendActions(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        if (duplicateAction != null) {
            builder.append(" ON DUPLICATE KEY UPDATE ");
            duplicateAction.appendTo(builder, expressionRenderer);
        }
        if (conflictAction != null) {
            builder.append(" ON CONFLICT");
            if (conflictTarget != null) {
                conflictTarget.appendTo(builder, expressionRenderer);
            }
            conflictAction.appendTo(builder, expressionRenderer);
        }
    }

    /** Appends {@code (a, b)} with the plain column names, as INSERT column lists allow. */
    static StringBuilder appendColumnNamesTo(StringBuilder builder, List<Column> columns) {
        builder.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns.get(i).getColumnName());
        }
        return builder.append(")");
    }

    public Insert withWithItemsList(List<WithItem<?>> withList) {
        this.withItemsList = withList;
        return this;
    }

    public Insert withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Insert withModifierPriority(InsertModifierPriority modifierPriority) {
        this.setModifierPriority(modifierPriority);
        return this;
    }

    public Insert withModifierIgnore(boolean modifierIgnore) {
        this.setModifierIgnore(modifierIgnore);
        return this;
    }

    public Insert withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Insert withColumns(ExpressionList<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Insert addColumns(Column... columns) {
        return addColumns(Arrays.asList(columns));
    }

    public Insert addColumns(Collection<Column> columns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getColumns()).orElseGet(ExpressionList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public InsertDuplicateAction getDuplicateAction() {
        return duplicateAction;
    }

    public void setDuplicateAction(InsertDuplicateAction duplicateAction) {
        this.duplicateAction = duplicateAction;
    }

    public Alias getRowAlias() {
        return rowAlias;
    }

    public void setRowAlias(Alias rowAlias) {
        this.rowAlias = rowAlias;
    }

    public Insert withOracleMultiInsert(boolean oracleMultiInsert) {
        this.setOracleMultiInsert(oracleMultiInsert);
        return this;
    }

    public Insert withOracleMultiInsertFirst(boolean oracleMultiInsertFirst) {
        this.setOracleMultiInsertFirst(oracleMultiInsertFirst);
        return this;
    }

    public Insert withOracleMultiInsertBranches(
            List<OracleMultiInsertBranch> oracleMultiInsertBranches) {
        this.setOracleMultiInsertBranches(oracleMultiInsertBranches);
        return this;
    }
}
