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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Room database keep rules ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.limit_tracker.**
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep interface com.example.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase_Impl

# --- Kotlin Serialization rules ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# --- Lifecycle and ViewModels rules ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
