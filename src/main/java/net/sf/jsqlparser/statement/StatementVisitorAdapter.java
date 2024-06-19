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
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class StatementVisitorAdapter<T> implements StatementVisitor<T> {

    @Override
    public T visit(Comment comment) {

        return null;
    }

    @Override
    public T visit(Commit commit) {

        return null;
    }

    @Override
    public T visit(Select select) {

        return null;
    }

    @Override
    public T visit(Delete delete) {

        return null;
    }

    @Override
    public T visit(Update update) {

        return null;
    }

    @Override
    public T visit(Insert insert) {

        return null;
    }

    @Override
    public T visit(Drop drop) {

        return null;
    }

    @Override
    public T visit(Truncate truncate) {

        return null;
    }

    @Override
    public T visit(CreateIndex createIndex) {

        return null;
    }

    @Override
    public T visit(CreateSchema aThis) {
        return null;
    }

    @Override
    public T visit(CreateTable createTable) {

        return null;
    }

    @Override
    public T visit(CreateView createView) {

        return null;
    }

    @Override
    public T visit(Alter alter) {

        return null;
    }

    @Override
    public T visit(Statements stmts) {
        for (Statement statement : stmts) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public T visit(Execute execute) {

        return null;
    }

    @Override
    public T visit(SetStatement set) {

        return null;
    }

    @Override
    public T visit(ResetStatement reset) {

        return null;
    }

    @Override
    public T visit(Merge merge) {

        return null;
    }

    @Override
    public T visit(AlterView alterView) {
        return null;
    }

    @Override
    public T visit(Upsert upsert) {
        return null;
    }

    @Override
    public T visit(UseStatement use) {
        return null;
    }

    @Override
    public T visit(Block block) {
        return null;
    }

    @Override
    public T visit(DescribeStatement describe) {
        return null;
    }

    @Override
    public T visit(ExplainStatement aThis) {
        return null;
    }

    @Override
    public T visit(ShowStatement aThis) {
        return null;
    }

    @Override
    public T visit(ShowColumnsStatement set) {
        return null;
    }

    @Override
    public T visit(ShowIndexStatement set) {
        return null;
    }

    @Override
    public T visit(ShowTablesStatement showTables) {
        return null;
    }

    @Override
    public T visit(DeclareStatement aThis) {
        return null;
    }

    @Override
    public T visit(Grant grant) {
        return null;
    }

    @Override
    public T visit(CreateSequence createSequence) {
        return null;
    }

    @Override
    public T visit(AlterSequence alterSequence) {
        return null;
    }

    @Override
    public T visit(CreateFunctionalStatement createFunctionalStatement) {
        return null;
    }

    @Override
    public T visit(CreateSynonym createSynonym) {
        return null;
    }

    @Override
    public T visit(Analyze analyze) {

        return null;
    }

    @Override
    public T visit(SavepointStatement savepointStatement) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public T visit(RollbackStatement rollbackStatement) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public T visit(AlterSession alterSession) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public T visit(IfElseStatement ifElseStatement) {
        ifElseStatement.getIfStatement().accept(this);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this);
        }
        return null;
    }

    @Override
    public T visit(RenameTableStatement renameTableStatement) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public T visit(PurgeStatement purgeStatement) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public T visit(AlterSystemStatement alterSystemStatement) {
        return null;
    }

    @Override
    public T visit(UnsupportedStatement unsupportedStatement) {

        return null;
    }

    @Override
    public T visit(RefreshMaterializedViewStatement materializedView) {

        return null;
    }
}
