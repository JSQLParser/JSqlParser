/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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
package net.sf.jsqlparser.expression;

/**
 *
 * @author toben
 */
public class Alias {

	private String name;
	private boolean useAs = true;

	public Alias(String name) {
		this.name = name;
	}
	
	public Alias(String name, boolean useAs) {
		this.name = name;
		this.useAs = useAs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUseAs() {
		return useAs;
	}

	public void setUseAs(boolean useAs) {
		this.useAs = useAs;
	}

	@Override
	public String toString() {
		return (useAs ? " AS " : " ") + name;
	}
}
