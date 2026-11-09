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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.load.MySqlLoadDataStatement;
import net.sf.jsqlparser.statement.select.MySqlSelectIntoClause;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlFileFormatTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a FROM t INTO OUTFILE '/tmp/o' CHARSET utf8mb4 FIELDS TERMINATED BY ','",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' CHARACTER SET 'utf8mb4' "
                    + "LINES TERMINATED BY '\\n'",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' CHARACTER SET binary",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' COLUMNS ENCLOSED BY '\"' LINES STARTING BY 'x'",
            "SELECT a INTO OUTFILE '/tmp/o' FIELDS ESCAPED BY '^' LINES STARTING BY '>' "
                    + "TERMINATED BY ';' FROM t"})
    void parsesIntoOutfileClauses(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, NO_DIALECT)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertEquals(sql, deparse(statement));
        }
    }

    @Test
    void intoOutfileAcceptsSubclausesInAnyOrder() throws JSQLParserException {
        String sql = "SELECT a INTO OUTFILE '/tmp/o' FIELDS ESCAPED BY '^' TERMINATED BY ',' "
                + "TERMINATED BY ';' LINES TERMINATED BY '\\n' STARTING BY 'x' FROM t";
        String expected = "SELECT a INTO OUTFILE '/tmp/o' FIELDS TERMINATED BY ';' ESCAPED BY '^' "
                + "LINES STARTING BY 'x' TERMINATED BY '\\n' FROM t";
        for (Consumer<CCJSqlParser> dialect : List.of(MYSQL, NO_DIALECT)) {
            Statement statement = CCJSqlParserUtil.parse(sql, dialect);
            assertEquals(expected, statement.toString());
            assertEquals(expected, deparse(statement));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a INTO OUTFILE '/tmp/o' FIELDS FROM t",
            "SELECT a INTO OUTFILE '/tmp/o' LINES FROM t",
            "SELECT a INTO OUTFILE '/tmp/o' COLUMNS LINES TERMINATED BY 'x' FROM t",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' FIELDS",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' LINES",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' FIELDS TERMINATED BY ',' CHARSET latin1",
            "SELECT a FROM t INTO OUTFILE '/tmp/o' LINES TERMINATED BY 'x' FIELDS ESCAPED BY '^'"})
    void rejectsBareOrMisplacedClauses(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void outfileGettersReadTheSharedFormat() throws JSQLParserException {
        MySqlSelectIntoClause into = into("SELECT a FROM t INTO OUTFILE '/tmp/o' CHARSET latin1 "
                + "COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' ESCAPED BY '^' "
                + "LINES STARTING BY 'x' TERMINATED BY ';'");
        MySqlFileFormat format = into.getFileFormat();

        assertEquals("latin1", into.getCharacterSet());
        assertTrue(into.isUseCharsetKeyword());
        assertEquals(MySqlSelectIntoClause.FieldsKeyword.COLUMNS, into.getFieldsKeyword());
        assertEquals(MySqlFileFormat.FieldsKeyword.COLUMNS, format.getFieldsKeyword());
        assertSame(format.getFieldsTerminatedBy(), into.getFieldsTerminatedBy());
        assertTrue(into.isFieldsOptionallyEnclosed());
        assertSame(format.getFieldsEnclosedBy(), into.getFieldsEnclosedBy());
        assertSame(format.getFieldsEscapedBy(), into.getFieldsEscapedBy());
        assertSame(format.getLinesStartingBy(), into.getLinesStartingBy());
        assertSame(format.getLinesTerminatedBy(), into.getLinesTerminatedBy());
        assertTrue(into.hasFieldsClause());
        assertTrue(into.hasLinesClause());

        MySqlSelectIntoClause plain = into("SELECT a FROM t INTO OUTFILE '/tmp/o'");
        assertNull(plain.getFileFormat());
        assertNull(plain.getFieldsKeyword());
        assertNull(plain.getFieldsTerminatedBy());
        assertFalse(plain.isFieldsOptionallyEnclosed());
        assertNull(plain.getLinesTerminatedBy());
        assertFalse(plain.hasFieldsClause());
        assertFalse(plain.hasLinesClause());
    }

    @Test
    void loadDataAndIntoOutfileShareTheFormat() throws JSQLParserException {
        String clauses = "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'";
        MySqlFileFormat outfile =
                into("SELECT a FROM t INTO OUTFILE '/tmp/o' " + clauses).getFileFormat();
        MySqlFileFormat load = ((MySqlLoadDataStatement) CCJSqlParserUtil
                .parse("LOAD DATA INFILE '/tmp/o' INTO TABLE t " + clauses)).getFileFormat();
        assertEquals(clauses, outfile.toString());
        assertEquals(clauses, load.toString());
    }

    @Test
    void legacySettersWriteTheSharedFormat() {
        MySqlSelectIntoClause into = new MySqlSelectIntoClause();
        into.setType(MySqlSelectIntoClause.Type.OUTFILE);
        into.setFileName(new StringValue("/tmp/o"));
        assertNull(into.getFileFormat());
        assertEquals("INTO OUTFILE '/tmp/o'", into.toString());

        // OPTIONALLY set before the character it applies to still renders
        into.setFieldsOptionallyEnclosed(true);
        into.setFieldsEnclosedBy(new StringValue("\""));
        into.setFieldsKeyword(MySqlSelectIntoClause.FieldsKeyword.COLUMNS);
        into.setLinesTerminatedBy(new StringValue(";"));
        into.setCharacterSet("utf8mb4");
        assertEquals(MySqlFileFormat.FieldsKeyword.COLUMNS,
                into.getFileFormat().getFieldsKeyword());
        assertEquals("INTO OUTFILE '/tmp/o' CHARACTER SET utf8mb4 COLUMNS OPTIONALLY ENCLOSED BY "
                + "'\"' LINES TERMINATED BY ';'", into.toString());

        into.setFieldsKeyword(null);
        into.setUseCharsetKeyword(true);
        assertNull(into.getFieldsKeyword());
        assertEquals("INTO OUTFILE '/tmp/o' CHARSET utf8mb4 FIELDS OPTIONALLY ENCLOSED BY '\"' "
                + "LINES TERMINATED BY ';'", into.toString());

        into.withFileFormat(new MySqlFileFormat().withFieldsEscapedBy(new StringValue("^")));
        assertEquals("^", into.getFieldsEscapedBy().getValue());
        assertNull(into.getFieldsEnclosedBy());
        assertEquals("INTO OUTFILE '/tmp/o' CHARSET utf8mb4 FIELDS ESCAPED BY '^'",
                into.toString());
    }

    @Test
    void rendersEachClauseOnlyWhenSet() {
        MySqlFileFormat format = new MySqlFileFormat();
        assertTrue(format.isEmpty());
        assertEquals("", format.toString());

        format.setLinesStartingBy(new StringValue("x"));
        assertFalse(format.hasFieldsClause());
        assertEquals("LINES STARTING BY 'x'", format.toString());

        format.withFieldsEnclosedBy(new StringValue("\"")).withFieldsOptionallyEnclosed(false)
                .withLinesStartingBy(null).withLinesTerminatedBy(new StringValue(";"));
        assertEquals("FIELDS ENCLOSED BY '\"' LINES TERMINATED BY ';'", format.toString());

        // a spelling without a subclause renders as before, though MySQL rejects it
        MySqlFileFormat keywordOnly =
                new MySqlFileFormat().withFieldsKeyword(MySqlFileFormat.FieldsKeyword.FIELDS);
        assertTrue(keywordOnly.hasFieldsClause());
        assertEquals("FIELDS", keywordOnly.toString());
    }

    @Test
    void nodesSpanTheIntoClauseAndItsFormat() throws JSQLParserException {
        String sql = "SELECT a INTO OUTFILE '/tmp/o' CHARSET latin1 FIELDS TERMINATED BY ',' "
                + "LINES TERMINATED BY ';' FROM t";
        MySqlSelectIntoClause into = into(sql);
        assertEquals("INTO OUTFILE '/tmp/o' CHARSET latin1 FIELDS TERMINATED BY ',' "
                + "LINES TERMINATED BY ';'", text(sql, into));
        assertEquals("FIELDS TERMINATED BY ',' LINES TERMINATED BY ';'",
                text(sql, into.getFileFormat()));
        assertEquals("';'", text(sql, into.getLinesTerminatedBy()));
    }

    private static MySqlSelectIntoClause into(String sql) throws JSQLParserException {
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
        return select.getPlainSelect().getMySqlSelectIntoClause();
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    private static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}
