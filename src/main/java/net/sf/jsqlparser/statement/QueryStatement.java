/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package net.sf.jsqlparser.statement;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public abstract class QueryStatement extends StatementImpl {
    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public StatementType getStatementType() {
        return StatementType.QUERY;
    }
}
