# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#-keeppackagenames the.package.where.the.file.is.kept

-keepclassmembers class **.R$* {
   public static <fields>;
}

# === COMPREHENSIVE R8 MISSING CLASSES FIX ===

# Missing XML parsers (your specific error)
-dontwarn org.kxml2.io.KXmlParser
-dontwarn org.kxml2.io.KXmlSerializer
-dontwarn org.xmlpull.v1.**
-dontwarn kxml2.io.**

# Google API Client & HTTP dependencies
-dontwarn com.google.api.client.http.**
-dontwarn com.google.api.client.googleapis.**
-dontwarn com.google.api.client.util.**
-dontwarn com.google.http.client.**
-dontwarn org.apache.http.**
-dontwarn org.apache.commons.**

# Firebase & Google Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Google API Services & Drive
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.**

# Jackson JSON (used by Google API Client)
-dontwarn com.fasterxml.jackson.**
-dontwarn org.codehaus.jackson.**

# Jetty HTTP (Google OAuth Client)
-dontwarn org.mortbay.jetty.**
-dontwarn org.eclipse.jetty.**

# Apache HTTP Components
-dontwarn org.apache.http.conn.ssl.**
-dontwarn org.apache.http.conn.scheme.**

# Keep model classes (for Firebase)
-keep class com.antandbuffalo.birthdayreminder.models.** { *; }

# Keep custom views and activities
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep alarm receiver specifically (CRITICAL for your app)
-keep class com.antandbuffalo.birthdayreminder.notification.AlarmReceiver { *; }
-keep class com.antandbuffalo.birthdayreminder.notification.BootComplete { *; }

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# AndroidX and support library
-keep class androidx.** { *; }
-dontwarn androidx.**

# Material Design Components
-dontwarn com.google.android.material.**

# Kotlin coroutines (if using)
-dontwarn kotlinx.coroutines.**

# OkHttp and networking
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson (if used by Firebase)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Test dependencies (for debugAndroidTest)
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn androidx.test.**
-dontwarn junit.**

# Conscrypt (Google security library)
-dontwarn org.conscrypt.**

# Remove debug logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
