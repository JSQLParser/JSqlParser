/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum H2Version implements Version {
    V_1_4_200("1.4.200",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // http://h2database.com/html/commands.html#select
                    Feature.select,
                    Feature.selectGroupBy,
                    // https://h2database.com/html/grammar.html?#table_expression
                    // https://h2database.com/html/grammar.html?#join_specification
                    Feature.join,
                    Feature.joinSimple,
                    Feature.joinRight,
                    Feature.joinFull,
                    Feature.joinLeft,
                    Feature.joinCross,
                    Feature.joinOuter,
                    Feature.joinInner,
                    Feature.joinNatural,
                    Feature.joinUsingColumns,

                    // http://h2database.com/html/commands.html#comment
                    Feature.comment,
                    Feature.commentOnTable,
                    Feature.commentOnColumn,
                    Feature.commentOnView,

                    // http://h2database.com/html/functions.html#table
                    Feature.tableFunction,

                    // http://h2database.com/html/commands.html#select
                    Feature.setOperation,
                    Feature.setOperationUnion,
                    Feature.setOperationIntersect,
                    Feature.setOperationExcept,
                    Feature.setOperationMinus,

                    // http://h2database.com/html/commands.html#create_sequence
                    Feature.createSequence,
                    // http://h2database.com/html/commands.html#alter_sequence
                    Feature.alterSequence,
                    // http://h2database.com/html/commands.html#create_schema
                    Feature.createSchema,
                    // http://h2database.com/html/commands.html#create_index
                    Feature.createIndex,
                    // http://h2database.com/html/commands.html#create_table
                    Feature.createTable,
                    // http://h2database.com/html/commands.html#create_view
                    Feature.createView,
                    // http://h2database.com/html/commands.html#alter_view_rename
                    // Feature.alterView,

                    // http://h2database.com/html/commands.html#select
                    Feature.top,

                    Feature.distinct,
                    // http://h2database.com/html/commands.html#insert
                    Feature.insert,
                    Feature.insertFromSelect,
                    // http://h2database.com/html/commands.html#update
                    Feature.update,
                    // http://h2database.com/html/commands.html#delete
                    Feature.delete,
                    // http://h2database.com/html/commands.html#truncate_table
                    Feature.truncate,
                    // http://h2database.com/html/commands.html#drop_table
                    // http://h2database.com/html/commands.html#drop_index
                    // ...
                    Feature.drop,
                    // http://h2database.com/html/commands.html#alter_table_add
                    // http://h2database.com/html/commands.html#alter_table_add_constraint
                    // ...
                    Feature.alter));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private H2Version(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private H2Version(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
        this.versionString = versionString;
        this.features = featuresSupported;
        this.features.removeAll(unsupported);
    }

    @Override
    public String getVersionString() {
        return versionString;
    }

    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

    @Override
    public String getName() {
        return DatabaseType.H2.getName() + " " + getVersionString();
    }

}
