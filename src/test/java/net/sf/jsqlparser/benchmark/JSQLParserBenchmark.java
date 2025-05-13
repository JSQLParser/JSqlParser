/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.benchmark;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statements;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class JSQLParserBenchmark {

    private String sqlContent;
    private ExecutorService executorService;

    SqlParserRunner runner;

    // @Param({ "latest", "5.2", "5.1", "5.0", "4.9", "4.8", "4.7", "4.6", "4.5" })
    @Param({"latest", "5.2", "5.1"})
    public String version;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        if ("latest".equals(version)) {
            runner = new LatestClasspathRunner(); // direct call, no reflection
        } else {
            Path jarPath = downloadJsqlparserJar(version);
            URLClassLoader loader = new URLClassLoader(new URL[] {jarPath.toUri().toURL()}, null);
            runner = new DynamicParserRunner(loader);
        }

        // Adjust path as necessary based on where source root is during test execution
        Path path = Paths.get("src/test/resources/net/sf/jsqlparser/performance.sql");
        sqlContent = Files.readString(path, StandardCharsets.UTF_8);
        executorService = Executors.newSingleThreadExecutor();
    }

    private Path downloadJsqlparserJar(String version) throws IOException {
        String jarUrl = String.format(
                "https://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/%s/jsqlparser-%s.jar",
                version, version);

        Path cacheDir = Paths.get("build/libs/downloaded-jars");
        Files.createDirectories(cacheDir);
        Path jarFile = cacheDir.resolve("jsqlparser-" + version + ".jar");

        if (!Files.exists(jarFile)) {
            System.out.println("Downloading " + version);
            try (InputStream in = new URL(jarUrl).openStream()) {
                Files.copy(in, jarFile);
            }
        }

        return jarFile;
    }

    @Benchmark
    public void parseSQLStatements() throws Exception {
        final Statements statements = runner.parseStatements(
                sqlContent,
                executorService,
                (Consumer<CCJSqlParser>) parser -> {
                    // No-op consumer (or you can log/validate each parser if desired)
                });
        assert statements.size() == 4;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        executorService.shutdown();
    }
}
