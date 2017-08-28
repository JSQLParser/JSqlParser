package net.sf.jsqlparser.expression.mysql;

import net.sf.jsqlparser.expression.MySqlSqlCalcFoundRows;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author sam
 */
public class SelectVisitorFactory {
    public static SelectVisitor create(final AtomicReference<MySqlSqlCalcFoundRows> ref) {
        return new SelectVisitor() {
            @Override
            public void visit(PlainSelect plainSelect) {
                ref.set(plainSelect.getMySqlSqlCalcFoundRows());
            }

            @Override
            public void visit(SetOperationList setOpList) {

            }

            @Override
            public void visit(WithItem withItem) {

            }
        };
    }
}
