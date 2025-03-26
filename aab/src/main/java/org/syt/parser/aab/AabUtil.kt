package org.syt.parser.aab

import com.android.tools.build.bundletool.io.AppBundleSerializer
import com.android.tools.build.bundletool.model.AndroidManifest
import com.android.tools.build.bundletool.model.AppBundle
import com.android.tools.build.bundletool.model.BundleModule
import com.android.tools.build.bundletool.model.BundleModuleName
import com.android.tools.build.bundletool.model.utils.ResourcesUtils
import com.android.tools.build.bundletool.model.utils.xmlproto.XmlProtoNode
import com.android.tools.build.bundletool.model.version.BundleToolVersion
import com.google.common.collect.ImmutableMap
import org.syt.parser.log.Log
import java.io.File
import java.util.*
import java.util.zip.ZipFile


/*
 * zhulei 2024/12/11-17:53
 */
object AabUtil {

    fun editAabPackageName(releaseAab: File, resultAab: File, packageName: String) {
        // 执行命令
        val appBundle: AppBundle = AppBundle.buildFromZip(ZipFile(releaseAab))
        val tesAppBundle = modifyAab(appBundle, packageName)
        // 转换打包
        repackaging(tesAppBundle, resultAab)
    }

    /**
     * 修改生成新的aab文件 支持移除firebase信息
     */
    private fun modifyAab(appBundle: AppBundle, packageName: String): AppBundle {
        val obfuscatedModules: HashMap<BundleModuleName, BundleModule> = HashMap()
        appBundle.modules.forEach { (bundleModuleName, bundleModule) ->
            var module = modifyPkg(bundleModule, packageName)
            module = modifyFirebase(module)
            obfuscatedModules[bundleModuleName] = module
        }


        return appBundle.toBuilder().setModules(ImmutableMap.copyOf(obfuscatedModules)).build()
    }

    private fun modifyPkg(bundleModule: BundleModule, packageName: String): BundleModule {
        val androidManifest = bundleModule.androidManifest
        val manifestBuilder = androidManifest.manifestRoot.element.toBuilder()
        var pkg = ""
        // 修改包名
        manifestBuilder.attributes.forEach {
            if (it.name == "package") {
                pkg = it.valueAsString
                it.setValueAsString(packageName)
            }
            if (it.name == "android:versionName") {
                // 修改 android:versionName 加上 test
                it.setValueAsString(it.valueAsString + "test")
            }
        }
        try {
            // application 里面修改？
            // 如果是买量包，就需要修改里面   <provider android:authorities="包名.firebaseinitprovider"
            manifestBuilder.childrenElements
                .filter { it.name.equals("application") }
                .findFirst()
                .get()
                .childrenElements
                .forEach {
                    it.attributes.forEach {
                        if (it.name.equals("android:authorities") && it.valueAsString.contains(pkg)) {
                            it.setValueAsString(it.valueAsString.replace(pkg, packageName))
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(e, "RunAabCheckTask modifyPkg error")
        }

        val builder = bundleModule.toBuilder()
        builder.setAndroidManifest(
            AndroidManifest.create(
                XmlProtoNode.createElementNode(manifestBuilder.build()),
                BundleToolVersion.getCurrentVersion()
            )
        )
        return builder.build()
    }

    private fun modifyFirebase(bundleModule: BundleModule): BundleModule {
        val resourceTableBuilder = ResourceTableBuilder()
        ResourcesUtils.entries(bundleModule.resourceTable.get()).forEach { entry ->
            val string = entry.entry.name
            // gcm_defaultSenderId google_app_id google_api_key google_crash_reporting_api_key google_storage_bucket project_id
            val find = FirebaseKey.stringKeys.find { it == string }
            if (find != null) {
                println(" find $string ")
                // firebase 清理id
                val entryBuilder = entry.entry.toBuilder()
                entryBuilder.clearConfigValue()
            } else {
                resourceTableBuilder.addPackage(entry.getPackage()).addResource(entry.type, entry.entry)
            }
        }
        return bundleModule.toBuilder().setResourceTable(resourceTableBuilder.build()).build()
    }

    /**
     * 重新打包
     */
    private fun repackaging(appBundle: AppBundle, output: File) {
        val appBundleSerializer = AppBundleSerializer()
        output.delete()
        appBundleSerializer.writeToDisk(appBundle, output.toPath())
    }

}