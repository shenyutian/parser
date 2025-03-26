package org.syt.parser.entry

/*
 * zhulei 2024/8/8-下午3:29
 *
 * <receiver android:directBootAware=["true" | "false"]
          android:enabled=["true" | "false"]
          android:exported=["true" | "false"]
          android:icon="drawable resource"
          android:label="string resource"
          android:name="string"
          android:permission="string"
          android:process="string" >
    ...
</receiver>
*
 */
data class Receiver(
    var directBootAware: Boolean = false,
) : AndroidComponent() {
    override fun toString(): String {
        return "Receiver: $name"
    }
}
