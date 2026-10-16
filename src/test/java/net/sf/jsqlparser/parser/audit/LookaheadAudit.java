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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/** Repository entry point: read git and files, calculate both snapshots, write a review report. */
public final class LookaheadAudit {
    private static final String GRAMMAR =
            "src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt";
    private static final String TOOL = "src/test/java/net/sf/jsqlparser/parser/audit/";
    private static final Set<String> BUILD_INPUTS = Set.of("build.gradle", "settings.gradle",
            "gradle.properties", "pom.xml", "gradle/wrapper/gradle-wrapper.properties",
            "src/test/java/net/sf/jsqlparser/parser/LookaheadCleanupTest.java");

    private LookaheadAudit() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "root base-ref report-directory evidence.tsv [fail-on-findings]");
        }
        Path root = Path.of(args[0]).toAbsolutePath();
        Path output = Path.of(args[2]).toAbsolutePath();

        Files.createDirectories(output);
        String base = baseCommit(root, args[1]);
        String engine = engine(root);
        Map<String, String> beforeInputs = baseInputs(root, base);
        Map<String, String> afterInputs = workingInputs(root);

        System.out.println("Calculating base lookahead facts at " + base);
        LookaheadSnapshot before =
                generate(git(root, "show", base + ":" + GRAMMAR), output.resolve("base"),
                        LookaheadAnalysis.sha(LookaheadAnalysis.json(beforeInputs)), engine);
        System.out.println("Calculating working-tree lookahead facts");
        LookaheadSnapshot after =
                generate(Files.readString(root.resolve(GRAMMAR)), output.resolve("current"),
                        LookaheadAnalysis.sha(LookaheadAnalysis.json(afterInputs)), engine);
        Evidence evidence = readEvidence(Path.of(args[3]), after.contexts());
        Map<String, Object> report = LookaheadSnapshot.compare(before, after, evidence.bindings);
        report.put("baseCommit", base);
        report.put("watchedBefore", beforeInputs);
        report.put("watchedCurrent", afterInputs);
        report.put("guarantee",
                "Tracked-context change detection only; no automatic rewrite or semantic equivalence claim.");
        Files.writeString(output.resolve("report.json"), LookaheadAnalysis.json(report) + "\n");
        Files.write(output.resolve("binding-candidates.tsv"), evidence.proposed);
        System.out.println("Lookahead report: " + output.resolve("report.json"));
        if (args.length == 5 && Boolean.parseBoolean(args[4])
                && Boolean.TRUE.equals(report.get("needsReview"))) {
            throw new IllegalStateException("Lookahead contexts require review; see report.json");
        }
    }

    private static String baseCommit(Path root, String argument) throws Exception {
        if (!git(root, "rev-parse", "--show-object-format").trim().equals("sha1")) {
            throw new IllegalArgumentException(
                    "This repository entry point requires Git SHA-1 objects");
        }
        String ref = argument.matches("0+") ? "HEAD" : argument;
        return git(root, "rev-parse", "--verify", "--end-of-options", ref + "^{commit}").trim();
    }

    private static Evidence readEvidence(Path path,
            Map<String, List<Map<String, Object>>> contexts) throws IOException {
        Evidence evidence = new Evidence();
        for (String line : Files.readAllLines(path)) {
            if (!line.isBlank() && !line.startsWith("#")) {
                evidence.bind(line, contexts);
            }
        }
        return evidence;
    }

    private static final class Evidence {
        private final Map<String, String> bindings = new TreeMap<>();
        private final List<String> proposed = new ArrayList<>();

        private void bind(String line, Map<String, List<Map<String, Object>>> contexts) {
            String[] fields = line.split("\t", 4);
            if (fields.length != 4 || fields[2].isBlank() || fields[3].isBlank()) {
                throw new IllegalArgumentException(
                        "Evidence requires key, fingerprint, reason and test reference");
            }
            List<String> matching = contexts.keySet().stream()
                    .filter(k -> k.equals(fields[0]) || k.startsWith(fields[0] + ":"))
                    .collect(Collectors.toList());
            boolean unique = matching.size() == 1;
            String key = unique ? matching.get(0) : "unbound:" + fields[0];
            bindings.put(key, fields[1]);
            if (unique && contexts.get(key).size() == 1) {
                proposed.add(fields[0] + "\t" + contexts.get(key).get(0).get("fingerprint") + "\t"
                        + fields[2] + "\t" + fields[3]);
            }
        }
    }

    static boolean watched(String path) {
        return path.startsWith("src/main/java/") || path.startsWith("src/main/resources/")
                || BUILD_INPUTS.contains(path);
    }

    static LookaheadSnapshot generate(String source, Path directory, String external, String engine)
            throws Exception {
        Files.createDirectories(directory);
        Path input = directory.resolve("JSqlParserCC.jjt");
        Path generated = directory.resolve("generated");

        Files.writeString(input, source);
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
        Process process = new ProcessBuilder(java.toString(), "-Dfile.encoding=UTF-8", "-cp",
                System.getProperty("java.class.path"),
                "org.javacc.jjtree.Main", "-GRAMMAR_ENCODING=UTF-8",
                "-OUTPUT_DIRECTORY=" + generated, input.toString())
                .redirectErrorStream(true).redirectOutput(directory.resolve("jjtree.log").toFile())
                .start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException(
                    "JJTree failed; see " + directory.resolve("jjtree.log"));
        }
        return LookaheadSnapshot.parse(Files.readString(generated.resolve("JSqlParserCC.jj")),
                external, engine);
    }

    private static Map<String, String> baseInputs(Path root, String base) throws Exception {
        Map<String, String> result = new TreeMap<>();
        for (String entry : git(root, "ls-tree", "-r", "-z", "--full-tree", base).split("\u0000")) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] fields = entry.split("\t", 2);
            String[] header = fields[0].split(" ");

            if (watched(fields[1])) {
                if (!header[1].equals("blob") || !header[0].startsWith("100")) {
                    throw new IllegalStateException("Unsupported watched input " + fields[1]);
                }
                result.put(fields[1], header[2]);
            }
        }
        return result;
    }

    private static Map<String, String> workingInputs(Path root) throws Exception {
        Map<String, String> result = new TreeMap<>();
        for (String folder : List.of("src/main/java", "src/main/resources")) {
            Path start = root.resolve(folder);
            if (!Files.exists(start)) {
                continue;
            }
            try (java.util.stream.Stream<Path> paths = Files.walk(start)) {
                for (Path path : paths.collect(Collectors.toList())) {
                    if (Files.isSymbolicLink(path)) {
                        throw new IllegalStateException("Symlink in watched inputs: " + path);
                    }
                    if (Files.isRegularFile(path)) {
                        addInput(root, path, result);
                    }
                }
            }
        }
        for (String file : BUILD_INPUTS) {
            if (Files.exists(root.resolve(file))) {
                addInput(root, root.resolve(file), result);
            }
        }
        return result;
    }

    private static void addInput(Path root, Path path, Map<String, String> result)
            throws Exception {
        if (Files.isSymbolicLink(path)) {
            throw new IllegalStateException("Symlink in watched inputs: " + path);
        }
        byte[] bytes = Files.readAllBytes(path);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(("blob " + bytes.length + "\0").getBytes(StandardCharsets.US_ASCII));
        StringBuilder hash = new StringBuilder();
        for (byte b : digest.digest(bytes)) {
            hash.append(String.format(Locale.ROOT, "%02x", b));
        }
        result.put(root.relativize(path).toString().replace('\\', '/'), hash.toString());
    }

    private static String engine(Path root) throws Exception {
        Map<String, String> inputs = new TreeMap<>();
        try (java.util.stream.Stream<Path> files = Files.walk(root.resolve(TOOL))) {
            for (Path path : files.filter(Files::isRegularFile).collect(Collectors.toList())) {
                inputs.put(root.relativize(path).toString().replace('\\', '/'),
                        LookaheadAnalysis.sha(Files.readAllBytes(path)));
            }
        }
        int generators = 0;
        for (String entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            Path artifact = Path.of(entry);
            String name = artifact.getFileName().toString();
            if (name.startsWith("javacc-") && name.endsWith(".jar")) {
                inputs.put(name, LookaheadAnalysis.sha(Files.readAllBytes(artifact)));
                generators++;
            }
        }
        if (generators < 2) {
            throw new IllegalStateException("Expected both JavaCC core and Java generator JARs");
        }
        return LookaheadAnalysis.sha(LookaheadAnalysis.json(inputs));
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git", "-C", root.toString()));
        command.addAll(List.of(arguments));
        Process process =
                new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        byte[] output = process.getInputStream().readAllBytes();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Git read failed: " + command);
        }
        return new String(output, StandardCharsets.UTF_8);
    }
}
