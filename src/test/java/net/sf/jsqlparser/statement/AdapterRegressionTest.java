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
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Regressions for two adapter traversal defects.
 */
class AdapterRegressionTest {

    /**
     * {@code SelectVisitorAdapter#visit(WithItem)} used to call {@code withItem.getSelect()}, an
     * unchecked cast to {@code ParenthesedSelect}. Any data-modifying CTE reached through the
     * select path threw {@link ClassCastException} - and the select path is taken for every
     * sub-select, because {@code ExpressionVisitorAdapter#visit(Select)} loops the with-items into
     * the select visitor.
     */
    @Test
    @DisplayName("a data-modifying CTE inside a sub-select no longer throws ClassCastException")
    void dataModifyingCteOnTheSelectPath() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM u WHERE id IN ("
                + "WITH c AS (DELETE FROM t RETURNING id) SELECT id FROM c)");

        assertThatCode(() -> statement.accept(new StatementVisitorAdapter<>(), null))
                .doesNotThrowAnyException();
    }

    /**
     * The CTE body must actually be traversed, not merely survived.
     *
     * <p>
     * This is the test that catches <em>both</em> defects, and it is the one that exposed the
     * larger of the two. Reaching {@code visit(Delete)} here requires:
     *
     * <ol>
     * <li>the expression chain to be connected at all. {@code StatementVisitorAdapter()} used to
     * build its {@code ExpressionVisitorAdapter} with the no-arg constructor, which leaves
     * {@code selectVisitor} null - and {@code ExpressionVisitorAdapter#visit(Select)} returns
     * immediately when it is. Every sub-select inside an expression (IN, EXISTS, scalar) was
     * silently dropped, so no subclass could observe anything below one. Its
     * {@code FromItemVisitorAdapter} had the mirror problem: the no-arg constructor builds a
     * throwaway select/expression chain of its own.</li>
     * <li>{@code SelectVisitorAdapter#visit(WithItem)} to dispatch the CTE body through
     * {@code getParenthesedStatement()} rather than casting it to ParenthesedSelect, and to have a
     * statement visitor to hand a DELETE body to.</li>
     * </ol>
     *
     * <p>
     * Defect (1) is also why (2) never surfaced as a ClassCastException in practice: the traversal
     * never got that far. Fixing the wiring is what makes the cast reachable, so the two fixes have
     * to land together.
     *
     * <p>
     * Note this can only be asserted through its effects - {@code StatementVisitorAdapter} exposes
     * no getters for its sub-visitors, so the wiring cannot be inspected directly. Adding them
     * would be a reasonable follow-up.
     */
    @Test
    @DisplayName("the CTE body is traversed, not skipped")
    void dataModifyingCteBodyIsVisited() throws JSQLParserException {
        List<String> visited = new ArrayList<>();

        StatementVisitorAdapter<Void> visitor = new StatementVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(net.sf.jsqlparser.statement.delete.Delete delete, S context) {
                visited.add("delete");
                return super.visit(delete, context);
            }
        };

        CCJSqlParserUtil
                .parse("SELECT * FROM u WHERE id IN ("
                        + "WITH c AS (DELETE FROM t RETURNING id) SELECT id FROM c)")
                .accept(visitor, null);

        assertThat(visited).containsExactly("delete");
    }

    /**
     * A bare SelectVisitorAdapter has no statement visitor, so it cannot accept a DELETE body. It
     * must skip it rather than fail - the old cast made this case fatal.
     */
    @Test
    @DisplayName("a standalone SelectVisitorAdapter skips a DML CTE instead of failing")
    void standaloneSelectVisitorSkipsDmlCte() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil
                .parse("WITH c AS (DELETE FROM t RETURNING id) SELECT id FROM c");

        SelectVisitorAdapter<Void> selects = new SelectVisitorAdapter<>();
        assertThat(selects.getStatementVisitor()).isNull();
        assertThatCode(() -> select.accept(selects, null)).doesNotThrowAnyException();
    }

    /**
     * {@code StatementVisitorAdapter#visit(Delete)} never called {@code visitReturningClause},
     * unlike its Insert and Update counterparts, so expressions in the RETURNING list were never
     * handed to the expression visitor.
     */
    @Test
    @DisplayName("DELETE .. RETURNING expressions are traversed")
    void deleteReturningExpressionsAreVisited() throws JSQLParserException {
        List<String> functions = new ArrayList<>();

        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Function function, S context) {
                functions.add(function.getName().toLowerCase());
                return super.visit(function, context);
            }
        };

        StatementVisitorAdapter<Void> visitor =
                new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions));

        CCJSqlParserUtil.parse("DELETE FROM t WHERE a = 1 RETURNING upper(name)")
                .accept(visitor, null);

        assertThat(functions).contains("upper");
    }
}
