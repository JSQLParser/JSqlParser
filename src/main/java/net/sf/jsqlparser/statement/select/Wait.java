/*
 * #%L JSQLParser library %% Copyright (C) 2004 - 2017 JSQLParser %% This program is free software:
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 2.1 of the License, or (at your
 * option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Lesser Public License for more details. You should have
 * received a copy of the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>. #L%
 */
package net.sf.jsqlparser.statement.select;

import lombok.Data;

/**
 * A timeout applied to SELECT to specify how long to wait for the row on the lock to be released.
 *
 * @author janmonterrubio
 */
@Data
public class Wait {

    /**
     * The number of seconds specified for the WAIT command
     */
    private long timeout;

    /**
     * Returns a String containing the WAIT clause and its timeout, where TIMEOUT is specified by
     * {@link #getTimeout()}. The returned string will null     be:<code>
     * &quot; WAIT &lt;TIMEOUT&gt;&quot;
     * </code>
     */
    @Override
    public String toString() {
        return " WAIT " + timeout;
    }
}
