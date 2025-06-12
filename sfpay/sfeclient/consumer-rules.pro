# SFE Frontend SDK ProGuard Rules
# These rules ensure the SDK functions correctly in release builds while maintaining security

# Keep the main SDK entry point
-keep class com.gradientgeeks.sfesdk.SfeFrontendSdk { *; }
-keepclassmembers class com.gradientgeeks.sfesdk.SfeFrontendSdk {
    public static ** Companion;
}
-keep class com.gradientgeeks.sfesdk.SfeFrontendSdk$SdkConfig { *; }

# Keep public manager interfaces (public API)
-keep interface com.gradientgeeks.sfesdk.managers.**Manager { *; }

# Keep public data models used in API
-keep class com.gradientgeeks.sfesdk.models.** { *; }

# Keep exception classes
-keep class com.gradientgeeks.sfesdk.exceptions.** { *; }

# Protect manager implementations but keep essential methods
-keep class com.gradientgeeks.sfesdk.managers.impl.**ManagerImpl {
    public <methods>;
}

# Keep methods that are called via reflection or from native code
-keepclassmembers class com.gradientgeeks.sfesdk.managers.impl.** {
    public <init>(...);
}

# Kotlin specific rules
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Google Play Integrity API
-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**

# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Security: Obfuscate sensitive security logic while keeping interfaces
-keepclassmembers class com.gradientgeeks.sfesdk.managers.impl.RaspManagerImpl {
    !private <methods>;
}

-keepclassmembers class com.gradientgeeks.sfesdk.managers.impl.AttestationManagerImpl {
    !private <methods>;
}

# Keep Android components that might be accessed via reflection
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Remove debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Additional security measures for sensitive data
-keepclassmembers class * {
    @com.gradientgeeks.sfesdk.annotations.KeepForSecurity *;
}

# Optimize and obfuscate everything else
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

