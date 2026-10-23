/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static net.sf.jsqlparser.parser.CCJSqlParserConstants.*;

/**
 * Token nesting checks used by grammar lookaheads. Kept outside the generated parser, whose class
 * is close to the size JaCoCo can still instrument.
 */
final class TokenNesting {
    private TokenNesting() {}

    /**
     * Whether the target is not nested in brackets, braces, parentheses or a CASE expression opened
     * from the first token on.
     */
    static boolean isTopLevelFrom(Token first, Token target) {
        int depth = 0;
        for (Token t = first; t != null && t != target; t = t.next) {
            if (t.kind == K_CASE || "(".equals(t.image) || "[".equals(t.image)
                    || "{".equals(t.image)) {
                depth++;
            } else if (t.kind == K_END || ")".equals(t.image) || "]".equals(t.image)
                    || "}".equals(t.image)) {
                depth--;
            }
        }
        return depth == 0;
    }
}
