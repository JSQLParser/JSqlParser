/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.database.CreateDatabase;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.export.Export;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Derives a {@link StatementFeatures} verdict from a statement tree.
 *
 * <p>
 * Built on the existing adapter chain rather than on a hand-written traversal: only the nodes that
 * <em>contribute</em> a feature are overridden, everything else is inherited from
 * {@link StatementVisitorAdapter} / {@link SelectVisitorAdapter} / {@link ExpressionVisitorAdapter}
 * / {@link FromItemVisitorAdapter}.
 *
 * <h2>Why a top-level flag and not a flag union</h2>
 *
 * {@code RETURNS_RESULT_SET} is a property of the statement's own result position, not of whatever
 * happens to appear somewhere in the tree:
 *
 * <pre>
 *   SELECT * FROM t                                        RETURNS_RESULT_SET, READS_DATA
 *   INSERT INTO x SELECT * FROM t                          READS_DATA, MODIFIES_DATA  (no result set)
 *   DELETE FROM t RETURNING *                              RETURNS_RESULT_SET, MODIFIES_DATA
 *   WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c  RETURNS_RESULT_SET, MODIFIES_DATA
 *   INSERT INTO x WITH c AS (DELETE .. RETURNING *) SELECT MODIFIES_DATA            (no result set)
 * </pre>
 *
 * The first statement visit claims the top level; every nested statement - reached through a
 * data-modifying CTE, a subquery or an IF block - finds it already claimed and can therefore
 * contribute {@code MODIFIES_DATA} but never {@code RETURNS_RESULT_SET}.
 *
 * <h2>Two upstream defects this class works around</h2>
 *
 * <ol>
 * <li>{@code SelectVisitorAdapter#visit(WithItem)} calls {@code withItem.getSelect()}, an unchecked
 * cast to {@code ParenthesedSelect}. A data-modifying CTE reached through the <em>select</em> path
 * - i.e. from a subquery, via {@code ExpressionVisitorAdapter#visit(Select)} - throws
 * ClassCastException. {@link FeatureSelectVisitor} routes through {@code getParenthesedStatement()}
 * instead. Worth fixing in the adapter itself.</li>
 * <li>{@code StatementVisitorAdapter#visit(Delete)} never calls {@code visitReturningClause},
 * unlike its Insert and Update counterparts, so expressions in {@code DELETE .. RETURNING f(x)} are
 * not traversed. Walked explicitly here.</li>
 * </ol>
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class StatementFeatureVisitor extends StatementVisitorAdapter<Void> {

    private final Analysis analysis;

    // ---- construction -----------------------------------------------------------------------

    private StatementFeatureVisitor(Analysis analysis) {
        super(wire(analysis));
        this.analysis = analysis;
        analysis.statements = this;
    }

    /**
     * Builds the mutually recursive visitor chain. The cycle (expression -&gt; select -&gt; from
     * item -&gt; expression) is closed after construction through the adapters' setters, because
     * neither adapter can be handed a reference to something not yet constructed.
     */
    private static SelectVisitorAdapter<Void> wire(Analysis analysis) {
        FeatureExpressionVisitor expressions = new FeatureExpressionVisitor(analysis);
        FeatureFromItemVisitor fromItems = new FeatureFromItemVisitor(analysis);

        FeatureSelectVisitor selects = new FeatureSelectVisitor(analysis, expressions,
                new PivotVisitorAdapter<>(expressions),
                new SelectItemVisitorAdapter<>(expressions), fromItems);

        expressions.setSelectVisitor(selects);
        fromItems.setSelectVisitor((SelectVisitor<Void>) selects).setExpressionVisitor(expressions);

        analysis.expressions = expressions;
        analysis.selects = selects;
        return selects;
    }

    public static StatementFeatures analyse(Statement statement) {
        return analyse(statement, name -> false);
    }

    /**
     * @param pureFunctions names (lower case) the caller can prove side-effect free, e.g. from
     *        {@code pg_proc.provolatile} or a static allow-list. Anything not proven pure leaves
     *        {@code MODIFIES_DATA} in the <em>possible</em> set.
     */
    public static StatementFeatures analyse(Statement statement, Predicate<String> pureFunctions) {
        Analysis analysis = new Analysis(pureFunctions);
        StatementFeatureVisitor visitor = new StatementFeatureVisitor(analysis);
        statement.accept(visitor, null);
        analysis.failLoudIfSilent(statement.getClass().getSimpleName());
        return new StatementFeatures(analysis.certain, analysis.uncertain, analysis.unresolved);
    }

    public static StatementFeatures analyse(String sql) throws JSQLParserException {
        return analyse(CCJSqlParserUtil.parse(sql));
    }

    /**
     * Aggregate verdict over a whole script.
     *
     * <p>
     * {@link Statements} is an {@code ArrayList<Statement>}, not a {@link Statement}, so it cannot
     * go through {@link #analyse(Statement)} and has no {@code getFeatures()} default method -
     * hence this overload.
     *
     * <p>
     * <b>Read the result as a union, and only for guard purposes.</b> Each statement in the script
     * claims its own result position, so {@code UPDATE ..; SELECT ..;} yields both
     * {@code MODIFIES_DATA} and {@code RETURNS_RESULT_SET} - which is the right answer for "may
     * this script write?" and a meaningless one for "executeQuery or executeUpdate?", since the
     * union says nothing about <em>which</em> statement returns rows. Dispatchers want
     * {@link #analyseEach(Statements)}.
     */
    public static StatementFeatures analyse(Statements statements) {
        return analyse(statements, name -> false);
    }

    public static StatementFeatures analyse(Statements statements,
            Predicate<String> pureFunctions) {
        Analysis analysis = new Analysis(pureFunctions);
        StatementFeatureVisitor visitor = new StatementFeatureVisitor(analysis);
        statements.accept(visitor, null);
        analysis.failLoudIfSilent(Statements.class.getSimpleName());
        return new StatementFeatures(analysis.certain, analysis.uncertain, analysis.unresolved);
    }

    /**
     * One verdict per statement, in script order. This is what a dispatcher needs: it answers "does
     * statement <i>n</i> return rows?" instead of "does anything in here return rows?". Each
     * statement is analysed independently, so no feature leaks between them.
     */
    public static List<StatementFeatures> analyseEach(Statements statements) {
        return analyseEach(statements, name -> false);
    }

    public static List<StatementFeatures> analyseEach(Statements statements,
            Predicate<String> pureFunctions) {
        List<StatementFeatures> perStatement = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            perStatement.add(analyse(statement, pureFunctions));
        }
        return perStatement;
    }

    // ---- shared state -----------------------------------------------------------------------

    /**
     * The {@code S context} argument cannot carry this state:
     * {@code StatementVisitorAdapter#visit(Insert)} passes a hardcoded {@code null} context down to
     * the select visitor, so anything threaded through the context is lost at exactly the node that
     * matters most.
     */
    static final class Analysis {
        final EnumSet<StmtFeature> certain = EnumSet.noneOf(StmtFeature.class);
        final EnumSet<StmtFeature> uncertain = EnumSet.noneOf(StmtFeature.class);
        final Set<String> unresolved = new LinkedHashSet<>();
        final Predicate<String> pureFunctions;

        StatementVisitor<Void> statements;
        ExpressionVisitor<Void> expressions;
        SelectVisitor<Void> selects;

        private boolean topLevelClaimed;
        private int readsSuppressed;

        Analysis(Predicate<String> pureFunctions) {
            this.pureFunctions = pureFunctions;
        }

        /** True exactly once per top-level statement; false for everything nested. */
        boolean claimTopLevel() {
            if (topLevelClaimed) {
                return false;
            }
            topLevelClaimed = true;
            return true;
        }

        void releaseTopLevel() {
            topLevelClaimed = false;
        }

        void certain(StmtFeature... features) {
            for (StmtFeature feature : features) {
                if (feature == StmtFeature.READS_DATA && readsSuppressed > 0) {
                    continue;
                }
                certain.add(feature);
            }
        }

        void possible(StmtFeature... features) {
            for (StmtFeature feature : features) {
                uncertain.add(feature);
            }
        }

        void unresolved(String reference) {
            unresolved.add(reference.toLowerCase(Locale.ROOT));
        }

        /** A view definition references tables without reading them. */
        void suppressReads(Runnable body) {
            readsSuppressed++;
            try {
                body.run();
            } finally {
                readsSuppressed--;
            }
        }

        void opaque(String reference) {
            certain.add(StmtFeature.OPAQUE);
            uncertain.addAll(EnumSet.of(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET,
                    StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA));
            unresolved(reference);
        }

        /**
         * A statement that contributed nothing was not classified - almost certainly a statement
         * type added to the grammar after this visitor was written, whose inherited adapter method
         * silently returns null. Degrade loudly, never to "harmless". This is what replaces a
         * hand-maintained type table; the price is that a genuinely inert statement is also
         * reported opaque, which is the right direction to be wrong in.
         */
        void failLoudIfSilent(String label) {
            if (certain.isEmpty()) {
                opaque(label);
            }
        }
    }

    // ---- result-producing clauses -----------------------------------------------------------

    /**
     * RETURNING always ships rows to the client. So does OUTPUT - unless the rows are redirected,
     * which the grammar allows in two ways:
     *
     * <pre>
     *   OUTPUT DELETED.*                    -&gt; rows go to the client
     *   OUTPUT DELETED.id INTO audit        -&gt; rows go to a table       (getOutputTable())
     *   OUTPUT DELETED.id INTO @MyTableVar  -&gt; rows go to a table variable (getTableVariable())
     * </pre>
     *
     * Both redirections have to be checked. {@code OutputClause()} in the grammar is
     * {@code <K_INTO> ( UserVariable() | Table() )}, so the table-variable form leaves
     * {@code getOutputTable()} null - testing only that one would report
     * {@code OUTPUT .. INTO @MyTableVar} as returning a result set, which it does not.
     */
    private void resultClauses(boolean topLevel, ReturningClause returning, OutputClause output) {
        if (!topLevel) {
            return;
        }
        if (returning != null && !returning.isEmpty()) {
            analysis.certain(StmtFeature.RETURNS_RESULT_SET);
        }
        if (output != null && output.getSelectItemList() != null
                && output.getOutputTable() == null && output.getTableVariable() == null) {
            analysis.certain(StmtFeature.RETURNS_RESULT_SET);
        }
        if (output != null && (output.getOutputTable() != null
                || output.getTableVariable() != null)) {
            // OUTPUT .. INTO <table> writes rows somewhere else
            analysis.certain(StmtFeature.MODIFIES_DATA);
        }
    }

    /** Works around defect (2): the adapter does not walk DELETE's RETURNING expressions. */
    private void walkReturning(ReturningClause returning) {
        if (returning == null) {
            return;
        }
        for (SelectItem<?> item : returning) {
            Expression expression = item.getExpression();
            if (expression != null) {
                expression.accept(analysis.expressions, null);
            }
        }
    }

    // ---- DML --------------------------------------------------------------------------------

    @Override
    public <S> Void visit(Select select, S context) {
        if (analysis.claimTopLevel()) {
            analysis.certain(StmtFeature.RETURNS_RESULT_SET);
        }
        // READS_DATA is decided in FeatureSelectVisitor - "SELECT 1" reads nothing.
        return super.visit(select, context);
    }

    @Override
    public <S> Void visit(Insert insert, S context) {
        boolean topLevel = analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA);
        resultClauses(topLevel, insert.getReturningClause(), insert.getOutputClause());
        if (insert.getSelect() != null) {
            analysis.certain(StmtFeature.READS_DATA);
        }
        return super.visit(insert, context);
    }

    @Override
    public <S> Void visit(Update update, S context) {
        boolean topLevel = analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA);
        resultClauses(topLevel, update.getReturningClause(), update.getOutputClause());
        return super.visit(update, context);
    }

    @Override
    public <S> Void visit(Delete delete, S context) {
        boolean topLevel = analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA);
        resultClauses(topLevel, delete.getReturningClause(), delete.getOutputClause());
        super.visit(delete, context);
        walkReturning(delete.getReturningClause());
        return null;
    }

    @Override
    public <S> Void visit(Merge merge, S context) {
        boolean topLevel = analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA);
        resultClauses(topLevel, null, merge.getOutputClause());
        return super.visit(merge, context);
    }

    @Override
    public <S> Void visit(Upsert upsert, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA);
        if (upsert.getSelect() != null) {
            analysis.certain(StmtFeature.READS_DATA);
            upsert.getSelect().accept(analysis.selects, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(Import imprt, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_DATA);
        return null;
    }

    @Override
    public <S> Void visit(Export export, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.READS_DATA);
        if (export.getSelect() != null) {
            export.getSelect().accept(analysis.selects, context);
        }
        return null;
    }

    // ---- DDL --------------------------------------------------------------------------------

    @Override
    public <S> Void visit(CreateTable createTable, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA);
        if (createTable.getSelect() != null) {
            // CTAS reads; it does not return a result set - the top level is already claimed
            analysis.certain(StmtFeature.READS_DATA);
            createTable.getSelect().accept(analysis.selects, context);
        }
        return super.visit(createTable, context);
    }

    @Override
    public <S> Void visit(CreateView createView, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA);
        analysis.suppressReads(() -> {
            if (createView.getSelect() != null) {
                createView.getSelect().accept(analysis.selects, null);
            }
        });
        return null;
    }

    @Override
    public <S> Void visit(AlterView alterView, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA);
        analysis.suppressReads(() -> {
            if (alterView.getSelect() != null) {
                alterView.getSelect().accept(analysis.selects, null);
            }
        });
        return null;
    }

    @Override
    public <S> Void visit(RefreshMaterializedViewStatement materializedView, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA,
                StmtFeature.READS_DATA);
        return null;
    }

    @Override
    public <S> Void visit(Truncate truncate, S context) {
        analysis.claimTopLevel();
        // DDL in most engines (implicit commit, no row triggers) but it destroys rows:
        // a read-only guard must see MODIFIES_DATA here
        analysis.certain(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA);
        return null;
    }

    @Override
    public <S> Void visit(Drop drop, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA);
        return null;
    }

    @Override
    public <S> Void visit(PurgeStatement purgeStatement, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA);
        return null;
    }

    @Override
    public <S> Void visit(Alter alter, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreateIndex createIndex, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreateSchema createSchema, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreateDatabase createDatabase, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreateSequence createSequence, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(AlterSequence alterSequence, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreateSynonym createSynonym, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(CreatePolicy createPolicy, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(RenameTableStatement renameTableStatement, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(Comment comment, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(Grant grant, S context) {
        return schemaOnly();
    }

    @Override
    public <S> Void visit(Analyze analyze, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA, StmtFeature.READS_DATA); // rewrites
                                                                               // statistics
        return null;
    }

    private Void schemaOnly() {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA);
        return null;
    }

    // ---- opaque -----------------------------------------------------------------------------

    @Override
    public <S> Void visit(Execute execute, S context) {
        analysis.claimTopLevel();
        analysis.opaque(String.valueOf(execute.getName()));
        return null;
    }

    @Override
    public <S> Void visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SCHEMA);
        // the body is an opaque token list - nothing about it is knowable here
        analysis.possible(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA);
        analysis.unresolved(createFunctionalStatement.getClass().getSimpleName());
        return null;
    }

    @Override
    public <S> Void visit(UnsupportedStatement unsupportedStatement, S context) {
        analysis.claimTopLevel();
        analysis.opaque("unsupported");
        return null;
    }

    @Override
    public <S> Void visit(ExplainStatement explainStatement, S context) {
        if (analysis.claimTopLevel()) {
            analysis.certain(StmtFeature.RETURNS_RESULT_SET);
        }
        // EXPLAIN ANALYZE executes the statement. The option set is dialect-specific, so the
        // side effects of the explained statement stay in the possible set.
        analysis.possible(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA);
        analysis.unresolved("explain");
        if (explainStatement.getStatement() != null) {
            explainStatement.getStatement().accept(this, context);
        }
        return null;
    }

    // ---- control flow -----------------------------------------------------------------------

    @Override
    public <S> Void visit(Statements statements, S context) {
        for (Statement statement : statements) {
            analysis.releaseTopLevel(); // each statement in a script has its own result position
            statement.accept(this, context);
        }
        analysis.releaseTopLevel();
        return null;
    }

    @Override
    public <S> Void visit(Block block, S context) {
        analysis.claimTopLevel();
        // Statements is itself a List<Statement>; getStatements() is deprecated
        Statements inner = block.getStatements();
        if (inner != null) {
            for (Statement statement : inner) {
                statement.accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(IfElseStatement ifElseStatement, S context) {
        analysis.claimTopLevel();
        // both branches contribute: this is what the statement *may* do, not what it will do
        return super.visit(ifElseStatement, context);
    }

    // ---- session / transaction --------------------------------------------------------------

    @Override
    public <S> Void visit(Commit commit, S context) {
        return transactionOnly();
    }

    @Override
    public <S> Void visit(RollbackStatement rollbackStatement, S context) {
        return transactionOnly();
    }

    @Override
    public <S> Void visit(SavepointStatement savepointStatement, S context) {
        return transactionOnly();
    }

    @Override
    public <S> Void visit(LockStatement lock, S context) {
        return transactionOnly();
    }

    private Void transactionOnly() {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_TRANSACTION);
        return null;
    }

    @Override
    public <S> Void visit(SetStatement set, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(ResetStatement reset, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(UseStatement use, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(SessionStatement sessionStatement, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(AlterSession alterSession, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(DeclareStatement declareStatement, S context) {
        return sessionOnly();
    }

    @Override
    public <S> Void visit(AlterSystemStatement alterSystemStatement, S context) {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SESSION);
        analysis.possible(StmtFeature.MODIFIES_SCHEMA);
        return null;
    }

    private Void sessionOnly() {
        analysis.claimTopLevel();
        analysis.certain(StmtFeature.MODIFIES_SESSION);
        return null;
    }

    // ---- metadata readers -------------------------------------------------------------------

    @Override
    public <S> Void visit(ShowStatement showStatement, S context) {
        return metadataOnly();
    }

    @Override
    public <S> Void visit(ShowColumnsStatement showColumnsStatement, S context) {
        return metadataOnly();
    }

    @Override
    public <S> Void visit(ShowIndexStatement showIndexStatement, S context) {
        return metadataOnly();
    }

    @Override
    public <S> Void visit(ShowTablesStatement showTables, S context) {
        return metadataOnly();
    }

    @Override
    public <S> Void visit(DescribeStatement describe, S context) {
        return metadataOnly();
    }

    /** Reads the catalogue, not the data. */
    private Void metadataOnly() {
        if (analysis.claimTopLevel()) {
            analysis.certain(StmtFeature.RETURNS_RESULT_SET);
        } else {
            analysis.certain(StmtFeature.OPAQUE); // keeps failLoudIfSilent honest when nested
        }
        return null;
    }

    // ---- nested visitors --------------------------------------------------------------------

    static final class FeatureSelectVisitor extends SelectVisitorAdapter<Void> {
        private final Analysis analysis;

        FeatureSelectVisitor(Analysis analysis, ExpressionVisitor<Void> expressionVisitor,
                PivotVisitor<Void> pivotVisitor, SelectItemVisitor<Void> selectItemVisitor,
                FromItemVisitor<Void> fromItemVisitor) {
            super(expressionVisitor, pivotVisitor, selectItemVisitor, fromItemVisitor);
            this.analysis = analysis;
        }

        @Override
        public <S> Void visit(PlainSelect plainSelect, S context) {
            if (plainSelect.getFromItem() != null
                    || plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
                analysis.certain(StmtFeature.READS_DATA);
            }

            // SELECT .. INTO <table> materialises rows instead of returning them, so the
            // RETURNS_RESULT_SET claimed by the statement visit has to be withdrawn
            boolean into = plainSelect.getIntoTables() != null
                    && !plainSelect.getIntoTables().isEmpty()
                    || plainSelect.getIntoTempTable() != null;
            if (into) {
                analysis.certain(StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA);
                analysis.certain.remove(StmtFeature.RETURNS_RESULT_SET);
            }

            if (plainSelect.getForMode() != null) {
                // FOR UPDATE / FOR SHARE take row locks
                analysis.certain(StmtFeature.MODIFIES_TRANSACTION);
            }

            return super.visit(plainSelect, context);
        }

        @Override
        public <S> Void visit(TableStatement tableStatement, S context) {
            analysis.certain(StmtFeature.READS_DATA);
            return super.visit(tableStatement, context);
        }

        /**
         * Replaces the inherited implementation, which casts the CTE body to ParenthesedSelect and
         * therefore throws on {@code WITH c AS (DELETE .. RETURNING *)}. Routing to the statement
         * visitor is also what makes a data-modifying CTE contribute MODIFIES_DATA.
         */
        @Override
        public <S> Void visit(WithItem<?> withItem, S context) {
            ParenthesedStatement body = withItem.getParenthesedStatement();
            if (body instanceof Statement) {
                ((Statement) body).accept(analysis.statements, context);
            }
            return null;
        }
    }

    static final class FeatureExpressionVisitor extends ExpressionVisitorAdapter<Void> {
        private final Analysis analysis;

        FeatureExpressionVisitor(Analysis analysis) {
            this.analysis = analysis;
        }

        /**
         * Volatility is not a syntactic property. Everything the caller has not proven pure stays
         * in the <em>possible</em> set, with the name recorded so it can be resolved against a
         * catalogue rather than guessed at here.
         */
        @Override
        public <S> Void visit(Function function, S context) {
            String name = function.getName() == null
                    ? "?"
                    : function.getName().toLowerCase(Locale.ROOT);
            if (!analysis.pureFunctions.test(name)) {
                analysis.possible(StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA);
                analysis.unresolved(name);
            }
            return super.visit(function, context);
        }
    }

    static final class FeatureFromItemVisitor extends FromItemVisitorAdapter<Void> {
        private final Analysis analysis;

        FeatureFromItemVisitor(Analysis analysis) {
            this.analysis = analysis;
        }

        /** The inherited implementation is empty, so table functions escape the purity check. */
        @Override
        public <S> Void visit(TableFunction tableFunction, S context) {
            if (tableFunction.getFunction() != null) {
                tableFunction.getFunction().accept(analysis.expressions, context);
            }
            return super.visit(tableFunction, context);
        }
    }
}
