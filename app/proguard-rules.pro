# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all data/model classes used by Gson / Retrofit (Kotlin data classes with JSON fields)
-keep class com.binahr.data.** { *; }
-keepclassmembers class com.binahr.data.** { *; }

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# CameraX
-keep class androidx.camera.** { *; }

# Compose — keep lambdas and composable internals
-dontwarn androidx.compose.**

# Encrypted SharedPreferences / Security-Crypto
-keep class androidx.security.crypto.** { *; }