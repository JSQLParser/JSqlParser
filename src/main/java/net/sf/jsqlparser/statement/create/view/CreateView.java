/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.view;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class CreateView implements Statement {

    private MySqlViewOptions mySqlOptions;

    public MySqlViewOptions getMySqlOptions() {
        return mySqlOptions;
    }

    public void setMySqlOptions(MySqlViewOptions mySqlOptions) {
        this.mySqlOptions = mySqlOptions;
    }

    public void appendMySqlOptionsTo(StringBuilder builder) {
        if (mySqlOptions != null) {
            mySqlOptions.appendTo(builder);
        }
    }

    private Table view;
    private Select select;
    private boolean orReplace = false;
    private ExpressionList<Column> columnNames = null;
    private boolean materialized = false;
    private ForceOption force = ForceOption.NONE;
    private boolean secure = false;
    private TemporaryOption temp = TemporaryOption.NONE;
    private AutoRefreshOption autoRefresh = AutoRefreshOption.NONE;
    private Boolean backup;
    private boolean withReadOnly = false;
    private boolean ifNotExists = false;
    private List<String> viewCommentOptions = null;
    private boolean recursive;
    private boolean ifNotExistsAfterViewName;
    private List<ViewOption> options;
    private String accessMethod;
    private List<Index.Option> storageParameters;
    private String tableSpace;
    private CheckOption checkOption;
    private Boolean withData;

    /** DEFAULT preserves a CHECK OPTION clause with no explicit LOCAL or CASCADED keyword. */
    public enum CheckOption {
        DEFAULT, LOCAL, CASCADED
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /** Whether the legacy syntax placed IF NOT EXISTS after the view name. */
    public boolean isIfNotExistsAfterViewName() {
        return ifNotExistsAfterViewName;
    }

    public void setIfNotExistsAfterViewName(boolean afterViewName) {
        this.ifNotExistsAfterViewName = afterViewName;
    }

    public List<ViewOption> getOptions() {
        return options;
    }

    public void setOptions(List<ViewOption> options) {
        this.options = options == null ? null : new ArrayList<>(options);
    }

    public String getAccessMethod() {
        return accessMethod;
    }

    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
    }

    public List<Index.Option> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<Index.Option> storageParameters) {
        this.storageParameters =
                storageParameters == null ? null : new ArrayList<>(storageParameters);
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    /** Returns null when the trailing CHECK OPTION clause was omitted. */
    public CheckOption getCheckOption() {
        return checkOption;
    }

    public void setCheckOption(CheckOption checkOption) {
        this.checkOption = checkOption;
    }

    /** Resolves a bare WITH CHECK OPTION to CASCADED; null still means no trailing clause. */
    public CheckOption getEffectiveCheckOption() {
        return checkOption == CheckOption.DEFAULT ? CheckOption.CASCADED : checkOption;
    }

    /** Returns null for omission, true for WITH DATA, and false for WITH NO DATA. */
    public Boolean getWithData() {
        return withData;
    }

    public void setWithData(Boolean withData) {
        this.withData = withData;
    }

    /** Checks combinations requiring ordinary, recursive or materialized views. */
    public void validateOptions() {
        if (backup != null && !materialized) {
            throw new IllegalArgumentException("BACKUP requires a materialized view");
        }
        if (recursive && (materialized || columnNames == null || columnNames.isEmpty()
                || checkOption != null)) {
            throw new IllegalArgumentException(
                    "A recursive view requires columns and cannot be materialized or have CHECK OPTION");
        }
        if (materialized && (options != null || checkOption != null)) {
            throw new IllegalArgumentException(
                    "View parameters and CHECK OPTION require an ordinary view");
        }
        if (!materialized && (accessMethod != null || storageParameters != null
                || tableSpace != null || withData != null)) {
            throw new IllegalArgumentException(
                    "Storage parameters and WITH DATA require a materialized view");
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    /**
     * @param orReplace was "OR REPLACE" specified?
     */
    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public ExpressionList<Column> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(ExpressionList<Column> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isMaterialized() {
        return materialized;
    }

    public void setMaterialized(boolean materialized) {
        this.materialized = materialized;
    }

    public ForceOption getForce() {
        return force;
    }

    public void setForce(ForceOption force) {
        this.force = force;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public TemporaryOption getTemporary() {
        return temp;
    }

    public void setTemporary(TemporaryOption temp) {
        this.temp = temp;
    }

    public AutoRefreshOption getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(AutoRefreshOption autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    /** Null means omitted; true and false preserve Redshift's BACKUP YES and BACKUP NO. */
    public Boolean getBackup() {
        return backup;
    }

    public void setBackup(Boolean backup) {
        this.backup = backup;
    }

    public CreateView withBackup(Boolean backup) {
        setBackup(backup);
        return this;
    }

    /** Shared SQL rendering for options immediately following the materialized view name. */
    public void appendMaterializationOptionsTo(StringBuilder sql) {
        if (backup != null) {
            sql.append(backup ? " BACKUP YES" : " BACKUP NO");
        }
        if (autoRefresh != AutoRefreshOption.NONE) {
            sql.append(" AUTO REFRESH ").append(autoRefresh.name());
        }
    }

    public boolean isWithReadOnly() {
        return withReadOnly;
    }

    public void setWithReadOnly(boolean withReadOnly) {
        this.withReadOnly = withReadOnly;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @Override
    public String toString() {
        validateOptions();
        StringBuilder sql = new StringBuilder("CREATE ");
        if (isOrReplace()) {
            sql.append("OR REPLACE ");
        }
        appendMySqlOptionsTo(sql);
        appendForceOptionIfApplicable(sql);
        if (secure) {
            sql.append("SECURE ");
        }

        if (temp != TemporaryOption.NONE) {
            sql.append(temp.name()).append(" ");
        }

        if (recursive) {
            sql.append("RECURSIVE ");
        }
        if (isMaterialized()) {
            sql.append("MATERIALIZED ");
        }
        sql.append("VIEW ");
        if (ifNotExists && !ifNotExistsAfterViewName) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(view);
        if (ifNotExists && ifNotExistsAfterViewName) {
            sql.append(" IF NOT EXISTS");
        }
        appendMaterializationOptionsTo(sql);
        if (columnNames != null) {
            sql.append("(");
            sql.append(columnNames);
            sql.append(")");
        }
        if (viewCommentOptions != null) {
            sql.append(PlainSelect.getStringList(viewCommentOptions, false, false));
        }
        appendParametersTo(sql);
        sql.append(" AS ").append(select);
        if (isWithReadOnly()) {
            sql.append(" WITH READ ONLY");
        }
        appendOptionsAfterQueryTo(sql);
        return sql.toString();
    }

    /** Appends the parameters between the view name/columns and AS. */
    public void appendParametersTo(StringBuilder sql) {
        if (accessMethod != null) {
            sql.append(" USING ").append(accessMethod);
        }
        if (options != null) {
            sql.append(" WITH ").append(PlainSelect.getStringList(options, true, true));
        }
        if (storageParameters != null) {
            sql.append(" WITH ").append(PlainSelect.getStringList(storageParameters, true, true));
        }
        if (tableSpace != null) {
            sql.append(" TABLESPACE ").append(tableSpace);
        }
    }

    public static void appendCheckOptionTo(StringBuilder sql, CheckOption checkOption) {
        if (checkOption != null) {
            sql.append(" WITH ");
            if (checkOption != CheckOption.DEFAULT) {
                sql.append(checkOption).append(' ');
            }
            sql.append("CHECK OPTION");
        }
    }

    public void appendOptionsAfterQueryTo(StringBuilder sql) {
        appendCheckOptionTo(sql, checkOption);
        if (withData != null) {
            sql.append(withData ? " WITH DATA" : " WITH NO DATA");
        }
    }

    private void appendForceOptionIfApplicable(StringBuilder sql) {
        switch (force) {
            case FORCE:
                sql.append("FORCE ");
                break;
            case NO_FORCE:
                sql.append("NO FORCE ");
                break;
            default:
                // nothing
        }
    }

    public CreateView withView(Table view) {
        this.setView(view);
        return this;
    }

    public CreateView withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public CreateView withOrReplace(boolean orReplace) {
        this.setOrReplace(orReplace);
        return this;
    }

    public CreateView withColumnNames(ExpressionList<Column> columnNames) {
        this.setColumnNames(columnNames);
        return this;
    }

    public CreateView withMaterialized(boolean materialized) {
        this.setMaterialized(materialized);
        return this;
    }

    public CreateView withForce(ForceOption force) {
        this.setForce(force);
        return this;
    }

    public CreateView withWithReadOnly(boolean withReadOnly) {
        this.setWithReadOnly(withReadOnly);
        return this;
    }

    public List<String> getViewCommentOptions() {
        return viewCommentOptions;
    }

    public void setViewCommentOptions(List<String> viewCommentOptions) {
        this.viewCommentOptions = viewCommentOptions;
    }
}
