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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterTriggerTest {

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TRIGGER trg ON users RENAME TO trg_new",
            "ALTER TRIGGER \"Trg\" ON app.\"Users\" RENAME TO \"Trg 2\"",
            "ALTER TRIGGER trg ON users DEPENDS ON EXTENSION ext",
            "ALTER TRIGGER trg ON users NO DEPENDS ON EXTENSION ext"})
    void testRoundTrip(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> consumer : List.<Consumer<CCJSqlParser>>of(parser -> {
        }, parser -> parser.withDialect(Dialect.POSTGRESQL))) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, consumer);
            assertInstanceOf(AlterTrigger.class, statement);
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed));
            assertEquals(sql, deparsed.toString());
            assertEquals(sql, CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());
            assertEquals(2,
                    CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", consumer).size());
        }
    }

    @Test
    void testActionsAndTable() throws JSQLParserException {
        AlterTrigger rename = parse("ALTER TRIGGER trg ON app.users RENAME TO trg_new");
        assertEquals("trg", rename.getName());
        assertEquals("app.users", rename.getTable().getFullyQualifiedName());
        assertEquals(AlterTrigger.Action.RENAME, rename.getAction());
        assertEquals("trg_new", rename.getNewName());
        assertEquals(List.of("app.users"),
                List.copyOf(new TablesNamesFinder().getTables((Statement) rename)));

        AlterTrigger dependency = parse("ALTER TRIGGER trg ON users NO DEPENDS ON EXTENSION ext");
        assertEquals(AlterTrigger.Action.DEPENDS_ON_EXTENSION, dependency.getAction());
        assertEquals("ext", dependency.getExtensionName());
        assertTrue(dependency.isNoDependency());

        dependency.setNoDependency(false);
        dependency.setTable(new Table("orders"));
        assertEquals("ALTER TRIGGER trg ON orders DEPENDS ON EXTENSION ext",
                dependency.toString());
        dependency.setAction(AlterTrigger.Action.RENAME);
        dependency.setNewName("trg2");
        assertEquals("ALTER TRIGGER trg ON orders RENAME TO trg2", dependency.toString());

        assertEquals("context:trg", rename.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterTrigger trigger, S context) {
                return context + ":" + trigger.getName();
            }
        }, "context"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TRIGGER trg ENABLE", "ALTER TRIGGER trg COMPILE",
            "ALTER TRIGGER trg ON t AFTER INSERT AS SELECT 1",
            "ALTER TRIGGER trg ON t RENAME TO t2 CASCADE",
            "ALTER TRIGGER trg ON t DEPENDS ON SCHEMA s"})
    void testOtherFormsRemainUnsupported(String sql) throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql));
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
    }

    @Test
    void testValidation() {
        validateNoErrors("ALTER TRIGGER trg ON users RENAME TO trg_new", 1,
                DatabaseType.POSTGRESQL);
        assertFalse(new Validation(List.of(DatabaseType.MYSQL),
                "ALTER TRIGGER trg ON users RENAME TO trg_new").validate().isEmpty());
    }

    private static AlterTrigger parse(String sql) throws JSQLParserException {
        return (AlterTrigger) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }
}
