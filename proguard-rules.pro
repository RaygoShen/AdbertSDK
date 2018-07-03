# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/RomanYu/Library/Android/sdk/tools/proguard/proguard-android.txt
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

#hide log
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-dontskipnonpubliclibraryclassmembers
#-dontoptimize
-dontusemixedcaseclassnames
-keepattributes *Annotation*,SourceFile,LineNumberTable,JavascriptInterface,Signature
-renamesourcefileattribute SourceFile
-dontwarn com.adbert.**,javax.**,android.**,com.google.android.gms.**,com.google.**
-ignorewarnings

-keepnames class com.adbert.AdbertActivity{
    void onCreate*(...);
    public protected <methods>;
}
-keepnames class com.adbert.AdbertInterstitialActivity{
    void onCreate*(...);
    public protected <methods>;
}
-keep public class com.adbert.AdbertNativeADListener {
    <fields>;
    <methods>;
}
-keep public class com.adbert.AdbertOrientation{
     <fields>;
     <methods>;
}
-keep public class com.adbert.ExpandVideoPosition {
    <fields>;
    <methods>;
}

-keep public class com.adbert.AdbertVideoBoxListener {
    <fields>;
    <methods>;
}

-keep class com.adbert.AdbertADView{
    public <methods>;
}

-keep class com.adbert.AdbertADView$*{
    public <methods>;
}

-keep class com.adbert.AdbertInterstitialAD{
    public <methods>;
}

-keep class com.adbert.AdbertInterstitialAD$*{
    public <methods>;
}

-keep public class com.adbert.AdbertListener {
    <fields>;
    <methods>;
}

-keep class com.adbert.AdbertLoopADView{
    public <methods>;
}

-keep class com.adbert.AdbertNativeAD{
    public <methods>;
}

-keep class com.adbert.AdbertVideoBox{
    public <methods>;
}



#-keep public class com.adbert.AdbertNativeADView {
#    public protected <methods>;
#}
#
#-keep class com.adbert.AdbertNativeADView$* {
#    public protected <methods>;
#}

#-keepclasseswithmembers class * {
#    void onClick*(...);
#}
#
#-keepclasseswithmembers class * {
#    void onPrepared*(...);
#}
#
#-keepclasseswithmembers class * {
#    void onSeekComplete*(...);
#}
#
#-keepclasseswithmembers class * {
#    void onCompletion*(...);
#}