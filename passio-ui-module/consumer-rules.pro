-dontwarn java.lang.invoke.StringConcatFactory
-keepclassmembers class ai.passio.nutrition.uimodule.NutritionUIConfiguration {
    public <fields>;
}

#-keep class ai.passio.nutrition.** { *; }
#-keep class ai.passio.passiosdk.** { *; }
#-keep class ai.passio.nutrition.uimodule.** { *;}
#-keepclassmembers class ai.passio.nutrition.uimodule.** {
#    *;
#}

-keep class ai.passio.passiosdk.** { *; }
-keep class ai.passio.nutrition.uimodule.ui.** { *;}
-keep class ai.passio.nutrition.uimodule.data.db.entity.** { *;}
-keep class ai.passio.nutrition.uimodule.data.db.mapper.** { *;}
-keep class ai.passio.nutrition.uimodule.data.db.typeconverter.** { *;}
-keep class ai.passio.nutrition.uimodule.ui.model.** { *;}
-keep class ai.passio.nutrition.uimodule.NutritionUIConfiguration {*;}
-keep class ai.passio.nutrition.uimodule.NutritionUIModule {*;}
-keep class ai.passio.nutrition.uimodule.data.PassioConnector {*;}
#-dontwarn ai.passio.nutrition.uimodule.NutritionUIConfiguration
#-dontwarn ai.passio.nutrition.uimodule.NutritionUIModule
#-dontwarn ai.passio.nutrition.uimodule.data.PassioConnector


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken