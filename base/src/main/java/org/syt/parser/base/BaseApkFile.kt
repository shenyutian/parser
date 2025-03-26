package org.syt.parser.base

import org.syt.parser.entry.ApkMeta
import org.syt.parser.entry.DexClass
import org.syt.parser.json.JSONObject
import org.syt.parser.log.Log
import org.syt.parser.log.Log.e
import java.io.IOException
import java.security.PublicKey


/*
 * zhulei 2025/3/16-22:57
 */
abstract class BaseApkFile {

    abstract fun getApkMeta() : ApkMeta

    @Throws(IOException::class)
    abstract fun getDexClasses() : Array<DexClass>

    open fun getInfo() : JSONObject {
        val jsonObject = JSONObject();

        try {
            val apkMeta = getApkMeta();
            jsonObject.putOpt("pkg", apkMeta.packageName);
            jsonObject.putOpt("label", apkMeta.label);
            jsonObject.putOpt("launcher", apkMeta.getLauncher()?.name);
            jsonObject.putOpt("version", apkMeta.versionName);
            jsonObject.putOpt("versionCode", apkMeta.versionCode);
            jsonObject.putOpt("MinSdkVersion", apkMeta.minSdkVersion);
            jsonObject.putOpt("MaxSdkVersion", apkMeta.maxSdkVersion);
            jsonObject.putOpt("TargetSdkVersion", apkMeta.targetSdkVersion);
        } catch (e: Exception) {
            Log.e(e);
        }

        try {
            var isUnity = false;
            var isUnityAds = false;
            var isFacebookAds = false;
            var isGoogleAdmob = false;
            var isIronsource = false;
            var isMintegral = false;
            var isApplovin = false;
            getDexClasses().forEach { dc ->
                if (dc.toString().contains("com/unity3d/player")) {
                    isUnity = true;
                }
                if (dc.toString().contains("com/unity3d/ads")) {
                    isUnityAds = true;
                }
                if (dc.toString().contains("com/facebook/ads")) {
                    isFacebookAds = true;
                }
                if (dc.toString().contains("com/google/ads")) {
                    isGoogleAdmob = true;
                }
                if (dc.toString().contains("com/ironsource")) {
                    isIronsource = true;
                }
                if (dc.toString().contains("com/mintegral")) {
                    isMintegral = true;
                }
                if (dc.toString().contains("com/applovin")) {
                    isApplovin = true;
                }
            }
            jsonObject.putOpt("isUnityGame", isUnity);
            jsonObject.putOpt("isUnityAds", isUnityAds);
            jsonObject.putOpt("isFacebookAds", isFacebookAds);
            jsonObject.putOpt("isGoogleAdmob", isGoogleAdmob);
            jsonObject.putOpt("isIronsource", isIronsource);
            jsonObject.putOpt("isMintegral", isMintegral);
            jsonObject.putOpt("isApplovin", isApplovin);
        } catch (e: Exception) {
            Log.e(e);
        }
        return jsonObject
    }

}