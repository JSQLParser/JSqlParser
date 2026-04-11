package net.sf.jsqlparser.parser;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Verifies that no method in the generated parser or token manager
 * exceeds the JVM's 64KB bytecode limit per method.
 *
 * <p>Catches the exact failure scenario:</p>
 * <pre>
 * org.objectweb.asm.MethodTooLargeException:
 *   Method too large: net/sf/jsqlparser/parser/CCJSqlParserTokenManager.&lt;clinit&gt; ()V
 * </pre>
 *
 * <p>This error occurs when bytecode instrumentation tools (JaCoCo, ASM, ByteBuddy)
 * add probes to methods that are already near the 64KB limit.  The tests therefore
 * enforce a <b>safety margin</b> ({@value #INSTRUMENTATION_HEADROOM_PERCENT}%)
 * below the hard JVM limit.</p>
 *
 * <p>Requires {@link BytecodeSizeVerifier} on the test classpath (same package).</p>
 */
public class BytecodeSizeTest {

    /**
     * Safety margin for bytecode instrumentation (JaCoCo, ASM, etc.).
     * Instrumentation typically adds 5-15% overhead; 20% gives comfortable headroom.
     * A method at 80% of 64KB (52,428 bytes) will still be safe after instrumentation.
     */
    private static final int INSTRUMENTATION_HEADROOM_PERCENT = 20;

    /**
     * Hard fail threshold: 100% minus instrumentation headroom.
     * Methods exceeding this will likely break under JaCoCo/ASM instrumentation.
     */
    private static final int FAIL_PERCENT = 100 - INSTRUMENTATION_HEADROOM_PERCENT;

    /**
     * Warn threshold — earlier alert for methods approaching danger zone.
     */
    private static final int WARN_PERCENT = 50;

    private static final int FAIL_THRESHOLD =
            (int) (BytecodeSizeVerifier.JVM_CODE_LIMIT * (FAIL_PERCENT / 100.0));

    /**
     * The generated parser/token manager classes to check.
     * Inner classes (e.g. CharDataConsts) are checked automatically
     * since we scan the entire classes directory.
     */
    private static final List<String> CRITICAL_CLASSES = List.of(
            "CCJSqlParser",
            "CCJSqlParserTokenManager"
    );

    // -----------------------------------------------------------------------
    // Test: clinit specifically (matches the reported ASM error)
    // -----------------------------------------------------------------------

    /**
     * Reproduces the exact failure scenario:
     * <pre>
     * org.objectweb.asm.MethodTooLargeException:
     *   Method too large: net/sf/jsqlparser/parser/CCJSqlParserTokenManager.&lt;clinit&gt; ()V
     * </pre>
     *
     * <p>Checks {@code <clinit>} in all parser-related classes (including inner classes
     * like {@code CharDataConsts}) against the instrumentation-safe threshold.</p>
     */
    @Test
    void clinitMustFitWithinInstrumentationSafeLimit() throws Exception {
        List<BytecodeSizeVerifier.Result> results = scanParserClasses();
        if (results.isEmpty()) return; // skip if classes not found

        List<String> failures = new ArrayList<>();

        for (BytecodeSizeVerifier.Result r : results) {
            if (r.clinitSize > 0) {
                double pct = (r.clinitSize * 100.0) / BytecodeSizeVerifier.JVM_CODE_LIMIT;
                System.err.printf("  <clinit> %-60s %,6d bytes (%5.1f%%)%n",
                        r.className, r.clinitSize, pct);

                if (r.clinitSize > FAIL_THRESHOLD) {
                    failures.add(String.format(
                            "%s.<clinit>: %,d bytes (%.1f%%) exceeds %d%% safe limit.\n"
                            + "  ASM/JaCoCo instrumentation will push this over 64KB.\n"
                            + "  Fix: move static array initializers to _init() methods.",
                            r.className, r.clinitSize, pct, FAIL_PERCENT));
                }
            }
        }

        assertTrue(failures.isEmpty(),
                "Static initializer(s) too large for safe instrumentation:\n"
                + String.join("\n", failures));
    }

    // -----------------------------------------------------------------------
    // Test: all methods (general code-too-large prevention)
    // -----------------------------------------------------------------------

    /**
     * Checks every method in the generated parser and token manager classes
     * against the instrumentation-safe threshold.
     *
     * <p>Covers: production methods, jj_3R_* lookahead scanners,
     * jj_rescan_token, jj_la1_init_*, and all other generated methods.</p>
     */
    @Test
    void allMethodsMustFitWithinInstrumentationSafeLimit() throws Exception {
        List<BytecodeSizeVerifier.Result> results = scanParserClasses();
        if (results.isEmpty()) return;

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
        System.err.println("\nTop 10 largest methods across parser classes:");
        results.stream()
                .flatMap(r -> r.allMethods.stream())
                .sorted((a, b) -> Integer.compare(b.codeSize, a.codeSize))
                .limit(10)
                .forEach(m -> System.err.printf("  %6d bytes (%5.1f%%) %s.%s%s%n",
                        m.codeSize, m.percentOfLimit(),
                        m.className, m.methodName, m.descriptor));
        System.err.printf("\nScanned %d methods in %d classes (fail threshold: %d%% = %,d bytes)%n",
                totalMethods, results.size(), FAIL_PERCENT, FAIL_THRESHOLD);

        assertTrue(failures.isEmpty(),
                failures.size() + " method(s) too large for safe instrumentation:\n"
                + String.join("\n", failures));
    }

    // -----------------------------------------------------------------------
    // Test: named critical classes must be found
    // -----------------------------------------------------------------------

    /**
     * Ensures the critical parser classes actually exist in the build output.
     * Catches misconfigured build paths that would silently skip all checks.
     */
    @Test
    void criticalClassesMustBePresent() throws Exception {
        Path classesDir = findClassesDir();
        if (classesDir == null) {
            System.err.println("WARNING: classes directory not found, skipping presence check");
            return;
        }

        for (String className : CRITICAL_CLASSES) {
            boolean found = false;
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
        Path classesDir = findClassesDir();
        if (classesDir == null) {
            System.err.println("WARNING: compiled classes directory not found, skipping bytecode checks");
            return Collections.emptyList();
        }

        List<BytecodeSizeVerifier.Result> results =
                BytecodeSizeVerifier.verifyDirectory(classesDir, WARN_PERCENT);

        assertFalse(results.isEmpty(), "No .class files found in " + classesDir);
        return results;
    }

    /**
     * Locate the compiled classes directory.
     * Tries standard Gradle and Maven layouts.
     */
    private Path findClassesDir() {
        // Try to infer from this test class's own location
        String thisClass = getClass().getName().replace('.', '/') + ".class";
        java.net.URL url = getClass().getClassLoader().getResource(thisClass);
        if (url != null && "file".equals(url.getProtocol())) {
            Path testClassFile = Path.of(url.getPath());
            Path classesRoot = testClassFile;
            for (int i = 0; i < thisClass.chars().filter(c -> c == '/').count() + 1; i++) {
                classesRoot = classesRoot.getParent();
            }
            // classesRoot = build/classes/java/test -> look for build/classes/java/main
            Path mainClasses = classesRoot.getParent().resolve("main");
            if (Files.isDirectory(mainClasses)) {
                return mainClasses;
            }
        }

        // Fallback: standard locations relative to working directory
        for (String candidate : new String[]{
                "build/classes/java/main",
                "target/classes",
                "build/classes/main",
                "out/production/classes"
        }) {
            Path p = Path.of(candidate);
            if (Files.isDirectory(p)) return p;
        }
        return null;
    }
}
