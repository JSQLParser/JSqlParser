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

import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;

/**
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementDeParser
        extends AbstractDeParser<RefreshMaterializedViewStatement> {

    public RefreshMaterializedViewStatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    @Override
    public void deParse(RefreshMaterializedViewStatement view) {
        builder.append("REFRESH MATERIALIZED VIEW ");
        if (view.getRefreshMode() == null) {
            if (view.isConcurrently()) {
                builder.append("CONCURRENTLY ");
            }
            builder.append(view.getView());
            return;
        }
        switch (view.getRefreshMode()) {
            case WITH_DATA:
                if (view.isConcurrently()) {
                    builder.append("CONCURRENTLY ");
                }
                builder.append(view.getView());
                builder.append(" WITH DATA");
                break;
            case WITH_NO_DATA:
                builder.append(view.getView());
                if (view.isConcurrently()) {
                    builder.append(" WITH NO DATA");
                }
                break;
        }
    }

}
