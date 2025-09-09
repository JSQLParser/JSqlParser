package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithFunctionDeclarationTest {
    static final String FUNCTION_NAME = "func1";
    static final String RETURN_TYPE = "integer";

    @Mock
    Expression expression;
    @Mock
    WithFunctionParameter withFunctionParameter1;
    @Mock
    WithFunctionParameter withFunctionParameter2;

    WithFunctionDeclaration withFunctionDeclaration;

    @Test
    void fullConstructorAndGetters() {
        withFunctionDeclaration = new WithFunctionDeclaration(FUNCTION_NAME,
                List.of(withFunctionParameter1, withFunctionParameter2), RETURN_TYPE, expression);
        assertThat(withFunctionDeclaration.getFunctionName()).isEqualTo(FUNCTION_NAME);
        assertThat(withFunctionDeclaration.getParameters())
                .isEqualTo(List.of(withFunctionParameter1, withFunctionParameter2));
        assertThat(withFunctionDeclaration.getReturnType()).isEqualTo(RETURN_TYPE);
        assertThat(withFunctionDeclaration.getReturnExpression()).isEqualTo(expression);
    }

    @Test
    void defaultConstructorAndSetters() {
        withFunctionDeclaration = new WithFunctionDeclaration();
        withFunctionDeclaration.setFunctionName(FUNCTION_NAME);
        withFunctionDeclaration
                .setParameters(List.of(withFunctionParameter1, withFunctionParameter2));
        withFunctionDeclaration.setReturnType(RETURN_TYPE);
        withFunctionDeclaration.setReturnExpression(expression);
        assertThat(withFunctionDeclaration.getFunctionName()).isEqualTo(FUNCTION_NAME);
        assertThat(withFunctionDeclaration.getParameters())
                .isEqualTo(List.of(withFunctionParameter1, withFunctionParameter2));
        assertThat(withFunctionDeclaration.getReturnType()).isEqualTo(RETURN_TYPE);
        assertThat(withFunctionDeclaration.getReturnExpression()).isEqualTo(expression);
    }

    @Test
    void defaultConstructorAndWithers() {
        withFunctionDeclaration = new WithFunctionDeclaration()
                .withFunctionName(FUNCTION_NAME)
                .withParameters(List.of(withFunctionParameter1, withFunctionParameter2))
                .withReturnType(RETURN_TYPE)
                .withReturnExpression(expression);
        assertThat(withFunctionDeclaration.getFunctionName()).isEqualTo(FUNCTION_NAME);
        assertThat(withFunctionDeclaration.getParameters())
                .isEqualTo(List.of(withFunctionParameter1, withFunctionParameter2));
        assertThat(withFunctionDeclaration.getReturnType()).isEqualTo(RETURN_TYPE);
        assertThat(withFunctionDeclaration.getReturnExpression()).isEqualTo(expression);
    }

    @Test
    void toStringTestWithParameters() {
        when(withFunctionParameter1.appendTo(any(StringBuilder.class))).thenAnswer(invocation -> {
            StringBuilder builder = invocation.getArgument(0);
            return builder.append("param1 bigint");
        });
        when(withFunctionParameter2.appendTo(any(StringBuilder.class))).thenAnswer(invocation -> {
            StringBuilder builder = invocation.getArgument(0);
            return builder.append("param2 double");
        });
        when(expression.toString()).thenReturn("1 + 1");
        withFunctionDeclaration = new WithFunctionDeclaration(FUNCTION_NAME,
                List.of(withFunctionParameter1, withFunctionParameter2), RETURN_TYPE, expression);

        assertThat(withFunctionDeclaration.toString()).isEqualTo(
                "FUNCTION func1(param1 bigint, param2 double) RETURNS integer RETURN 1 + 1");
    }

    @Test
    void toStringTestWithNoParameters() {
        when(expression.toString()).thenReturn("1 + 1");
        withFunctionDeclaration =
                new WithFunctionDeclaration(FUNCTION_NAME, List.of(), RETURN_TYPE, expression);

        assertThat(withFunctionDeclaration.toString())
                .isEqualTo("FUNCTION func1() RETURNS integer RETURN 1 + 1");
    }
}
