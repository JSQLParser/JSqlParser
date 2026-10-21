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
import java.util.stream.Collectors;
import org.javacc.parser.*;

/** A deliberately small effect-free grammar subset. Anything else keeps the Java fallback. */
final class LookaheadPurity {
    private LookaheadPurity() {}

    static boolean isPure(Context context) {
        if (!plainParser(context) || !plainLexer(context) || customRuntime()) {
            return false;
        }
        for (NormalProduction production : context.globals().bnfproductions) {
            if (!(production instanceof BNFProduction)
                    || !((BNFProduction) production).getDeclarationTokens().isEmpty()
                    || !production.getParameterListTokens().isEmpty()
                    || !production.getThrowsList().isEmpty()
                    || !tokens(production.getReturnTypeTokens()).equals("void")
                    || !pureExpansion(production.getExpansion(),
                            Collections.newSetFromMap(new IdentityHashMap<>()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean plainParser(Context context) {
        List<Token> java = new ArrayList<>(context.globals().cu_to_insertion_point_1);
        java.addAll(context.globals().cu_to_insertion_point_2);
        java.addAll(context.globals().cu_from_insertion_point_2);
        String body = tokens(java);
        String name = context.globals().cu_name;
        return body.equals("public class " + name + " { }")
                || body.equals("class " + name + " { }");
    }

    private static String tokens(List<Token> tokens) {
        return tokens.stream().map(t -> t.image).collect(Collectors.joining(" "));
    }

    private static boolean customRuntime() {
        return Options.getUserCharStream() || Options.getUserTokenManager()
                || Options.getTokenManagerUsesParser() || !blank(Options.getTokenFactory())
                || !blank(Options.getTokenExtends()) || !blank(Options.getTokenManagerSuperClass())
                || !blank(Options.getTokenClass()) && !Options.getTokenClass().equals("Token");
    }

    private static boolean blank(String value) {
        return value == null || value.isEmpty();
    }

    private static boolean plainLexer(Context context) {
        if (context.globals().lexstate_I2S.size() != 1
                || !context.globals().lexstate_I2S.containsValue("DEFAULT")
                || context.globals().nextStateForEof != null
                || context.globals().token_mgr_decls != null
                        && !context.globals().token_mgr_decls.isEmpty()
                || context.globals().actForEof != null
                        && !context.globals().actForEof.getActionTokens().isEmpty()) {
            return false;
        }
        for (TokenProduction production : context.globals().rexprlist) {
            if (!plainTokenProduction(production)) {
                return false;
            }
        }
        return true;
    }

    private static boolean plainTokenProduction(TokenProduction production) {
        for (RegExprSpec specification : production.respecs) {
            if (specification.nsTok != null) {
                return false;
            }
        }
        int braces = 0;
        Set<Token> end = Collections.newSetFromMap(new IdentityHashMap<>());
        end.add(production.lastToken);
        for (Token token = production.firstToken; token != null; token = token.next) {
            if (token.image.equals("{")) {
                braces++;
            }
            if (end.contains(token)) {
                break;
            }
        }
        return braces <= 1;
    }

    private static boolean pureExpansion(Expansion expansion, Set<Expansion> seen) {
        if (expansion == null || !seen.add(expansion)) {
            return expansion != null;
        }
        if (expansion instanceof Action || expansion instanceof TryBlock) {
            return false;
        }
        if (expansion instanceof NonTerminal) {
            return pureCall((NonTerminal) expansion);
        }
        if (expansion instanceof RegularExpression) {
            return pureToken((RegularExpression) expansion);
        }
        if (expansion instanceof Lookahead
                && !pureLookahead((Lookahead) expansion, seen)) {
            return false;
        }
        return LookaheadAnalysis.children(expansion).stream()
                .allMatch(child -> pureExpansion(child, seen));
    }

    private static boolean pureCall(NonTerminal call) {
        return call.getArgumentTokens().isEmpty() && call.getLhsTokens().isEmpty()
                && call.getParametrizedTypeTokens().isEmpty();
    }

    private static boolean pureToken(RegularExpression token) {
        return token.lhsTokens.isEmpty() && token.rhsToken == null;
    }

    private static boolean pureLookahead(Lookahead lookahead, Set<Expansion> seen) {
        return lookahead.getActionTokens().isEmpty()
                && pureExpansion(lookahead.getLaExpansion(), seen);
    }

}
