package com.antandbuffalo.birthdayreminder;

import android.app.Activity;
import android.app.Application;
import android.graphics.Insets;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class BirthdayReminderApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Android 15+ draws every targetSdk 35+ app edge to edge, so the toolbar slid under
        // the status bar and the FAB under the navigation bar. Pad each screen's content by
        // the system bars and paint the status bar strip, as Android did before.
        if (Build.VERSION.SDK_INT >= 35) {
            registerActivityLifecycleCallbacks(new SystemBarPadding());
        }
    }

    private static class SystemBarPadding implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            if (Build.VERSION.SDK_INT < 35) return;
            View decor = activity.getWindow().getDecorView();
            View content = activity.findViewById(android.R.id.content);
            Drawable windowBackground = decor.getBackground();
            int statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimaryDark);

            content.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);

                LayerDrawable background = new LayerDrawable(new Drawable[]{
                        windowBackground != null ? windowBackground : new ColorDrawable(0),
                        new ColorDrawable(statusBarColor)});
                background.setLayerGravity(1, Gravity.TOP | Gravity.FILL_HORIZONTAL);
                background.setLayerHeight(1, bars.top);
                decor.setBackground(background);
                return WindowInsets.CONSUMED;
            });
            content.requestApplyInsets();
        }

        @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
        @Override public void onActivityStarted(@NonNull Activity activity) {}
        @Override public void onActivityResumed(@NonNull Activity activity) {}
        @Override public void onActivityPaused(@NonNull Activity activity) {}
        @Override public void onActivityStopped(@NonNull Activity activity) {}
        @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
        @Override public void onActivityDestroyed(@NonNull Activity activity) {}
    }
}
