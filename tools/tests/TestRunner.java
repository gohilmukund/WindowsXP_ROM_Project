package tools.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== Running LunaLauncher Regression Test Suite ===");

        Class<?>[] testClasses = new Class<?>[] {
            ThemeManagerTest.class,
            DosBoxConfigTest.class
        };

        final List<String> preferredOrder = Arrays.asList(
            "testAllThemesExist",
            "testDynamicTrayMethodsExist",
            "testStartMenuPaneAccessorsExist",
            "testStartMenuColorArrays",
            "testStartButtonWidths"
        );

        int passed = 0;
        int failed = 0;

        for (Class<?> testClass : testClasses) {
            List<Method> methods = new ArrayList<Method>();
            for (Method m : testClass.getDeclaredMethods()) {
                if (m.getName().startsWith("test") && m.getParameterTypes().length == 0) {
                    methods.add(m);
                }
            }

            Collections.sort(methods, new Comparator<Method>() {
                @Override
                public int compare(Method m1, Method m2) {
                    int i1 = preferredOrder.indexOf(m1.getName());
                    int i2 = preferredOrder.indexOf(m2.getName());
                    if (i1 != -1 && i2 != -1) return Integer.compare(i1, i2);
                    if (i1 != -1) return -1;
                    if (i2 != -1) return 1;
                    return m1.getName().compareTo(m2.getName());
                }
            });

            for (Method m : methods) {
                try {
                    m.setAccessible(true);
                    m.invoke(null);
                    System.out.println("  [PASS] " + m.getName());
                    passed++;
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                    String msg = cause.getMessage() != null ? cause.getMessage() : cause.toString();
                    System.out.println("  [FAIL] " + m.getName() + " - " + cause.getClass().getSimpleName() + ": " + msg);
                    failed++;
                } catch (Throwable t) {
                    String msg = t.getMessage() != null ? t.getMessage() : t.toString();
                    System.out.println("  [FAIL] " + m.getName() + " - " + t.getClass().getSimpleName() + ": " + msg);
                    failed++;
                }
            }
        }

        System.out.println("\n=== Test Results ===");
        System.out.println("Total passed: " + passed);
        System.out.println("Total failed: " + failed);

        if (failed > 0) {
            System.out.println("=== SOME TESTS FAILED ===");
            System.exit(1);
        } else {
            System.out.println("=== ALL TESTS PASSED ===");
            System.exit(0);
        }
    }
}
