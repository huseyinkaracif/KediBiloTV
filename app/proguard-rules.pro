# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.kedibilotv.**$$serializer { *; }
-keepclassmembers class com.kedibilotv.** { *** Companion; }
-keepclasseswithmembers class com.kedibilotv.** { kotlinx.serialization.KSerializer serializer(...); }
