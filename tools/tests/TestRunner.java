package tools.tests;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== Running LunaLauncher Regression Test Suite ===");
        try {
            ThemeManagerTest.runAllTests();
            System.out.println("=== ALL TESTS PASSED ===");
            System.exit(0);
        } catch (Throwable t) {
            System.err.println("=== TEST FAILURE ===");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
