package org.syt.parser.base

import org.syt.parser.entry.ApkMeta
import org.syt.parser.entry.DexClass
import org.syt.parser.entry.IconFace
import org.syt.parser.json.JSONObject
import org.syt.parser.log.Log
import java.io.IOException

/*
 * zhulei 2025/3/16-22:57
 */
abstract class BaseApkFile {

    abstract fun getApkMeta() : ApkMeta

    @Throws(IOException::class)
    abstract fun getDexClasses() : Array<DexClass>

    @Throws(IOException::class)
    abstract fun getAllIcons() : List<IconFace>

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
            val adFrameworks = mapOf(
                "isUnityGame" to "com/unity3d/player",
                "isUnityAds" to "com/unity3d/ads",
                "isFacebookAds" to "com/facebook/ads",
                "isGoogleAdmob" to "com/google/ads",
                "isIronsource" to "com/ironsource",
                "isMintegral" to "com/mbridge",
                "isApplovin" to "com/applovin",
                "isAdjust" to "com/adjust/sdk",
                "isAppsflyer" to "com/appsflyer",
                "isFirebaseAnalytics" to "com/google/firebase/analytics/FirebaseAnalytics",
                "isFacebookAnalytics" to "com/facebook/appevents",
            )
            
            val results = mutableMapOf<String, Boolean>().apply {
                adFrameworks.forEach { (key, _) -> this[key] = false }
            }
            
            getDexClasses().forEach { dc ->
                Log.d(dc.toString())
                adFrameworks.forEach { (key, path) ->
                    if (!results[key]!! && dc.toString().contains(path)) {
                        results[key] = true
                    }
                }
            }
            
            results.forEach { (key, value) ->
                jsonObject.putOpt(key, value)
            }
        } catch (e: Exception) {
            Log.e(e);
        }
        return jsonObject
    }

}