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

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.io.TempDir;

class LookaheadIncrementalTest {
    private static final String HEADER = "options { STATIC=false; }\n"
            + "PARSER_BEGIN(Fixture) public class Fixture {} PARSER_END(Fixture)\n"
            + "TOKEN: { <A: \"a\"> | <B: \"b\"> | <C: \"c\"> | <D: \"d\"> }\n";
    @TempDir
    Path directory;

    private static LookaheadSnapshot parse(String grammar) throws Exception {
        return LookaheadSnapshot.parse(grammar, "external", "engine");
    }

    private static Map<String, String> binding(LookaheadSnapshot snapshot, String production,
            String fingerprint) {
        return snapshot.contexts().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(production + ":"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().get(0).get(fingerprint).toString()));
    }

    private static LookaheadSnapshot update(LookaheadSnapshot before, String grammar)
            throws Exception {
        LookaheadSnapshot incremental =
                LookaheadSnapshot.parse(grammar, "external", "engine", before);
        LookaheadSnapshot full = parse(grammar);
        assertEquals(full.firstFacts, incremental.firstFacts, "FIRST must equal a cold solve");
        assertEquals(full.followFacts, incremental.followFacts, "FOLLOW must equal a cold solve");
        assertEquals(full.sites, incremental.sites, "site facts and locations must agree");
        assertEquals(full.contexts(), incremental.contexts(), "bindings must agree");
        return incremental;
    }

    @Test
    void unrelatedPureProductionPreservesFullContextEvidence() throws Exception {
        String grammar = HEADER + "void Entry():{} { (LOOKAHEAD(2) <A><B> | <C>) <EOF> }\n"
                + "void Other():{} { <C> }";
        LookaheadSnapshot before = parse(grammar);
        assertTrue(before.pureGrammar);
        LookaheadSnapshot after = update(before,
                grammar.replace("void Other():{} { <C>", "void Other():{} { <B>"));
        assertEquals(1, after.statistics.get("firstProductionsRecomputed"));
        assertEquals(0, after.statistics.get("followProductionsRecomputed"));
        assertEquals(0, after.statistics.get("sitesRecomputed"));
        Map<String, String> evidence = binding(before, "Entry", "fingerprint");
        Map<String, Object> report = LookaheadSnapshot.compare(before, after, evidence);
        assertEquals(false, report.get("needsReview"));
        assertEquals(List.copyOf(evidence.keySet()), report.get("contextEvidenceReused"));
    }

    @Test
    void unchangedFirstDoesNotHideAChangedCalleeBody() throws Exception {
        String grammar = HEADER + "void Entry():{} { (LOOKAHEAD(2) Leaf() | <C>) <EOF> }\n"
                + "void Leaf():{} { <A><B> }";
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, grammar.replace("<A><B> }", "<A><C> }"));
        assertEquals(1, after.statistics.get("firstProductionsRecomputed"));
        assertEquals(1, after.statistics.get("sitesReused"));
        Map<String, String> evidence = binding(before, "Entry", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
    }

    @Test
    void continuationBeyondFollowOneInvalidatesEvenWhenRowsAreReused() throws Exception {
        String grammar = HEADER + "void Entry():{} { Opt() <A><B><EOF> }\n"
                + "void Opt():{} { [LOOKAHEAD(2) <A><B>] }";
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before,
                grammar.replace("Opt() <A><B>", "Opt() <A><C>"));
        assertEquals(before.followFacts, after.followFacts);
        assertEquals(1, after.statistics.get("sitesReused"));
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
    }

    @Test
    void shortNonNullableSuffixStillDependsOnHigherCallerTokens() throws Exception {
        String grammar = HEADER + "void Entry():{} { Middle() <B><C><EOF> }\n"
                + "void Middle():{} { Opt() <A> }\n"
                + "void Opt():{} { [LOOKAHEAD(3) <A><B><C>] }";
        String changed = grammar.replace("Middle() <B><C>", "Middle() <B><D>");
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, changed);
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
        try (GeneratedLookaheadParser original =
                new GeneratedLookaheadParser(directory.resolve("original"), grammar);
                GeneratedLookaheadParser removed = new GeneratedLookaheadParser(
                        directory.resolve("removed"), grammar.replace("LOOKAHEAD(3)", ""));
                GeneratedLookaheadParser current =
                        new GeneratedLookaheadParser(directory.resolve("current"), changed);
                GeneratedLookaheadParser currentRemoved = new GeneratedLookaheadParser(
                        directory.resolve("current-removed"),
                        changed.replace("LOOKAHEAD(3)", ""))) {
            assertFalse(original.accepts("abc"));
            assertFalse(removed.accepts("abc"));
            assertTrue(original.accepts("abcabc"));
            assertTrue(removed.accepts("abcabc"));
            assertTrue(current.accepts("abd"));
            assertFalse(currentRemoved.accepts("abd"));
            assertTrue(current.accepts("abcabd"));
            assertTrue(currentRemoved.accepts("abcabd"));
        }
    }

    @Test
    void recursiveFirstAndFollowFactsCanShrink() throws Exception {
        String grammar = HEADER + "void Entry():{} { P() <B><EOF> }\n"
                + "void P():{} { (<A> [P()] | <B>) }";
        LookaheadSnapshot after = update(parse(grammar),
                grammar.replace("P() <B><EOF>", "P() <C><EOF>")
                        .replace("| <B>)", "| <C>)"));
        assertEquals(Set.of(1, 3), after.firstFacts.get("P").get("tokens"));
        assertEquals(Set.of(-1, 3), after.followFacts.get("P"));
    }

    @Test
    void deletingAnActionRemovesRecursiveCaveats() throws Exception {
        String grammar = HEADER + "void Entry():{} { P() <EOF> }\n"
                + "void P():{} { <A> [Q()] }\nvoid Q():{} { <B> [P()] {int x=1;} }";
        LookaheadSnapshot after = update(parse(grammar), grammar.replace("{int x=1;}", ""));
        assertEquals(Set.of(), after.firstFacts.get("P").get("caveats"));
        assertEquals(Set.of(), after.firstFacts.get("Q").get("caveats"));
    }

    @Test
    void deletedAndAddedCallSitesDoNotLeaveOldFollowContributions() throws Exception {
        String grammar = HEADER + "void Entry():{} { P() Q() <EOF> }\n"
                + "void P():{} { <A> }\nvoid Q():{} { <B> }";
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot removed = update(before, grammar.replace("P() Q() <EOF>", "P() <EOF>"));
        assertEquals(Set.of(-1), removed.followFacts.get("Q"));
        update(removed, grammar);
    }

    @Test
    void movedLinesRelocateReusedDuplicateAnchors() throws Exception {
        String grammar = HEADER + "void Entry():{} { [LOOKAHEAD(2) <A>] [LOOKAHEAD(2) <A>] }";
        LookaheadSnapshot after = update(parse(grammar), grammar.replace(
                "[LOOKAHEAD(2) <A>] [", "\n[LOOKAHEAD(2)\n<A>]\n["));
        assertEquals(0, after.statistics.get("sitesRecomputed"));
        assertEquals(2, after.statistics.get("sitesReused"));
    }

    @Test
    void implicitLiteralChangesInvalidateTheTokenNamespace() throws Exception {
        String grammar = HEADER + "void Entry():{} { [LOOKAHEAD(<A>) <A>] <EOF> }\n"
                + "void Other():{} { <B> }";
        LookaheadSnapshot after = update(parse(grammar),
                grammar.replace("void Other():{} { <B>", "void Other():{} { \"z\""));
        assertEquals("lexer-options-or-engine-changed", after.statistics.get("fallback"));
    }

    @Test
    void tokenCertificateDoesNotInheritAnOpaqueContextClaim() throws Exception {
        String grammar = HEADER.replace("class Fixture {}", "class Fixture {boolean flag;}")
                + "void Entry():{} { [LOOKAHEAD(<A>) <A>] <EOF> }\n"
                + "void Other():{} { <B> }";
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before,
                grammar.replace("void Other():{} { <B>", "void Other():{} { <C>"));
        assertFalse(before.pureGrammar);
        Map<String, String> context = binding(before, "Entry", "fingerprint");
        assertEquals(List.copyOf(context.keySet()),
                LookaheadSnapshot.compare(before, after, context).get("invalidatedEvidence"));
        Map<String, String> certificate = binding(before, "Entry", "tokenFingerprint");
        String key = certificate.keySet().iterator().next();
        Map<String, Object> scoped = LookaheadSnapshot.compare(before, after, certificate,
                Map.of(key, "token-probe"));
        assertEquals(List.of(key), scoped.get("tokenCertificatesReused"));
        assertEquals(List.of(), scoped.get("contextEvidenceReused"));
    }

    @Test
    void probeOnlyCallersAreNotRuntimeContinuations() throws Exception {
        String grammar = HEADER + "void Entry():{} { Opt()<EOF> }\n"
                + "void Opt():{} { (LOOKAHEAD(2) <A><B> | <B>) }\n"
                + "void Other():{} { <C> }";
        LookaheadSnapshot before = parse(grammar);
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        String probing = grammar.replace("void Other():{} { <C>",
                "void Other():{} { (LOOKAHEAD(Opt()) <C> | <D>)");
        LookaheadSnapshot after = update(before, probing);
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("contextEvidenceReused"));
        String calling = grammar.replace("void Other():{} { <C>", "void Other():{} { Opt()<D>");
        LookaheadSnapshot actual = update(before, calling);
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, actual, evidence).get("invalidatedEvidence"));
    }

    @Test
    void probeCalleesInCallerSuffixesInvalidateAnUnchangedCallee() throws Exception {
        String grammar = HEADER + "void Entry():{} { Opt() "
                + "(LOOKAHEAD(Probe()) <A><B> | <A><C>) <EOF> }\n"
                + "void Probe():{} { <A><B> }\n"
                + "void Opt():{} { [LOOKAHEAD(2) <D><D>] }";
        String changed = grammar.replace("void Probe():{} { <A><B>", "void Probe():{} { <A><C>");
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, changed);
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
        try (GeneratedLookaheadParser original =
                new GeneratedLookaheadParser(directory.resolve("before"), grammar);
                GeneratedLookaheadParser current =
                        new GeneratedLookaheadParser(directory.resolve("after"), changed)) {
            assertTrue(original.accepts("ab"));
            assertFalse(current.accepts("ab"));
            assertTrue(original.accepts("ddac"));
            assertFalse(current.accepts("ddac"));
        }
    }

    @Test
    void changedProbeFirstRecomputesRowsWithoutAnActualCallEdge() throws Exception {
        String grammar = HEADER + "void Entry():{} { "
                + "(LOOKAHEAD(Probe()) <A><B> | <C>) <EOF> }\n"
                + "void Probe():{} { <A><B> }";
        LookaheadSnapshot after = update(parse(grammar),
                grammar.replace("void Probe():{} { <A><B>", "void Probe():{} { <C><B>"));
        assertEquals(1, after.statistics.get("sitesRecomputed"));
        assertEquals(List.of("<C>"), after.sites.get(0).get("probeFirst"));
    }

    @Test
    void inlineNamedRegexChangesTheGlobalLexerEvenWhenItsLabelIsStable() throws Exception {
        String grammar = HEADER + "void Entry():{} { (LOOKAHEAD(2) <A><A> | <B>) <EOF> }\n"
                + "void Other():{} { <X:\"x\"> }";
        String changed = grammar.replace("<X:\"x\">", "<X:\"aa\">");
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, changed);
        assertNotEquals(before.globalHash, after.globalHash);
        assertEquals("lexer-options-or-engine-changed", after.statistics.get("fallback"));
        try (GeneratedLookaheadParser original =
                new GeneratedLookaheadParser(directory.resolve("before"), grammar);
                GeneratedLookaheadParser current =
                        new GeneratedLookaheadParser(directory.resolve("after"), changed)) {
            assertTrue(original.accepts("aa"));
            assertFalse(current.accepts("aa"));
        }
    }

    @Test
    void inlineRegexRangesNegationAndRepetitionAreAllBound() throws Exception {
        for (List<String> pair : List.of(List.of("([\"x\"-\"z\"])+", "([\"w\"-\"z\"])+"),
                List.of("~[\"x\"]", "~[\"y\"]"),
                List.of("(\"x\"){2,3}", "(\"x\"){2,4}"))) {
            String grammar = HEADER + "void Entry():{} { [LOOKAHEAD(<A>) <A>]<EOF> }\n"
                    + "void Other():{} { <X:" + pair.get(0) + "> }";
            LookaheadSnapshot before = parse(grammar);
            LookaheadSnapshot after = update(before, grammar.replace(pair.get(0), pair.get(1)));
            assertNotEquals(before.globalHash, after.globalHash, pair.toString());
        }
        String grammar = HEADER + "void Entry():{} { <X:(\"x\")+><EOF> }";
        assertEquals(parse(grammar).globalHash,
                parse(grammar.replace("<X:", "\n<X: /* same regex */\n")).globalHash);
    }

    @Test
    void lexicalStateTransitionsKeepTheWholeGrammarFallback() throws Exception {
        String grammar = "options{STATIC=false;}\n"
                + "PARSER_BEGIN(Fixture) public class Fixture {} PARSER_END(Fixture)\n"
                + "<DEFAULT> TOKEN:{<P:\"p\">:S|<Q:\"q\">:T}\n"
                + "<S,T> TOKEN:{<A:\"a\">|<B:\"b\">}\n<T> TOKEN:{<C:\"c\">}\n"
                + "void Entry():{} {<P> Opt() (<A><C>|<B>)<EOF>}\n"
                + "void Opt():{} {[LOOKAHEAD(2)<A><B>]}";
        String changed = grammar.replace("{<P> Opt()", "{<Q> Opt()");
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, changed);
        assertFalse(before.pureGrammar);
        assertFalse(after.pureGrammar);
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
        try (GeneratedLookaheadParser original =
                new GeneratedLookaheadParser(directory.resolve("before"), grammar);
                GeneratedLookaheadParser removed = new GeneratedLookaheadParser(
                        directory.resolve("removed"), grammar.replace("LOOKAHEAD(2)", ""));
                GeneratedLookaheadParser current =
                        new GeneratedLookaheadParser(directory.resolve("after"), changed);
                GeneratedLookaheadParser currentRemoved = new GeneratedLookaheadParser(
                        directory.resolve("after-removed"), changed.replace("LOOKAHEAD(2)", ""))) {
            assertTrue(original.accepts("pb"));
            assertTrue(removed.accepts("pb"));
            assertFalse(original.accepts("pac"));
            assertFalse(removed.accepts("pac"));
            assertTrue(current.accepts("qac"));
            assertFalse(currentRemoved.accepts("qac"));
        }
    }

    static Stream<Arguments> callerPrefixChanges() {
        String opt = "void Opt():{} { [LOOKAHEAD(2) <A><B>] }";
        String direct = "void Entry():{} { (LOOKAHEAD(<A><B>) Opt() <A><C> | <D>) <EOF> }";
        String probe = "void Entry():{} { (LOOKAHEAD(Probe()) Opt() <A><C> | <D>) <EOF> }"
                + "void Probe():{} { <A><B> }";
        String preceding = "void Entry():{} { (LOOKAHEAD(<D>) Gate() Opt() <A><C> | <B>) <EOF> }"
                + "void Gate():{} { [LOOKAHEAD(<D><A><B>) <D>] }";
        String competitor = "void Entry():{} { (LOOKAHEAD(<A><C>) <A><C> | Opt() <A><C>) <EOF> }";
        return Stream.of(
                Arguments.of("direct-prefix", direct + opt,
                        direct.replace("LOOKAHEAD(<A><B>)", "LOOKAHEAD(<A>)") + opt,
                        "ac", Set.of("abac", "d")),
                Arguments.of("same-FIRST-probe-target", probe + opt,
                        probe.replace("void Probe():{} { <A><B> }", "void Probe():{} { <A> }")
                                + opt,
                        "ac", Set.of("abac", "d")),
                Arguments.of("preceding-callee", preceding + opt,
                        preceding.replace("LOOKAHEAD(<D><A><B>)", "LOOKAHEAD(<D><A>)") + opt,
                        "dac", Set.of("b", "dabac")),
                Arguments.of("earlier-competitor", competitor + opt,
                        competitor.replace("LOOKAHEAD(<A><C>)", "LOOKAHEAD(<A><B>)") + opt,
                        "ac", Set.of("abac", "ac")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("callerPrefixChanges")
    void callersCanChangeReachableInputsBeforeAnUnchangedCall(String name, String original,
            String changed, String witness, Set<String> language) throws Exception {
        String grammar = HEADER + original;
        String current = HEADER + changed;
        LookaheadSnapshot before = parse(grammar);
        LookaheadSnapshot after = update(before, current);
        assertTrue(before.pureGrammar);
        assertTrue(after.pureGrammar);
        Map<String, String> evidence = binding(before, "Opt", "fingerprint");
        assertEquals(List.copyOf(evidence.keySet()),
                LookaheadSnapshot.compare(before, after, evidence).get("invalidatedEvidence"));
        try (GeneratedLookaheadParser old =
                new GeneratedLookaheadParser(directory.resolve(name + "-old"), grammar);
                GeneratedLookaheadParser oldDeleted = new GeneratedLookaheadParser(
                        directory.resolve(name + "-old-deleted"),
                        grammar.replace("LOOKAHEAD(2)", ""));
                GeneratedLookaheadParser revised =
                        new GeneratedLookaheadParser(directory.resolve(name + "-current"), current);
                GeneratedLookaheadParser revisedDeleted = new GeneratedLookaheadParser(
                        directory.resolve(name + "-current-deleted"),
                        current.replace("LOOKAHEAD(2)", ""))) {
            Set<String> accepted = new TreeSet<>();
            for (String input : finiteInputs()) {
                boolean expected = old.accepts(input);
                assertEquals(expected, oldDeleted.accepts(input), input);
                if (expected) {
                    accepted.add(input);
                }
            }
            assertEquals(language, accepted);
            assertTrue(revised.accepts(witness));
            assertFalse(revisedDeleted.accepts(witness));
        }
    }

    private static List<String> finiteInputs() {
        List<String> result = new ArrayList<>(List.of(""));
        List<String> previous = List.of("");
        for (int length = 0; length < 5; length++) {
            List<String> next = new ArrayList<>();
            for (String prefix : previous) {
                for (String token : List.of("a", "b", "c", "d")) {
                    next.add(prefix + token);
                }
            }
            result.addAll(next);
            previous = next;
        }
        return result;
    }

}
