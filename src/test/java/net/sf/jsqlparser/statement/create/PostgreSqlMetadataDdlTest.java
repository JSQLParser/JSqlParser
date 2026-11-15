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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.DefinitionArgument;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterLanguage;
import net.sf.jsqlparser.statement.alter.AlterLargeObject;
import net.sf.jsqlparser.statement.alter.AlterTextSearchDictionary;
import net.sf.jsqlparser.statement.alter.AlterTextSearchParser;
import net.sf.jsqlparser.statement.alter.AlterTextSearchTemplate;
import net.sf.jsqlparser.statement.create.language.CreateLanguage;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchDictionary;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchParser;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchTemplate;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import net.sf.jsqlparser.util.validation.validator.StatementValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** PostgreSQL 18 language, large-object ownership and text-search metadata DDL. */
class PostgreSqlMetadataDdlTest extends ValidationTestAsserts {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, POSTGRESQL);
    }

    static Stream<Arguments> metadataStatements() {
        return Stream.of(
                Arguments.of(
                        "CREATE TRUSTED PROCEDURAL LANGUAGE plsample HANDLER plsample_call_handler INLINE plsample_inline_handler VALIDATOR plsample_validator",
                        "CreateLanguage"),
                Arguments.of("ALTER PROCEDURAL LANGUAGE plsample RENAME TO plsample_new",
                        "AlterLanguage"),
                Arguments.of("ALTER LANGUAGE plsample OWNER TO app_owner", "AlterLanguage"),
                Arguments.of("ALTER LARGE OBJECT 12345 OWNER TO app_owner", "AlterLargeObject"),
                Arguments.of(
                        "CREATE TEXT SEARCH DICTIONARY public.simple_dict (TEMPLATE = pg_catalog.simple)",
                        "CreateTextSearchDictionary"),
                Arguments.of(
                        "ALTER TEXT SEARCH DICTIONARY public.simple_dict (STOPWORDS = english)",
                        "AlterTextSearchDictionary"),
                Arguments.of(
                        "ALTER TEXT SEARCH DICTIONARY public.simple_dict RENAME TO simple_dict2",
                        "AlterTextSearchDictionary"),
                Arguments.of(
                        "CREATE TEXT SEARCH PARSER public.sample_parser (START = prsd_start, GETTOKEN = prsd_nexttoken, END = prsd_end, LEXTYPES = prsd_lextype)",
                        "CreateTextSearchParser"),
                Arguments.of(
                        "ALTER TEXT SEARCH PARSER public.sample_parser RENAME TO sample_parser2",
                        "AlterTextSearchParser"),
                Arguments.of(
                        "CREATE TEXT SEARCH TEMPLATE public.sample_template (LEXIZE = dsimple_lexize)",
                        "CreateTextSearchTemplate"),
                Arguments.of("ALTER TEXT SEARCH TEMPLATE public.sample_template SET SCHEMA app",
                        "AlterTextSearchTemplate"));
    }

    @ParameterizedTest
    @MethodSource("metadataStatements")
    void parsesTypedMetadataStatements(String sql, String statementClass) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement.getClass().getSimpleName()).isEqualTo(statementClass);
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(statement.getClass());
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14);
        validateNoErrors(sql, 1, FeaturesAllowed.DDL);
        Feature feature = Feature.valueOf(Character.toLowerCase(statementClass.charAt(0))
                + statementClass.substring(1));
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT, feature);
        validate(sql, 1, 1, MySqlVersion.V8_0);
        Node node = ((ASTNodeAccess) statement).getASTNode();
        assertThat(node.jjtGetFirstToken().beginColumn).isEqualTo(1);
        assertThat(node.jjtGetLastToken().endColumn).isEqualTo(sql.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE LANGUAGE plsample",
            "CREATE OR REPLACE LANGUAGE plsample",
            "CREATE TRUSTED LANGUAGE \"Language 1\"",
            "CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE \"Language 1\" HANDLER s.\"Handler\" INLINE s.inline_fn VALIDATOR s.validator_fn",
            "CREATE LANGUAGE plsample HANDLER h",
            "CREATE LANGUAGE plsample HANDLER h INLINE i",
            "CREATE LANGUAGE plsample HANDLER h VALIDATOR v",
            "CREATE LANGUAGE plsample HANDLER h NO VALIDATOR",
            "CREATE LANGUAGE plsample HANDLER h INLINE i NO VALIDATOR",
            "ALTER LANGUAGE plsample RENAME TO \"New Language\"",
            "ALTER LANGUAGE plsample OWNER TO CURRENT_USER",
            "ALTER PROCEDURAL LANGUAGE \"Language 1\" OWNER TO CURRENT_ROLE",
            "ALTER LANGUAGE plsample OWNER TO SESSION_USER",
            "ALTER LARGE OBJECT 4294967295 OWNER TO \"Role 1\"",
            "ALTER LARGE OBJECT +12345 OWNER TO CURRENT_ROLE",
            "ALTER LARGE OBJECT 0x3039 OWNER TO CURRENT_USER",
            "ALTER LARGE OBJECT +0x3039 OWNER TO CURRENT_USER",
            "ALTER LARGE OBJECT -1 OWNER TO SESSION_USER",
            "CREATE TEXT SEARCH DICTIONARY s.d (STOPWORDS = english, TEMPLATE = pg_catalog.simple, ACCEPT = FALSE)",
            "CREATE TEXT SEARCH DICTIONARY s.d (TEMPLATE = s.custom_template, count = -2.5, label = E'value')",
            "CREATE TEXT SEARCH DICTIONARY \"S\".\"D\" (\"template\" = pg_catalog.simple)",
            "ALTER TEXT SEARCH DICTIONARY s.d (STOPWORDS)",
            "ALTER TEXT SEARCH DICTIONARY s.d (language = dutch, STOPWORDS)",
            "ALTER TEXT SEARCH DICTIONARY s.d OWNER TO CURRENT_USER",
            "ALTER TEXT SEARCH DICTIONARY s.d OWNER TO CURRENT_ROLE",
            "ALTER TEXT SEARCH DICTIONARY s.d OWNER TO SESSION_USER",
            "ALTER TEXT SEARCH DICTIONARY s.d SET SCHEMA \"New Schema\"",
            "CREATE TEXT SEARCH PARSER p (HEADLINE = s.h, LEXTYPES = s.l, END = s.e, GETTOKEN = s.g, START = s.s)",
            "CREATE TEXT SEARCH PARSER \"S\".\"P\" (\"start\" = s.s, \"gettoken\" = s.g, \"end\" = s.e, \"lextypes\" = s.l)",
            "ALTER TEXT SEARCH PARSER s.p SET SCHEMA \"New Schema\"",
            "CREATE TEXT SEARCH TEMPLATE t (INIT = s.i, LEXIZE = s.l)",
            "CREATE TEXT SEARCH TEMPLATE t (LEXIZE = pg_catalog.dsimple_lexize (1), INIT = s.i)",
            "CREATE TEXT SEARCH TEMPLATE t (LEXIZE = 'dsimple_lexize')",
            "CREATE TEXT SEARCH TEMPLATE t (\"lexize\" = s.l)",
            "ALTER TEXT SEARCH TEMPLATE s.t RENAME TO \"New Template\""})
    void preservesSupportedVariants(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isNotInstanceOf(UnsupportedStatement.class);
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(statement.getClass());
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE LANGUAGE 'plsample' HANDLER h",
            "CREATE LANGUAGE s.plsample HANDLER h",
            "CREATE PROCEDURAL TRUSTED LANGUAGE plsample HANDLER h",
            "CREATE LANGUAGE plsample INLINE i",
            "CREATE LANGUAGE plsample NO VALIDATOR",
            "CREATE LANGUAGE plsample HANDLER h() VALIDATOR v",
            "CREATE LANGUAGE plsample HANDLER h VALIDATOR v INLINE i",
            "CREATE LANGUAGE plsample HANDLER h NO VALIDATOR VALIDATOR v",
            "CREATE LANGUAGE plsample HANDLER",
            "ALTER LANGUAGE plsample SET SCHEMA s",
            "ALTER LANGUAGE plsample OWNER TO s.r",
            "ALTER LARGE OBJECT name OWNER TO r",
            "ALTER LARGE OBJECT '12345' OWNER TO r",
            "ALTER LARGE OBJECT X'3039' OWNER TO r",
            "ALTER LARGE OBJECT 12345 RENAME TO oid2",
            "CREATE OR REPLACE TEXT SEARCH DICTIONARY d (TEMPLATE = simple)",
            "CREATE TEXT SEARCH DICTIONARY d ()",
            "CREATE TEXT SEARCH DICTIONARY d (STOPWORDS = english)",
            "CREATE TEXT SEARCH DICTIONARY d (TEMPLATE)",
            "CREATE TEXT SEARCH DICTIONARY d (TEMPLATE = simple, STOPWORDS)",
            "CREATE TEXT SEARCH PARSER p (START = f, GETTOKEN = f, END = f)",
            "CREATE TEXT SEARCH PARSER p (START = f, GETTOKEN = f, END = f, LEXTYPES = f, UNKNOWN = f)",
            "CREATE TEXT SEARCH PARSER p (START, GETTOKEN = f, END = f, LEXTYPES = f)",
            "CREATE TEXT SEARCH TEMPLATE t (INIT = f)",
            "CREATE TEXT SEARCH TEMPLATE t (LEXIZE)",
            "CREATE OR REPLACE TEXT SEARCH TEMPLATE t (LEXIZE = f)",
            "ALTER TEXT SEARCH PARSER p OWNER TO r",
            "ALTER TEXT SEARCH TEMPLATE t OWNER TO r",
            "ALTER TEXT SEARCH PARSER p (START = f)",
            "ALTER TEXT SEARCH TEMPLATE t (LEXIZE = f)"})
    void rejectsInvalidMetadataForms(String sql) {
        assertThatThrownBy(() -> parse(sql)).isInstanceOf(JSQLParserException.class);
    }

    @Test
    void readsLanguageFieldsAndMaintainsValidatorAlternatives() throws Exception {
        CreateLanguage language = (CreateLanguage) parse(
                "CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE \"L\" HANDLER s.h INLINE s.i NO VALIDATOR");
        assertThat(language.isOrReplace()).isTrue();
        assertThat(language.isTrusted()).isTrue();
        assertThat(language.isProcedural()).isTrue();
        assertThat(language.getName()).isEqualTo("\"L\"");
        assertThat(language.getHandler()).isEqualTo("s.h");
        assertThat(language.getInlineHandler()).isEqualTo("s.i");
        assertThat(language.isNoValidator()).isTrue();
        assertThat(language.getValidator()).isNull();
        language.setValidator("s.v");
        assertThat(language.isNoValidator()).isFalse();
        assertThat(language).hasToString(
                "CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE \"L\" HANDLER s.h INLINE s.i VALIDATOR s.v");
        language.setNoValidator(true);
        assertThat(language.getValidator()).isNull();
        language.setHandler(null);
        assertThat(language.getInlineHandler()).isNull();
        assertThat(language.getValidator()).isNull();
        assertThat(language.isNoValidator()).isFalse();
        assertThat(language).hasToString("CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE \"L\"");
        AlterLanguage alter =
                (AlterLanguage) parse("ALTER PROCEDURAL LANGUAGE plsample OWNER TO CURRENT_ROLE");
        assertThat(alter.isProcedural()).isTrue();
        assertThat(alter.getAction()).isEqualTo(AlterLanguage.Action.OWNER);
        assertThat(alter.getOwner()).isEqualTo("CURRENT_ROLE");
    }

    @Test
    void exposesTypedOrderedDictionaryValuesAndResetOptions() throws Exception {
        CreateTextSearchDictionary dictionary = (CreateTextSearchDictionary) parse(
                "CREATE TEXT SEARCH DICTIONARY s.d (count = -2.5, TEMPLATE = s.custom_template, label = 'text')");
        assertThat(dictionary.getName()).isEqualTo("s.d");
        assertThat(dictionary.getOptions()).extracting(DefinitionElement::getName)
                .containsExactly("count", "TEMPLATE", "label");
        assertThat(dictionary.getTemplate().getKind()).isEqualTo(DefinitionArgument.Kind.TYPE);
        assertThat(dictionary.getTemplate().getDataType().getDataType())
                .isEqualTo("s.custom_template");
        assertThat(dictionary.getOption("count").getArgument().getNumber()).hasToString("-2.5");
        assertThat(dictionary.getOption("label").getArgument().getString().getValue())
                .isEqualTo("text");
        AlterTextSearchDictionary alter = (AlterTextSearchDictionary) parse(
                "ALTER TEXT SEARCH DICTIONARY s.d (STOPWORDS, language = dutch)");
        assertThat(alter.getAction()).isEqualTo(AlterTextSearchDictionary.Action.OPTIONS);
        assertThat(alter.getOption("STOPWORDS").getArgument()).isNull();
        assertThat(alter.getOption("language").getArgument().getDataType().getDataType())
                .isEqualTo("dutch");
        String unicode = "CREATE TEXT SEARCH DICTIONARY d (TEMPLATE = simple, label = U&'text')";
        assertThat(assertSqlCanBeParsedAndDeparsed(unicode, false, POSTGRESQL))
                .isInstanceOf(CreateTextSearchDictionary.class);
    }

    @Test
    void exposesParserAndTemplateFunctionDefinitionsWithoutTableLineage() throws Exception {
        CreateTextSearchParser parser = (CreateTextSearchParser) parse(
                "CREATE TEXT SEARCH PARSER p (LEXTYPES = s.l, END = s.e, GETTOKEN = s.g, START = s.s, HEADLINE = s.h)");
        assertThat(parser.getFunctions()).extracting(DefinitionElement::getName)
                .containsExactly("LEXTYPES", "END", "GETTOKEN", "START", "HEADLINE");
        assertThat(parser.getStartFunction().getDataType().getDataType()).isEqualTo("s.s");
        assertThat(parser.getGetTokenFunction().getDataType().getDataType()).isEqualTo("s.g");
        assertThat(parser.getEndFunction().getDataType().getDataType()).isEqualTo("s.e");
        assertThat(parser.getLexTypesFunction().getDataType().getDataType()).isEqualTo("s.l");
        assertThat(parser.getHeadlineFunction().getDataType().getDataType()).isEqualTo("s.h");
        CreateTextSearchTemplate template = (CreateTextSearchTemplate) parse(
                "CREATE TEXT SEARCH TEMPLATE t (\"lexize\" = pg_catalog.dsimple_lexize(1))");
        assertThat(template.getInitFunction()).isNull();
        assertThat(template.getLexizeFunction().getDataType().getDataType())
                .isEqualTo("pg_catalog.dsimple_lexize");
        assertThat(template.getLexizeFunction().getDataType().getArgumentsStringList())
                .containsExactly("1");
        template = (CreateTextSearchTemplate) parse(
                "CREATE TEXT SEARCH TEMPLATE t (LEXIZE = 'dsimple_lexize')");
        assertThat(template.getLexizeFunction().getKind())
                .isEqualTo(DefinitionArgument.Kind.STRING);
        assertThat(template.getLexizeFunction().getString().getValue()).isEqualTo("dsimple_lexize");
        assertThat(new TablesNamesFinder<Void>().getTables(parser)).isEmpty();
        assertThat(new TablesNamesFinder<Void>().getTables(template)).isEmpty();
    }

    @Test
    void preservesDuplicateDefinitionsAndExposesTheLastValue() throws Exception {
        String dictionarySql =
                "CREATE TEXT SEARCH DICTIONARY d (TEMPLATE = simple, TEMPLATE = snowball, Language = english)";
        CreateTextSearchDictionary dictionary =
                (CreateTextSearchDictionary) assertSqlCanBeParsedAndDeparsed(dictionarySql, false,
                        POSTGRESQL);
        assertThat(dictionary.getOptions()).extracting(DefinitionElement::getName)
                .containsExactly("TEMPLATE", "TEMPLATE", "Language");
        assertThat(dictionary.getTemplate().getDataType().getDataType()).isEqualTo("snowball");
        dictionary = (CreateTextSearchDictionary) parse(
                "CREATE TEXT SEARCH DICTIONARY d (TEMPLATE = simple, \"Template\" = custom)");
        assertThat(dictionary.getTemplate().getDataType().getDataType()).isEqualTo("simple");
        assertThat(dictionary.getOption("\"Template\"").getArgument().getDataType().getDataType())
                .isEqualTo("custom");
        String templateSql =
                "CREATE TEXT SEARCH TEMPLATE t (LEXIZE = dsimple_lexize, LEXIZE = dsynonym_lexize)";
        CreateTextSearchTemplate template =
                (CreateTextSearchTemplate) assertSqlCanBeParsedAndDeparsed(templateSql, false,
                        POSTGRESQL);
        assertThat(template.getFunctions()).hasSize(2);
        assertThat(template.getLexizeFunction().getDataType().getDataType())
                .isEqualTo("dsynonym_lexize");
        String parserSql =
                "CREATE TEXT SEARCH PARSER p (START = prsd_start, START = pg_catalog.prsd_start, GETTOKEN = prsd_nexttoken, END = prsd_end, LEXTYPES = prsd_lextype)";
        CreateTextSearchParser parser =
                (CreateTextSearchParser) assertSqlCanBeParsedAndDeparsed(parserSql, false,
                        POSTGRESQL);
        assertThat(parser.getFunctions()).hasSize(5);
        assertThat(parser.getStartFunction().getDataType().getDataType())
                .isEqualTo("pg_catalog.prsd_start");
        AlterTextSearchDictionary alter = (AlterTextSearchDictionary) parse(
                "ALTER TEXT SEARCH DICTIONARY d (STOPWORDS = english, STOPWORDS)");
        assertThat(alter.getOptions()).hasSize(2);
        assertThat(alter.getOption("STOPWORDS").getArgument()).isNull();
    }

    @Test
    void preservesStatementBoundariesAndContextualIdentifiers() throws Exception {
        String sql = "CREATE LANGUAGE plsample HANDLER h; ALTER LANGUAGE plsample OWNER TO r; "
                + "CREATE TEXT SEARCH TEMPLATE t (LEXIZE = f); ALTER TEXT SEARCH TEMPLATE t SET SCHEMA s; "
                + "ALTER LARGE OBJECT 12345 OWNER TO r; SELECT language, trusted, procedural, dictionary, template, handler, validator, lexize FROM names";
        Statements statements = CCJSqlParserUtil.parseStatements(sql, POSTGRESQL);
        assertThat(statements).hasSize(6);
        assertThat(statements.get(0)).isInstanceOf(CreateLanguage.class);
        assertThat(statements.get(1)).isInstanceOf(AlterLanguage.class);
        assertThat(statements.get(2)).isInstanceOf(CreateTextSearchTemplate.class);
        assertThat(statements.get(3)).isInstanceOf(AlterTextSearchTemplate.class);
        assertThat(statements.get(4)).isInstanceOf(AlterLargeObject.class);
        assertThat(statements.get(5).toString()).contains("language", "handler", "validator");
        assertThat(CCJSqlParserUtil.parse("CREATE LANGUAGE java EXTERNAL NAME 'jar'"))
                .isInstanceOf(UnsupportedStatement.class);
        assertThat(CCJSqlParserUtil.parse("ALTER LANGUAGE java COMPILE"))
                .isInstanceOf(UnsupportedStatement.class);
    }

    @Test
    void numericOidReachesExpressionVisitorsDeparsersAndValidationWithItsRange() throws Exception {
        AlterLargeObject statement =
                (AlterLargeObject) parse("ALTER LARGE OBJECT 12345 OWNER TO r");
        LongValue oid = (LongValue) statement.getLargeObjectId();
        assertThat(oid.getASTNode().jjtGetFirstToken().image).isEqualTo("12345");
        assertThat(oid.getASTNode().jjtGetLastToken().image).isEqualTo("12345");
        List<Expression> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertThat(context).isEqualTo("context");
                visited.add(value);
                return null;
            }
        };
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "context");
        assertThat(visited).containsExactly(oid);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                assertThat(context).isEqualTo("context");
                return getBuilder().append(value.getValue() + 1);
            }
        };
        statement.accept(new StatementDeParser(deparser, new SelectDeParser(), output), "context");
        assertThat(output).hasToString("ALTER LARGE OBJECT 12346 OWNER TO r");
        assertThat(statement).hasToString("ALTER LARGE OBJECT 12345 OWNER TO r");
        visited.clear();
        StatementValidator validator = new StatementValidator() {
            @Override
            protected void validateOptionalExpression(Expression value) {
                visited.add(value);
                super.validateOptionalExpression(value);
            }
        };
        validator.setContext(
                new ValidationContext().setCapabilities(Arrays.asList(FeaturesAllowed.DDL)));
        validator.validate(statement);
        assertThat(visited).containsExactly(oid);
        assertThat(validator.getValidationErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("metadataStatements")
    void dispatchesTypedVisitorsWithContextAndLegacyOverloads(String sql, String statementClass)
            throws Exception {
        List<String> visits = new ArrayList<>();
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<String>() {
            private <S> String record(Statement statement, S context) {
                visits.add(statement.getClass().getSimpleName() + ":" + context);
                return String.valueOf(context);
            }

            @Override
            public <S> String visit(CreateLanguage statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(AlterLanguage statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(AlterLargeObject statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(CreateTextSearchDictionary statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(AlterTextSearchDictionary statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(CreateTextSearchParser statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(AlterTextSearchParser statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(CreateTextSearchTemplate statement, S context) {
                return record(statement, context);
            }

            @Override
            public <S> String visit(AlterTextSearchTemplate statement, S context) {
                return record(statement, context);
            }

        };
        Statement statement = parse(sql);
        assertThat(statement.accept(visitor, "context")).isEqualTo("context");
        if (statement instanceof CreateLanguage) {
            visitor.visit((CreateLanguage) statement);
        } else if (statement instanceof AlterLanguage) {
            visitor.visit((AlterLanguage) statement);
        } else if (statement instanceof AlterLargeObject) {
            visitor.visit((AlterLargeObject) statement);
        } else if (statement instanceof CreateTextSearchDictionary) {
            visitor.visit((CreateTextSearchDictionary) statement);
        } else if (statement instanceof AlterTextSearchDictionary) {
            visitor.visit((AlterTextSearchDictionary) statement);
        } else if (statement instanceof CreateTextSearchParser) {
            visitor.visit((CreateTextSearchParser) statement);
        } else if (statement instanceof AlterTextSearchParser) {
            visitor.visit((AlterTextSearchParser) statement);
        } else if (statement instanceof CreateTextSearchTemplate) {
            visitor.visit((CreateTextSearchTemplate) statement);
        } else if (statement instanceof AlterTextSearchTemplate) {
            visitor.visit((AlterTextSearchTemplate) statement);
        }
        assertThat(visits).containsExactly(statementClass + ":context", statementClass + ":null");
    }

    @Test
    void buildersRenderTypedMetadataWithoutRawStatementParts() {
        CreateLanguage language = new CreateLanguage().withName("l").withOrReplace(true)
                .withTrusted(true).withProcedural(true).withHandler("s.h")
                .withInlineHandler("s.i").withNoValidator(true);
        assertThat(language).hasToString(
                "CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE l HANDLER s.h INLINE s.i NO VALIDATOR");
        assertThat(new AlterLanguage().withName("l").withProcedural(true)
                .withAction(AlterLanguage.Action.RENAME).withNewName("m"))
                .hasToString("ALTER PROCEDURAL LANGUAGE l RENAME TO m");
        assertThat(new AlterLargeObject().withLargeObjectId(new LongValue(12345)).withOwner("r"))
                .hasToString("ALTER LARGE OBJECT 12345 OWNER TO r");
        DefinitionElement function = new DefinitionElement("LEXIZE",
                DefinitionArgument.type(new ColDataType("s.f")));
        assertThat(
                new CreateTextSearchTemplate().withName("t").withFunctions(Arrays.asList(function)))
                .hasToString("CREATE TEXT SEARCH TEMPLATE t (LEXIZE = s.f)");
        assertThat(new CreateTextSearchDictionary().withName("d").withOptions(Arrays.asList(
                new DefinitionElement("TEMPLATE",
                        DefinitionArgument.type(new ColDataType("simple"))))))
                .hasToString("CREATE TEXT SEARCH DICTIONARY d (TEMPLATE = simple)");
        assertThat(new AlterTextSearchDictionary().withName("d")
                .withAction(AlterTextSearchDictionary.Action.OPTIONS)
                .withOptions(Arrays.asList(new DefinitionElement("STOPWORDS", null))))
                .hasToString("ALTER TEXT SEARCH DICTIONARY d (STOPWORDS)");
        assertThat(new AlterTextSearchParser().withName("p")
                .withAction(AlterTextSearchParser.Action.RENAME).withNewName("q"))
                .hasToString("ALTER TEXT SEARCH PARSER p RENAME TO q");
        assertThat(new AlterTextSearchTemplate().withName("t")
                .withAction(AlterTextSearchTemplate.Action.SET_SCHEMA).withSchemaName("s"))
                .hasToString("ALTER TEXT SEARCH TEMPLATE t SET SCHEMA s");
    }

    @ParameterizedTest
    @ValueSource(strings = {"create language l handler h no validator",
            "alter language l owner to r", "alter large object 12345 owner to r",
            "create text search template t (lexize = f)"})
    void lowerCaseKeywordsNormalizeWithoutChangingNames(String sql) throws Exception {
        Statement statement = parse(sql);
        assertThat(statement).isNotInstanceOf(UnsupportedStatement.class);
        assertThat(statement.toString()).isEqualToIgnoringCase(sql);
        assertThat(parse(statement.toString())).hasToString(statement.toString());
    }
}
