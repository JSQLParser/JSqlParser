/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

public abstract class SetOperation extends ASTNodeAccessImpl {
    String modifier;

    public String getModifier() {
        return modifier != null ? modifier : "";
    }

    public boolean isAll() {
        return leadsWith("ALL");
    }

    public void setAll(boolean all) {
        this.modifier = all ? "ALL" : "";
    }

    public boolean isDistinct() {
        return leadsWith("DISTINCT");
    }

    public void setDistinct(boolean distinct) {
        this.modifier = distinct ? "DISTINCT" : "";
    }

    /**
     * Whether the modifier opens with {@code keyword}, which the grammar records verbatim from the
     * source and so may be written in any case. Anchored at the start and required to end on a word
     * boundary, so a {@code CORRESPONDING BY NAME MATCHING(all)} column list cannot be mistaken for
     * the ALL quantifier.
     */
    private boolean leadsWith(String keyword) {
        if (modifier == null) {
            return false;
        }
        String trimmed = modifier.trim();
        return trimmed.regionMatches(true, 0, keyword, 0, keyword.length())
                && (trimmed.length() == keyword.length()
                        || !Character.isLetterOrDigit(trimmed.charAt(keyword.length())));
    }

    private final SetOperationType type;

    public SetOperation(SetOperationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return modifier == null || modifier.isEmpty() ? type.name() : type.name() + " " + modifier;
    }
}
