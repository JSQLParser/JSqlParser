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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LookaheadConservatismTest {
    private static final String HEADER = "options { STATIC=false; }\n"
            + "PARSER_BEGIN(Fixture) public class Fixture { Token state, other; } PARSER_END(Fixture)\n"
            + "TOKEN: { <A: \"a\"> | <B: \"b\"> | <C: \"c\"> }\n";
    @TempDir
    Path directory;

    private static LookaheadSnapshot parse(String grammar) throws Exception {
        return LookaheadSnapshot.parse(grammar, "external", "engine");
    }

    private static Map<String, String> binding(LookaheadSnapshot snapshot, String production) {
        return snapshot.contexts().entrySet().stream()
                .filter(e -> e.getKey().startsWith(production + ":"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().get(0).get("fingerprint").toString()));
    }

    private static void invalidates(String before, String after, String production)
            throws Exception {
        LookaheadSnapshot baseline = parse(before);
        Map<String, String> evidence = binding(baseline, production);
        assertFalse(evidence.isEmpty());
        Map<String, Object> report = LookaheadSnapshot.compare(baseline, parse(after), evidence);
        assertEquals(true, report.get("needsReview"));
        assertEquals(List.copyOf(evidence.keySet()), report.get("invalidatedEvidence"));
        assertEquals(List.of(), report.get("reusableEvidence"));
        assertEquals(false, LookaheadSnapshot.compare(baseline, parse(before), evidence)
                .get("needsReview"));
    }

    @Test
    void callerContinuationBeyondFollowOneInvalidatesEvidence() throws Exception {
        String grammar = HEADER + "void Opt():{} { [LOOKAHEAD(2) <A> <B>] }\n"
                + "void Entry():{} { Opt() <A> <B> <EOF> }\n";
        String changed = grammar.replace("Opt() <A> <B>", "Opt() <A> <C>");
        try (GeneratedLookaheadParser original = new GeneratedLookaheadParser(
                directory.resolve("original"), grammar);
                GeneratedLookaheadParser removed = new GeneratedLookaheadParser(
                        directory.resolve("removed"), grammar.replace("LOOKAHEAD(2)", ""));
                GeneratedLookaheadParser current = new GeneratedLookaheadParser(
                        directory.resolve("current"), changed);
                GeneratedLookaheadParser currentRemoved = new GeneratedLookaheadParser(
                        directory.resolve("current-removed"),
                        changed.replace("LOOKAHEAD(2)", ""))) {
            for (String input : List.of("ab", "ac", "abab", "abac")) {
                assertEquals(input.equals("abab"), original.accepts(input));
                assertEquals(original.accepts(input), removed.accepts(input));
                assertEquals(input.equals("ac") || input.equals("abac"), current.accepts(input));
                assertEquals(input.equals("abac"), currentRemoved.accepts(input));
            }
        }
        invalidates(grammar, changed, "Opt");
        invalidates(changed, grammar, "Opt");
    }

    @Test
    void callerContinuationCalleesAndAncestorsAreDependencies() throws Exception {
        String grammar = HEADER + "void Opt():{} { [LOOKAHEAD(2) <A> <B>] }\n"
                + "void Middle():{} { Opt() }\n"
                + "void Entry():{} { Middle() Tail() <EOF> }\n"
                + "void Tail():{} { <A> <B> }\n";
        invalidates(grammar, grammar.replace("void Tail():{} { <A> <B>",
                "void Tail():{} { <A> <C>"), "Opt");
        invalidates(grammar, grammar.replace("Middle() Tail()", "Middle() <A> <C>"), "Opt");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void assignmentTargetsInvalidateSharedStatePredicates(boolean production) throws Exception {
        String grammar = HEADER + "void Entry():{} { Setter() Gate() <EOF> }\n"
                + "void Setter():{} { state=" + (production ? "Value()" : "<A>") + " }\n"
                + "Token Value():{Token value;} { value=<A> {return value;} }\n"
                + "void Gate():{} { (LOOKAHEAD({state == null}) <B> | <C>) }\n";
        String changed = grammar.replace("state=", "other=");
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), changed)) {
            assertFalse(before.accepts("ab"));
            assertTrue(after.accepts("ab"));
            assertTrue(before.accepts("ac"));
            assertFalse(after.accepts("ac"));
        }
        assertNotEquals(parse(grammar).opaqueHash, parse(changed).opaqueHash);
        invalidates(grammar, changed, "Gate");
    }

    @Test
    void productionSignaturesInvalidateJavaOverloadDecisions() throws Exception {
        String grammar = HEADER.replace("Token state, other;",
                "boolean flag(Number n) {return true;} boolean flag(Integer n) {return false;} "
                        + "boolean enabled() {try {return flag(Value());} "
                        + "catch(ParseException error) {return false;}}")
                + "Number Value():{} { {return 1;} }\n"
                + "void Entry():{} { (LOOKAHEAD({enabled()}) <A> | <B>) <EOF> }\n";
        String changed = grammar.replace("Number Value()", "Integer Value()");
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), changed)) {
            assertTrue(before.accepts("a"));
            assertFalse(after.accepts("a"));
        }
        assertNotEquals(parse(grammar).opaqueHash, parse(changed).opaqueHash);
        invalidates(grammar, changed, "Entry");
    }

    @Test
    void actionsAfterTheFirstTokenPreventDisjointSuggestions() throws Exception {
        String grammar = HEADER.replace("TOKEN: { <A: \"a\"> | <B: \"b\"> | <C: \"c\"> }",
                "<DEFAULT> TOKEN: { <A: \"a\"> | <B: \"b\"> | <D: \"d\"> }\n"
                        + "<S> TOKEN: { <C: \"b\"> }")
                + "void Entry():{} { (LOOKAHEAD(2) <A> {token_source.SwitchTo(S);} <B> | <D>) <EOF> }";
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), grammar.replace("LOOKAHEAD(2)", ""))) {
            assertTrue(before.accepts("ab"));
            assertFalse(after.accepts("ab"));
            assertTrue(before.accepts("d"));
            assertTrue(after.accepts("d"));
        }
        Map<String, Object> site = parse(grammar).sites.get(0);
        assertEquals("review-shared-prefix-or-unknown", site.get("triage"));
        assertTrue(site.get("caveats").toString().contains("java-action"));
    }

    @Test
    void independentExplicitProbesAreNotAssumedToMatchTheirBranch() throws Exception {
        String grammar = HEADER + "void Entry():{} { "
                + "(LOOKAHEAD(<A><C>) <A> [<C>] [<B>] | <B>) <EOF> }";
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), grammar.replace("LOOKAHEAD(<A><C>)", ""))) {
            assertFalse(before.accepts("ab"));
            assertTrue(after.accepts("ab"));
            assertTrue(before.accepts("ac"));
            assertTrue(after.accepts("ac"));
        }
        assertEquals("review-shared-prefix-or-unknown", parse(grammar).sites.get(0).get("triage"));
    }

    @Test
    void nestedProbeDecisionsRemainVisibleWithoutInventingParents() throws Exception {
        String grammar = HEADER + "void Entry():{} { "
                + "(LOOKAHEAD(2, (LOOKAHEAD(2) <A><B> | <A><C>)) <A><B> | <C>) <EOF> }";
        LookaheadSnapshot snapshot = parse(grammar);
        assertEquals(2, snapshot.sites.size());
        assertTrue(snapshot.sites.stream()
                .allMatch(s -> s.get("triage").equals("review-shared-prefix-or-unknown")));
    }

    @Test
    void mavenEncodingConfigurationIsARealEvidenceInput() throws Exception {
        Path root = directory.resolve("repo");
        Files.createDirectories(root.resolve(".mvn"));
        Path grammar = root.resolve("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        Files.createDirectories(grammar.getParent());
        Files.writeString(grammar,
                HEADER + "void Entry():{} { (LOOKAHEAD(2) <A><B> | <C>) <EOF> }");
        Path tool = root.resolve("src/test/java/net/sf/jsqlparser/parser/audit/Engine.java");
        Files.createDirectories(tool.getParent());
        Files.writeString(tool, "// fixed test engine input\n");
        Path configuration = root.resolve(".mvn/jvm.config");
        Files.writeString(configuration, "-Dfile.encoding=UTF-8\n");
        git(root, "init");
        git(root, "add", ".");
        git(root, "-c", "user.name=Fixture", "-c", "user.email=fixture@example.invalid", "commit",
                "-m", "baseline");
        Path evidence = root.resolve("evidence.tsv");
        Files.writeString(evidence, "Entry\tstale\treviewed\tregression\n");
        Path first = root.resolve("first-report");
        audit(root, first, evidence);
        String candidate = Files.readString(first.resolve("binding-candidates.tsv"));
        assertTrue(candidate.startsWith("Entry\t"));
        Files.writeString(evidence, candidate);
        Path stable = root.resolve("stable-report");
        audit(root, stable, evidence);
        String unchanged = Files.readString(stable.resolve("report.json"));
        assertTrue(unchanged.contains("\"needsReview\":false"));
        assertTrue(unchanged.contains("\"reusableEvidence\":[\"Entry:"));
        Files.writeString(configuration, "-Dfile.encoding=US-ASCII\n");
        Path second = root.resolve("second-report");
        audit(root, second, evidence);
        String changed = Files.readString(second.resolve("report.json"));
        assertTrue(changed.contains("\"needsReview\":true"));
        assertTrue(changed.contains("production-Java-resources-or-build"));
        assertTrue(changed.contains("\"reusableEvidence\":[]"));
        assertTrue(changed.contains("\"invalidatedEvidence\":[\"Entry:"));
    }

    @Test
    void tokenFieldAccessIsPartOfOpaqueJava() throws Exception {
        String grammar = HEADER.replace("Token state, other;", "Object state;")
                + "void Entry():{} { Setter() Gate() <EOF> }\n"
                + "void Setter():{} { state=<A>.image }\n"
                + "void Gate():{} { (LOOKAHEAD({state instanceof String}) <B> | <C>) }";
        String changed = grammar.replace("<A>.image", "<A>.kind");
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), changed)) {
            assertTrue(before.accepts("ab"));
            assertFalse(after.accepts("ab"));
        }
        assertNotEquals(parse(grammar).opaqueHash, parse(changed).opaqueHash);
        invalidates(grammar, changed, "Gate");
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOOKAHEAD(0)", "LOOKAHEAD(0,<A>)"})
    void zeroDepthNeverSuggestsAnEquivalentTokenGuard(String probe) throws Exception {
        String grammar = HEADER + "void Entry():{} { (" + probe + " <A> | <B>) <EOF> }";
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), grammar.replace(probe, ""))) {
            assertFalse(before.accepts("b"));
            assertTrue(after.accepts("b"));
        }
        assertEquals("review-shared-prefix-or-unknown", parse(grammar).sites.get(0).get("triage"));
    }

    @Test
    void defaultDepthMeansTheConfiguredDefault() throws Exception {
        String grammar = HEADER.replace("STATIC=false;", "STATIC=false; LOOKAHEAD=2;")
                + "void Entry():{} { [LOOKAHEAD(1) <A><B>] <A><C><EOF> }";
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), grammar.replace("LOOKAHEAD(1)", ""))) {
            assertFalse(before.accepts("ac"));
            assertTrue(after.accepts("ac"));
        }
        assertEquals("review-shared-prefix-or-unknown", parse(grammar).sites.get(0).get("triage"));
    }

    @Test
    void mavenJvmEncodingChangesActualGeneratedTokens() throws Exception {
        for (String encoding : List.of("UTF-8", "US-ASCII")) {
            Path output = directory.resolve(encoding);
            Files.createDirectories(output);
            Path input = output.resolve("Fixture.jjt");
            Files.writeString(input, HEADER.replace("<A: \"a\">", "<A: \"ʰ\">")
                    + "void Entry():{} { <A><EOF> }");
            GeneratedLookaheadParser.run(output, encoding, "org.javacc.jjtree.Main",
                    "-GRAMMAR_ENCODING=UTF-8", "-OUTPUT_DIRECTORY=" + output, input.toString());
            String generated = Files.readString(output.resolve("Fixture.jj"));
            try (GeneratedLookaheadParser parser =
                    new GeneratedLookaheadParser(output, generated)) {
                assertEquals(encoding.equals("UTF-8"), parser.accepts("ʰ"));
                assertEquals(encoding.equals("US-ASCII"), parser.accepts("?"));
            }
        }
    }

    @Test
    void effectsAfterTheFirstTokenPropagateThroughCallsAndCompetingBranches() throws Exception {
        String grammar = HEADER
                + "void Entry():{} { (LOOKAHEAD(2) <A> Effect() | <C>) <EOF> }\n";
        for (String effect : List.of("void Effect():{} { {state=null;} <B> }",
                "void Effect():{Token token;} { token=<B> }")) {
            LookaheadSnapshot snapshot = parse(grammar + effect);
            Map<String, Object> site = snapshot.sites.get(0);
            assertEquals(List.of("<A>"), site.get("branchFirst"));
            assertEquals(false, site.get("nullable"));
            assertEquals("review-shared-prefix-or-unknown", site.get("triage"));
            assertFalse(site.get("caveats").toString().equals("[]"));
            String competing = grammar.replace("<A> Effect() | <C>", "<A> <B> | <C> Effect()");
            assertEquals("review-shared-prefix-or-unknown",
                    parse(competing + effect).sites.get(0).get("triage"));
        }
    }

    @Test
    void javaHelpersCanCallProductionsOutsideTheGrammarCallGraph() throws Exception {
        String grammar = HEADER.replace("Token state, other;",
                "boolean enabled() {try {Leaf(); return true;} "
                        + "catch(ParseException error) {return false;}}")
                + "void Entry():{} { (LOOKAHEAD({enabled()}) <B> | <C>) <EOF> }\n"
                + "void Leaf():{} { <A> }";
        String changed = grammar.replace("void Leaf():{} { <A> }", "void Leaf():{} { <A><B> }");
        try (GeneratedLookaheadParser before = new GeneratedLookaheadParser(
                directory.resolve("before"), grammar);
                GeneratedLookaheadParser after = new GeneratedLookaheadParser(
                        directory.resolve("after"), changed)) {
            assertTrue(before.accepts("ab"));
            assertFalse(after.accepts("ab"));
            assertFalse(before.accepts("abb"));
            assertTrue(after.accepts("abb"));
            assertTrue(before.accepts("c"));
            assertTrue(after.accepts("c"));
        }
        invalidates(grammar, changed, "Entry");
    }

    @Test
    void signatureAdditionsCannotBeAssumedUnrelatedToOpaqueJava() throws Exception {
        String grammar = HEADER + "void Entry():{} { (LOOKAHEAD(2) <A><B> | <C>) <EOF> }";
        String disconnected = grammar + "\nvoid Unrelated():{} { <C> }";
        invalidates(grammar, disconnected, "Entry");
        String changed = disconnected.replace("void Unrelated():{} { <C> }",
                "void Unrelated():{} { <B> }");
        invalidates(disconnected, changed, "Entry");
        LookaheadSnapshot original = parse(disconnected);
        Map<String, Object> report = LookaheadSnapshot.compare(original, parse(changed),
                binding(original, "Entry"));
        assertEquals(List.of("grammar-visible-to-opaque-Java"), report.get("globalSignals"));
        assertEquals(List.of(), report.get("changes"));
    }

    @Test
    void brokenWatchedRootAndNestedSymlinksAreRejected() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(
                System.getProperty("os.name").startsWith("Windows"));
        int index = 0;
        for (String input : List.of(".mvn", ".mvn/jvm.config", "gradle", "gradle/wrapper/jar",
                "src/main/java", "src/main/resources", "gradlew")) {
            Path root = directory.resolve("symlink-" + index++);
            Path link = root.resolve(input);
            Files.createDirectories(link.getParent());
            Files.createSymbolicLink(link, root.resolve("missing-target").toAbsolutePath());
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> LookaheadAudit.workingInputs(root));
            assertTrue(failure.getMessage().contains("Symlink"));
        }
    }

    private static void audit(Path root, Path output, Path evidence) throws Exception {
        GeneratedLookaheadParser.run(root, "UTF-8", LookaheadAudit.class.getName(), root.toString(),
                "HEAD", output.toString(), evidence.toString());
    }

    private static void git(Path root, String... arguments) throws Exception {
        java.util.ArrayList<String> command = new java.util.ArrayList<>(
                List.of("git", "-C", root.toString()));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);
    }
}
