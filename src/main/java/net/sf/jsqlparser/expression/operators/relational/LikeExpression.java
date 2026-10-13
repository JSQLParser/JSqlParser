/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import java.util.Locale;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class LikeExpression extends BinaryExpression implements SupportsOldOracleJoinSyntax {
    private boolean not = false;
    private boolean useBinary = false;
    private Expression escapeExpression = null;
    private String likeOperator = null;
    private KeyWord likeKeyWord = KeyWord.LIKE;

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    public boolean isUseBinary() {
        return useBinary;
    }

    public LikeExpression setUseBinary(boolean useBinary) {
        this.useBinary = useBinary;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Deprecated
    @Override
    public String getStringExpression() {
        return likeKeyWord.toString();
    }

    /** DuckDB's and PostgreSQL's operator spelling, e.g. {@code !~~} for {@code NOT LIKE}. */
    public String getLikeOperator() {
        return likeOperator;
    }

    /**
     * Sets the operator spelling and the matching keyword: {@code ~~} is {@code LIKE}, {@code ~~*}
     * {@code ILIKE}, {@code ~~~} {@code GLOB}, and a leading {@code !} negates.
     */
    public LikeExpression setLikeOperator(String likeOperator) {
        boolean negated = likeOperator.startsWith("!");
        String operator = negated ? likeOperator.substring(1) : likeOperator;
        switch (operator) {
            case "~~":
                likeKeyWord = KeyWord.LIKE;
                break;
            case "~~*":
                likeKeyWord = KeyWord.ILIKE;
                break;
            case "~~~":
                likeKeyWord = KeyWord.GLOB;
                break;
            default:
                throw new IllegalArgumentException("Unknown LIKE operator: " + likeOperator);
        }
        this.not = negated;
        this.likeOperator = likeOperator;
        return this;
    }

    @Override
    public String toString() {
        if (likeOperator != null) {
            return getLeftExpression() + " " + likeOperator + " " + getRightExpression()
                    + (escapeExpression != null ? " ESCAPE " + escapeExpression : "");
        }
        String retval = (oraclePriorPosition == ORACLE_PRIOR_START ? "PRIOR " : "")
                + getLeftExpression() + " " + (not ? "NOT " : "")
                + (likeKeyWord == KeyWord.SIMILAR_TO ? "SIMILAR TO" : likeKeyWord) + " "
                + (useBinary ? "BINARY " : "") + getRightExpression();
        if (escapeExpression != null) {
            retval += " ESCAPE " + escapeExpression;
        }
        return retval;
    }

    public Expression getEscape() {
        return escapeExpression;
    }

    public void setEscape(Expression escapeExpression) {
        this.escapeExpression = escapeExpression;
    }

    @Deprecated
    public boolean isCaseInsensitive() {
        return likeKeyWord == KeyWord.ILIKE;
    }

    @Deprecated
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.likeKeyWord = KeyWord.ILIKE;
    }

    public KeyWord getLikeKeyWord() {
        return likeKeyWord;
    }

    public LikeExpression setLikeKeyWord(KeyWord likeKeyWord) {
        this.likeKeyWord = likeKeyWord;
        return this;
    }

    public LikeExpression setLikeKeyWord(String likeKeyWord) {
        this.likeKeyWord = KeyWord.from(likeKeyWord);
        return this;
    }

    public LikeExpression withEscape(Expression escape) {
        this.setEscape(escape);
        return this;
    }

    @Deprecated
    public LikeExpression withCaseInsensitive(boolean caseInsensitive) {
        this.setCaseInsensitive(caseInsensitive);
        return this;
    }

    public LikeExpression withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    @Override
    public LikeExpression withLeftExpression(Expression arg0) {
        return (LikeExpression) super.withLeftExpression(arg0);
    }

    @Override
    public LikeExpression withRightExpression(Expression arg0) {
        return (LikeExpression) super.withRightExpression(arg0);
    }

    public enum KeyWord {
        LIKE, ILIKE, RLIKE, REGEXP_LIKE, REGEXP, GLOB, SIMILAR_TO, MATCH_ANY, MATCH_ALL, MATCH_PHRASE, MATCH_PHRASE_PREFIX, MATCH_REGEXP;

        public static KeyWord from(String keyword) {
            return Enum.valueOf(KeyWord.class,
                    keyword.toUpperCase(Locale.ROOT).replaceAll("\\s+", "_"));
        }
    }

    private int oraclePriorPosition = NO_ORACLE_PRIOR;

    @Override
    public int getOldOracleJoinSyntax() {
        return NO_ORACLE_JOIN;
    }

    @Override
    public void setOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        throw new IllegalArgumentException(
                "oracle join operator (+) is not supported on this condition");
    }

    @Override
    public LikeExpression withOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        setOldOracleJoinSyntax(oldOracleJoinSyntax);
        return this;
    }

    @Override
    public int getOraclePriorPosition() {
        return oraclePriorPosition;
    }

    @Override
    public void setOraclePriorPosition(int oraclePriorPosition) {
        this.oraclePriorPosition = oraclePriorPosition;
    }

    public LikeExpression withOraclePriorPosition(int oraclePriorPosition) {
        setOraclePriorPosition(oraclePriorPosition);
        return this;
    }

}
