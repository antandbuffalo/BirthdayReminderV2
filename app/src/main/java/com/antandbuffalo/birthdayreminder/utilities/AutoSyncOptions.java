package com.antandbuffalo.birthdayreminder.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoSyncOptions {
    private static AutoSyncOptions autoSyncOptions = null;
    private static List<Map<String, String>> values;
    private AutoSyncOptions() {
        values = new ArrayList<>();
        Map<String, String> item = new HashMap<>();
        item.put("key", "none");
        item.put("value", "None");
        values.add(item);

        item = new HashMap<>();
        item.put("key", "daily");
        item.put("value", "Daily");
        values.add(item);

        item = new HashMap<>();
        item.put("key", "weekly");
        item.put("value", "Weekly");
        values.add(item);

        item = new HashMap<>();
        item.put("key", "monthly");
        item.put("value", "Monthly");
        values.add(item);
    }

    public static AutoSyncOptions getInstance() {
        if(autoSyncOptions == null) {
            autoSyncOptions = new AutoSyncOptions();
        }
        return autoSyncOptions;
    }
    public List<Map<String, String>> getValues() {
        return values;
    }
}
