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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class OracleHint extends ASTNodeAccessImpl implements Expression {

    private static final Pattern SINGLE_LINE = Pattern.compile("--\\+ *([^ ].*[^ ])");
    private static final Pattern MULTI_LINE = Pattern.
            compile("\\/\\*\\+ *([^ ].*[^ ]) *\\*+\\/", Pattern.MULTILINE | Pattern.DOTALL);

    private String value;
    private boolean singleLine = false;

    public static boolean isHintMatch(String comment) {
        return SINGLE_LINE.matcher(comment).find()
                || MULTI_LINE.matcher(comment).find();
    }

    public final void setComment(String comment) {
        Matcher m;
        m = SINGLE_LINE.matcher(comment);
        if (m.find()) {
            this.value = m.group(1);
            this.singleLine = true;
            return;
        }
        m = MULTI_LINE.matcher(comment);
        if (m.find()) {
            this.value = m.group(1);
            this.singleLine = false;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (singleLine) {
            return "--+ " + value + "\n";
        } else {
            return "/*+ " + value + " */";
        }
    }

}
