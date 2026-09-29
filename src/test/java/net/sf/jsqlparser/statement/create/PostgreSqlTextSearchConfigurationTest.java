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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterTextSearchConfiguration;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchConfiguration;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTextSearchConfigurationTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE TEXT SEARCH CONFIGURATION cfg (COPY=pg_catalog.english)",
            "CREATE TEXT SEARCH CONFIGURATION cfg (COPY='english')",
            "CREATE TEXT SEARCH CONFIGURATION app.cfg (PARSER=pg_catalog.default)",
            "ALTER TEXT SEARCH CONFIGURATION cfg ADD MAPPING FOR word WITH simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg ADD MAPPING FOR word,asciiword WITH pg_catalog.simple,english_stem",
            "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING FOR word,asciiword WITH simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING REPLACE english_stem WITH simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING FOR word REPLACE english_stem WITH pg_catalog.simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg DROP MAPPING FOR word,asciiword",
            "ALTER TEXT SEARCH CONFIGURATION cfg DROP MAPPING IF EXISTS FOR word",
            "ALTER TEXT SEARCH CONFIGURATION app.cfg RENAME TO new_cfg",
            "ALTER TEXT SEARCH CONFIGURATION cfg OWNER TO CURRENT_USER",
            "ALTER TEXT SEARCH CONFIGURATION cfg SET SCHEMA app"})
    void configurationAndMappingActionsRoundTrip(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertTrue(statement instanceof CreateTextSearchConfiguration
                || statement instanceof AlterTextSearchConfiguration);
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        roundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        assertTrue(new Validation(List.of(FeaturesAllowed.DDL), sql).validate().isEmpty());
    }

    @Test
    void sourceAndMappingsCanBeChangedWithoutRawTokens() throws JSQLParserException {
        CreateTextSearchConfiguration create =
                (CreateTextSearchConfiguration) CCJSqlParserUtil.parse(
                        "CREATE TEXT SEARCH CONFIGURATION cfg(COPY=pg_catalog.english)");
        assertEquals(CreateTextSearchConfiguration.SourceKind.COPY, create.getSourceKind());
        create.setSourceKind(CreateTextSearchConfiguration.SourceKind.PARSER);
        create.setSourceName("pg_catalog.default");
        assertEquals("CREATE TEXT SEARCH CONFIGURATION cfg (PARSER = pg_catalog.default)",
                create.toString());
        roundTrip(create);
        AlterTextSearchConfiguration alter = (AlterTextSearchConfiguration) CCJSqlParserUtil.parse(
                "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING FOR word REPLACE english_stem WITH simple");
        assertEquals(AlterTextSearchConfiguration.Action.REPLACE_MAPPING, alter.getAction());
        assertEquals(List.of("word"), alter.getTokenTypes());
        assertEquals("english_stem", alter.getOldDictionary());
        alter.setNewDictionary("pg_catalog.simple");
        alter.getTokenTypes().add("asciiword");
        roundTrip(alter);
        alter.getTokenTypes().clear();
        assertEquals(
                "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING REPLACE english_stem WITH pg_catalog.simple",
                alter.toString());
        roundTrip(alter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TEXT SEARCH CONFIGURATION cfg()",
            "CREATE TEXT SEARCH CONFIGURATION cfg(COPY=)",
            "CREATE TEXT SEARCH CONFIGURATION cfg(COPY=english,PARSER=default)",
            "CREATE OR REPLACE TEXT SEARCH CONFIGURATION cfg(COPY=english)",
            "ALTER TEXT SEARCH CONFIGURATION cfg ADD MAPPING WITH simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING WITH simple",
            "ALTER TEXT SEARCH CONFIGURATION cfg DROP MAPPING FOR",
            "ALTER TEXT SEARCH CONFIGURATION cfg ALTER MAPPING REPLACE simple WITH",
            "ALTER TEXT SEARCH CONFIGURATION cfg ADD MAPPING FOR word WITH simple,"})
    void rejectsIncompleteOrIncompatibleForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
