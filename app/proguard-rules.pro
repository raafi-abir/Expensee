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

# Line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database and Entities
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class com.expensee.data.AppDatabase_Impl { *; }
-keep class com.expensee.data.dao.** { *; }
-keep class com.expensee.data.model.** { *; }

# Domain calculation and analysis engines
-keep class com.expensee.domain.** { *; }

# Moshi / JSON Serialization
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keepattributes *Annotation*
-dontwarn com.squareup.moshi.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose Runtime stability
-keep class androidx.compose.runtime.** { *; }

