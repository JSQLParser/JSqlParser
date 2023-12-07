    /*-
     * #%L
     * JSQLParser library
     * %%
     * Copyright (C) 2004 - 2019 JSQLParser
     * %%
     * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
     * #L%
     */
package net.sf.jsqlparser.statement.refreshView;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * REFRESH MATERIALIZED VIEW [ CONCURRENTLY ] name
 *     [ WITH [ NO ] DATA ]
 *     
*  https://www.postgresql.org/docs/16/sql-refreshmaterializedview.html
* @author jxni-liguobin
*/

public class RefreshMaterializedViewStatement implements Statement {

    private String tableName;
    private RefreshMode refreshMode = RefreshMode.DEFAULT;
    private boolean concurrently;

    public RefreshMaterializedViewStatement() {
    }
    
    public RefreshMaterializedViewStatement(RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
    }

    public RefreshMaterializedViewStatement(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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
                builder.append(tableName);
                builder.append(" WITH DATA");
                break;
            case WITH_NO_DATA:
                builder.append(tableName);
                if (!concurrently) {
                    builder.append(" WITH NO DATA");
                }
                break;
            case DEFAULT:
                if (concurrently) {
                    builder.append("CONCURRENTLY ");
                }
                builder.append(tableName);
                break;
        }
        return builder.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public RefreshMaterializedViewStatement withTableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }
    public RefreshMaterializedViewStatement withConcurrently(boolean concurrently) {
        this.setConcurrently(concurrently);
        return this;
    }
}