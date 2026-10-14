/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.RoutineCharacteristic;
import net.sf.jsqlparser.statement.RoutineCharacteristic.Kind;
import net.sf.jsqlparser.statement.RoutineCharacteristic.SecurityClause;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlRoutineCharacteristicsTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '`', value = {
            "CREATE PROCEDURE cp1() COMMENT 'proc comment' SELECT 1|COMMENT",
            "CREATE FUNCTION cf1() RETURNS INT DETERMINISTIC COMMENT 'fn' RETURN 1"
                    + "|DETERMINISTIC COMMENT",
            "CREATE PROCEDURE gp13() COMMENT 'c' LANGUAGE SQL SQL SECURITY INVOKER SELECT 1"
                    + "|COMMENT LANGUAGE_SQL SECURITY_INVOKER",
            "CREATE FUNCTION gf14() RETURNS INT DETERMINISTIC RETURN 1|DETERMINISTIC",
            "CREATE FUNCTION f1 (a INT) RETURNS INT DETERMINISTIC RETURN a + 1|DETERMINISTIC",
            "CREATE DEFINER=CURRENT_USER FUNCTION f2 (s CHAR(20)) RETURNS CHAR(50) "
                    + "CHARSET utf8mb4 NO SQL RETURN CONCAT('Hello, ', s, '!')|NO_SQL",
            "CREATE FUNCTION f() RETURNS VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin "
                    + "READS SQL DATA RETURN 'a'|READS_SQL_DATA",
            "CREATE FUNCTION f() RETURNS SET('a', 'b') CHAR SET latin1 MODIFIES SQL DATA "
                    + "RETURN 'a'|MODIFIES_SQL_DATA",
            "CREATE PROCEDURE p (IN a INT) NOT DETERMINISTIC CONTAINS SQL SQL SECURITY DEFINER "
                    + "BEGIN SELECT a; END|NOT_DETERMINISTIC CONTAINS_SQL SECURITY_DEFINER",
            "CREATE FUNCTION f() RETURNS INT not deterministic comment 'x' return 1"
                    + "|NOT_DETERMINISTIC COMMENT",
            "CREATE PROCEDURE p1 (IN a INT, OUT b INT) BEGIN SELECT a INTO b; END|``",
            "CREATE DEFINER=CURRENT_USER PROCEDURE p3 () SELECT 1|``",
            "CREATE FUNCTION f() RETURNS INT RETURN (SELECT deterministic FROM t)|``",
            "CREATE PROCEDURE p() READS SQL DATA BEGIN DECLARE deterministic INT; END"
                    + "|READS_SQL_DATA"})
    void modelsCharacteristicsWithoutChangingTokens(String sql, String kinds)
            throws JSQLParserException {
        // Without a dialect, the declaration is only captured as tokens.
        CreateFunctionalStatement plain =
                (CreateFunctionalStatement) CCJSqlParserUtil.parse(sql);
        assertTrue(plain.getCharacteristics().isEmpty());

        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB}) {
            Consumer<CCJSqlParser> consumer = parser -> parser.withDialect(dialect);
            CreateFunctionalStatement statement =
                    (CreateFunctionalStatement) CCJSqlParserUtil.parse(sql, consumer);
            assertEquals(kinds.isEmpty() ? List.of() : List.of(kinds.split(" ")),
                    statement.getCharacteristics().stream()
                            .map(characteristic -> characteristic.getKind().name())
                            .collect(Collectors.toList()));

            assertEquals(plain.getFunctionDeclarationParts(),
                    statement.getFunctionDeclarationParts());
            assertEquals(plain.toString(), statement.toString());
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed));
            assertEquals(plain.toString(), deparsed.toString());
            assertEquals(statement.toString(),
                    CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());

            Statements script = CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", consumer);
            assertEquals(2, script.size());
            assertEquals(statement.getCharacteristics().size(),
                    ((CreateFunctionalStatement) script.get(0)).getCharacteristics().size());
        }
    }

    @Test
    void exposesCharacteristicValues() throws JSQLParserException {
        CreateFunctionalStatement procedure = parse("CREATE PROCEDURE gp13() COMMENT 'c' "
                + "LANGUAGE SQL SQL SECURITY INVOKER SELECT 1");
        List<RoutineCharacteristic> characteristics = procedure.getCharacteristics();
        assertEquals("c", characteristics.get(0).getComment().getNotExcapedValue());
        assertEquals("'c'", characteristics.get(0).getValue());
        assertEquals(SecurityClause.SQL_SECURITY, characteristics.get(2).getSecurityClause());
        assertEquals("SQL SECURITY INVOKER", characteristics.get(2).toString());
        assertThrows(UnsupportedOperationException.class,
                () -> characteristics.add(new RoutineCharacteristic(Kind.NO_SQL)));

        CreateFunctionalStatement function =
                parse("CREATE FUNCTION f() RETURNS INT not deterministic comment 'x' return 1");
        // The tokens keep the spelling; the characteristics render canonical keywords.
        assertEquals("CREATE FUNCTION f ( ) RETURNS INT not deterministic comment 'x' return 1",
                function.toString());
        assertEquals("NOT DETERMINISTIC", function.getCharacteristics().get(0).toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '`', value = {"'it\\'s'|it's", "'it''s'|it's",
            "\"dq\"|dq", "'a\\\\b'|a\\b", "'한글 설명 😀'|한글 설명 😀"})
    void decodesComments(String literal, String stored) throws JSQLParserException {
        CreateFunctionalStatement statement =
                parse("CREATE PROCEDURE p() COMMENT " + literal + " SELECT 1");
        RoutineCharacteristic comment = statement.getCharacteristics().get(0);
        assertEquals(Kind.COMMENT, comment.getKind());
        assertEquals(literal, comment.getValue());
        assertEquals(stored, comment.getComment().getNotExcapedValue());
        assertEquals("CREATE PROCEDURE p ( ) COMMENT " + literal + " SELECT 1",
                statement.toString());
    }

    @Test
    void replacingTheTokensClearsTheCharacteristics() throws JSQLParserException {
        CreateFunctionalStatement statement =
                parse("CREATE FUNCTION gf14() RETURNS INT DETERMINISTIC RETURN 1");
        assertEquals(1, statement.getCharacteristics().size());

        statement.addFunctionDeclarationParts("+", "1");
        assertEquals(1, statement.getCharacteristics().size());
        assertEquals("CREATE FUNCTION gf14 ( ) RETURNS INT DETERMINISTIC RETURN 1 + 1",
                statement.toString());

        statement.setFunctionDeclarationParts(List.of("g", "(", ")", "SELECT", "1"));
        assertTrue(statement.getCharacteristics().isEmpty());
        assertEquals("CREATE FUNCTION g ( ) SELECT 1", statement.toString());

        statement.setFunctionDeclarationParts(List.of("g", "(", ")", "NO", "SQL", "SELECT", "1"),
                List.of(new RoutineCharacteristic(Kind.NO_SQL)));
        assertEquals(Kind.NO_SQL, statement.getCharacteristics().get(0).getKind());
        assertEquals("CREATE FUNCTION g ( ) NO SQL SELECT 1", statement.toString());
    }

    @Test
    void otherDialectsKeepOnlyTokens() throws JSQLParserException {
        String postgres = "CREATE FUNCTION add(a integer, b integer) RETURNS integer "
                + "LANGUAGE sql IMMUTABLE STRICT AS $$ SELECT a + b $$";
        assertTrue(((CreateFunctionalStatement) CCJSqlParserUtil.parse(postgres,
                parser -> parser.withDialect(Dialect.POSTGRESQL))).getCharacteristics().isEmpty());
        assertTrue(((CreateFunctionalStatement) CCJSqlParserUtil.parse(postgres))
                .getCharacteristics().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER FUNCTION f1 DETERMINISTIC",
            "ALTER PROCEDURE p1 NOT DETERMINISTIC"})
    void alterKeepsRejectingDeterministic(String sql) throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class,
                CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }

    private static CreateFunctionalStatement parse(String sql) throws JSQLParserException {
        return (CreateFunctionalStatement) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL));
    }
}
