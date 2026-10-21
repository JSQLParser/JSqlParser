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
import java.nio.file.*;
import java.util.*;

/** Local, content-addressed data cache. Never deserializes JavaCC objects or executable objects. */
final class LookaheadCache {
    private static final String FORMAT = "lookahead-facts-2";
    private static final int LIMIT = 64 * 1024 * 1024;

    private LookaheadCache() {}

    static String key(String source, String engine) {
        return LookaheadAnalysis.sha(FORMAT + "\n" + engine + "\n" + source);
    }

    static LookaheadSnapshot read(Path directory, String key) {
        Path path = directory.resolve(key + ".facts");
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(path) > LIMIT) {
                return null;
            }
            try (DataInputStream input = new DataInputStream(Files.newInputStream(path))) {
                if (!FORMAT.equals(input.readUTF()) || !key.equals(input.readUTF())) {
                    return null;
                }
                String checksum = input.readUTF();
                byte[] bytes = readBytes(input);
                if (!checksum.equals(LookaheadAnalysis.sha(bytes)) || input.read() != -1) {
                    return null;
                }
                try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes))) {
                    LookaheadSnapshot snapshot = LookaheadSnapshot.fromCache(readValue(data, 0));
                    return data.read() == -1 ? snapshot : null;
                }
            }
        } catch (IOException | RuntimeException error) {
            // A truncated, incompatible or corrupted cache is a miss, never reusable evidence.
            return null;
        }
    }

    static void write(Path directory, String key, LookaheadSnapshot snapshot) throws IOException {
        Files.createDirectories(directory);
        if (Files.isSymbolicLink(directory)) {
            throw new IOException("Symlink cache directory: " + directory);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            writeValue(data, snapshot.cacheValue());
        }
        byte[] payload = bytes.toByteArray();
        if (payload.length > LIMIT) {
            throw new IOException("Lookahead cache exceeds size limit");
        }
        Path temporary = Files.createTempFile(directory, "facts-", ".tmp");
        try {
            try (DataOutputStream data =
                    new DataOutputStream(Files.newOutputStream(temporary))) {
                data.writeUTF(FORMAT);
                data.writeUTF(key);
                data.writeUTF(LookaheadAnalysis.sha(payload));
                data.writeInt(payload.length);
                data.write(payload);
            }
            try {
                Files.move(temporary, directory.resolve(key + ".facts"),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, directory.resolve(key + ".facts"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int size(DataInputStream input) throws IOException {
        int size = input.readInt();
        if (size < 0 || size > LIMIT) {
            throw new IOException("Invalid cache size");
        }
        return size;
    }

    private static byte[] readBytes(DataInputStream input) throws IOException {
        int expected = size(input);
        byte[] bytes = input.readNBytes(expected);
        if (bytes.length != expected) {
            throw new EOFException("Truncated cache value");
        }
        return bytes;
    }

    private static void writeValue(DataOutputStream output, Object value) throws IOException {
        if (value == null) {
            output.writeByte('n');
        } else if (value instanceof String) {
            output.writeByte('s');
            byte[] bytes = value.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
        } else if (value instanceof Boolean) {
            output.writeByte('b');
            output.writeBoolean((Boolean) value);
        } else if (value instanceof Integer) {
            output.writeByte('i');
            output.writeInt((Integer) value);
        } else if (value instanceof Map<?, ?>) {
            output.writeByte('m');
            output.writeInt(((Map<?, ?>) value).size());
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                writeValue(output, entry.getKey().toString());
                writeValue(output, entry.getValue());
            }
        } else if (value instanceof Collection<?>) {
            output.writeByte(value instanceof Set<?> ? 't' : 'l');
            output.writeInt(((Collection<?>) value).size());
            for (Object item : (Collection<?>) value) {
                writeValue(output, item);
            }
        } else {
            throw new IOException("Unsupported cache value: " + value.getClass());
        }
    }

    private static Object readValue(DataInputStream input, int depth) throws IOException {
        if (depth > 64) {
            throw new IOException("Invalid cache nesting");
        }
        int tag = input.readUnsignedByte();
        switch (tag) {
            case 'n':
                return null;
            case 's':
                return new String(readBytes(input),
                        java.nio.charset.StandardCharsets.UTF_8);
            case 'b':
                return input.readBoolean();
            case 'i':
                return input.readInt();
            case 'm':
                return readMap(input, depth);
            case 't':
            case 'l':
                return readCollection(input, depth, tag == 't');
            default:
                throw new IOException("Unknown cache value");
        }
    }

    private static Map<String, Object> readMap(DataInputStream input, int depth)
            throws IOException {
        Map<String, Object> result = new TreeMap<>();
        int size = size(input);
        for (int i = 0; i < size; i++) {
            Object key = readValue(input, depth + 1);
            if (!(key instanceof String) || result.containsKey(key)) {
                throw new IOException("Invalid cache key");
            }
            result.put((String) key, readValue(input, depth + 1));
        }
        return result;
    }

    private static Collection<Object> readCollection(DataInputStream input, int depth, boolean set)
            throws IOException {
        Collection<Object> result = set ? new LinkedHashSet<>() : new ArrayList<>();
        int size = size(input);
        for (int i = 0; i < size; i++) {
            result.add(readValue(input, depth + 1));
        }
        return result;
    }
}
