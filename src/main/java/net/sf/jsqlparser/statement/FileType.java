/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

public class FileType implements SourceDestinationType {
    private final Kind fileType;

    public FileType(String fileType) {
        this.fileType = Kind.valueOf(fileType.toUpperCase());
    }

    private enum Kind {
        CSV, FBV
    }

    @Override
    public String toString() {
        return fileType.toString();
    }
}
