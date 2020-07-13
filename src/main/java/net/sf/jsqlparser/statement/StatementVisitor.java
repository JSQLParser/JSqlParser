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
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public interface StatementVisitor {

    void visit(Comment comment);

    void visit(Commit commit);

    void visit(Delete delete);

    void visit(Update update);

    void visit(Insert insert);

    void visit(Replace replace);

    void visit(Drop drop);

    void visit(Truncate truncate);

    void visit(CreateIndex createIndex);

    void visit(CreateSchema aThis);

    void visit(CreateTable createTable);

    void visit(CreateView createView);

    void visit(AlterView alterView);

    void visit(Alter alter);

    void visit(Statements stmts);

    void visit(Execute execute);

    void visit(SetStatement set);

    void visit(ShowColumnsStatement set);

    void visit(ShowTablesStatement showTables);

    void visit(Merge merge);

    void visit(Select select);

    void visit(Upsert upsert);

    void visit(UseStatement use);

    void visit(Block block);

    void visit(ValuesStatement values);

    void visit(DescribeStatement describe);

    public void visit(ExplainStatement aThis);

    public void visit(ShowStatement aThis);

    public void visit(DeclareStatement aThis);

    void visit(Grant grant);

    void visit(CreateSequence createSequence);

    void visit(AlterSequence alterSequence);

    void visit(CreateFunctionalStatement createFunctionalStatement);
}
