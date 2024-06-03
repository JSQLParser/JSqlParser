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
import java.util.stream.Collectors;
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

public class StatementDeParser extends AbstractDeParser<Statement> implements StatementVisitor {

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
    public void visit(CreateIndex createIndex) {
        CreateIndexDeParser createIndexDeParser = new CreateIndexDeParser(buffer);
        createIndexDeParser.deParse(createIndex);
    }

    @Override
    public void visit(CreateTable createTable) {
        CreateTableDeParser createTableDeParser = new CreateTableDeParser(this, buffer);
        createTableDeParser.deParse(createTable);
    }

    @Override
    public void visit(CreateView createView) {
        CreateViewDeParser createViewDeParser = new CreateViewDeParser(buffer);
        createViewDeParser.deParse(createView);
    }

    @Override
    public void visit(RefreshMaterializedViewStatement materializedViewStatement) {
        new RefreshMaterializedViewStatementDeParser(buffer).deParse(materializedViewStatement);
    }

    @Override
    public void visit(AlterView alterView) {
        AlterViewDeParser alterViewDeParser = new AlterViewDeParser(buffer);
        alterViewDeParser.deParse(alterView);
    }

    @Override
    public void visit(Delete delete) {
        DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser, buffer);
        deleteDeParser.deParse(delete);
    }

    @Override
    public void visit(Drop drop) {
        DropDeParser dropDeParser = new DropDeParser(buffer);
        dropDeParser.deParse(drop);
    }

    @Override
    public void visit(Insert insert) {
        InsertDeParser insertDeParser =
                new InsertDeParser(expressionDeParser, selectDeParser, buffer);
        insertDeParser.deParse(insert);
    }

    @Override
    public void visit(Select select) {
        select.accept(selectDeParser);
    }

    @Override
    public void visit(Truncate truncate) {
        buffer.append("TRUNCATE");
        if (truncate.isTableToken()) {
            buffer.append(" TABLE");
        }
        if (truncate.isOnly()) {
            buffer.append(" ONLY");
        }
        buffer.append(" ");
        buffer.append(truncate.getTable());

        if (truncate.getCascade()) {
            buffer.append(" CASCADE");
        }

    }

    @Override
    public void visit(Update update) {
        UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser, buffer);
        updateDeParser.deParse(update);

    }

    public void visit(Analyze analyzer) {
        buffer.append("ANALYZE ");
        buffer.append(analyzer.getTable());
    }

    @Override
    public void visit(Alter alter) {
        AlterDeParser alterDeParser = new AlterDeParser(buffer);
        alterDeParser.deParse(alter);
    }

    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }

    @Override
    public void visit(Execute execute) {
        ExecuteDeParser executeDeParser = new ExecuteDeParser(expressionDeParser, buffer);
        executeDeParser.deParse(execute);
    }

    @Override
    public void visit(SetStatement set) {
        SetStatementDeParser setStatementDeparser =
                new SetStatementDeParser(expressionDeParser, buffer);
        setStatementDeparser.deParse(set);
    }

    @Override
    public void visit(ResetStatement reset) {
        ResetStatementDeParser setStatementDeparser =
                new ResetStatementDeParser(expressionDeParser, buffer);
        setStatementDeparser.deParse(reset);
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    @Override
    public void visit(Merge merge) {
        new MergeDeParser(expressionDeParser, selectDeParser, buffer).deParse(merge);
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
        buffer.append(savepointStatement.toString());
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        buffer.append(rollbackStatement.toString());
    }

    @Override
    public void visit(Commit commit) {
        buffer.append(commit.toString());
    }

    @Override
    public void visit(Upsert upsert) {
        UpsertDeParser upsertDeParser =
                new UpsertDeParser(expressionDeParser, selectDeParser, buffer);
        upsertDeParser.deParse(upsert);
    }

    @Override
    public void visit(UseStatement use) {
        new UseStatementDeParser(buffer).deParse(use);
    }

    @Override
    public void visit(ShowColumnsStatement show) {
        new ShowColumnsStatementDeParser(buffer).deParse(show);
    }

    @Override
    public void visit(ShowIndexStatement showIndexes) {
        new ShowIndexStatementDeParser(buffer).deParse(showIndexes);
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        new ShowTablesStatementDeparser(buffer).deParse(showTables);
    }

    @Override
    public void visit(Block block) {
        buffer.append("BEGIN\n");
        if (block.getStatements() != null) {
            for (Statement stmt : block.getStatements().getStatements()) {
                stmt.accept(this);
                buffer.append(";\n");
            }
        }
        buffer.append("END");
        if (block.hasSemicolonAfterEnd()) {
            buffer.append(";");
        }
    }

    @Override
    public void visit(Comment comment) {
        buffer.append(comment.toString());
    }

    @Override
    public void visit(DescribeStatement describe) {
        buffer.append(describe.getDescribeType());
        buffer.append(" ");
        buffer.append(describe.getTable());
    }

    @Override
    public void visit(ExplainStatement explain) {
        buffer.append("EXPLAIN ");
        if (explain.getTable() != null) {
            buffer.append(explain.getTable());
        } else if (explain.getOptions() != null) {
            buffer.append(explain.getOptions().values().stream()
                    .map(ExplainStatement.Option::formatOption).collect(Collectors.joining(" ")));
            buffer.append(" ");
        }
        if (explain.getStatement() != null) {
            explain.getStatement().accept(this);

        }
    }

    @Override
    public void visit(ShowStatement show) {
        new ShowStatementDeParser(buffer).deParse(show);
    }

    @Override
    public void visit(DeclareStatement declare) {
        new DeclareStatementDeParser(expressionDeParser, buffer).deParse(declare);
    }

    @Override
    public void visit(Grant grant) {
        GrantDeParser grantDeParser = new GrantDeParser(buffer);
        grantDeParser.deParse(grant);
    }

    @Override
    public void visit(CreateSchema aThis) {
        buffer.append(aThis.toString());
    }

    @Override
    public void visit(CreateSequence createSequence) {
        new CreateSequenceDeParser(buffer).deParse(createSequence);
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        new AlterSequenceDeParser(buffer).deParse(alterSequence);
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        buffer.append(createFunctionalStatement.toString());
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        new CreateSynonymDeparser(buffer).deParse(createSynonym);
    }

    @Override
    void deParse(Statement statement) {
        statement.accept(this);
    }

    @Override
    public void visit(AlterSession alterSession) {
        new AlterSessionDeParser(buffer).deParse(alterSession);
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        ifElseStatement.appendTo(buffer);
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        renameTableStatement.appendTo(buffer);
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        purgeStatement.appendTo(buffer);
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        alterSystemStatement.appendTo(buffer);
    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        unsupportedStatement.appendTo(buffer);
    }

    public ExpressionDeParser getExpressionDeParser() {
        return expressionDeParser;
    }

    public SelectDeParser getSelectDeParser() {
        return selectDeParser;
    }
}
