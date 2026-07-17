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
            // Target the ScrimController
            Class<?> scrimController = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.ScrimController", 
                lpparam.classLoader
            );
            
            // Hook the method that applies the dimming alpha
            XposedBridge.hookAllMethods(scrimController, "setScrimAlpha", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Force the alpha value to 0.0 (completely transparent)
                    // The argument for alpha is typically the last float in the method signature
                    if (param.args.length > 0) {
                        param.args[param.args.length - 1] = 0.0f;
                    }
                }
            });

        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
