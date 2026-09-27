/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.refresh;

import java.util.function.Consumer;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * REFRESH MATERIALIZED VIEW [ CONCURRENTLY ] name [ WITH [ NO ] DATA ]
 * <p>
 * https://www.postgresql.org/docs/16/sql-refreshmaterializedview.html
 *
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatement implements Statement {

    private Table view;
    private RefreshMode refreshMode;
    private boolean concurrently = false;

    public RefreshMaterializedViewStatement() {}

    public RefreshMaterializedViewStatement(Table view, boolean concurrently,
            RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
        this.concurrently = concurrently;
        this.view = view;
    }

    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
    }

    public RefreshMode getRefreshMode() {
        return refreshMode;
    }

    public void setRefreshMode(RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
    }

    public boolean isConcurrently() {
        return concurrently;
    }

    public void setConcurrently(boolean concurrently) {
        this.concurrently = concurrently;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> tablePrinter) {
        builder.append("REFRESH MATERIALIZED VIEW ");
        if (concurrently) {
            builder.append("CONCURRENTLY ");
        }
        tablePrinter.accept(view);
        if (refreshMode == RefreshMode.WITH_DATA) {
            builder.append(" WITH DATA");
        } else if (refreshMode == RefreshMode.WITH_NO_DATA) {
            builder.append(" WITH NO DATA");
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public RefreshMaterializedViewStatement withTableName(Table view) {
        this.setView(view);
        return this;
    }

    public RefreshMaterializedViewStatement withConcurrently(boolean concurrently) {
        this.setConcurrently(concurrently);
        return this;
    }
}
