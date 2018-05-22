/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.util.regex.*;

public final class Server implements MultiPartName {

    public static final Pattern SERVER_PATTERN = Pattern.
            compile("\\[([^\\]]+?)(?:\\\\([^\\]]+))?\\]");

    private String serverName;
    private String instanceName;
    private String simpleName;

    public Server(String serverAndInstanceName) {
        if (serverAndInstanceName != null) {
            final Matcher matcher = SERVER_PATTERN.matcher(serverAndInstanceName);
            if (!matcher.find()) {
                simpleName = serverAndInstanceName;
            } else {
                setServerName(matcher.group(1));
                setInstanceName(matcher.group(2));
            }
        }
    }

    public Server(String serverName, String instanceName) {
        setServerName(serverName);
        setInstanceName(instanceName);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public String getFullyQualifiedName() {
        if (serverName != null && !serverName.isEmpty() && instanceName != null && !instanceName.
                isEmpty()) {
            return String.format("[%s\\%s]", serverName, instanceName);
        } else if (serverName != null && !serverName.isEmpty()) {
            return String.format("[%s]", serverName);
        } else if (simpleName != null && !simpleName.isEmpty()) {
            return simpleName;
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }
}
