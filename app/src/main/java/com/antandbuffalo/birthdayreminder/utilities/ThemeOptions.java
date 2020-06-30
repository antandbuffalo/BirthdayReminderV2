package com.antandbuffalo.birthdayreminder.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeOptions {
    private static ThemeOptions themeOptions = null;
    private static List<Map<String, String>> values;
    public static String KEY_DEFAULT = "default";
    public static String KEY_LIGHT = "light";
    public static String KEY_DARK = "dark";
    private ThemeOptions() {
        values = new ArrayList<>();
        Map<String, String> item = new HashMap<>();
        item.put("key", KEY_DEFAULT);
        item.put("value", "System Default");
        values.add(item);

        item = new HashMap<>();
        item.put("key", KEY_LIGHT);
        item.put("value", "Light");
        values.add(item);

        item = new HashMap<>();
        item.put("key", KEY_DARK);
        item.put("value", "Dark");
        values.add(item);
    }

    public static ThemeOptions getInstance() {
        if(themeOptions == null) {
            themeOptions = new ThemeOptions();
        }
        return themeOptions;
    }
    public List<Map<String, String>> getValues() {
        return values;
    }
}
