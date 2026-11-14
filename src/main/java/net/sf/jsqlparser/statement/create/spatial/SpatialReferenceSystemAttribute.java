/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.spatial;

import java.util.Objects;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * One attribute of MySQL's {@code CREATE SPATIAL REFERENCE SYSTEM}: {@code NAME 'srs_name'},
 * {@code DEFINITION 'definition'}, {@code ORGANIZATION 'org_name' IDENTIFIED BY org_id} or
 * {@code DESCRIPTION 'description'}.
 *
 * @see CreateSpatialReferenceSystem
 */
public class SpatialReferenceSystemAttribute extends ASTNodeAccessImpl {
    /** The keyword that starts the attribute. */
    public enum Kind {
        NAME, DEFINITION, ORGANIZATION, DESCRIPTION
    }

    private final Kind kind;
    private StringValue value;
    private Expression organizationId;

    /**
     * A NAME, DEFINITION or DESCRIPTION attribute.
     *
     * @throws IllegalArgumentException for {@link Kind#ORGANIZATION}, which needs an identifier
     */
    public SpatialReferenceSystemAttribute(Kind kind, StringValue value) {
        this.kind = Objects.requireNonNull(kind, "kind");
        if (kind == Kind.ORGANIZATION) {
            throw new IllegalArgumentException("ORGANIZATION requires IDENTIFIED BY");
        }
        setValue(value);
    }

    /** An {@code ORGANIZATION 'org_name' IDENTIFIED BY org_id} attribute. */
    public SpatialReferenceSystemAttribute(StringValue organization, Expression organizationId) {
        this.kind = Kind.ORGANIZATION;
        setValue(organization);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId");
    }

    public Kind getKind() {
        return kind;
    }

    /** The string after the keyword; the organization name for {@link Kind#ORGANIZATION}. */
    public StringValue getValue() {
        return value;
    }

    public final void setValue(StringValue value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public SpatialReferenceSystemAttribute withValue(StringValue value) {
        setValue(value);
        return this;
    }

    /**
     * The number after IDENTIFIED BY as written, a {@link net.sf.jsqlparser.expression.LongValue}
     * or a {@link net.sf.jsqlparser.expression.HexValue}; null unless the attribute is an
     * {@link Kind#ORGANIZATION}.
     */
    public Expression getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the number after IDENTIFIED BY.
     *
     * @throws IllegalArgumentException if the attribute is not an ORGANIZATION
     */
    public void setOrganizationId(Expression organizationId) {
        if (kind != Kind.ORGANIZATION) {
            throw new IllegalArgumentException(kind + " takes no IDENTIFIED BY");
        }
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId");
    }

    public SpatialReferenceSystemAttribute withOrganizationId(Expression organizationId) {
        setOrganizationId(organizationId);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(kind).append(' ').append(value);
        if (organizationId != null) {
            builder.append(" IDENTIFIED BY ").append(organizationId);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
