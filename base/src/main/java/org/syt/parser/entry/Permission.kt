package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午3:28
 *
 * <permission android:description="string resource"
            android:icon="drawable resource"
            android:label="string resource"
            android:name="string"
            android:permissionGroup="string"
            android:protectionLevel=["normal" | "dangerous" |
                                     "signature" | ...] />
                                     *
 */
data class Permission(
    private val name: String? = null,
    private val label: String? = null,
    private val icon: String? = null,
    private val description: String? = null,
    private val group: String? = null,
    private val protectionLevel: String? = null,
) {
    
}
