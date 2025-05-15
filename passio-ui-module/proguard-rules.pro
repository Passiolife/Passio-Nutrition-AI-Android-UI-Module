# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
-dontwarn java.lang.invoke.StringConcatFactory
-keepclassmembers class ai.passio.nutrition.uimodule.NutritionUIConfiguration {
    public <fields>;
}


#-keep class ai.passio.passiosdk.** { *; }
#-keep class ai.passio.nutrition.uimodule.data.db.entity.** { *;}
#-keep class ai.passio.nutrition.uimodule.data.db.mapper.** { *;}
#-keep class ai.passio.nutrition.uimodule.data.db.typeconverter.** { *;}
#-keep class ai.passio.nutrition.uimodule.ui.model.** { *;}
#-keep class ai.passio.nutrition.uimodule.NutritionUIConfiguration {*;}
#-keep class ai.passio.nutrition.uimodule.NutritionUIModule {*;}
#-keep class ai.passio.nutrition.uimodule.data.PassioConnector {*;}
#-dontwarn ai.passio.nutrition.uimodule.NutritionUIConfiguration
#-dontwarn ai.passio.nutrition.uimodule.NutritionUIModule
#-dontwarn ai.passio.nutrition.uimodule.data.PassioConnector