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
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class StatementVisitorAdapter<T> implements StatementVisitor<T> {

    @Override
    public <S> T visit(Comment comment, S context) {

        return null;
    }

    @Override
    public <S> T visit(Commit commit, S context) {

        return null;
    }

    @Override
    public <S> T visit(Select select, S context) {

        return null;
    }

    @Override
    public <S> T visit(Delete delete, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedDelete delete, S context) {

        return null;
    }

    @Override
    public <S> T visit(Update update, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedUpdate update, S context) {

        return null;
    }

    @Override
    public <S> T visit(Insert insert, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedInsert insert, S context) {

        return null;
    }

    @Override
    public <S> T visit(Drop drop, S context) {

        return null;
    }

    @Override
    public <S> T visit(Truncate truncate, S context) {

        return null;
    }

    @Override
    public <S> T visit(CreateIndex createIndex, S context) {

        return null;
    }

    @Override
    public <S> T visit(CreateSchema createSchema, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateTable createTable, S context) {

        return null;
    }

    @Override
    public <S> T visit(CreateView createView, S context) {

        return null;
    }

    @Override
    public <S> T visit(Alter alter, S context) {

        return null;
    }

    @Override
    public <S> T visit(Statements statements, S context) {
        for (Statement statement : statements) {
            statement.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(Execute execute, S context) {

        return null;
    }

    @Override
    public <S> T visit(SetStatement set, S context) {

        return null;
    }

    @Override
    public <S> T visit(ResetStatement reset, S context) {

        return null;
    }

    @Override
    public <S> T visit(Merge merge, S context) {

        return null;
    }

    @Override
    public <S> T visit(AlterView alterView, S context) {
        return null;
    }

    @Override
    public <S> T visit(Upsert upsert, S context) {
        return null;
    }

    @Override
    public <S> T visit(UseStatement use, S context) {
        return null;
    }

    @Override
    public <S> T visit(Block block, S context) {
        return null;
    }

    @Override
    public <S> T visit(DescribeStatement describe, S context) {
        return null;
    }

    @Override
    public <S> T visit(ExplainStatement explainStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowStatement showStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowColumnsStatement showColumnsStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowIndexStatement showIndexStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(ShowTablesStatement showTables, S context) {
        return null;
    }

    @Override
    public <S> T visit(DeclareStatement declareStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(Grant grant, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateSequence createSequence, S context) {
        return null;
    }

    @Override
    public <S> T visit(AlterSequence alterSequence, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(CreateSynonym createSynonym, S context) {
        return null;
    }

    @Override
    public <S> T visit(Analyze analyze, S context) {

        return null;
    }

    @Override
    public <S> T visit(SavepointStatement savepointStatement, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(RollbackStatement rollbackStatement, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(AlterSession alterSession, S context) {
        // @todo: do something usefully here
        return null;
    }

    @Override
    public <S> T visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.getIfStatement().accept(this, context);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(RenameTableStatement renameTableStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(PurgeStatement purgeStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(AlterSystemStatement alterSystemStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(UnsupportedStatement unsupportedStatement, S context) {

        return null;
    }

    @Override
    public <S> T visit(RefreshMaterializedViewStatement materializedView, S context) {
        return null;
    }

    @Override
    public <S> T visit(Import imprt, S context) {
        return null;
    }
}
