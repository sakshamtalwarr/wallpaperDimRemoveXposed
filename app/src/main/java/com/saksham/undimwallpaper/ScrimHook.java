package com.saksham.undimwallpaper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ScrimHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // Ensure this only injects into the System UI framework
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        try {
            // Target the core Android PowerManager framework class
            Class<?> powerManager = XposedHelpers.findClass(
                "android.os.PowerManager", 
                lpparam.classLoader
            );
            
            // Intercept the method that checks if Battery Saver is enabled
            XposedBridge.hookAllMethods(powerManager, "isPowerSaveMode", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Force the system to return FALSE.
                    // SystemUI will now believe Battery Saver is completely OFF at all times,
                    // retaining full blurs, bright wallpapers, and natural UI rendering.
                    param.setResult(false);
                }
            });

        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
