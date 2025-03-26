package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午2:29
 *
 * <activity android:allowEmbedded=["true" | "false"]
          android:allowTaskReparenting=["true" | "false"]
          android:alwaysRetainTaskState=["true" | "false"]
          android:autoRemoveFromRecents=["true" | "false"]
          android:banner="drawable resource"
          android:canDisplayOnRemoteDevices=["true" | "false"]
          android:clearTaskOnLaunch=["true" | "false"]
          android:colorMode=[ "hdr" | "wideColorGamut"]
          android:configChanges=["colorMode", "density",
                                 "fontScale", "fontWeightAdjustment",
                                 "grammaticalGender", "keyboard",
                                 "keyboardHidden", "layoutDirection", "locale",
                                 "mcc", "mnc", "navigation", "orientation",
                                 "screenLayout", "screenSize",
                                 "smallestScreenSize", "touchscreen", "uiMode"]
          android:directBootAware=["true" | "false"]
          android:documentLaunchMode=["intoExisting" | "always" |
                                  "none" | "never"]
          android:enabled=["true" | "false"]
          android:enabledOnBackInvokedCallback=["true" | "false"]
          android:excludeFromRecents=["true" | "false"]
          android:exported=["true" | "false"]
          android:finishOnTaskLaunch=["true" | "false"]
          android:hardwareAccelerated=["true" | "false"]
          android:icon="drawable resource"
          android:immersive=["true" | "false"]
          android:label="string resource"
          android:launchMode=["standard" | "singleTop" |
                              "singleTask" | "singleInstance" | "singleInstancePerTask"]
          android:lockTaskMode=["normal" | "never" |
                              "if_whitelisted" | "always"]
          android:maxRecents="integer"
          android:maxAspectRatio="float"
          android:multiprocess=["true" | "false"]
          android:name="string"
          android:noHistory=["true" | "false"]
          android:parentActivityName="string"
          android:persistableMode=["persistRootOnly" |
                                   "persistAcrossReboots" | "persistNever"]
          android:permission="string"
          android:process="string"
          android:relinquishTaskIdentity=["true" | "false"]
          android:requireContentUriPermissionFromCaller=["none" | "read" | "readAndWrite" |
                                                         "readOrWrite" | "write"]
          android:resizeableActivity=["true" | "false"]
          android:screenOrientation=["unspecified" | "behind" |
                                     "landscape" | "portrait" |
                                     "reverseLandscape" | "reversePortrait" |
                                     "sensorLandscape" | "sensorPortrait" |
                                     "userLandscape" | "userPortrait" |
                                     "sensor" | "fullSensor" | "nosensor" |
                                     "user" | "fullUser" | "locked"]
          android:showForAllUsers=["true" | "false"]
          android:stateNotNeeded=["true" | "false"]
          android:supportsPictureInPicture=["true" | "false"]
          android:taskAffinity="string"
          android:theme="resource or theme"
          android:uiOptions=["none" | "splitActionBarWhenNarrow"]
          android:windowSoftInputMode=["stateUnspecified",
                                       "stateUnchanged", "stateHidden",
                                       "stateAlwaysHidden", "stateVisible",
                                       "stateAlwaysVisible", "adjustUnspecified",
                                       "adjustResize", "adjustPan"] >
    ...
</activity>
*
* 可能包含
* <intent-filter>
<meta-data>
<layout>

 */
data class Activity(
    private val allowEmbedded: Boolean? = null,
    private val allowTaskReparenting: Boolean? = null,
    private val alwaysRetainTaskState: Boolean? = null,
    private val autoRemoveFromRecents: Boolean? = null,
    private val banner: String? = null,
    private val canDisplayOnRemoteDevices: Boolean? = null,
    private val clearTaskOnLaunch: Boolean? = null,
    private val colorMode: String? = null,
    private val configChanges: String? = null,
    private val directBootAware: Boolean? = null,
    private val documentLaunchMode: String? = null,
    private val enabledOnBackInvokedCallback: Boolean? = null,
    private val excludeFromRecents: Boolean? = null,
    private val finishOnTaskLaunch: Boolean? = null,
    private val hardwareAccelerated: Boolean? = null,
    private val immersive: Boolean? = null,
    private val launchMode: String? = null,
    private val lockTaskMode: String? = null,
    private val maxRecents: Long? = null,
    private val maxAspectRatio: Float? = null,
    private val multiprocess: Boolean? = null,
    private val noHistory: Boolean? = null,
    private val parentActivityName: String? = null,
    private val persistableMode: String? = null,
    private val relinquishTaskIdentity: Boolean? = null,
    private val requireContentUriPermissionFromCaller: String? = null,
    private val resizeableActivity: Boolean? = null,
    private val screenOrientation: String? = null,
    private val showForAllUsers: Boolean? = null,
    private val stateNotNeeded: Boolean? = null,
    private val supportsPictureInPicture: Boolean? = null,
    private val taskAffinity: String? = null,
    private val theme: String? = null,
    private val uiOptions: String? = null,
    private val windowSoftInputMode: String? = null,
    private val layoutDirection: String? = null,
    private val windowLayoutDirection: String? = null,
    private val windowIsTranslucent: Boolean? = null,
    private val windowIsFloating: Boolean? = null,
    private val windowIsTransient: Boolean? = null,
    private val fitsSystemWindows: String? = null,
) : AndroidComponent() {
    override fun toString(): String {
        return "Activity $name"
    }
}