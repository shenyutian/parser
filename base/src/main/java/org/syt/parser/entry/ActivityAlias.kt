package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午3:51
 *
 * <activity-alias android:enabled=["true" | "false"]
                android:exported=["true" | "false"]
                android:icon="drawable resource"
                android:label="string resource"
                android:name="string"
                android:permission="string"
                android:targetActivity="string" >
    ...
</activity-alias>
 */
data class ActivityAlias(
    private val targetActivity: String? = null,
) : AndroidComponent() {
    override fun toString(): String {
        return "ActivityAlias $name"
    }
}