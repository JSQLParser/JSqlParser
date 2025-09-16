/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithFunctionParameterTest {
    static final String PARAMETER_NAME = "param1";
    static final String PARAMETER_TYPE = "integer";

    WithFunctionParameter withFunctionParameter;

    @Test
    void fullConstructorAndGetters() {
        withFunctionParameter = new WithFunctionParameter(PARAMETER_NAME, PARAMETER_TYPE);
        assertThat(withFunctionParameter.getName()).isEqualTo(PARAMETER_NAME);
        assertThat(withFunctionParameter.getType()).isEqualTo(PARAMETER_TYPE);
    }

    @Test
    void defaultConstructorAndSetters() {
        withFunctionParameter = new WithFunctionParameter();
        withFunctionParameter.setName(PARAMETER_NAME);
        withFunctionParameter.setType(PARAMETER_TYPE);
        assertThat(withFunctionParameter.getName()).isEqualTo(PARAMETER_NAME);
        assertThat(withFunctionParameter.getType()).isEqualTo(PARAMETER_TYPE);
    }

    @Test
    void defaultConstructorAndWithers() {
        withFunctionParameter = new WithFunctionParameter()
                .withName(PARAMETER_NAME)
                .withType(PARAMETER_TYPE);
        assertThat(withFunctionParameter.getName()).isEqualTo(PARAMETER_NAME);
        assertThat(withFunctionParameter.getType()).isEqualTo(PARAMETER_TYPE);
    }

    @Test
    void testToString() {
        withFunctionParameter = new WithFunctionParameter(PARAMETER_NAME, PARAMETER_TYPE);
        assertThat(withFunctionParameter.toString()).isEqualTo("param1 integer");
    }
}
