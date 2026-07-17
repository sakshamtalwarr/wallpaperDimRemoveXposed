package com.saksham.undimwallpaper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ScrimHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // We only want to modify SystemUI
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        try {
            // Target the class responsible for the dark overlays
            Class<?> scrimController = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.ScrimController", 
                lpparam.classLoader
            );
            
            // Hook all methods dealing with scrim alpha (transparency)
            XposedBridge.hookAllMethods(scrimController, "setScrimAlpha", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Method signature usually takes (ScrimView scrim, float alpha).
                    // We check the arguments dynamically to make it crash-proof.
                    if (param.args.length > 1 && param.args[1] instanceof Float) {
                        param.args[1] = 0.0f; // Force completely transparent
                    } else if (param.args.length > 0 && param.args[0] instanceof Float) {
                        param.args[0] = 0.0f; 
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
