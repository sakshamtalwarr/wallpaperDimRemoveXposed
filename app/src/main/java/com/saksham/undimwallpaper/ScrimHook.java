package com.saksham.undimwallpaper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ScrimHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        try {
            // Target the ScrimController to force transparency
            Class<?> scrimController = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.ScrimController", 
                lpparam.classLoader
            );
            
            // Hook EVERY method that could possibly set the scrim alpha
            XposedBridge.hookAllMethods(scrimController, "setScrimAlpha", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Force the alpha to 0.0, which means 100% transparent/no dimming
                    param.args[param.args.length - 1] = 0.0f;
                }
            });

            // Additional hook to catch state changes
            XposedBridge.hookAllMethods(scrimController, "applyInternalResources", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null); 
                }
            });

        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
