package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午2:30
 *
 * <service android:description="string resource"
         android:directBootAware=["true" | "false"]
         android:enabled=["true" | "false"]
         android:exported=["true" | "false"]
         android:foregroundServiceType=["camera" | "connectedDevice" |
                                        "dataSync" | "health" | "location" |
                                        "mediaPlayback" | "mediaProjection" |
                                        "microphone" | "phoneCall" |
                                        "remoteMessaging" | "shortService" |
                                        "specialUse" | "systemExempted"]
         android:icon="drawable resource"
         android:isolatedProcess=["true" | "false"]
         android:label="string resource"
         android:name="string"
         android:permission="string"
         android:process="string" >
    ...
</service>
*
 */
data class Service(
    val description: String? = null,
    val directBootAware: Boolean? = null,
    val foregroundServiceType: String? = null,
    val isolatedProcess: Boolean? = null,
    val visibleToInstantApps: Boolean? = null,
) : AndroidComponent() {

    override fun toString(): String {
        return "Service $name"
    }

}