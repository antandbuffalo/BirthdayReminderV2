package com.antandbuffalo.birthdayreminder.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeOptions {
    private static ThemeOptions themeOptions = null;
    private static List<Map<String, String>> values;
    private ThemeOptions() {
        values = new ArrayList<>();
        Map<String, String> item = new HashMap<>();
        item.put("key", "default");
        item.put("value", "System Default");
        values.add(item);

        item = new HashMap<>();
        item.put("key", "light");
        item.put("value", "Light");
        values.add(item);

        item = new HashMap<>();
        item.put("key", "dark");
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
