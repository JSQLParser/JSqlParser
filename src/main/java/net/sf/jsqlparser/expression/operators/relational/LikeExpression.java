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
import java.util.regex.Pattern;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class LikeExpression extends BinaryExpression implements SupportsOldOracleJoinSyntax {
    private boolean not = false;
    private boolean useBinary = false;
    private Expression escapeExpression = null;
    private boolean useOperator = false;
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

    /**
     * The operator spelling of this condition when it was written as one and has one, e.g.
     * {@code !~~} for {@code NOT LIKE}; otherwise {@code null} and the keyword form is used.
     */
    public String getLikeOperator() {
        if (!useOperator) {
            return null;
        }
        switch (likeKeyWord) {
            case LIKE:
                return not ? "!~~" : "~~";
            case ILIKE:
                return not ? "!~~*" : "~~*";
            case GLOB:
                return not ? null : "~~~";
            default:
                return null;
        }
    }

    /**
     * Sets the keyword and {@code NOT} from DuckDB's and PostgreSQL's operator spelling: {@code ~~}
     * is {@code LIKE}, {@code ~~*} {@code ILIKE}, {@code ~~~} {@code GLOB}, and a leading {@code !}
     * negates the first two. The operator is printed while it matches the keyword and {@code NOT}.
     */
    public LikeExpression setLikeOperator(String likeOperator) {
        if (likeOperator == null) {
            throw new IllegalArgumentException("The LIKE operator must not be null");
        }
        switch (likeOperator) {
            case "~~":
            case "!~~":
                likeKeyWord = KeyWord.LIKE;
                break;
            case "~~*":
            case "!~~*":
                likeKeyWord = KeyWord.ILIKE;
                break;
            case "~~~":
                likeKeyWord = KeyWord.GLOB;
                break;
            default:
                throw new IllegalArgumentException("Unknown LIKE operator: " + likeOperator);
        }
        this.not = likeOperator.startsWith("!");
        this.useOperator = true;
        return this;
    }

    @Override
    public String toString() {
        String likeOperator = getLikeOperator();
        String retval = (oraclePriorPosition == ORACLE_PRIOR_START ? "PRIOR " : "")
                + getLeftExpression() + " "
                + (likeOperator != null ? likeOperator
                        : (not ? "NOT " : "")
                                + (likeKeyWord == KeyWord.SIMILAR_TO ? "SIMILAR TO" : likeKeyWord))
                + " " + (useBinary ? "BINARY " : "") + getRightExpression();
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

        private static final Pattern WHITESPACE = Pattern.compile("\\s+");

        public static KeyWord from(String keyword) {
            return Enum.valueOf(KeyWord.class,
                    WHITESPACE.matcher(keyword.toUpperCase(Locale.ROOT)).replaceAll("_"));
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
