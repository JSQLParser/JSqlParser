/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Locale;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public enum PurgeObjectType {
    TABLE, INDEX, RECYCLEBIN, DBA_RECYCLEBIN, TABLESPACE,
    /** MySQL and MariaDB {@code PURGE BINARY LOGS} */
    BINARY_LOGS,
    /** MariaDB {@code PURGE MASTER LOGS}, which MySQL 8.4 removed */
    MASTER_LOGS;

    public static PurgeObjectType from(String type) {
        return Enum.valueOf(PurgeObjectType.class, type.toUpperCase(Locale.ROOT));
    }
}
