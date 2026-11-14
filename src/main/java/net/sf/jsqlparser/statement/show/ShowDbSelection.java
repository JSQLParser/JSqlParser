/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import java.io.Serializable;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;

/**
 * The {@code {FROM | IN} db} clause of a MySQL {@code SHOW} statement, which names a database.
 *
 * <p>
 * A selection without a name only keeps the selection mode for a later name; statements do not
 * render it.
 * </p>
 */
public class ShowDbSelection implements Serializable {

    private static final long serialVersionUID = 1L;

    private SelectionMode selectionMode = SelectionMode.FROM;
    private String name;

    public ShowDbSelection() {
        // empty constructor
    }

    /** A {@code FROM name} clause. */
    public ShowDbSelection(String name) {
        this.name = name;
    }

    public ShowDbSelection(SelectionMode selectionMode, String name) {
        setSelectionMode(selectionMode);
        this.name = name;
    }

    /** The keyword before the database name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    /** Sets the keyword before the database name; null means {@code FROM}. */
    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode == null ? SelectionMode.FROM : selectionMode;
    }

    /** The database name as written, including any quotes. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Whether the selection has a database name and is rendered. */
    public static boolean isPresent(ShowDbSelection selection) {
        return selection != null && selection.name != null;
    }

    /** The database name of the selection, or null. */
    public static String name(ShowDbSelection selection) {
        return selection == null ? null : selection.name;
    }

    /**
     * The selection after a legacy selection mode setter: the mode is kept until a name is set.
     */
    public static ShowDbSelection updateSelectionMode(ShowDbSelection selection,
            SelectionMode selectionMode) {
        ShowDbSelection result = selection == null ? new ShowDbSelection() : selection;
        result.setSelectionMode(selectionMode);
        return result;
    }

    /** The selection after a legacy database name setter, keeping the selection mode. */
    public static ShowDbSelection updateName(ShowDbSelection selection, String name) {
        if (selection == null && name == null) {
            return null;
        }
        ShowDbSelection result = selection == null ? new ShowDbSelection() : selection;
        result.setName(name);
        return result;
    }

    @Override
    public String toString() {
        return selectionMode + " " + name;
    }

    public ShowDbSelection withSelectionMode(SelectionMode selectionMode) {
        this.setSelectionMode(selectionMode);
        return this;
    }

    public ShowDbSelection withName(String name) {
        this.setName(name);
        return this;
    }
}
