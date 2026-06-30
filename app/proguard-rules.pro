# ===== Room =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * implements androidx.room.Dao
-keep @androidx.room.Query class *
-dontwarn androidx.room.paging.**

# ===== Moshi =====
-keep @com.squareup.moshi.JsonClass class *
-keep class com.example.data.ExtractedHabit { *; }
-keep class com.example.data.GenerateContentRequest { *; }
-keep class com.example.data.GenerateContentResponse { *; }
-keep class com.example.data.Content { *; }
-keep class com.example.data.Part { *; }
-keep class com.example.data.GenerationConfig { *; }
-keep class com.example.data.ResponseSchema { *; }
-keep class com.example.data.Candidate { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ===== Retrofit =====
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.**
-dontwarn retrofit2.**

# ===== OkHttp =====
-dontwarn okhttp3.**
-dontwarn okio.**

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===== Compose =====
-dontwarn androidx.compose.**

# ===== General Android =====
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
