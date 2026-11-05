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
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.UtilityOption;

/**
 * PostgreSQL {@code CLUSTER [(option [value], ...)] [table [USING index]]}, the unparenthesized
 * {@code CLUSTER [VERBOSE] [table [USING index]]} and the pre-8.3 form
 * {@code CLUSTER [VERBOSE] index ON table}. Without a table, every previously clustered table of
 * the database is clustered again.
 * <p>
 * {@code CLUSTER VERBOSE} holds the option {@code VERBOSE} like {@code CLUSTER (VERBOSE)}, and
 * {@link #isParenthesizedOptions()} tells them apart. Both index forms keep the table and the
 * index, and {@link #getIndexSyntax()} tells them apart.
 */
public class ClusterStatement extends ASTNodeAccessImpl implements Statement {

    /** The options that PostgreSQL 18 documents for CLUSTER. */
    public enum OptionKind {
        VERBOSE
    }

    /** How the index is named: {@code table USING index}, or the pre-8.3 {@code index ON table}. */
    public enum IndexSyntax {
        USING, ON
    }

    private static final String[] KEYWORD_OPTIONS = {"VERBOSE"};

    private final List<UtilityOption> options = new ArrayList<>();
    private boolean parenthesizedOptions;
    private Table table;
    private String indexName;
    private IndexSyntax indexSyntax = IndexSyntax.USING;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public List<UtilityOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<? extends UtilityOption> options) {
        List<UtilityOption> replacement =
                options == null ? Collections.emptyList() : new ArrayList<>(options);
        this.options.clear();
        this.options.addAll(replacement);
    }

    public ClusterStatement addOptions(UtilityOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public ClusterStatement withOptions(Collection<? extends UtilityOption> options) {
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

    public ClusterStatement withParenthesizedOptions(boolean parenthesizedOptions) {
        setParenthesizedOptions(parenthesizedOptions);
        return this;
    }

    /** The table to cluster, or null to cluster every previously clustered table. */
    public Table getTable() {
        return table;
    }

    /** Sets the table; without a table the index name is discarded too. */
    public void setTable(Table table) {
        this.table = table;
        if (table == null) {
            indexName = null;
        }
    }

    public ClusterStatement withTable(Table table) {
        setTable(table);
        return this;
    }

    /** The index of {@code USING index}, or null to use the index the table was clustered on. */
    public String getIndexName() {
        return indexName;
    }

    /** Sets the index; it requires a table. */
    public void setIndexName(String indexName) {
        if (indexName != null && table == null) {
            throw new IllegalStateException("CLUSTER ... USING requires a table");
        }
        this.indexName = indexName;
    }

    public ClusterStatement withIndexName(String indexName) {
        setIndexName(indexName);
        return this;
    }

    /**
     * {@code USING}, or {@code ON} for {@code CLUSTER [VERBOSE] index ON table}. The {@code ON}
     * form is rendered only with a table, an index and options that it can express, which is at
     * most an unparenthesized {@code VERBOSE}; otherwise the {@code USING} form is rendered.
     */
    public IndexSyntax getIndexSyntax() {
        return indexSyntax;
    }

    public void setIndexSyntax(IndexSyntax indexSyntax) {
        this.indexSyntax = Objects.requireNonNull(indexSyntax, "indexSyntax");
    }

    public ClusterStatement withIndexSyntax(IndexSyntax indexSyntax) {
        setIndexSyntax(indexSyntax);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CLUSTER");
        if (indexSyntax == IndexSyntax.ON && table != null && indexName != null
                && !parenthesizedOptions && UtilityOption.isKeywordForm(options, KEYWORD_OPTIONS)) {
            UtilityOption.appendOptions(builder, options, false, KEYWORD_OPTIONS);
            return builder.append(' ').append(indexName).append(" ON ")
                    .append(table.getFullyQualifiedName());
        }
        UtilityOption.appendOptions(builder, options, parenthesizedOptions, KEYWORD_OPTIONS);
        if (table != null) {
            builder.append(' ').append(table.getFullyQualifiedName());
            if (indexName != null) {
                builder.append(" USING ").append(indexName);
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
