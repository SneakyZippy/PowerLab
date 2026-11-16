# Proguard rules for Battery Diagnostics
-keepclassmembers class com.android.internal.os.PowerProfile {
    public <init>(...);
    public double getAveragePower(...);
}
-keep class com.sneakyzippy.powerlab.data.db.** { *; }
-keep class com.sneakyzippy.powerlab.core.permission.ShizukuShellService { *; }
-keep class com.sneakyzippy.powerlab.IShizukuShellService { *; }
-keep class com.sneakyzippy.powerlab.IShizukuShellService$** { *; }
