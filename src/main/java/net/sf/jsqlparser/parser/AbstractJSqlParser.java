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
import java.util.List;

public abstract class AbstractJSqlParser<P> {

    protected int jdbcParameterIndex = 0;
    protected boolean errorRecovery = false;
    protected List<ParseException> parseErrors = new ArrayList<>();

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

    public P withTimeOut(long timeOutMillSeconds) {
        return withFeature(Feature.timeOut, timeOutMillSeconds);
    }

    public P withBackslashEscapeCharacter() {
        return withFeature(Feature.allowBackslashEscapeCharacter, true);
    }

    public P withBackslashEscapeCharacter(boolean allowBackslashEscapeCharacter) {
        return withFeature(Feature.allowBackslashEscapeCharacter, allowBackslashEscapeCharacter);
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

    public abstract FeatureConfiguration getConfiguration();

    public abstract P me();

    public boolean getAsBoolean(Feature f) {
        return getConfiguration().getAsBoolean(f);
    }

    public Long getAsLong(Feature f) {
        return getConfiguration().getAsLong(f);
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
