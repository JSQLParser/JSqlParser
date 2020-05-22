/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SequenceTest {

    @Test
    public void testSetName() {
        Sequence sequence = new Sequence();
        sequence.setName("foo");

        assertThat(sequence.getName()).isEqualTo("foo");
        assertThat(sequence.getFullyQualifiedName()).isEqualTo("foo");
    }

    @Test
    public void testSetSchemaName() {
        Sequence sequence = new Sequence();
        sequence.setName("foo");
        sequence.setSchemaName("bar");

        assertThat(sequence.getSchemaName()).isEqualTo("bar");
        assertThat(sequence.getFullyQualifiedName()).isEqualTo("bar.foo");
    }

    @Test
    public void testSetDatabase() {
        Sequence sequence = new Sequence();
        sequence.setName("foo");
        sequence.setSchemaName("bar");
        sequence.setDatabase(new Database("default"));

        assertThat(sequence.getDatabase().getDatabaseName()).isEqualTo("default");
        assertThat(sequence.getFullyQualifiedName()).isEqualTo("default.bar.foo");
    }

    @Test
    public void testSetPartialName() {
        Sequence sequence = new Sequence();
        sequence.setName("foo");
        sequence.setDatabase(new Database("default"));

        assertThat(sequence.getFullyQualifiedName()).isEqualTo("default..foo");
    }
}
