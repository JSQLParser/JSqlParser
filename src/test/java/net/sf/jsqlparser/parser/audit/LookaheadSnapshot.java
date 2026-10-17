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

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import org.javacc.parser.*;

/** Fresh snapshots deliberately avoid cached FIRST/FOLLOW or Java dependency inferences. */
final class LookaheadSnapshot {
    final Map<String, Map<String, Object>> productions = new TreeMap<>();
    final List<Map<String, Object>> sites = new ArrayList<>();
    final Map<String, String> anchors = new HashMap<>();
    String globalHash;
    String opaqueHash;
    String externalHash;
    String engineHash;

    static LookaheadSnapshot parse(String grammar, String external, String engine)
            throws Exception {
        Context context = new Context();
        JavaCCParser parser = new JavaCCParser(new StringReader(grammar));
        Token first = parser.getToken(1);
        parser.javacc_input(context);
        Semanticize.start(context);
        if (context.errors().get_error_count() != 0) {
            throw new IllegalArgumentException("Invalid grammar");
        }
        LookaheadSnapshot snapshot = new LookaheadSnapshot();
        snapshot.externalHash = external;
        snapshot.engineHash = engine;
        Set<Token> owned = Collections.newSetFromMap(new IdentityHashMap<>());
        List<String> opaque = new ArrayList<>();
        LookaheadAnalysis facts = new LookaheadAnalysis(context);
        for (NormalProduction production : context.globals().bnfproductions) {
            snapshot.recordProduction(production, owned, opaque, facts);
        }
        Collections.sort(opaque);
        snapshot.globalHash = globalFingerprint(first, owned);
        snapshot.opaqueHash = LookaheadAnalysis.sha(String.join("\n", opaque));
        facts.computeFirst();
        facts.computeFollows();
        for (NormalProduction production : context.globals().bnfproductions) {
            facts.visit(production.getExpansion(), production.getLhs(),
                    Collections.newSetFromMap(new IdentityHashMap<>()));
        }
        snapshot.recordSites(facts.rows, grammar.startsWith("/*@bgen"));
        return snapshot;
    }

    private static List<String> productionBody(NormalProduction production, Set<Token> owned) {
        List<String> body = new ArrayList<>();
        Token token = production.getFirstToken();
        if (token == null || production.getLastToken() == null) {
            throw new IllegalStateException("Missing production bounds");
        }
        Set<Token> end = Collections.newSetFromMap(new IdentityHashMap<>());
        end.add(production.getLastToken());
        while (true) {
            owned.add(token);
            body.add(token.image);
            if (end.contains(token)) {
                return body;
            }
            token = token.next;
            if (token == null) {
                throw new IllegalStateException("Unclosed production");
            }
        }
    }

    private void recordProduction(NormalProduction production, Set<Token> owned,
            List<String> opaque, LookaheadAnalysis facts) {
        List<String> body = productionBody(production, owned);
        Set<String> calls = new TreeSet<>();
        List<String> code = new ArrayList<>();
        if (production instanceof BNFProduction) {
            addCode(code, "declarations", ((BNFProduction) production).getDeclarationTokens());
        } else {
            code.add("opaque-production:" + String.join(" ", body));
        }
        addCode(code, "parameters", production.getParameterListTokens());
        walk(production.getExpansion(), "", production.getLhs(), null,
                Collections.newSetFromMap(new IdentityHashMap<>()), calls, code);
        if (!code.isEmpty()) {
            opaque.add(production.getLhs() + ":" + LookaheadAnalysis.sha(String.join("\n", code)));
        }
        Map<String, Object> value = new TreeMap<>();
        value.put("hash", LookaheadAnalysis.sha(String.join(" ", body)));
        value.put("calls", calls);
        value.put("nullable", production.getExpansion() == null ? "unknown"
                : Semanticize.emptyExpansionExists(production.getExpansion()));
        productions.put(production.getLhs(), value);
        facts.recordParents(production.getExpansion(), null);
    }

    private static String globalFingerprint(Token first, Set<Token> owned) {
        List<String> globals = new ArrayList<>();
        for (Token token = first; token != null && token.kind != 0; token = token.next) {
            if (!owned.contains(token)) {
                globals.add(token.image);
            }
        }
        return LookaheadAnalysis.sha(String.join(" ", globals));
    }

    private void recordSites(List<Map<String, Object>> rows, boolean generated) {
        for (Map<String, Object> row : rows) {
            String anchor = anchors.get(location(row));
            if (anchor == null) {
                throw new IllegalStateException("Unmapped explicit lookahead " + row);
            }
            row.put("anchor", anchor);
            if (generated) {
                row.put("line", ((Integer) row.get("line")) - 1);
            }
            sites.add(row);
        }
        int siteCount = sites.size();
        int anchorCount = anchors.size();
        if (siteCount != anchorCount) {
            throw new IllegalStateException("Lookahead count mismatch");
        }
    }

    private static String tokens(List<Token> tokens) {
        return tokens.stream().map(t -> t.image).collect(Collectors.joining(" "));
    }

    private static void addCode(List<String> target, String kind, List<Token> tokens) {
        if (!tokens.isEmpty()) {
            target.add(kind + ":" + tokens(tokens));
        }
    }

    private static String shape(Expansion expansion) {
        if (expansion instanceof Lookahead) {
            return "";
        }
        if (expansion instanceof RegularExpression) {
            RegularExpression re = (RegularExpression) expansion;
            if (!re.label.isEmpty()) {
                return "T:" + re.label;
            }
            return re instanceof RStringLiteral
                    ? "L:" + LookaheadAnalysis.json(((RStringLiteral) re).image)
                    : "R:" + re.getClass().getSimpleName();
        }
        if (expansion instanceof NonTerminal) {
            NonTerminal nt = (NonTerminal) expansion;
            return "N:" + nt.getName() + "(" + tokens(nt.getArgumentTokens()) + ")";
        }
        if (expansion instanceof Action) {
            return "Action";
        }
        return expansion.getClass().getSimpleName() + LookaheadAnalysis.children(expansion).stream()
                .map(LookaheadSnapshot::shape).filter(s -> !s.isEmpty())
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void walk(Expansion expansion, String path, String name, Expansion parent,
            Set<Expansion> seen, Set<String> calls, List<String> code) {
        if (expansion == null || !seen.add(expansion)) {
            return;
        }
        recordExpansionCode(expansion, calls, code);
        if (expansion instanceof Lookahead) {
            Lookahead la = (Lookahead) expansion;
            recordLookahead(la, path, name, parent, code);
            walk(la.getLaExpansion(), path + "/probe", name, expansion, seen, calls, code);
        }
        for (Expansion child : LookaheadAnalysis.children(expansion)) {
            walk(child, path + "/" + expansion.getClass().getSimpleName(), name, expansion, seen,
                    calls, code);
        }
    }

    private static void recordExpansionCode(Expansion expansion, Set<String> calls,
            List<String> code) {
        if (expansion instanceof NonTerminal) {
            NonTerminal nt = (NonTerminal) expansion;
            calls.add(nt.getName());
            addCode(code, "arguments:" + nt.getName(), nt.getArgumentTokens());
        }
        if (expansion instanceof Action) {
            addCode(code, "action", ((Action) expansion).getActionTokens());
        }
        if (expansion instanceof TryBlock) {
            recordRecoveryCode((TryBlock) expansion, code);
        }
    }

    private static void recordRecoveryCode(TryBlock block, List<String> code) {
        for (List<Token> type : block.types) {
            addCode(code, "catch-type", type);
        }
        for (List<Token> body : block.catchblks) {
            addCode(code, "catch-body", body);
        }
        if (block.finallyblk != null) {
            addCode(code, "finally-body", block.finallyblk);
        }
    }

    private void recordLookahead(Lookahead la, String path, String name, Expansion parent,
            List<String> code) {
        if (!la.isExplicit()) {
            return;
        }
        addCode(code, "predicate", la.getActionTokens());
        String key = name + ":" + la.getLine() + ":" + la.getColumn();
        String anchor = name + ":"
                + LookaheadAnalysis.sha(path + "|" + shape(parent)).substring(0, 16);
        if (anchors.put(key, anchor) != null) {
            throw new IllegalStateException("Duplicate lookahead location");
        }
    }

    private static String location(Map<String, Object> row) {
        return row.get("production") + ":" + row.get("line") + ":" + row.get("column");
    }

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> contexts() {
        Map<String, List<Map<String, Object>>> result = new TreeMap<>();
        for (Map<String, Object> site : sites) {
            Set<String> closure = new TreeSet<>();
            Deque<String> pending = new ArrayDeque<>();
            pending.add(site.get("production").toString());
            while (!pending.isEmpty()) {
                String name = pending.removeFirst();
                if (!closure.add(name)) {
                    continue;
                }
                if (!productions.containsKey(name)) {
                    throw new IllegalStateException("Unresolved production " + name);
                }
                pending.addAll((Set<String>) productions.get(name).get("calls"));
            }
            Map<String, Object> facts = new TreeMap<>(site);
            facts.keySet().removeAll(Set.of("line", "column", "anchor"));
            Map<String, Object> dependencies = new TreeMap<>();
            for (String name : closure) {
                dependencies.put(name, productions.get(name));
            }
            Map<String, Object> local = new TreeMap<>();
            local.put("facts", facts);
            local.put("dependencies", dependencies);
            Map<String, Object> context = new TreeMap<>(local);
            context.put("global", globalHash);
            context.put("opaque", opaqueHash);
            context.put("external", externalHash);
            context.put("engine", engineHash);
            Map<String, Object> binding = new TreeMap<>();
            binding.put("production", site.get("production"));
            binding.put("line", site.get("line"));
            binding.put("triage", site.get("triage"));
            binding.put("facts", facts);
            binding.put("dependencies", dependencies);
            binding.put("localFingerprint", LookaheadAnalysis.sha(LookaheadAnalysis.json(local)));
            binding.put("fingerprint", LookaheadAnalysis.sha(LookaheadAnalysis.json(context)));
            result.computeIfAbsent(site.get("anchor").toString(), k -> new ArrayList<>())
                    .add(binding);
        }
        return result;
    }

    static Map<String, Object> compare(LookaheadSnapshot before, LookaheadSnapshot after,
            Map<String, String> evidence) {
        Comparison comparison = new Comparison(before.contexts(), after.contexts(), evidence);
        comparison.collect();
        return comparison.report(before.sites.size(), after.sites.size(),
                globalChanges(before, after));
    }

    private static List<String> globalChanges(LookaheadSnapshot before, LookaheadSnapshot after) {
        List<String> globals = new ArrayList<>();
        if (!before.globalHash.equals(after.globalHash)) {
            globals.add("parser-options-helpers-or-lexer");
        }
        if (!before.opaqueHash.equals(after.opaqueHash)) {
            globals.add("opaque-Java-or-predicate");
        }
        if (!before.externalHash.equals(after.externalHash)) {
            globals.add("production-Java-resources-or-build");
        }
        if (!before.engineHash.equals(after.engineHash)) {
            globals.add("generator-or-analysis-version");
        }
        return globals;
    }

    private static final class Comparison {
        private final Map<String, List<Map<String, Object>>> old;
        private final Map<String, List<Map<String, Object>>> current;
        private final Map<String, String> evidence;
        private final List<Map<String, Object>> changed = new ArrayList<>();
        private final List<String> reused = new ArrayList<>();
        private final List<String> stale = new ArrayList<>();
        private final List<String> ambiguous = new ArrayList<>();
        private final Set<String> direct = new TreeSet<>();
        private final Set<String> roots = new TreeSet<>();

        private Comparison(Map<String, List<Map<String, Object>>> old,
                Map<String, List<Map<String, Object>>> current, Map<String, String> evidence) {
            this.old = old;
            this.current = current;
            this.evidence = evidence;
        }

        private void collect() {
            Set<String> keys = new TreeSet<>(old.keySet());
            keys.addAll(current.keySet());
            keys.addAll(evidence.keySet());
            for (String key : keys) {
                List<Map<String, Object>> before = old.getOrDefault(key, List.of());
                List<Map<String, Object>> after = current.getOrDefault(key, List.of());
                String kind = changeKind(key, before, after);
                if (kind != null) {
                    recordFinding(key, kind, before, after);
                }
                if (evidence.containsKey(key)) {
                    recordEvidence(key, after);
                }
            }
        }

        private String changeKind(String key, List<Map<String, Object>> before,
                List<Map<String, Object>> after) {
            if (before.isEmpty() && after.isEmpty()) {
                return "unbound-evidence";
            }
            if (before.size() > 1 || after.size() > 1) {
                ambiguous.add(key);
                return localFingerprints(before).equals(localFingerprints(after)) ? null
                        : "ambiguous-context-changed";
            }
            if (before.isEmpty()) {
                return "added";
            }
            if (after.isEmpty()) {
                return "removed";
            }
            return before.get(0).get("localFingerprint")
                    .equals(after.get(0).get("localFingerprint"))
                            ? null
                            : "context-changed";
        }

        private static List<String> localFingerprints(List<Map<String, Object>> bindings) {
            return bindings.stream().map(x -> x.get("localFingerprint").toString()).sorted()
                    .collect(Collectors.toList());
        }

        private void recordFinding(String key, String kind, List<Map<String, Object>> before,
                List<Map<String, Object>> after) {
            Map<String, Object> finding = new TreeMap<>();
            finding.put("anchor", key);
            finding.put("kind", kind);
            finding.put("beforeLines",
                    before.stream().map(x -> x.get("line")).collect(Collectors.toList()));
            finding.put("afterLines",
                    after.stream().map(x -> x.get("line")).collect(Collectors.toList()));
            List<Map<String, Object>> available = after.isEmpty() ? before : after;
            if (!available.isEmpty()) {
                finding.put("triage", available.get(0).get("triage"));
            }
            if (before.size() == 1 && after.size() == 1) {
                recordFacts(key, finding, before.get(0), after.get(0));
            }
            if (kind.equals("added") || kind.equals("removed")) {
                direct.add(key);
            }
            changed.add(finding);
        }

        @SuppressWarnings("unchecked")
        private void recordFacts(String key, Map<String, Object> finding,
                Map<String, Object> before,
                Map<String, Object> after) {
            boolean factChange = !before.get("facts").equals(after.get("facts"));
            finding.put("localFactsChanged", factChange);
            if (factChange) {
                direct.add(key);
            }
            Map<String, Object> oldDependencies = (Map<String, Object>) before.get("dependencies");
            Map<String, Object> newDependencies = (Map<String, Object>) after.get("dependencies");
            Set<String> names = new TreeSet<>(oldDependencies.keySet());
            names.addAll(newDependencies.keySet());
            List<String> differences = names.stream()
                    .filter(n -> !Objects.equals(oldDependencies.get(n), newDependencies.get(n)))
                    .collect(Collectors.toList());
            roots.addAll(differences);
            finding.put("dependencyChanges", differences);
        }

        private void recordEvidence(String key, List<Map<String, Object>> bindings) {
            if (bindings.size() == 1
                    && evidence.get(key).equals(bindings.get(0).get("fingerprint"))) {
                reused.add(key);
            } else {
                stale.add(key);
            }
        }

        private Map<String, Object> report(int beforeCount, int afterCount, List<String> globals) {
            Map<String, Object> report = new TreeMap<>();
            report.put("needsReview", !changed.isEmpty() || !stale.isEmpty() || !globals.isEmpty());
            report.put("changes", changed);
            report.put("directDecisions", direct);
            report.put("dependencyRoots", roots);
            report.put("reusableEvidence", reused);
            report.put("invalidatedEvidence", stale);
            report.put("globalSignals", globals);
            report.put("ambiguousAnchors", ambiguous);
            report.put("beforeSites", beforeCount);
            report.put("afterSites", afterCount);
            report.put("automaticGrammarChanges", 0);
            return report;
        }
    }
}
