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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class JSQLParserBenchmark {

    private String sqlContent;
    private ExecutorService executorService;

    SqlParserRunner runner;

    // @Param({ "latest", "5.2", "5.1", "5.0", "4.9", "4.8", "4.7", "4.6", "4.5" })
    @Param({"latest", "5.3", "5.1"})
    public String version;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        if ("latest".equals(version)) {
            runner = new LatestClasspathRunner(); // direct call, no reflection
        } else {
            Path jarPath = downloadJsqlparserJar(version);
            URLClassLoader loader = new URLClassLoader(new URL[] {jarPath.toUri().toURL()}, null);
            try {
                runner = new DynamicParserRunner(loader);
            } catch (Exception ex) {
                loader.close();
                throw ex;
            }
        }

        // Reject incompatible SQL/configurations before collecting measurements.
        try {
            Path path = Paths.get("src/test/resources/net/sf/jsqlparser/performance.sql");
            sqlContent = Files.readString(path, StandardCharsets.UTF_8);
            executorService = Executors.newSingleThreadExecutor();
            Object statements = runner.parseStatements(sqlContent, executorService,
                    SqlParserRunner.Configuration.SIMPLE);
            if (statements == null) {
                throw new IllegalStateException("Parser " + version
                        + " returned no statements for the benchmark corpus with SIMPLE configuration");
            }
        } catch (Exception ex) {
            try {
                tearDown();
            } catch (Exception cleanup) {
                ex.addSuppressed(cleanup);
            }
            throw ex;
        }
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
    public void parseSQLStatements(Blackhole blackhole) throws Exception {
        final Object statements = runner.parseStatements(
                sqlContent,
                executorService,
                SqlParserRunner.Configuration.SIMPLE);
        blackhole.consume(statements);
    }

    // @Benchmark
    public void parseQuotedText(Blackhole blackhole) throws Exception {
        String sqlStr = "SELECT ('\\'', 'a');\n"
                + "INSERT INTO recycle_record (a,f) VALUES ('\\'anything', 'abc');\n"
                + "INSERT INTO recycle_record (a,f) VALUES ('\\'','83653692186728700711687663398101');\n";

        final Object statements = runner.parseStatements(
                sqlStr,
                executorService,
                SqlParserRunner.Configuration.BACKSLASH_ESCAPES);
        blackhole.consume(statements);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        try {
            if (executorService != null) {
                executorService.shutdownNow();
            }
        } finally {
            if (runner != null) {
                runner.close();
            }
        }
    }
}
