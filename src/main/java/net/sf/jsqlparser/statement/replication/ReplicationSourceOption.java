/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replication;

import java.math.BigInteger;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * An option of MySQL's {@code CHANGE REPLICATION SOURCE TO}. Its value is a string, a number,
 * {@code NULL}, a keyword such as {@code OFF} (held as a {@link Column}) or, for
 * {@code IGNORE_SERVER_IDS}, a parenthesized list of server IDs. {@code PRIVILEGE_CHECKS_USER}
 * names an account, which {@link #getAccount()} returns, or {@code NULL}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/change-replication-source-to.html">MySQL
 *      CHANGE REPLICATION SOURCE TO</a>
 */
public class ReplicationSourceOption extends ReplicationOption<ReplicationSourceOption.Kind> {
    private final UserAccount account;

    /** The values an option takes in the MySQL 8.4 grammar. */
    public enum ValueType {
        /** A string literal. */
        STRING,
        /** A string literal or {@code NULL}. */
        STRING_OR_NULL,
        /** An unsigned integer or decimal literal. */
        NUMBER,
        /** The integer literal 0 or 1. */
        ZERO_OR_ONE,
        /** A parenthesized, possibly empty list of numbers. */
        NUMBER_LIST,
        /** An account or {@code NULL}. */
        ACCOUNT_OR_NULL,
        /** {@code STREAM}, {@code ON}, {@code OFF} or {@code GENERATE}. */
        PRIMARY_KEY_CHECK,
        /** {@code OFF}, {@code LOCAL} or a UUID string. */
        GTID_ASSIGNMENT,
        /** No value at all. */
        NONE;

        /**
         * Whether MySQL accepts {@code value} for an option of this type. An account is not an
         * expression, so for {@link #ACCOUNT_OR_NULL} only {@code NULL} is checked here.
         */
        public boolean accepts(Expression value) {
            switch (this) {
                case STRING:
                    return value instanceof StringValue;
                case STRING_OR_NULL:
                    return value instanceof StringValue || value instanceof NullValue;
                case NUMBER:
                    return value instanceof LongValue || value instanceof DoubleValue;
                case ZERO_OR_ONE:
                    return value instanceof LongValue && BigInteger.ONE
                            .compareTo(((LongValue) value).getBigIntegerValue()) >= 0;
                case NUMBER_LIST:
                    return value instanceof ParenthesedExpressionList;
                case ACCOUNT_OR_NULL:
                    return value instanceof NullValue;
                case PRIMARY_KEY_CHECK:
                    return isKeyword(value, "STREAM", "ON", "OFF", "GENERATE");
                case GTID_ASSIGNMENT:
                    return value instanceof StringValue || isKeyword(value, "OFF", "LOCAL");
                default:
                    return value == null;
            }
        }

        private static boolean isKeyword(Expression value, String... keywords) {
            if (!(value instanceof Column)) {
                return false;
            }
            String name = ((Column) value).getColumnName();
            for (String keyword : keywords) {
                if (keyword.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** The options of MySQL 8.4; the MASTER_ spellings were removed in 8.4. */
    public enum Kind {
        /** The network interface of the replica */
        SOURCE_BIND(ValueType.STRING),
        /** The host name or IP address of the source */
        SOURCE_HOST(ValueType.STRING),
        /** The replication user account */
        SOURCE_USER(ValueType.STRING),
        /** The password of the replication user */
        SOURCE_PASSWORD(ValueType.STRING),
        /** The TCP/IP port of the source */
        SOURCE_PORT(ValueType.NUMBER),
        /** The account that privilege checks of the applier use, or NULL */
        PRIVILEGE_CHECKS_USER(ValueType.ACCOUNT_OR_NULL),
        /** Whether the channel accepts only row-based events */
        REQUIRE_ROW_FORMAT(ValueType.NUMBER),
        /** The primary key check of the channel */
        REQUIRE_TABLE_PRIMARY_KEY_CHECK(ValueType.PRIMARY_KEY_CHECK),
        /** How anonymous transactions get a GTID */
        ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS(ValueType.GTID_ASSIGNMENT),
        /** The binary log file to read from */
        SOURCE_LOG_FILE(ValueType.STRING),
        /** The position to read from in the binary log file */
        SOURCE_LOG_POS(ValueType.NUMBER),
        /** Whether the replica finds its position by GTID */
        SOURCE_AUTO_POSITION(ValueType.NUMBER),
        /** The relay log file the applier starts from */
        RELAY_LOG_FILE(ValueType.STRING),
        /** The position the applier starts from in the relay log file */
        RELAY_LOG_POS(ValueType.NUMBER),
        /** The heartbeat interval in seconds */
        SOURCE_HEARTBEAT_PERIOD(ValueType.NUMBER),
        /** The seconds between reconnection attempts */
        SOURCE_CONNECT_RETRY(ValueType.NUMBER),
        /** The number of reconnection attempts */
        SOURCE_RETRY_COUNT(ValueType.NUMBER),
        /** Whether the channel fails over to another source */
        SOURCE_CONNECTION_AUTO_FAILOVER(ValueType.ZERO_OR_ONE),
        /** The seconds the replica lags behind the source */
        SOURCE_DELAY(ValueType.NUMBER),
        /** The permitted compression algorithms */
        SOURCE_COMPRESSION_ALGORITHMS(ValueType.STRING),
        /** The zstd compression level */
        SOURCE_ZSTD_COMPRESSION_LEVEL(ValueType.NUMBER),
        /** Whether the connection uses SSL */
        SOURCE_SSL(ValueType.NUMBER),
        /** The certificate authority file */
        SOURCE_SSL_CA(ValueType.STRING),
        /** The directory of certificate authority files */
        SOURCE_SSL_CAPATH(ValueType.STRING),
        /** The certificate file */
        SOURCE_SSL_CERT(ValueType.STRING),
        /** The certificate revocation list file */
        SOURCE_SSL_CRL(ValueType.STRING),
        /** The directory of certificate revocation list files */
        SOURCE_SSL_CRLPATH(ValueType.STRING),
        /** The private key file */
        SOURCE_SSL_KEY(ValueType.STRING),
        /** The permitted ciphers */
        SOURCE_SSL_CIPHER(ValueType.STRING),
        /** Whether the replica verifies the certificate of the source */
        SOURCE_SSL_VERIFY_SERVER_CERT(ValueType.NUMBER),
        /** The permitted TLS protocols */
        SOURCE_TLS_VERSION(ValueType.STRING),
        /** The permitted TLSv1.3 cipher suites, or NULL */
        SOURCE_TLS_CIPHERSUITES(ValueType.STRING_OR_NULL),
        /** The file of the RSA public key of the source */
        SOURCE_PUBLIC_KEY_PATH(ValueType.STRING),
        /** Whether the replica requests the RSA public key of the source */
        GET_SOURCE_PUBLIC_KEY(ValueType.NUMBER),
        /** The network namespace of the connection */
        NETWORK_NAMESPACE(ValueType.STRING),
        /** The server IDs whose events the replica ignores */
        IGNORE_SERVER_IDS(ValueType.NUMBER_LIST),
        /** Whether the channel keeps no file names and positions */
        GTID_ONLY(ValueType.ZERO_OR_ONE);

        private final ValueType valueType;

        Kind(ValueType valueType) {
            this.valueType = valueType;
        }

        public ValueType getValueType() {
            return valueType;
        }
    }

    /** The values of {@code REQUIRE_TABLE_PRIMARY_KEY_CHECK}. */
    public enum PrimaryKeyCheck {
        STREAM, ON, OFF, GENERATE
    }

    /** The values of {@code ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS}; UUID is a quoted UUID. */
    public enum GtidAssignment {
        OFF, LOCAL, UUID
    }

    public ReplicationSourceOption(String name, Kind kind, Expression value) {
        super(name, kind, value);
        this.account = null;
    }

    /** {@code PRIVILEGE_CHECKS_USER = account}. */
    public ReplicationSourceOption(String name, UserAccount account) {
        super(name, Kind.PRIVILEGE_CHECKS_USER, null);
        this.account = account;
    }

    /** The account of {@code PRIVILEGE_CHECKS_USER}, null for {@code NULL} and other options. */
    public UserAccount getAccount() {
        return account;
    }

    public PrimaryKeyCheck getPrimaryKeyCheck() {
        return getKind() == Kind.REQUIRE_TABLE_PRIMARY_KEY_CHECK
                ? enumValue(PrimaryKeyCheck.class)
                : null;
    }

    public GtidAssignment getGtidAssignment() {
        if (getKind() != Kind.ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS) {
            return null;
        }
        return getValue() instanceof StringValue ? GtidAssignment.UUID
                : enumValue(GtidAssignment.class);
    }

    @Override
    public String getValueText() {
        return account != null ? account.toString() : super.getValueText();
    }

    @Override
    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        if (account == null) {
            super.appendTo(sql, expressions);
        } else {
            sql.append(getName()).append(" = ").append(account);
        }
    }
}
