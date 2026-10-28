/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Alter implements Statement {
    private Table table;
    private boolean useOnly = false;
    private boolean useTableIfExists = false;
    private List<AlterExpression> alterExpressions;
    private boolean useDescendants;
    private boolean foreignTable;



    public boolean isUseDescendants() {
        return useDescendants;
    }

    public void setUseDescendants(boolean useDescendants) {
        this.useDescendants = useDescendants;
    }

    public StringBuilder appendTargetTo(StringBuilder builder) {
        builder.append(foreignTable ? "ALTER FOREIGN TABLE " : "ALTER TABLE ");
        if (useTableIfExists) {
            builder.append("IF EXISTS ");
        }
        if (useOnly) {
            builder.append("ONLY ");
        }
        builder.append(table.getFullyQualifiedName());
        if (useDescendants) {
            builder.append(" *");
        }
        return builder.append(' ');
    }


    public boolean isForeignTable() {
        return foreignTable;
    }

    public void setForeignTable(boolean foreignTable) {
        this.foreignTable = foreignTable;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isUseOnly() {
        return useOnly;
    }

    public void setUseOnly(boolean useOnly) {
        this.useOnly = useOnly;
    }

    public boolean isUseTableIfExists() {
        return useTableIfExists;
    }

    public void setUseTableIfExists(boolean useTableIfExists) {
        this.useTableIfExists = useTableIfExists;
    }

    public Alter withUseTableIfExists(boolean useTableIfExists) {
        this.useTableIfExists = useTableIfExists;
        return this;
    }

    public void addAlterExpression(AlterExpression alterExpression) {
        if (alterExpressions == null) {
            alterExpressions = new ArrayList<AlterExpression>();
        }
        alterExpressions.add(alterExpression);
    }

    public List<AlterExpression> getAlterExpressions() {
        return alterExpressions;
    }

    public void setAlterExpressions(List<AlterExpression> alterExpressions) {
        this.alterExpressions = alterExpressions;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        appendTargetTo(b);

        for (int i = 0; i < alterExpressions.size(); i++) {
            AlterExpression action = alterExpressions.get(i);
            if (i > 0) {
                b.append(separatorBefore(action));
            }
            b.append(action);
        }

        return b.toString();
    }

    /**
     * Whether the action is MySQL's PARTITION BY or REMOVE PARTITIONING, which may end the action
     * list without a comma.
     */
    public static boolean isTrailingPartitionOption(AlterExpression action) {
        return action != null && (action.getOperation() == AlterOperation.PARTITION_BY
                || action.getOperation() == AlterOperation.REMOVE_PARTITIONING);
    }

    /**
     * The separator written before an action that follows another: a comma, or a space before
     * PARTITION BY and REMOVE PARTITIONING, which MySQL does not accept after a comma.
     */
    public static String separatorBefore(AlterExpression action) {
        return isTrailingPartitionOption(action) ? " " : ", ";
    }

    public Alter withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Alter withUseOnly(boolean useOnly) {
        this.setUseOnly(useOnly);
        return this;
    }

    public Alter withAlterExpressions(List<AlterExpression> alterExpressions) {
        this.setAlterExpressions(alterExpressions);
        return this;
    }

    public Alter addAlterExpressions(AlterExpression... alterExpressions) {
        List<AlterExpression> collection =
                Optional.ofNullable(getAlterExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, alterExpressions);
        return this.withAlterExpressions(collection);
    }

    public Alter addAlterExpressions(Collection<? extends AlterExpression> alterExpressions) {
        List<AlterExpression> collection =
                Optional.ofNullable(getAlterExpressions()).orElseGet(ArrayList::new);
        collection.addAll(alterExpressions);
        return this.withAlterExpressions(collection);
    }
}
