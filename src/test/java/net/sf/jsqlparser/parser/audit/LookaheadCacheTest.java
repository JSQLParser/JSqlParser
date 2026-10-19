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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LookaheadCacheTest {
    private static final String GRAMMAR = "options { STATIC=false; }\n"
            + "PARSER_BEGIN(Fixture) public class Fixture {} PARSER_END(Fixture)\n"
            + "TOKEN: { <A: \"a\"> | <B: \"b\"> }\n"
            + "void Entry():{} { (LOOKAHEAD(<A>) <A> | <B>) <EOF> }";
    @TempDir
    Path root;

    @Test
    void independentJvmsReuseFactsAndKeepExternalBindingsSeparate() throws Exception {
        Path grammar = root.resolve("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        Files.createDirectories(grammar.getParent());
        Files.writeString(grammar, GRAMMAR);
        Path tool = root.resolve("src/test/java/net/sf/jsqlparser/parser/audit/Engine.java");
        Files.createDirectories(tool.getParent());
        Files.writeString(tool, "// fixed engine input\n");
        git("init", "-q");
        git("add", ".");
        git("-c", "user.name=Fixture", "-c", "user.email=fixture@example.invalid", "commit", "-qm",
                "baseline");
        Path evidence = root.resolve("evidence.tsv");
        Files.writeString(evidence, "Entry\tstale\treviewed context\tregression\n");
        run("cold", evidence, false);
        Files.writeString(evidence,
                Files.readString(root.resolve("cold/binding-candidates.tsv")));
        String warm = run("warm", evidence, false);
        assertEquals(2, occurrences(warm, "\"frontEndCacheHit\":true"));
        assertEquals(2, occurrences(warm, "\"javaCCParses\":0"));
        assertEquals(2, occurrences(warm, "\"firstProductionsRecomputed\":0"));
        assertTrue(warm.contains("\"needsReview\":false"));
        Path model = root.resolve("src/main/java/Model.java");
        Files.createDirectories(model.getParent());
        Files.writeString(model, "class Model {}\n");
        String changed = run("external", evidence, false);
        assertEquals(2, occurrences(changed, "\"javaCCParses\":0"));
        assertTrue(changed.contains("\"invalidatedEvidence\":[\"Entry:"));
        assertTrue(changed.contains("\"contextEvidenceReused\":[]"));
        String full = run("full", evidence, true);
        assertEquals(2, occurrences(full, "\"frontEndCacheHit\":false"));
        assertEquals(2, occurrences(full, "\"javaCCParses\":1"));
        assertTrue(full.contains("\"invalidatedEvidence\":[\"Entry:"));
        assertEquals(Files.readString(root.resolve("warm/inventory.json")),
                Files.readString(root.resolve("full/inventory.json")));
        assertEquals(2, Files.readAllLines(root.resolve("warm/inventory.tsv")).size());
    }

    @Test
    void corruptedCacheIsRebuiltAndDoesNotSupplyFacts() throws Exception {
        Path cache = root.resolve("cache");
        LookaheadSnapshot original = LookaheadAudit.load(GRAMMAR, root.resolve("first"),
                "external", "engine", cache, null, false);
        String key = LookaheadCache.key(GRAMMAR, "engine");
        Files.writeString(cache.resolve(key + ".facts"), "truncated cache");
        LookaheadSnapshot rebuilt = LookaheadAudit.load(GRAMMAR, root.resolve("rebuilt"),
                "external", "engine", cache, null, false);
        assertEquals(false, rebuilt.statistics.get("frontEndCacheHit"));
        assertEquals(1, rebuilt.statistics.get("javaCCParses"));
        assertEquals(original.firstFacts, rebuilt.firstFacts);
        assertEquals(original.followFacts, rebuilt.followFacts);
        assertEquals(original.contexts(), rebuilt.contexts());
    }

    @Test
    void aValidChecksumDoesNotHideATruncatedDeclaredPayload() throws Exception {
        Path cache = root.resolve("cache");
        LookaheadSnapshot original = LookaheadAudit.load(GRAMMAR, root.resolve("first"),
                "external", "engine", cache, null, false);
        Path path = cache.resolve(LookaheadCache.key(GRAMMAR, "engine") + ".facts");
        byte[] encoded = Files.readAllBytes(path);
        java.io.ByteArrayInputStream bytes = new java.io.ByteArrayInputStream(encoded);
        try (java.io.DataInputStream input = new java.io.DataInputStream(bytes)) {
            input.readUTF();
            input.readUTF();
            input.readUTF();
            int position = encoded.length - bytes.available();
            int declared = input.readInt();
            java.nio.ByteBuffer.wrap(encoded, position, 4).putInt(declared + 1);
        }
        Files.write(path, encoded);
        LookaheadSnapshot rebuilt = LookaheadAudit.load(GRAMMAR, root.resolve("rebuilt"),
                "external", "engine", cache, null, false);
        assertEquals(false, rebuilt.statistics.get("frontEndCacheHit"));
        assertEquals(original.contexts(), rebuilt.contexts());
    }

    @Test
    void changedEngineCannotReuseAPreviousPayload() throws Exception {
        Path cache = root.resolve("cache");
        LookaheadSnapshot before = LookaheadAudit.load(GRAMMAR, root.resolve("before"),
                "external", "engine", cache, null, false);
        LookaheadSnapshot after = LookaheadAudit.load(GRAMMAR, root.resolve("after"),
                "external", "new-engine", cache, before, false);
        assertEquals(false, after.statistics.get("frontEndCacheHit"));
        assertEquals("lexer-options-or-engine-changed", after.statistics.get("fallback"));
        assertEquals(before.productions.size(), after.statistics.get("firstProductionsRecomputed"));
    }

    private String run(String name, Path evidence, boolean full) throws Exception {
        Path output = root.resolve(name);
        GeneratedLookaheadParser.run(root, "UTF-8", LookaheadAudit.class.getName(), root.toString(),
                "HEAD", output.toString(), evidence.toString(), "false", Boolean.toString(full));
        return Files.readString(output.resolve("report.json"));
    }

    private static long occurrences(String text, String value) {
        return java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(value)).matcher(text)
                .results().count();
    }

    private void git(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git", "-C", root.toString()));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);
    }
}
