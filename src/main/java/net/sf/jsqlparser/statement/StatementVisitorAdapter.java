/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
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
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class StatementVisitorAdapter implements StatementVisitor {

    @Override
    public void visit(Comment comment) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Select select) {

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
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements stmts) {
        for (Statement statement : stmts.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement set) {

    }

    @Override
    public void visit(ShowColumnsStatement set) {
    }

    @Override
    public void visit(Merge merge) {

    }

    @Override
    public void visit(AlterView alterView) {
    }

    @Override
    public void visit(Upsert upsert) {
    }

    @Override
    public void visit(UseStatement use) {
    }

    @Override
    public void visit(Block block) {
    }

    @Override
    public void visit(ValuesStatement values) {
    }

    @Override
    public void visit(DescribeStatement describe) {
    }

    @Override
    public void visit(ExplainStatement aThis) {
    }

    @Override
    public void visit(ShowStatement aThis) {
    }

    @Override
    public void visit(DeclareStatement aThis) {
    }
}
