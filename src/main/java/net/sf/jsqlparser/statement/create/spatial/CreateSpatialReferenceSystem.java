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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code CREATE OR REPLACE SPATIAL REFERENCE SYSTEM srid srs_attribute ...} and
 * {@code CREATE SPATIAL REFERENCE SYSTEM [IF NOT EXISTS] srid srs_attribute ...}. OR REPLACE and IF
 * NOT EXISTS exclude each other.
 *
 * <p>
 * The attributes keep their source order. MySQL's grammar takes them in any order and any number;
 * the server then reports a repeated attribute and a missing NAME or DEFINITION as errors of their
 * own, not as syntax errors, so the parser accepts them. The typed getters return the first
 * attribute of their kind.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/create-spatial-reference-system.html">MySQL
 *      CREATE SPATIAL REFERENCE SYSTEM</a>
 */
public class CreateSpatialReferenceSystem extends ASTNodeAccessImpl implements Statement {
    private boolean orReplace;
    private boolean ifNotExists;
    private Expression srid;
    private final List<SpatialReferenceSystemAttribute> attributes = new ArrayList<>();

    public CreateSpatialReferenceSystem(Expression srid) {
        setSrid(srid);
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    /**
     * Sets OR REPLACE.
     *
     * @throws IllegalArgumentException if the statement has IF NOT EXISTS
     */
    public void setOrReplace(boolean orReplace) {
        if (orReplace && ifNotExists) {
            throw new IllegalArgumentException("OR REPLACE excludes IF NOT EXISTS");
        }
        this.orReplace = orReplace;
    }

    public CreateSpatialReferenceSystem withOrReplace(boolean orReplace) {
        setOrReplace(orReplace);
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    /**
     * Sets IF NOT EXISTS.
     *
     * @throws IllegalArgumentException if the statement has OR REPLACE
     */
    public void setIfNotExists(boolean ifNotExists) {
        if (ifNotExists && orReplace) {
            throw new IllegalArgumentException("IF NOT EXISTS excludes OR REPLACE");
        }
        this.ifNotExists = ifNotExists;
    }

    public CreateSpatialReferenceSystem withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }

    /**
     * The SRID as written: a {@link net.sf.jsqlparser.expression.LongValue}, or a
     * {@link net.sf.jsqlparser.expression.HexValue} for {@code 0x...} and {@code X'...'}.
     */
    public Expression getSrid() {
        return srid;
    }

    public final void setSrid(Expression srid) {
        this.srid = Objects.requireNonNull(srid, "srid");
    }

    public CreateSpatialReferenceSystem withSrid(Expression srid) {
        setSrid(srid);
        return this;
    }

    /** The mutable attributes in source order. */
    public List<SpatialReferenceSystemAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<? extends SpatialReferenceSystemAttribute> attributes) {
        this.attributes.clear();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
    }

    public CreateSpatialReferenceSystem withAttributes(
            Collection<? extends SpatialReferenceSystemAttribute> attributes) {
        setAttributes(attributes);
        return this;
    }

    public CreateSpatialReferenceSystem addAttributes(
            SpatialReferenceSystemAttribute... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    /** The first attribute of the kind, or null. */
    public SpatialReferenceSystemAttribute getAttribute(
            SpatialReferenceSystemAttribute.Kind kind) {
        for (SpatialReferenceSystemAttribute attribute : attributes) {
            if (attribute.getKind() == kind) {
                return attribute;
            }
        }
        return null;
    }

    private StringValue getValue(SpatialReferenceSystemAttribute.Kind kind) {
        SpatialReferenceSystemAttribute attribute = getAttribute(kind);
        return attribute == null ? null : attribute.getValue();
    }

    /** The string of {@code NAME 'srs_name'}, or null. */
    public StringValue getName() {
        return getValue(SpatialReferenceSystemAttribute.Kind.NAME);
    }

    /** The string of {@code DEFINITION 'definition'}, or null. */
    public StringValue getDefinition() {
        return getValue(SpatialReferenceSystemAttribute.Kind.DEFINITION);
    }

    /** The organization name of {@code ORGANIZATION 'org_name' IDENTIFIED BY org_id}, or null. */
    public StringValue getOrganization() {
        return getValue(SpatialReferenceSystemAttribute.Kind.ORGANIZATION);
    }

    /** The number of {@code ORGANIZATION 'org_name' IDENTIFIED BY org_id}, or null. */
    public Expression getOrganizationId() {
        SpatialReferenceSystemAttribute attribute =
                getAttribute(SpatialReferenceSystemAttribute.Kind.ORGANIZATION);
        return attribute == null ? null : attribute.getOrganizationId();
    }

    /** The string of {@code DESCRIPTION 'description'}, or null. */
    public StringValue getDescription() {
        return getValue(SpatialReferenceSystemAttribute.Kind.DESCRIPTION);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(orReplace ? "CREATE OR REPLACE " : "CREATE ")
                .append("SPATIAL REFERENCE SYSTEM ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(srid);
        for (SpatialReferenceSystemAttribute attribute : attributes) {
            attribute.appendTo(builder.append(' '));
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
