/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.ConnectByPriorOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;

/**
 * Every object a parse returns that implements {@link ASTNodeAccess} maps back to its source text.
 * Its node spans at least one character, inside the range of the object that holds it. No two
 * ranges in a statement partly overlap, and the text of an expression's range parses to that
 * expression again. The model is walked through its getters, over the SQL files of the test
 * resources.
 */
public class ASTNodeCoverageTest {
    private static final Path CORPUS = Paths.get("src/test/resources");

    @Test
    public void testEveryParsedNodeMapsBackToItsSource() throws IOException {
        Map<String, String> problems = new TreeMap<>();
        for (Path file : sqlFiles()) {
            String sql = Files.readString(file);
            Object statements;
            try {
                // a statement the parser rejects leaves the rest of its file to check, and the
                // Oracle tests parse in the dialect they are written in
                boolean oracle = file.toString().contains("oracle-tests");
                statements = CCJSqlParserUtil.parseStatements(sql, parser -> {
                    parser.withErrorRecovery();
                    if (oracle) {
                        parser.withDialect(AbstractJSqlParser.Dialect.ORACLE);
                    }
                });
            } catch (JSQLParserException e) {
                // the corpus also holds SQL the parser rejects
                continue;
            }
            new Walk(file.toString(), sql, problems).check(statements);
        }
        assertThat(problems).isEmpty();
    }

    @Test
    public void testRangesOfStatementsClausesAndTypes() throws JSQLParserException {
        String sql = "WITH t AS (SELECT 1) SELECT * FROM t;";
        assertSpans("WITH t AS (SELECT 1) SELECT * FROM t", sql, (ASTNodeAccess) statement(sql));

        sql = "UPDATE t SET a = 1 WHERE b = 2";
        assertSpans(sql, sql, (ASTNodeAccess) statement(sql));

        sql = "SELECT /*+ INDEX(t i) */ a FROM t";
        assertSpans("/*+ INDEX(t i) */", sql, ((PlainSelect) statement(sql)).getOracleHint());

        sql = "SELECT CASE WHEN a = 1 THEN 'x' END, CAST(a AS DECIMAL(10, 2)), s.t.c,"
                + " ROW_NUMBER() OVER (PARTITION BY a ORDER BY b), SUBSTRING(a FROM 2 FOR 3)"
                + " FROM t ORDER BY a DESC NULLS LAST";
        PlainSelect select = (PlainSelect) statement(sql);
        assertSpans("WHEN a = 1 THEN 'x'", sql,
                ((CaseExpression) item(select, 0)).getWhenClauses().get(0));
        assertSpans("DECIMAL(10, 2)", sql, ((CastExpression) item(select, 1)).getColDataType());
        assertSpans("s.t", sql, ((Column) item(select, 2)).getTable());
        assertSpans("PARTITION BY a", sql,
                (ASTNodeAccess) ((AnalyticExpression) item(select, 3))
                        .getPartitionExpressionList());
        // the list prints the parentheses of its function
        assertSpans("(a FROM 2 FOR 3)", sql, ((Function) item(select, 4)).getNamedParameters());
        assertSpans("a DESC NULLS LAST", sql, select.getOrderByElements().get(0));

        sql = "SELECT * FROM a, b WHERE a.id(+) = b.id";
        assertSpans("a.id(+)", sql,
                ((EqualsTo) ((PlainSelect) statement(sql)).getWhere()).getLeftExpression());

        sql = "SELECT id FROM t LIMIT 5 BY id";
        assertSpans("LIMIT 5 BY id", sql, ((PlainSelect) statement(sql)).getLimitBy());

        sql = "CREATE SEQUENCE s INCREMENT BY 1 OWNED BY t.c";
        assertSpans("s INCREMENT BY 1 OWNED BY t.c", sql,
                ((CreateSequence) statement(sql)).getSequence());

        sql = "SELECT 'a'\n'b'";
        Statement merged = CCJSqlParserUtil.parse(sql, parser -> parser.withAdjacentStringLiterals(
                AbstractJSqlParser.AdjacentStringLiterals.NEWLINE));
        assertSpans("'a'\n'b'", sql, item((PlainSelect) merged, 0));
    }

    @Test
    public void testStatementsTheCorpusLacks() throws JSQLParserException {
        Map<String, String> problems = new TreeMap<>();
        for (String sql : new String[] {
                "WITH x AS (INSERT INTO t VALUES (1) RETURNING *) SELECT * FROM x",
                "WITH x AS (UPDATE t SET a = 1 RETURNING *) SELECT * FROM x",
                "WITH x AS (DELETE FROM t RETURNING *) SELECT * FROM x",
                "CREATE SCHEMA s CREATE TABLE t (a INT) CREATE VIEW v AS SELECT 1",
                "CREATE TABLE t AS EXECUTE p",
                "ALTER DEFAULT PRIVILEGES GRANT SELECT ON TABLES TO r",
                "IF a = 1 SELECT 1;",
                "BEGIN SELECT 1; END;"}) {
            new Walk(sql, sql, problems).check(CCJSqlParserUtil.parseStatements(sql));
        }
        String block = "BEGIN x := 1; NULL; foo(1); END;";
        new Walk(block, block, problems).check(CCJSqlParserUtil.parseStatements(block,
                parser -> parser.withDialect(AbstractJSqlParser.Dialect.ORACLE)));
        String opaque = "FOO BAR BAZ";
        new Walk(opaque, opaque, problems).check(CCJSqlParserUtil.parseStatements(opaque,
                parser -> parser.withUnsupportedStatements(true)));
        assertThat(problems).isEmpty();
    }

    @Test
    public void testCommentBeforeEndOfInputHasPosition() throws ParseException {
        String sql = "SELECT a FROM t -- trailing";
        Statements statements = CCJSqlParserUtil.newParser(sql).Statements();
        Token endOfInput = ((ASTNodeAccess) statements.get(0)).getASTNode().jjtGetLastToken().next;
        Token comment = endOfInput.specialToken;
        assertEquals("-- trailing",
                sql.substring(comment.absoluteBegin - 1, comment.absoluteEnd - 1));
    }

    private static Statement statement(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

    private static Expression item(PlainSelect select, int index) {
        return select.getSelectItem(index).getExpression();
    }

    private static void assertSpans(String expected, String sql, ASTNodeAccess part) {
        Node node = part.getASTNode();
        assertEquals(expected, sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1), part.getClass().getSimpleName());
    }

    private static List<Path> sqlFiles() throws IOException {
        try (Stream<Path> files = Files.walk(CORPUS)) {
            return files.filter(file -> file.toString().endsWith(".sql")).sorted()
                    .collect(Collectors.toList());
        }
    }

    private static final class Walk {
        private final String source;
        private final String sql;
        private final Map<String, String> problems;
        private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        private final List<Object[]> ranges = new ArrayList<>();

        Walk(String source, String sql, Map<String, String> problems) {
            this.source = source;
            this.sql = sql;
            this.problems = problems;
        }

        void check(Object statements) {
            visit(statements, null);
            checkNoPartialOverlap();
        }

        private void visit(Object value, int[] enclosing) {
            if (value == null || !visited.add(value)) {
                return;
            }
            int[] range = range(value);
            if (range != null) {
                checkRange(value, range, enclosing);
            }
            int[] inner = range != null ? range : enclosing;
            if (value instanceof Collection) {
                for (Object element : (Collection<?>) value) {
                    visit(element, inner);
                }
            } else if (value instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    visit(entry.getKey(), inner);
                    visit(entry.getValue(), inner);
                }
            } else if (value instanceof Map.Entry) {
                visit(((Map.Entry<?, ?>) value).getKey(), inner);
                visit(((Map.Entry<?, ?>) value).getValue(), inner);
            } else if (value instanceof Optional) {
                ((Optional<?>) value).ifPresent(present -> visit(present, inner));
            }
            if (isModel(value)) {
                for (Method getter : value.getClass().getMethods()) {
                    if (isChildGetter(getter)) {
                        visitChild(value, getter, inner);
                    }
                }
            }
        }

        private void visitChild(Object value, Method getter, int[] enclosing) {
            try {
                Object child = getter.invoke(value);
                if (isHeld(child, getter.invoke(value))) {
                    visit(child, enclosing);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                // a getter that cannot answer for this object has no child to walk
            }
        }

        /**
         * The object's range, or null when it has none, reporting an object that should have one.
         */
        private int[] range(Object value) {
            // an object that prints as nothing, such as a clause left at its default, has no text
            if (!(value instanceof ASTNodeAccess) || value.toString().isBlank()) {
                return null;
            }
            Node node = ((ASTNodeAccess) value).getASTNode();
            if (node == null) {
                report("has no node", value);
                return null;
            }
            if (node.parser != null) {
                report("keeps its parser alive", value);
            }
            int begin = node.jjtGetFirstToken().absoluteBegin - 1;
            int end = node.jjtGetLastToken().absoluteEnd - 1;
            if (begin < 0 || end <= begin || end > sql.length()) {
                report("spans no text", value);
                return null;
            }
            return new int[] {begin, end};
        }

        private void checkRange(Object value, int[] range, int[] enclosing) {
            if (enclosing != null && (range[0] < enclosing[0] || range[1] > enclosing[1])) {
                report("lies outside the object that holds it", value);
            }
            ASTNodeAccess parent = value instanceof ASTNodeAccessImpl
                    ? ((ASTNodeAccessImpl) value).getParent()
                    : null;
            Node parentNode = parent == null ? null : parent.getASTNode();
            if (parentNode != null && (parentNode.jjtGetFirstToken().absoluteBegin - 1 > range[0]
                    || parentNode.jjtGetLastToken().absoluteEnd - 1 < range[1])) {
                report("lies outside its parent " + parent.getClass().getSimpleName(), value);
            }
            ranges.add(new Object[] {range, value});
            String text = sql.substring(range[0], range[1]);
            // PRIOR is an operator only inside CONNECT BY
            if (value instanceof Expression && !(value instanceof ConnectByPriorOperator)) {
                checkReparses(value, text,
                        expression -> CCJSqlParserUtil.parseExpression(expression, false));
            } else if (value instanceof Statement) {
                checkReparses(value, text, CCJSqlParserUtil::parse);
            }
        }

        private void checkReparses(Object value, String text, Parse parse) {
            Object again;
            try {
                again = parse.apply(text);
            } catch (JSQLParserException e) {
                // text that needs its surroundings to parse proves nothing
                return;
            }
            if (again != null
                    && !normalized(again.toString()).equals(normalized(value.toString()))) {
                report("reparses to something else", value, value + " reparses to " + again);
            }
        }

        // sorted by start, the longer first, ranges nest when each ends inside the one still open
        private void checkNoPartialOverlap() {
            ranges.sort(Comparator.<Object[]>comparingInt(entry -> ((int[]) entry[0])[0])
                    .thenComparingInt(entry -> -((int[]) entry[0])[1]));
            Deque<Object[]> open = new ArrayDeque<>();
            for (Object[] entry : ranges) {
                int[] range = (int[]) entry[0];
                while (!open.isEmpty() && ((int[]) open.peek()[0])[1] <= range[0]) {
                    open.pop();
                }
                if (!open.isEmpty() && ((int[]) open.peek()[0])[1] < range[1]) {
                    report("partly overlaps " + open.peek()[1].getClass().getSimpleName(),
                            entry[1], entry[1]);
                }
                open.push(entry);
            }
        }

        private void report(String problem, Object value) {
            report(problem, value, value);
        }

        private void report(String problem, Object value, Object detail) {
            problems.putIfAbsent(value.getClass().getSimpleName() + " " + problem,
                    source + ": " + detail);
        }
    }

    private interface Parse {
        Object apply(String text) throws JSQLParserException;
    }

    private static String normalized(String text) {
        return text.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    // a getter that builds new objects on each call computes them rather than holding them
    private static boolean isHeld(Object child, Object again) {
        if (child instanceof ASTNodeAccess) {
            return child == again;
        }
        if (child instanceof Collection && !((Collection<?>) child).isEmpty()) {
            return again instanceof Collection && !((Collection<?>) again).isEmpty()
                    && ((Collection<?>) child).iterator().next() == ((Collection<?>) again)
                            .iterator().next();
        }
        return true;
    }

    private static boolean isModel(Object value) {
        String name = value.getClass().getName();
        return name.startsWith("net.sf.jsqlparser.")
                && !name.startsWith("net.sf.jsqlparser.parser.")
                && !(value instanceof Enum);
    }

    private static boolean isChildGetter(Method method) {
        return method.getParameterCount() == 0 && method.getName().startsWith("get")
                && !Modifier.isStatic(method.getModifiers())
                && !method.getName().equals("getParent") && !method.getName().equals("getASTNode");
    }
}
