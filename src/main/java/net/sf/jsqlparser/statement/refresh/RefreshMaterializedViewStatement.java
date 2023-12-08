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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * REFRESH MATERIALIZED VIEW [ CONCURRENTLY ] name [ WITH [ NO ] DATA ]
 * 
 * https://www.postgresql.org/docs/16/sql-refreshmaterializedview.html
 * 
 * @author jxni-liguobin
 */

public class RefreshMaterializedViewStatement implements Statement {

    private Table view;
    private RefreshMode refreshMode = RefreshMode.DEFAULT;
    private boolean concurrently;

    public RefreshMaterializedViewStatement() {}

    public RefreshMaterializedViewStatement(RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
    }

    public RefreshMaterializedViewStatement(Table view) {
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

    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("REFRESH MATERIALIZED VIEW ");
        switch (this.refreshMode) {
            case WITH_DATA:
                if (concurrently) {
                    builder.append("CONCURRENTLY ");
                }
                builder.append(view);
                builder.append(" WITH DATA");
                break;
            case WITH_NO_DATA:
                builder.append(view);
                if (!concurrently) {
                    builder.append(" WITH NO DATA");
                }
                break;
            case DEFAULT:
                if (concurrently) {
                    builder.append("CONCURRENTLY ");
                }
                builder.append(view);
                break;
        }
        return builder.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
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
