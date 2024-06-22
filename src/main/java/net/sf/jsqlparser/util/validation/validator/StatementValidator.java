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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
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
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class StatementValidator extends AbstractValidator<Statement>
        implements StatementVisitor<Void> {

    @Override
    public Void visit(CreateIndex createIndex) {
        getValidator(CreateIndexValidator.class).validate(createIndex);
        return null;
    }

    @Override
    public Void visit(CreateTable createTable) {
        getValidator(CreateTableValidator.class).validate(createTable);
        return null;
    }

    @Override
    public Void visit(CreateView createView) {
        getValidator(CreateViewValidator.class).validate(createView);
        return null;
    }

    @Override
    public Void visit(AlterView alterView) {
        getValidator(AlterViewValidator.class).validate(alterView);
        return null;
    }

    @Override
    public Void visit(RefreshMaterializedViewStatement materializedView) {
        getValidator(RefreshMaterializedViewStatementValidator.class).validate(materializedView);
        return null;
    }

    @Override
    public Void visit(Delete delete) {
        getValidator(DeleteValidator.class).validate(delete);
        return null;
    }

    @Override
    public Void visit(Drop drop) {
        getValidator(DropValidator.class).validate(drop);
        return null;
    }

    @Override
    public Void visit(Insert insert) {
        getValidator(InsertValidator.class).validate(insert);
        return null;
    }

    @Override
    public Void visit(Select select) {
        validateFeature(Feature.select);

        SelectValidator selectValidator = getValidator(SelectValidator.class);
        select.accept(selectValidator, null);
        return null;
    }

    @Override
    public Void visit(Truncate truncate) {
        validateFeature(Feature.truncate);
        validateOptionalFromItem(truncate.getTable());
        return null;
    }

    @Override
    public Void visit(Update update) {
        getValidator(UpdateValidator.class).validate(update);
        return null;
    }

    @Override
    public Void visit(Alter alter) {
        getValidator(AlterValidator.class).validate(alter);
        return null;
    }

    @Override
    public Void visit(Statements stmts) {
        stmts.forEach(s -> s.accept(this));
        return null;
    }

    @Override
    public Void visit(Execute execute) {
        getValidator(ExecuteValidator.class).validate(execute);
        return null;
    }

    @Override
    public Void visit(SetStatement set) {
        getValidator(SetStatementValidator.class).validate(set);
        return null;
    }

    @Override
    public Void visit(ResetStatement reset) {
        getValidator(ResetStatementValidator.class).validate(reset);
        return null;
    }

    @Override
    public Void visit(Merge merge) {
        getValidator(MergeValidator.class).validate(merge);
        return null;
    }

    @Override
    public Void visit(Commit commit) {
        validateFeature(Feature.commit);
        return null;
    }

    @Override
    public Void visit(Upsert upsert) {
        getValidator(UpsertValidator.class).validate(upsert);
        return null;
    }

    @Override
    public Void visit(UseStatement use) {
        getValidator(UseStatementValidator.class).validate(use);
        return null;
    }

    @Override
    public Void visit(ShowStatement show) {
        getValidator(ShowStatementValidator.class).validate(show);
        return null;
    }

    @Override
    public Void visit(ShowColumnsStatement show) {
        getValidator(ShowColumnsStatementValidator.class).validate(show);
        return null;
    }

    @Override
    public Void visit(ShowIndexStatement show) {
        getValidator(ShowIndexStatementValidator.class).validate(show);
        return null;
    }

    @Override
    public Void visit(ShowTablesStatement showTables) {
        getValidator(ShowTablesStatementValidator.class).validate(showTables);
        return null;
    }

    @Override
    public Void visit(Block block) {
        validateFeature(Feature.block);
        block.getStatements().accept(this);
        return null;
    }

    @Override
    public Void visit(Comment comment) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.comment);
            validateOptionalFeature(c, comment.getTable(), Feature.commentOnTable);
            validateOptionalFeature(c, comment.getColumn(), Feature.commentOnColumn);
            validateOptionalFeature(c, comment.getView(), Feature.commentOnView);
        }
        return null;
    }

    @Override
    public Void visit(DescribeStatement describe) {
        validateFeature(Feature.describe);
        validateFeature(Feature.desc);
        validateOptionalFromItem(describe.getTable());
        return null;
    }

    @Override
    public Void visit(ExplainStatement explain) {
        validateFeature(Feature.explain);
        if (explain.getStatement() != null) {
            explain.getStatement().accept(this);
        }
        return null;
    }


    @Override
    public Void visit(DeclareStatement declare) {
        getValidator(DeclareStatementValidator.class).validate(declare);
        return null;
    }

    @Override
    public Void visit(Grant grant) {
        getValidator(GrantValidator.class).validate(grant);
        return null;
    }

    @Override
    public Void visit(CreateSchema aThis) {
        validateFeatureAndName(Feature.createSchema, NamedObject.schema, aThis.getSchemaName());
        aThis.getStatements().forEach(s -> s.accept(this));
        return null;
    }

    @Override
    public Void visit(CreateSequence createSequence) {
        getValidator(CreateSequenceValidator.class).validate(createSequence);
        return null;
    }

    @Override
    public Void visit(AlterSequence alterSequence) {
        getValidator(AlterSequenceValidator.class).validate(alterSequence);
        return null;
    }

    @Override
    public Void visit(CreateFunctionalStatement createFunctionalStatement) {
        validateFeature(Feature.functionalStatement);
        if (createFunctionalStatement instanceof CreateFunction) {
            validateFeature(Feature.createFunction);
        } else if (createFunctionalStatement instanceof CreateProcedure) {
            validateFeature(Feature.createProcedure);
        }
        return null;
    }

    @Override
    public void validate(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Void visit(CreateSynonym createSynonym) {
        getValidator(CreateSynonymValidator.class).validate(createSynonym);
        return null;
    }

    @Override
    public Void visit(Analyze analyze) {
        getValidator(AnalyzeValidator.class).validate(analyze);
        return null;
    }

    @Override
    public Void visit(SavepointStatement savepointStatement) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(RollbackStatement rollbackStatement) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(AlterSession alterSession) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(IfElseStatement ifElseStatement) {
        ifElseStatement.getIfStatement().accept(this);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this);
        }
        return null;
    }

    public Void visit(RenameTableStatement renameTableStatement) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(PurgeStatement purgeStatement) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(AlterSystemStatement alterSystemStatement) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public Void visit(UnsupportedStatement unsupportedStatement) {

        return null;
    }
}
