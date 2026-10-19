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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LookaheadTokenProbeTest {
    private static final String HEADER = "options { STATIC=false; }\n"
            + "PARSER_BEGIN(Fixture) public class Fixture {} PARSER_END(Fixture)\n"
            + "TOKEN: { <A: \"a\"> | <B: \"b\"> | <C: \"c\"> | <D: \"d\"> | <E: \"e\"> }\n";
    @TempDir
    Path directory;

    static Stream<Arguments> contexts() {
        return Stream.of(
                Arguments.of("choice", "<A>", "(PROBE Wide() | <E>) <E><EOF>"),
                Arguments.of("optional", "<A>", "[PROBE Wide()] <E><EOF>"),
                Arguments.of("repeat", "<A>", "(PROBE Wide())* <E><EOF>"),
                Arguments.of("token-set", "(<A>|<B>)", "[PROBE Wide()] <E><EOF>"),
                Arguments.of("nested", "<B>",
                        "(LOOKAHEAD(3) <A> [PROBE Wide()] <E> | <D>) <EOF>"),
                Arguments.of("nested-set", "(<B>|<C>)",
                        "(LOOKAHEAD(3) <A> [PROBE Wide()] <E> | <D>) <EOF>"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("contexts")
    void boundedSyntacticProbesPreserveAcceptanceAndRestriction(String name, String probe,
            String expansion) throws Exception {
        String grammar = HEADER + "void Entry():{} { " + expansion.replace("PROBE",
                "LOOKAHEAD(" + probe + ")") + " }\n"
                + "void Wide():{} { (<A>|<B>|<C>|<D>) }";
        String bounded = grammar.replace("LOOKAHEAD(" + probe + ")",
                "LOOKAHEAD(1," + probe + ")");
        String deleted = grammar.replace("LOOKAHEAD(" + probe + ")", "");
        int changesWithoutGuard = 0;
        try (GeneratedLookaheadParser before =
                new GeneratedLookaheadParser(directory.resolve(name + "-before"), grammar);
                GeneratedLookaheadParser after =
                        new GeneratedLookaheadParser(directory.resolve(name + "-after"), bounded);
                GeneratedLookaheadParser removed =
                        new GeneratedLookaheadParser(directory.resolve(name + "-deleted"),
                                deleted)) {
            for (String input : inputs(4)) {
                boolean accepted = before.accepts(input);
                assertEquals(accepted, after.accepts(input), input);
                if (accepted != removed.accepts(input)) {
                    changesWithoutGuard++;
                }
            }
        }
        assertTrue(changesWithoutGuard > 0, "the mutation must expose a necessary restriction");
    }

    @Test
    void certificatesRejectSemanticZeroDepthNestedAndMultiTokenProbes() throws Exception {
        for (String probe : List.of("LOOKAHEAD(0,<A>)", "LOOKAHEAD(1,<A>,{true})",
                "LOOKAHEAD(<A><B>)", "LOOKAHEAD(2,(LOOKAHEAD(<A>) <A>))")) {
            LookaheadSnapshot snapshot = LookaheadSnapshot.parse(HEADER
                    + "void Entry():{} { (" + probe + " <A> | <B>) <EOF> }", "external", "engine");
            assertTrue(snapshot.sites.stream()
                    .noneMatch(site -> Boolean.TRUE.equals(site.get("tokenProbe"))), probe);
            assertTrue(snapshot.contexts().values().stream().flatMap(List::stream)
                    .noneMatch(binding -> binding.containsKey("tokenFingerprint")), probe);
        }
    }

    private static List<String> inputs(int depth) {
        List<String> result = new ArrayList<>();
        result.add("");
        List<String> previous = List.of("");
        for (int i = 0; i < depth; i++) {
            List<String> next = new ArrayList<>();
            for (String prefix : previous) {
                for (String token : List.of("a", "b", "c", "d", "e")) {
                    next.add(prefix + token);
                }
            }
            result.addAll(next);
            previous = next;
        }
        return result;
    }
}
