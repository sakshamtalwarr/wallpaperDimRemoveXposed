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
            // ==========================================
            // FIX 1: Prevent Wallpaper Dimming (Scrims)
            // ==========================================
            Class<?> scrimController = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.ScrimController", 
                lpparam.classLoader
            );
            
            XposedBridge.hookAllMethods(scrimController, "setScrimAlpha", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length > 1 && param.args[1] instanceof Float) {
                        param.args[1] = 0.0f; 
                    } else if (param.args.length > 0 && param.args[0] instanceof Float) {
                        param.args[0] = 0.0f; 
                    }
                }
            });

            // ==========================================
            // FIX 2: Force Notification Blur (Translucency)
            // ==========================================
            Class<?> blurListeners = XposedHelpers.findClassIfExists(
                "android.view.CrossWindowBlurListeners", 
                lpparam.classLoader
            );
            
            if (blurListeners != null) {
                XposedBridge.hookAllMethods(blurListeners, "isCrossWindowBlurEnabled", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // Intercept the check and force Android to keep blurs turned ON
                        param.setResult(true); 
                    }
                });
            }

        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
