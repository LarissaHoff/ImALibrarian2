# Add project specific ProGuard rules here.

# Preserve annotations and generic signatures used by reflection-based libraries.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# ---------------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------------
-keep,includedescriptorclasses class im.a.librarian.**$$serializer { *; }

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Retrofit / OkHttp
# ---------------------------------------------------------------------------
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Hilt / Dagger
# ---------------------------------------------------------------------------
-dontwarn dagger.hilt.**

# ---------------------------------------------------------------------------
# CameraX
# ---------------------------------------------------------------------------
-dontwarn androidx.camera.**

# ---------------------------------------------------------------------------
# ML Kit
# ---------------------------------------------------------------------------
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# ---------------------------------------------------------------------------
# Kotlin metadata
# ---------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
