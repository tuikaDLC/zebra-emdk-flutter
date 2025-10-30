# Zebra EMDK
-keep class com.zebra.** { *; }
-dontwarn com.zebra.**
-keepclassmembers class com.zebra.** { *; }

# RFID API
-keep class com.symbol.** { *; }
-dontwarn com.symbol.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
