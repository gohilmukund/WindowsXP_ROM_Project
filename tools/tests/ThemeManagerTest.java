package tools.tests;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ThemeManagerTest {

    public static void runAllTests() {
        testAllThemesExist();
        testDynamicTrayMethodsExist();
        testStartMenuPaneAccessorsExist();
        testStartMenuColorArrays();
        testStartButtonWidths();
    }

    private static void testAllThemesExist() {
        try {
            Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
            Object[] constants = themeEnum.getEnumConstants();
            List<String> names = Arrays.asList(
                "LUNA_BLUE", "LUNA_OLIVE", "LUNA_SILVER", "ROYALE",
                "CLASSIC", "VISTA", "WIN7_AERO", "WIN11"
            );
            for (String expected : names) {
                boolean found = false;
                for (Object c : constants) {
                    if (c.toString().equals(expected)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new AssertionError("Missing expected theme enum: " + expected);
                }
            }
            System.out.println("  [PASS] testAllThemesExist");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Theme enum class not found: " + e.getMessage());
        }
    }

    private static void testDynamicTrayMethodsExist() {
        try {
            Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
            tmClass.getMethod("getTaskbarTrayBgRes");
            tmClass.getMethod("getTaskbarTrayTextColor");
            tmClass.getMethod("getTaskbarTrayBatteryColor");
            System.out.println("  [PASS] testDynamicTrayMethodsExist");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Missing dynamic tray accessor: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("ThemeManager class not found: " + e.getMessage());
        }
    }

    private static void testStartMenuPaneAccessorsExist() {
        try {
            Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
            tmClass.getMethod("getStartMenuRightPaneBgColor");
            tmClass.getMethod("getStartMenuRightPaneTextColor");
            tmClass.getMethod("getStartMenuDividerColor");
            System.out.println("  [PASS] testStartMenuPaneAccessorsExist");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Missing start menu pane accessor: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("ThemeManager class not found: " + e.getMessage());
        }
    }

    private static void testStartMenuColorArrays() {
        try {
            Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
            Method mHeader = tmClass.getMethod("getStartMenuHeaderColors");
            Method mBottom = tmClass.getMethod("getStartMenuBottomColors");
            System.out.println("  [PASS] testStartMenuColorArrays");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Missing start menu color array accessor: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("ThemeManager class not found: " + e.getMessage());
        }
    }

    private static void testStartButtonWidths() {
        try {
            Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
            tmClass.getMethod("getStartButtonWidthDp");
            System.out.println("  [PASS] testStartButtonWidths");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Missing getStartButtonWidthDp: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new AssertionError("ThemeManager class not found: " + e.getMessage());
        }
    }
}
