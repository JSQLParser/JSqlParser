/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** Structured PostgreSQL property actions shared by tables, indexes and views. */
public class RelationAlterAction extends AlterExpression {
    private TriggerState triggerState;
    private TriggerTarget triggerTarget;
    private Kind kind;
    private ColumnAction columnAction;
    private ReplicaIdentity replicaIdentity;
    private String newName;
    private String value;
    private Integer columnNumber;
    private Long statistics;
    private boolean statisticsDefault;
    private Expression defaultExpression;
    private Expression generationExpression;

    public Expression getGenerationExpression() {
        return generationExpression;
    }

    public void setGenerationExpression(Expression expression) {
        generationExpression = expression;
    }

    private Table relation;
    private boolean noInherit;
    private boolean noDependency;
    private List<Index.Option> options;
    private List<String> resetOptions;

    public enum Kind {
        RENAME, RENAME_COLUMN, OWNER, SET_SCHEMA, SET_TABLESPACE, SET_ACCESS_METHOD, SET_OPTIONS, RESET_OPTIONS, ALTER_COLUMN, ATTACH_PARTITION, DEPENDS_ON_EXTENSION, VALIDATE_CONSTRAINT, INHERIT, ALTER_CONSTRAINT_INHERIT, REPLICA_IDENTITY, CLUSTER_ON, SET_WITHOUT_CLUSTER, SET_WITHOUT_OIDS, SET_LOGGED, SET_UNLOGGED, OF, NOT_OF, TRIGGER_STATE
    }

    public enum ColumnAction {
        SET_DEFAULT, DROP_DEFAULT, SET_STATISTICS, SET_STORAGE, SET_COMPRESSION, SET_EXPRESSION, DROP_EXPRESSION, SET_OPTIONS, RESET_OPTIONS
    }

    public enum ReplicaIdentity {
        DEFAULT, FULL, NOTHING, USING_INDEX
    }

    public enum TriggerState {
        ENABLE, DISABLE, ENABLE_ALWAYS, ENABLE_REPLICA
    }
    public enum TriggerTarget {
        NAME, ALL, USER
    }


    public TriggerState getTriggerState() {
        return triggerState;
    }

    public void setTriggerState(TriggerState triggerState) {
        this.triggerState = triggerState;
    }

    public TriggerTarget getTriggerTarget() {
        return triggerTarget;
    }

    public void setTriggerTarget(TriggerTarget triggerTarget) {
        this.triggerTarget = triggerTarget;
    }


    public RelationAlterAction() {
        setOperation(AlterOperation.ALTER_RELATION);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public ColumnAction getColumnAction() {
        return columnAction;
    }

    public void setColumnAction(ColumnAction columnAction) {
        this.columnAction = columnAction;
    }

    public ReplicaIdentity getReplicaIdentity() {
        return replicaIdentity;
    }

    public void setReplicaIdentity(ReplicaIdentity replicaIdentity) {
        this.replicaIdentity = replicaIdentity;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public Long getStatistics() {
        return statistics;
    }

    public void setStatistics(Long statistics) {
        this.statistics = statistics;
        statisticsDefault = false;
    }

    public boolean isStatisticsDefault() {
        return statisticsDefault;
    }

    public void setStatisticsDefault(boolean statisticsDefault) {
        this.statisticsDefault = statisticsDefault;
        if (statisticsDefault) {
            statistics = null;
        }
    }

    public Expression getDefaultExpression() {
        return defaultExpression;
    }

    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    public Table getRelation() {
        return relation;
    }

    public void setRelation(Table relation) {
        this.relation = relation;
    }

    public boolean isNoInherit() {
        return noInherit;
    }

    public void setNoInherit(boolean noInherit) {
        this.noInherit = noInherit;
    }

    public boolean isNoDependency() {
        return noDependency;
    }

    public void setNoDependency(boolean noDependency) {
        this.noDependency = noDependency;
    }

    public List<Index.Option> getOptions() {
        return options;
    }

    public void setOptions(List<Index.Option> options) {
        this.options = options;
    }

    public List<String> getResetOptions() {
        return resetOptions;
    }

    public void setResetOptions(List<String> resetOptions) {
        this.resetOptions = resetOptions;
    }

    @Override
    protected void appendBody(StringBuilder builder) {
        appendDefinition(builder, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        appendDefinition(builder, expressionPrinter);
        appendCommonTail(builder);
        return builder;
    }

    private void appendDefinition(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        switch (kind) {
            case CLUSTER_ON:
                builder.append("CLUSTER ON ").append(value);
                break;
            case SET_WITHOUT_CLUSTER:
            case SET_WITHOUT_OIDS:
            case SET_LOGGED:
            case SET_UNLOGGED:
            case NOT_OF:
                builder.append(kind.name().replace('_', ' '));
                break;
            case OF:
                builder.append("OF ").append(value);
                break;
            case TRIGGER_STATE:
                builder.append(triggerState.name().replace('_', ' ')).append(" TRIGGER ")
                        .append(triggerTarget == TriggerTarget.NAME ? value : triggerTarget);
                break;
            case RENAME:
                builder.append("RENAME TO ").append(newName);
                break;
            case RENAME_COLUMN:
                builder.append(hasColumn() ? "RENAME COLUMN " : "RENAME ")
                        .append(getColumnName()).append(" TO ").append(newName);
                break;
            case OWNER:
                builder.append("OWNER TO ").append(value);
                break;
            case SET_SCHEMA:
                builder.append("SET SCHEMA ").append(value);
                break;
            case SET_TABLESPACE:
                builder.append("SET TABLESPACE ").append(value);
                break;
            case SET_ACCESS_METHOD:
                builder.append("SET ACCESS METHOD ").append(value);
                break;
            case SET_OPTIONS:
                builder.append("SET ");
                Index.Option.appendListTo(builder, options, expressionPrinter);
                break;
            case RESET_OPTIONS:
                builder.append("RESET ")
                        .append(PlainSelect.getStringList(resetOptions, true, true));
                break;
            case ALTER_COLUMN:
                appendColumnChange(builder, expressionPrinter);
                break;
            case ATTACH_PARTITION:
                builder.append("ATTACH PARTITION ").append(relation);
                break;
            case DEPENDS_ON_EXTENSION:
                builder.append(noDependency ? "NO DEPENDS ON EXTENSION " : "DEPENDS ON EXTENSION ")
                        .append(value);
                break;
            case VALIDATE_CONSTRAINT:
                builder.append("VALIDATE CONSTRAINT ").append(getConstraintName());
                break;
            case INHERIT:
                builder.append(noInherit ? "NO INHERIT " : "INHERIT ").append(relation);
                break;
            case ALTER_CONSTRAINT_INHERIT:
                builder.append("ALTER CONSTRAINT ").append(getConstraintName())
                        .append(noInherit ? " NO INHERIT" : " INHERIT");
                break;
            case REPLICA_IDENTITY:
                builder.append("REPLICA IDENTITY ");
                if (replicaIdentity == ReplicaIdentity.USING_INDEX) {
                    builder.append("USING INDEX ").append(value);
                } else {
                    builder.append(replicaIdentity);
                }
                break;
            default:
                throw new IllegalStateException("Unsupported relation alteration: " + kind);
        }
    }

    private void appendColumnChange(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append(hasColumn() ? "ALTER COLUMN " : "ALTER ")
                .append(columnNumber != null ? columnNumber : getColumnName());
        switch (columnAction) {
            case SET_DEFAULT:
                builder.append(" SET DEFAULT ");
                expressionPrinter.accept(defaultExpression);
                break;
            case DROP_DEFAULT:
                builder.append(" DROP DEFAULT");
                break;
            case SET_STATISTICS:
                builder.append(" SET STATISTICS ")
                        .append(statisticsDefault ? "DEFAULT" : statistics);
                break;
            case SET_OPTIONS:
                builder.append(" SET ");
                Index.Option.appendListTo(builder, options, expressionPrinter);
                break;
            case RESET_OPTIONS:
                builder.append(" RESET ")
                        .append(PlainSelect.getStringList(resetOptions, true, true));
                break;
            case SET_STORAGE:
                builder.append(" SET STORAGE ").append(value);
                break;
            case SET_COMPRESSION:
                builder.append(" SET COMPRESSION ").append(value);
                break;
            case SET_EXPRESSION:
                builder.append(" SET EXPRESSION ");
                net.sf.jsqlparser.statement.create.table.GeneratedColumnDefinition
                        .appendExpressionTo(
                                builder, generationExpression, expressionPrinter);
                break;
            case DROP_EXPRESSION:
                builder.append(" DROP EXPRESSION");
                if (isUsingIfExists()) {
                    builder.append(" IF EXISTS");
                }
                break;
            default:
                throw new IllegalStateException("Unsupported column alteration: " + columnAction);
        }
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (kind == Kind.ALTER_COLUMN && columnAction == ColumnAction.SET_DEFAULT) {
            visitor.accept(defaultExpression);
        } else if (kind == Kind.ALTER_COLUMN && columnAction == ColumnAction.SET_EXPRESSION) {
            visitor.accept(generationExpression);
        } else if ((kind == Kind.SET_OPTIONS || kind == Kind.ALTER_COLUMN
                && columnAction == ColumnAction.SET_OPTIONS) && options != null) {
            options.stream().map(Index.Option::getValue).filter(java.util.Objects::nonNull)
                    .forEach(visitor);
        }
    }

    public void visitTables(Consumer<Table> visitor) {
        if (kind == Kind.INHERIT && relation != null) {
            visitor.accept(relation);
        }
    }
}
