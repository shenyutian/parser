package org.syt.parser.entry


/*
 * zhulei 2024/8/9-上午10:17
 *
 * <queries>
    <package android:name="string" />
    <intent>
        ...
    </intent>
    <provider android:authorities="list" />
    ...
</queries>
*
 */
data class Queries(
    val packageName: MutableList<String> = mutableListOf(),
    val intent: MutableList<IntentFilter> = mutableListOf(),
    val provider: MutableList<String> = mutableListOf(),
) {
    fun addPackageName(packageName: String) {
        this.packageName.add(packageName)
    }

    fun addIntent(intent: IntentFilter) {
        this.intent.add(intent)
    }

    fun addProvider(provider: String) {
        this.provider.add(provider)
    }
}