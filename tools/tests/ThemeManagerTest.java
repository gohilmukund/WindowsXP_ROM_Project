package tools.tests;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThemeManagerTest {

    public static void runAllTests() throws Exception {
        testAllThemesExist();
        testDynamicTrayMethodsExist();
        testStartMenuPaneAccessorsExist();
        testStartMenuColorArrays();
        testStartButtonWidths();
    }

    public static void testAllThemesExist() throws Exception {
        Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
        Object[] constants = themeEnum.getEnumConstants();
        List<String> names = Arrays.asList(
            "LUNA_BLUE", "LUNA_OLIVE", "LUNA_SILVER", "ROYALE",
            "CLASSIC", "VISTA", "WIN7_AERO", "WIN11"
        );
        List<String> missing = new ArrayList<String>();
        for (String expected : names) {
            boolean found = false;
            for (Object c : constants) {
                if (((Enum<?>) c).name().equals(expected)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(expected);
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("Missing expected theme enum(s): " + missing);
        }
    }

    public static void testDynamicTrayMethodsExist() throws Exception {
        Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
        Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
        Method mBg = tmClass.getMethod("getTaskbarTrayBgRes");
        Method mText = tmClass.getMethod("getTaskbarTrayTextColor");
        Method mBattery = tmClass.getMethod("getTaskbarTrayBatteryColor");

        if (mBg.getReturnType() != int.class) {
            throw new AssertionError("getTaskbarTrayBgRes must return int, got " + mBg.getReturnType().getName());
        }
        if (mText.getReturnType() != int.class) {
            throw new AssertionError("getTaskbarTrayTextColor must return int, got " + mText.getReturnType().getName());
        }
        if (mBattery.getReturnType() != int.class) {
            throw new AssertionError("getTaskbarTrayBatteryColor must return int, got " + mBattery.getReturnType().getName());
        }

        Method getInstanceMethod = tmClass.getMethod("getInstance");
        Object tm = getInstanceMethod.invoke(null);
        Method setThemeMethod = tmClass.getMethod("setTheme", themeEnum);

        // Verify null guards on setTheme and addListener
        setThemeMethod.invoke(tm, new Object[]{null});
        Class<?> listenerInterface = Class.forName("com.windows.xp.launcher.theme.ThemeManager$OnThemeChangeListener");
        Method addListenerMethod = tmClass.getMethod("addListener", listenerInterface);
        addListenerMethod.invoke(tm, new Object[]{null});

        Object[] constants = themeEnum.getEnumConstants();
        for (Object c : constants) {
            String themeName = ((Enum<?>) c).name();
            setThemeMethod.invoke(tm, c);
            Object bgVal = mBg.invoke(tm);
            Object textVal = mText.invoke(tm);
            Object batteryVal = mBattery.invoke(tm);
            if (bgVal == null || textVal == null || batteryVal == null) {
                throw new AssertionError("Dynamic tray accessors returned null for theme " + themeName);
            }

            int textColor = (Integer) textVal;
            int expectedTextColor = ("LUNA_SILVER".equals(themeName) || "CLASSIC".equals(themeName) || "WIN11".equals(themeName))
                ? 0xFF1C1C1C : 0xFFFFFFFF;
            if (textColor != expectedTextColor) {
                throw new AssertionError("Theme " + themeName + " expected tray text color 0x" +
                    Integer.toHexString(expectedTextColor).toUpperCase() + ", got 0x" +
                    Integer.toHexString(textColor).toUpperCase());
            }
        }
    }

    public static void testStartMenuPaneAccessorsExist() throws Exception {
        Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
        Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
        Method mRightBg = tmClass.getMethod("getStartMenuRightPaneBgColor");
        Method mRightText = tmClass.getMethod("getStartMenuRightPaneTextColor");
        Method mDivider = tmClass.getMethod("getStartMenuDividerColor");

        if (mRightBg.getReturnType() != int.class) {
            throw new AssertionError("getStartMenuRightPaneBgColor must return int, got " + mRightBg.getReturnType().getName());
        }
        if (mRightText.getReturnType() != int.class) {
            throw new AssertionError("getStartMenuRightPaneTextColor must return int, got " + mRightText.getReturnType().getName());
        }
        if (mDivider.getReturnType() != int.class) {
            throw new AssertionError("getStartMenuDividerColor must return int, got " + mDivider.getReturnType().getName());
        }

        Method getInstanceMethod = tmClass.getMethod("getInstance");
        Object tm = getInstanceMethod.invoke(null);
        Method setThemeMethod = tmClass.getMethod("setTheme", themeEnum);

        Object[] constants = themeEnum.getEnumConstants();
        for (Object c : constants) {
            String themeName = ((Enum<?>) c).name();
            setThemeMethod.invoke(tm, c);
            Object rightBgVal = mRightBg.invoke(tm);
            Object rightTextVal = mRightText.invoke(tm);
            Object dividerVal = mDivider.invoke(tm);
            if (rightBgVal == null || rightTextVal == null || dividerVal == null) {
                throw new AssertionError("Start menu pane accessors returned null for theme " + themeName);
            }

            int rightTextColor = (Integer) rightTextVal;
            if ("VISTA".equals(themeName) || "WIN7_AERO".equals(themeName)) {
                if (rightTextColor != 0xFFFFFFFF) {
                    throw new AssertionError("Theme " + themeName + " expected right pane text color 0xFFFFFFFF, got 0x" +
                        Integer.toHexString(rightTextColor).toUpperCase());
                }
            } else if ("WIN11".equals(themeName)) {
                if (rightTextColor != 0xFF1A1A1A) {
                    throw new AssertionError("Theme " + themeName + " expected right pane text color 0xFF1A1A1A, got 0x" +
                        Integer.toHexString(rightTextColor).toUpperCase());
                }
            } else if ("CLASSIC".equals(themeName) || "LUNA_SILVER".equals(themeName)) {
                if (rightTextColor != 0xFF000000) {
                    throw new AssertionError("Theme " + themeName + " expected right pane text color 0xFF000000, got 0x" +
                        Integer.toHexString(rightTextColor).toUpperCase());
                }
            }
        }
    }

    public static void testStartMenuColorArrays() throws Exception {
        Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
        Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
        Method mHeader = tmClass.getMethod("getStartMenuHeaderColors");
        Method mBottom = tmClass.getMethod("getStartMenuBottomColors");

        if (mHeader.getReturnType() != int[].class) {
            throw new AssertionError("getStartMenuHeaderColors must return int[]");
        }
        if (mBottom.getReturnType() != int[].class) {
            throw new AssertionError("getStartMenuBottomColors must return int[]");
        }

        Method getInstanceMethod = tmClass.getMethod("getInstance");
        Object tm = getInstanceMethod.invoke(null);
        Method setThemeMethod = tmClass.getMethod("setTheme", themeEnum);

        Object[] constants = themeEnum.getEnumConstants();
        for (Object c : constants) {
            String themeName = ((Enum<?>) c).name();
            setThemeMethod.invoke(tm, c);

            Object headerObj = mHeader.invoke(tm);
            if (!(headerObj instanceof int[])) {
                throw new AssertionError("getStartMenuHeaderColors did not return int[] for theme " + themeName);
            }
            int[] header = (int[]) headerObj;
            if (header.length != 2) {
                throw new AssertionError("getStartMenuHeaderColors length is " + header.length + " (expected 2) for theme " + themeName);
            }
            if (header[0] == 0 || header[1] == 0) {
                throw new AssertionError("getStartMenuHeaderColors elements must be non-zero for theme " + themeName);
            }

            Object bottomObj = mBottom.invoke(tm);
            if (!(bottomObj instanceof int[])) {
                throw new AssertionError("getStartMenuBottomColors did not return int[] for theme " + themeName);
            }
            int[] bottom = (int[]) bottomObj;
            if (bottom.length != 2) {
                throw new AssertionError("getStartMenuBottomColors length is " + bottom.length + " (expected 2) for theme " + themeName);
            }
            if (bottom[0] == 0 || bottom[1] == 0) {
                throw new AssertionError("getStartMenuBottomColors elements must be non-zero for theme " + themeName);
            }
        }
    }

    public static void testStartButtonWidths() throws Exception {
        Class<?> tmClass = Class.forName("com.windows.xp.launcher.theme.ThemeManager");
        Class<?> themeEnum = Class.forName("com.windows.xp.launcher.theme.ThemeManager$Theme");
        Method mWidth = tmClass.getMethod("getStartButtonWidthDp");

        if (mWidth.getReturnType() != int.class) {
            throw new AssertionError("getStartButtonWidthDp must return int, got " + mWidth.getReturnType().getName());
        }

        Method getInstanceMethod = tmClass.getMethod("getInstance");
        Object tm = getInstanceMethod.invoke(null);
        Method setThemeMethod = tmClass.getMethod("setTheme", themeEnum);

        Object[] constants = themeEnum.getEnumConstants();
        for (Object c : constants) {
            String themeName = ((Enum<?>) c).name();
            setThemeMethod.invoke(tm, c);
            int width = (Integer) mWidth.invoke(tm);

            int expectedWidth;
            if ("LUNA_BLUE".equals(themeName) || "LUNA_OLIVE".equals(themeName) ||
                "LUNA_SILVER".equals(themeName) || "ROYALE".equals(themeName)) {
                expectedWidth = 106;
            } else if ("WIN7_AERO".equals(themeName) || "VISTA".equals(themeName)) {
                expectedWidth = 48;
            } else if ("WIN11".equals(themeName)) {
                expectedWidth = 42;
            } else if ("CLASSIC".equals(themeName)) {
                expectedWidth = 65;
            } else {
                throw new AssertionError("Unexpected theme: " + themeName);
            }

            if (width != expectedWidth) {
                throw new AssertionError("Theme " + themeName + " expected start button width " + expectedWidth + " dp, got " + width);
            }
        }
    }
}
