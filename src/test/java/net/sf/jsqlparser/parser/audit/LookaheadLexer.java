/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.audit;

import java.util.*;
import org.javacc.parser.*;

/** Binds the complete source of token definitions, including named inline regular expressions. */
final class LookaheadLexer {
    final Map<Integer, Object> namespace = new TreeMap<>();
    boolean fallback;

    LookaheadLexer(Context context, Token first) {
        Map<String, Token> positions = new HashMap<>();
        for (Token token = first; token != null; token = token.next) {
            positions.put(position(token.beginLine, token.beginColumn), token);
        }
        context.globals().rexps_of_tokens.forEach((ordinal, expression) -> {
            if (ordinal == 0) {
                namespace.put(ordinal, "EOF");
            } else {
                Token start = positions.get(position(expression.getLine(), expression.getColumn()));
                List<Object> definition = definition(start);
                if (definition == null || expression.tpContext == null) {
                    fallback = true;
                    namespace.put(ordinal, List.of(expression.label, "unmapped-definition"));
                } else {
                    TokenProduction owner = expression.tpContext;
                    namespace.put(ordinal, List.of(expression.label, definition, owner.kind,
                            owner.ignoreCase, owner.lexStates == null ? List.of("ALL-STATES")
                                    : Arrays.asList(owner.lexStates)));
                }
            }
        });
        if (fallback) {
            List<Object> all = new ArrayList<>();
            for (Token token = first; token != null && token.kind != 0; token = token.next) {
                all.add(List.of(token.kind, token.image));
            }
            namespace.put(-1, LookaheadAnalysis.sha(LookaheadAnalysis.json(all)));
        }
    }

    private static String position(int line, int column) {
        return line + ":" + column;
    }

    private static List<Object> definition(Token start) {
        if (start == null) {
            return null;
        }
        if (start.image.startsWith("\"")) {
            return List.of(List.of(start.kind, start.image));
        }
        if (!start.image.equals("<")) {
            return null;
        }
        List<Object> tokens = new ArrayList<>();
        int depth = 0;
        for (Token token = start; token != null && token.kind != 0; token = token.next) {
            tokens.add(List.of(token.kind, token.image));
            if (token.image.equals("<")) {
                depth++;
            } else if (token.image.equals(">") && --depth == 0) {
                return tokens;
            }
        }
        return null;
    }
}
