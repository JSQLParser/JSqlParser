/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.UtilityOption;
import net.sf.jsqlparser.statement.maintenance.MaintenanceTarget;

/**
 * {@code ANALYZE} in its MySQL and PostgreSQL forms, which exclude each other.
 * <ul>
 * <li>MySQL: {@code ANALYZE [NO_WRITE_TO_BINLOG | LOCAL] TABLE t, ... [histogram]}, where the
 * histogram clause is {@code UPDATE HISTOGRAM ON c, ... [WITH n BUCKETS] [{AUTO | MANUAL} UPDATE]},
 * {@code UPDATE HISTOGRAM ON c USING DATA 'json'} or {@code DROP HISTOGRAM ON c, ...}.</li>
 * <li>PostgreSQL: {@code ANALYZE [(option [value], ...)] [target, ...]} and the unparenthesized
 * {@code ANALYZE [VERBOSE] [target, ...]}, where a target can name columns. Without targets, every
 * table of the database is analyzed. {@code ANALYZE VERBOSE t} holds the option {@code VERBOSE}
 * like {@code ANALYZE (VERBOSE) t}, and {@link #isParenthesizedOptions()} tells them apart. The
 * statement can also be spelled {@code ANALYSE}, which {@link #getCommand()} keeps.</li>
 * </ul>
 * The tables of both forms are kept as {@link MaintenanceTarget}s; {@link #getTables()} lists their
 * tables. The modifier, the {@code TABLE} keyword and the histogram clause belong to the MySQL
 * form, the options and the targets with {@code ONLY}, {@code *} or columns to the PostgreSQL form,
 * and the parser accepts no statement that mixes them.
 */
public class Analyze extends ASTNodeAccessImpl implements Statement {

    public enum Modifier {
        NO_WRITE_TO_BINLOG, LOCAL
    }

    public enum HistogramAction {
        UPDATE, DROP
    }

    /** Whether MySQL updates a histogram automatically when it updates the table statistics. */
    public enum HistogramUpdateMode {
        AUTO, MANUAL
    }

    /** The options that PostgreSQL 18 documents for ANALYZE. */
    public enum OptionKind {
        VERBOSE, SKIP_LOCKED, BUFFER_USAGE_LIMIT
    }

    /** The spelling of the statement; PostgreSQL also accepts {@code ANALYSE}. */
    public enum Command {
        ANALYZE, ANALYSE
    }

    private static final String[] KEYWORD_OPTIONS = {"VERBOSE"};

    private final List<MaintenanceTarget> targets = new ArrayList<>();
    private final List<UtilityOption> options = new ArrayList<>();
    private boolean parenthesizedOptions;
    private Command command = Command.ANALYZE;
    private boolean tableKeyword;
    private Modifier modifier;
    private HistogramAction histogramAction;
    private final List<Column> histogramColumns = new ArrayList<>();
    private Integer histogramBucketCount;
    private StringValue histogramData;
    private HistogramUpdateMode histogramUpdateMode;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        return targets.isEmpty() ? null : targets.get(0).getTable();
    }

    /** Replaces the targets with the table; null removes every target. */
    public void setTable(Table table) {
        targets.clear();
        if (table != null) {
            targets.add(new MaintenanceTarget(table));
        }
    }

    /** A read-only snapshot of the tables of {@link #getTargets()}. */
    public List<Table> getTables() {
        return Collections.unmodifiableList(targets.stream().map(MaintenanceTarget::getTable)
                .collect(Collectors.toList()));
    }

    /** Replaces the targets with plain tables, without {@code ONLY} or columns. */
    public void setTables(Collection<? extends Table> tables) {
        targets.clear();
        if (tables != null) {
            tables.forEach(table -> targets.add(new MaintenanceTarget(table)));
        }
    }

    /** The tables to analyze, with their PostgreSQL {@code ONLY} and column lists. */
    public List<MaintenanceTarget> getTargets() {
        return targets;
    }

    public void setTargets(Collection<? extends MaintenanceTarget> targets) {
        List<MaintenanceTarget> replacement =
                targets == null ? Collections.emptyList() : new ArrayList<>(targets);
        this.targets.clear();
        this.targets.addAll(replacement);
    }

    public Analyze addTargets(MaintenanceTarget... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public Analyze withTargets(Collection<? extends MaintenanceTarget> targets) {
        setTargets(targets);
        return this;
    }

    /** The PostgreSQL options, such as {@code VERBOSE} or {@code SKIP_LOCKED}. */
    public List<UtilityOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<? extends UtilityOption> options) {
        List<UtilityOption> replacement =
                options == null ? Collections.emptyList() : new ArrayList<>(options);
        this.options.clear();
        this.options.addAll(replacement);
    }

    public Analyze addOptions(UtilityOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public Analyze withOptions(Collection<? extends UtilityOption> options) {
        setOptions(options);
        return this;
    }

    /**
     * Whether the options are written in parentheses. Options that the unparenthesized form cannot
     * express are always rendered in parentheses.
     */
    public boolean isParenthesizedOptions() {
        return parenthesizedOptions;
    }

    public void setParenthesizedOptions(boolean parenthesizedOptions) {
        this.parenthesizedOptions = parenthesizedOptions;
    }

    public Analyze withParenthesizedOptions(boolean parenthesizedOptions) {
        setParenthesizedOptions(parenthesizedOptions);
        return this;
    }

    /** {@code ANALYZE}, or PostgreSQL's {@code ANALYSE}, which takes only the PostgreSQL form. */
    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = Objects.requireNonNull(command, "command");
    }

    public Analyze withCommand(Command command) {
        setCommand(command);
        return this;
    }

    public boolean isTableKeyword() {
        return tableKeyword;
    }

    public void setTableKeyword(boolean tableKeyword) {
        this.tableKeyword = tableKeyword;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public HistogramAction getHistogramAction() {
        return histogramAction;
    }

    public void setHistogramAction(HistogramAction histogramAction) {
        this.histogramAction = histogramAction;
    }

    public List<Column> getHistogramColumns() {
        return Collections.unmodifiableList(histogramColumns);
    }

    public void setHistogramColumns(Collection<? extends Column> columns) {
        histogramColumns.clear();
        if (columns != null) {
            histogramColumns.addAll(columns);
        }
    }

    public Integer getHistogramBucketCount() {
        return histogramBucketCount;
    }

    public void setHistogramBucketCount(Integer histogramBucketCount) {
        this.histogramBucketCount = histogramBucketCount;
    }

    /** The JSON histogram of {@code UPDATE HISTOGRAM ... USING DATA}, or null. */
    public StringValue getHistogramData() {
        return histogramData;
    }

    public void setHistogramData(StringValue histogramData) {
        this.histogramData = histogramData;
    }

    /** The {@code AUTO UPDATE} or {@code MANUAL UPDATE} of {@code UPDATE HISTOGRAM}, or null. */
    public HistogramUpdateMode getHistogramUpdateMode() {
        return histogramUpdateMode;
    }

    public void setHistogramUpdateMode(HistogramUpdateMode histogramUpdateMode) {
        this.histogramUpdateMode = histogramUpdateMode;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(command);
        if (modifier != null) {
            builder.append(" ").append(modifier);
        }
        UtilityOption.appendOptions(builder, options, parenthesizedOptions, KEYWORD_OPTIONS);
        if (tableKeyword) {
            builder.append(" TABLE");
        }
        MaintenanceTarget.appendTargets(builder, targets);
        if (histogramAction != null) {
            builder.append(" ").append(histogramAction).append(" HISTOGRAM ON ")
                    .append(histogramColumns.stream().map(Column::toString)
                            .collect(Collectors.joining(", ")));
            if (histogramBucketCount != null) {
                builder.append(" WITH ").append(histogramBucketCount).append(" BUCKETS");
            }
            if (histogramData != null) {
                builder.append(" USING DATA ").append(histogramData);
            }
            if (histogramUpdateMode != null) {
                builder.append(" ").append(histogramUpdateMode).append(" UPDATE");
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public Analyze withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Analyze withTables(Collection<? extends Table> tables) {
        setTables(tables);
        return this;
    }

    /** Adds plain tables, without {@code ONLY} or columns. */
    public Analyze addTables(Table... tables) {
        for (Table table : tables) {
            targets.add(new MaintenanceTarget(table));
        }
        return this;
    }

    public Analyze withTableKeyword(boolean tableKeyword) {
        setTableKeyword(tableKeyword);
        return this;
    }

    public Analyze withModifier(Modifier modifier) {
        setModifier(modifier);
        return this;
    }

    public Analyze withHistogramAction(HistogramAction histogramAction) {
        setHistogramAction(histogramAction);
        return this;
    }

    public Analyze withHistogramColumns(Collection<? extends Column> columns) {
        setHistogramColumns(columns);
        return this;
    }

    public Analyze addHistogramColumns(Column... columns) {
        Collections.addAll(histogramColumns, columns);
        return this;
    }

    public Analyze withHistogramBucketCount(Integer histogramBucketCount) {
        setHistogramBucketCount(histogramBucketCount);
        return this;
    }

    public Analyze withHistogramData(StringValue histogramData) {
        setHistogramData(histogramData);
        return this;
    }

    public Analyze withHistogramUpdateMode(HistogramUpdateMode histogramUpdateMode) {
        setHistogramUpdateMode(histogramUpdateMode);
        return this;
    }
}
