package net.sf.jsqlparser.parser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.*;

/**
 * Zero-dependency bytecode verifier that scans compiled .class files and reports methods
 * approaching or exceeding the JVM 64KB code size limit.
 *
 * Also checks clinit (static initializer) sizes separately since large static initializers are a
 * common problem with generated parsers.
 *
 * Usage as standalone tool: java BytecodeSizeVerifier path/to/classes [--warn-pct 75] [--fail]
 *
 * Usage as library (from JUnit test): BytecodeSizeVerifier.Result r =
 * BytecodeSizeVerifier.verify(classFile, 80); assertTrue(r.violations.isEmpty(), r.report());
 */
public class BytecodeSizeVerifier {

    /** JVM hard limit: 65535 bytes of bytecode per method */
    public static final int JVM_CODE_LIMIT = 65535;

    /** Method info from a .class file */
    public static final class MethodInfo {
        public final String className;
        public final String methodName;
        public final String descriptor;
        public final int codeSize;

        MethodInfo(String className, String methodName, String descriptor, int codeSize) {
            this.className = className;
            this.methodName = methodName;
            this.descriptor = descriptor;
            this.codeSize = codeSize;
        }

        public double percentOfLimit() {
            return (codeSize * 100.0) / JVM_CODE_LIMIT;
        }

        @Override
        public String toString() {
            return String.format("%s.%s%s: %,d bytes (%.1f%%)",
                    className, methodName, descriptor, codeSize, percentOfLimit());
        }
    }

    /** Verification result for one .class file */
    public static final class Result {
        public final String classFile;
        public final String className;
        public final List<MethodInfo> allMethods = new ArrayList<>();
        public final List<MethodInfo> violations = new ArrayList<>();
        public final List<MethodInfo> warnings = new ArrayList<>();
        public int clinitSize = -1;

        Result(String classFile, String className) {
            this.classFile = classFile;
            this.className = className;
        }

        /** Generate a human-readable report */
        public String report() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(className).append(" (").append(classFile).append(") ===\n");
            sb.append(String.format("  Total methods: %d\n", allMethods.size()));
            if (clinitSize >= 0) {
                sb.append(String.format("  <clinit>: %,d bytes (%.1f%% of 64KB limit)\n",
                        clinitSize, (clinitSize * 100.0) / JVM_CODE_LIMIT));
            }
            if (!violations.isEmpty()) {
                sb.append("  VIOLATIONS (exceed 64KB limit):\n");
                for (MethodInfo m : violations) {
                    sb.append(String.format("    %s%s: %,d bytes (%.1f%%)\n",
                            m.methodName, m.descriptor, m.codeSize, m.percentOfLimit()));
                }
            }
            if (!warnings.isEmpty()) {
                sb.append("  WARNINGS (approaching limit):\n");
                for (MethodInfo m : warnings) {
                    sb.append(String.format("    %s%s: %,d bytes (%.1f%%)\n",
                            m.methodName, m.descriptor, m.codeSize, m.percentOfLimit()));
                }
            }
            List<MethodInfo> sorted = allMethods.stream()
                    .sorted((a, b) -> Integer.compare(b.codeSize, a.codeSize))
                    .limit(10)
                    .collect(Collectors.toList());
            if (!sorted.isEmpty()) {
                sb.append("  Top 10 largest methods:\n");
                for (MethodInfo m : sorted) {
                    sb.append(String.format("    %6d bytes (%5.1f%%) %s%s\n",
                            m.codeSize, m.percentOfLimit(), m.methodName, m.descriptor));
                }
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return report();
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Verify a single .class file.
     * 
     * @param classFile path to the .class file
     * @param warnPercent warn when method exceeds this % of the 64KB limit (e.g. 75)
     */
    public static Result verify(Path classFile, int warnPercent) throws IOException {
        byte[] data = Files.readAllBytes(classFile);
        return verify(classFile.toString(), data, warnPercent);
    }

    /**
     * Verify class bytes directly.
     * 
     * @param name descriptive name for reporting
     * @param classBytes raw .class file bytes
     * @param warnPercent warn threshold (0-100)
     */
    public static Result verify(String name, byte[] classBytes, int warnPercent)
            throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(classBytes));

        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IOException("Not a valid .class file: " + name);
        }

        in.readUnsignedShort(); // minor version
        in.readUnsignedShort(); // major version

        // Constant pool
        int cpCount = in.readUnsignedShort();
        String[] cpUtf8 = new String[cpCount];
        int[] cpClassNameIdx = new int[cpCount];

        for (int i = 1; i < cpCount; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 1: // CONSTANT_Utf8
                    cpUtf8[i] = in.readUTF();
                    break;
                case 3:
                case 4: // Integer, Float
                    in.readInt();
                    break;
                case 5:
                case 6: // Long, Double
                    in.readLong();
                    i++; // takes two CP slots
                    break;
                case 7: // CONSTANT_Class
                    cpClassNameIdx[i] = in.readUnsignedShort();
                    break;
                case 8:
                case 16:
                case 19:
                case 20:
                    // String, MethodType, Module, Package
                    in.readUnsignedShort();
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18:
                    // Fieldref, Methodref, InterfaceMethodref, NameAndType, Dynamic, InvokeDynamic
                    in.readUnsignedShort();
                    in.readUnsignedShort();
                    break;
                case 15: // MethodHandle
                    in.readUnsignedByte();
                    in.readUnsignedShort();
                    break;
                default:
                    throw new IOException(
                            "Unknown CP tag: " + tag + " at index " + i + " in " + name);
            }
        }

        in.readUnsignedShort(); // access flags

        // This class name
        int thisClassIdx = in.readUnsignedShort();
        String className = name;
        if (thisClassIdx > 0 && thisClassIdx < cpCount) {
            int ni = cpClassNameIdx[thisClassIdx];
            if (ni > 0 && ni < cpCount && cpUtf8[ni] != null) {
                className = cpUtf8[ni].replace('/', '.');
            }
        }

        Result result = new Result(name, className);

        in.readUnsignedShort(); // super class

        int ifCount = in.readUnsignedShort();
        for (int i = 0; i < ifCount; i++) {
            in.readUnsignedShort();
        }

        // Fields - skip
        int fieldCount = in.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            in.readUnsignedShort(); // access
            in.readUnsignedShort(); // name
            in.readUnsignedShort(); // descriptor
            skipAttributes(in);
        }

        // Methods
        int methodCount = in.readUnsignedShort();
        int warnThreshold = (int) (JVM_CODE_LIMIT * (warnPercent / 100.0));

        for (int i = 0; i < methodCount; i++) {
            in.readUnsignedShort(); // access flags
            int nameIdx = in.readUnsignedShort();
            int descIdx = in.readUnsignedShort();
            String methodName = safeUtf8(cpUtf8, nameIdx, "#" + nameIdx);
            String descriptor = safeUtf8(cpUtf8, descIdx, "");

            int codeSize = readMethodCodeSize(in, cpUtf8);

            if (codeSize > 0) {
                MethodInfo info = new MethodInfo(className, methodName, descriptor, codeSize);
                result.allMethods.add(info);

                if ("<clinit>".equals(methodName)) {
                    result.clinitSize = codeSize;
                }

                if (codeSize > JVM_CODE_LIMIT) {
                    result.violations.add(info);
                } else if (codeSize > warnThreshold) {
                    result.warnings.add(info);
                }
            }
        }

        return result;
    }

    /**
     * Scan a directory tree for .class files and verify all of them.
     */
    public static List<Result> verifyDirectory(Path dir, int warnPercent) throws IOException {
        List<Result> results = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                if (file.toString().endsWith(".class")) {
                    results.add(verify(file, warnPercent));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return results;
    }

    // -----------------------------------------------------------------------
    // Classfile parsing helpers
    // -----------------------------------------------------------------------

    private static String safeUtf8(String[] pool, int idx, String fallback) {
        return (idx > 0 && idx < pool.length && pool[idx] != null) ? pool[idx] : fallback;
    }

    private static int readMethodCodeSize(DataInputStream in, String[] cpUtf8) throws IOException {
        int attrCount = in.readUnsignedShort();
        int codeSize = -1;
        for (int a = 0; a < attrCount; a++) {
            int attrNameIdx = in.readUnsignedShort();
            int attrLen = in.readInt();
            String attrName = safeUtf8(cpUtf8, attrNameIdx, null);

            if ("Code".equals(attrName)) {
                in.readUnsignedShort(); // max_stack
                in.readUnsignedShort(); // max_locals
                codeSize = in.readInt(); // code_length
                skipNBytes(in, codeSize);
                int excCount = in.readUnsignedShort();
                skipNBytes(in, excCount * 8L);
                skipAttributes(in); // Code sub-attributes
            } else {
                skipNBytes(in, attrLen);
            }
        }
        return codeSize;
    }

    private static void skipAttributes(DataInputStream in) throws IOException {
        int count = in.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            in.readUnsignedShort(); // name index
            int len = in.readInt();
            skipNBytes(in, len);
        }
    }

    private static void skipNBytes(DataInputStream in, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            int toSkip = (int) Math.min(remaining, 8192);
            int skipped = in.skipBytes(toSkip);
            if (skipped <= 0) {
                in.readByte();
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java BytecodeSizeVerifier <path> [--warn-pct N] [--fail]");
            System.err.println("  <path>       .class file or directory to scan");
            System.err.println("  --warn-pct N warn when method exceeds N% of 64KB (default: 75)");
            System.err.println("  --fail       exit with code 1 if any violations found");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        int warnPct = 75;
        boolean failOnViolation = false;
        for (int i = 1; i < args.length; i++) {
            if ("--warn-pct".equals(args[i]) && i + 1 < args.length) {
                warnPct = Integer.parseInt(args[++i]);
            } else if ("--fail".equals(args[i])) {
                failOnViolation = true;
            }
        }

        List<Result> results;
        if (Files.isDirectory(path)) {
            results = verifyDirectory(path, warnPct);
        } else {
            results = List.of(verify(path, warnPct));
        }

        boolean hasViolations = false;

        for (Result result : results) {
            if (!result.violations.isEmpty() || !result.warnings.isEmpty()) {
                System.out.println(result.report());
                if (!result.violations.isEmpty()) {
                    hasViolations = true;
                }
            }
        }

        int totalMethods = results.stream().mapToInt(res -> res.allMethods.size()).sum();
        int totalViolations = results.stream().mapToInt(res -> res.violations.size()).sum();
        int totalWarnings = results.stream().mapToInt(res -> res.warnings.size()).sum();

        System.out.printf("\nSummary: %d classes, %d methods, %d violations, %d warnings\n",
                results.size(), totalMethods, totalViolations, totalWarnings);

        if (hasViolations && failOnViolation) {
            System.exit(1);
        }
    }
}
