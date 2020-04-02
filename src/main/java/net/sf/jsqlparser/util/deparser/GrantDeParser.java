/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.grant.Grant;

import java.util.Iterator;

public class GrantDeParser {
    protected StringBuilder buffer;

    public GrantDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(Grant grant) {
        buffer.append("GRANT ");
        if (grant.getRole() != null) {
            buffer.append(grant.getRole());
        } else {
            for (Iterator<String> iter = grant.getPrivileges().iterator(); iter.hasNext(); ) {
                String privilege = iter.next();
                buffer.append(privilege);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(" ON ");
            buffer.append(grant.getObjectName());
        }
        buffer.append(" TO ");
        for (Iterator<String> iter = grant.getUsers().iterator(); iter.hasNext(); ) {
            String user = iter.next();
            buffer.append(user);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

}
