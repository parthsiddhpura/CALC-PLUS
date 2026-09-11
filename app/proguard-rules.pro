# ProGuard / R8 Optimization Configuration for Google Play Console

# Aggressive Optimization & Code Inlining
-allowaccessmodification
-repackageclasses ''

# Preserve line numbers and file names for Play Console crash symbolication / deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Room entities, DAOs, and database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep Application Models & Enums used in serialization and storage
-keep class com.example.model.** { *; }
-keepclassmembers enum com.example.model.** { *; }
-keepclassmembers class com.example.model.** {
    <fields>;
    <init>(...);
}

# Keep Moshi JSON Adapters & Models
-keep class *JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep Lifecycle ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Kotlin Coroutines and Reflection suppression for unused features
-dontwarn kotlinx.coroutines.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**

