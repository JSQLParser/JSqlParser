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
 * Decides whether an ALTER TABLE ADD table constraint needs the table-constraint production shared
 * with CREATE TABLE in dialects that otherwise keep the generic ALTER representation. Kept outside
 * the generated parser, whose class is close to the JVM size limits.
 */
final class AlterConstraintLookahead {
    private AlterConstraintLookahead() {}

    /**
     * Whether the action starting at {@code first} is an unnamed CHECK, a FOREIGN KEY, EXCLUDE, or
     * uses a constraint attribute (INITIALLY IMMEDIATE|DEFERRED, NOT VALID, or ENFORCED outside
     * CHECK) before the end of the action.
     */
    static boolean needsSharedTableConstraint(Token first, CCJSqlParserTokenManager source) {
        Token second = next(first, source);
        Token body = first.kind == K_CONSTRAINT ? next(second, source) : first;
        if (first.kind == K_CHECK && second.kind == OPENING_BRACKET || first.kind == K_FOREIGN
                || isExcludeConstraint(body, source)) {
            return true;
        }
        if (first.kind != K_CONSTRAINT && first.kind != K_PRIMARY && first.kind != K_UNIQUE) {
            return false;
        }
        boolean check = body.kind == K_CHECK;
        int depth = 0;
        for (Token token = first; token.kind != EOF; token = next(token, source)) {
            if (token.kind == OPENING_BRACKET) {
                depth++;
            } else if (token.kind == CLOSING_BRACKET) {
                if (depth-- == 0) {
                    return false;
                }
            } else if (depth == 0) {
                if (token.kind == ST_SEMICOLON || token.kind == K_COMMA) {
                    return false;
                }
                if (isConstraintAttribute(token, next(token, source), check)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isConstraintAttribute(Token token, Token next, boolean check) {
        if ("INITIALLY".equalsIgnoreCase(token.image)) {
            return "IMMEDIATE".equalsIgnoreCase(next.image)
                    || "DEFERRED".equalsIgnoreCase(next.image);
        }
        return token.kind == K_NOT && "VALID".equalsIgnoreCase(next.image)
                || token.kind == K_ENFORCED && !check;
    }

    /** EXCLUDE starts an exclusion constraint only before USING, its elements or WHERE. */
    private static boolean isExcludeConstraint(Token token, CCJSqlParserTokenManager source) {
        int following = next(token, source).kind;
        return token.kind == K_EXCLUDE && (following == K_USING
                || following == OPENING_BRACKET || following == K_WHERE);
    }

    private static Token next(Token token, CCJSqlParserTokenManager source) {
        if (token.next == null) {
            token.next = source.getNextToken();
        }
        return token.next;
    }
}
