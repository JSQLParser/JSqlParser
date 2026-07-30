/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.net.URISyntaxException;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Verifies that no method in the generated parser or token manager exceeds the JVM's 64KB bytecode
 * limit per method.
 *
 * <p>
 * Catches the exact failure scenario:
 * </p>
 *
 * <pre>
 * org.objectweb.asm.MethodTooLargeException:
 *   Method too large: net/sf/jsqlparser/parser/CCJSqlParserTokenManager.&lt;clinit&gt; ()V
 * </pre>
 *
 * <p>
 * This error occurs when bytecode instrumentation tools (JaCoCo, ASM, ByteBuddy) add probes to
 * methods that are already near the 64KB limit. The tests therefore enforce a <b>safety margin</b>
 * ({@value #INSTRUMENTATION_HEADROOM_PERCENT}%) below the hard JVM limit.
 * </p>
 *
 * <p>
 * Requires {@link BytecodeSizeVerifier} on the test classpath (same package).
 * </p>
 */
public class BytecodeSizeTest {

    /**
     * Safety margin for bytecode instrumentation (JaCoCo, ASM, etc.). Instrumentation typically
     * adds 5-15% overhead; 20% gives comfortable headroom. A method at 80% of 64KB (52,428 bytes)
     * will still be safe after instrumentation.
     */
    private static final int INSTRUMENTATION_HEADROOM_PERCENT = 20;

    /**
     * Hard fail threshold: 100% minus instrumentation headroom. Methods exceeding this will likely
     * break under JaCoCo/ASM instrumentation.
     */
    private static final int FAIL_PERCENT = 100 - INSTRUMENTATION_HEADROOM_PERCENT;

    /**
     * Warn threshold — earlier alert for methods approaching danger zone.
     */
    private static final int WARN_PERCENT = 50;

    private static final int FAIL_THRESHOLD =
            (int) (BytecodeSizeVerifier.JVM_CODE_LIMIT * (FAIL_PERCENT / 100.0));

    /**
     * The generated parser/token manager classes to check. Inner classes (e.g. CharDataConsts) are
     * checked automatically since we scan the entire classes directory.
     */
    private static final List<String> CRITICAL_CLASSES = List.of(
            "CCJSqlParser",
            "CCJSqlParserTokenManager");

    /**
     * Standard build output locations, tried in order if the code source of the generated parser
     * cannot be resolved (e.g. when it is loaded from a JAR).
     */
    private static final List<String> FALLBACK_CLASSES_DIRS = List.of(
            "build/classes/java/main",
            "target/classes",
            "build/classes/main",
            "out/production/classes");

    // -----------------------------------------------------------------------
    // Test: clinit specifically (matches the reported ASM error)
    // -----------------------------------------------------------------------

    /**
     * Reproduces the exact failure scenario:
     *
     * <pre>
     * org.objectweb.asm.MethodTooLargeException:
     *   Method too large: net/sf/jsqlparser/parser/CCJSqlParserTokenManager.&lt;clinit&gt; ()V
     * </pre>
     *
     * <p>
     * Checks {@code <clinit>} in all parser-related classes (including inner classes like
     * {@code CharDataConsts}) against the instrumentation-safe threshold.
     * </p>
     */
    @Test
    void clinitMustFitWithinInstrumentationSafeLimit() throws Exception {
        List<BytecodeSizeVerifier.Result> results = scanParserClasses();

        List<String> failures = new ArrayList<>();

        for (BytecodeSizeVerifier.Result r : results) {
            if (r.clinitSize > 0) {
                double pct = (r.clinitSize * 100.0) / BytecodeSizeVerifier.JVM_CODE_LIMIT;
                System.err.printf("  <clinit> %-60s %,6d bytes (%5.1f%%)%n",
                                  r.className, r.clinitSize, pct);

                if (r.clinitSize > FAIL_THRESHOLD) {
                    failures.add(String.format(
                            "%s.<clinit>: %,d bytes (%.1f%%) exceeds %d%% safe limit.%n"
                                    + "  ASM/JaCoCo instrumentation will push this over 64KB.%n"
                                    + "  Fix: move static array initializers to _init() methods.",
                            r.className, r.clinitSize, pct, FAIL_PERCENT));
                }
            }
        }

        assertTrue(failures.isEmpty(),
                   () -> "Static initializer(s) too large for safe instrumentation:"
                                 + System.lineSeparator() + String.join(System.lineSeparator(), failures));
    }

    // -----------------------------------------------------------------------
    // Test: all methods (general code-too-large prevention)
    // -----------------------------------------------------------------------

    /**
     * Checks every method in the generated parser and token manager classes against the
     * instrumentation-safe threshold.
     *
     * <p>
     * Covers: production methods, jj_3R_* lookahead scanners, jj_rescan_token, jj_la1_init_*, and
     * all other generated methods.
     * </p>
     */
    @Test
    void allMethodsMustFitWithinInstrumentationSafeLimit() throws Exception {
        List<BytecodeSizeVerifier.Result> results = scanParserClasses();

        List<String> failures = new ArrayList<>();
        int totalMethods = 0;

        for (BytecodeSizeVerifier.Result r : results) {
            totalMethods += r.allMethods.size();
            for (BytecodeSizeVerifier.MethodInfo m : r.allMethods) {
                if (m.codeSize > FAIL_THRESHOLD) {
                    failures.add(String.format(
                            "%s.%s%s: %,d bytes (%.1f%%) exceeds %d%% safe limit",
                            m.className, m.methodName, m.descriptor,
                            m.codeSize, m.percentOfLimit(), FAIL_PERCENT));
                }
            }
        }

        // Always log the top-10 largest methods for monitoring
        System.err.println();
        System.err.println("Top 10 largest methods across parser classes:");
        results.stream()
               .flatMap(r -> r.allMethods.stream())
               .sorted((a, b) -> Integer.compare(b.codeSize, a.codeSize))
               .limit(10)
               .forEach(m -> System.err.printf("  %6d bytes (%5.1f%%) %s.%s%s%n",
                                               m.codeSize, m.percentOfLimit(),
                                               m.className, m.methodName, m.descriptor));
        System.err.printf("%nScanned %d methods in %d classes (fail threshold: %d%% = %,d bytes)%n",
                          totalMethods, results.size(), FAIL_PERCENT, FAIL_THRESHOLD);

        assertTrue(failures.isEmpty(),
                   () -> failures.size() + " method(s) too large for safe instrumentation:"
                                 + System.lineSeparator() + String.join(System.lineSeparator(), failures));
    }

    // -----------------------------------------------------------------------
    // Test: named critical classes must be found
    // -----------------------------------------------------------------------

    /**
     * Ensures the critical parser classes actually exist in the build output. Catches misconfigured
     * build paths that would otherwise silently skip all checks.
     */
    @Test
    void criticalClassesMustBePresent() throws Exception {
        Path classesDir = requireClassesDir();

        for (String className : CRITICAL_CLASSES) {
            boolean found;
            try (Stream<Path> walk = Files.walk(classesDir)) {
                found = walk.anyMatch(p -> p.getFileName().toString().equals(className + ".class"));
            }
            assertTrue(found,
                       className + ".class not found under " + classesDir
                               + " — check build configuration");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Scan all parser-related .class files (including inner classes).
     */
    private List<BytecodeSizeVerifier.Result> scanParserClasses() throws Exception {
        Path classesDir = requireClassesDir();

        List<BytecodeSizeVerifier.Result> results =
                BytecodeSizeVerifier.verifyDirectory(classesDir, WARN_PERCENT);

        assertFalse(results.isEmpty(), "No .class files found in " + classesDir);
        return results;
    }

    /**
     * Locate the compiled main classes directory, failing the test if it cannot be found. A missing
     * directory means the check would silently pass without verifying anything, which is worse than
     * a red build.
     */
    private Path requireClassesDir() {
        Path classesDir = findClassesDir();
        assertNotNull(classesDir,
                      "Compiled main classes directory not found. Tried the code source of "
                              + CCJSqlParser.class.getSimpleName() + " and " + FALLBACK_CLASSES_DIRS
                              + " relative to " + Path.of("").toAbsolutePath()
                              + " — check build configuration");
        return classesDir;
    }

    /**
     * Locate the compiled classes directory.
     *
     * <p>
     * Primary strategy: ask the generated parser class where its own code source is. That is by
     * definition the directory holding the classes under test, so it needs no assumptions about
     * Gradle vs. Maven layout or about {@code main} and {@code test} output being siblings.
     * </p>
     *
     * <p>
     * Note {@link CodeSource#getLocation()} must be converted via {@code Path.of(URI)} rather than
     * {@code Path.of(url.getPath())}: on Windows the URL path of a local file carries a leading
     * slash before the drive letter ({@code /C:/...}), which {@code Path.of(String)} rejects with
     * {@link InvalidPathException}. {@code getPath()} also leaves percent-escapes undecoded, so a
     * space in the checkout path would break on every platform.
     * </p>
     *
     * @return the classes directory, or {@code null} if it cannot be determined
     */
    private Path findClassesDir() {
        try {
            CodeSource codeSource =
                    CCJSqlParser.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                Path location = Path.of(codeSource.getLocation().toURI());
                if (Files.isDirectory(location)) {
                    return location;
                }
            }
        } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException
                 | SecurityException e) {
            // Classes may come from a JAR or an exotic class loader; fall through to the scan.
            System.err.println("Could not resolve code source location: " + e);
        }

        for (String candidate : FALLBACK_CLASSES_DIRS) {
            Path p = Path.of(candidate);
            if (Files.isDirectory(p)) {
                return p;
            }
        }
        return null;
    }
}