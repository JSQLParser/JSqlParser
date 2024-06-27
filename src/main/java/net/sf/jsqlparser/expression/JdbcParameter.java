/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A '?' in a statement or a ?&lt;number&gt; e.g. ?4
 */
public class JdbcParameter extends ASTNodeAccessImpl implements Expression {

    private String parameterCharacter = "?";
    private Integer index;
    private boolean useFixedIndex = false;

    public JdbcParameter() {}

    public JdbcParameter(Integer index, boolean useFixedIndex, String parameterCharacter) {
        this.index = index;
        this.useFixedIndex = useFixedIndex;
        this.parameterCharacter = parameterCharacter;

        // This is needed for Parameters starting with "$" like "$2"
        // Those will contain the index in the parameterCharacter
        final Pattern pattern = Pattern.compile("(\\$)(\\d*)");
        final Matcher matcher = pattern.matcher(parameterCharacter);
        if (matcher.find() && matcher.groupCount() == 2) {
            this.useFixedIndex = true;
            this.parameterCharacter = matcher.group(1);
            this.index = Integer.valueOf(matcher.group(2));
        }
    }

    public String getParameterCharacter() {
        return parameterCharacter;
    }

    public JdbcParameter setParameterCharacter(String parameterCharacter) {
        this.parameterCharacter = parameterCharacter;
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public boolean isUseFixedIndex() {
        return useFixedIndex;
    }

    public void setUseFixedIndex(boolean useFixedIndex) {
        this.useFixedIndex = useFixedIndex;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return useFixedIndex ? parameterCharacter + index : parameterCharacter;
    }

    public JdbcParameter withIndex(Integer index) {
        this.setIndex(index);
        return this;
    }

    public JdbcParameter withUseFixedIndex(boolean useFixedIndex) {
        this.setUseFixedIndex(useFixedIndex);
        return this;
    }
}
