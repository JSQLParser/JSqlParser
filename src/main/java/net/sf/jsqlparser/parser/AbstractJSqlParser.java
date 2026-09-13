/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractJSqlParser<P> {

    protected int jdbcParameterIndex = 0;
    protected boolean errorRecovery = false;
    protected List<ParseException> parseErrors = new ArrayList<>();

    public enum AdjacentStringLiterals {
        OFF, NEWLINE, WHITESPACE
    }

    public enum Dialect {
        ANSI_SQL(AdjacentStringLiterals.NEWLINE), ORACLE, MYSQL(AdjacentStringLiterals.OFF,
                Feature.allowBackslashEscapeCharacter,
                Feature.allowHashLineComments,
                Feature.allowDoubleQuotedStrings), MARIADB(AdjacentStringLiterals.OFF,
                        Feature.allowBackslashEscapeCharacter,
                        Feature.allowHashLineComments,
                        Feature.allowDoubleQuotedStrings), SQLSERVER(AdjacentStringLiterals.OFF,
                                Feature.allowSquareBracketQuotation), POSTGRESQL(
                                        AdjacentStringLiterals.NEWLINE,
                                        Feature.allowDollarQuotedStringTags), H2, EXASOL, BIGQUERY(
                                                AdjacentStringLiterals.WHITESPACE,
                                                Feature.allowDoubleQuotedStrings,
                                                Feature.allowHashLineComments,
                                                Feature.allowBackslashEscapeCharacter), DATABRICKS(
                                                        AdjacentStringLiterals.WHITESPACE,
                                                        Feature.allowDoubleQuotedStrings,
                                                        Feature.allowBackslashEscapeCharacter), SNOWFLAKE(
                                                                Feature.allowBackslashEscapeCharacter), INFORMIX, SPANNER, DORIS, COCKROACHDB, TERADATA;

        private final Set<Feature> lexerFeatures;
        private final AdjacentStringLiterals adjacentStringLiterals;

        Dialect(AdjacentStringLiterals adjacentStringLiterals, Feature... lexerFeatures) {
            this.adjacentStringLiterals = adjacentStringLiterals;
            this.lexerFeatures = lexerFeatures.length == 0 ? EnumSet.noneOf(Feature.class)
                    : EnumSet.copyOf(Arrays.asList(lexerFeatures));
        }

        Dialect(Feature... lexerFeatures) {
            this(AdjacentStringLiterals.OFF, lexerFeatures);
        }

        /** Returns the immutable lexer defaults shared by this dialect preset. */
        public Set<Feature> getLexerFeatures() {
            return Collections.unmodifiableSet(lexerFeatures);
        }

        public AdjacentStringLiterals getAdjacentStringLiterals() {
            return adjacentStringLiterals;
        }
    }

    public P withSquareBracketQuotation() {
        return withFeature(Feature.allowSquareBracketQuotation, true);
    }

    public P withSquareBracketQuotation(boolean allowSquareBracketQuotation) {
        return withFeature(Feature.allowSquareBracketQuotation, allowSquareBracketQuotation);
    }

    public P withAllowComplexParsing() {
        return withFeature(Feature.allowComplexParsing, true);
    }

    public P withAllowComplexParsing(boolean allowComplexParsing) {
        return withFeature(Feature.allowComplexParsing, allowComplexParsing);
    }

    public P withComplexParsing() {
        return withFeature(Feature.allowComplexParsing, true);
    }

    public P withComplexParsing(boolean allowComplexParsing) {
        return withFeature(Feature.allowComplexParsing, allowComplexParsing);
    }

    public P withUnsupportedStatements() {
        return withFeature(Feature.allowUnsupportedStatements, true);
    }

    public P withUnsupportedStatements(boolean allowUnsupportedStatements) {
        return withFeature(Feature.allowUnsupportedStatements, allowUnsupportedStatements);
    }

    /** Enables GROUP BY ASC/DESC for MySQL versions before 8.0.13. Requires MYSQL dialect. */
    public P withLegacyMySqlGroupBy(boolean enabled) {
        return withFeature(Feature.allowLegacyMySqlGroupBy, enabled);
    }

    public P withTimeOut(long timeOutMillSeconds) {
        return withFeature(Feature.timeOut, timeOutMillSeconds);
    }

    /**
     * Applies this dialect's enabled lexer defaults to the current configuration. Existing options
     * remain set; apply explicit overrides after this call, or use a new parser for a fresh preset.
     */
    public P withDialect(Dialect dialect) {
        withFeature(Feature.dialect, dialect.name());
        if (dialect.getAdjacentStringLiterals() != AdjacentStringLiterals.OFF) {
            withAdjacentStringLiterals(dialect.getAdjacentStringLiterals());
        }
        for (Feature lexerFeature : dialect.getLexerFeatures()) {
            withFeature(lexerFeature, true);
        }
        return me();
    }

    public P withAdjacentStringLiterals() {
        return withAdjacentStringLiterals(AdjacentStringLiterals.NEWLINE);
    }

    public P withAdjacentStringLiterals(boolean adjacentStringLiterals) {
        return withAdjacentStringLiterals(
                adjacentStringLiterals ? AdjacentStringLiterals.NEWLINE
                        : AdjacentStringLiterals.OFF);
    }

    public P withAdjacentStringLiterals(AdjacentStringLiterals adjacentStringLiterals) {
        getConfiguration().setValue(Feature.adjacentStringLiterals, adjacentStringLiterals);
        return me();
    }

    public P withAllowedNestingDepth(int allowedNestingDepth) {
        return withFeature(Feature.allowedNestingDepth, allowedNestingDepth);
    }

    public P withBackslashEscapeCharacter() {
        return withFeature(Feature.allowBackslashEscapeCharacter, true);
    }

    public P withBackslashEscapeCharacter(boolean allowBackslashEscapeCharacter) {
        return withFeature(Feature.allowBackslashEscapeCharacter, allowBackslashEscapeCharacter);
    }

    /**
     * Controls tagged dollar quotes; disabled by default, enabled by the PostgreSQL dialect preset.
     * False preserves dollar-containing identifier spellings.
     */
    public P withDollarQuotedStringTags(boolean allowDollarQuotedStringTags) {
        return withFeature(Feature.allowDollarQuotedStringTags, allowDollarQuotedStringTags);
    }

    public P withDoubleQuotedStrings() {
        return withFeature(Feature.allowDoubleQuotedStrings, true);
    }

    public P withDoubleQuotedStrings(boolean allowDoubleQuotedStrings) {
        return withFeature(Feature.allowDoubleQuotedStrings, allowDoubleQuotedStrings);
    }

    public P withHashLineComments() {
        return withFeature(Feature.allowHashLineComments, true);
    }

    public P withHashLineComments(boolean allowHashLineComments) {
        return withFeature(Feature.allowHashLineComments, allowHashLineComments);
    }

    public P withUnparenthesizedSubSelects() {
        return withFeature(Feature.allowUnparenthesizedSubSelects, true);
    }

    public P withUnparenthesizedSubSelects(boolean allowUnparenthesizedSubSelects) {
        return withFeature(Feature.allowUnparenthesizedSubSelects, allowUnparenthesizedSubSelects);
    }

    public P withFeature(Feature f, boolean enabled) {
        getConfiguration().setValue(f, enabled);
        return me();
    }

    public P withFeature(Feature f, long value) {
        getConfiguration().setValue(f, value);
        return me();
    }

    public P withFeature(Feature f, String value) {
        getConfiguration().setValue(f, value);
        return me();
    }

    public abstract FeatureConfiguration getConfiguration();

    public FeatureConfiguration setValue(Feature feature, Object value) {
        return getConfiguration().setValue(feature, value);
    }

    public Object getValue(Feature feature) {
        return getConfiguration().getValue(feature);
    }

    public abstract P me();

    public boolean getAsBoolean(Feature f) {
        return getConfiguration().getAsBoolean(f);
    }

    public Long getAsLong(Feature f) {
        return getConfiguration().getAsLong(f);
    }

    public int getAsInt(Feature f) {
        return getConfiguration().getAsInt(f);
    }

    public Integer getAsInteger(Feature f) {
        return getConfiguration().getAsInteger(f);
    }

    public String getAsString(Feature f) {
        return getConfiguration().getAsString(f);
    }

    public void setErrorRecovery(boolean errorRecovery) {
        this.errorRecovery = errorRecovery;
    }

    public P withErrorRecovery() {
        this.errorRecovery = true;
        return me();
    }

    public P withErrorRecovery(boolean errorRecovery) {
        this.errorRecovery = errorRecovery;
        return me();
    }

    public List<ParseException> getParseErrors() {
        return parseErrors;
    }
}
