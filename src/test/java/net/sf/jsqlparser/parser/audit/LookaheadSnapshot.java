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

/** Validated syntax plus versioned lexical facts; Java effects remain conservative. */
final class LookaheadSnapshot {
    final Map<String, Map<String, Object>> productions = new TreeMap<>();
    final List<Map<String, Object>> sites = new ArrayList<>();
    final Map<String, String> anchors = new HashMap<>();
    final Map<String, List<Map<String, Object>>> positions = new TreeMap<>();
    final Map<String, Map<String, Object>> firstFacts = new TreeMap<>();
    final Map<String, Set<Integer>> followFacts = new TreeMap<>();
    final Map<String, List<Map<String, Object>>> followRules = new TreeMap<>();
    final Map<String, String> firstInputs = new TreeMap<>();
    final Map<String, String> ruleInputs = new TreeMap<>();
    final Map<String, String> followInputs = new TreeMap<>();
    final Map<String, String> rowInputs = new TreeMap<>();
    final Map<String, Object> statistics = new TreeMap<>();
    final Map<String, Map<String, Object>> dependencyCache = new TreeMap<>();
    final Map<String, String> dependencyDigests = new TreeMap<>();
    String globalHash;
    String opaqueHash;
    String grammarHash;
    String externalHash;
    String engineHash;
    boolean pureGrammar;

    static LookaheadSnapshot parse(String grammar, String external, String engine)
            throws Exception {
        return parse(grammar, external, engine, null);
    }

    static LookaheadSnapshot parse(String grammar, String external, String engine,
            LookaheadSnapshot previous) throws Exception {
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
        LookaheadLexer lexer = new LookaheadLexer(context, first);
        snapshot.globalHash = LookaheadAnalysis.sha(globalFingerprint(first, owned) + "\n"
                + LookaheadAnalysis.json(lexer.namespace));
        snapshot.statistics.put("lexicalNamespaceFallback", lexer.fallback);
        snapshot.opaqueHash = LookaheadAnalysis.sha(String.join("\n", opaque));
        snapshot.grammarHash = LookaheadAnalysis.sha(LookaheadAnalysis.json(snapshot.productions));
        snapshot.pureGrammar = LookaheadPurity.isPure(context);
        LookaheadSnapshot usable = previous != null
                && previous.globalHash.equals(snapshot.globalHash)
                && previous.engineHash.equals(engine) ? previous : null;
        new LookaheadIncremental(snapshot, usable, facts).compute();
        snapshot.statistics.put("javaCCParses", 1);
        snapshot.statistics.put("parsedProductions", snapshot.productions.size());
        if (previous != null && usable == null) {
            snapshot.statistics.put("fallback", "lexer-options-or-engine-changed");
        }
        snapshot.recordSites(facts.rows, grammar.startsWith("/*@bgen"));
        snapshot.prepareDependencies(usable);
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
        code.add("access:" + production.getAccessMod());
        addCode(code, "return-type", production.getReturnTypeTokens());
        addCode(code, "parameters", production.getParameterListTokens());
        for (List<Token> exception : production.getThrowsList()) {
            addCode(code, "throws", exception);
        }
        walk(production.getExpansion(), "", production.getLhs(), null,
                Collections.newSetFromMap(new IdentityHashMap<>()), calls, code);
        if (!code.isEmpty()) {
            opaque.add(production.getLhs() + ":" + LookaheadAnalysis.sha(String.join("\n", code)));
        }
        Map<String, Object> value = new TreeMap<>();
        value.put("hash", LookaheadAnalysis.sha(String.join(" ", body)));
        value.put("calls", calls);
        value.put("actualCalls", LookaheadGraph.calls(production.getExpansion(), false));
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

    static String shape(Expansion expansion) {
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

    static String continuationShape(Expansion expansion) {
        return continuationShape(expansion, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String continuationShape(Expansion expansion, Set<Expansion> path) {
        if (expansion == null) {
            return "unknown";
        }
        if (!path.add(expansion)) {
            return "actual-branch";
        }
        try {
            if (expansion instanceof Lookahead) {
                Lookahead lookahead = (Lookahead) expansion;
                return lookahead.isExplicit() ? "lookahead:" + lookahead.getAmount() + ":"
                        + tokens(lookahead.getActionTokens()) + ":"
                        + continuationShape(lookahead.getLaExpansion(), path) : "";
            }
            if (expansion instanceof RegularExpression || expansion instanceof NonTerminal) {
                return shape(expansion);
            }
            if (expansion instanceof Action) {
                return "action:" + tokens(((Action) expansion).getActionTokens());
            }
            List<String> children = new ArrayList<>();
            for (Expansion child : LookaheadAnalysis.children(expansion)) {
                children.add(continuationShape(child, path));
            }
            return expansion.getClass().getSimpleName() + LookaheadAnalysis.json(children);
        } finally {
            path.remove(expansion);
        }
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
            addCode(code, "assignment", nt.getLhsTokens());
            addCode(code, "type-arguments", nt.getParametrizedTypeTokens());
            addCode(code, "arguments:" + nt.getName(), nt.getArgumentTokens());
        }
        if (expansion instanceof RegularExpression) {
            RegularExpression token = (RegularExpression) expansion;
            addCode(code, "assignment", token.lhsTokens);
            if (token.rhsToken != null) {
                addCode(code, "token-field", List.of(token.rhsToken));
            }
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
        Map<String, Object> position = new TreeMap<>();
        position.put("line", la.getLine());
        position.put("column", la.getColumn());
        positions.computeIfAbsent(anchor, k -> new ArrayList<>()).add(position);
    }

    int reuseRows(String production, LookaheadSnapshot previous, List<Map<String, Object>> rows) {
        Map<String, Integer> indexes = new HashMap<>();
        int reused = 0;
        for (Map<String, Object> old : previous.sites) {
            if (old.get("production").equals(production)) {
                String anchor = old.get("anchor").toString();
                int index = indexes.getOrDefault(anchor, 0);
                Map<String, Object> row = new TreeMap<>(old);
                row.remove("anchor");
                row.putAll(positions.get(anchor).get(index));
                indexes.put(anchor, index + 1);
                rows.add(row);
                reused++;
            }
        }
        return reused;
    }

    Map<String, Object> cacheValue() {
        Map<String, Object> value = new TreeMap<>();
        value.put("productions", productions);
        value.put("sites", sites);
        value.put("anchors", anchors);
        value.put("positions", positions);
        value.put("firstFacts", firstFacts);
        value.put("followFacts", followFacts);
        value.put("followRules", followRules);
        value.put("firstInputs", firstInputs);
        value.put("ruleInputs", ruleInputs);
        value.put("followInputs", followInputs);
        value.put("rowInputs", rowInputs);
        value.put("dependencies", dependencyCache);
        value.put("dependencyDigests", dependencyDigests);
        value.put("global", globalHash);
        value.put("opaque", opaqueHash);
        value.put("grammar", grammarHash);
        value.put("engine", engineHash);
        value.put("pure", pureGrammar);
        return value;
    }

    @SuppressWarnings("unchecked")
    static LookaheadSnapshot fromCache(Object value) {
        Map<String, Object> data = (Map<String, Object>) value;
        LookaheadSnapshot result = new LookaheadSnapshot();
        result.productions.putAll((Map<String, Map<String, Object>>) data.get("productions"));
        result.sites.addAll((List<Map<String, Object>>) data.get("sites"));
        result.anchors.putAll((Map<String, String>) data.get("anchors"));
        result.positions.putAll((Map<String, List<Map<String, Object>>>) data.get("positions"));
        result.firstFacts.putAll((Map<String, Map<String, Object>>) data.get("firstFacts"));
        result.followFacts.putAll((Map<String, Set<Integer>>) data.get("followFacts"));
        result.followRules.putAll((Map<String, List<Map<String, Object>>>) data.get("followRules"));
        result.firstInputs.putAll((Map<String, String>) data.get("firstInputs"));
        result.ruleInputs.putAll((Map<String, String>) data.get("ruleInputs"));
        result.followInputs.putAll((Map<String, String>) data.get("followInputs"));
        result.rowInputs.putAll((Map<String, String>) data.get("rowInputs"));
        result.dependencyCache.putAll(
                (Map<String, Map<String, Object>>) data.get("dependencies"));
        result.dependencyDigests.putAll((Map<String, String>) data.get("dependencyDigests"));
        result.globalHash = Objects.requireNonNull((String) data.get("global"));
        result.opaqueHash = Objects.requireNonNull((String) data.get("opaque"));
        result.grammarHash = Objects.requireNonNull((String) data.get("grammar"));
        result.engineHash = Objects.requireNonNull((String) data.get("engine"));
        result.pureGrammar = (Boolean) data.get("pure");
        return result;
    }

    private static String location(Map<String, Object> row) {
        return row.get("production") + ":" + row.get("line") + ":" + row.get("column");
    }

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> contexts() {
        Map<String, List<Map<String, Object>>> result = new TreeMap<>();
        for (Map<String, Object> site : sites) {
            String production = site.get("production").toString();
            Map<String, Object> roots = dependencyCache.get(production);
            Map<String, Object> facts = new TreeMap<>(site);
            facts.keySet().removeAll(Set.of("line", "column", "anchor"));
            Map<String, Object> local = new TreeMap<>();
            local.put("facts", LookaheadAnalysis.sha(LookaheadAnalysis.json(facts)));
            local.put("dependencies", dependencyDigests.get(production));
            String localFingerprint = LookaheadAnalysis.sha(LookaheadAnalysis.json(local));
            Map<String, Object> context = new TreeMap<>();
            context.put("local", localFingerprint);
            context.put("global", globalHash);
            context.put("opaque", pureGrammar ? "effect-free-grammar" : opaqueHash);
            context.put("grammar", pureGrammar ? "dependent-productions" : grammarHash);
            context.put("external", externalHash);
            context.put("engine", engineHash);
            Map<String, Object> binding = new TreeMap<>();
            binding.put("production", site.get("production"));
            binding.put("line", site.get("line"));
            binding.put("triage", site.get("triage"));
            binding.put("facts", facts);
            binding.put("dependencies", roots);
            binding.put("localFingerprint", localFingerprint);
            binding.put("fingerprint", LookaheadAnalysis.sha(LookaheadAnalysis.json(context)));
            if (Boolean.TRUE.equals(site.get("tokenProbe"))) {
                Map<String, Object> certificate = new TreeMap<>();
                certificate.put("scope", "token-probe-v1");
                certificate.put("amount", site.get("amount"));
                certificate.put("tokens", site.get("probeFirst"));
                certificate.put("global", globalHash);
                certificate.put("external", externalHash);
                certificate.put("engine", engineHash);
                binding.put("tokenFingerprint",
                        LookaheadAnalysis.sha(LookaheadAnalysis.json(certificate)));
            }
            result.computeIfAbsent(site.get("anchor").toString(), k -> new ArrayList<>())
                    .add(binding);
        }
        return result;
    }

    private void prepareDependencies(LookaheadSnapshot previous) {
        LookaheadGraph graph = new LookaheadGraph(productions);
        Map<String, Set<String>> actualCallers =
                new LookaheadGraph(productions, "actualCalls").callers;
        Map<String, List<Map<String, Object>>> incoming = new TreeMap<>();
        productions.keySet().forEach(name -> incoming.put(name, new ArrayList<>()));
        followRules.values().forEach(rules -> rules.forEach(
                rule -> incoming.get(rule.get("target")).add(rule)));
        Map<String, String> continuationDigests = new TreeMap<>();
        incoming.forEach((name, rules) -> {
            List<String> descriptions = new ArrayList<>();
            for (Map<String, Object> rule : rules) {
                descriptions.add(LookaheadAnalysis.json(List.of(rule.get("caller"),
                        rule.get("suffix"), rule.get("calls"))));
            }
            Collections.sort(descriptions);
            continuationDigests.put(name,
                    LookaheadAnalysis.sha(LookaheadAnalysis.json(descriptions)));
        });
        int recomputed = 0;
        Set<String> owners = new TreeSet<>();
        sites.forEach(site -> owners.add(site.get("production").toString()));
        for (String owner : owners) {
            Map<String, Object> old = previous == null ? null : previous.dependencyCache.get(owner);
            if (old != null && dependenciesMatch(old, continuationDigests)) {
                dependencyCache.put(owner, old);
                dependencyDigests.put(owner, previous.dependencyDigests.get(owner));
            } else {
                Map<String, Object> value =
                        dependencyHashes(owner, graph, actualCallers, continuationDigests);
                dependencyCache.put(owner, value);
                dependencyDigests.put(owner, LookaheadAnalysis.sha(LookaheadAnalysis.json(value)));
                recomputed++;
            }
        }
        statistics.put("dependencyContextsRecomputed", recomputed);
        statistics.put("dependencyContextsReused", owners.size() - recomputed);
    }

    private boolean dependenciesMatch(Map<String, Object> old,
            Map<String, String> continuations) {
        for (Map.Entry<String, Object> entry : old.entrySet()) {
            String name = entry.getKey();
            Object current = name.startsWith("caller-continuations:")
                    ? continuations.get(name.substring("caller-continuations:".length()))
                    : productions.getOrDefault(name, Map.of()).get("hash");
            if (!entry.getValue().equals(current)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> dependencyHashes(String production, LookaheadGraph graph,
            Map<String, Set<String>> actualCallers,
            Map<String, String> continuationDigests) {
        // Prefix probes and competing alternatives can restrict which inputs reach this call.
        Set<String> ancestors = LookaheadGraph.closure(Set.of(production), actualCallers);
        Set<String> needed = LookaheadGraph.closure(ancestors, graph.calls);
        Map<String, Object> result = new TreeMap<>();
        for (String name : needed) {
            if (!productions.containsKey(name)) {
                throw new IllegalStateException("Unresolved production " + name);
            }
            result.put(name, productions.get(name).get("hash"));
        }
        ancestors.forEach(target -> result.put("caller-continuations:" + target,
                continuationDigests.get(target)));
        return result;
    }

    static Map<String, Object> compare(LookaheadSnapshot before, LookaheadSnapshot after,
            Map<String, String> evidence) {
        return compare(before, after, evidence, Map.of());
    }

    static Map<String, Object> compare(LookaheadSnapshot before, LookaheadSnapshot after,
            Map<String, String> evidence, Map<String, String> scopes) {
        if (!Set.of("context", "token-probe").containsAll(scopes.values())) {
            throw new IllegalArgumentException("Unknown evidence scope");
        }
        Comparison comparison = new Comparison(before.contexts(), after.contexts(), evidence,
                scopes);
        comparison.collect();
        return comparison.report(before.sites.size(), after.sites.size(),
                globalChanges(before, after));
    }

    private static List<String> globalChanges(LookaheadSnapshot before, LookaheadSnapshot after) {
        List<String> globals = new ArrayList<>();
        if (!before.globalHash.equals(after.globalHash)) {
            globals.add("parser-options-helpers-or-lexer");
        }
        boolean pure = before.pureGrammar && after.pureGrammar;
        if (!pure && !before.opaqueHash.equals(after.opaqueHash)) {
            globals.add("opaque-Java-or-predicate");
        }
        if (!pure && !before.grammarHash.equals(after.grammarHash)) {
            globals.add("grammar-visible-to-opaque-Java");
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
        private final Map<String, String> scopes;
        private final List<Map<String, Object>> changed = new ArrayList<>();
        private final List<String> reused = new ArrayList<>();
        private final List<String> contextReused = new ArrayList<>();
        private final List<String> tokensReused = new ArrayList<>();
        private final List<String> stale = new ArrayList<>();
        private final List<String> ambiguous = new ArrayList<>();
        private final Set<String> direct = new TreeSet<>();
        private final Set<String> roots = new TreeSet<>();

        private Comparison(Map<String, List<Map<String, Object>>> old,
                Map<String, List<Map<String, Object>>> current, Map<String, String> evidence,
                Map<String, String> scopes) {
            this.old = old;
            this.current = current;
            this.evidence = evidence;
            this.scopes = scopes;
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
            boolean token = scopes.getOrDefault(key, "context").equals("token-probe");
            if (bindings.size() == 1
                    && evidence.get(key).equals(bindings.get(0)
                            .get(token ? "tokenFingerprint" : "fingerprint"))) {
                reused.add(key);
                (token ? tokensReused : contextReused).add(key);
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
            report.put("contextEvidenceReused", contextReused);
            report.put("tokenCertificatesReused", tokensReused);
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
