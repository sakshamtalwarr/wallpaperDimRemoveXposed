package com.saksham.undimwallpaper;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
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
            // ==========================================
            // FIX 1: Prevent Wallpaper Dimming
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
            // FIX 2: Force Translucent Notification Background
            // ==========================================
            Class<?> notificationPanel = XposedHelpers.findClassIfExists(
                "com.android.systemui.qs.QSPanelBackground", 
                lpparam.classLoader
            );
            
            if (notificationPanel == null) {
                // Fallback for different Pixel framework structures
                notificationPanel = XposedHelpers.findClassIfExists(
                    "com.android.systemui.qs.QSContainerImpl", 
                    lpparam.classLoader
                );
            }

            if (notificationPanel != null) {
                XposedBridge.hookAllMethods(notificationPanel, "updateBackground", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        // Force a 75% transparent dark background instead of solid opaque
                        int translucentDark = Color.argb(190, 20, 20, 20); 
                        view.setBackground(new ColorDrawable(translucentDark));
                    }
                });
            }

        } catch (Throwable t) {
            XposedBridge.log("BrightWallpaperFix Error: " + t.getMessage());
        }
    }
}
