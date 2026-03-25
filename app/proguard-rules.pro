# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializersKt
-keep,includedescriptorclasses class com.torbox.downloader.**$$serializer { *; }
-keepclassmembers class com.torbox.downloader.** {
    *** Companion;
}
-keepclasseswithmembers class com.torbox.downloader.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Preserve Room
-keep class androidx.room.** { *; }

# Keep models
-keep class com.torbox.downloader.data.** { *; }
