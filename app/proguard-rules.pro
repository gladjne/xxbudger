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

# --- Reverse Engineering Protections ---

# 1. Keeps Room entity class names and their fields/methods to ensure SQL mapping remains reliable
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# 2. Keeps DataStore serializer class names to prevent schema serialization lookup failures
-keep class * implements androidx.datastore.core.Serializer { *; }

# Prevent Moshi models and generated adapters from being stripped or obfuscated
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit service interface support for reflection mapping
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# 3. Keep SQLCipher classes so they do not get obfuscated or stripped when loaded via reflection
-keep class net.zetetic.** { *; }
-dontwarn net.zetetic.**

