/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.statement.oracle.OracleBlock;
import net.sf.jsqlparser.statement.oracle.OracleAssignment;
import net.sf.jsqlparser.statement.oracle.OracleNullStatement;

import net.sf.jsqlparser.statement.role.CreateRole;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.statement.role.RoleOption;
import net.sf.jsqlparser.statement.grant.Revoke;
import net.sf.jsqlparser.statement.grant.AlterDefaultPrivileges;
import net.sf.jsqlparser.schema.Table;

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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DoStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SessionStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.SetIdentityInsertStatement;
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
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
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
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import net.sf.jsqlparser.statement.AssertStatement;
import net.sf.jsqlparser.statement.export.ExportDataStatement;
import net.sf.jsqlparser.statement.load.LoadDataStatement;

/**
 * @author gitmotte
 */
public class StatementValidator extends AbstractValidator<Statement>
        implements StatementVisitor<Void> {
    @Override
    public <S> Void visit(CreateIndex createIndex, S context) {
        getValidator(CreateIndexValidator.class).validate(createIndex);
        return null;
    }

    @Override
    public <S> Void visit(CreateTable createTable, S context) {
        getValidator(CreateTableValidator.class).validate(createTable);
        return null;
    }

    @Override
    public <S> Void visit(CreateTrigger createTrigger, S context) {
        if (createTrigger.getRoutine() != null) {
            validateFeature(Feature.createTrigger);
        }
        createTrigger.visit(this::validateOptionalFromItem,
                this::validateOptionalExpression);
        if (createTrigger.getBody() != null) {
            createTrigger.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateView createView, S context) {
        getValidator(CreateViewValidator.class).validate(createView);
        return null;
    }

    @Override
    public <S> Void visit(AlterView alterView, S context) {
        getValidator(AlterViewValidator.class).validate(alterView);
        return null;
    }

    @Override
    public <S> Void visit(RefreshMaterializedViewStatement materializedView, S context) {
        getValidator(RefreshMaterializedViewStatementValidator.class).validate(materializedView);
        return null;
    }

    @Override
    public <S> Void visit(Delete delete, S context) {
        getValidator(DeleteValidator.class).validate(delete);
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedDelete delete, S context) {
        return visit(delete.getDelete(), context);
    }

    @Override
    public <S> Void visit(SessionStatement sessionStatement, S context) {
        return null;
    }


    @Override
    public <S> Void visit(Drop drop, S context) {
        getValidator(DropValidator.class).validate(drop);
        return null;
    }

    @Override
    public <S> Void visit(Insert insert, S context) {
        getValidator(InsertValidator.class).validate(insert);
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedInsert insert, S context) {
        return visit(insert.getInsert(), context);
    }

    @Override
    public <S> Void visit(Select select, S context) {
        validateFeature(Feature.select);

        SelectValidator selectValidator = getValidator(SelectValidator.class);
        select.accept((SelectVisitor<Void>) selectValidator, null);
        return null;
    }

    @Override
    public <S> Void visit(Truncate truncate, S context) {
        validateFeature(Feature.truncate);
        validateOptionalFromItem(truncate.getTable());
        return null;
    }

    @Override
    public <S> Void visit(Update update, S context) {
        getValidator(UpdateValidator.class).validate(update);
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedUpdate update, S context) {
        return visit(update.getUpdate(), context);
    }

    @Override
    public <S> Void visit(Alter alter, S context) {
        getValidator(AlterValidator.class).validate(alter);
        return null;
    }

    @Override
    public <S> Void visit(AlterEvent alterEvent, S context) {
        if (alterEvent.getBody() != null) {
            alterEvent.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(Statements statements, S context) {
        statements.forEach(s -> s.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(Execute execute, S context) {
        getValidator(ExecuteValidator.class).validate(execute);
        return null;
    }

    @Override
    public <S> Void visit(DoStatement statement, S context) {
        validateFeature(Feature.doStatement);
        return null;
    }

    @Override
    public <S> Void visit(InsertBulk statement, S context) {
        getValidator(InsertBulkValidator.class).validate(statement);
        return null;
    }

    @Override
    public <S> Void visit(SetStatement set, S context) {
        getValidator(SetStatementValidator.class).validate(set);
        return null;
    }

    @Override
    public <S> Void visit(ResetStatement reset, S context) {
        getValidator(ResetStatementValidator.class).validate(reset);
        return null;
    }

    @Override
    public <S> Void visit(Merge merge, S context) {
        getValidator(MergeValidator.class).validate(merge);
        return null;
    }

    @Override
    public <S> Void visit(Commit commit, S context) {
        validateFeature(Feature.commit);
        return null;
    }

    @Override
    public <S> Void visit(Upsert upsert, S context) {
        getValidator(UpsertValidator.class).validate(upsert);
        return null;
    }

    @Override
    public <S> Void visit(SetIdentityInsertStatement statement, S context) {
        getValidator(SetIdentityInsertValidator.class).validate(statement);
        return null;
    }

    @Override
    public <S> Void visit(UseStatement use, S context) {
        getValidator(UseStatementValidator.class).validate(use);
        return null;
    }

    @Override
    public <S> Void visit(ShowStatement showStatement, S context) {
        getValidator(ShowStatementValidator.class).validate(showStatement);
        return null;
    }

    @Override
    public <S> Void visit(ShowColumnsStatement show, S context) {
        getValidator(ShowColumnsStatementValidator.class).validate(show);
        return null;
    }

    @Override
    public <S> Void visit(ShowIndexStatement show, S context) {
        getValidator(ShowIndexStatementValidator.class).validate(show);
        return null;
    }

    @Override
    public <S> Void visit(ShowTablesStatement showTables, S context) {
        getValidator(ShowTablesStatementValidator.class).validate(showTables);
        return null;
    }

    @Override
    public <S> Void visit(Block block, S context) {
        validateFeature(Feature.block);
        block.getStatements().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Comment comment, S context) {
        getValidator(CommentValidator.class).validate(comment);
        return null;
    }

    @Override
    public <S> Void visit(DescribeStatement describe, S context) {
        validateFeature(Feature.describe);
        validateFeature(Feature.desc);
        validateOptionalFromItem(describe.getTable());
        return null;
    }

    @Override
    public <S> Void visit(ExplainStatement explainStatement, S context) {
        validateFeature(Feature.explain);
        if (explainStatement.getStatement() != null) {
            explainStatement.getStatement().accept(this, context);
        }
        return null;
    }


    @Override
    public <S> Void visit(DeclareStatement declareStatement, S context) {
        getValidator(DeclareStatementValidator.class).validate(declareStatement);
        return null;
    }

    @Override
    public <S> Void visit(Grant grant, S context) {
        getValidator(GrantValidator.class).validate(grant);
        return null;
    }

    @Override
    public <S> Void visit(CreateSchema aThis, S context) {
        validateFeatureAndName(Feature.createSchema, NamedObject.schema, aThis.getSchemaName());
        aThis.getStatements().forEach(s -> s.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(CreateDatabase aThis, S context) {
        validateFeatureAndName(Feature.createDatabase, NamedObject.database,
                aThis.getDatabaseName());
        return null;
    }

    @Override
    public <S> Void visit(CreateUser createUser, S context) {
        return null;
    }

    @Override
    public <S> Void visit(CreateEvent createEvent, S context) {
        if (createEvent.getBody() != null) {
            createEvent.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateSequence createSequence, S context) {
        getValidator(CreateSequenceValidator.class).validate(createSequence);
        return null;
    }

    @Override
    public <S> Void visit(AlterSequence alterSequence, S context) {
        getValidator(AlterSequenceValidator.class).validate(alterSequence);
        return null;
    }

    @Override
    public <S> Void visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        validateFeature(Feature.functionalStatement);
        if (createFunctionalStatement
                .getOperation() == CreateFunctionalStatement.Operation.CREATE_OR_ALTER) {
            validateFeature(Feature.createOrAlterRoutine);
        }
        if (createFunctionalStatement.getReturnType() != null
                && createFunctionalStatement.getReturnType().getTableElements() != null) {
            createFunctionalStatement.getReturnType().getTableElements()
                    .forEach(element -> net.sf.jsqlparser.util.TableDefinitionTraversal.visit(
                            element,
                            this::validateOptionalExpression, this::validateOptionalFromItem));
        }
        if (createFunctionalStatement instanceof CreateFunction) {
            validateFeature(createFunctionalStatement
                    .getOperation() == CreateFunctionalStatement.Operation.ALTER
                            ? Feature.alterFunction
                            : Feature.createFunction);
        } else if (createFunctionalStatement instanceof CreateProcedure) {
            validateFeature(createFunctionalStatement
                    .getOperation() == CreateFunctionalStatement.Operation.ALTER
                            ? Feature.alterProcedure
                            : Feature.createProcedure);
        }
        return null;
    }

    @Override
    public void validate(Statement statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> Void visit(CreateSynonym createSynonym, S context) {
        getValidator(CreateSynonymValidator.class).validate(createSynonym);
        return null;
    }

    @Override
    public <S> Void visit(Analyze analyze, S context) {
        getValidator(AnalyzeValidator.class).validate(analyze);
        return null;
    }

    @Override
    public <S> Void visit(SavepointStatement savepointStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(RollbackStatement rollbackStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(AlterSession alterSession, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.getIfStatement().accept(this, context);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this, context);
        }
        return null;
    }

    public <S> Void visit(RenameTableStatement renameTableStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(AssertStatement assertStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(ExportDataStatement exportDataStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(LoadDataStatement loadDataStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(PurgeStatement purgeStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(AlterSystemStatement alterSystemStatement, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(UnsupportedStatement unsupportedStatement, S context) {

        return null;
    }

    @Override
    public <S> Void visit(Import imprt, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(Export export, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public <S> Void visit(LockStatement lock, S context) {
        // TODO: not yet implemented
        return null;
    }

    public void visit(CreateIndex createIndex) {
        visit(createIndex, null);
    }

    public void visit(CreateTable createTable) {
        visit(createTable, null);
    }

    public void visit(CreateTrigger createTrigger) {
        visit(createTrigger, null);
    }

    public void visit(CreateView createView) {
        visit(createView, null);
    }

    public void visit(AlterView alterView) {
        visit(alterView, null);
    }

    public void visit(RefreshMaterializedViewStatement materializedView) {
        visit(materializedView, null);
    }

    public void visit(Delete delete) {
        visit(delete, null);
    }

    public void visit(Drop drop) {
        visit(drop, null);
    }

    public void visit(Insert insert) {
        visit(insert, null);
    }

    public void visit(Select select) {
        visit(select, null);
    }

    public void visit(Truncate truncate) {
        visit(truncate, null);
    }

    public void visit(Update update) {
        visit(update, null);
    }

    public void visit(Alter alter) {
        visit(alter, null);
    }

    public void visit(AlterEvent alterEvent) {
        visit(alterEvent, null);
    }

    public void visit(Statements statements) {
        visit(statements, null);
    }

    public void visit(Execute execute) {
        visit(execute, null);
    }

    public void visit(SetStatement set) {
        visit(set, null);
    }

    public void visit(ResetStatement reset) {
        visit(reset, null);
    }

    public void visit(Merge merge) {
        visit(merge, null);
    }

    public void visit(Commit commit) {
        visit(commit, null);
    }

    public void visit(Upsert upsert) {
        visit(upsert, null);
    }

    public void visit(UseStatement use) {
        visit(use, null);
    }

    public void visit(ShowStatement showStatement) {
        visit(showStatement, null);
    }

    public void visit(ShowColumnsStatement show) {
        visit(show, null);
    }

    public void visit(ShowIndexStatement show) {
        visit(show, null);
    }

    public void visit(ShowTablesStatement showTables) {
        visit(showTables, null);
    }

    public void visit(Block block) {
        visit(block, null);
    }

    public void visit(Comment comment) {
        visit(comment, null);
    }

    public void visit(DescribeStatement describe) {
        visit(describe, null);
    }

    public void visit(ExplainStatement explainStatement) {
        visit(explainStatement, null);
    }

    public void visit(DeclareStatement declareStatement) {
        visit(declareStatement, null);
    }

    public void visit(Grant grant) {
        visit(grant, null);
    }

    public void visit(CreateSchema aThis) {
        visit(aThis, null);
    }

    public void visit(CreateUser createUser) {
        visit(createUser, null);
    }

    public void visit(CreateEvent createEvent) {
        visit(createEvent, null);
    }

    public void visit(CreateSequence createSequence) {
        visit(createSequence, null);
    }

    public void visit(AlterSequence alterSequence) {
        visit(alterSequence, null);
    }

    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        visit(createFunctionalStatement, null);
    }

    public void visit(CreateSynonym createSynonym) {
        visit(createSynonym, null);
    }

    public void visit(Analyze analyze) {
        visit(analyze, null);
    }

    public void visit(SavepointStatement savepointStatement) {
        visit(savepointStatement, null);
    }

    public void visit(RollbackStatement rollbackStatement) {
        visit(rollbackStatement, null);
    }

    public void visit(AlterSession alterSession) {
        visit(alterSession, null);
    }

    public void visit(IfElseStatement ifElseStatement) {
        visit(ifElseStatement, null);
    }

    public void visit(RenameTableStatement renameTableStatement) {
        visit(renameTableStatement, null);
    }

    public void visit(AssertStatement assertStatement) {
        visit(assertStatement, null);
    }

    public void visit(ExportDataStatement exportDataStatement) {
        visit(exportDataStatement, null);
    }

    public void visit(LoadDataStatement loadDataStatement) {
        visit(loadDataStatement, null);
    }

    public void visit(PurgeStatement purgeStatement) {
        visit(purgeStatement, null);
    }

    public void visit(AlterSystemStatement alterSystemStatement) {
        visit(alterSystemStatement, null);
    }

    public void visit(UnsupportedStatement unsupportedStatement) {
        visit(unsupportedStatement, null);
    }

    public void visit(Import imprt) {
        visit(imprt, null);
    }

    public void visit(Export export) {
        visit(export, null);
    }

    @Override
    public <S> Void visit(CreatePolicy createPolicy, S context) {
        validateOptionalFromItem(createPolicy.getTable());
        validateOptionalExpression(createPolicy.getUsingExpression());
        validateOptionalExpression(createPolicy.getWithCheckExpression());
        return null;
    }

    public void visit(CreatePolicy createPolicy) {
        visit(createPolicy, null);
    }

    @Override
    public <S> Void visit(CreateRole statement, S context) {
        validateFeature(Feature.createRole);
        for (RoleOption option : statement.getOptions()) {
            validateOptionalExpression(option.getValue());
        }
        return null;
    }

    @Override
    public <S> Void visit(AlterRole statement, S context) {
        validateFeature(Feature.alterRole);
        for (RoleOption option : statement.getOptions()) {
            validateOptionalExpression(option.getValue());
        }
        if (statement.getValues() != null) {
            statement.getValues().forEach(this::validateOptionalExpression);
        }
        return null;
    }

    @Override
    public <S> Void visit(Revoke statement, S context) {
        validateFeature(Feature.revoke);
        statement.getClause().visit(this::validateOptionalFromItem,
                this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterDefaultPrivileges statement, S context) {
        validateFeature(Feature.alterDefaultPrivileges);
        if (statement.getGrant() != null) {
            statement.getGrant().accept(this, context);
        }
        if (statement.getRevoke() != null) {
            statement.getRevoke().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateType statement, S context) {
        validateFeature(Feature.createType);

        return null;
    }

    @Override
    public <S> Void visit(AlterType statement, S context) {
        validateFeature(Feature.alterType);

        return null;
    }

    @Override
    public <S> Void visit(CreateDomain statement, S context) {
        validateFeature(Feature.createDomain);
        validateOptionalExpression(statement.getDefaultExpression());
        statement.getConstraints()
                .forEach(constraint -> validateOptionalExpression(constraint.getExpression()));
        return null;
    }

    @Override
    public <S> Void visit(AlterDomain statement, S context) {
        validateFeature(Feature.alterDomain);
        validateOptionalExpression(statement.getDefaultExpression());
        if (statement.getConstraint() != null) {
            validateOptionalExpression(statement.getConstraint().getExpression());
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateExtension statement, S context) {
        validateFeature(Feature.createExtension);

        return null;
    }

    @Override
    public <S> Void visit(AlterExtension statement, S context) {
        validateFeature(Feature.alterExtension);
        if (statement.getMember() != null && statement.getMember().isTable()) {
            validateOptionalFromItem(
                    new Table(statement.getMember().getName()));
        }
        return null;
    }

    @Override
    public <S> Void visit(CreatePublication statement, S context) {
        validateFeature(Feature.createPublication);
        statement.getTargets().forEach(target -> target.visit(
                this::validateOptionalFromItem, this::validateOptionalExpression));
        return null;
    }

    @Override
    public <S> Void visit(AlterPublication statement, S context) {
        validateFeature(Feature.alterPublication);
        statement.getTargets().forEach(target -> target.visit(
                this::validateOptionalFromItem, this::validateOptionalExpression));
        return null;
    }

    @Override
    public <S> Void visit(CreateSubscription statement, S context) {
        validateFeature(Feature.createSubscription);

        return null;
    }

    @Override
    public <S> Void visit(AlterSubscription statement, S context) {
        validateFeature(Feature.alterSubscription);

        return null;
    }

    @Override
    public <S> Void visit(OracleBlock block, S context) {
        validateFeature(Feature.block);
        validateFeature(Feature.oracleBlock);
        block.visitChildren(this::validateOptionalExpression,
                statement -> statement.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(OracleAssignment assignment, S context) {
        validateFeature(Feature.oracleBlock);
        validateOptionalExpression(assignment.getTarget());
        validateOptionalExpression(assignment.getValue());
        return null;
    }

    @Override
    public <S> Void visit(OracleNullStatement statement, S context) {
        validateFeature(Feature.oracleBlock);
        return null;
    }

}
