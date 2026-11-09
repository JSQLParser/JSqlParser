/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.UtilityOption;

/**
 * PostgreSQL {@code VACUUM [(option [value], ...)] [target, ...]} and the unparenthesized
 * {@code VACUUM [FULL] [FREEZE] [VERBOSE] [ANALYZE] [target, ...]}.
 * <p>
 * Both forms keep their options in {@link #getOptions()}: {@code VACUUM FULL t} holds the option
 * {@code FULL} like {@code VACUUM (FULL) t}, and {@link #isParenthesizedOptions()} tells them
 * apart.
 */
public class VacuumStatement extends ASTNodeAccessImpl implements Statement {

    /**
     * The options that PostgreSQL 18 documents for VACUUM; see
     * {@link UtilityOption#getKind(Class)}.
     */
    public enum OptionKind {
        FULL, FREEZE, VERBOSE, ANALYZE, DISABLE_PAGE_SKIPPING, SKIP_LOCKED, INDEX_CLEANUP, PROCESS_MAIN, PROCESS_TOAST, TRUNCATE, PARALLEL, SKIP_DATABASE_STATS, ONLY_DATABASE_STATS, BUFFER_USAGE_LIMIT
    }

    /** The options of the unparenthesized form, in the order that PostgreSQL requires. */
    private static final String[] KEYWORD_OPTIONS = {"FULL", "FREEZE", "VERBOSE", "ANALYZE"};

    private final List<UtilityOption> options = new ArrayList<>();
    private boolean parenthesizedOptions;
    private final List<MaintenanceTarget> targets = new ArrayList<>();

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public List<UtilityOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<? extends UtilityOption> options) {
        replace(this.options, options);
    }

    public VacuumStatement addOptions(UtilityOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public VacuumStatement withOptions(Collection<? extends UtilityOption> options) {
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

    public VacuumStatement withParenthesizedOptions(boolean parenthesizedOptions) {
        setParenthesizedOptions(parenthesizedOptions);
        return this;
    }

    /** The tables to vacuum; empty for every table of the database. */
    public List<MaintenanceTarget> getTargets() {
        return targets;
    }

    public void setTargets(Collection<? extends MaintenanceTarget> targets) {
        replace(this.targets, targets);
    }

    public VacuumStatement addTargets(MaintenanceTarget... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public VacuumStatement withTargets(Collection<? extends MaintenanceTarget> targets) {
        setTargets(targets);
        return this;
    }

    private static <E> void replace(List<E> list, Collection<? extends E> elements) {
        List<E> replacement =
                elements == null ? Collections.emptyList() : new ArrayList<>(elements);
        list.clear();
        list.addAll(replacement);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("VACUUM");
        UtilityOption.appendOptions(builder, options, parenthesizedOptions, KEYWORD_OPTIONS);
        return MaintenanceTarget.appendTargets(builder, targets);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
