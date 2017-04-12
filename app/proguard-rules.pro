# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/connorlin/Program/SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class android.support.** {*;}
-keep class com.bumptech.glide.** {*;}
-dontwarn com.bumptech.glide.**

-keep class com.zhuoyi.appStatistics.**{*;}
-dontwarn com.zhuoyi.appStatistics.**
-keep class android.os.**{*;}

# provided 'javax.annotation:jsr250-api:1.0'
-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static Joiner on(String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

-dontwarn sun.misc.Unsafe
-dontwarn javax.annotation.**