package com.oosl.colorostool;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.coloros.safecenter")) {
            //去除只能开启5个应用自启动的限制
            ColorOSToolLog("Hook safecenter success!");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> clazz;
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                try {
                    clazz = cl.loadClass("com.coloros.safecenter.startupapp.b");
                    ColorOSToolLog("Hook safecenter.startupapp.b success!");
                } catch (Exception e) {
                    return;
                }
                XposedHelpers.findAndHookMethod(clazz, "c", Context.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        if(param == null)
                            return null;
                        XposedHelpers.setStaticIntField(clazz, "b", 114514);
                        Log.d("StartupManager", "update max allow count ? " + 114514);
                        ColorOSToolLog("After Hook c ! the max startup allowed app is " + XposedHelpers.getStaticIntField(clazz, "b"));
                        return null;
                    }
                });
                }
            });
        }else if (lpparam.packageName.equals("com.oppo.launcher")) {
            // 去除多任务后台只能锁定5个的限制
            ColorOSToolLog("Hook oppoLauncher success!");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> clazz;
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                try {
                    clazz = cl.loadClass("com.coloros.quickstep.applock.ColorLockManager");
                    ColorOSToolLog("Hook launcher.quickstep.applock.ColorLockManager success!");
                } catch (Exception e) {
                    return;
                }
                XposedHelpers.findAndHookConstructor(clazz, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedHelpers.setIntField(param.thisObject, "mLockAppLimit", 114514);
                        ColorOSToolLog("Hook launcher.~.ColorLockManager Constructor success!");
                    }
                });
                }
            });
        }else if (lpparam.packageName.equals("com.android.packageinstaller")) {
            ColorOSToolLog("Hook packageinstaller success!");
            Class<?> clazz, clazz2;
            // 去除安装前的验证
            clazz = lpparam.classLoader.loadClass("com.android.packageinstaller.oplus.OPlusPackageInstallerActivity");
            XposedHelpers.findAndHookMethod(clazz, "continueOppoSafeInstall", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    ColorOSToolLog("replace OppoSafeInstall() OK!!");
                    XposedHelpers.callMethod(param.thisObject,"continueAppInstall");
                    return null;
                }
            });
            // 使用原生安装器而非OPPO自己写的, false暂时禁用
            if(false) {
                clazz2 = lpparam.classLoader.loadClass("com.android.packageinstaller.oplus.common.FeatureOption");
                ColorOSToolLog("sIsClosedSuperFirewall is " + XposedHelpers.getStaticBooleanField(clazz2, "sIsClosedSuperFirewall"));
                XposedHelpers.findAndHookMethod(clazz, "setIsClosedSuperFirewall", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedHelpers.setStaticBooleanField(clazz2, "sIsClosedSuperFirewall", true);
                        ColorOSToolLog("after sIsClosedSuperFirewall is " + XposedHelpers.getStaticBooleanField(clazz2, "sIsClosedSuperFirewall"));
                    }
                });
            }
        }
    }

    private static void ColorOSToolLog(String str){
        final String TAG = "ColorOSTool";

        XposedBridge.log(TAG + ": " + str);
        //Log.d(TAG,str);
    }
}