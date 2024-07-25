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

import static java.util.stream.Collectors.joining;

import java.lang.reflect.InvocationTargetException;
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
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.*;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
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

        this.selectDeParser.setBuffer(buffer);
        this.selectDeParser.setExpressionVisitor(expressionDeParser);

        this.expressionDeParser.setSelectVisitor(selectDeParser);
        this.expressionDeParser.setBuffer(buffer);
    }

    @Override
    public <S> StringBuilder visit(CreateIndex createIndex, S context) {
        CreateIndexDeParser createIndexDeParser = new CreateIndexDeParser(buffer);
        createIndexDeParser.deParse(createIndex);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateTable createTable, S context) {
        CreateTableDeParser createTableDeParser = new CreateTableDeParser(this, buffer);
        createTableDeParser.deParse(createTable);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateView createView, S context) {
        CreateViewDeParser createViewDeParser = new CreateViewDeParser(buffer);
        createViewDeParser.deParse(createView);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RefreshMaterializedViewStatement materializedViewStatement,
            S context) {
        new RefreshMaterializedViewStatementDeParser(buffer).deParse(materializedViewStatement);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AlterView alterView, S context) {
        AlterViewDeParser alterViewDeParser = new AlterViewDeParser(buffer);
        alterViewDeParser.deParse(alterView);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Delete delete, S context) {
        DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser, buffer);
        deleteDeParser.deParse(delete);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Drop drop, S context) {
        DropDeParser dropDeParser = new DropDeParser(buffer);
        dropDeParser.deParse(drop);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Insert insert, S context) {
        InsertDeParser insertDeParser =
                new InsertDeParser(expressionDeParser, selectDeParser, buffer);
        insertDeParser.deParse(insert);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Select select, S context) {
        select.accept(selectDeParser, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Truncate truncate, S context) {
        buffer.append("TRUNCATE");
        if (truncate.isTableToken()) {
            buffer.append(" TABLE");
        }
        if (truncate.isOnly()) {
            buffer.append(" ONLY");
        }
        buffer.append(" ");
        if (truncate.getTables() != null && !truncate.getTables().isEmpty()) {
            buffer.append(truncate.getTables().stream()
                .map(Table::toString)
                .collect(joining(", ")));
        } else {
            buffer.append(truncate.getTable());
        }
        if (truncate.getCascade()) {
            buffer.append(" CASCADE");
        }

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Update update, S context) {
        UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser, buffer);
        updateDeParser.deParse(update);

        return buffer;
    }

    public <S> StringBuilder visit(Analyze analyzer, S context) {
        buffer.append("ANALYZE ");
        buffer.append(analyzer.getTable());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Alter alter, S context) {
        AlterDeParser alterDeParser = new AlterDeParser(buffer);
        alterDeParser.deParse(alter);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Statements statements, S context) {
        statements.accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Execute execute, S context) {
        ExecuteDeParser executeDeParser = new ExecuteDeParser(expressionDeParser, buffer);
        executeDeParser.deParse(execute);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SetStatement set, S context) {
        SetStatementDeParser setStatementDeparser =
                new SetStatementDeParser(expressionDeParser, buffer);
        setStatementDeparser.deParse(set);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ResetStatement reset, S context) {
        ResetStatementDeParser setStatementDeparser =
                new ResetStatementDeParser(expressionDeParser, buffer);
        setStatementDeparser.deParse(reset);
        return buffer;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    @Override
    public <S> StringBuilder visit(Merge merge, S context) {
        new MergeDeParser(expressionDeParser, selectDeParser, buffer).deParse(merge);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SavepointStatement savepointStatement, S context) {
        buffer.append(savepointStatement.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RollbackStatement rollbackStatement, S context) {
        buffer.append(rollbackStatement.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Commit commit, S context) {
        buffer.append(commit.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Upsert upsert, S context) {
        UpsertDeParser upsertDeParser =
                new UpsertDeParser(expressionDeParser, selectDeParser, buffer);
        upsertDeParser.deParse(upsert);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(UseStatement use, S context) {
        new UseStatementDeParser(buffer).deParse(use);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ShowColumnsStatement show, S context) {
        new ShowColumnsStatementDeParser(buffer).deParse(show);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ShowIndexStatement showIndexes, S context) {
        new ShowIndexStatementDeParser(buffer).deParse(showIndexes);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ShowTablesStatement showTables, S context) {
        new ShowTablesStatementDeparser(buffer).deParse(showTables);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Block block, S context) {
        buffer.append("BEGIN\n");
        if (block.getStatements() != null) {
            for (Statement stmt : block.getStatements().getStatements()) {
                stmt.accept(this, context);
                buffer.append(";\n");
            }
        }
        buffer.append("END");
        if (block.hasSemicolonAfterEnd()) {
            buffer.append(";");
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Comment comment, S context) {
        buffer.append(comment.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DescribeStatement describe, S context) {
        buffer.append(describe.getDescribeType());
        buffer.append(" ");
        buffer.append(describe.getTable());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExplainStatement explainStatement, S context) {
        buffer.append("EXPLAIN ");
        if (explainStatement.getTable() != null) {
            buffer.append(explainStatement.getTable());
        } else if (explainStatement.getOptions() != null) {
            buffer.append(explainStatement.getOptions().values().stream()
                    .map(ExplainStatement.Option::formatOption).collect(Collectors.joining(" ")));
            buffer.append(" ");
        }
        if (explainStatement.getStatement() != null) {
            explainStatement.getStatement().accept(this, context);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ShowStatement showStatement, S context) {
        new ShowStatementDeParser(buffer).deParse(showStatement);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DeclareStatement declareStatement, S context) {
        new DeclareStatementDeParser(expressionDeParser, buffer).deParse(declareStatement);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Grant grant, S context) {
        GrantDeParser grantDeParser = new GrantDeParser(buffer);
        grantDeParser.deParse(grant);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateSchema aThis, S context) {
        buffer.append(aThis.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateSequence createSequence, S context) {
        new CreateSequenceDeParser(buffer).deParse(createSequence);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AlterSequence alterSequence, S context) {
        new AlterSequenceDeParser(buffer).deParse(alterSequence);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        buffer.append(createFunctionalStatement.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CreateSynonym createSynonym, S context) {
        new CreateSynonymDeparser(buffer).deParse(createSynonym);
        return buffer;
    }

    @Override
    void deParse(Statement statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> StringBuilder visit(AlterSession alterSession, S context) {
        new AlterSessionDeParser(buffer).deParse(alterSession);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.appendTo(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RenameTableStatement renameTableStatement, S context) {
        renameTableStatement.appendTo(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(PurgeStatement purgeStatement, S context) {
        purgeStatement.appendTo(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AlterSystemStatement alterSystemStatement, S context) {
        alterSystemStatement.appendTo(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(UnsupportedStatement unsupportedStatement, S context) {
        unsupportedStatement.appendTo(buffer);
        return buffer;
    }

    public ExpressionDeParser getExpressionDeParser() {
        return expressionDeParser;
    }

    public SelectDeParser getSelectDeParser() {
        return selectDeParser;
    }
}
