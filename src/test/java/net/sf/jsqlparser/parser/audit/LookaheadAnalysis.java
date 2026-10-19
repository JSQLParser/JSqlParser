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

/** Conservative lexical facts for review; never a parser equivalence proof. */
final class LookaheadAnalysis {
    final Context context;
    final Map<String, First> first = new LinkedHashMap<>();
    final Map<String, Set<Integer>> follows = new LinkedHashMap<>();
    final IdentityHashMap<Expansion, Set<Integer>> localFollows = new IdentityHashMap<>();
    final IdentityHashMap<Expansion, Expansion> parents = new IdentityHashMap<>();
    final List<Map<String, Object>> rows = new ArrayList<>();
    private final int defaultLookahead;

    static final class First {
        final Set<Integer> tokens = new TreeSet<>();
        boolean nullable;
        final Set<String> caveats = new TreeSet<>();

        void add(First other) {
            tokens.addAll(other.tokens);
            caveats.addAll(other.caveats);
        }

        String signature() {
            return tokens + ":" + nullable + ":" + caveats;
        }
    }

    LookaheadAnalysis(Context context) {
        this.context = context;
        defaultLookahead = Options.getLookahead();
    }

    static List<Expansion> children(Expansion e) {
        if (e instanceof Sequence) {
            return ((Sequence) e).units;
        }
        if (e instanceof Choice) {
            return ((Choice) e).getChoices();
        }
        if (e instanceof ZeroOrOne) {
            return List.of(((ZeroOrOne) e).getExpansion());
        }
        if (e instanceof ZeroOrMore) {
            return List.of(((ZeroOrMore) e).getExpansion());
        }
        if (e instanceof OneOrMore) {
            return List.of(((OneOrMore) e).getExpansion());
        }
        if (e instanceof TryBlock) {
            return List.of(((TryBlock) e).exp);
        }
        return List.of(); // A NonTerminal is a call; do not traverse its target here.
    }

    First inspect(Expansion e) {
        First f = new First();
        if (e == null) {
            f.caveats.add("opaque-production");
            return f;
        }
        if (e instanceof RegularExpression) {
            return inspectToken((RegularExpression) e, f);
        }
        if (e instanceof Lookahead) {
            return inspectLookahead((Lookahead) e, f);
        }
        if (e instanceof Action) {
            f.nullable = true;
            f.caveats.add("java-action");
            return f;
        }
        if (e instanceof NonTerminal) {
            return inspectCall((NonTerminal) e, f);
        }
        if (e instanceof Sequence) {
            return inspectSequence((Sequence) e, f);
        }
        if (e instanceof Choice) {
            return inspectChoice((Choice) e, f);
        }
        return inspectGroup(e, f);
    }

    private First inspectToken(RegularExpression token, First result) {
        result.tokens.add(token.ordinal);
        if (!token.lhsTokens.isEmpty() || token.rhsToken != null) {
            result.caveats.add("java-token-assignment");
        }
        return result;
    }

    private First inspectLookahead(Lookahead lookahead, First result) {
        result.nullable = true;
        if (!lookahead.getActionTokens().isEmpty()) {
            result.caveats.add("semantic-predicate");
        }
        return result;
    }

    private First inspectCall(NonTerminal call, First result) {
        if (!call.getLhsTokens().isEmpty() || !call.getArgumentTokens().isEmpty()
                || !call.getParametrizedTypeTokens().isEmpty()) {
            result.caveats.add("java-call");
        }
        First target = first.get(call.getName());
        if (target == null) {
            result.caveats.add("unresolved-call");
        } else {
            result.add(target);
            result.nullable = target.nullable;
        }
        return result;
    }

    private First inspectSequence(Sequence sequence, First result) {
        result.nullable = true;
        for (Expansion child : children(sequence)) {
            First current = inspect(child);
            result.caveats.addAll(current.caveats);
            if (result.nullable) {
                result.tokens.addAll(current.tokens);
                result.nullable = current.nullable;
            }
        }
        return result;
    }

    private First inspectChoice(Choice choice, First result) {
        for (Expansion child : children(choice)) {
            First current = inspect(child);
            result.add(current);
            result.nullable |= current.nullable;
        }
        return result;
    }

    private First inspectGroup(Expansion e, First f) {
        List<Expansion> cs = children(e);
        if (!cs.isEmpty()) {
            First c = inspect(cs.get(0));
            f.add(c);
            f.nullable = c.nullable;
            if (e instanceof ZeroOrOne || e instanceof ZeroOrMore) {
                f.nullable = true;
            }
            if (e instanceof TryBlock) {
                f.caveats.add("exception-recovery");
            }
        } else {
            f.caveats.add("opaque-" + e.getClass().getSimpleName());
        }
        return f;
    }

    void recordParents(Expansion e, Expansion parent) {
        if (e == null) {
            return;
        }
        parents.put(e, parent);
        for (Expansion child : children(e)) {
            recordParents(child, e);
        }
    }

    boolean propagate(Expansion e, Set<Integer> after) {
        if (e == null) {
            return false;
        }
        boolean changed = localFollows.computeIfAbsent(e, k -> new TreeSet<>()).addAll(after);
        if (!(e instanceof NonTerminal)) {
            if (e instanceof Sequence) {
                Set<Integer> next = new TreeSet<>(after);
                List<Expansion> cs = children(e);
                for (int i = cs.size() - 1; i >= 0; i--) {
                    Expansion c = cs.get(i);
                    changed |= propagate(c, next);
                    First cf = inspect(c);
                    Set<Integer> n = new TreeSet<>(cf.tokens);
                    if (cf.nullable) {
                        n.addAll(next);
                    }
                    next = n;
                }
            } else {
                Set<Integer> next = new TreeSet<>(after);
                if (e instanceof ZeroOrMore || e instanceof OneOrMore) {
                    next.addAll(inspect(e).tokens);
                }
                for (Expansion c : children(e)) {
                    changed |= propagate(c, next);
                }
            }
        }
        return changed;
    }

    boolean oneTokenOnly(Expansion e) {
        if (e instanceof RegularExpression) {
            RegularExpression token = (RegularExpression) e;
            return token.lhsTokens.isEmpty() && token.rhsToken == null;
        }
        if (e instanceof Sequence) {
            List<Expansion> real = new ArrayList<>();
            for (Expansion c : children(e)) {
                if (c instanceof Lookahead && !((Lookahead) c).isExplicit()
                        && ((Lookahead) c).getActionTokens().isEmpty()) {
                    continue;
                }
                real.add(c);
            }
            return real.size() == 1 && oneTokenOnly(real.get(0));
        }
        if (e instanceof Choice) {
            return !children(e).isEmpty() && children(e).stream().allMatch(this::oneTokenOnly);
        }
        return false;
    }

    List<String> tokenNames(Set<Integer> tokens) {
        List<String> names = new ArrayList<>();
        for (int n : tokens) {
            if (n == -1) {
                names.add("<UNKNOWN_EXTERNAL_CONTINUATION>");
                continue;
            }
            if (n == 0) {
                names.add("<EOF>");
                continue;
            }
            RegularExpression re = context.globals().rexps_of_tokens.get(n);
            if (re != null && !re.label.isEmpty()) {
                names.add("<" + re.label + ">");
            } else {
                if (re instanceof RStringLiteral) {
                    names.add("\"" + ((RStringLiteral) re).image + "\"");
                } else {
                    names.add("<TOKEN_" + n + ">");
                }
            }
        }
        return names;
    }

    void visit(Expansion e, String production, Set<Expansion> seen) {
        visit(e, production, seen, false);
    }

    private void visit(Expansion e, String production, Set<Expansion> seen, boolean inProbe) {
        if (e == null || !seen.add(e)) {
            return;
        }
        if (e instanceof Lookahead && ((Lookahead) e).isExplicit()) {
            recordLookahead((Lookahead) e, production, inProbe);
            visit(((Lookahead) e).getLaExpansion(), production, seen, true);
        }
        for (Expansion child : children(e)) {
            visit(child, production, seen, inProbe);
        }
    }

    private First competitors(Expansion decision, Expansion branch) {
        First result = new First();
        if (decision instanceof Choice) {
            Set<Expansion> selected = Collections.newSetFromMap(new IdentityHashMap<>());
            selected.add(branch);
            for (Expansion other : children(decision)) {
                if (!selected.contains(other)) {
                    addCompetitor(result, other, decision);
                }
            }
            return result;
        }
        if (decision instanceof ZeroOrOne || decision instanceof ZeroOrMore
                || decision instanceof OneOrMore) {
            result.tokens.addAll(localFollows.getOrDefault(decision, Set.of(-1)));
        } else {
            result.tokens.add(-1);
        }
        return result;
    }

    private void addCompetitor(First result, Expansion other, Expansion decision) {
        First competing = inspect(other);
        result.add(competing);
        if (competing.nullable) {
            result.tokens.addAll(localFollows.getOrDefault(decision, Set.of(-1)));
        }
    }

    private String triage(Lookahead la, Expansion decision, First branch, First competitors,
            Set<Integer> overlap, String semantic) {
        if (!semantic.isEmpty()) {
            return la.getAmount() == 0 ? "retain-state-or-policy" : "retain-combined-guard";
        }
        if (la.getAmount() == 0) {
            return "review-shared-prefix-or-unknown";
        }
        if (oneTokenOnly(la.getLaExpansion())) {
            return "review-exact-token-probe";
        }
        return branchTriage(la, decision, branch, competitors, overlap);
    }

    private String branchTriage(Lookahead la, Expansion decision, First branch, First competitors,
            Set<Integer> overlap) {
        if (!scansActualBranch(la) || !branch.caveats.isEmpty()
                || !competitors.caveats.isEmpty()) {
            return "review-shared-prefix-or-unknown";
        }
        if (la.getAmount() == defaultLookahead) {
            return "review-default-depth";
        }
        if (decision instanceof Choice && !branch.nullable && overlap.isEmpty()
                && !competitors.tokens.contains(-1)) {
            return "review-disjoint-FIRST1";
        }
        return "review-shared-prefix-or-unknown";
    }

    private boolean scansActualBranch(Lookahead lookahead) {
        Set<Expansion> branch = Collections.newSetFromMap(new IdentityHashMap<>());
        branch.add(parents.get(lookahead));
        return branch.contains(lookahead.getLaExpansion());
    }

    private void recordLookahead(Lookahead la, String production, boolean inProbe) {
        Expansion sequence = parents.get(la);
        Expansion decision = parents.get(sequence);
        First branch = inspect(sequence);
        First competing = competitors(decision, sequence);
        Set<Integer> overlap = new TreeSet<>(branch.tokens);
        overlap.retainAll(competing.tokens);
        String semantic = la.getActionTokens().stream().map(t -> t.image)
                .collect(java.util.stream.Collectors.joining(" "));
        Map<String, Object> row = new TreeMap<>();
        row.put("production", production);
        row.put("line", la.getLine());
        row.put("column", la.getColumn());
        row.put("amount", la.getAmount());
        row.put("semantic", semantic);
        row.put("inProbe", inProbe);
        row.put("tokenProbe", !inProbe && la.getAmount() > 0 && semantic.isEmpty()
                && oneTokenOnly(la.getLaExpansion()));
        row.put("triage", triage(la, decision, branch, competing, overlap, semantic));
        row.put("probeFirst", tokenNames(inspect(la.getLaExpansion()).tokens));
        row.put("decision", decision == null ? "unknown" : decision.getClass().getSimpleName());
        row.put("branchFirst", tokenNames(branch.tokens));
        row.put("nullable", branch.nullable);
        row.put("caveats", branch.caveats);
        row.put("competitorFirst", tokenNames(competing.tokens));
        row.put("competitorCaveats", competing.caveats);
        row.put("overlap", tokenNames(overlap));
        rows.add(row);
    }

    static String sha(String value) {
        return sha(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static String sha(byte[] value) {
        try {
            byte[] bytes = java.security.MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format(Locale.ROOT, "%02x", b));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String json(Object value) {
        if (value == null || value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?>) {
            List<String> parts = new ArrayList<>();
            ((Map<?, ?>) value).forEach((k, v) -> parts.add(json(k.toString()) + ":" + json(v)));
            return "{" + String.join(",", parts) + "}";
        }
        if (value instanceof Collection<?>) {
            List<String> parts = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                parts.add(json(item));
            }
            return "[" + String.join(",", parts) + "]";
        }
        StringBuilder result = new StringBuilder("\"");
        for (char ch : value.toString().toCharArray()) {
            if (ch == '"' || ch == '\\') {
                result.append('\\').append(ch);
            } else {
                if (ch < 32) {
                    result.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
                } else {
                    result.append(ch);
                }
            }
        }
        return result.append('"').toString();
    }
}
