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

import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Partition;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.ReturningClause;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class Insert implements Statement {

    private Table table;
    private OracleHint oracleHint = null;
    private ExpressionList<Column> columns;
    private List<Partition> partitions;
    private Select select;
    private boolean onlyDefaultValues = false;
    private List<UpdateSet> duplicateUpdateSets = null;
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

    public List<UpdateSet> getDuplicateUpdateSets() {
        return duplicateUpdateSets;
    }

    public List<UpdateSet> getSetUpdateSets() {
        return setUpdateSets;
    }

    public Insert withDuplicateUpdateSets(List<UpdateSet> duplicateUpdateSets) {
        this.duplicateUpdateSets = duplicateUpdateSets;
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
        return duplicateUpdateSets != null && !duplicateUpdateSets.isEmpty();
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

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            sql.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem<?> withItem = iter.next();
                sql.append(withItem);
                if (iter.hasNext()) {
                    sql.append(",");
                }
                sql.append(" ");
            }
        }
        sql.append("INSERT ");
        if (oracleHint != null) {
            sql.append(oracleHint).append(" ");
        }
        if (modifierPriority != null) {
            sql.append(modifierPriority.name()).append(" ");
        }
        if (modifierIgnore) {
            sql.append("IGNORE ");
        }
        if (overwrite) {
            sql.append("OVERWRITE ");
        } else {
            sql.append("INTO ");
        }
        if (tableKeyword) {
            sql.append("TABLE ");
        }
        sql.append(table).append(" ");

        if (onlyDefaultValues) {
            sql.append("DEFAULT VALUES");
        }

        if (columns != null) {
            sql.append("(");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                // only plain names, but not fully qualified names allowed
                sql.append(columns.get(i).getColumnName());
            }
            sql.append(") ");
        }

        if (partitions != null) {
            sql.append(" PARTITION (");
            Partition.appendPartitionsTo(sql, partitions);
            sql.append(") ");
        }

        if (outputClause != null) {
            sql.append(outputClause);
        }

        if (select != null) {
            sql.append(select);
        }

        if (setUpdateSets != null && !setUpdateSets.isEmpty()) {
            sql.append("SET ");
            sql = UpdateSet.appendUpdateSetsTo(sql, setUpdateSets);
        }

        if (duplicateUpdateSets != null && !duplicateUpdateSets.isEmpty()) {
            sql.append(" ON DUPLICATE KEY UPDATE ");
            sql = UpdateSet.appendUpdateSetsTo(sql, duplicateUpdateSets);
        }

        if (conflictAction != null) {
            sql.append(" ON CONFLICT");

            if (conflictTarget != null) {
                conflictTarget.appendTo(sql);
            }
            conflictAction.appendTo(sql);
        }

        if (returningClause != null) {
            returningClause.appendTo(sql);
        }

        return sql.toString();
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
}
