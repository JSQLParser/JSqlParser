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

/** Recomputes changed data-flow components from bottom; unchanged facts are immutable inputs. */
final class LookaheadIncremental {
    private final LookaheadSnapshot current;
    private final LookaheadSnapshot previous;
    private final LookaheadAnalysis facts;
    private final LookaheadGraph graph;
    private final Map<String, NormalProduction> definitions = new TreeMap<>();

    LookaheadIncremental(LookaheadSnapshot current, LookaheadSnapshot previous,
            LookaheadAnalysis facts) {
        this.current = current;
        this.previous = previous;
        this.facts = facts;
        graph = new LookaheadGraph(current.productions, "actualCalls");
        for (NormalProduction production : facts.context.globals().bnfproductions) {
            definitions.put(production.getLhs(), production);
        }
    }

    void compute() {
        current.statistics.put("mode", previous == null ? "cold" : "incremental");
        computeFirst();
        computeRules();
        computeFollows();
        computeRows();
    }

    private static String componentKey(Set<String> component) {
        return LookaheadAnalysis.json(component);
    }

    private Map<String, Object> firstDependencies(String production, Set<String> excluded) {
        Map<String, Object> result = new TreeMap<>();
        for (String name : graph.calls.get(production)) {
            if (!excluded.contains(name)) {
                result.put(name, facts.first.get(name).signature());
            }
        }
        return result;
    }

    private void computeFirst() {
        definitions.keySet().forEach(name -> facts.first.put(name,
                previous == null ? new LookaheadAnalysis.First()
                        : first(previous.firstFacts.get(name))));
        int recomputed = 0;
        int components = 0;
        for (Set<String> component : graph.components) {
            Map<String, Object> input = new TreeMap<>();
            for (String name : component) {
                input.put(name, List.of(current.productions.get(name).get("hash"),
                        firstDependencies(name, component)));
            }
            String key = componentKey(component);
            String signature = LookaheadAnalysis.sha(LookaheadAnalysis.json(input));
            current.firstInputs.put(key, signature);
            if (previous == null || !signature.equals(previous.firstInputs.get(key))) {
                component.forEach(name -> facts.first.put(name, new LookaheadAnalysis.First()));
                solveFirst(component);
                recomputed += component.size();
                components++;
            }
        }
        facts.first.forEach((name, value) -> current.firstFacts.put(name, firstValue(value)));
        current.statistics.put("firstProductionsRecomputed", recomputed);
        current.statistics.put("firstComponentsRecomputed", components);
        current.statistics.put("firstProductionsReused", definitions.size() - recomputed);
    }

    private void solveFirst(Set<String> component) {
        boolean changed;
        do {
            changed = false;
            for (String name : component) {
                NormalProduction production = definitions.get(name);
                LookaheadAnalysis.First value = facts.inspect(production.getExpansion());
                if (production instanceof BNFProduction
                        && !((BNFProduction) production).getDeclarationTokens().isEmpty()) {
                    value.caveats.add("java-declarations");
                }
                if (!value.signature().equals(facts.first.get(name).signature())) {
                    facts.first.put(name, value);
                    changed = true;
                }
            }
        } while (changed);
    }

    private static Map<String, Object> firstValue(LookaheadAnalysis.First first) {
        Map<String, Object> result = new TreeMap<>();
        result.put("tokens", first.tokens);
        result.put("nullable", first.nullable);
        result.put("caveats", first.caveats);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static LookaheadAnalysis.First first(Map<String, Object> value) {
        LookaheadAnalysis.First result = new LookaheadAnalysis.First();
        if (value != null) {
            result.tokens.addAll((Set<Integer>) value.get("tokens"));
            result.nullable = (Boolean) value.get("nullable");
            result.caveats.addAll((Set<String>) value.get("caveats"));
        }
        return result;
    }

    private void computeRules() {
        int recomputed = 0;
        for (String name : definitions.keySet()) {
            String signature = LookaheadAnalysis.sha(LookaheadAnalysis.json(List.of(
                    current.productions.get(name).get("hash"), firstDependencies(name, Set.of()))));
            current.ruleInputs.put(name, signature);
            if (previous != null && signature.equals(previous.ruleInputs.get(name))) {
                current.followRules.put(name, previous.followRules.get(name));
            } else {
                List<Map<String, Object>> rules = new ArrayList<>();
                collectRules(definitions.get(name).getExpansion(), new Suffix(), name, rules);
                current.followRules.put(name, rules);
                recomputed++;
            }
        }
        current.statistics.put("ruleProductionsRecomputed", recomputed);
    }

    private void collectRules(Expansion expansion, Suffix after, String caller,
            List<Map<String, Object>> rules) {
        if (expansion == null) {
            return;
        }
        if (expansion instanceof NonTerminal) {
            Map<String, Object> rule = new TreeMap<>();
            rule.put("caller", caller);
            rule.put("target", ((NonTerminal) expansion).getName());
            rule.put("tokens", after.tokens);
            rule.put("inherits", after.inherits);
            rule.put("suffix", after.shape);
            rule.put("calls", after.calls);
            rules.add(rule);
        } else if (expansion instanceof Sequence) {
            Suffix next = after;
            List<Expansion> children = LookaheadAnalysis.children(expansion);
            for (int i = children.size() - 1; i >= 0; i--) {
                Expansion child = children.get(i);
                collectRules(child, next, caller, rules);
                next = after(child, next, false);
            }
        } else {
            Suffix next = expansion instanceof OneOrMore || expansion instanceof ZeroOrMore
                    ? after(expansion, after, true)
                    : after;
            for (Expansion child : LookaheadAnalysis.children(expansion)) {
                collectRules(child, next, caller, rules);
            }
        }
    }

    private Suffix after(Expansion expansion, Suffix following, boolean loop) {
        LookaheadAnalysis.First first = facts.inspect(expansion);
        Suffix result = new Suffix();
        result.tokens.addAll(first.tokens);
        if (loop || first.nullable) {
            result.tokens.addAll(following.tokens);
        }
        result.inherits = (loop || first.nullable) && following.inherits;
        result.shape.add((loop ? "repeat:" : "")
                + LookaheadAnalysis.sha(LookaheadSnapshot.continuationShape(expansion)));
        result.shape.addAll(following.shape);
        result.calls.addAll(following.calls);
        result.calls.addAll(LookaheadGraph.calls(expansion, true));
        return result;
    }

    private static final class Suffix {
        private final Set<Integer> tokens = new TreeSet<>();
        private boolean inherits = true;
        private final List<String> shape = new ArrayList<>();
        private final Set<String> calls = new TreeSet<>();
    }

    private void computeFollows() {
        definitions.keySet().forEach(name -> facts.follows.put(name,
                previous == null ? new TreeSet<>(Set.of(-1))
                        : new TreeSet<>(previous.followFacts.getOrDefault(name, Set.of(-1)))));
        List<Set<String>> order = new ArrayList<>(graph.components);
        Collections.reverse(order);
        List<Map<String, Object>> all = new ArrayList<>();
        current.followRules.values().forEach(all::addAll);
        int recomputed = 0;
        for (Set<String> component : order) {
            List<Map<String, Object>> incoming = new ArrayList<>();
            List<String> inputs = new ArrayList<>();
            for (Map<String, Object> rule : all) {
                if (component.contains(rule.get("target"))) {
                    incoming.add(rule);
                    inputs.add(followInput(rule, component));
                }
            }
            Collections.sort(inputs);
            String key = componentKey(component);
            String signature = LookaheadAnalysis.sha(LookaheadAnalysis.json(inputs));
            current.followInputs.put(key, signature);
            if (previous == null || !signature.equals(previous.followInputs.get(key))) {
                component.forEach(name -> facts.follows.put(name, new TreeSet<>(Set.of(-1))));
                solveFollows(incoming);
                recomputed += component.size();
            }
        }
        facts.follows.forEach((name, value) -> current.followFacts.put(name, new TreeSet<>(value)));
        current.statistics.put("followProductionsRecomputed", recomputed);
        current.statistics.put("followProductionsReused", definitions.size() - recomputed);
    }

    private String followInput(Map<String, Object> rule, Set<String> component) {
        String caller = rule.get("caller").toString();
        return LookaheadAnalysis.json(List.of(caller, rule.get("target"), rule.get("tokens"),
                rule.get("inherits"), component.contains(caller) ? "internal"
                        : facts.follows.get(caller)));
    }

    @SuppressWarnings("unchecked")
    private void solveFollows(List<Map<String, Object>> incoming) {
        boolean changed;
        do {
            changed = false;
            for (Map<String, Object> rule : incoming) {
                Set<Integer> value = facts.follows.get(rule.get("target"));
                changed |= value.addAll((Set<Integer>) rule.get("tokens"));
                if (Boolean.TRUE.equals(rule.get("inherits"))) {
                    changed |= value.addAll(facts.follows.get(rule.get("caller")));
                }
            }
        } while (changed);
    }

    private void computeRows() {
        int recomputed = 0;
        int reused = 0;
        for (String name : definitions.keySet()) {
            String signature = LookaheadAnalysis.sha(LookaheadAnalysis.json(List.of(
                    current.productions.get(name).get("hash"), rowFirstDependencies(name),
                    facts.follows.get(name))));
            current.rowInputs.put(name, signature);
            if (previous != null && signature.equals(previous.rowInputs.get(name))) {
                reused += current.reuseRows(name, previous, facts.rows);
            } else {
                NormalProduction production = definitions.get(name);
                facts.propagate(production.getExpansion(), facts.follows.get(name));
                int before = facts.rows.size();
                facts.visit(production.getExpansion(), name,
                        Collections.newSetFromMap(new IdentityHashMap<>()));
                recomputed += facts.rows.size() - before;
            }
        }
        current.statistics.put("sitesRecomputed", recomputed);
        current.statistics.put("sitesReused", reused);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> rowFirstDependencies(String name) {
        Map<String, Object> result = new TreeMap<>();
        for (String target : (Set<String>) current.productions.get(name).get("calls")) {
            result.put(target, facts.first.get(target).signature());
        }
        return result;
    }
}
