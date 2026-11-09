/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlConvertToCharacterSetTest {
    private static final Consumer<CCJSqlParser> MYSQL = p -> p.withDialect(Dialect.MYSQL);

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t CONVERT TO CHARSET utf8mb4",
            "ALTER TABLE t CONVERT TO CHARSET utf8mb4 COLLATE utf8mb4_bin",
            "ALTER TABLE t CONVERT TO CHARACTER SET DEFAULT",
            "ALTER TABLE t CONVERT TO CHARACTER SET DEFAULT COLLATE utf8mb4_bin",
            "ALTER TABLE t CONVERT TO CHARACTER SET binary",
            "ALTER TABLE t CONVERT TO CHARACTER SET binary COLLATE binary",
            "ALTER TABLE t CONVERT TO CHARACTER SET 'utf8mb4'",
            "ALTER TABLE t CONVERT TO CHARACTER SET utf8mb4 COLLATE 'utf8mb4_bin'",
            "ALTER TABLE t CONVERT TO CHARACTER SET `utf8mb4` COLLATE `utf8mb4_bin`",
            "ALTER TABLE t CONVERT TO CHARSET utf8mb4, ADD COLUMN c INT",
            "ALTER TABLE t DEFAULT CHARSET utf8mb4",
            "ALTER TABLE t CHARSET = utf8mb4 COLLATE = utf8mb4_bin"})
    void keepsEveryCharacterSetForm(String sql) throws JSQLParserException {
        for (Statement statement : new Statement[] {CCJSqlParserUtil.parse(sql),
                CCJSqlParserUtil.parse(sql, MYSQL)}) {
            assertEquals(sql, statement.toString());
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed));
            assertEquals(sql, deparsed.toString());
        }
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1", MYSQL).size());
    }

    @Test
    void exposesCharsetSpellingNameAndCollation() throws JSQLParserException {
        AlterExpression action =
                parseAction("ALTER TABLE t CONVERT TO CHARSET utf8mb4 COLLATE 'utf8mb4_bin'");
        assertEquals(AlterOperation.CONVERT, action.getOperation());
        assertEquals(AlterExpression.ConvertType.CONVERT_TO, action.getConvertType());
        assertTrue(action.isUseCharsetKeyword());
        assertEquals("utf8mb4", action.getCharacterSet());
        assertEquals("'utf8mb4_bin'", action.getCollation());

        action.setUseCharsetKeyword(false);
        action.setCollation("utf8mb4_0900_ai_ci");
        assertEquals("CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci",
                action.toString());

        AlterExpression standard = parseAction("ALTER TABLE t CONVERT TO CHARACTER SET DEFAULT");
        assertFalse(standard.isUseCharsetKeyword());
        assertEquals("DEFAULT", standard.getCharacterSet());
        assertNull(standard.getCollation());
    }

    @Test
    void keepsCharsetSpellingOfTableOptions() throws JSQLParserException {
        AlterExpression action = parseAction("ALTER TABLE t DEFAULT CHARSET = utf8mb4");
        assertEquals(AlterExpression.ConvertType.DEFAULT_CHARACTER_SET, action.getConvertType());
        assertTrue(action.isUseCharsetKeyword());
        assertFalse(
                parseAction("ALTER TABLE t DEFAULT CHARACTER SET = utf8mb4").isUseCharsetKeyword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t CONVERT TO CHARSET",
            "ALTER TABLE t CONVERT TO utf8mb4",
            "ALTER TABLE t CONVERT TO CHARSET = utf8mb4",
            "ALTER TABLE t CONVERT TO CHARACTER SET utf8mb4 COLLATE"})
    void rejectsIncompleteForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    private static AlterExpression parseAction(String sql) throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql, MYSQL);
        assertEquals(1, alter.getAlterExpressions().size());
        return alter.getAlterExpressions().get(0);
    }
}
