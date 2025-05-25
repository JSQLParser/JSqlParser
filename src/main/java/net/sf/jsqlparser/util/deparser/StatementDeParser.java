/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Table;
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
import net.sf.jsqlparser.statement.SessionStatement;
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
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class StatementDeParser extends AbstractDeParser<Statement>
        implements StatementVisitor<StringBuilder> {

    private final ExpressionDeParser expressionDeParser;

    private final SelectDeParser selectDeParser;

    public StatementDeParser(Class<? extends ExpressionDeParser> expressionDeparserClass,
            Class<? extends SelectDeParser> selectDeparserClass, StringBuilder builder)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        super(builder);

        this.selectDeParser = selectDeparserClass
                .getConstructor(Class.class, StringBuilder.class)
                .newInstance(expressionDeparserClass, builder);


        this.expressionDeParser =
                expressionDeparserClass.cast(this.selectDeParser.getExpressionVisitor());

    }

    public StatementDeParser(Class<? extends ExpressionDeParser> expressionDeparserClass,
            Class<? extends SelectDeParser> selectDeparserClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        this(expressionDeparserClass, selectDeparserClass, new StringBuilder());
    }

    public StatementDeParser(StringBuilder buffer) {
        this(new ExpressionDeParser(), new SelectDeParser(), buffer);
    }

    public StatementDeParser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser,
            StringBuilder buffer) {
        super(buffer);

        this.expressionDeParser = expressionDeParser;
        this.selectDeParser = selectDeParser;

        this.selectDeParser.setBuilder(buffer);
        this.selectDeParser.setExpressionVisitor(expressionDeParser);

        this.expressionDeParser.setSelectVisitor(selectDeParser);
        this.expressionDeParser.setBuilder(buffer);
    }

    @Override
    public <S> StringBuilder visit(CreateIndex createIndex, S context) {
        CreateIndexDeParser createIndexDeParser = new CreateIndexDeParser(builder);
        createIndexDeParser.deParse(createIndex);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateTable createTable, S context) {
        CreateTableDeParser createTableDeParser = new CreateTableDeParser(this, builder);
        createTableDeParser.deParse(createTable);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateView createView, S context) {
        CreateViewDeParser createViewDeParser = new CreateViewDeParser(builder);
        createViewDeParser.deParse(createView);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RefreshMaterializedViewStatement materializedViewStatement,
            S context) {
        new RefreshMaterializedViewStatementDeParser(builder).deParse(materializedViewStatement);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AlterView alterView, S context) {
        AlterViewDeParser alterViewDeParser = new AlterViewDeParser(builder);
        alterViewDeParser.deParse(alterView);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Delete delete, S context) {
        DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser, builder);
        deleteDeParser.deParse(delete);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Drop drop, S context) {
        DropDeParser dropDeParser = new DropDeParser(builder);
        dropDeParser.deParse(drop);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Insert insert, S context) {
        InsertDeParser insertDeParser =
                new InsertDeParser(expressionDeParser, selectDeParser, builder);
        insertDeParser.deParse(insert);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedInsert insert, S context) {
        List<WithItem<?>> withItemsList = insert.getWithItemsList();
        addWithItemsToBuffer(withItemsList, context);
        builder.append("(");
        insert.getInsert().accept(this, context);
        builder.append(")");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedUpdate update, S context) {
        List<WithItem<?>> withItemsList = update.getWithItemsList();
        addWithItemsToBuffer(withItemsList, context);
        builder.append("(");
        update.getUpdate().accept(this, context);
        builder.append(")");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedDelete delete, S context) {
        List<WithItem<?>> withItemsList = delete.getWithItemsList();
        addWithItemsToBuffer(withItemsList, context);
        builder.append("(");
        delete.getDelete().accept(this, context);
        builder.append(")");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(SessionStatement sessionStatement, S context) {
        return builder.append(sessionStatement.toString());
    }


    private <S> StringBuilder addWithItemsToBuffer(List<WithItem<?>> withItemsList, S context) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
                builder.append(" ");
            }
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Select select, S context) {
        select.accept((SelectVisitor<StringBuilder>) selectDeParser, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Truncate truncate, S context) {
        builder.append("TRUNCATE");
        if (truncate.isTableToken()) {
            builder.append(" TABLE");
        }
        if (truncate.isOnly()) {
            builder.append(" ONLY");
        }
        builder.append(" ");
        if (truncate.getTables() != null && !truncate.getTables().isEmpty()) {
            builder.append(truncate.getTables().stream()
                    .map(Table::toString)
                    .collect(Collectors.joining(", ")));
        } else {
            builder.append(truncate.getTable());
        }
        if (truncate.getCascade()) {
            builder.append(" CASCADE");
        }

        return builder;
    }

    @Override
    public <S> StringBuilder visit(Update update, S context) {
        UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser, builder);
        updateDeParser.deParse(update);

        return builder;
    }

    public <S> StringBuilder visit(Analyze analyzer, S context) {
        builder.append("ANALYZE ");
        builder.append(analyzer.getTable());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Alter alter, S context) {
        AlterDeParser alterDeParser = new AlterDeParser(builder);
        alterDeParser.deParse(alter);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Statements statements, S context) {
        statements.accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Execute execute, S context) {
        ExecuteDeParser executeDeParser = new ExecuteDeParser(expressionDeParser, builder);
        executeDeParser.deParse(execute);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(SetStatement set, S context) {
        SetStatementDeParser setStatementDeparser =
                new SetStatementDeParser(expressionDeParser, builder);
        setStatementDeparser.deParse(set);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ResetStatement reset, S context) {
        ResetStatementDeParser setStatementDeparser =
                new ResetStatementDeParser(expressionDeParser, builder);
        setStatementDeparser.deParse(reset);
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    @Override
    public <S> StringBuilder visit(Merge merge, S context) {
        new MergeDeParser(expressionDeParser, selectDeParser, builder).deParse(merge);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(SavepointStatement savepointStatement, S context) {
        builder.append(savepointStatement.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RollbackStatement rollbackStatement, S context) {
        builder.append(rollbackStatement.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Commit commit, S context) {
        builder.append(commit.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Upsert upsert, S context) {
        UpsertDeParser upsertDeParser =
                new UpsertDeParser(expressionDeParser, selectDeParser, builder);
        upsertDeParser.deParse(upsert);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(UseStatement use, S context) {
        new UseStatementDeParser(builder).deParse(use);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ShowColumnsStatement show, S context) {
        new ShowColumnsStatementDeParser(builder).deParse(show);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ShowIndexStatement showIndexes, S context) {
        new ShowIndexStatementDeParser(builder).deParse(showIndexes);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ShowTablesStatement showTables, S context) {
        new ShowTablesStatementDeparser(builder).deParse(showTables);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Block block, S context) {
        builder.append("BEGIN\n");
        if (block.getStatements() != null) {
            for (Statement stmt : block.getStatements()) {
                stmt.accept(this, context);
                builder.append(";\n");
            }
        }
        builder.append("END");
        if (block.hasSemicolonAfterEnd()) {
            builder.append(";");
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Comment comment, S context) {
        builder.append(comment.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(DescribeStatement describe, S context) {
        builder.append(describe.getDescribeType());
        builder.append(" ");
        builder.append(describe.getTable());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ExplainStatement explainStatement, S context) {
        builder.append(explainStatement.getKeyword()).append(" ");
        if (explainStatement.getTable() != null) {
            builder.append(explainStatement.getTable());
        } else if (explainStatement.getOptions() != null) {
            builder.append(explainStatement.getOptions().values().stream()
                    .map(ExplainStatement.Option::formatOption).collect(Collectors.joining(" ")));
            builder.append(" ");
        }
        if (explainStatement.getStatement() != null) {
            explainStatement.getStatement().accept(this, context);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ShowStatement showStatement, S context) {
        new ShowStatementDeParser(builder).deParse(showStatement);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(DeclareStatement declareStatement, S context) {
        new DeclareStatementDeParser(expressionDeParser, builder).deParse(declareStatement);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Grant grant, S context) {
        GrantDeParser grantDeParser = new GrantDeParser(builder);
        grantDeParser.deParse(grant);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateSchema aThis, S context) {
        builder.append(aThis.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateSequence createSequence, S context) {
        new CreateSequenceDeParser(builder).deParse(createSequence);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AlterSequence alterSequence, S context) {
        new AlterSequenceDeParser(builder).deParse(alterSequence);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        builder.append(createFunctionalStatement.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CreateSynonym createSynonym, S context) {
        new CreateSynonymDeparser(builder).deParse(createSynonym);
        return builder;
    }

    @Override
    void deParse(Statement statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> StringBuilder visit(AlterSession alterSession, S context) {
        new AlterSessionDeParser(builder).deParse(alterSession);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.appendTo(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RenameTableStatement renameTableStatement, S context) {
        renameTableStatement.appendTo(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(PurgeStatement purgeStatement, S context) {
        purgeStatement.appendTo(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AlterSystemStatement alterSystemStatement, S context) {
        alterSystemStatement.appendTo(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(UnsupportedStatement unsupportedStatement, S context) {
        unsupportedStatement.appendTo(builder);
        return builder;
    }

    public ExpressionDeParser getExpressionDeParser() {
        return expressionDeParser;
    }

    public SelectDeParser getSelectDeParser() {
        return selectDeParser;
    }
}
