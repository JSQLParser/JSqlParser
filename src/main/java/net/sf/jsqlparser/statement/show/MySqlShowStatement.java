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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;

/**
 * A MySQL {@code SHOW} statement that reads server settings, state or object lists, such as
 * {@code SHOW GLOBAL VARIABLES LIKE 'max%'}, {@code SHOW GRANTS FOR 'app'@'%'},
 * {@code SHOW BINLOG EVENTS IN 'binlog.000001' LIMIT 10}, {@code SHOW ENGINE INNODB STATUS} or
 * {@code SHOW PROFILE CPU FOR QUERY 2}.
 *
 * <p>
 * {@link #getKind()} tells which statement it is and {@link #getKeyword()} how its keywords were
 * spelled. Each {@link Kind} allows a fixed set of {@link Part parts}; setting a part the kind does
 * not allow throws {@link IllegalArgumentException}, and {@link #setKind(Kind)} drops the parts the
 * new kind does not allow. The table and column statements ({@code SHOW TABLES}, {@code COLUMNS},
 * {@code INDEX}, {@code TABLE STATUS} and {@code CREATE}) have their own classes.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show.html">MySQL SHOW statements</a>
 */
public class MySqlShowStatement extends ASTNodeAccessImpl implements Statement {

    private Kind kind;
    private String keyword;
    private Scope scope;
    private boolean full;
    private boolean count;
    private Table routineName;
    private String engineName;
    private EngineInfo engineInfo = EngineInfo.STATUS;
    private ShowDbSelection dbSelection;
    private ShowFilter filter;
    private UserAccount account;
    private final List<UserAccount> usingRoles = new ArrayList<>();
    private StringValue logName;
    private Expression position;
    private Limit limit;
    private Offset offset;
    private StringValue channel;
    private final List<ProfileType> profileTypes = new ArrayList<>();
    private Expression profileQuery;

    public MySqlShowStatement() {
        // empty constructor
    }

    public MySqlShowStatement(Kind kind) {
        setKind(kind);
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * Sets the kind, resets the keyword to the kind's first spelling and drops the parts the new
     * kind does not allow.
     */
    public void setKind(Kind kind) {
        this.kind = kind;
        this.keyword = null;
        if (kind != null) {
            for (Part part : Part.values()) {
                if (!kind.allows(part)) {
                    clear(part);
                }
            }
        }
    }

    private void clear(Part part) {
        switch (part) {
            case SCOPE:
                scope = null;
                break;
            case FULL:
                full = false;
                break;
            case COUNT:
                count = false;
                break;
            case ROUTINE:
                routineName = null;
                break;
            case ENGINE:
                engineName = null;
                engineInfo = EngineInfo.STATUS;
                break;
            case DATABASE:
                dbSelection = null;
                break;
            case FILTER:
                filter = null;
                break;
            case ACCOUNT:
                account = null;
                usingRoles.clear();
                break;
            default:
                clearRangeParts(part);
        }
    }

    private void clearRangeParts(Part part) {
        switch (part) {
            case PROFILE:
                profileTypes.clear();
                profileQuery = null;
                break;
            case LOG_NAME:
                logName = null;
                break;
            case POSITION:
                position = null;
                break;
            case LIMIT:
                limit = null;
                offset = null;
                break;
            default:
                channel = null;
        }
    }

    /**
     * The keywords as written, one of {@code getKind().getKeywords()}, such as {@code SCHEMAS} for
     * {@link Kind#DATABASES}; null without a kind.
     */
    public String getKeyword() {
        if (keyword != null || kind == null) {
            return keyword;
        }
        return kind.getKeywords().get(0);
    }

    /**
     * Sets the keywords and selects the kind they spell, as {@link #setKind(Kind)} does if the kind
     * changes; null resets the keywords to the first spelling of the kind.
     *
     * @throws IllegalArgumentException if the keywords spell no kind
     */
    public void setKeyword(String keyword) {
        if (keyword == null) {
            this.keyword = null;
            return;
        }
        Kind spelled = Kind.fromKeyword(keyword);
        if (spelled != kind) {
            setKind(spelled);
        }
        this.keyword = Kind.normalize(keyword);
    }

    private void require(Part part, boolean present) {
        if (present && kind != null && !kind.allows(part)) {
            throw new IllegalArgumentException(
                    "SHOW " + getKeyword() + " does not take the " + part + " part");
        }
    }

    /** {@code GLOBAL}, {@code SESSION} or {@code LOCAL}, or null if none was written. */
    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        require(Part.SCOPE, scope != null);
        this.scope = scope;
    }

    /** Whether {@code FULL} was written, as in {@code SHOW FULL PROCESSLIST}. */
    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        require(Part.FULL, full);
        this.full = full;
    }

    /** Whether the statement is {@code SHOW COUNT(*) {WARNINGS | ERRORS}}. */
    public boolean isCount() {
        return count;
    }

    /** Selects the {@code COUNT(*)} form, which takes no limit; {@code true} clears the limit. */
    public void setCount(boolean count) {
        require(Part.COUNT, count);
        this.count = count;
        if (count) {
            limit = null;
            offset = null;
        }
    }

    /** The routine of {@code SHOW {PROCEDURE | FUNCTION} CODE name}, or null. */
    public Table getRoutineName() {
        return routineName;
    }

    public void setRoutineName(Table routineName) {
        require(Part.ROUTINE, routineName != null);
        this.routineName = routineName;
    }

    /**
     * The engine of {@code SHOW ENGINE name ...} as written, including the quotes of a quoted name;
     * {@code ALL} for every engine.
     */
    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        require(Part.ENGINE, engineName != null);
        this.engineName = engineName;
    }

    /** {@code STATUS}, {@code MUTEX} or {@code LOGS} of {@code SHOW ENGINE}; never null. */
    public EngineInfo getEngineInfo() {
        return engineInfo;
    }

    /** Sets what {@code SHOW ENGINE} shows; null means {@code STATUS}. */
    public void setEngineInfo(EngineInfo engineInfo) {
        this.engineInfo = engineInfo == null ? EngineInfo.STATUS : engineInfo;
    }

    /** The {@code {FROM | IN} db} clause, or null. */
    public ShowDbSelection getDbSelection() {
        return dbSelection;
    }

    public void setDbSelection(ShowDbSelection dbSelection) {
        require(Part.DATABASE, dbSelection != null);
        this.dbSelection = dbSelection;
    }

    /** The database of the {@code {FROM | IN} db} clause, or null. */
    public String getDbName() {
        return ShowDbSelection.name(dbSelection);
    }

    /** The {@code LIKE} or {@code WHERE} filter, or null. */
    public ShowFilter getFilter() {
        return filter;
    }

    public void setFilter(ShowFilter filter) {
        require(Part.FILTER, filter != null);
        this.filter = filter;
    }

    /** The pattern of a {@code LIKE} filter, or null. */
    public Expression getLikeExpression() {
        return ShowFilter.likeExpression(filter);
    }

    /** The condition of a {@code WHERE} filter, or null. */
    public Expression getWhereCondition() {
        return ShowFilter.whereCondition(filter);
    }

    /**
     * The account or role of {@code SHOW GRANTS FOR}, without authentication, or null for the
     * current account.
     */
    public UserAccount getAccount() {
        return account;
    }

    /** Sets the account of {@code SHOW GRANTS FOR}; null also removes the {@code USING} roles. */
    public void setAccount(UserAccount account) {
        require(Part.ACCOUNT, account != null);
        if (account == null) {
            usingRoles.clear();
        }
        this.account = account;
    }

    /** The roles of {@code USING}, as a read-only snapshot; empty without {@code USING}. */
    public List<UserAccount> getUsingRoles() {
        return Collections.unmodifiableList(new ArrayList<>(usingRoles));
    }

    /** Replaces the roles of {@code USING}; null or an empty list removes {@code USING}. */
    public void setUsingRoles(List<UserAccount> usingRoles) {
        require(Part.ACCOUNT, usingRoles != null && !usingRoles.isEmpty());
        this.usingRoles.clear();
        if (usingRoles != null) {
            this.usingRoles.addAll(usingRoles);
        }
    }

    /** The log file of {@code IN 'log_name'}, or null. */
    public StringValue getLogName() {
        return logName;
    }

    public void setLogName(StringValue logName) {
        require(Part.LOG_NAME, logName != null);
        this.logName = logName;
    }

    /** The position of {@code FROM pos}, a number literal, or null. */
    public Expression getPosition() {
        return position;
    }

    public void setPosition(Expression position) {
        require(Part.POSITION, position != null);
        this.position = position;
    }

    /**
     * The {@code LIMIT [offset,] row_count} clause, or null. {@code LIMIT row_count OFFSET offset}
     * keeps its offset in {@link #getOffset()}.
     */
    public Limit getLimit() {
        return limit;
    }

    /** Sets the limit; a limit clears the {@code COUNT(*)} form. */
    public void setLimit(Limit limit) {
        require(Part.LIMIT, limit != null);
        this.limit = limit;
        if (limit == null) {
            offset = null;
        } else {
            count = false;
        }
    }

    /** The {@code OFFSET offset} that follows {@code LIMIT row_count}, or null. */
    public Offset getOffset() {
        return offset;
    }

    /**
     * Sets the {@code OFFSET offset} after the limit.
     *
     * @throws IllegalArgumentException without a limit, or with a {@code LIMIT offset, row_count}
     */
    public void setOffset(Offset offset) {
        require(Part.LIMIT, offset != null);
        if (offset != null && (limit == null || limit.getOffset() != null)) {
            throw new IllegalArgumentException("OFFSET needs a LIMIT row_count before it");
        }
        this.offset = offset;
    }

    /** The channel of {@code FOR CHANNEL 'channel'}, or null. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        require(Part.CHANNEL, channel != null);
        this.channel = channel;
    }

    /** The types of {@code SHOW PROFILE type [, type] ...}, as a read-only snapshot. */
    public List<ProfileType> getProfileTypes() {
        return Collections.unmodifiableList(new ArrayList<>(profileTypes));
    }

    /** Replaces the types of {@code SHOW PROFILE}; null or an empty list removes them. */
    public void setProfileTypes(List<ProfileType> profileTypes) {
        require(Part.PROFILE, profileTypes != null && !profileTypes.isEmpty());
        this.profileTypes.clear();
        if (profileTypes != null) {
            this.profileTypes.addAll(profileTypes);
        }
    }

    /** The query number of {@code SHOW PROFILE ... FOR QUERY n}, or null. */
    public Expression getProfileQuery() {
        return profileQuery;
    }

    public void setProfileQuery(Expression profileQuery) {
        require(Part.PROFILE, profileQuery != null);
        this.profileQuery = profileQuery;
    }

    /** Appends the statement, printing its expressions with the given printer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> printer) {
        builder.append("SHOW");
        if (scope != null) {
            builder.append(' ').append(scope);
        }
        if (full) {
            builder.append(" FULL");
        }
        if (count) {
            builder.append(" COUNT(*)");
        }
        builder.append(' ').append(getKeyword());
        appendObject(builder, printer);
        if (ShowDbSelection.isPresent(dbSelection)) {
            builder.append(' ').append(dbSelection);
        }
        if (filter != null) {
            filter.appendTo(builder.append(' '), printer);
        }
        appendAccount(builder);
        appendLogRange(builder, printer);
        if (channel != null) {
            builder.append(" FOR CHANNEL ");
            printer.accept(channel);
        }
        return builder;
    }

    private void appendObject(StringBuilder builder, Consumer<Expression> printer) {
        if (kind == Kind.ENGINE) {
            builder.append(' ').append(engineName).append(' ').append(engineInfo);
        } else if (routineName != null) {
            builder.append(' ').append(routineName);
        }
        for (int i = 0; i < profileTypes.size(); i++) {
            builder.append(i == 0 ? " " : ", ").append(profileTypes.get(i));
        }
        if (profileQuery != null) {
            builder.append(" FOR QUERY ");
            printer.accept(profileQuery);
        }
    }

    private void appendAccount(StringBuilder builder) {
        if (account == null) {
            return;
        }
        builder.append(" FOR ").append(account);
        for (int i = 0; i < usingRoles.size(); i++) {
            builder.append(i == 0 ? " USING " : ", ").append(usingRoles.get(i));
        }
    }

    private void appendLogRange(StringBuilder builder, Consumer<Expression> printer) {
        if (logName != null) {
            builder.append(" IN ");
            printer.accept(logName);
        }
        if (position != null) {
            builder.append(" FROM ");
            printer.accept(position);
        }
        if (limit != null) {
            builder.append(" LIMIT ");
            if (limit.getOffset() != null) {
                printer.accept(limit.getOffset());
                builder.append(", ");
            }
            printer.accept(limit.getRowCount());
        }
        if (offset != null) {
            builder.append(" OFFSET ");
            printer.accept(offset.getOffset());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public MySqlShowStatement withKind(Kind kind) {
        this.setKind(kind);
        return this;
    }

    public MySqlShowStatement withKeyword(String keyword) {
        this.setKeyword(keyword);
        return this;
    }

    public MySqlShowStatement withScope(Scope scope) {
        this.setScope(scope);
        return this;
    }

    public MySqlShowStatement withFull(boolean full) {
        this.setFull(full);
        return this;
    }

    public MySqlShowStatement withCount(boolean count) {
        this.setCount(count);
        return this;
    }

    public MySqlShowStatement withRoutineName(Table routineName) {
        this.setRoutineName(routineName);
        return this;
    }

    public MySqlShowStatement withEngineName(String engineName) {
        this.setEngineName(engineName);
        return this;
    }

    public MySqlShowStatement withEngineInfo(EngineInfo engineInfo) {
        this.setEngineInfo(engineInfo);
        return this;
    }

    public MySqlShowStatement withDbSelection(ShowDbSelection dbSelection) {
        this.setDbSelection(dbSelection);
        return this;
    }

    public MySqlShowStatement withFilter(ShowFilter filter) {
        this.setFilter(filter);
        return this;
    }

    public MySqlShowStatement withAccount(UserAccount account) {
        this.setAccount(account);
        return this;
    }

    public MySqlShowStatement withUsingRoles(List<UserAccount> usingRoles) {
        this.setUsingRoles(usingRoles);
        return this;
    }

    public MySqlShowStatement withLogName(StringValue logName) {
        this.setLogName(logName);
        return this;
    }

    public MySqlShowStatement withPosition(Expression position) {
        this.setPosition(position);
        return this;
    }

    public MySqlShowStatement withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public MySqlShowStatement withOffset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public MySqlShowStatement withChannel(StringValue channel) {
        this.setChannel(channel);
        return this;
    }

    public MySqlShowStatement withProfileTypes(List<ProfileType> profileTypes) {
        this.setProfileTypes(profileTypes);
        return this;
    }

    public MySqlShowStatement withProfileQuery(Expression profileQuery) {
        this.setProfileQuery(profileQuery);
        return this;
    }

    /** An optional or required part of a {@link MySqlShowStatement}, allowed per {@link Kind}. */
    public enum Part {
        /** {@code GLOBAL}, {@code SESSION} or {@code LOCAL} before the keywords. */
        SCOPE,
        /** {@code FULL} before the keywords. */
        FULL,
        /** {@code COUNT(*)} before the keywords. */
        COUNT,
        /** The routine name after {@code PROCEDURE CODE} or {@code FUNCTION CODE}. */
        ROUTINE,
        /**
         * The engine name and {@code STATUS}, {@code MUTEX} or {@code LOGS} after {@code ENGINE}.
         */
        ENGINE,
        /** {@code {FROM | IN} db}. */
        DATABASE,
        /** {@code LIKE 'pattern'} or {@code WHERE expr}. */
        FILTER,
        /** {@code FOR account [USING role [, role] ...]}. */
        ACCOUNT,
        /** {@code IN 'log_name'}. */
        LOG_NAME,
        /** {@code FROM pos}. */
        POSITION,
        /** {@code LIMIT [offset,] row_count} or {@code LIMIT row_count OFFSET offset}. */
        LIMIT,
        /** {@code FOR CHANNEL 'channel'}. */
        CHANNEL,
        /** The types and {@code FOR QUERY n} of {@code SHOW PROFILE}. */
        PROFILE
    }

    /** {@code GLOBAL}, {@code SESSION} or its synonym {@code LOCAL}. */
    public enum Scope {
        GLOBAL, SESSION, LOCAL
    }

    /** What {@code SHOW ENGINE} shows. */
    public enum EngineInfo {
        STATUS, MUTEX, LOGS
    }

    /** A type of information of {@code SHOW PROFILE}. */
    public enum ProfileType {
        ALL, BLOCK_IO, CONTEXT_SWITCHES, CPU, IPC, MEMORY, PAGE_FAULTS, SOURCE, SWAPS;

        /** The type spelled with the keywords, such as {@code BLOCK IO}, ignoring case. */
        public static ProfileType fromKeyword(String keyword) {
            return valueOf(Kind.normalize(keyword).replace(' ', '_'));
        }

        @Override
        public String toString() {
            return name().replace('_', ' ');
        }
    }

    /**
     * The statement. The first keyword spelling is the MySQL 8.4 one; the others are synonyms, or
     * the spellings MySQL 8.4 removed ({@code MASTER} and {@code SLAVE}), which the parser reads
     * with {@code Dialect.MARIADB} and without a dialect only.
     */
    public enum Kind {
        VARIABLES(EnumSet.of(Part.SCOPE, Part.FILTER), "VARIABLES"),

        STATUS(EnumSet.of(Part.SCOPE, Part.FILTER), "STATUS"),

        PROCESSLIST(EnumSet.of(Part.FULL), "PROCESSLIST"),

        DATABASES(EnumSet.of(Part.FILTER), "DATABASES", "SCHEMAS"),

        TRIGGERS(EnumSet.of(Part.FULL, Part.DATABASE, Part.FILTER), "TRIGGERS"),

        EVENTS(EnumSet.of(Part.DATABASE, Part.FILTER), "EVENTS"),

        OPEN_TABLES(EnumSet.of(Part.DATABASE, Part.FILTER), "OPEN TABLES"),

        PROCEDURE_STATUS(EnumSet.of(Part.FILTER), "PROCEDURE STATUS"),

        FUNCTION_STATUS(EnumSet.of(Part.FILTER), "FUNCTION STATUS"),

        PROCEDURE_CODE(EnumSet.of(Part.ROUTINE), "PROCEDURE CODE"),

        FUNCTION_CODE(EnumSet.of(Part.ROUTINE), "FUNCTION CODE"),

        CHARACTER_SET(EnumSet.of(Part.FILTER), "CHARACTER SET", "CHARSET", "CHAR SET"),

        COLLATION(EnumSet.of(Part.FILTER), "COLLATION"),

        ENGINES(EnumSet.noneOf(Part.class), "ENGINES", "STORAGE ENGINES"),

        ENGINE(EnumSet.of(Part.ENGINE), "ENGINE"),

        PLUGINS(EnumSet.noneOf(Part.class), "PLUGINS"),

        PRIVILEGES(EnumSet.noneOf(Part.class), "PRIVILEGES"),

        PROFILES(EnumSet.noneOf(Part.class), "PROFILES"),

        PROFILE(EnumSet.of(Part.PROFILE, Part.LIMIT), "PROFILE"),

        GRANTS(EnumSet.of(Part.ACCOUNT), "GRANTS"),

        WARNINGS(EnumSet.of(Part.COUNT, Part.LIMIT), "WARNINGS"),

        ERRORS(EnumSet.of(Part.COUNT, Part.LIMIT), "ERRORS"),

        BINARY_LOGS(EnumSet.noneOf(Part.class), "BINARY LOGS", "MASTER LOGS"),

        BINARY_LOG_STATUS(EnumSet.noneOf(Part.class), "BINARY LOG STATUS", "MASTER STATUS"),

        BINLOG_EVENTS(EnumSet.of(Part.LOG_NAME, Part.POSITION, Part.LIMIT), "BINLOG EVENTS"),

        RELAYLOG_EVENTS(EnumSet.of(Part.LOG_NAME, Part.POSITION, Part.LIMIT, Part.CHANNEL),
                "RELAYLOG EVENTS"),

        REPLICAS(EnumSet.noneOf(Part.class), "REPLICAS", "SLAVE HOSTS"),

        REPLICA_STATUS(EnumSet.of(Part.CHANNEL), "REPLICA STATUS", "SLAVE STATUS");

        private final Set<Part> parts;
        private final List<String> keywords;

        Kind(Set<Part> parts, String... keywords) {
            this.parts = Collections.unmodifiableSet(parts);
            this.keywords = Collections.unmodifiableList(Arrays.asList(keywords));
        }

        /** Whether statements of this kind may have the part. */
        public boolean allows(Part part) {
            return parts.contains(part);
        }

        /** The parts statements of this kind may have. */
        public Set<Part> getParts() {
            return parts;
        }

        /** The keyword spellings, the MySQL 8.4 spelling first, single-spaced and upper case. */
        public List<String> getKeywords() {
            return keywords;
        }

        static String normalize(String keyword) {
            return keyword.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        }

        /**
         * The kind spelled with the keywords, compared case-insensitively.
         *
         * @throws IllegalArgumentException if no kind is spelled so
         */
        public static Kind fromKeyword(String keyword) {
            String spelling = normalize(keyword);
            for (Kind kind : values()) {
                if (kind.keywords.contains(spelling)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("Unknown MySQL SHOW keywords: " + keyword);
        }
    }
}
