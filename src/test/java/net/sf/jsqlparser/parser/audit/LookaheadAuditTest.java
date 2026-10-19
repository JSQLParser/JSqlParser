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
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LookaheadAuditTest {
    private static final String GRAMMAR = "options { STATIC=false; }\n"
            + "PARSER_BEGIN(Fixture) public class Fixture { boolean enabled=true; } PARSER_END(Fixture)\n"
            + "TOKEN: { <A: \"a\"> | <B: \"b\"> | <C: \"c\"> }\n"
            + "void Direct():{} { (LOOKAHEAD(2) Leaf() | <C>) <EOF> }\n"
            + "void Indirect():{} { (LOOKAHEAD(2) Bridge() | <C>) <EOF> }\n"
            + "void Bridge():{} { Leaf() }\nvoid Leaf():{} { <A> <B> }\n"
            + "void Optional():{} { [LOOKAHEAD(2) <A> <B>] }\n"
            + "void Caller():{} { Optional() Tail() <EOF> }\nvoid Tail():{} { [<C>] }\n"
            + "void Gate():{} { (LOOKAHEAD({enabled}) <A> | <B>) <EOF> }\n"
            + "void Anchor():{} { [LOOKAHEAD(<B>) <B>] [LOOKAHEAD(<C>) <C>] <EOF> }\n"
            + "void Duplicate():{} { [LOOKAHEAD(2) <A>] [LOOKAHEAD(2) <A>] }\n"
            + "void Recovery():{} { try { <A> } catch (ParseException error) { enabled=true; } finally { enabled=true; } }\n"
            + "void UnknownChoice():{} { (LOOKAHEAD(2) <C> | { enabled=true; } <A>) <EOF> }\n"
            + "void NullableAlternative():{} { (LOOKAHEAD(2) <C> <B> | [<A>]) <C> }\n";
    private static LookaheadSnapshot baseline;
    private static Map<String, String> evidence;

    @BeforeAll
    static void baseline() throws Exception {
        baseline = parse(GRAMMAR);
        evidence = binding(baseline, "Direct");
    }

    private static LookaheadSnapshot parse(String grammar) throws Exception {
        return LookaheadSnapshot.parse(grammar, "external", "engine");
    }

    private static Map<String, String> binding(LookaheadSnapshot snapshot, String production) {
        Map<String, List<Map<String, Object>>> contexts = snapshot.contexts();
        String key = contexts.keySet().stream().filter(k -> k.startsWith(production + ":"))
                .findFirst().orElseThrow();
        return Map.of(key, contexts.get(key).get(0).get("fingerprint").toString());
    }

    private static Map<String, Object> compare(String text) throws Exception {
        return LookaheadSnapshot.compare(baseline, parse(text), evidence);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> findings(Map<String, Object> report) {
        return (List<Map<String, Object>>) report.get("changes");
    }

    @Test
    void commentsAndLineMovesDoNotReopenEvidence() throws Exception {
        assertEquals(false, compare("// shifted line\n\n" + GRAMMAR).get("needsReview"));
        Map<String, Object> report = compare(GRAMMAR.replace("void Leaf()", "\n\nvoid Leaf()"));
        assertEquals(false, report.get("needsReview"));
        assertEquals(new ArrayList<>(evidence.keySet()), report.get("reusableEvidence"));
    }

    @Test
    void insertedProbeDoesNotRenumberExistingAnchors() throws Exception {
        LookaheadSnapshot changed = parse(
                GRAMMAR.replace("void Anchor():{} {", "void Anchor():{} { [LOOKAHEAD(<A>) <A>]"));
        assertTrue(changed.contexts().keySet().containsAll(baseline.contexts().keySet()));
        assertEquals(1, findings(LookaheadSnapshot.compare(baseline, changed, evidence)).stream()
                .filter(r -> r.get("kind").equals("added")).count());
    }

    @Test
    void indirectCalleeChangesInvalidateEvidenceEvenWhenFirstIsUnchanged() throws Exception {
        Map<String, Object> report = compare(
                GRAMMAR.replace("void Leaf():{} { <A> <B> }", "void Leaf():{} { <A> <C> }"));
        Map<String, Object> indirect = findings(report).stream()
                .filter(r -> r.get("anchor").toString().startsWith("Indirect:")).findFirst()
                .orElseThrow();
        assertEquals(false, indirect.get("localFactsChanged"));
        assertEquals(List.of("Leaf"), indirect.get("dependencyChanges"));
        assertEquals(new ArrayList<>(evidence.keySet()), report.get("invalidatedEvidence"));
    }

    @Test
    void nullableAndFollowChangesReachAnUnchangedCallee() throws Exception {
        for (String replacement : List.of("<C>", "[<A>]")) {
            Map<String, Object> report = compare(GRAMMAR.replace("void Tail():{} { [<C>] }",
                    "void Tail():{} { " + replacement + " }"));
            Map<String, Object> finding = findings(report).stream()
                    .filter(r -> r.get("anchor").toString().startsWith("Optional:")).findFirst()
                    .orElseThrow();
            assertEquals(true, finding.get("localFactsChanged"));
            assertEquals(List.of("Tail"), finding.get("dependencyChanges"));
        }
    }

    @Test
    void changedDepthAndDeletionAreVisible() throws Exception {
        assertTrue(findings(compare(GRAMMAR.replace("LOOKAHEAD(2) Leaf()", "LOOKAHEAD(3) Leaf()")))
                .stream().anyMatch(r -> r.get("anchor").toString().startsWith("Direct:")));
        assertEquals(1, findings(compare(GRAMMAR.replace("[LOOKAHEAD(<B>) <B>]", "[<B>]"))).stream()
                .filter(r -> r.get("kind").equals("removed")).count());
    }

    @Test
    void parserPolicyLexerExternalAndEngineChangesCannotReuseEvidence() throws Exception {
        for (String text : List.of(
                GRAMMAR.replace("boolean enabled=true;", "boolean enabled=false;"),
                GRAMMAR.replace("<A: \"a\">", "<A: \"z\">"))) {
            Map<String, Object> report = compare(text);
            assertEquals(new ArrayList<>(evidence.keySet()), report.get("invalidatedEvidence"));
            assertEquals(List.of(), report.get("changes"));
        }
        for (LookaheadSnapshot changed : List.of(LookaheadSnapshot.parse(GRAMMAR, "new", "engine"),
                LookaheadSnapshot.parse(GRAMMAR, "external", "new"))) {
            assertEquals(new ArrayList<>(evidence.keySet()), LookaheadSnapshot
                    .compare(baseline, changed, evidence).get("invalidatedEvidence"));
        }
    }

    @Test
    void recoveryJavaChangesInvalidateUnrelatedEvidenceConservatively() throws Exception {
        Map<String, Object> report = compare(GRAMMAR.replace(
                "catch (ParseException error) { enabled=true; } finally { enabled=true; }",
                "catch (ParseException error) { enabled=false; } finally { enabled=false; }"));
        assertEquals(new ArrayList<>(evidence.keySet()), report.get("invalidatedEvidence"));
        assertEquals(List.of("opaque-Java-or-predicate", "grammar-visible-to-opaque-Java"),
                report.get("globalSignals"));
        assertEquals(List.of(), report.get("changes"));
    }

    @Test
    void ambiguousAndMissingBindingsAreConservative() {
        Map<String, String> duplicate = binding(baseline, "Duplicate");
        assertEquals(new ArrayList<>(duplicate.keySet()), LookaheadSnapshot
                .compare(baseline, baseline, duplicate).get("invalidatedEvidence"));
        assertEquals(List.of("unbound:missing"),
                LookaheadSnapshot.compare(baseline, baseline, Map.of("unbound:missing", "hash"))
                        .get("invalidatedEvidence"));
    }

    @Test
    void externalWatchScopeIncludesAllProductionModelsResourcesAndBuildInputs() {
        assertTrue(LookaheadAudit.watched("src/main/java/example/AnUnrelatedModel.java"));
        assertTrue(LookaheadAudit.watched("src/main/resources/dialect.properties"));
        assertTrue(LookaheadAudit.watched("pom.xml"));
        assertTrue(LookaheadAudit.watched("build.gradle"));
        for (String path : List.of(".mvn/jvm.config", ".mvn/maven.config", ".mvn/extensions.xml",
                "gradle/wrapper/gradle-wrapper.jar", "gradlew", "mvnw", ".mvn", "gradle")) {
            assertTrue(LookaheadAudit.watched(path), path);
        }
        assertTrue(LookaheadAudit
                .watched("src/test/java/net/sf/jsqlparser/parser/LookaheadCleanupTest.java"));
        assertFalse(LookaheadAudit.watched("build/generated/Parser.java"));
    }

    @Test
    void predicatesAreRetainedAndNothingIsAutomaticallyRewritten() {
        Map<String, Object> gate = baseline.sites.stream()
                .filter(r -> r.get("production").equals("Gate")).findFirst().orElseThrow();
        assertEquals("retain-state-or-policy", gate.get("triage"));
        assertEquals(0, LookaheadSnapshot.compare(baseline, baseline, evidence)
                .get("automaticGrammarChanges"));
    }

    @Test
    void competingActionsPreventAClaimOfDisjointDispatch() {
        Map<String, Object> decision = baseline.sites.stream()
                .filter(r -> r.get("production").equals("UnknownChoice")).findFirst().orElseThrow();
        assertEquals("review-shared-prefix-or-unknown", decision.get("triage"));
        assertEquals(Set.of("java-action"), decision.get("competitorCaveats"));
    }

    @Test
    void nullableCompetitorContributesFollowToTheDecision() {
        Map<String, Object> decision = baseline.sites.stream()
                .filter(r -> r.get("production").equals("NullableAlternative")).findFirst()
                .orElseThrow();
        assertEquals("review-shared-prefix-or-unknown", decision.get("triage"));
        assertEquals(List.of("<C>"), decision.get("overlap"));
    }
}
