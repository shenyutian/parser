package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午3:33
 *
 * <application android:allowTaskReparenting=["true" | "false"]
             android:allowBackup=["true" | "false"]
             android:allowClearUserData=["true" | "false"]
             android:allowNativeHeapPointerTagging=["true" | "false"]
             android:appCategory=["accessibility" | "audio" | "game" |
             "image" | "maps" | "news" | "productivity" | "social" | "video"]
             android:backupAgent="string"
             android:backupInForeground=["true" | "false"]
             android:banner="drawable resource"
             android:dataExtractionRules="string resource"
             android:debuggable=["true" | "false"]
             android:description="string resource"
             android:enabled=["true" | "false"]
             android:extractNativeLibs=["true" | "false"]
             android:fullBackupContent="string"
             android:fullBackupOnly=["true" | "false"]
             android:gwpAsanMode=["always" | "never"]
             android:hasCode=["true" | "false"]
             android:hasFragileUserData=["true" | "false"]
             android:hardwareAccelerated=["true" | "false"]
             android:icon="drawable resource"
             android:isGame=["true" | "false"]
             android:isMonitoringTool=["parental_control" | "enterprise_management" |
             "other"]
             android:killAfterRestore=["true" | "false"]
             android:largeHeap=["true" | "false"]
             android:label="string resource"
             android:logo="drawable resource"
             android:manageSpaceActivity="string"
             android:name="string"
             android:networkSecurityConfig="xml resource"
             android:permission="string"
             android:persistent=["true" | "false"]
             android:process="string"
             android:restoreAnyVersion=["true" | "false"]
             android:requestLegacyExternalStorage=["true" | "false"]
             android:requiredAccountType="string"
             android:resizeableActivity=["true" | "false"]
             android:restrictedAccountType="string"
             android:supportsRtl=["true" | "false"]
             android:taskAffinity="string"
             android:testOnly=["true" | "false"]
             android:theme="resource or theme"
             android:uiOptions=["none" | "splitActionBarWhenNarrow"]
             android:usesCleartextTraffic=["true" | "false"]
             android:vmSafeMode=["true" | "false"] >
    . . .
</application>
*
 */
data class Application(
    var allowTaskReparenting: Boolean = false,
    var allowBackup: Boolean = false,
    var allowClearUserData: Boolean = false,
    var allowNativeHeapPointerTagging: Boolean = false,
    var appCategory: String? = null,
    var backupAgent: String? = null,
    var backupInForeground: Boolean = false,
    var banner: String? = null,
    var dataExtractionRules: String? = null,
    var debuggable: Boolean = false,
    var description: String? = null,
    var extractNativeLibs: Boolean = false,
    var fullBackupContent: String? = null,
    var fullBackupOnly: Boolean = false,
    var gwpAsanMode: String? = null,
    var hasCode: Boolean = true,
    var hasFragileUserData: Boolean = false,
    var hardwareAccelerated: Boolean = false,
    var isGame: Boolean = false,
    var isMonitoringTool: String? = null,
    var killAfterRestore: Boolean = false,
    var largeHeap: Boolean = false,
    var logo: String? = null,
    var roundIcon: String? = null,
    var appComponentFactory: String? = null,
    var manageSpaceActivity: String? = null,
    var networkSecurityConfig: String? = null,
    var persistent: Boolean = false,
    var restoreAnyVersion: Boolean = false,
    var requestLegacyExternalStorage: Boolean = false,
    var requiredAccountType: String? = null,
    var resizeableActivity: Boolean = false,
    var restrictedAccountType: String? = null,
    var supportsRtl: Boolean = false,
    var taskAffinity: String? = null,
    var testOnly: Boolean = false,
    var theme: String? = null,
    var uiOptions: String? = null,
    var usesCleartextTraffic: Boolean = false,
    var vmSafeMode: Boolean = false,
    val usesLibrary: MutableMap<String, String> = mutableMapOf(),
) : AndroidComponent() {

    fun addUsesLibrary(key: String, value: String) {
        usesLibrary[key] = value
    }

    override fun toString(): String {
        return "Application $name"
    }
}