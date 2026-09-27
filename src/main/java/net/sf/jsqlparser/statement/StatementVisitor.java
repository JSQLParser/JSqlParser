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

import net.sf.jsqlparser.statement.create.fdw.CreateForeignDataWrapper;
import net.sf.jsqlparser.statement.alter.AlterForeignDataWrapper;
import net.sf.jsqlparser.statement.create.server.CreateServer;
import net.sf.jsqlparser.statement.alter.AlterServer;
import net.sf.jsqlparser.statement.create.usermapping.CreateUserMapping;
import net.sf.jsqlparser.statement.alter.AlterUserMapping;
import net.sf.jsqlparser.statement.alter.AlterPolicy;
import net.sf.jsqlparser.statement.drop.DropPolicy;
import net.sf.jsqlparser.statement.create.statistics.CreateStatistics;
import net.sf.jsqlparser.statement.alter.AlterStatistics;
import net.sf.jsqlparser.statement.alter.AlterRelation;
import net.sf.jsqlparser.statement.alter.AlterTablespaceMove;
import net.sf.jsqlparser.statement.alter.database.AlterDatabase;
import net.sf.jsqlparser.statement.alter.schema.AlterSchema;
import net.sf.jsqlparser.statement.oracle.OracleBlock;
import net.sf.jsqlparser.statement.oracle.OracleAssignment;
import net.sf.jsqlparser.statement.oracle.OracleNullStatement;

import net.sf.jsqlparser.statement.role.CreateRole;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.statement.grant.Revoke;
import net.sf.jsqlparser.statement.grant.AlterDefaultPrivileges;
import net.sf.jsqlparser.statement.create.type.CreateType;
import net.sf.jsqlparser.statement.alter.AlterType;
import net.sf.jsqlparser.statement.create.domain.CreateDomain;
import net.sf.jsqlparser.statement.alter.AlterDomain;
import net.sf.jsqlparser.statement.create.extension.CreateExtension;
import net.sf.jsqlparser.statement.alter.AlterExtension;
import net.sf.jsqlparser.statement.create.publication.CreatePublication;
import net.sf.jsqlparser.statement.alter.AlterPublication;
import net.sf.jsqlparser.statement.create.subscription.CreateSubscription;
import net.sf.jsqlparser.statement.alter.AlterSubscription;

import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.database.CreateDatabase;
import net.sf.jsqlparser.statement.create.event.AlterEvent;
import net.sf.jsqlparser.statement.create.event.CreateEvent;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.create.user.CreateUser;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.export.Export;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.InsertBulk;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.create.macro.CreateMacro;
import net.sf.jsqlparser.statement.create.extension.CreateExtensionRepository;
import net.sf.jsqlparser.statement.export.ExportDataStatement;
import net.sf.jsqlparser.statement.load.LoadDataStatement;

public interface StatementVisitor<T> {

    default <S> T visit(InsertBulk statement, S context) {
        return null;
    }

    default void visit(InsertBulk statement) {
        this.visit(statement, null);
    }

    <S> T visit(Analyze analyze, S context);

    default void visit(Analyze analyze) {
        this.visit(analyze, null);
    }

    <S> T visit(SavepointStatement savepointStatement, S context);

    default void visit(SavepointStatement savepointStatement) {
        this.visit(savepointStatement, null);
    }

    <S> T visit(RollbackStatement rollbackStatement, S context);

    default void visit(RollbackStatement rollbackStatement) {
        this.visit(rollbackStatement, null);
    }

    <S> T visit(Comment comment, S context);

    default void visit(Comment comment) {
        this.visit(comment, null);
    }

    <S> T visit(Commit commit, S context);

    default void visit(Commit commit) {
        this.visit(commit, null);
    }

    <S> T visit(Delete delete, S context);

    default void visit(Delete delete) {
        this.visit(delete, null);
    }

    <S> T visit(Update update, S context);

    default void visit(Update update) {
        this.visit(update, null);
    }

    <S> T visit(Insert insert, S context);

    default void visit(Insert insert) {
        this.visit(insert, null);
    }

    <S> T visit(Drop drop, S context);

    default void visit(Drop drop) {
        this.visit(drop, null);
    }

    <S> T visit(Truncate truncate, S context);

    default void visit(Truncate truncate) {
        this.visit(truncate, null);
    }

    <S> T visit(CreateIndex createIndex, S context);

    default void visit(CreateIndex createIndex) {
        this.visit(createIndex, null);
    }

    <S> T visit(CreateSchema createSchema, S context);

    default void visit(CreateSchema createSchema) {
        this.visit(createSchema, null);
    }

    <S> T visit(CreateDatabase createDatabase, S context);

    default void visit(CreateDatabase createDatabase) {
        this.visit(createDatabase, null);
    }

    <S> T visit(CreateUser createUser, S context);

    default void visit(CreateUser createUser) {
        this.visit(createUser, null);
    }

    <S> T visit(CreateEvent createEvent, S context);

    default void visit(CreateEvent createEvent) {
        this.visit(createEvent, null);
    }

    <S> T visit(CreateTable createTable, S context);

    default void visit(CreateTable createTable) {
        this.visit(createTable, null);
    }

    <S> T visit(CreateTrigger createTrigger, S context);

    default void visit(CreateTrigger createTrigger) {
        this.visit(createTrigger, null);
    }

    <S> T visit(CreateView createView, S context);

    default void visit(CreateView createView) {
        this.visit(createView, null);
    }

    <S> T visit(AlterView alterView, S context);

    default void visit(AlterView alterView) {
        this.visit(alterView, null);
    }

    <S> T visit(RefreshMaterializedViewStatement materializedView, S context);

    default void visit(RefreshMaterializedViewStatement materializedView) {
        this.visit(materializedView, null);
    }

    <S> T visit(Alter alter, S context);

    default void visit(Alter alter) {
        this.visit(alter, null);
    }

    <S> T visit(AlterEvent alterEvent, S context);

    default void visit(AlterEvent alterEvent) {
        this.visit(alterEvent, null);
    }

    <S> T visit(Statements statements, S context);

    default void visit(Statements statements) {
        this.visit(statements, null);
    }

    <S> T visit(Execute execute, S context);

    default <S> T visit(DoStatement statement, S context) {
        return null;
    }

    default void visit(DoStatement statement) {
        this.visit(statement, null);
    }

    default void visit(Execute execute) {
        this.visit(execute, null);
    }

    <S> T visit(SetStatement set, S context);

    default void visit(SetStatement set) {
        this.visit(set, null);
    }

    <S> T visit(ResetStatement reset, S context);

    default void visit(ResetStatement reset) {
        this.visit(reset, null);
    }

    <S> T visit(ShowColumnsStatement showColumns, S context);

    default void visit(ShowColumnsStatement showColumns) {
        this.visit(showColumns, null);
    }

    <S> T visit(ShowIndexStatement showIndex, S context);

    default void visit(ShowIndexStatement showIndex) {
        this.visit(showIndex, null);
    }

    <S> T visit(ShowTablesStatement showTables, S context);

    default void visit(ShowTablesStatement showTables) {
        this.visit(showTables, null);
    }

    <S> T visit(Merge merge, S context);

    default void visit(Merge merge) {
        this.visit(merge, null);
    }

    <S> T visit(Select select, S context);

    default void visit(Select select) {
        this.visit(select, null);
    }

    <S> T visit(Upsert upsert, S context);

    default void visit(Upsert upsert) {
        this.visit(upsert, null);
    }

    default <S> T visit(SetIdentityInsertStatement statement, S context) {
        return null;
    }

    default void visit(SetIdentityInsertStatement statement) {
        visit(statement, null);
    }

    <S> T visit(UseStatement use, S context);

    default void visit(UseStatement use) {
        this.visit(use, null);
    }

    <S> T visit(Block block, S context);

    default void visit(Block block) {
        this.visit(block, null);
    }

    <S> T visit(DescribeStatement describe, S context);

    default void visit(DescribeStatement describe) {
        this.visit(describe, null);
    }

    <S> T visit(ExplainStatement explainStatement, S context);

    default void visit(ExplainStatement explainStatement) {
        this.visit(explainStatement, null);
    }

    <S> T visit(ShowStatement showStatement, S context);

    default void visit(ShowStatement showStatement) {
        this.visit(showStatement, null);
    }

    <S> T visit(DeclareStatement declareStatement, S context);

    default void visit(DeclareStatement declareStatement) {
        this.visit(declareStatement, null);
    }

    <S> T visit(Grant grant, S context);

    default void visit(Grant grant) {
        this.visit(grant, null);
    }

    <S> T visit(CreateSequence createSequence, S context);

    default void visit(CreateSequence createSequence) {
        this.visit(createSequence, null);
    }

    <S> T visit(AlterSequence alterSequence, S context);

    default void visit(AlterSequence alterSequence) {
        this.visit(alterSequence, null);
    }

    <S> T visit(CreateFunctionalStatement createFunctionalStatement, S context);

    default void visit(CreateFunctionalStatement createFunctionalStatement) {
        this.visit(createFunctionalStatement, null);
    }

    <S> T visit(CreateSynonym createSynonym, S context);

    default void visit(CreateSynonym createSynonym) {
        this.visit(createSynonym, null);
    }

    <S> T visit(AlterSession alterSession, S context);

    default void visit(AlterSession alterSession) {
        this.visit(alterSession, null);
    }

    <S> T visit(IfElseStatement ifElseStatement, S context);

    default void visit(IfElseStatement ifElseStatement) {
        this.visit(ifElseStatement, null);
    }

    <S> T visit(RenameTableStatement renameTableStatement, S context);

    default void visit(RenameTableStatement renameTableStatement) {
        this.visit(renameTableStatement, null);
    }

    <S> T visit(PragmaStatement pragmaStatement, S context);

    default void visit(PragmaStatement pragmaStatement) {
        this.visit(pragmaStatement, null);
    }

    <S> T visit(ExtensionStatement extensionStatement, S context);

    default void visit(ExtensionStatement extensionStatement) {
        this.visit(extensionStatement, null);
    }

    <S> T visit(AttachStatement attachStatement, S context);

    default void visit(AttachStatement attachStatement) {
        this.visit(attachStatement, null);
    }

    <S> T visit(DetachStatement detachStatement, S context);

    default void visit(DetachStatement detachStatement) {
        this.visit(detachStatement, null);
    }

    <S> T visit(ConnectStatement connectStatement, S context);

    default void visit(ConnectStatement connectStatement) {
        this.visit(connectStatement, null);
    }

    <S> T visit(DisconnectStatement disconnectStatement, S context);

    default void visit(DisconnectStatement disconnectStatement) {
        this.visit(disconnectStatement, null);
    }

    <S> T visit(PrepareStatement prepareStatement, S context);

    default void visit(PrepareStatement prepareStatement) {
        this.visit(prepareStatement, null);
    }

    <S> T visit(DeallocateStatement deallocateStatement, S context);

    default void visit(DeallocateStatement deallocateStatement) {
        this.visit(deallocateStatement, null);
    }

    <S> T visit(CopyStatement copyStatement, S context);

    default void visit(CopyStatement copyStatement) {
        this.visit(copyStatement, null);
    }

    <S> T visit(CreateMacro createMacro, S context);

    default void visit(CreateMacro createMacro) {
        this.visit(createMacro, null);
    }

    <S> T visit(CreateExtensionRepository createExtensionRepository, S context);

    default void visit(CreateExtensionRepository createExtensionRepository) {
        this.visit(createExtensionRepository, null);
    }

    <S> T visit(AssertStatement assertStatement, S context);

    default void visit(AssertStatement assertStatement) {
        this.visit(assertStatement, null);
    }

    <S> T visit(ExportDataStatement exportDataStatement, S context);

    default void visit(ExportDataStatement exportDataStatement) {
        this.visit(exportDataStatement, null);
    }

    <S> T visit(LoadDataStatement loadDataStatement, S context);

    default void visit(LoadDataStatement loadDataStatement) {
        this.visit(loadDataStatement, null);
    }

    <S> T visit(PurgeStatement purgeStatement, S context);

    default void visit(PurgeStatement purgeStatement) {
        this.visit(purgeStatement, null);
    }

    <S> T visit(AlterSystemStatement alterSystemStatement, S context);

    default void visit(AlterSystemStatement alterSystemStatement) {
        this.visit(alterSystemStatement, null);
    }

    <S> T visit(UnsupportedStatement unsupportedStatement, S context);

    default void visit(UnsupportedStatement unsupportedStatement) {
        this.visit(unsupportedStatement, null);
    }

    <S> T visit(ParenthesedInsert parenthesedInsert, S context);

    default void visit(ParenthesedInsert parenthesedInsert) {
        this.visit(parenthesedInsert, null);
    }

    <S> T visit(ParenthesedUpdate parenthesedUpdate, S context);

    default void visit(ParenthesedUpdate parenthesedUpdate) {
        this.visit(parenthesedUpdate, null);
    }

    <S> T visit(ParenthesedDelete parenthesedDelete, S context);

    default void visit(ParenthesedDelete parenthesedDelete) {
        this.visit(parenthesedDelete, null);
    }

    <S> T visit(SessionStatement sessionStatement, S context);

    default void visit(SessionStatement sessionStatement) {
        this.visit(sessionStatement, null);
    }

    <S> T visit(Import imprt, S context);

    default void visit(Import imprt) {
        this.visit(imprt, null);
    }

    <S> T visit(Export export, S context);

    default void visit(Export export) {
        this.visit(export, null);
    }

    <S> T visit(LockStatement lock, S context);

    default void visit(LockStatement lock) {
        this.visit(lock, null);
    }

    <S> T visit(CreatePolicy createPolicy, S context);

    default void visit(CreatePolicy createPolicy) {
        this.visit(createPolicy, null);
    }


    default <S> T visit(CreateRole statement, S context) {
        return null;
    }

    default void visit(CreateRole statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterSchema statement, S context) {
        return null;
    }

    default void visit(AlterSchema statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterRole statement, S context) {
        return null;
    }

    default void visit(AlterRole statement) {
        visit(statement, null);
    }

    default <S> T visit(Revoke statement, S context) {
        return null;
    }

    default void visit(Revoke statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterDefaultPrivileges statement, S context) {
        return null;
    }

    default void visit(AlterDefaultPrivileges statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateType statement, S context) {
        return null;
    }

    default void visit(CreateType statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterType statement, S context) {
        return null;
    }

    default void visit(AlterType statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateDomain statement, S context) {
        return null;
    }

    default void visit(CreateDomain statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterDomain statement, S context) {
        return null;
    }

    default void visit(AlterDomain statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateExtension statement, S context) {
        return null;
    }

    default void visit(CreateExtension statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterExtension statement, S context) {
        return null;
    }

    default void visit(AlterExtension statement) {
        visit(statement, null);
    }

    default <S> T visit(CreatePublication statement, S context) {
        return null;
    }

    default void visit(CreatePublication statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterPublication statement, S context) {
        return null;
    }

    default void visit(AlterPublication statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateSubscription statement, S context) {
        return null;
    }

    default void visit(CreateSubscription statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterSubscription statement, S context) {
        return null;
    }

    default void visit(AlterSubscription statement) {
        visit(statement, null);
    }

    default <S> T visit(OracleBlock block, S context) {
        return visit((Block) block, context);
    }

    default void visit(OracleBlock block) {
        visit(block, null);
    }

    default <S> T visit(OracleAssignment assignment, S context) {
        return null;
    }

    default void visit(OracleAssignment assignment) {
        visit(assignment, null);
    }

    default <S> T visit(OracleNullStatement statement, S context) {
        return null;
    }

    default void visit(OracleNullStatement statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterRelation statement, S context) {
        return null;
    }

    default void visit(AlterRelation statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterDatabase statement, S context) {
        return null;
    }

    default void visit(AlterDatabase statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterTablespaceMove statement, S context) {
        return null;
    }

    default void visit(AlterTablespaceMove statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterPolicy statement, S context) {
        return null;
    }

    default void visit(AlterPolicy statement) {
        visit(statement, null);
    }

    default <S> T visit(DropPolicy statement, S context) {
        return null;
    }

    default void visit(DropPolicy statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateStatistics statement, S context) {
        return null;
    }

    default void visit(CreateStatistics statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterStatistics statement, S context) {
        return null;
    }

    default void visit(AlterStatistics statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateForeignDataWrapper statement, S context) {
        return null;
    }

    default void visit(CreateForeignDataWrapper statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterForeignDataWrapper statement, S context) {
        return null;
    }

    default void visit(AlterForeignDataWrapper statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateServer statement, S context) {
        return null;
    }

    default void visit(CreateServer statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterServer statement, S context) {
        return null;
    }

    default void visit(AlterServer statement) {
        visit(statement, null);
    }

    default <S> T visit(CreateUserMapping statement, S context) {
        return null;
    }

    default void visit(CreateUserMapping statement) {
        visit(statement, null);
    }

    default <S> T visit(AlterUserMapping statement, S context) {
        return null;
    }

    default void visit(AlterUserMapping statement) {
        visit(statement, null);
    }
}
