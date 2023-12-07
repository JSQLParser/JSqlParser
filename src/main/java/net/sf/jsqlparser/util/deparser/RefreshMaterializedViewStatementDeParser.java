/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.refreshView.RefreshMaterializedViewStatement;

/**
*
* @author jxnu-liguobin
*/

public class RefreshMaterializedViewStatementDeParser extends AbstractDeParser<RefreshMaterializedViewStatement> {
    
    public RefreshMaterializedViewStatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    @Override
    public void deParse(RefreshMaterializedViewStatement view) {
        buffer.append("REFRESH MATERIALIZED VIEW ");
        switch (view.getRefreshType()){
            case WITH_DATA:
                if (view.isConcurrently()) {
                    buffer.append("CONCURRENTLY ");
                }
                buffer.append(view.getTableName());
                buffer.append(" WITH DATA");
                break;
            case WITH_NO_DATA:
                if (view.isConcurrently()) {
                    throw new IllegalArgumentException(
                            "CONCURRENTLY and WITH NO DATA may not be specified together.");
                }
                buffer.append(view.getTableName());
                buffer.append(" WITH NO DATA");
                break;
            case DEFAULT:
                buffer.append(view.getTableName());
                break;
        }
    }
    
}