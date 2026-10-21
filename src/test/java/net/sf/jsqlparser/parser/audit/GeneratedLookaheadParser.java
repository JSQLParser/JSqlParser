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

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.javacc.parser.Context;

/** Exercises the actual generator and generated parser instead of a model of lookahead. */
final class GeneratedLookaheadParser implements AutoCloseable {
    private final URLClassLoader loader;

    GeneratedLookaheadParser(Path directory, String grammar) throws Exception {
        Files.createDirectories(directory);
        Path input = directory.resolve("Fixture.jj");
        Files.writeString(input, grammar);
        run(directory, "UTF-8", "org.javacc.parser.Main", "-GRAMMAR_ENCODING=UTF-8",
                "-OUTPUT_DIRECTORY=" + directory, input.toString());
        List<String> arguments =
                new ArrayList<>(List.of(tool("javac"), "--release", "11", "-encoding", "UTF-8",
                        "-d", directory.toString()));
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            arguments.addAll(files.filter(p -> p.toString().endsWith(".java")).map(Path::toString)
                    .collect(Collectors.toList()));
        }
        execute(directory, "javac", arguments);
        loader = new URLClassLoader(new java.net.URL[] {directory.toUri().toURL()},
                GeneratedLookaheadParser.class.getClassLoader());
    }

    boolean accepts(String input) throws Exception {
        Class<?> parser = loader.loadClass("Fixture");
        Object instance = parser.getConstructor(java.io.Reader.class)
                .newInstance(new StringReader(input));
        try {
            parser.getMethod("Entry").invoke(instance);
            return true;
        } catch (InvocationTargetException error) {
            String type = error.getCause().getClass().getSimpleName();
            if (type.equals("ParseException") || type.equals("TokenMgrError")) {
                return false;
            }
            throw error;
        }
    }

    static void run(Path directory, String encoding, String main, String... arguments)
            throws Exception {
        List<String> command =
                new ArrayList<>(List.of(tool("java"), "-Dfile.encoding=" + encoding, "-cp",
                        classpath(), main));
        command.addAll(List.of(arguments));
        execute(directory, main, command);
    }

    private static String tool(String name) {
        String executable =
                System.getProperty("os.name").startsWith("Windows") ? name + ".exe" : name;
        return Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }

    private static void execute(Path directory, String name, List<String> command)
            throws Exception {
        Path log = directory.resolve(name + ".log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Generator failed: " + Files.readString(log));
        }
    }

    private static String classpath() throws Exception {
        List<String> entries = new ArrayList<>();
        for (Class<?> type : List.of(LookaheadAudit.class, Context.class,
                Class.forName("org.javacc.java.JavaCodeGenerator"))) {
            entries.add(Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toString());
        }
        return String.join(File.pathSeparator, entries);
    }

    @Override
    public void close() throws Exception {
        loader.close();
    }
}
