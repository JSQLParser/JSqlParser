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
package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Foreign Key Index
 *
 * @author toben
 */
@Data
public class ForeignKeyIndex extends NamedConstraint {

    private Table table;
    private List<String> referencedColumnNames;
    private String onDeleteReferenceOption;
    private String onUpdateReferenceOption;

    @Override
    public String toString() {
        String referenceOptions = "";
        if (onDeleteReferenceOption != null) {
            referenceOptions += " ON DELETE " + onDeleteReferenceOption;
        }
        if (onUpdateReferenceOption != null) {
            referenceOptions += " ON UPDATE " + onUpdateReferenceOption;
        }
        return super.toString()
                + " REFERENCES " + table + PlainSelect.
                        getStringList(getReferencedColumnNames(), true, true)
                + referenceOptions;
    }
}
