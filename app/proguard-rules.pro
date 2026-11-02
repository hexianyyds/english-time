# 飞岩英语 R8 规则

# kotlinx.serialization：保留 @Serializable 类的合成 serializer
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.feiyan.english.core.model.**$$serializer { *; }
-keepclassmembers class com.feiyan.english.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.feiyan.english.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Media3
-dontwarn androidx.media3.**

# Coil
-dontwarn coil3.**

# 反射用到的枚举/图标（ImageVector 懒加载不受影响，这里兜底）
-keep class com.feiyan.english.ui.icons.** { *; }

# 保留行号信息，便于线上崩溃定位
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
