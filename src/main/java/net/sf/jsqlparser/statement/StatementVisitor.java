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

public interface StatementVisitor<T> {

    T visit(Analyze analyze);

    T visit(SavepointStatement savepointStatement);

    T visit(RollbackStatement rollbackStatement);

    T visit(Comment comment);

    T visit(Commit commit);

    T visit(Delete delete);

    T visit(Update update);

    T visit(Insert insert);

    T visit(Drop drop);

    T visit(Truncate truncate);

    T visit(CreateIndex createIndex);

    T visit(CreateSchema aThis);

    T visit(CreateTable createTable);

    T visit(CreateView createView);

    T visit(AlterView alterView);

    T visit(RefreshMaterializedViewStatement materializedView);

    T visit(Alter alter);

    T visit(Statements stmts);

    T visit(Execute execute);

    T visit(SetStatement set);

    T visit(ResetStatement reset);

    T visit(ShowColumnsStatement set);

    T visit(ShowIndexStatement showIndex);

    T visit(ShowTablesStatement showTables);

    T visit(Merge merge);

    T visit(Select select);

    T visit(Upsert upsert);

    T visit(UseStatement use);

    T visit(Block block);

    T visit(DescribeStatement describe);

    T visit(ExplainStatement aThis);

    T visit(ShowStatement aThis);

    T visit(DeclareStatement aThis);

    T visit(Grant grant);

    T visit(CreateSequence createSequence);

    T visit(AlterSequence alterSequence);

    T visit(CreateFunctionalStatement createFunctionalStatement);

    T visit(CreateSynonym createSynonym);

    T visit(AlterSession alterSession);

    T visit(IfElseStatement aThis);

    T visit(RenameTableStatement renameTableStatement);

    T visit(PurgeStatement purgeStatement);

    T visit(AlterSystemStatement alterSystemStatement);

    T visit(UnsupportedStatement unsupportedStatement);
}
