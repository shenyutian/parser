package org.syt.parser.aab

import com.android.tools.build.bundletool.model.AppBundle
import com.android.tools.build.bundletool.model.BundleModule
import com.android.tools.build.bundletool.model.utils.ResourcesUtils
import com.android.tools.build.bundletool.model.utils.xmlproto.XmlProtoAttribute
import com.android.tools.build.bundletool.model.utils.xmlproto.XmlProtoElement
import org.syt.parser.entry.*
import org.syt.parser.log.Log
import java.io.File
import java.lang.reflect.Field
import java.util.stream.Stream
import java.util.zip.ZipFile

/*
 * zhulei 2024/8/8-上午11:00
 */
object AabMetaTranslator {

    fun parse(aabFile: File): ApkMeta {
        val appBundle = AppBundle.buildFromZip(ZipFile(aabFile))
        val androidManifest = appBundle.baseModule.androidManifest

        val apkMeta = ApkMeta()

        androidManifest.manifestRoot.element.apply {
            attributes.forEach {
                when (it.name) {
                    "package" -> apkMeta.packageName = it.valueAsString
                    "versionCode" -> apkMeta.versionCode = it.valueAsString.toLong()
                    "versionName" -> apkMeta.versionName = it.valueAsString
                    "minSdkVersion" -> apkMeta.minSdkVersion = (it.valueAsString)
                    "platformBuildVersionCode" -> apkMeta.platformBuildVersionCode = (it.valueAsString)
                    "platformBuildVersionName" -> apkMeta.platformBuildVersionName = (it.valueAsString)
                    "compileSdkVersionCodename" -> apkMeta.compileSdkVersionCodename = (it.valueAsString)
                    "compileSdkVersion" -> apkMeta.compileSdkVersion = (it.valueAsString)
                    else -> {
                        Log.d("unknown attribute: ${it.name}")
                    }
                }
            }
            childrenElements.forEach {
                when (it.name) {
                    "application" -> {
                        fillData(it.attributes, apkMeta.application)
                        it.attributes.forEach { attribute ->
                            // todo 指向 res/string 下面的真实label
                            when (attribute.name) {
                                "label" -> apkMeta.label = attribute.valueAsString
                                "icon" -> apkMeta.icon = attribute.valueAsString
                                "debuggable" -> apkMeta.application.debuggable = attribute.valueAsBoolean
                                "allowBackup" -> apkMeta.application.allowBackup = attribute.valueAsBoolean
                                "supportsRtl" -> apkMeta.application.supportsRtl = attribute.valueAsBoolean
                                "largeHeap" -> apkMeta.application.largeHeap = attribute.valueAsBoolean
                                "hardwareAccelerated" -> apkMeta.application.hardwareAccelerated = attribute.valueAsBoolean
                                "usesCleartextTraffic" -> apkMeta.application.usesCleartextTraffic = attribute.valueAsBoolean
                                else -> {}
                            }
                        }
                        it.childrenElements.forEach {
                            var ap: AndroidComponent? = null
                            when (it.name) {
                                "activity" -> {
                                    ap = Activity()
                                    apkMeta.addActivity(ap)
                                }

                                "service" -> {
                                    ap = Service()
                                    apkMeta.addService(ap)
                                }

                                "receiver" -> {
                                    ap = Receiver()
                                    apkMeta.addReceiver(ap)
                                }

                                "provider" -> {
                                    ap = Provider()
                                    apkMeta.addProvider(ap)
                                }

                                "activity-alias" -> {
                                    ap = ActivityAlias()
                                    apkMeta.addActivityAlias(ap)
                                }

                                "meta-data" -> {
                                    val metaData = MetaData()
                                    fillData(it.attributes, metaData)
                                    apkMeta.application.addMetaData(metaData)
                                }

                                "uses-library" -> {
                                    var name = ""
                                    var required = ""
                                    it.attributes.forEach {
                                        when (it.name) {
                                            "name" -> name = it.valueAsString
                                            "required" -> required = it.valueAsString
                                            else -> {
                                                Log.d("unknown meta-data attribute: ${it.name}")
                                            }
                                        }
                                    }
                                    apkMeta.application.addUsesLibrary(name, required)
                                }

                                "property" -> {
                                    val property = Property()
                                    fillData(it.attributes, property)
                                    apkMeta.application.addProperty(property)
                                }

                                else -> {
                                    Log.d("unknown application child: ${it.name}")
                                }
                            }
                            if (ap != null) {
                                fillData(it.attributes, ap)
                                it.childrenElements.forEach {
                                    when (it.name) {
                                        "intent-filter" -> {
                                            ap.addIntentFilter(intentFilterTrans(it))
                                        }

                                        "property" -> {
                                            val property = Property()
                                            fillData(it.attributes, property)
                                            ap.addProperty(property)
                                        }

                                        "meta-data" -> {
                                            val metaData = MetaData()
                                            fillData(it.attributes, metaData)
                                            ap.addMetaData(metaData)
                                        }

                                        else -> {
                                            Log.d("unknown ${ap.javaClass.simpleName} child: ${it.name}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "uses-permission" -> {
                        it.attributes.forEach {
                            when (it.name) {
                                "name" -> apkMeta.addUsesPermission(it.valueAsString)
                                else -> {
                                    Log.d("unknown uses-permission attribute: ${it.name}")
                                }
                            }
                        }
                    }

                    "uses-sdk" -> {
                        it.attributes.forEach {
                            when (it.name) {
                                "minSdkVersion" -> apkMeta.minSdkVersion = it.valueAsString
                                "targetSdkVersion" -> apkMeta.targetSdkVersion = it.valueAsString
                                else -> {
                                    Log.d("unknown uses-sdk attribute: ${it.name}")
                                }
                            }
                        }
                    }

                    "permission" -> {
                        val permission = Permission()
                        fillData(it.attributes, permission)
                        apkMeta.addPermissions(permission)
                    }

                    "queries" -> {
                        it.attributes.forEach {
                            Log.d("unknown queries attr: ${it.name}")
                        }
                        it.childrenElements.forEach {
                            when (it.name) {
                                "package" -> {
                                    it.attributes.forEach {
                                        when (it.name) {
                                            "name" -> apkMeta.queries.packageName.add(it.valueAsString)
                                            else -> {
                                                Log.d("unknown package attr: ${it.name}")
                                            }
                                        }
                                    }
                                }

                                "provider" -> {
                                    it.attributes.forEach {
                                        when (it.name) {
                                            "authorities" -> apkMeta.queries.provider.add(it.valueAsString)
                                            else -> {
                                                Log.d("unknown provider attr: ${it.name}")
                                            }
                                        }
                                    }
                                }

                                "intent" -> {
                                    apkMeta.queries.addIntent(intentFilterTrans(it))
                                }
                            }
                        }
                    }

                    else -> {
                        Log.d("unknown root child: ${it.name}")
                    }
                }
            }
        }

        appBundle.modules.forEach { (bundleModuleName, bundleModule) ->
            readBundleModule(bundleModule, apkMeta)
        }

        return apkMeta;
    }

    private fun fillData(attributes: Stream<XmlProtoAttribute>, any: Any) {
        val declaredFieldAll = getAllFields(any.javaClass)
        attributes.forEach { attribute ->
            val fs = declaredFieldAll.filter { it.name == attribute.name }
            if (fs.isEmpty()) {
                Log.d("unknown invoke ${any.javaClass.simpleName} attribute: ${attribute.name}")
            } else {
                fs.forEach { field ->
                    field.isAccessible = true
                    if (field.type == String::class.java) {
                        field.set(any, attribute.valueAsString)
                        return@forEach
                    }
                    if (attribute.valueAsString.startsWith("@")) {
                        Log.w("无法处理 ${any.javaClass.simpleName} attribute: ${attribute.name} ${attribute.valueAsString}")
                    } else {
                        if (field.type == Boolean::class.java || field.type.toString() == "class java.lang.Boolean") {
                            field.set(any, attribute.valueAsBoolean)
                        } else if (field.type == Long::class.java || field.type.toString() == "class java.lang.Long") {
                            field.set(any, attribute.valueAsString.toLong())
                        } else if (field.type == Int::class.java || field.type.toString() == "class java.lang.Integer") {
                            field.set(any, attribute.valueAsInteger)
                        } else {
                            Log.d("unknown type ${any.javaClass.simpleName} attribute: ${attribute.name} ${field.type}")
                        }
                    }
                }
            }
        }
    }

    fun getAllFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()

        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }

        return fields
    }

    fun intentFilterTrans(xmlProtoElement: XmlProtoElement): IntentFilter {
        val intentFilter = IntentFilter()
        fillData(xmlProtoElement.attributes, intentFilter)
        xmlProtoElement.childrenElements.forEach {
            when (it.name) {
                "action" -> {
                    it.attributes.forEach {
                        when (it.name) {
                            "name" -> intentFilter.addAction(it.valueAsString)
                            else -> {
                                Log.d("unknown action attribute: ${it.name}")
                            }
                        }
                    }
                }

                "category" -> {
                    it.attributes.forEach {
                        when (it.name) {
                            "name" -> intentFilter.addCategory(it.valueAsString)
                            else -> {
                                Log.d("unknown category attribute: ${it.name}")
                            }
                        }
                    }
                }
            }
        }
        return intentFilter
    }

    private fun readBundleModule(bundleModule: BundleModule, apkMeta: ApkMeta) {

        val appName = apkMeta.label?.split("/")?.last()
        if (appName != null) {
            ResourcesUtils.entries(bundleModule.resourceTable.get()).forEach { entry ->
                val entryName = entry.entry.name
                if (appName.equals(entryName)) {
                    // 多语言没问题，todo res混淆 读取不到应用的包名等信息
                    Log.d("entry: $entryName  ${entry.entry.configValueList[0].value.item.str.value}")
                    apkMeta.label = entry.entry.configValueList[0].value.item.str.value
                }
                Log.d("entry: $entryName ")
            }
        }
    }

}