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

import java.util.Objects;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see <a href=
 *      "https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_9018.htm">Purge</a>
 */

public class PurgeStatement implements Statement {
    private final PurgeObjectType purgeObjectType;
    private final Object object;
    private String userName;

    public PurgeStatement(Table table) {
        this.purgeObjectType = PurgeObjectType.TABLE;
        this.object = Objects.requireNonNull(table,
                "The TABLE of the PURGE TABLE statement must not be null.");
    }

    public PurgeStatement(Index index) {
        this.purgeObjectType = PurgeObjectType.INDEX;
        this.object = Objects.requireNonNull(index,
                "The INDEX of the PURGE INDEX statement must not be null.");
    }

    public PurgeStatement(PurgeObjectType purgeObjectType) {
        this.purgeObjectType = purgeObjectType;
        this.object = null;
    }

    public PurgeStatement(PurgeObjectType purgeObjectType, String tableSpaceName, String userName) {
        this.purgeObjectType = purgeObjectType;
        this.object = Objects.requireNonNull(tableSpaceName,
                "The TABLESPACE NAME of the PURGE TABLESPACE statement must not be null.");
        this.userName = userName;
    }

    @Override
    public <T> T accept(StatementVisitor<T> statementVisitor) {
        return statementVisitor.visit(this);
    }

    @SuppressWarnings({"PMD.MissingBreakInSwitch", "PMD.SwitchStmtsShouldHaveDefault",
            "PMD.CyclomaticComplexity"})
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("PURGE ");

        switch (purgeObjectType) {
            case RECYCLEBIN:
            case DBA_RECYCLEBIN:
                builder.append(purgeObjectType);
                break;
            case TABLE:
            case INDEX:
                builder.append(purgeObjectType);
                if (object != null) {
                    builder.append(" ").append(object);
                }
                break;
            case TABLESPACE:
                builder.append(purgeObjectType);
                if (object != null) {
                    builder.append(" ").append(object);
                }
                if (userName != null && userName.length() > 0) {
                    builder.append(" USER ").append(userName);
                }
                break;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public PurgeObjectType getPurgeObjectType() {
        return purgeObjectType;
    }

    public Object getObject() {
        return object;
    }
}
