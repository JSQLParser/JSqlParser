package net.sf.jsqlparser.expression.mysql;

import net.sf.jsqlparser.expression.MySqlSqlCalcFoundRows;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author sam
 */
public class StatementVisitorFactory {
    public static StatementVisitor create(final AtomicReference<MySqlSqlCalcFoundRows> ref) {
       return new StatementVisitor() {
           @Override
           public void visit(Commit commit) {

           }

           @Override
           public void visit(Delete delete) {

           }

           @Override
           public void visit(Update update) {

           }

           @Override
           public void visit(Insert insert) {

           }

           @Override
           public void visit(Replace replace) {

           }

           @Override
           public void visit(Drop drop) {

           }

           @Override
           public void visit(Truncate truncate) {

           }

           @Override
           public void visit(CreateIndex createIndex) {

           }

           @Override
           public void visit(CreateTable createTable) {

           }

           @Override
           public void visit(CreateView createView) {

           }

           @Override
           public void visit(AlterView alterView) {

           }

           @Override
           public void visit(Alter alter) {

           }

           @Override
           public void visit(Statements stmts) {

           }

           @Override
           public void visit(Execute execute) {

           }

           @Override
           public void visit(SetStatement set) {

           }

           @Override
           public void visit(Merge merge) {

           }

           @Override
           public void visit(Select select) {
               select.getSelectBody().accept(SelectVisitorFactory.create(ref));
           }

           @Override
           public void visit(Upsert upsert) {

           }
       };
    }
}
