/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UseStatement;
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

public class StatementValidator extends AbstractValidator<Statement> implements StatementVisitor {


    @Override
    public void visit(CreateIndex createIndex) {
        //        CreateIndexDeParser createIndexDeParser = new CreateIndexDeParser(errors);
        //        createIndexDeParser.deParse(createIndex);
    }

    @Override
    public void visit(CreateTable createTable) {
        //        CreateTableDeParser createTableDeParser = new CreateTableDeParser(this, buffer);
        //        createTableDeParser.deParse(createTable);
    }

    @Override
    public void visit(CreateView createView) {
        //        CreateViewDeParser createViewDeParser = new CreateViewDeParser(buffer);
        //        createViewDeParser.deParse(createView);
    }

    @Override
    public void visit(AlterView alterView) {
        //        AlterViewDeParser alterViewDeParser = new AlterViewDeParser(buffer);
        //        alterViewDeParser.deParse(alterView);
    }

    @Override
    public void visit(Delete delete) {
        // selectValidator.setBuffer(buffer);
        // expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(buffer);
        // selectValidator.setExpressionVisitor(expressionDeParser);
        //        DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser, buffer);
        //        deleteDeParser.deParse(delete);
    }

    @Override
    public void visit(Drop drop) {
        //        DropDeParser dropDeParser = new DropDeParser(buffer);
        //        dropDeParser.deParse(drop);
    }

    @Override
    public void visit(Insert insert) {
        // selectValidator.setBuffer(buffer);
        // expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(buffer);
        // selectValidator.setExpressionVisitor(expressionDeParser);
        // InsertDeParser insertDeParser = new InsertDeParser(expressionDeParser,
        // selectValidator, buffer);
        //        insertDeParser.deParse(insert);
    }

    @Override
    public void visit(Replace replace) {
        // selectValidator.setBuffer(buffer);
        // expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(buffer);
        // selectValidator.setExpressionVisitor(expressionDeParser);
        // ReplaceDeParser replaceDeParser = new ReplaceDeParser(expressionDeParser,
        // selectValidator, buffer);
        //        replaceDeParser.deParse(replace);
    }

    @Override
    public void visit(Select select) {
        // selectValidator.setBuffer(errors);
        // expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(errors);
        // selectValidator.setExpressionVisitor(expressionDeParser);
        //        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
        //            errors.append("WITH ");
        //            for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter.hasNext();) {
        //                WithItem withItem = iter.next();
        // withItem.accept(selectValidator);
        //                if (iter.hasNext()) {
        //                    errors.append(",");
        //                }
        //                errors.append(" ");
        //            }
        //        }
        select.getSelectBody().accept(getValidator(SelectValidator.class));
    }

    @Override
    public void visit(Truncate truncate) {
        //        errors.append("TRUNCATE TABLE ");
        //        errors.append(truncate.getTable());
        //        if (truncate.getCascade()) {
        //            errors.append(" CASCADE");
        //        }
    }

    @Override
    public void visit(Update update) {
        //        selectValidator.setBuffer(errors);
        //        expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(errors);
        //        UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser, selectValidator, errors);
        //        selectValidator.setExpressionVisitor(expressionDeParser);
        //        updateDeParser.deParse(update);

    }

    @Override
    public void visit(Alter alter) {
        //        AlterDeParser alterDeParser = new AlterDeParser(errors);
        //        alterDeParser.deParse(alter);
        //
    }

    @Override
    public void visit(Statements stmts) {
        //        stmts.accept(this);
    }

    @Override
    public void visit(Execute execute) {
        //        selectValidator.setBuffer(errors);
        //        expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(errors);
        //        ExecuteDeParser executeDeParser = new ExecuteDeParser(expressionDeParser, errors);
        //        selectValidator.setExpressionVisitor(expressionDeParser);
        //        executeDeParser.deParse(execute);
    }

    @Override
    public void visit(SetStatement set) {
        //        selectValidator.setBuffer(errors);
        //        expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(errors);
        //        SetStatementDeParser setStatementDeparser = new SetStatementDeParser(expressionDeParser, errors);
        //        selectValidator.setExpressionVisitor(expressionDeParser);
        //        setStatementDeparser.deParse(set);
    }

    @Override
    public void visit(Merge merge) {
        //TODO implementation of a deparser
        //        errors.append(merge.toString());
    }

    @Override
    public void visit(Commit commit) {
        //        errors.append(commit.toString());
    }

    @Override
    public void visit(Upsert upsert) {
        //        selectValidator.setBuffer(errors);
        //        expressionDeParser.setSelectVisitor(selectValidator);
        //        expressionDeParser.setBuffer(errors);
        //        selectValidator.setExpressionVisitor(expressionDeParser);
        //        UpsertDeParser upsertDeParser = new UpsertDeParser(expressionDeParser, selectValidator, errors);
        //        upsertDeParser.deParse(upsert);
    }

    @Override
    public void visit(UseStatement use) {
        //        new UseStatementDeParser(errors).deParse(use);
    }

    @Override
    public void visit(ShowColumnsStatement show) {
        //        new ShowColumnsStatementDeParser(errors).deParse(show);
    }

    @Override
    public void visit(Block block) {
        //        errors.append("BEGIN\n");
        //        if (block.getStatements() != null) {
        //            for (Statement stmt : block.getStatements().getStatements()) {
        //                stmt.accept(this);
        //                errors.append(";\n");
        //            }
        //        }
        //        errors.append("END");
    }

    @Override
    public void visit(Comment comment) {
        //        errors.append(comment.toString());
    }

    @Override
    public void visit(ValuesStatement values) {
        //        expressionDeParser.setBuffer(errors);
        //        new ValuesStatementDeParser(expressionDeParser, errors).deParse(values);
    }

    @Override
    public void visit(DescribeStatement describe) {
        //        errors.append("DESCRIBE ");
        //        errors.append(describe.getTable());
    }

    @Override
    public void visit(ExplainStatement explain) {
        //        errors.append("EXPLAIN ");
        //        if (explain.getOptions() != null) {
        //            errors.append(explain.getOptions().values().stream().map(ExplainStatement.Option::formatOption)
        //                    .collect(Collectors.joining(" ")));
        //            errors.append(" ");
        //        }
        //        explain.getStatement().accept(this);
    }

    @Override
    public void visit(ShowStatement show) {
        //        new ShowStatementDeParser(errors).deParse(show);
    }

    @Override
    public void visit(DeclareStatement declare) {
        //        expressionDeParser.setBuffer(errors);
        //        new DeclareStatementDeParser(expressionDeParser, errors).deParse(declare);
    }

    @Override
    public void visit(Grant grant) {
        //        GrantDeParser grantDeParser = new GrantDeParser(errors);
        //        grantDeParser.deParse(grant);
    }

    @Override
    public void visit(CreateSchema aThis) {
        //        errors.append(aThis.toString());
    }

    @Override
    public void visit(CreateSequence createSequence) {
        //        new CreateSequenceDeParser(errors).deParse(createSequence);
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        //        new AlterSequenceDeParser(errors).deParse(alterSequence);
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        //        errors.append(createFunctionalStatement.toString());
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validate(Statement statement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        // TODO Auto-generated method stub

    }
}
