# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep FFmpeg decoder classes
-keep class androidx.media3.exoplayer.ext.ffmpeg.** { *; }

# Keep model classes used for JSON/XML parsing
-keep class com.github.cgang.myiptv.Channel { *; }
-keep class com.github.cgang.myiptv.Playlist { *; }
-keep class com.github.cgang.myiptv.xmltv.** { *; }
-keep class com.github.cgang.myiptv.smil.** { *; }

# Keep Kotlin metadata
-keepAttributes *Annotation*
-keepclassmembers class ** {
    @org.jetbrains.annotations.NotNull *;
    @org.jetbrains.annotations.Nullable *;
}

# Keep network interface classes
-keep class java.net.NetworkInterface { *; }