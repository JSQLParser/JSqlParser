/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.view;

import java.io.Serializable;
import net.sf.jsqlparser.statement.MySqlDefiner;

/** Prefix options shared by MySQL CREATE VIEW and ALTER VIEW. */
public class MySqlViewOptions implements Serializable {
    public enum Algorithm {
        UNDEFINED, MERGE, TEMPTABLE
    }

    public enum Security {
        DEFINER, INVOKER
    }

    private Algorithm algorithm;
    private MySqlDefiner definer;
    private Security security;

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public MySqlDefiner getDefiner() {
        return definer;
    }

    public void setDefiner(MySqlDefiner definer) {
        this.definer = definer;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public void appendTo(StringBuilder builder) {
        if (algorithm != null) {
            builder.append("ALGORITHM = ").append(algorithm).append(' ');
        }
        if (definer != null) {
            builder.append("DEFINER = ").append(definer).append(' ');
        }
        if (security != null) {
            builder.append("SQL SECURITY ").append(security).append(' ');
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql);
        return sql.toString().trim();
    }
}
