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

/** Dependency components, in callee-before-caller order. */
final class LookaheadGraph {
    final Map<String, Set<String>> calls = new TreeMap<>();
    final Map<String, Set<String>> callers = new TreeMap<>();
    final List<Set<String>> components = new ArrayList<>();
    private final Map<String, Integer> indexes = new HashMap<>();
    private final Map<String, Integer> low = new HashMap<>();
    private final Deque<String> stack = new ArrayDeque<>();
    private final Set<String> active = new HashSet<>();

    LookaheadGraph(Map<String, Map<String, Object>> productions) {
        this(productions, "calls");
    }

    @SuppressWarnings("unchecked")
    LookaheadGraph(Map<String, Map<String, Object>> productions, String edgeKind) {
        productions.forEach((name, value) -> {
            calls.put(name, new TreeSet<>((Set<String>) value.get(edgeKind)));
            callers.put(name, new TreeSet<>());
        });
        calls.forEach((source, targets) -> targets.forEach(target -> {
            if (!callers.containsKey(target)) {
                throw new IllegalArgumentException("Unresolved production " + target);
            }
            callers.get(target).add(source);
        }));
        for (String name : calls.keySet()) {
            if (!indexes.containsKey(name)) {
                connect(name);
            }
        }
    }

    static Set<String> calls(Expansion expansion, boolean probes) {
        Set<String> result = new TreeSet<>();
        Set<Expansion> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Expansion> pending = new ArrayDeque<>();
        if (expansion != null) {
            pending.add(expansion);
        }
        while (!pending.isEmpty()) {
            Expansion current = pending.removeFirst();
            if (seen.add(current)) {
                if (current instanceof NonTerminal) {
                    result.add(((NonTerminal) current).getName());
                }
                if (probes && current instanceof Lookahead
                        && ((Lookahead) current).getLaExpansion() != null) {
                    pending.add(((Lookahead) current).getLaExpansion());
                }
                pending.addAll(LookaheadAnalysis.children(current));
            }
        }
        return result;
    }

    private void connect(String name) {
        int index = indexes.size();
        indexes.put(name, index);
        low.put(name, index);
        stack.push(name);
        active.add(name);
        for (String target : calls.get(name)) {
            if (!indexes.containsKey(target)) {
                connect(target);
                low.put(name, Math.min(low.get(name), low.get(target)));
            } else if (active.contains(target)) {
                low.put(name, Math.min(low.get(name), indexes.get(target)));
            }
        }
        if (low.get(name).equals(indexes.get(name))) {
            Set<String> component = new TreeSet<>();
            String member;
            do {
                member = stack.pop();
                active.remove(member);
                component.add(member);
            } while (!member.equals(name));
            components.add(component);
        }
    }

    static Set<String> closure(Collection<String> roots, Map<String, Set<String>> edges) {
        Set<String> result = new TreeSet<>();
        Deque<String> pending = new ArrayDeque<>(roots);
        while (!pending.isEmpty()) {
            String name = pending.removeFirst();
            if (result.add(name)) {
                pending.addAll(edges.getOrDefault(name, Set.of()));
            }
        }
        return result;
    }
}
