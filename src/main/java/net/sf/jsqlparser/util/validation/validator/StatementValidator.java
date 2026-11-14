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

import net.sf.jsqlparser.statement.alter.AlterRelation;
import net.sf.jsqlparser.statement.create.operator.CreateOperator;
import net.sf.jsqlparser.statement.create.operator.CreateOperatorClass;
import net.sf.jsqlparser.statement.create.operator.CreateOperatorFamily;
import net.sf.jsqlparser.statement.create.aggregate.CreateAggregate;
import net.sf.jsqlparser.statement.alter.AlterOperator;
import net.sf.jsqlparser.statement.alter.AlterOperatorClass;
import net.sf.jsqlparser.statement.alter.AlterOperatorFamily;
import net.sf.jsqlparser.statement.alter.AlterAggregate;
import net.sf.jsqlparser.statement.alter.AlterRule;
import net.sf.jsqlparser.statement.create.cast.CreateCast;
import net.sf.jsqlparser.statement.create.transform.CreateTransform;
import net.sf.jsqlparser.statement.create.conversion.CreateConversion;
import net.sf.jsqlparser.statement.alter.AlterConversion;
import net.sf.jsqlparser.statement.alter.AlterTablespace;
import net.sf.jsqlparser.statement.alter.AlterTablespaceMove;
import net.sf.jsqlparser.statement.alter.AlterPolicy;
import net.sf.jsqlparser.statement.alter.AlterStatistics;
import net.sf.jsqlparser.statement.drop.DropPolicy;
import net.sf.jsqlparser.statement.drop.DropUserMapping;
import net.sf.jsqlparser.statement.create.statistics.CreateStatistics;

import net.sf.jsqlparser.statement.create.accessmethod.CreateAccessMethod;

import net.sf.jsqlparser.statement.create.fdw.CreateForeignDataWrapper;
import net.sf.jsqlparser.statement.alter.AlterForeignDataWrapper;
import net.sf.jsqlparser.statement.create.server.CreateServer;
import net.sf.jsqlparser.statement.alter.AlterServer;
import net.sf.jsqlparser.statement.create.usermapping.CreateUserMapping;
import net.sf.jsqlparser.statement.alter.AlterUserMapping;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchConfiguration;
import net.sf.jsqlparser.statement.alter.AlterTextSearchConfiguration;
import net.sf.jsqlparser.statement.create.rule.CreateRule;
import net.sf.jsqlparser.statement.notify.ListenStatement;
import net.sf.jsqlparser.statement.notify.NotifyStatement;
import net.sf.jsqlparser.statement.create.collation.CreateCollation;
import net.sf.jsqlparser.statement.alter.AlterCollation;
import net.sf.jsqlparser.statement.alter.AlterRoutine;
import net.sf.jsqlparser.statement.alter.AlterTrigger;
import net.sf.jsqlparser.statement.alter.schema.AlterSchema;
import net.sf.jsqlparser.statement.oracle.OracleBlock;
import net.sf.jsqlparser.statement.oracle.OracleAssignment;
import net.sf.jsqlparser.statement.oracle.OracleNullStatement;

import net.sf.jsqlparser.statement.role.CreateRole;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.statement.role.ReassignOwned;
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
import net.sf.jsqlparser.statement.StartTransaction;
import net.sf.jsqlparser.statement.ReleaseSavepointStatement;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DoStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.DiscardStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SessionStatement;
import net.sf.jsqlparser.statement.SetConstraintsStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.SetDefaultRoleStatement;
import net.sf.jsqlparser.statement.SetPasswordStatement;
import net.sf.jsqlparser.statement.SetRoleStatement;
import net.sf.jsqlparser.statement.SetTransactionStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.XaStatement;
import net.sf.jsqlparser.statement.SetIdentityInsertStatement;
import net.sf.jsqlparser.statement.SetScope;
import net.sf.jsqlparser.statement.TableMaintenanceStatement;
import net.sf.jsqlparser.statement.TransactionCompletionStatement;
import net.sf.jsqlparser.statement.TransactionMode;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterEventTrigger;
import net.sf.jsqlparser.statement.alter.AlterInstance;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.AlterUser;
import net.sf.jsqlparser.statement.alter.RenameUser;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.database.CreateDatabase;
import net.sf.jsqlparser.statement.alter.database.AlterDatabase;
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
import net.sf.jsqlparser.statement.create.spatial.CreateSpatialReferenceSystem;
import net.sf.jsqlparser.statement.create.tablespace.CreateTablespace;
import net.sf.jsqlparser.statement.create.trigger.CreateEventTrigger;
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
import net.sf.jsqlparser.statement.admin.FlushStatement;
import net.sf.jsqlparser.statement.admin.KillStatement;
import net.sf.jsqlparser.statement.handler.HandlerStatement;
import net.sf.jsqlparser.statement.lock.BackupLockStatement;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.statement.lock.UnlockStatement;
import net.sf.jsqlparser.statement.replication.ChangeReplicationFilterStatement;
import net.sf.jsqlparser.statement.replication.ChangeReplicationSourceStatement;
import net.sf.jsqlparser.statement.replication.ResetReplicationStatement;
import net.sf.jsqlparser.statement.replication.StartReplicaStatement;
import net.sf.jsqlparser.statement.replication.StopReplicaStatement;
import net.sf.jsqlparser.statement.maintenance.CheckpointStatement;
import net.sf.jsqlparser.statement.maintenance.ClusterStatement;
import net.sf.jsqlparser.statement.maintenance.VacuumStatement;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.reindex.Reindex;
import net.sf.jsqlparser.statement.resourcegroup.AlterResourceGroup;
import net.sf.jsqlparser.statement.resourcegroup.CreateResourceGroup;
import net.sf.jsqlparser.statement.resourcegroup.SetResourceGroup;
import net.sf.jsqlparser.statement.securitylabel.SecurityLabel;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.show.MySqlShowStatement;
import net.sf.jsqlparser.statement.show.ShowCreateStatement;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTableStatusStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.validation.ValidationUtil;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import net.sf.jsqlparser.statement.PragmaStatement;
import net.sf.jsqlparser.statement.ExtensionStatement;
import net.sf.jsqlparser.statement.AttachStatement;
import net.sf.jsqlparser.statement.DetachStatement;
import net.sf.jsqlparser.statement.ConnectStatement;
import net.sf.jsqlparser.statement.DisconnectStatement;
import net.sf.jsqlparser.statement.PrepareStatement;
import net.sf.jsqlparser.statement.PluginStatement;
import net.sf.jsqlparser.statement.PrepareTransactionStatement;
import net.sf.jsqlparser.statement.DeallocateStatement;
import net.sf.jsqlparser.statement.CopyStatement;
import net.sf.jsqlparser.statement.create.macro.CreateMacro;
import net.sf.jsqlparser.statement.create.extension.CreateExtensionRepository;
import net.sf.jsqlparser.statement.cursor.CloseCursorStatement;
import net.sf.jsqlparser.statement.cursor.DeclareCursorStatement;
import net.sf.jsqlparser.statement.cursor.FetchCursorStatement;
import net.sf.jsqlparser.statement.AssertStatement;
import net.sf.jsqlparser.statement.export.ExportDataStatement;
import net.sf.jsqlparser.statement.load.LoadDataStatement;
import net.sf.jsqlparser.statement.load.MySqlLoadDataStatement;

import java.util.List;

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
        validateFeature(Feature.createTrigger);
        createTrigger.visit(this::validateOptionalFromItem,
                this::validateOptionalExpression);
        if (createTrigger.getBody() != null) {
            createTrigger.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateEventTrigger statement, S context) {
        // the filter values are tags and the function is declared, not called
        validateFeature(Feature.createEventTrigger);
        return null;
    }

    @Override
    public <S> Void visit(AlterEventTrigger statement, S context) {
        validateFeature(Feature.alterEventTrigger);
        return null;
    }

    @Override
    public <S> Void visit(Reindex statement, S context) {
        validateFeature(Feature.reindex);
        if (statement.isConcurrently() || statement.getOptions().stream()
                .anyMatch(option -> option.getName() == Reindex.Option.Name.CONCURRENTLY)) {
            validateFeature(Feature.reindexConcurrently);
        }
        if (statement.getName() != null) {
            validateName(reindexedObject(statement.getTarget()),
                    statement.getName().getFullyQualifiedName());
        }
        return null;
    }

    private static NamedObject reindexedObject(Reindex.Target target) {
        switch (target) {
            case INDEX:
                return NamedObject.index;
            case TABLE:
                return NamedObject.table;
            case SCHEMA:
                return NamedObject.schema;
            default:
                return NamedObject.database;
        }
    }

    @Override
    public <S> Void visit(SecurityLabel statement, S context) {
        validateFeature(Feature.securityLabel);
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
        validateFeature(Feature.session);
        return null;
    }


    @Override
    public <S> Void visit(Drop drop, S context) {
        getValidator(DropValidator.class).validate(drop);
        return null;
    }

    @Override
    public <S> Void visit(ReassignOwned statement, S context) {
        validateFeature(Feature.reassignOwned);
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
        truncate.getTables().forEach(this::validateOptionalFromItem);
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
        validateFeature(Feature.alterEvent);
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
        if (statement.getSelectItems() != null) {
            statement.visitExpressions(this::validateOptionalExpression);
        }
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
    public <S> Void visit(StartTransaction statement, S context) {
        validateFeature(Feature.startTransaction);
        validateTransactionModes(statement.getModes(), statement.getModeContext());
        return null;
    }

    /**
     * Checks the mode features of BEGIN, START TRANSACTION and SET TRANSACTION. An isolation level
     * is a feature of BEGIN and START TRANSACTION only, since SET TRANSACTION exists to set it.
     */
    private void validateTransactionModes(List<TransactionMode> modes,
            TransactionMode.Context context) {
        for (TransactionMode mode : modes) {
            if (mode.isIsolationLevel()) {
                if (context != TransactionMode.Context.SET_TRANSACTION) {
                    validateFeature(Feature.transactionIsolationLevel);
                }
            } else if (mode.isDeferrability()) {
                validateFeature(Feature.transactionDeferrable);
            } else if (mode == TransactionMode.WITH_CONSISTENT_SNAPSHOT) {
                validateFeature(Feature.transactionConsistentSnapshot);
            }
        }
    }

    @Override
    public <S> Void visit(ReleaseSavepointStatement statement, S context) {
        validateFeature(Feature.releaseSavepoint);
        return null;
    }

    @Override
    public <S> Void visit(SetTransactionStatement statement, S context) {
        validateFeature(Feature.setTransaction);
        if (statement.getScope() == SetScope.GLOBAL) {
            validateFeature(Feature.setTransactionGlobal);
        } else if (statement.getScope() == SetScope.PERSIST
                || statement.getScope() == SetScope.PERSIST_ONLY) {
            validateFeature(Feature.setTransactionPersist);
        }
        if (statement.isSessionCharacteristics()) {
            validateFeature(Feature.setTransactionSessionCharacteristics);
        }
        if (statement.getSnapshotId() != null) {
            validateFeature(Feature.setTransactionSnapshot);
        }
        validateTransactionModes(statement.getModes(), TransactionMode.Context.SET_TRANSACTION);
        return null;
    }

    @Override
    public <S> Void visit(SetConstraintsStatement statement, S context) {
        validateFeature(Feature.setConstraints);
        return null;
    }

    @Override
    public <S> Void visit(PrepareTransactionStatement statement, S context) {
        validateFeature(Feature.twoPhaseCommit);
        return null;
    }

    @Override
    public <S> Void visit(XaStatement statement, S context) {
        validateFeature(Feature.xa);
        return null;
    }

    @Override
    public <S> Void visit(CreateResourceGroup statement, S context) {
        validateFeature(Feature.createResourceGroup);
        return null;
    }

    @Override
    public <S> Void visit(AlterResourceGroup statement, S context) {
        validateFeature(Feature.alterResourceGroup);
        return null;
    }

    @Override
    public <S> Void visit(SetResourceGroup statement, S context) {
        validateFeature(Feature.setResourceGroup);
        return null;
    }

    @Override
    public <S> Void visit(PluginStatement statement, S context) {
        validateFeature(statement.getObjectType() == PluginStatement.ObjectType.COMPONENT
                ? Feature.installComponent
                : Feature.installPlugin);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(Commit commit, S context) {
        validateFeature(Feature.commit);
        validateTransactionCompletion(commit);
        return null;
    }

    /**
     * Checks the clauses shared by COMMIT and ROLLBACK and their END and ABORT spellings. Oracle's
     * ROLLBACK FORCE has no feature of its own.
     */
    private void validateTransactionCompletion(TransactionCompletionStatement statement) {
        if (statement
                .getPreparedTransactionKeyword() == TransactionCompletionStatement.PreparedTransactionKeyword.PREPARED) {
            validateFeature(Feature.twoPhaseCommit);
        }
        if (statement.isPostgresSpelling()) {
            validateFeature(Feature.transactionEndAbort);
        }
        if (statement.getChain() != null) {
            validateFeature(Feature.transactionChain);
        }
        if (statement.getRelease() != null) {
            validateFeature(Feature.transactionRelease);
        }
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
    public <S> Void visit(ShowTableStatusStatement showTableStatus, S context) {
        getValidator(ShowTableStatusStatementValidator.class).validate(showTableStatus);
        return null;
    }

    @Override
    public <S> Void visit(ShowCreateStatement showCreate, S context) {
        getValidator(ShowCreateStatementValidator.class).validate(showCreate);
        return null;
    }

    @Override
    public <S> Void visit(MySqlShowStatement mySqlShow, S context) {
        getValidator(MySqlShowStatementValidator.class).validate(mySqlShow);
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
    public <S> Void visit(AlterDatabase statement, S context) {
        validateFeature(Feature.alterDatabase);
        if (statement.getParameterChange() != null
                && statement.getParameterChange().getValues() != null) {
            statement.getParameterChange().getValues().forEach(this::validateOptionalExpression);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateUser createUser, S context) {
        validateFeature(Feature.createUser);
        return null;
    }

    @Override
    public <S> Void visit(AlterUser alterUser, S context) {
        validateFeature(Feature.alterUser);
        return null;
    }

    @Override
    public <S> Void visit(RenameUser renameUser, S context) {
        validateFeature(Feature.renameUser);
        return null;
    }

    @Override
    public <S> Void visit(SetRoleStatement setRole, S context) {
        validateFeature(Feature.setRole);
        return null;
    }

    @Override
    public <S> Void visit(SetDefaultRoleStatement setDefaultRole, S context) {
        validateFeature(Feature.setDefaultRole);
        return null;
    }

    @Override
    public <S> Void visit(SetPasswordStatement setPassword, S context) {
        validateFeature(Feature.setPassword);
        validateOptionalExpression(setPassword.getPassword());
        return null;
    }

    @Override
    public <S> Void visit(CreateEvent createEvent, S context) {
        validateFeature(Feature.createEvent);
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
    public <S> Void visit(TableMaintenanceStatement statement, S context) {
        getValidator(TableMaintenanceValidator.class).validate(statement);
        return null;
    }

    @Override
    public <S> Void visit(FlushStatement flush, S context) {
        validateFeature(Feature.flush);
        for (Table table : flush.getTables()) {
            validateName(NamedObject.table, table.getFullyQualifiedName());
        }
        return null;
    }

    @Override
    public <S> Void visit(KillStatement kill, S context) {
        validateFeature(Feature.kill);
        validateOptionalExpression(kill.getProcesslistId());
        return null;
    }

    @Override
    public <S> Void visit(HandlerStatement handler, S context) {
        validateFeature(Feature.handler);
        if (handler.getAction() == HandlerStatement.Action.OPEN) {
            Table table = handler.getTable();
            validateNameWithAlias(NamedObject.table, table.getFullyQualifiedName(),
                    ValidationUtil.getAlias(table.getAlias()));
        }
        validateOptionalExpression(handler.getKeyValues());
        validateOptionalExpression(handler.getWhere());
        return null;
    }

    @Override
    public <S> Void visit(VacuumStatement vacuum, S context) {
        getValidator(VacuumValidator.class).validate(vacuum);
        return null;
    }

    @Override
    public <S> Void visit(ClusterStatement cluster, S context) {
        getValidator(ClusterValidator.class).validate(cluster);
        return null;
    }

    @Override
    public <S> Void visit(CheckpointStatement checkpoint, S context) {
        validateFeature(Feature.checkpoint);
        return null;
    }

    @Override
    public <S> Void visit(SavepointStatement savepointStatement, S context) {
        validateFeature(Feature.savepoint);
        return null;
    }

    @Override
    public <S> Void visit(RollbackStatement rollbackStatement, S context) {
        validateFeature(Feature.rollback);
        if (rollbackStatement.getSavepointName() != null) {
            validateFeature(Feature.savepoint);
        }
        validateTransactionCompletion(rollbackStatement);
        return null;
    }

    @Override
    public <S> Void visit(AlterSession alterSession, S context) {
        validateFeature(Feature.alterSession);
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
        // the same operation as ALTER TABLE ... RENAME TO
        validateFeature(Feature.alterTable);
        return null;
    }

    @Override
    public <S> Void visit(PragmaStatement pragmaStatement, S context) {
        validateFeature(Feature.pragma);
        return null;
    }

    @Override
    public <S> Void visit(AssertStatement assertStatement, S context) {
        validateFeature(Feature.assertStatement);
        return null;
    }

    @Override
    public <S> Void visit(ExtensionStatement extensionStatement, S context) {
        validateFeature(extensionStatement.getOperation() == ExtensionStatement.Operation.LOAD
                ? Feature.loadExtension
                : Feature.installExtension);
        return null;
    }

    @Override
    public <S> Void visit(ExportDataStatement exportDataStatement, S context) {
        validateFeature(Feature.export);
        return null;
    }

    @Override
    public <S> Void visit(AttachStatement attachStatement, S context) {
        validateFeature(Feature.attach);
        return null;
    }

    @Override
    public <S> Void visit(DetachStatement detachStatement, S context) {
        validateFeature(Feature.detach);
        return null;
    }

    @Override
    public <S> Void visit(ConnectStatement connectStatement, S context) {
        validateFeature(Feature.connect);
        return null;
    }

    @Override
    public <S> Void visit(DisconnectStatement disconnectStatement, S context) {
        validateFeature(Feature.disconnect);
        return null;
    }

    @Override
    public <S> Void visit(PrepareStatement prepareStatement, S context) {
        validateFeature(Feature.prepare);
        validateOptionalExpression(prepareStatement.getSource());
        return null;
    }

    @Override
    public <S> Void visit(DeallocateStatement deallocateStatement, S context) {
        validateFeature(Feature.deallocate);
        return null;
    }

    @Override
    public <S> Void visit(CopyStatement copyStatement, S context) {
        validateFeature(Feature.copy);
        return null;
    }

    @Override
    public <S> Void visit(CreateMacro createMacro, S context) {
        validateFeature(Feature.createMacro);
        return null;
    }

    @Override
    public <S> Void visit(CreateExtensionRepository createExtensionRepository, S context) {
        validateFeature(Feature.createExtensionRepository);
        return null;
    }

    @Override
    public <S> Void visit(LoadDataStatement loadDataStatement, S context) {
        validateFeature(Feature.loadData);
        return null;
    }

    @Override
    public <S> Void visit(MySqlLoadDataStatement loadData, S context) {
        validateFeature(Feature.loadData);
        validateOptionalFromItem(loadData.getTable());
        validateOptionalExpressions(loadData.getColumns());
        validateOptionalUpdateSets(loadData.getUpdateSets());
        return null;
    }

    @Override
    public <S> Void visit(PurgeStatement purgeStatement, S context) {
        validateFeature(purgeStatement.getLogBound() != null ? Feature.purgeBinaryLogs
                : Feature.purge);
        return null;
    }

    @Override
    public <S> Void visit(AlterSystemStatement alterSystemStatement, S context) {
        validateFeature(Feature.alterSystem);
        return null;
    }

    @Override
    public <S> Void visit(UnsupportedStatement unsupportedStatement, S context) {
        // its tokens are not modelled, so nothing inside it can be validated
        validateFeature(Feature.allowUnsupportedStatements);
        return null;
    }

    @Override
    public <S> Void visit(Import imprt, S context) {
        validateFeature(Feature.imprt);
        return null;
    }

    @Override
    public <S> Void visit(Export export, S context) {
        validateFeature(Feature.export);
        return null;
    }

    @Override
    public <S> Void visit(LockStatement lock, S context) {
        validateFeature(Feature.lock);
        for (LockStatement.Target target : lock.getTargets()) {
            Table table = target.getTable();
            validateNameWithAlias(NamedObject.table, table.getFullyQualifiedName(),
                    ValidationUtil.getAlias(table.getAlias()));
        }
        return null;
    }

    @Override
    public <S> Void visit(UnlockStatement unlock, S context) {
        validateFeature(Feature.unlockTables);
        return null;
    }

    @Override
    public <S> Void visit(BackupLockStatement backupLock, S context) {
        validateFeature(Feature.backupLock);
        return null;
    }

    @Override
    public <S> Void visit(ChangeReplicationSourceStatement changeReplicationSource, S context) {
        validateFeature(Feature.changeReplicationSource);
        return null;
    }

    @Override
    public <S> Void visit(ChangeReplicationFilterStatement changeReplicationFilter, S context) {
        validateFeature(Feature.changeReplicationFilter);
        return null;
    }

    @Override
    public <S> Void visit(StartReplicaStatement startReplica, S context) {
        validateFeature(Feature.startReplica);
        return null;
    }

    @Override
    public <S> Void visit(StopReplicaStatement stopReplica, S context) {
        validateFeature(Feature.stopReplica);
        return null;
    }

    @Override
    public <S> Void visit(ResetReplicationStatement resetReplication, S context) {
        validateFeature(Feature.resetReplication);
        return null;
    }

    @Override
    public <S> Void visit(DeclareCursorStatement declareCursor, S context) {
        validateFeature(Feature.declareCursor);
        if (declareCursor.getQuery() != null) {
            visit(declareCursor.getQuery(), context);
        }
        return null;
    }

    @Override
    public <S> Void visit(FetchCursorStatement fetchCursor, S context) {
        validateFeature(Feature.fetchCursor);
        return null;
    }

    @Override
    public <S> Void visit(CloseCursorStatement closeCursor, S context) {
        validateFeature(Feature.closeCursor);
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

    public void visit(ReassignOwned statement) {
        visit(statement, null);
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

    @Override
    public void visit(ShowTableStatusStatement showTableStatus) {
        visit(showTableStatus, null);
    }

    @Override
    public void visit(ShowCreateStatement showCreate) {
        visit(showCreate, null);
    }

    @Override
    public void visit(MySqlShowStatement mySqlShow) {
        visit(mySqlShow, null);
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

    public void visit(AlterUser alterUser) {
        visit(alterUser, null);
    }

    public void visit(RenameUser renameUser) {
        visit(renameUser, null);
    }

    public void visit(SetRoleStatement setRole) {
        visit(setRole, null);
    }

    public void visit(SetDefaultRoleStatement setDefaultRole) {
        visit(setDefaultRole, null);
    }

    public void visit(SetPasswordStatement setPassword) {
        visit(setPassword, null);
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

    @Override
    public void visit(TableMaintenanceStatement statement) {
        visit(statement, null);
    }

    @Override
    public void visit(VacuumStatement vacuum) {
        visit(vacuum, null);
    }

    @Override
    public void visit(ClusterStatement cluster) {
        visit(cluster, null);
    }

    @Override
    public void visit(CheckpointStatement checkpoint) {
        visit(checkpoint, null);
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

    public void visit(PragmaStatement pragmaStatement) {
        visit(pragmaStatement, null);
    }

    public void visit(ExtensionStatement extensionStatement) {
        visit(extensionStatement, null);
    }

    public void visit(AttachStatement attachStatement) {
        visit(attachStatement, null);
    }

    public void visit(DetachStatement detachStatement) {
        visit(detachStatement, null);
    }

    public void visit(ConnectStatement connectStatement) {
        visit(connectStatement, null);
    }

    public void visit(DisconnectStatement disconnectStatement) {
        visit(disconnectStatement, null);
    }

    public void visit(PrepareStatement prepareStatement) {
        visit(prepareStatement, null);
    }

    public void visit(DeallocateStatement deallocateStatement) {
        visit(deallocateStatement, null);
    }

    public void visit(CopyStatement copyStatement) {
        visit(copyStatement, null);
    }

    public void visit(CreateMacro createMacro) {
        visit(createMacro, null);
    }

    public void visit(CreateExtensionRepository createExtensionRepository) {
        visit(createExtensionRepository, null);
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

    public void visit(MySqlLoadDataStatement loadData) {
        visit(loadData, null);
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
        validateFeature(Feature.createPolicy);
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
    public <S> Void visit(AlterSchema statement, S context) {
        validateFeature(Feature.alterSchema);
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


    @Override
    public <S> Void visit(CreateForeignDataWrapper statement, S context) {
        validateFeature(Feature.createForeignDataWrapper);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterForeignDataWrapper statement, S context) {
        validateFeature(Feature.alterForeignDataWrapper);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(CreateServer statement, S context) {
        validateFeature(Feature.createServer);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterServer statement, S context) {
        validateFeature(Feature.alterServer);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(CreateUserMapping statement, S context) {
        validateFeature(Feature.createUserMapping);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterUserMapping statement, S context) {
        validateFeature(Feature.alterUserMapping);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(DropUserMapping statement, S context) {
        validateFeature(Feature.drop);
        return null;
    }

    @Override
    public <S> Void visit(CreateAccessMethod statement, S context) {
        validateFeature(Feature.createAccessMethod);
        if (statement.getType() == CreateAccessMethod.Type.TABLE) {
            validateFeature(Feature.createAccessMethodTable);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateTextSearchConfiguration statement, S context) {
        validateFeature(Feature.createTextSearchConfiguration);
        return null;
    }

    @Override
    public <S> Void visit(AlterTextSearchConfiguration statement, S context) {
        validateFeature(Feature.alterTextSearchConfiguration);
        return null;
    }

    @Override
    public <S> Void visit(CreateRule statement, S context) {
        validateFeature(Feature.createRule);
        statement.visitExpressions(this::validateOptionalExpression);
        statement.visitTables(this::validateOptionalFromItem);
        statement.getActions().forEach(action -> action.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(NotifyStatement statement, S context) {
        validateFeature(Feature.notifyStatement);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(ListenStatement listen, S context) {
        validateFeature(Feature.listen);
        return null;
    }

    @Override
    public <S> Void visit(DiscardStatement discard, S context) {
        validateFeature(Feature.discard);
        return null;
    }

    @Override
    public <S> Void visit(CreateCollation statement, S context) {
        validateFeature(Feature.createCollation);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterCollation statement, S context) {
        validateFeature(Feature.alterCollation);
        return null;
    }

    @Override
    public <S> Void visit(AlterRoutine statement, S context) {
        switch (statement.getRoutineType()) {
            case PROCEDURE:
                validateFeature(Feature.alterProcedure);
                break;
            case ROUTINE:
                validateFeature(Feature.alterRoutine);
                break;
            default:
                validateFeature(Feature.alterFunction);
        }
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterTrigger statement, S context) {
        validateFeature(Feature.alterTrigger);
        validateOptionalFromItem(statement.getTable());
        return null;
    }

    @Override
    public <S> Void visit(AlterRelation statement, S context) {
        boolean index = statement.getObjectType() == AlterRelation.ObjectType.INDEX;
        validateFeature(index ? Feature.alterIndex : Feature.alterView);
        if (!index) {
            validateOptionalFromItem(statement.getRelation());
        }
        statement.getActions().forEach(action -> {
            action.visitExpressions(this::validateOptionalExpression);
            action.visitTables(this::validateOptionalFromItem);
        });
        return null;
    }

    @Override
    public <S> Void visit(CreateTablespace statement, S context) {
        validateFeature(Feature.createTablespace);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(CreateSpatialReferenceSystem statement, S context) {
        validateFeature(Feature.createSpatialReferenceSystem);
        return null;
    }

    @Override
    public <S> Void visit(AlterInstance statement, S context) {
        validateFeature(Feature.alterInstance);
        return null;
    }

    @Override
    public <S> Void visit(AlterTablespace statement, S context) {
        validateFeature(Feature.alterTablespace);
        statement.visitExpressions(this::validateOptionalExpression);
        return null;
    }

    @Override
    public <S> Void visit(AlterTablespaceMove statement, S context) {
        validateFeature(Feature.alterTablespace);
        return null;
    }

    @Override
    public <S> Void visit(AlterPolicy statement, S context) {
        validateFeature(Feature.alterPolicy);
        validateOptionalFromItem(statement.getTable());
        if (statement.getNewName() == null) {
            statement.getOptions().visitExpressions(this::validateOptionalExpression);
        }
        return null;
    }

    @Override
    public <S> Void visit(DropPolicy statement, S context) {
        validateFeature(Feature.drop);
        validateOptionalFromItem(statement.getTable());
        return null;
    }

    @Override
    public <S> Void visit(CreateStatistics statement, S context) {
        validateFeature(Feature.createStatistics);
        validateOptionalFromItem(statement.getTable());
        validateOptionalExpression(statement.getExpressions());
        return null;
    }

    @Override
    public <S> Void visit(AlterStatistics statement, S context) {
        validateFeature(Feature.alterStatistics);
        return null;
    }


    @Override
    public <S> Void visit(AlterRule statement, S context) {
        validateFeature(Feature.alterRule);
        validateOptionalFromItem(statement.getTable());
        return null;
    }

    @Override
    public <S> Void visit(CreateCast statement, S context) {
        validateFeature(Feature.createCast);
        return null;
    }

    @Override
    public <S> Void visit(CreateOperator statement, S context) {
        validateFeature(Feature.createOperator);
        return null;
    }

    @Override
    public <S> Void visit(CreateTransform statement, S context) {
        validateFeature(Feature.createTransform);
        return null;
    }

    @Override
    public <S> Void visit(CreateOperatorClass statement, S context) {
        validateFeature(Feature.createOperatorClass);
        return null;
    }

    @Override
    public <S> Void visit(CreateConversion statement, S context) {
        validateFeature(Feature.createConversion);
        return null;
    }

    @Override
    public <S> Void visit(CreateOperatorFamily statement, S context) {
        validateFeature(Feature.createOperatorFamily);
        return null;
    }

    @Override
    public <S> Void visit(AlterConversion statement, S context) {
        validateFeature(Feature.alterConversion);
        return null;
    }

    @Override
    public <S> Void visit(CreateAggregate statement, S context) {
        validateFeature(Feature.createAggregate);
        return null;
    }

    @Override
    public <S> Void visit(AlterOperator statement, S context) {
        validateFeature(Feature.alterOperator);
        return null;
    }

    @Override
    public <S> Void visit(AlterOperatorClass statement, S context) {
        validateFeature(Feature.alterOperatorClass);
        return null;
    }

    @Override
    public <S> Void visit(AlterOperatorFamily statement, S context) {
        validateFeature(Feature.alterOperatorFamily);
        return null;
    }

    @Override
    public <S> Void visit(AlterAggregate statement, S context) {
        validateFeature(Feature.alterAggregate);
        return null;
    }
}
