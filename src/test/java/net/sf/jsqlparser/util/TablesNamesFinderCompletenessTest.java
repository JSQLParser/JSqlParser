package net.sf.jsqlparser.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * TablesNamesFinder collects tables by dispatching through the visitor interfaces it declares. Most
 * dispatch methods are abstract, so a missing implementation fails the build already. But the
 * interfaces progressively gain default methods (e.g. SelectVisitor#visit(PivotQuery, context)
 * returns null), and for those a missing TablesNamesFinder implementation compiles silently and
 * loses the tables of that node type without any test failing.
 *
 * This test fails the build when a declared visitor interface has a dispatch method visit(Node,
 * context) for an AST node that is not implemented in the TablesNamesFinder class hierarchy (it
 * resolves to an interface default instead). It does not catch empty-shell implementations (an
 * override that visits nothing) nor visitor families the finder does not declare at all: both
 * remain manual review duties.
 */
public class TablesNamesFinderCompletenessTest {

    // GroupByElement: ExpressionVisitor's default forwards the group-by expressions and the
    // grouping sets to the visitor, TablesNamesFinder relies on it from visit(PlainSelect).
    private static final Set<String> INHERITED_DISPATCH_NODES =
            Set.of("net.sf.jsqlparser.statement.select.GroupByElement");

    @Test
    void testImplementsEveryDispatchMethodOfEveryDeclaredVisitorInterface()
            throws NoSuchMethodException {
        Class<?>[] visitorInterfaces = TablesNamesFinder.class.getInterfaces();
        assertTrue(visitorInterfaces.length >= 8, "TablesNamesFinder must keep declaring the"
                + " visitor families it dispatches through (currently 8), dropping one shrinks"
                + " the completeness walk silently.");

        List<String> gaps = new ArrayList<>();
        List<String> interfacesWithoutDispatch = new ArrayList<>();
        for (Class<?> visitorInterface : visitorInterfaces) {
            int dispatchMethods = 0;
            for (Method dispatch : visitorInterface.getMethods()) {
                if (!dispatch.getName().equals("visit") || dispatch.getParameterCount() != 2) {
                    continue;
                }
                Class<?> nodeType = dispatch.getParameterTypes()[0];
                // skips bulk dispatch helpers carrying a java.util collection
                if (!nodeType.getName().startsWith("net.sf.jsqlparser.")) {
                    continue;
                }
                if (INHERITED_DISPATCH_NODES.contains(nodeType.getName())) {
                    continue;
                }
                dispatchMethods++;
                Method implementation = TablesNamesFinder.class.getMethod(dispatch.getName(),
                        dispatch.getParameterTypes());
                if (implementation.getDeclaringClass().isInterface()) {
                    gaps.add(visitorInterface.getSimpleName() + "#" + dispatch.getName() + "("
                            + nodeType.getSimpleName() + ", context)");
                }
            }
            if (dispatchMethods == 0) {
                interfacesWithoutDispatch.add(visitorInterface.getSimpleName());
            }
        }

        assertTrue(interfacesWithoutDispatch.isEmpty(),
                () -> "No guarded dispatch method found for: "
                        + String.join(", ", interfacesWithoutDispatch) + ". Either the reflection"
                        + " walk lost the dispatch methods (filters too narrow, the completeness"
                        + " check would pass vacuously) or the interface is not a dispatch"
                        + " interface and must not be declared by TablesNamesFinder.");
        assertTrue(gaps.isEmpty(), () -> "TablesNamesFinder must implement every dispatch"
                + " method of the visitor interfaces it declares, otherwise the tables of that"
                + " node type are silently not collected. Missing:\n" + String.join("\n", gaps)
                + "\nImplement visit(Node, context) in TablesNamesFinder, or - when the"
                + " inherited default already forwards every child - document the node in"
                + " INHERITED_DISPATCH_NODES.");
    }
}
