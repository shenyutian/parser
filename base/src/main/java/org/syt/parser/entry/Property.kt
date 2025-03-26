package org.syt.parser.entry


/*
 * zhulei 2024/8/8-下午6:16
 *
 * <property android:name="string"
           android:resource="resource specification"
           android:value="string" />
 */
class Property(
    val name: String? = null,
    val resource: String? = null,
    val value: String? = null,
)