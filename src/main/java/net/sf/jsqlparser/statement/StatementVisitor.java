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

public interface StatementVisitor {

    default void visit(Comment comment) { // default implementation ignored
    }

    default void visit(Commit commit) { // default implementation ignored
    }

    default void visit(Delete delete) { // default implementation ignored
    }

    default void visit(Update update) { // default implementation ignored
    }

    default void visit(Insert insert) { // default implementation ignored
    }

    default void visit(Replace replace) { // default implementation ignored
    }

    default void visit(Drop drop) { // default implementation ignored
    }

    default void visit(Truncate truncate) { // default implementation ignored
    }

    default void visit(CreateIndex createIndex) { // default implementation ignored
    }

    default void visit(CreateTable createTable) { // default implementation ignored
    }

    default void visit(CreateView createView) { // default implementation ignored
    }

    default void visit(AlterView alterView) { // default implementation ignored
    }

    default void visit(Alter alter) { // default implementation ignored
    }

    default void visit(Statements stmts) { // default implementation ignored
    }

    default void visit(Execute execute) { // default implementation ignored
    }

    default void visit(SetStatement set) { // default implementation ignored
    }

    default void visit(ShowColumnsStatement set) { // default implementation ignored
    }

    default void visit(Merge merge) { // default implementation ignored
    }

    default void visit(Select select) { // default implementation ignored
    }

    default void visit(Upsert upsert) { // default implementation ignored
    }

    default void visit(UseStatement use) { // default implementation ignored
    }

    default void visit(Block block) { // default implementation ignored
    }

    default void visit(ValuesStatement values) { // default implementation ignored
    }

    default void visit(DescribeStatement describe) { // default implementation ignored
    }

    default void visit(ExplainStatement aThis) { // default implementation ignored
    }

    default void visit(ShowStatement aThis) { // default implementation ignored
    }
}
