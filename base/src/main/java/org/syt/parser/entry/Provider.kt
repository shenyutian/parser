package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午3:41
 *
 * <provider android:authorities="list"
          android:directBootAware=["true" | "false"]
          android:enabled=["true" | "false"]
          android:exported=["true" | "false"]
          android:grantUriPermissions=["true" | "false"]
          android:icon="drawable resource"
          android:initOrder="integer"
          android:label="string resource"
          android:multiprocess=["true" | "false"]
          android:name="string"
          android:permission="string"
          android:process="string"
          android:readPermission="string"
          android:syncable=["true" | "false"]
          android:writePermission="string" >
    ...
</provider>
*
 */
data class Provider(
    var authorities: String? = null,
    var grantUriPermissions: Boolean? = null,
    var multiprocess: Boolean? = null,
    var syncable: Boolean? = null,
    var readPermission: String? = null,
    var writePermission: String? = null,
    var initOrder: Int? = null,
    var directBootAware: Boolean? = null,
) : AndroidComponent() {

    override fun toString(): String {
        return "Provider $name"
    }

}
