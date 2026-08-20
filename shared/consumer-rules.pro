# CoreCmp ProGuard/R8 rules — add to host app if minification strips CoreCmp APIs:
#   proguardFiles("consumer-rules.pro")  (copy from coreCmp AAR or this file)

-keep class com.corecmp.shared.CoreCmp { *; }
-keep class com.corecmp.shared.api.** { *; }
-keep class com.corecmp.shared.picker.** { *; }
-keep class com.corecmp.shared.permission.** { *; }

-keepattributes *Annotation*, InnerClasses
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.android.play.core.**
-dontwarn com.google.zxing.**
